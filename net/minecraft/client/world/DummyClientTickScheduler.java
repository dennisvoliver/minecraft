package net.minecraft.client.world;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.TickPriority;
import net.minecraft.world.TickScheduler;

public class DummyClientTickScheduler<T> implements TickScheduler<T> {
   private static final DummyClientTickScheduler<Object> INSTANCE = new DummyClientTickScheduler();

   public static <T> DummyClientTickScheduler<T> get() {
      return INSTANCE;
   }

   public boolean isScheduled(BlockPos pos, T object) {
      return false;
   }

   public void schedule(BlockPos pos, T object, int delay) {
   }

   public void schedule(BlockPos pos, T object, int delay, TickPriority priority) {
   }

   public boolean isTicking(BlockPos pos, T object) {
      return false;
   }
}
