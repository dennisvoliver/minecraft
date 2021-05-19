package net.minecraft.entity.ai.goal;

import java.util.EnumSet;
import net.minecraft.entity.mob.MobEntity;

public class LookAroundGoal extends Goal {
   private final MobEntity mob;
   private double deltaX;
   private double deltaZ;
   private int lookTime;

   public LookAroundGoal(MobEntity mob) {
      this.mob = mob;
      this.setControls(EnumSet.of(Goal.Control.MOVE, Goal.Control.LOOK));
   }

   public boolean canStart() {
      return this.mob.getRandom().nextFloat() < 0.02F;
   }

   public boolean shouldContinue() {
      return this.lookTime >= 0;
   }

   public void start() {
      double d = 6.283185307179586D * this.mob.getRandom().nextDouble();
      this.deltaX = Math.cos(d);
      this.deltaZ = Math.sin(d);
      this.lookTime = 20 + this.mob.getRandom().nextInt(20);
   }

   public void tick() {
      --this.lookTime;
      this.mob.getLookControl().lookAt(this.mob.getX() + this.deltaX, this.mob.getEyeY(), this.mob.getZ() + this.deltaZ);
   }
}
