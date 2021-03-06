package net.minecraft.world.gen.trunk;

import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Function3;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import net.minecraft.block.BlockState;
import net.minecraft.block.PillarBlock;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.ModifiableTestableWorld;
import net.minecraft.world.gen.feature.TreeFeature;
import net.minecraft.world.gen.feature.TreeFeatureConfig;
import net.minecraft.world.gen.foliage.FoliagePlacer;

public class LargeOakTrunkPlacer extends TrunkPlacer {
   public static final Codec<LargeOakTrunkPlacer> CODEC = RecordCodecBuilder.create((instance) -> {
      return method_28904(instance).apply(instance, (Function3)(LargeOakTrunkPlacer::new));
   });

   public LargeOakTrunkPlacer(int i, int j, int k) {
      super(i, j, k);
   }

   protected TrunkPlacerType<?> getType() {
      return TrunkPlacerType.FANCY_TRUNK_PLACER;
   }

   public List<FoliagePlacer.TreeNode> generate(ModifiableTestableWorld world, Random random, int trunkHeight, BlockPos pos, Set<BlockPos> placedStates, BlockBox box, TreeFeatureConfig config) {
      int i = true;
      int j = trunkHeight + 2;
      int k = MathHelper.floor((double)j * 0.618D);
      if (!config.skipFluidCheck) {
         setToDirt(world, pos.down());
      }

      double d = 1.0D;
      int l = Math.min(1, MathHelper.floor(1.382D + Math.pow(1.0D * (double)j / 13.0D, 2.0D)));
      int m = pos.getY() + k;
      int n = j - 5;
      List<LargeOakTrunkPlacer.BranchPosition> list = Lists.newArrayList();
      list.add(new LargeOakTrunkPlacer.BranchPosition(pos.up(n), m));

      for(; n >= 0; --n) {
         float f = this.shouldGenerateBranch(j, n);
         if (!(f < 0.0F)) {
            for(int o = 0; o < l; ++o) {
               double e = 1.0D;
               double g = 1.0D * (double)f * ((double)random.nextFloat() + 0.328D);
               double h = (double)(random.nextFloat() * 2.0F) * 3.141592653589793D;
               double p = g * Math.sin(h) + 0.5D;
               double q = g * Math.cos(h) + 0.5D;
               BlockPos blockPos = pos.add(p, (double)(n - 1), q);
               BlockPos blockPos2 = blockPos.up(5);
               if (this.makeOrCheckBranch(world, random, blockPos, blockPos2, false, placedStates, box, config)) {
                  int r = pos.getX() - blockPos.getX();
                  int s = pos.getZ() - blockPos.getZ();
                  double t = (double)blockPos.getY() - Math.sqrt((double)(r * r + s * s)) * 0.381D;
                  int u = t > (double)m ? m : (int)t;
                  BlockPos blockPos3 = new BlockPos(pos.getX(), u, pos.getZ());
                  if (this.makeOrCheckBranch(world, random, blockPos3, blockPos, false, placedStates, box, config)) {
                     list.add(new LargeOakTrunkPlacer.BranchPosition(blockPos, blockPos3.getY()));
                  }
               }
            }
         }
      }

      this.makeOrCheckBranch(world, random, pos, pos.up(k), true, placedStates, box, config);
      this.makeBranches(world, random, j, pos, list, placedStates, box, config);
      List<FoliagePlacer.TreeNode> list2 = Lists.newArrayList();
      Iterator var38 = list.iterator();

      while(var38.hasNext()) {
         LargeOakTrunkPlacer.BranchPosition branchPosition = (LargeOakTrunkPlacer.BranchPosition)var38.next();
         if (this.isHighEnough(j, branchPosition.getEndY() - pos.getY())) {
            list2.add(branchPosition.node);
         }
      }

      return list2;
   }

   private boolean makeOrCheckBranch(ModifiableTestableWorld world, Random random, BlockPos start, BlockPos end, boolean make, Set<BlockPos> placedStates, BlockBox box, TreeFeatureConfig config) {
      if (!make && Objects.equals(start, end)) {
         return true;
      } else {
         BlockPos blockPos = end.add(-start.getX(), -start.getY(), -start.getZ());
         int i = this.getLongestSide(blockPos);
         float f = (float)blockPos.getX() / (float)i;
         float g = (float)blockPos.getY() / (float)i;
         float h = (float)blockPos.getZ() / (float)i;

         for(int j = 0; j <= i; ++j) {
            BlockPos blockPos2 = start.add((double)(0.5F + (float)j * f), (double)(0.5F + (float)j * g), (double)(0.5F + (float)j * h));
            if (make) {
               setBlockState(world, blockPos2, (BlockState)config.trunkProvider.getBlockState(random, blockPos2).with(PillarBlock.AXIS, this.getLogAxis(start, blockPos2)), box);
               placedStates.add(blockPos2.toImmutable());
            } else if (!TreeFeature.canTreeReplace(world, blockPos2)) {
               return false;
            }
         }

         return true;
      }
   }

   private int getLongestSide(BlockPos offset) {
      int i = MathHelper.abs(offset.getX());
      int j = MathHelper.abs(offset.getY());
      int k = MathHelper.abs(offset.getZ());
      return Math.max(i, Math.max(j, k));
   }

   private Direction.Axis getLogAxis(BlockPos branchStart, BlockPos branchEnd) {
      Direction.Axis axis = Direction.Axis.Y;
      int i = Math.abs(branchEnd.getX() - branchStart.getX());
      int j = Math.abs(branchEnd.getZ() - branchStart.getZ());
      int k = Math.max(i, j);
      if (k > 0) {
         if (i == k) {
            axis = Direction.Axis.X;
         } else {
            axis = Direction.Axis.Z;
         }
      }

      return axis;
   }

   private boolean isHighEnough(int treeHeight, int height) {
      return (double)height >= (double)treeHeight * 0.2D;
   }

   private void makeBranches(ModifiableTestableWorld world, Random random, int treeHeight, BlockPos treePos, List<LargeOakTrunkPlacer.BranchPosition> branches, Set<BlockPos> placedStates, BlockBox box, TreeFeatureConfig config) {
      Iterator var9 = branches.iterator();

      while(var9.hasNext()) {
         LargeOakTrunkPlacer.BranchPosition branchPosition = (LargeOakTrunkPlacer.BranchPosition)var9.next();
         int i = branchPosition.getEndY();
         BlockPos blockPos = new BlockPos(treePos.getX(), i, treePos.getZ());
         if (!blockPos.equals(branchPosition.node.getCenter()) && this.isHighEnough(treeHeight, i - treePos.getY())) {
            this.makeOrCheckBranch(world, random, blockPos, branchPosition.node.getCenter(), true, placedStates, box, config);
         }
      }

   }

   /**
    * If the returned value is greater than or equal to 0, a branch will be generated.
    */
   private float shouldGenerateBranch(int trunkHeight, int y) {
      if ((float)y < (float)trunkHeight * 0.3F) {
         return -1.0F;
      } else {
         float f = (float)trunkHeight / 2.0F;
         float g = f - (float)y;
         float h = MathHelper.sqrt(f * f - g * g);
         if (g == 0.0F) {
            h = f;
         } else if (Math.abs(g) >= f) {
            return 0.0F;
         }

         return h * 0.5F;
      }
   }

   static class BranchPosition {
      private final FoliagePlacer.TreeNode node;
      private final int endY;

      public BranchPosition(BlockPos pos, int width) {
         this.node = new FoliagePlacer.TreeNode(pos, 0, false);
         this.endY = width;
      }

      public int getEndY() {
         return this.endY;
      }
   }
}
