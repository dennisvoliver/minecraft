package net.minecraft.entity.ai.goal;

import java.util.EnumSet;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.world.BlockView;

public class AttackGoal extends Goal {
   private final BlockView world;
   private final MobEntity mob;
   private LivingEntity target;
   private int cooldown;

   public AttackGoal(MobEntity mob) {
      this.mob = mob;
      this.world = mob.world;
      this.setControls(EnumSet.of(Goal.Control.MOVE, Goal.Control.LOOK));
   }

   public boolean canStart() {
      LivingEntity livingEntity = this.mob.getTarget();
      if (livingEntity == null) {
         return false;
      } else {
         this.target = livingEntity;
         return true;
      }
   }

   public boolean shouldContinue() {
      if (!this.target.isAlive()) {
         return false;
      } else if (this.mob.squaredDistanceTo(this.target) > 225.0D) {
         return false;
      } else {
         return !this.mob.getNavigation().isIdle() || this.canStart();
      }
   }

   public void stop() {
      this.target = null;
      this.mob.getNavigation().stop();
   }

   public void tick() {
      this.mob.getLookControl().lookAt(this.target, 30.0F, 30.0F);
      double d = (double)(this.mob.getWidth() * 2.0F * this.mob.getWidth() * 2.0F);
      double e = this.mob.squaredDistanceTo(this.target.getX(), this.target.getY(), this.target.getZ());
      double f = 0.8D;
      if (e > d && e < 16.0D) {
         f = 1.33D;
      } else if (e < 225.0D) {
         f = 0.6D;
      }

      this.mob.getNavigation().startMovingTo(this.target, f);
      this.cooldown = Math.max(this.cooldown - 1, 0);
      if (!(e > d)) {
         if (this.cooldown <= 0) {
            this.cooldown = 20;
            this.mob.tryAttack(this.target);
         }
      }
   }
}
