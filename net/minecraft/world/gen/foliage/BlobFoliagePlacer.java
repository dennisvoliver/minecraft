package net.minecraft.world.gen.foliage;

import com.mojang.datafixers.Products.P3;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.util.Function3;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import com.mojang.serialization.codecs.RecordCodecBuilder.Mu;
import java.util.Random;
import java.util.Set;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ModifiableTestableWorld;
import net.minecraft.world.gen.UniformIntDistribution;
import net.minecraft.world.gen.feature.TreeFeatureConfig;

public class BlobFoliagePlacer extends FoliagePlacer {
   public static final Codec<BlobFoliagePlacer> CODEC = RecordCodecBuilder.create((instance) -> {
      return createCodec(instance).apply(instance, (Function3)(BlobFoliagePlacer::new));
   });
   protected final int height;

   protected static <P extends BlobFoliagePlacer> P3<Mu<P>, UniformIntDistribution, UniformIntDistribution, Integer> createCodec(Instance<P> builder) {
      return fillFoliagePlacerFields(builder).and((App)Codec.intRange(0, 16).fieldOf("height").forGetter((blobFoliagePlacer) -> {
         return blobFoliagePlacer.height;
      }));
   }

   public BlobFoliagePlacer(UniformIntDistribution radius, UniformIntDistribution offset, int height) {
      super(radius, offset);
      this.height = height;
   }

   protected FoliagePlacerType<?> getType() {
      return FoliagePlacerType.BLOB_FOLIAGE_PLACER;
   }

   protected void generate(ModifiableTestableWorld world, Random random, TreeFeatureConfig config, int trunkHeight, FoliagePlacer.TreeNode treeNode, int foliageHeight, int radius, Set<BlockPos> leaves, int offset, BlockBox box) {
      for(int i = offset; i >= offset - foliageHeight; --i) {
         int j = Math.max(radius + treeNode.getFoliageRadius() - 1 - i / 2, 0);
         this.generateSquare(world, random, config, treeNode.getCenter(), j, leaves, i, treeNode.isGiantTrunk(), box);
      }

   }

   public int getRandomHeight(Random random, int trunkHeight, TreeFeatureConfig config) {
      return this.height;
   }

   protected boolean isInvalidForLeaves(Random random, int baseHeight, int dx, int y, int dz, boolean giantTrunk) {
      return baseHeight == dz && y == dz && (random.nextInt(2) == 0 || dx == 0);
   }
}
