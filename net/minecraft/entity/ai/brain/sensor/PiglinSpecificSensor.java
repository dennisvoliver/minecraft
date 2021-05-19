package net.minecraft.entity.ai.brain.sensor;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.CampfireBlock;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.boss.WitherEntity;
import net.minecraft.entity.mob.AbstractPiglinEntity;
import net.minecraft.entity.mob.HoglinEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.PiglinBrain;
import net.minecraft.entity.mob.PiglinBruteEntity;
import net.minecraft.entity.mob.PiglinEntity;
import net.minecraft.entity.mob.WitherSkeletonEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.tag.BlockTags;
import net.minecraft.util.math.BlockPos;

public class PiglinSpecificSensor extends Sensor<LivingEntity> {
   public Set<MemoryModuleType<?>> getOutputMemoryModules() {
      return ImmutableSet.of(MemoryModuleType.VISIBLE_MOBS, MemoryModuleType.MOBS, MemoryModuleType.NEAREST_VISIBLE_NEMESIS, MemoryModuleType.NEAREST_TARGETABLE_PLAYER_NOT_WEARING_GOLD, MemoryModuleType.NEAREST_PLAYER_HOLDING_WANTED_ITEM, MemoryModuleType.NEAREST_VISIBLE_HUNTABLE_HOGLIN, MemoryModuleType.NEAREST_VISIBLE_BABY_HOGLIN, MemoryModuleType.NEAREST_VISIBLE_ADULT_PIGLINS, MemoryModuleType.NEARBY_ADULT_PIGLINS, MemoryModuleType.VISIBLE_ADULT_PIGLIN_COUNT, MemoryModuleType.VISIBLE_ADULT_HOGLIN_COUNT, MemoryModuleType.NEAREST_REPELLENT);
   }

   protected void sense(ServerWorld world, LivingEntity entity) {
      Brain<?> brain = entity.getBrain();
      brain.remember(MemoryModuleType.NEAREST_REPELLENT, findSoulFire(world, entity));
      Optional<MobEntity> optional = Optional.empty();
      Optional<HoglinEntity> optional2 = Optional.empty();
      Optional<HoglinEntity> optional3 = Optional.empty();
      Optional<PiglinEntity> optional4 = Optional.empty();
      Optional<LivingEntity> optional5 = Optional.empty();
      Optional<PlayerEntity> optional6 = Optional.empty();
      Optional<PlayerEntity> optional7 = Optional.empty();
      int i = 0;
      List<AbstractPiglinEntity> list = Lists.newArrayList();
      List<AbstractPiglinEntity> list2 = Lists.newArrayList();
      List<LivingEntity> list3 = (List)brain.getOptionalMemory(MemoryModuleType.VISIBLE_MOBS).orElse(ImmutableList.of());
      Iterator var15 = list3.iterator();

      while(true) {
         while(true) {
            while(var15.hasNext()) {
               LivingEntity livingEntity = (LivingEntity)var15.next();
               if (livingEntity instanceof HoglinEntity) {
                  HoglinEntity hoglinEntity = (HoglinEntity)livingEntity;
                  if (hoglinEntity.isBaby() && !optional3.isPresent()) {
                     optional3 = Optional.of(hoglinEntity);
                  } else if (hoglinEntity.isAdult()) {
                     ++i;
                     if (!optional2.isPresent() && hoglinEntity.canBeHunted()) {
                        optional2 = Optional.of(hoglinEntity);
                     }
                  }
               } else if (livingEntity instanceof PiglinBruteEntity) {
                  list.add((PiglinBruteEntity)livingEntity);
               } else if (livingEntity instanceof PiglinEntity) {
                  PiglinEntity piglinEntity = (PiglinEntity)livingEntity;
                  if (piglinEntity.isBaby() && !optional4.isPresent()) {
                     optional4 = Optional.of(piglinEntity);
                  } else if (piglinEntity.isAdult()) {
                     list.add(piglinEntity);
                  }
               } else if (livingEntity instanceof PlayerEntity) {
                  PlayerEntity playerEntity = (PlayerEntity)livingEntity;
                  if (!optional6.isPresent() && EntityPredicates.EXCEPT_CREATIVE_SPECTATOR_OR_PEACEFUL.test(livingEntity) && !PiglinBrain.wearsGoldArmor(playerEntity)) {
                     optional6 = Optional.of(playerEntity);
                  }

                  if (!optional7.isPresent() && !playerEntity.isSpectator() && PiglinBrain.isGoldHoldingPlayer(playerEntity)) {
                     optional7 = Optional.of(playerEntity);
                  }
               } else if (optional.isPresent() || !(livingEntity instanceof WitherSkeletonEntity) && !(livingEntity instanceof WitherEntity)) {
                  if (!optional5.isPresent() && PiglinBrain.isZombified(livingEntity.getType())) {
                     optional5 = Optional.of(livingEntity);
                  }
               } else {
                  optional = Optional.of((MobEntity)livingEntity);
               }
            }

            List<LivingEntity> list4 = (List)brain.getOptionalMemory(MemoryModuleType.MOBS).orElse(ImmutableList.of());
            Iterator var19 = list4.iterator();

            while(var19.hasNext()) {
               LivingEntity livingEntity2 = (LivingEntity)var19.next();
               if (livingEntity2 instanceof AbstractPiglinEntity && ((AbstractPiglinEntity)livingEntity2).isAdult()) {
                  list2.add((AbstractPiglinEntity)livingEntity2);
               }
            }

            brain.remember(MemoryModuleType.NEAREST_VISIBLE_NEMESIS, optional);
            brain.remember(MemoryModuleType.NEAREST_VISIBLE_HUNTABLE_HOGLIN, optional2);
            brain.remember(MemoryModuleType.NEAREST_VISIBLE_BABY_HOGLIN, optional3);
            brain.remember(MemoryModuleType.NEAREST_VISIBLE_ZOMBIFIED, optional5);
            brain.remember(MemoryModuleType.NEAREST_TARGETABLE_PLAYER_NOT_WEARING_GOLD, optional6);
            brain.remember(MemoryModuleType.NEAREST_PLAYER_HOLDING_WANTED_ITEM, optional7);
            brain.remember(MemoryModuleType.NEARBY_ADULT_PIGLINS, (Object)list2);
            brain.remember(MemoryModuleType.NEAREST_VISIBLE_ADULT_PIGLINS, (Object)list);
            brain.remember(MemoryModuleType.VISIBLE_ADULT_PIGLIN_COUNT, (Object)list.size());
            brain.remember(MemoryModuleType.VISIBLE_ADULT_HOGLIN_COUNT, (Object)i);
            return;
         }
      }
   }

   private static Optional<BlockPos> findSoulFire(ServerWorld world, LivingEntity entity) {
      return BlockPos.findClosest(entity.getBlockPos(), 8, 4, (blockPos) -> {
         return method_24648(world, blockPos);
      });
   }

   private static boolean method_24648(ServerWorld serverWorld, BlockPos blockPos) {
      BlockState blockState = serverWorld.getBlockState(blockPos);
      boolean bl = blockState.isIn(BlockTags.PIGLIN_REPELLENTS);
      return bl && blockState.isOf(Blocks.SOUL_CAMPFIRE) ? CampfireBlock.isLitCampfire(blockState) : bl;
   }
}
