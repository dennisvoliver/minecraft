package net.minecraft.entity.ai.goal;

import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.DolphinEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.sound.SoundEvents;
import net.minecraft.tag.FluidTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class DolphinJumpGoal extends DiveJumpingGoal {
   private static final int[] OFFSET_MULTIPLIERS = new int[]{0, 1, 4, 5, 6, 7};
   private final DolphinEntity dolphin;
   private final int chance;
   private boolean inWater;

   public DolphinJumpGoal(DolphinEntity dolphin, int chance) {
      this.dolphin = dolphin;
      this.chance = chance;
   }

   public boolean canStart() {
      if (this.dolphin.getRandom().nextInt(this.chance) != 0) {
         return false;
      } else {
         Direction direction = this.dolphin.getMovementDirection();
         int i = direction.getOffsetX();
         int j = direction.getOffsetZ();
         BlockPos blockPos = this.dolphin.getBlockPos();
         int[] var5 = OFFSET_MULTIPLIERS;
         int var6 = var5.length;

         for(int var7 = 0; var7 < var6; ++var7) {
            int k = var5[var7];
            if (!this.isWater(blockPos, i, j, k) || !this.isAirAbove(blockPos, i, j, k)) {
               return false;
            }
         }

         return true;
      }
   }

   private boolean isWater(BlockPos pos, int xOffset, int zOffset, int multiplier) {
      BlockPos blockPos = pos.add(xOffset * multiplier, 0, zOffset * multiplier);
      return this.dolphin.world.getFluidState(blockPos).isIn(FluidTags.WATER) && !this.dolphin.world.getBlockState(blockPos).getMaterial().blocksMovement();
   }

   private boolean isAirAbove(BlockPos pos, int xOffset, int zOffset, int multiplier) {
      return this.dolphin.world.getBlockState(pos.add(xOffset * multiplier, 1, zOffset * multiplier)).isAir() && this.dolphin.world.getBlockState(pos.add(xOffset * multiplier, 2, zOffset * multiplier)).isAir();
   }

   public boolean shouldContinue() {
      double d = this.dolphin.getVelocity().y;
      return (!(d * d < 0.029999999329447746D) || this.dolphin.pitch == 0.0F || !(Math.abs(this.dolphin.pitch) < 10.0F) || !this.dolphin.isTouchingWater()) && !this.dolphin.isOnGround();
   }

   public boolean canStop() {
      return false;
   }

   public void start() {
      Direction direction = this.dolphin.getMovementDirection();
      this.dolphin.setVelocity(this.dolphin.getVelocity().add((double)direction.getOffsetX() * 0.6D, 0.7D, (double)direction.getOffsetZ() * 0.6D));
      this.dolphin.getNavigation().stop();
   }

   public void stop() {
      this.dolphin.pitch = 0.0F;
   }

   public void tick() {
      boolean bl = this.inWater;
      if (!bl) {
         FluidState fluidState = this.dolphin.world.getFluidState(this.dolphin.getBlockPos());
         this.inWater = fluidState.isIn(FluidTags.WATER);
      }

      if (this.inWater && !bl) {
         this.dolphin.playSound(SoundEvents.ENTITY_DOLPHIN_JUMP, 1.0F, 1.0F);
      }

      Vec3d vec3d = this.dolphin.getVelocity();
      if (vec3d.y * vec3d.y < 0.029999999329447746D && this.dolphin.pitch != 0.0F) {
         this.dolphin.pitch = MathHelper.lerpAngle(this.dolphin.pitch, 0.0F, 0.2F);
      } else {
         double d = Math.sqrt(Entity.squaredHorizontalLength(vec3d));
         double e = Math.signum(-vec3d.y) * Math.acos(d / vec3d.length()) * 57.2957763671875D;
         this.dolphin.pitch = (float)e;
      }

   }
}
