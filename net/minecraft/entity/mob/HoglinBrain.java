package net.minecraft.entity.mob;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Pair;
import java.util.List;
import java.util.Optional;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.Durations;
import net.minecraft.entity.ai.brain.Activity;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.task.BreedTask;
import net.minecraft.entity.ai.brain.task.ConditionalTask;
import net.minecraft.entity.ai.brain.task.FollowMobTask;
import net.minecraft.entity.ai.brain.task.ForgetAttackTargetTask;
import net.minecraft.entity.ai.brain.task.ForgetTask;
import net.minecraft.entity.ai.brain.task.GoToRememberedPositionTask;
import net.minecraft.entity.ai.brain.task.GoTowardsLookTarget;
import net.minecraft.entity.ai.brain.task.LookAroundTask;
import net.minecraft.entity.ai.brain.task.LookTargetUtil;
import net.minecraft.entity.ai.brain.task.MeleeAttackTask;
import net.minecraft.entity.ai.brain.task.PacifyTask;
import net.minecraft.entity.ai.brain.task.RandomTask;
import net.minecraft.entity.ai.brain.task.RangedApproachTask;
import net.minecraft.entity.ai.brain.task.StrollTask;
import net.minecraft.entity.ai.brain.task.TimeLimitedTask;
import net.minecraft.entity.ai.brain.task.UpdateAttackTargetTask;
import net.minecraft.entity.ai.brain.task.WaitTask;
import net.minecraft.entity.ai.brain.task.WalkTowardClosestAdultTask;
import net.minecraft.entity.ai.brain.task.WanderAroundTask;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.IntRange;

public class HoglinBrain {
   private static final IntRange AVOID_MEMORY_DURATION = Durations.betweenSeconds(5, 20);
   private static final IntRange WALK_TOWARD_CLOSEST_ADULT_RANGE = IntRange.between(5, 16);

   protected static Brain<?> create(Brain<HoglinEntity> brain) {
      addCoreTasks(brain);
      addIdleTasks(brain);
      addFightTasks(brain);
      addAvoidTasks(brain);
      brain.setCoreActivities(ImmutableSet.of(Activity.CORE));
      brain.setDefaultActivity(Activity.IDLE);
      brain.resetPossibleActivities();
      return brain;
   }

   private static void addCoreTasks(Brain<HoglinEntity> brain) {
      brain.setTaskList(Activity.CORE, 0, ImmutableList.of(new LookAroundTask(45, 90), new WanderAroundTask()));
   }

   private static void addIdleTasks(Brain<HoglinEntity> brain) {
      brain.setTaskList(Activity.IDLE, 10, ImmutableList.of(new PacifyTask(MemoryModuleType.NEAREST_REPELLENT, 200), new BreedTask(EntityType.HOGLIN, 0.6F), GoToRememberedPositionTask.toBlock(MemoryModuleType.NEAREST_REPELLENT, 1.0F, 8, true), new UpdateAttackTargetTask(HoglinBrain::getNearestVisibleTargetablePlayer), new ConditionalTask(HoglinEntity::isAdult, GoToRememberedPositionTask.toEntity(MemoryModuleType.NEAREST_VISIBLE_ADULT_PIGLIN, 0.4F, 8, false)), new TimeLimitedTask(new FollowMobTask(8.0F), IntRange.between(30, 60)), new WalkTowardClosestAdultTask(WALK_TOWARD_CLOSEST_ADULT_RANGE, 0.6F), makeRandomWalkTask()));
   }

   private static void addFightTasks(Brain<HoglinEntity> brain) {
      brain.setTaskList(Activity.FIGHT, 10, ImmutableList.of(new PacifyTask(MemoryModuleType.NEAREST_REPELLENT, 200), new BreedTask(EntityType.HOGLIN, 0.6F), new RangedApproachTask(1.0F), new ConditionalTask(HoglinEntity::isAdult, new MeleeAttackTask(40)), new ConditionalTask(PassiveEntity::isBaby, new MeleeAttackTask(15)), new ForgetAttackTargetTask(), new ForgetTask(HoglinBrain::hasBreedTarget, MemoryModuleType.ATTACK_TARGET)), MemoryModuleType.ATTACK_TARGET);
   }

