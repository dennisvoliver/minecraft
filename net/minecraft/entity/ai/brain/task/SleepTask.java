package net.minecraft.entity.ai.brain.task;

import com.google.common.collect.ImmutableMap;
import java.util.Optional;
import net.minecraft.block.BedBlock;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.Activity;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.MemoryModuleState;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.pathing.PathNode;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.tag.BlockTags;
import net.minecraft.util.dynamic.GlobalPos;
import net.minecraft.util.math.BlockPos;

public class SleepTask extends Task<LivingEntity> {
   private long startTime;

   public SleepTask() {
      super(ImmutableMap.of(MemoryModuleType.HOME, MemoryModuleState.VALUE_PRESENT, MemoryModuleType.LAST_WOKEN, MemoryModuleState.REGISTERED));
   }

   protected boolean shouldRun(ServerWorld world, LivingEntity entity) {
      if (entity.hasVehicle()) {
         return false;
      } else {
         Brain<?> brain = entity.getBrain();
         GlobalPos globalPos = (GlobalPos)brain.getOptionalMemory(MemoryModuleType.HOME).get();
         if (world.getRegistryKey() != globalPos.getDimension()) {
            return false;
         } else {
            Optional<Long> optional = brain.getOptionalMemory(MemoryModuleType.LAST_WOKEN);
            if (optional.isPresent()) {
               long l = world.getTime() - (Long)optional.get();
               if (l > 0L && l < 100L) {
                  return false;
               }
            }

            BlockState blockState = world.getBlockState(globalPos.getPos());
            return globalPos.getPos().isWithinDistance(entity.getPos(), 2.0D) && blockState.getBlock().isIn(BlockTags.BEDS) && !(Boolean)blockState.get(BedBlock.OCCUPIED);
         }
      }
   }

   protected boolean shouldKeepRunning(ServerWorld world, LivingEntity entity, long time) {
      Optional<GlobalPos> optional = entity.getBrain().getOptionalMemory(MemoryModuleType.HOME);
      if (!optional.isPresent()) {
         return false;
      } else {
         BlockPos blockPos = ((GlobalPos)optional.get()).getPos();
         return entity.getBrain().hasActivity(Activity.REST) && entity.getY() > (double)blockPos.getY() + 0.4D && blockPos.isWithinDistance(entity.getPos(), 1.14D);
      }
   }

   protected void run(ServerWorld world, LivingEntity entity, long time) {
      if (time > this.startTime) {
         OpenDoorsTask.method_30760(world, entity, (PathNode)null, (PathNode)null);
         entity.sleep(((GlobalPos)entity.getBrain().getOptionalMemory(MemoryModuleType.HOME).get()).getPos());
      }

   }

   protected boolean isTimeLimitExceeded(long time) {
      return false;
   }

   protected void finishRunning(ServerWorld world, LivingEntity entity, long time) {
      if (entity.isSleeping()) {
         entity.wakeUp();
         this.startTime = time + 40L;
      }

   }
}
