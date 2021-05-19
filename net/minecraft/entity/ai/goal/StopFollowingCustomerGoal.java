package net.minecraft.entity.ai.goal;

import java.util.EnumSet;
import net.minecraft.entity.passive.MerchantEntity;
import net.minecraft.entity.player.PlayerEntity;

public class StopFollowingCustomerGoal extends Goal {
   private final MerchantEntity merchant;

   public StopFollowingCustomerGoal(MerchantEntity merchant) {
      this.merchant = merchant;
      this.setControls(EnumSet.of(Goal.Control.JUMP, Goal.Control.MOVE));
   }

   public boolean canStart() {
      if (!this.merchant.isAlive()) {
         return false;
      } else if (this.merchant.isTouchingWater()) {
         return false;
      } else if (!this.merchant.isOnGround()) {
         return false;
      } else if (this.merchant.velocityModified) {
         return false;
      } else {
         PlayerEntity playerEntity = this.merchant.getCurrentCustomer();
         if (playerEntity == null) {
            return false;
         } else if (this.merchant.squaredDistanceTo(playerEntity) > 16.0D) {
            return false;
         } else {
            return playerEntity.currentScreenHandler != null;
         }
      }
   }

   public void start() {
      this.merchant.getNavigation().stop();
   }

   public void stop() {
      this.merchant.setCurrentCustomer((PlayerEntity)null);
   }
}
