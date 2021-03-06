package net.minecraft.entity.ai.brain.task;

import net.minecraft.entity.LivingEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.IntRange;

public class TimeLimitedTask<E extends LivingEntity> extends Task<E> {
   private boolean needsTimeReset;
   private boolean delegateRunning;
   private final IntRange timeRange;
   private final Task<? super E> delegate;
   private int timeLeft;

   public TimeLimitedTask(Task<? super E> delegate, IntRange timeRange) {
      this(delegate, false, timeRange);
   }

   public TimeLimitedTask(Task<? super E> delegate, boolean skipFirstRun, IntRange timeRange) {
      super(delegate.requiredMemoryStates);
      this.delegate = delegate;
      this.needsTimeReset = !skipFirstRun;
      this.timeRange = timeRange;
   }

   protected boolean shouldRun(ServerWorld world, E entity) {
      if (!this.delegate.shouldRun(world, entity)) {
         return false;
      } else {
         if (this.needsTimeReset) {
            this.resetTimeLeft(world);
            this.needsTimeReset = false;
         }

         if (this.timeLeft > 0) {
            --this.timeLeft;
         }

         return !this.delegateRunning && this.timeLeft == 0;
      }
   }

   protected void run(ServerWorld world, E entity, long time) {
      this.delegate.run(world, entity, time);
   }

   protected boolean shouldKeepRunning(ServerWorld world, E entity, long time) {
      return this.delegate.shouldKeepRunning(world, entity, time);
   }

   protected void keepRunning(ServerWorld world, E entity, long time) {
      this.delegate.keepRunning(world, entity, time);
      this.delegateRunning = this.delegate.getStatus() == Task.Status.RUNNING;
   }

   protected void finishRunning(ServerWorld world, E entity, long time) {
      this.resetTimeLeft(world);
      this.delegate.finishRunning(world, entity, time);
   }

   private void resetTimeLeft(ServerWorld world) {
      this.timeLeft = this.timeRange.choose(world.random);
   }

   protected boolean isTimeLimitExceeded(long time) {
      return false;
   }

   public String toString() {
      return "RunSometimes: " + this.delegate;
   }
}
