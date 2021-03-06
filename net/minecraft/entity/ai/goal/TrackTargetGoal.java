package net.minecraft.entity.ai.goal;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.TargetPredicate;
import net.minecraft.entity.ai.pathing.Path;
import net.minecraft.entity.ai.pathing.PathNode;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.scoreboard.AbstractTeam;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;

public abstract class TrackTargetGoal extends Goal {
   protected final MobEntity mob;
   protected final boolean checkVisibility;
   private final boolean checkCanNavigate;
   private int canNavigateFlag;
   private int checkCanNavigateCooldown;
   private int timeWithoutVisibility;
   protected LivingEntity target;
   protected int maxTimeWithoutVisibility;

   public TrackTargetGoal(MobEntity mob, boolean checkVisibility) {
      this(mob, checkVisibility, false);
   }

   public TrackTargetGoal(MobEntity mob, boolean checkVisibility, boolean checkNavigable) {
      this.maxTimeWithoutVisibility = 60;
      this.mob = mob;
      this.checkVisibility = checkVisibility;
      this.checkCanNavigate = checkNavigable;
   }

   public boolean shouldContinue() {
      LivingEntity livingEntity = this.mob.getTarget();
      if (livingEntity == null) {
         livingEntity = this.target;
      }

      if (livingEntity == null) {
         return false;
      } else if (!livingEntity.isAlive()) {
         return false;
      } else {
         AbstractTeam abstractTeam = this.mob.getScoreboardTeam();
         AbstractTeam abstractTeam2 = livingEntity.getScoreboardTeam();
         if (abstractTeam != null && abstractTeam2 == abstractTeam) {
            return false;
         } else {
            double d = this.getFollowRange();
            if (this.mob.squaredDistanceTo(livingEntity) > d * d) {
               return false;
            } else {
               if (this.checkVisibility) {
                  if (this.mob.getVisibilityCache().canSee(livingEntity)) {
                     this.timeWithoutVisibility = 0;
                  } else if (++this.timeWithoutVisibility > this.maxTimeWithoutVisibility) {
                     return false;
                  }
               }

               if (livingEntity instanceof PlayerEntity && ((PlayerEntity)livingEntity).abilities.invulnerable) {
                  return false;
               } else {
                  this.mob.setTarget(livingEntity);
                  return true;
               }
            }
         }
      }
   }

   protected double getFollowRange() {
      return this.mob.getAttributeValue(EntityAttributes.GENERIC_FOLLOW_RANGE);
   }

   public void start() {
      this.canNavigateFlag = 0;
      this.checkCanNavigateCooldown = 0;
      this.timeWithoutVisibility = 0;
   }

   public void stop() {
      this.mob.setTarget((LivingEntity)null);
      this.target = null;
   }

   protected boolean canTrack(@Nullable LivingEntity target, TargetPredicate targetPredicate) {
      if (target == null) {
         return false;
      } else if (!targetPredicate.test(this.mob, target)) {
         return false;
      } else if (!this.mob.isInWalkTargetRange(target.getBlockPos())) {
         return false;
      } else {
         if (this.checkCanNavigate) {
            if (--this.checkCanNavigateCooldown <= 0) {
               this.canNavigateFlag = 0;
            }

            if (this.canNavigateFlag == 0) {
               this.canNavigateFlag = this.canNavigateToEntity(target) ? 1 : 2;
            }

            if (this.canNavigateFlag == 2) {
               return false;
            }
         }

         return true;
      }
   }

   private boolean canNavigateToEntity(LivingEntity entity) {
      this.checkCanNavigateCooldown = 10 + this.mob.getRandom().nextInt(5);
      Path path = this.mob.getNavigation().findPathTo((Entity)entity, 0);
      if (path == null) {
         return false;
      } else {
         PathNode pathNode = path.getEnd();
         if (pathNode == null) {
            return false;
         } else {
            int i = pathNode.x - MathHelper.floor(entity.getX());
            int j = pathNode.z - MathHelper.floor(entity.getZ());
            return (double)(i * i + j * j) <= 2.25D;
         }
      }
   }

   public TrackTargetGoal setMaxTimeWithoutVisibility(int time) {
      this.maxTimeWithoutVisibility = time;
      return this;
   }
}