   private static void addAvoidTasks(Brain<HoglinEntity> brain) {
      brain.setTaskList(Activity.AVOID, 10, ImmutableList.of(GoToRememberedPositionTask.toEntity(MemoryModuleType.AVOID_TARGET, 1.3F, 15, false), makeRandomWalkTask(), new TimeLimitedTask(new FollowMobTask(8.0F), IntRange.between(30, 60)), new ForgetTask(HoglinBrain::method_25947, MemoryModuleType.AVOID_TARGET)), MemoryModuleType.AVOID_TARGET);
   }

   private static RandomTask<HoglinEntity> makeRandomWalkTask() {
      return new RandomTask(ImmutableList.of(Pair.of(new StrollTask(0.4F), 2), Pair.of(new GoTowardsLookTarget(0.4F, 3), 2), Pair.of(new WaitTask(30, 60), 1)));
   }

   protected static void refreshActivities(HoglinEntity hoglin) {
      Brain<HoglinEntity> brain = hoglin.getBrain();
      Activity activity = (Activity)brain.getFirstPossibleNonCoreActivity().orElse((Object)null);
      brain.resetPossibleActivities((List)ImmutableList.of(Activity.FIGHT, Activity.AVOID, Activity.IDLE));
      Activity activity2 = (Activity)brain.getFirstPossibleNonCoreActivity().orElse((Object)null);
      if (activity != activity2) {
         method_30083(hoglin).ifPresent(hoglin::method_30081);
      }

      hoglin.setAttacking(brain.hasMemoryModule(MemoryModuleType.ATTACK_TARGET));
   }

   protected static void onAttacking(HoglinEntity hoglin, LivingEntity target) {
      if (!hoglin.isBaby()) {
         if (target.getType() == EntityType.PIGLIN && hasMoreHoglinsAround(hoglin)) {
            avoid(hoglin, target);
            askAdultsToAvoid(hoglin, target);
         } else {
            askAdultsForHelp(hoglin, target);
         }
      }
   }

   private static void askAdultsToAvoid(HoglinEntity hoglin, LivingEntity target) {
      getAdultHoglinsAround(hoglin).forEach((hoglinx) -> {
         avoidEnemy(hoglinx, target);
      });
   }

   private static void avoidEnemy(HoglinEntity hoglin, LivingEntity target) {
      Brain<HoglinEntity> brain = hoglin.getBrain();
      LivingEntity livingEntity = LookTargetUtil.getCloserEntity(hoglin, (Optional)brain.getOptionalMemory(MemoryModuleType.AVOID_TARGET), target);
      livingEntity = LookTargetUtil.getCloserEntity(hoglin, (Optional)brain.getOptionalMemory(MemoryModuleType.ATTACK_TARGET), livingEntity);
      avoid(hoglin, livingEntity);
   }

   private static void avoid(HoglinEntity hoglin, LivingEntity target) {
      hoglin.getBrain().forget(MemoryModuleType.ATTACK_TARGET);
      hoglin.getBrain().forget(MemoryModuleType.WALK_TARGET);
      hoglin.getBrain().remember(MemoryModuleType.AVOID_TARGET, target, (long)AVOID_MEMORY_DURATION.choose(hoglin.world.random));
   }

   private static Optional<? extends LivingEntity> getNearestVisibleTargetablePlayer(HoglinEntity hoglin) {
      return !isNearPlayer(hoglin) && !hasBreedTarget(hoglin) ? hoglin.getBrain().getOptionalMemory(MemoryModuleType.NEAREST_VISIBLE_TARGETABLE_PLAYER) : Optional.empty();
   }

   static boolean isWarpedFungusAround(HoglinEntity hoglin, BlockPos pos) {
      Optional<BlockPos> optional = hoglin.getBrain().getOptionalMemory(MemoryModuleType.NEAREST_REPELLENT);
      return optional.isPresent() && ((BlockPos)optional.get()).isWithinDistance(pos, 8.0D);
   }

   private static boolean method_25947(HoglinEntity hoglinEntity) {
      return hoglinEntity.isAdult() && !hasMoreHoglinsAround(hoglinEntity);
   }

