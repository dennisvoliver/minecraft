package net.minecraft.entity.ai.brain.task;

import com.google.common.collect.ImmutableMap;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.Activity;
import net.minecraft.server.world.ServerWorld;

public class WakeUpTask extends Task<LivingEntity> {
   public WakeUpTask() {
      super(ImmutableMap.of());
   }

   protected boolean shouldRun(ServerWorld world, LivingEntity entity) {
      return !entity.getBrain().hasActivity(Activity.REST) && entity.isSleeping();
   }

   protected void run(ServerWorld world, LivingEntity entity, long time) {
      entity.wakeUp();
   }
}
