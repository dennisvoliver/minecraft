package net.minecraft.entity.ai.goal;

import java.util.Iterator;
import net.minecraft.block.Block;
import net.minecraft.block.LeavesBlock;
import net.minecraft.entity.ai.TargetFinder;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.tag.BlockTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

public class FlyOntoTreeGoal extends WanderAroundFarGoal {
   public FlyOntoTreeGoal(PathAwareEntity pathAwareEntity, double d) {
      super(pathAwareEntity, d);
   }

   @Nullable
   protected Vec3d getWanderTarget() {
      Vec3d vec3d = null;
      if (this.mob.isTouchingWater()) {
         vec3d = TargetFinder.findGroundTarget(this.mob, 15, 15);
      }

      if (this.mob.getRandom().nextFloat() >= this.probability) {
         vec3d = this.getTreeTarget();
      }

      return vec3d == null ? super.getWanderTarget() : vec3d;
   }

   @Nullable
   private Vec3d getTreeTarget() {
      BlockPos blockPos = this.mob.getBlockPos();
      BlockPos.Mutable mutable = new BlockPos.Mutable();
      BlockPos.Mutable mutable2 = new BlockPos.Mutable();
      Iterable<BlockPos> iterable = BlockPos.iterate(MathHelper.floor(this.mob.getX() - 3.0D), MathHelper.floor(this.mob.getY() - 6.0D), MathHelper.floor(this.mob.getZ() - 3.0D), MathHelper.floor(this.mob.getX() + 3.0D), MathHelper.floor(this.mob.getY() + 6.0D), MathHelper.floor(this.mob.getZ() + 3.0D));
      Iterator var5 = iterable.iterator();

      BlockPos blockPos2;
      boolean bl;
      do {
         do {
            if (!var5.hasNext()) {
               return null;
            }

            blockPos2 = (BlockPos)var5.next();
         } while(blockPos.equals(blockPos2));

         Block block = this.mob.world.getBlockState(mutable2.set(blockPos2, Direction.DOWN)).getBlock();
         bl = block instanceof LeavesBlock || block.isIn(BlockTags.LOGS);
      } while(!bl || !this.mob.world.isAir(blockPos2) || !this.mob.world.isAir(mutable.set(blockPos2, Direction.UP)));

      return Vec3d.ofBottomCenter(blockPos2);
   }
}