   private static boolean hasMoreHoglinsAround(HoglinEntity hoglin) {
      if (hoglin.isBaby()) {
         return false;
      } else {
         int i = (Integer)hoglin.getBrain().getOptionalMemory(MemoryModuleType.VISIBLE_ADULT_PIGLIN_COUNT).orElse(0);
         int j = (Integer)hoglin.getBrain().getOptionalMemory(MemoryModuleType.VISIBLE_ADULT_HOGLIN_COUNT).orElse(0) + 1;
         return i > j;
      }
   }

   protected static void onAttacked(HoglinEntity hoglin, LivingEntity attacker) {
      Brain<HoglinEntity> brain = hoglin.getBrain();
      brain.forget(MemoryModuleType.PACIFIED);
      brain.forget(MemoryModuleType.BREED_TARGET);
      if (hoglin.isBaby()) {
         avoidEnemy(hoglin, attacker);
      } else {
         targetEnemy(hoglin, attacker);
      }
   }

   private static void targetEnemy(HoglinEntity hoglin, LivingEntity target) {
      if (!hoglin.getBrain().hasActivity(Activity.AVOID) || target.getType() != EntityType.PIGLIN) {
         if (EntityPredicates.EXCEPT_CREATIVE_SPECTATOR_OR_PEACEFUL.test(target)) {
            if (target.getType() != EntityType.HOGLIN) {
               if (!LookTargetUtil.isNewTargetTooFar(hoglin, target, 4.0D)) {
                  setAttackTarget(hoglin, target);
                  askAdultsForHelp(hoglin, target);
               }
            }
         }
      }
   }

   private static void setAttackTarget(HoglinEntity hoglin, LivingEntity target) {
      Brain<HoglinEntity> brain = hoglin.getBrain();
      brain.forget(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE);
      brain.forget(MemoryModuleType.BREED_TARGET);
      brain.remember(MemoryModuleType.ATTACK_TARGET, target, 200L);
   }

   private static void askAdultsForHelp(HoglinEntity hoglin, LivingEntity target) {
      getAdultHoglinsAround(hoglin).forEach((hoglinx) -> {
         setAttackTargetIfCloser(hoglinx, target);
      });
   }

   private static void setAttackTargetIfCloser(HoglinEntity hoglin, LivingEntity targetCandidate) {
      if (!isNearPlayer(hoglin)) {
         Optional<LivingEntity> optional = hoglin.getBrain().getOptionalMemory(MemoryModuleType.ATTACK_TARGET);
         LivingEntity livingEntity = LookTargetUtil.getCloserEntity(hoglin, (Optional)optional, targetCandidate);
         setAttackTarget(hoglin, livingEntity);
      }
   }

   public static Optional<SoundEvent> method_30083(HoglinEntity hoglinEntity) {
      return hoglinEntity.getBrain().getFirstPossibleNonCoreActivity().map((activity) -> {
         return method_30082(hoglinEntity, activity);
      });
   }

   private static SoundEvent method_30082(HoglinEntity hoglinEntity, Activity activity) {
      if (activity != Activity.AVOID && !hoglinEntity.canConvert()) {
         if (activity == Activity.FIGHT) {
            return SoundEvents.ENTITY_HOGLIN_ANGRY;
         } else {
            return method_30085(hoglinEntity) ? SoundEvents.ENTITY_HOGLIN_RETREAT : SoundEvents.ENTITY_HOGLIN_AMBIENT;
         }
      } else {
         return SoundEvents.ENTITY_HOGLIN_RETREAT;
      }
   }

   private static List<HoglinEntity> getAdultHoglinsAround(HoglinEntity hoglin) {
      return (List)hoglin.getBrain().getOptionalMemory(MemoryModuleType.NEAREST_VISIBLE_ADULT_HOGLINS).orElse(ImmutableList.of());
   }

   private static boolean method_30085(HoglinEntity hoglinEntity) {
      return hoglinEntity.getBrain().hasMemoryModule(MemoryModuleType.NEAREST_REPELLENT);
   }

   private static boolean hasBreedTarget(HoglinEntity hoglin) {
      return hoglin.getBrain().hasMemoryModule(MemoryModuleType.BREED_TARGET);
   }

   protected static boolean isNearPlayer(HoglinEntity hoglin) {
      return hoglin.getBrain().hasMemoryModule(MemoryModuleType.PACIFIED);
   }
}
