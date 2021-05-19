package net.minecraft.entity.projectile;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.AbstractBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.passive.LlamaEntity;
import net.minecraft.network.Packet;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class LlamaSpitEntity extends ProjectileEntity {
   public LlamaSpitEntity(EntityType<? extends LlamaSpitEntity> entityType, World world) {
      super(entityType, world);
   }

   public LlamaSpitEntity(World world, LlamaEntity owner) {
      this(EntityType.LLAMA_SPIT, world);
      super.setOwner(owner);
      this.updatePosition(owner.getX() - (double)(owner.getWidth() + 1.0F) * 0.5D * (double)MathHelper.sin(owner.bodyYaw * 0.017453292F), owner.getEyeY() - 0.10000000149011612D, owner.getZ() + (double)(owner.getWidth() + 1.0F) * 0.5D * (double)MathHelper.cos(owner.bodyYaw * 0.017453292F));
   }

   @Environment(EnvType.CLIENT)
   public LlamaSpitEntity(World world, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
      this(EntityType.LLAMA_SPIT, world);
      this.updatePosition(x, y, z);

      for(int i = 0; i < 7; ++i) {
         double d = 0.4D + 0.1D * (double)i;
         world.addParticle(ParticleTypes.SPIT, x, y, z, velocityX * d, velocityY, velocityZ * d);
      }

      this.setVelocity(velocityX, velocityY, velocityZ);
   }

   public void tick() {
      super.tick();
      Vec3d vec3d = this.getVelocity();
      HitResult hitResult = ProjectileUtil.getCollision(this, this::method_26958);
      if (hitResult != null) {
         this.onCollision(hitResult);
      }

      double d = this.getX() + vec3d.x;
      double e = this.getY() + vec3d.y;
      double f = this.getZ() + vec3d.z;
      this.method_26962();
      float g = 0.99F;
      float h = 0.06F;
      if (this.world.method_29546(this.getBoundingBox()).noneMatch(AbstractBlock.AbstractBlockState::isAir)) {
         this.remove();
      } else if (this.isInsideWaterOrBubbleColumn()) {
         this.remove();
      } else {
         this.setVelocity(vec3d.multiply(0.9900000095367432D));
         if (!this.hasNoGravity()) {
            this.setVelocity(this.getVelocity().add(0.0D, -0.05999999865889549D, 0.0D));
         }

         this.updatePosition(d, e, f);
      }
   }

   protected void onEntityHit(EntityHitResult entityHitResult) {
      super.onEntityHit(entityHitResult);
      Entity entity = this.getOwner();
      if (entity instanceof LivingEntity) {
         entityHitResult.getEntity().damage(DamageSource.mobProjectile(this, (LivingEntity)entity).setProjectile(), 1.0F);
      }

   }

   protected void onBlockHit(BlockHitResult blockHitResult) {
      super.onBlockHit(blockHitResult);
      if (!this.world.isClient) {
         this.remove();
      }

   }

   protected void initDataTracker() {
   }

   public Packet<?> createSpawnPacket() {
      return new EntitySpawnS2CPacket(this);
   }
}
