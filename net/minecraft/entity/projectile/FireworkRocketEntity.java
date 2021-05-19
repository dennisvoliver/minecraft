package net.minecraft.entity.projectile;

import java.util.Iterator;
import java.util.List;
import java.util.OptionalInt;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.api.EnvironmentInterface;
import net.fabricmc.api.EnvironmentInterfaces;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.FlyingItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.Packet;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

@EnvironmentInterfaces({@EnvironmentInterface(
   value = EnvType.CLIENT,
   itf = FlyingItemEntity.class
)})
public class FireworkRocketEntity extends ProjectileEntity implements FlyingItemEntity {
   private static final TrackedData<ItemStack> ITEM;
   private static final TrackedData<OptionalInt> SHOOTER_ENTITY_ID;
   private static final TrackedData<Boolean> SHOT_AT_ANGLE;
   private int life;
   private int lifeTime;
   private LivingEntity shooter;

   public FireworkRocketEntity(EntityType<? extends FireworkRocketEntity> entityType, World world) {
      super(entityType, world);
   }

   public FireworkRocketEntity(World world, double x, double y, double z, ItemStack stack) {
      super(EntityType.FIREWORK_ROCKET, world);
      this.life = 0;
      this.updatePosition(x, y, z);
      int i = 1;
      if (!stack.isEmpty() && stack.hasTag()) {
         this.dataTracker.set(ITEM, stack.copy());
         i += stack.getOrCreateSubTag("Fireworks").getByte("Flight");
      }

      this.setVelocity(this.random.nextGaussian() * 0.001D, 0.05D, this.random.nextGaussian() * 0.001D);
      this.lifeTime = 10 * i + this.random.nextInt(6) + this.random.nextInt(7);
   }

   public FireworkRocketEntity(World world, @Nullable Entity entity, double x, double y, double z, ItemStack stack) {
      this(world, x, y, z, stack);
      this.setOwner(entity);
   }

   public FireworkRocketEntity(World world, ItemStack stack, LivingEntity shooter) {
      this(world, shooter, shooter.getX(), shooter.getY(), shooter.getZ(), stack);
      this.dataTracker.set(SHOOTER_ENTITY_ID, OptionalInt.of(shooter.getEntityId()));
      this.shooter = shooter;
   }

   public FireworkRocketEntity(World world, ItemStack stack, double x, double y, double z, boolean shotAtAngle) {
      this(world, x, y, z, stack);
      this.dataTracker.set(SHOT_AT_ANGLE, shotAtAngle);
   }

   public FireworkRocketEntity(World world, ItemStack stack, Entity entity, double x, double y, double z, boolean shotAtAngle) {
      this(world, stack, x, y, z, shotAtAngle);
      this.setOwner(entity);
   }

   protected void initDataTracker() {
      this.dataTracker.startTracking(ITEM, ItemStack.EMPTY);
      this.dataTracker.startTracking(SHOOTER_ENTITY_ID, OptionalInt.empty());
      this.dataTracker.startTracking(SHOT_AT_ANGLE, false);
   }

   @Environment(EnvType.CLIENT)
   public boolean shouldRender(double distance) {
      return distance < 4096.0D && !this.wasShotByEntity();
   }

   @Environment(EnvType.CLIENT)
   public boolean shouldRender(double cameraX, double cameraY, double cameraZ) {
      return super.shouldRender(cameraX, cameraY, cameraZ) && !this.wasShotByEntity();
   }

