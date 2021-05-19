package net.minecraft.entity.ai.goal;

import net.minecraft.entity.mob.MobEntity;

public class LongDoorInteractGoal extends DoorInteractGoal {
   private final boolean delayedClose;
   private int ticksLeft;

   public LongDoorInteractGoal(MobEntity mob, boolean delayedClose) {
      super(mob);
      this.mob = mob;
      this.delayedClose = delayedClose;
   }

   public boolean shouldContinue() {
      return this.delayedClose && this.ticksLeft > 0 && super.shouldContinue();
   }

   public void start() {
      this.ticksLeft = 20;
      this.setDoorOpen(true);
   }

   public void stop() {
      this.setDoorOpen(false);
   }

   public void tick() {
      --this.ticksLeft;
      super.tick();
   }
}
