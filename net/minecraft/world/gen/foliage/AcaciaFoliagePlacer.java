package net.minecraft.world.gen.foliage;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Random;
import java.util.Set;
import java.util.function.BiFunction;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ModifiableTestableWorld;
import net.minecraft.world.gen.UniformIntDistribution;
import net.minecraft.world.gen.feature.TreeFeatureConfig;

public class AcaciaFoliagePlacer extends FoliagePlacer {
   public static final Codec<AcaciaFoliagePlacer> CODEC = RecordCodecBuilder.create((instance) -> {
      return fillFoliagePlacerFields(instance).apply(instance, (BiFunction)(AcaciaFoliagePlacer::new));
   });

   public AcaciaFoliagePlacer(UniformIntDistribution uniformIntDistribution, UniformIntDistribution uniformIntDistribution2) {
      super(uniformIntDistribution, uniformIntDistribution2);
   }

   protected FoliagePlacerType<?> getType() {
      return FoliagePlacerType.ACACIA_FOLIAGE_PLACER;
   }

   protected void generate(ModifiableTestableWorld world, Random random, TreeFeatureConfig config, int trunkHeight, FoliagePlacer.TreeNode treeNode, int foliageHeight, int radius, Set<BlockPos> leaves, int offset, BlockBox box) {
      boolean bl = treeNode.isGiantTrunk();
      BlockPos blockPos = treeNode.getCenter().up(offset);
      this.generateSquare(world, random, config, blockPos, radius + treeNode.getFoliageRadius(), leaves, -1 - foliageHeight, bl, box);
      this.generateSquare(world, random, config, blockPos, radius - 1, leaves, -foliageHeight, bl, box);
      this.generateSquare(world, random, config, blockPos, radius + treeNode.getFoliageRadius() - 1, leaves, 0, bl, box);
   }

   public int getRandomHeight(Random random, int trunkHeight, TreeFeatureConfig config) {
      return 0;
   }

   protected boolean isInvalidForLeaves(Random random, int baseHeight, int dx, int y, int dz, boolean giantTrunk) {
      if (dx == 0) {
         return (baseHeight > 1 || y > 1) && baseHeight != 0 && y != 0;
      } else {
         return baseHeight == dz && y == dz && dz > 0;
      }
   }
}
