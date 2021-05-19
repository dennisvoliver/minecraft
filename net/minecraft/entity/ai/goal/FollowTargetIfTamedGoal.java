package net.minecraft.entity.ai.goal;

import java.util.function.Predicate;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.TameableEntity;
import org.jetbrains.annotations.Nullable;

public class FollowTargetIfTamedGoal<T extends LivingEntity> extends FollowTargetGoal<T> {
   private final TameableEntity tameable;

   public FollowTargetIfTamedGoal(TameableEntity tameable, Class<T> targetClass, boolean checkVisibility, @Nullable Predicate<LivingEntity> targetPredicate) {
      super(tameable, targetClass, 10, checkVisibility, false, targetPredicate);
      this.tameable = tameable;
   }

   public boolean canStart() {
      return !this.tameable.isTamed() && super.canStart();
   }

   public boolean shouldContinue() {
      return this.targetPredicate != null ? this.targetPredicate.test(this.mob, this.targetEntity) : super.shouldContinue();
   }
}
