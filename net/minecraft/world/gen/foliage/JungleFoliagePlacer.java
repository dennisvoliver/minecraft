package net.minecraft.world.gen.foliage;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.util.Function3;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Random;
import java.util.Set;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ModifiableTestableWorld;
import net.minecraft.world.gen.UniformIntDistribution;
import net.minecraft.world.gen.feature.TreeFeatureConfig;

public class JungleFoliagePlacer extends FoliagePlacer {
   public static final Codec<JungleFoliagePlacer> CODEC = RecordCodecBuilder.create((instance) -> {
      return fillFoliagePlacerFields(instance).and((App)Codec.intRange(0, 16).fieldOf("height").forGetter((jungleFoliagePlacer) -> {
         return jungleFoliagePlacer.height;
      })).apply(instance, (Function3)(JungleFoliagePlacer::new));
   });
   protected final int height;

   public JungleFoliagePlacer(UniformIntDistribution radius, UniformIntDistribution offset, int height) {
      super(radius, offset);
      this.height = height;
   }

   protected FoliagePlacerType<?> getType() {
      return FoliagePlacerType.JUNGLE_FOLIAGE_PLACER;
   }

   protected void generate(ModifiableTestableWorld world, Random random, TreeFeatureConfig config, int trunkHeight, FoliagePlacer.TreeNode treeNode, int foliageHeight, int radius, Set<BlockPos> leaves, int offset, BlockBox box) {
      int i = treeNode.isGiantTrunk() ? foliageHeight : 1 + random.nextInt(2);

      for(int j = offset; j >= offset - i; --j) {
         int k = radius + treeNode.getFoliageRadius() + 1 - j;
         this.generateSquare(world, random, config, treeNode.getCenter(), k, leaves, j, treeNode.isGiantTrunk(), box);
      }

   }

   public int getRandomHeight(Random random, int trunkHeight, TreeFeatureConfig config) {
      return this.height;
   }

   protected boolean isInvalidForLeaves(Random random, int baseHeight, int dx, int y, int dz, boolean giantTrunk) {
      if (baseHeight + y >= 7) {
         return true;
      } else {
         return baseHeight * baseHeight + y * y > dz * dz;
      }
   }
}