   public void tick() {
      super.tick();
      Vec3d vec3d;
      if (this.wasShotByEntity()) {
         if (this.shooter == null) {
            ((OptionalInt)this.dataTracker.get(SHOOTER_ENTITY_ID)).ifPresent((i) -> {
               Entity entity = this.world.getEntityById(i);
               if (entity instanceof LivingEntity) {
                  this.shooter = (LivingEntity)entity;
               }

            });
         }

         if (this.shooter != null) {
            if (this.shooter.isFallFlying()) {
               vec3d = this.shooter.getRotationVector();
               double d = 1.5D;
               double e = 0.1D;
               Vec3d vec3d2 = this.shooter.getVelocity();
               this.shooter.setVelocity(vec3d2.add(vec3d.x * 0.1D + (vec3d.x * 1.5D - vec3d2.x) * 0.5D, vec3d.y * 0.1D + (vec3d.y * 1.5D - vec3d2.y) * 0.5D, vec3d.z * 0.1D + (vec3d.z * 1.5D - vec3d2.z) * 0.5D));
            }

            this.updatePosition(this.shooter.getX(), this.shooter.getY(), this.shooter.getZ());
            this.setVelocity(this.shooter.getVelocity());
         }
      } else {
         if (!this.wasShotAtAngle()) {
            double f = this.horizontalCollision ? 1.0D : 1.15D;
            this.setVelocity(this.getVelocity().multiply(f, 1.0D, f).add(0.0D, 0.04D, 0.0D));
         }

         vec3d = this.getVelocity();
         this.move(MovementType.SELF, vec3d);
         this.setVelocity(vec3d);
      }

      HitResult hitResult = ProjectileUtil.getCollision(this, this::method_26958);
      if (!this.noClip) {
         this.onCollision(hitResult);
         this.velocityDirty = true;
      }

      this.method_26962();
      if (this.life == 0 && !this.isSilent()) {
         this.world.playSound((PlayerEntity)null, this.getX(), this.getY(), this.getZ(), SoundEvents.ENTITY_FIREWORK_ROCKET_LAUNCH, SoundCategory.AMBIENT, 3.0F, 1.0F);
      }

      ++this.life;
      if (this.world.isClient && this.life % 2 < 2) {
         this.world.addParticle(ParticleTypes.FIREWORK, this.getX(), this.getY() - 0.3D, this.getZ(), this.random.nextGaussian() * 0.05D, -this.getVelocity().y * 0.5D, this.random.nextGaussian() * 0.05D);
      }

      if (!this.world.isClient && this.life > this.lifeTime) {
         this.explodeAndRemove();
      }

   }

   private void explodeAndRemove() {
      this.world.sendEntityStatus(this, (byte)17);
      this.explode();
      this.remove();
   }

   protected void onEntityHit(EntityHitResult entityHitResult) {
      super.onEntityHit(entityHitResult);
      if (!this.world.isClient) {
         this.explodeAndRemove();
      }
   }

   protected void onBlockHit(BlockHitResult blockHitResult) {
      BlockPos blockPos = new BlockPos(blockHitResult.getBlockPos());
      this.world.getBlockState(blockPos).onEntityCollision(this.world, blockPos, this);
      if (!this.world.isClient() && this.hasExplosionEffects()) {
         this.explodeAndRemove();
      }

      super.onBlockHit(blockHitResult);
   }

   private boolean hasExplosionEffects() {
      ItemStack itemStack = (ItemStack)this.dataTracker.get(ITEM);
      CompoundTag compoundTag = itemStack.isEmpty() ? null : itemStack.getSubTag("Fireworks");
      ListTag listTag = compoundTag != null ? compoundTag.getList("Explosions", 10) : null;
      return listTag != null && !listTag.isEmpty();
   }

   private void explode() {
      float f = 0.0F;
      ItemStack itemStack = (ItemStack)this.dataTracker.get(ITEM);
      CompoundTag compoundTag = itemStack.isEmpty() ? null : itemStack.getSubTag("Fireworks");
      ListTag listTag = compoundTag != null ? compoundTag.getList("Explosions", 10) : null;
      if (listTag != null && !listTag.isEmpty()) {
         f = 5.0F + (float)(listTag.size() * 2);
      }

      if (f > 0.0F) {
         if (this.shooter != null) {
            this.shooter.damage(DamageSource.firework(this, this.getOwner()), 5.0F + (float)(listTag.size() * 2));
         }

         double d = 5.0D;
         Vec3d vec3d = this.getPos();
         List<LivingEntity> list = this.world.getNonSpectatingEntities(LivingEntity.class, this.getBoundingBox().expand(5.0D));
         Iterator var9 = list.iterator();

         while(true) {
            LivingEntity livingEntity;
            do {
               do {
                  if (!var9.hasNext()) {
                     return;
                  }

                  livingEntity = (LivingEntity)var9.next();
               } while(livingEntity == this.shooter);
            } while(this.squaredDistanceTo(livingEntity) > 25.0D);

            boolean bl = false;

            for(int i = 0; i < 2; ++i) {
               Vec3d vec3d2 = new Vec3d(livingEntity.getX(), livingEntity.getBodyY(0.5D * (double)i), livingEntity.getZ());
               HitResult hitResult = this.world.raycast(new RaycastContext(vec3d, vec3d2, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, this));
               if (hitResult.getType() == HitResult.Type.MISS) {
                  bl = true;
                  break;
               }
            }

            if (bl) {
               float g = f * (float)Math.sqrt((5.0D - (double)this.distanceTo(livingEntity)) / 5.0D);
               livingEntity.damage(DamageSource.firework(this, this.getOwner()), g);
            }
         }
      }
   }

