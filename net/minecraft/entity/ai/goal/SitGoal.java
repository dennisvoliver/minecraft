package net.minecraft.entity.ai.goal;

import java.util.EnumSet;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.TameableEntity;

public class SitGoal extends Goal {
   private final TameableEntity tameable;

   public SitGoal(TameableEntity tameable) {
      this.tameable = tameable;
      this.setControls(EnumSet.of(Goal.Control.JUMP, Goal.Control.MOVE));
   }

   public boolean shouldContinue() {
      return this.tameable.isSitting();
   }

   public boolean canStart() {
      if (!this.tameable.isTamed()) {
         return false;
      } else if (this.tameable.isInsideWaterOrBubbleColumn()) {
         return false;
      } else if (!this.tameable.isOnGround()) {
         return false;
      } else {
         LivingEntity livingEntity = this.tameable.getOwner();
         if (livingEntity == null) {
            return true;
         } else {
            return this.tameable.squaredDistanceTo(livingEntity) < 144.0D && livingEntity.getAttacker() != null ? false : this.tameable.isSitting();
         }
      }
   }

   public void start() {
      this.tameable.getNavigation().stop();
      this.tameable.setInSittingPose(true);
   }

   public void stop() {
      this.tameable.setInSittingPose(false);
   }
}
