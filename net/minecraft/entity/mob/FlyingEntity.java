package net.minecraft.entity.mob;

import net.minecraft.block.BlockState;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.MovementType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public abstract class FlyingEntity extends MobEntity {
   protected FlyingEntity(EntityType<? extends FlyingEntity> entityType, World world) {
      super(entityType, world);
   }

   public boolean handleFallDamage(float fallDistance, float damageMultiplier) {
      return false;
   }

   protected void fall(double heightDifference, boolean onGround, BlockState landedState, BlockPos landedPosition) {
   }

   public void travel(Vec3d movementInput) {
      if (this.isTouchingWater()) {
         this.updateVelocity(0.02F, movementInput);
         this.move(MovementType.SELF, this.getVelocity());
         this.setVelocity(this.getVelocity().multiply(0.800000011920929D));
      } else if (this.isInLava()) {
         this.updateVelocity(0.02F, movementInput);
         this.move(MovementType.SELF, this.getVelocity());
         this.setVelocity(this.getVelocity().multiply(0.5D));
      } else {
         float f = 0.91F;
         if (this.onGround) {
            f = this.world.getBlockState(new BlockPos(this.getX(), this.getY() - 1.0D, this.getZ())).getBlock().getSlipperiness() * 0.91F;
         }

         float g = 0.16277137F / (f * f * f);
         f = 0.91F;
         if (this.onGround) {
            f = this.world.getBlockState(new BlockPos(this.getX(), this.getY() - 1.0D, this.getZ())).getBlock().getSlipperiness() * 0.91F;
         }

         this.updateVelocity(this.onGround ? 0.1F * g : 0.02F, movementInput);
         this.move(MovementType.SELF, this.getVelocity());
         this.setVelocity(this.getVelocity().multiply((double)f));
      }

      this.method_29242(this, false);
   }

   public boolean isClimbing() {
      return false;
   }
}
