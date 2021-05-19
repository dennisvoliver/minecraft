package net.minecraft.entity.ai.goal;

import java.util.function.Predicate;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.raid.RaiderEntity;
import org.jetbrains.annotations.Nullable;

public class RaidGoal<T extends LivingEntity> extends FollowTargetGoal<T> {
   private int cooldown = 0;

   public RaidGoal(RaiderEntity raider, Class<T> targetEntityClass, boolean checkVisibility, @Nullable Predicate<LivingEntity> targetPredicate) {
      super(raider, targetEntityClass, 500, checkVisibility, false, targetPredicate);
   }

   public int getCooldown() {
      return this.cooldown;
   }

   public void decreaseCooldown() {
      --this.cooldown;
   }

   public boolean canStart() {
      if (this.cooldown <= 0 && this.mob.getRandom().nextBoolean()) {
         if (!((RaiderEntity)this.mob).hasActiveRaid()) {
            return false;
         } else {
            this.findClosestTarget();
            return this.targetEntity != null;
         }
      } else {
         return false;
      }
   }

   public void start() {
      this.cooldown = 200;
      super.start();
   }
}
