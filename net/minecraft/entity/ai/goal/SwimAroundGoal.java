package net.minecraft.entity.ai.goal;

import net.minecraft.entity.ai.TargetFinder;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

public class SwimAroundGoal extends WanderAroundGoal {
   public SwimAroundGoal(PathAwareEntity pathAwareEntity, double d, int i) {
      super(pathAwareEntity, d, i);
   }

   @Nullable
   protected Vec3d getWanderTarget() {
      Vec3d vec3d = TargetFinder.findTarget(this.mob, 10, 7);

      for(int var2 = 0; vec3d != null && !this.mob.world.getBlockState(new BlockPos(vec3d)).canPathfindThrough(this.mob.world, new BlockPos(vec3d), NavigationType.WATER) && var2++ < 10; vec3d = TargetFinder.findTarget(this.mob, 10, 7)) {
      }

      return vec3d;
   }
}