   private boolean wasShotByEntity() {
      return ((OptionalInt)this.dataTracker.get(SHOOTER_ENTITY_ID)).isPresent();
   }

   public boolean wasShotAtAngle() {
      return (Boolean)this.dataTracker.get(SHOT_AT_ANGLE);
   }

   @Environment(EnvType.CLIENT)
   public void handleStatus(byte status) {
      if (status == 17 && this.world.isClient) {
         if (!this.hasExplosionEffects()) {
            for(int i = 0; i < this.random.nextInt(3) + 2; ++i) {
               this.world.addParticle(ParticleTypes.POOF, this.getX(), this.getY(), this.getZ(), this.random.nextGaussian() * 0.05D, 0.005D, this.random.nextGaussian() * 0.05D);
            }
         } else {
            ItemStack itemStack = (ItemStack)this.dataTracker.get(ITEM);
            CompoundTag compoundTag = itemStack.isEmpty() ? null : itemStack.getSubTag("Fireworks");
            Vec3d vec3d = this.getVelocity();
            this.world.addFireworkParticle(this.getX(), this.getY(), this.getZ(), vec3d.x, vec3d.y, vec3d.z, compoundTag);
         }
      }

      super.handleStatus(status);
   }

   public void writeCustomDataToTag(CompoundTag tag) {
      super.writeCustomDataToTag(tag);
      tag.putInt("Life", this.life);
      tag.putInt("LifeTime", this.lifeTime);
      ItemStack itemStack = (ItemStack)this.dataTracker.get(ITEM);
      if (!itemStack.isEmpty()) {
         tag.put("FireworksItem", itemStack.toTag(new CompoundTag()));
      }

      tag.putBoolean("ShotAtAngle", (Boolean)this.dataTracker.get(SHOT_AT_ANGLE));
   }

   public void readCustomDataFromTag(CompoundTag tag) {
      super.readCustomDataFromTag(tag);
      this.life = tag.getInt("Life");
      this.lifeTime = tag.getInt("LifeTime");
      ItemStack itemStack = ItemStack.fromTag(tag.getCompound("FireworksItem"));
      if (!itemStack.isEmpty()) {
         this.dataTracker.set(ITEM, itemStack);
      }

      if (tag.contains("ShotAtAngle")) {
         this.dataTracker.set(SHOT_AT_ANGLE, tag.getBoolean("ShotAtAngle"));
      }

   }

   @Environment(EnvType.CLIENT)
   public ItemStack getStack() {
      ItemStack itemStack = (ItemStack)this.dataTracker.get(ITEM);
      return itemStack.isEmpty() ? new ItemStack(Items.FIREWORK_ROCKET) : itemStack;
   }

   public boolean isAttackable() {
      return false;
   }

   public Packet<?> createSpawnPacket() {
      return new EntitySpawnS2CPacket(this);
   }

   static {
      ITEM = DataTracker.registerData(FireworkRocketEntity.class, TrackedDataHandlerRegistry.ITEM_STACK);
      SHOOTER_ENTITY_ID = DataTracker.registerData(FireworkRocketEntity.class, TrackedDataHandlerRegistry.FIREWORK_DATA);
      SHOT_AT_ANGLE = DataTracker.registerData(FireworkRocketEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
   }
}
