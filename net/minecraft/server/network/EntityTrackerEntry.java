package net.minecraft.server.network;

import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.decoration.ItemFrameEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.FilledMapItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.map.MapState;
import net.minecraft.network.Packet;
import net.minecraft.network.packet.s2c.play.EntityAttachS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityAttributesS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityEquipmentUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityPassengersSetS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityPositionS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityS2CPacket;
import net.minecraft.network.packet.s2c.play.EntitySetHeadYawS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityStatusEffectS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityTrackerUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.MobSpawnS2CPacket;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class EntityTrackerEntry {
   private static final Logger LOGGER = LogManager.getLogger();
   private final ServerWorld world;
   private final Entity entity;
   private final int tickInterval;
   private final boolean alwaysUpdateVelocity;
   private final Consumer<Packet<?>> receiver;
   private long lastX;
   private long lastY;
   private long lastZ;
   private int lastYaw;
   private int lastPitch;
   private int lastHeadPitch;
   private Vec3d velocity;
   private int trackingTick;
   private int updatesWithoutVehicle;
   private List<Entity> lastPassengers;
   private boolean hadVehicle;
   private boolean lastOnGround;

   public EntityTrackerEntry(ServerWorld world, Entity entity, int tickInterval, boolean alwaysUpdateVelocity, Consumer<Packet<?>> receiver) {
      this.velocity = Vec3d.ZERO;
      this.lastPassengers = Collections.emptyList();
      this.world = world;
      this.receiver = receiver;
      this.entity = entity;
      this.tickInterval = tickInterval;
      this.alwaysUpdateVelocity = alwaysUpdateVelocity;
      this.storeEncodedCoordinates();
      this.lastYaw = MathHelper.floor(entity.yaw * 256.0F / 360.0F);
      this.lastPitch = MathHelper.floor(entity.pitch * 256.0F / 360.0F);
      this.lastHeadPitch = MathHelper.floor(entity.getHeadYaw() * 256.0F / 360.0F);
      this.lastOnGround = entity.isOnGround();
   }

   public void tick() {
      List<Entity> list = this.entity.getPassengerList();
      if (!list.equals(this.lastPassengers)) {
         this.lastPassengers = list;
         this.receiver.accept(new EntityPassengersSetS2CPacket(this.entity));
      }

      if (this.entity instanceof ItemFrameEntity && this.trackingTick % 10 == 0) {
         ItemFrameEntity itemFrameEntity = (ItemFrameEntity)this.entity;
         ItemStack itemStack = itemFrameEntity.getHeldItemStack();
         if (itemStack.getItem() instanceof FilledMapItem) {
            MapState mapState = FilledMapItem.getOrCreateMapState(itemStack, this.world);
            Iterator var5 = this.world.getPlayers().iterator();

            while(var5.hasNext()) {
               ServerPlayerEntity serverPlayerEntity = (ServerPlayerEntity)var5.next();
               mapState.update(serverPlayerEntity, itemStack);
               Packet<?> packet = ((FilledMapItem)itemStack.getItem()).createSyncPacket(itemStack, this.world, serverPlayerEntity);
               if (packet != null) {
                  serverPlayerEntity.networkHandler.sendPacket(packet);
               }
            }
         }

         this.syncEntityData();
      }

      if (this.trackingTick % this.tickInterval == 0 || this.entity.velocityDirty || this.entity.getDataTracker().isDirty()) {
         int k;
         int l;
         if (this.entity.hasVehicle()) {
            k = MathHelper.floor(this.entity.yaw * 256.0F / 360.0F);
            l = MathHelper.floor(this.entity.pitch * 256.0F / 360.0F);
            boolean bl = Math.abs(k - this.lastYaw) >= 1 || Math.abs(l - this.lastPitch) >= 1;
            if (bl) {
               this.receiver.accept(new EntityS2CPacket.Rotate(this.entity.getEntityId(), (byte)k, (byte)l, this.entity.isOnGround()));
               this.lastYaw = k;
               this.lastPitch = l;
            }

            this.storeEncodedCoordinates();
            this.syncEntityData();
            this.hadVehicle = true;
         } else {
            ++this.updatesWithoutVehicle;
            k = MathHelper.floor(this.entity.yaw * 256.0F / 360.0F);
            l = MathHelper.floor(this.entity.pitch * 256.0F / 360.0F);
            Vec3d vec3d = this.entity.getPos().subtract(EntityS2CPacket.decodePacketCoordinates(this.lastX, this.lastY, this.lastZ));
            boolean bl2 = vec3d.lengthSquared() >= 7.62939453125E-6D;
            Packet<?> packet2 = null;
            boolean bl3 = bl2 || this.trackingTick % 60 == 0;
            boolean bl4 = Math.abs(k - this.lastYaw) >= 1 || Math.abs(l - this.lastPitch) >= 1;
            if (this.trackingTick > 0 || this.entity instanceof PersistentProjectileEntity) {
               long m = EntityS2CPacket.encodePacketCoordinate(vec3d.x);
               long n = EntityS2CPacket.encodePacketCoordinate(vec3d.y);
               long o = EntityS2CPacket.encodePacketCoordinate(vec3d.z);
               boolean bl5 = m < -32768L || m > 32767L || n < -32768L || n > 32767L || o < -32768L || o > 32767L;
               if (!bl5 && this.updatesWithoutVehicle <= 400 && !this.hadVehicle && this.lastOnGround == this.entity.isOnGround()) {
                  if ((!bl3 || !bl4) && !(this.entity instanceof PersistentProjectileEntity)) {
                     if (bl3) {
                        packet2 = new EntityS2CPacket.MoveRelative(this.entity.getEntityId(), (short)((int)m), (short)((int)n), (short)((int)o), this.entity.isOnGround());
                     } else if (bl4) {
                        packet2 = new EntityS2CPacket.Rotate(this.entity.getEntityId(), (byte)k, (byte)l, this.entity.isOnGround());
                     }
                  } else {
                     packet2 = new EntityS2CPacket.RotateAndMoveRelative(this.entity.getEntityId(), (short)((int)m), (short)((int)n), (short)((int)o), (byte)k, (byte)l, this.entity.isOnGround());
                  }
               } else {
                  this.lastOnGround = this.entity.isOnGround();
                  this.updatesWithoutVehicle = 0;
                  packet2 = new EntityPositionS2CPacket(this.entity);
               }
            }

            if ((this.alwaysUpdateVelocity || this.entity.velocityDirty || this.entity instanceof LivingEntity && ((LivingEntity)this.entity).isFallFlying()) && this.trackingTick > 0) {
               Vec3d vec3d2 = this.entity.getVelocity();
               double d = vec3d2.squaredDistanceTo(this.velocity);
               if (d > 1.0E-7D || d > 0.0D && vec3d2.lengthSquared() == 0.0D) {
                  this.velocity = vec3d2;
                  this.receiver.accept(new EntityVelocityUpdateS2CPacket(this.entity.getEntityId(), this.velocity));
               }
            }

            if (packet2 != null) {
               this.receiver.accept(packet2);
            }

            this.syncEntityData();
            if (bl3) {
               this.storeEncodedCoordinates();
            }

            if (bl4) {
               this.lastYaw = k;
               this.lastPitch = l;
            }

            this.hadVehicle = false;
         }

         k = MathHelper.floor(this.entity.getHeadYaw() * 256.0F / 360.0F);
         if (Math.abs(k - this.lastHeadPitch) >= 1) {
            this.receiver.accept(new EntitySetHeadYawS2CPacket(this.entity, (byte)k));
            this.lastHeadPitch = k;
         }

         this.entity.velocityDirty = false;
      }

      ++this.trackingTick;
      if (this.entity.velocityModified) {
         this.sendSyncPacket(new EntityVelocityUpdateS2CPacket(this.entity));
         this.entity.velocityModified = false;
      }

   }

   public void stopTracking(ServerPlayerEntity player) {
      this.entity.onStoppedTrackingBy(player);
      player.onStoppedTracking(this.entity);
   }

   public void startTracking(ServerPlayerEntity player) {
      ServerPlayNetworkHandler var10001 = player.networkHandler;
      this.sendPackets(var10001::sendPacket);
      this.entity.onStartedTrackingBy(player);
      player.onStartedTracking(this.entity);
   }

   public void sendPackets(Consumer<Packet<?>> sender) {
      if (this.entity.removed) {
         LOGGER.warn("Fetching packet for removed entity " + this.entity);
      }

      Packet<?> packet = this.entity.createSpawnPacket();
      this.lastHeadPitch = MathHelper.floor(this.entity.getHeadYaw() * 256.0F / 360.0F);
      sender.accept(packet);
      if (!this.entity.getDataTracker().isEmpty()) {
         sender.accept(new EntityTrackerUpdateS2CPacket(this.entity.getEntityId(), this.entity.getDataTracker(), true));
      }

      boolean bl = this.alwaysUpdateVelocity;
      if (this.entity instanceof LivingEntity) {
         Collection<EntityAttributeInstance> collection = ((LivingEntity)this.entity).getAttributes().getAttributesToSend();
         if (!collection.isEmpty()) {
            sender.accept(new EntityAttributesS2CPacket(this.entity.getEntityId(), collection));
         }

         if (((LivingEntity)this.entity).isFallFlying()) {
            bl = true;
         }
      }

      this.velocity = this.entity.getVelocity();
      if (bl && !(packet instanceof MobSpawnS2CPacket)) {
         sender.accept(new EntityVelocityUpdateS2CPacket(this.entity.getEntityId(), this.velocity));
      }

      if (this.entity instanceof LivingEntity) {
         List<Pair<EquipmentSlot, ItemStack>> list = Lists.newArrayList();
         EquipmentSlot[] var5 = EquipmentSlot.values();
         int var6 = var5.length;

         for(int var7 = 0; var7 < var6; ++var7) {
            EquipmentSlot equipmentSlot = var5[var7];
            ItemStack itemStack = ((LivingEntity)this.entity).getEquippedStack(equipmentSlot);
            if (!itemStack.isEmpty()) {
               list.add(Pair.of(equipmentSlot, itemStack.copy()));
            }
         }

         if (!list.isEmpty()) {
            sender.accept(new EntityEquipmentUpdateS2CPacket(this.entity.getEntityId(), list));
         }
      }

      if (this.entity instanceof LivingEntity) {
         LivingEntity livingEntity = (LivingEntity)this.entity;
         Iterator var13 = livingEntity.getStatusEffects().iterator();

         while(var13.hasNext()) {
            StatusEffectInstance statusEffectInstance = (StatusEffectInstance)var13.next();
            sender.accept(new EntityStatusEffectS2CPacket(this.entity.getEntityId(), statusEffectInstance));
         }
      }

      if (!this.entity.getPassengerList().isEmpty()) {
         sender.accept(new EntityPassengersSetS2CPacket(this.entity));
      }

      if (this.entity.hasVehicle()) {
         sender.accept(new EntityPassengersSetS2CPacket(this.entity.getVehicle()));
      }

      if (this.entity instanceof MobEntity) {
         MobEntity mobEntity = (MobEntity)this.entity;
         if (mobEntity.isLeashed()) {
            sender.accept(new EntityAttachS2CPacket(mobEntity, mobEntity.getHoldingEntity()));
         }
      }

   }

   /**
    * Synchronizes tracked data and attributes
    */
   private void syncEntityData() {
      DataTracker dataTracker = this.entity.getDataTracker();
      if (dataTracker.isDirty()) {
         this.sendSyncPacket(new EntityTrackerUpdateS2CPacket(this.entity.getEntityId(), dataTracker, false));
      }

      if (this.entity instanceof LivingEntity) {
         Set<EntityAttributeInstance> set = ((LivingEntity)this.entity).getAttributes().getTracked();
         if (!set.isEmpty()) {
            this.sendSyncPacket(new EntityAttributesS2CPacket(this.entity.getEntityId(), set));
         }

         set.clear();
      }

   }

   /**
    * Stores the tracked entity's current coordinates encoded as lastX/Y/Z
    */
   private void storeEncodedCoordinates() {
      this.lastX = EntityS2CPacket.encodePacketCoordinate(this.entity.getX());
      this.lastY = EntityS2CPacket.encodePacketCoordinate(this.entity.getY());
      this.lastZ = EntityS2CPacket.encodePacketCoordinate(this.entity.getZ());
   }

   /**
    * Decodes lastX/Y/Z into a position vector
    */
   public Vec3d getLastPos() {
      return EntityS2CPacket.decodePacketCoordinates(this.lastX, this.lastY, this.lastZ);
   }

   /**
    * Sends a packet for synchronization with watcher and tracked player (if applicable)
    */
   private void sendSyncPacket(Packet<?> packet) {
      this.receiver.accept(packet);
      if (this.entity instanceof ServerPlayerEntity) {
         ((ServerPlayerEntity)this.entity).networkHandler.sendPacket(packet);
      }

   }
}
