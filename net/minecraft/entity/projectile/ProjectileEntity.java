package net.minecraft.entity.projectile;

import java.util.Iterator;
import java.util.UUID;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public abstract class ProjectileEntity extends Entity {
   private UUID ownerUuid;
   private int ownerEntityId;
   private boolean leftOwner;

   ProjectileEntity(EntityType<? extends ProjectileEntity> entityType, World world) {
      super(entityType, world);
   }

   public void setOwner(@Nullable Entity entity) {
      if (entity != null) {
         this.ownerUuid = entity.getUuid();
         this.ownerEntityId = entity.getEntityId();
      }

   }

   @Nullable
   public Entity getOwner() {
      if (this.ownerUuid != null && this.world instanceof ServerWorld) {
         return ((ServerWorld)this.world).getEntity(this.ownerUuid);
      } else {
         return this.ownerEntityId != 0 ? this.world.getEntityById(this.ownerEntityId) : null;
      }
   }

   protected void writeCustomDataToTag(CompoundTag tag) {
      if (this.ownerUuid != null) {
         tag.putUuid("Owner", this.ownerUuid);
      }

      if (this.leftOwner) {
         tag.putBoolean("LeftOwner", true);
      }

   }

   protected void readCustomDataFromTag(CompoundTag tag) {
      if (tag.containsUuid("Owner")) {
         this.ownerUuid = tag.getUuid("Owner");
      }

      this.leftOwner = tag.getBoolean("LeftOwner");
   }

   public void tick() {
      if (!this.leftOwner) {
         this.leftOwner = this.method_26961();
      }

      super.tick();
   }

   private boolean method_26961() {
      Entity entity = this.getOwner();
      if (entity != null) {
         Iterator var2 = this.world.getOtherEntities(this, this.getBoundingBox().stretch(this.getVelocity()).expand(1.0D), (entityx) -> {
            return !entityx.isSpectator() && entityx.collides();
         }).iterator();

         while(var2.hasNext()) {
            Entity entity2 = (Entity)var2.next();
            if (entity2.getRootVehicle() == entity.getRootVehicle()) {
               return false;
            }
         }
      }

      return true;
   }

   public void setVelocity(double x, double y, double z, float speed, float divergence) {
      Vec3d vec3d = (new Vec3d(x, y, z)).normalize().add(this.random.nextGaussian() * 0.007499999832361937D * (double)divergence, this.random.nextGaussian() * 0.007499999832361937D * (double)divergence, this.random.nextGaussian() * 0.007499999832361937D * (double)divergence).multiply((double)speed);
      this.setVelocity(vec3d);
      float f = MathHelper.sqrt(squaredHorizontalLength(vec3d));
      this.yaw = (float)(MathHelper.atan2(vec3d.x, vec3d.z) * 57.2957763671875D);
      this.pitch = (float)(MathHelper.atan2(vec3d.y, (double)f) * 57.2957763671875D);
      this.prevYaw = this.yaw;
      this.prevPitch = this.pitch;
   }

   public void setProperties(Entity user, float pitch, float yaw, float roll, float modifierZ, float modifierXYZ) {
      float f = -MathHelper.sin(yaw * 0.017453292F) * MathHelper.cos(pitch * 0.017453292F);
      float g = -MathHelper.sin((pitch + roll) * 0.017453292F);
      float h = MathHelper.cos(yaw * 0.017453292F) * MathHelper.cos(pitch * 0.017453292F);
      this.setVelocity((double)f, (double)g, (double)h, modifierZ, modifierXYZ);
      Vec3d vec3d = user.getVelocity();
      this.setVelocity(this.getVelocity().add(vec3d.x, user.isOnGround() ? 0.0D : vec3d.y, vec3d.z));
   }

   protected void onCollision(HitResult hitResult) {
      HitResult.Type type = hitResult.getType();
      if (type == HitResult.Type.ENTITY) {
         this.onEntityHit((EntityHitResult)hitResult);
      } else if (type == HitResult.Type.BLOCK) {
         this.onBlockHit((BlockHitResult)hitResult);
      }

   }

   protected void onEntityHit(EntityHitResult entityHitResult) {
   }

   protected void onBlockHit(BlockHitResult blockHitResult) {
      BlockState blockState = this.world.getBlockState(blockHitResult.getBlockPos());
      blockState.onProjectileHit(this.world, blockState, blockHitResult, this);
   }

   @Environment(EnvType.CLIENT)
   public void setVelocityClient(double x, double y, double z) {
      this.setVelocity(x, y, z);
      if (this.prevPitch == 0.0F && this.prevYaw == 0.0F) {
         float f = MathHelper.sqrt(x * x + z * z);
         this.pitch = (float)(MathHelper.atan2(y, (double)f) * 57.2957763671875D);
         this.yaw = (float)(MathHelper.atan2(x, z) * 57.2957763671875D);
         this.prevPitch = this.pitch;
         this.prevYaw = this.yaw;
         this.refreshPositionAndAngles(this.getX(), this.getY(), this.getZ(), this.yaw, this.pitch);
      }

   }

   protected boolean method_26958(Entity entity) {
      if (!entity.isSpectator() && entity.isAlive() && entity.collides()) {
         Entity entity2 = this.getOwner();
         return entity2 == null || this.leftOwner || !entity2.isConnectedThroughVehicle(entity);
      } else {
         return false;
      }
   }

   protected void method_26962() {
      Vec3d vec3d = this.getVelocity();
      float f = MathHelper.sqrt(squaredHorizontalLength(vec3d));
      this.pitch = updateRotation(this.prevPitch, (float)(MathHelper.atan2(vec3d.y, (double)f) * 57.2957763671875D));
      this.yaw = updateRotation(this.prevYaw, (float)(MathHelper.atan2(vec3d.x, vec3d.z) * 57.2957763671875D));
   }

   protected static float updateRotation(float f, float g) {
      while(g - f < -180.0F) {
         f -= 360.0F;
      }

      while(g - f >= 180.0F) {
         f += 360.0F;
      }

      return MathHelper.lerp(0.2F, f, g);
   }
}
