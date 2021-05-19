package net.minecraft.entity.ai.goal;

import java.util.EnumSet;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.CreeperEntity;

public class CreeperIgniteGoal extends Goal {
   private final CreeperEntity creeper;
   private LivingEntity target;

   public CreeperIgniteGoal(CreeperEntity creeper) {
      this.creeper = creeper;
      this.setControls(EnumSet.of(Goal.Control.MOVE));
   }

   public boolean canStart() {
      LivingEntity livingEntity = this.creeper.getTarget();
      return this.creeper.getFuseSpeed() > 0 || livingEntity != null && this.creeper.squaredDistanceTo(livingEntity) < 9.0D;
   }

   public void start() {
      this.creeper.getNavigation().stop();
      this.target = this.creeper.getTarget();
   }

   public void stop() {
      this.target = null;
   }

   public void tick() {
      if (this.target == null) {
         this.creeper.setFuseSpeed(-1);
      } else if (this.creeper.squaredDistanceTo(this.target) > 49.0D) {
         this.creeper.setFuseSpeed(-1);
      } else if (!this.creeper.getVisibilityCache().canSee(this.target)) {
         this.creeper.setFuseSpeed(-1);
      } else {
         this.creeper.setFuseSpeed(1);
      }
   }
}
