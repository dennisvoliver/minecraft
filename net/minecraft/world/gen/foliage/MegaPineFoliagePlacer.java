package net.minecraft.world.gen.foliage;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.util.Function3;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Random;
import java.util.Set;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.ModifiableTestableWorld;
import net.minecraft.world.gen.UniformIntDistribution;
import net.minecraft.world.gen.feature.TreeFeatureConfig;

public class MegaPineFoliagePlacer extends FoliagePlacer {
   public static final Codec<MegaPineFoliagePlacer> CODEC = RecordCodecBuilder.create((instance) -> {
      return fillFoliagePlacerFields(instance).and((App)UniformIntDistribution.createValidatedCodec(0, 16, 8).fieldOf("crown_height").forGetter((megaPineFoliagePlacer) -> {
         return megaPineFoliagePlacer.crownHeight;
      })).apply(instance, (Function3)(MegaPineFoliagePlacer::new));
   });
   private final UniformIntDistribution crownHeight;

   public MegaPineFoliagePlacer(UniformIntDistribution radius, UniformIntDistribution offset, UniformIntDistribution crownHeight) {
      super(radius, offset);
      this.crownHeight = crownHeight;
   }

   protected FoliagePlacerType<?> getType() {
      return FoliagePlacerType.MEGA_PINE_FOLIAGE_PLACER;
   }

   protected void generate(ModifiableTestableWorld world, Random random, TreeFeatureConfig config, int trunkHeight, FoliagePlacer.TreeNode treeNode, int foliageHeight, int radius, Set<BlockPos> leaves, int offset, BlockBox box) {
      BlockPos blockPos = treeNode.getCenter();
      int i = 0;

      for(int j = blockPos.getY() - foliageHeight + offset; j <= blockPos.getY() + offset; ++j) {
         int k = blockPos.getY() - j;
         int l = radius + treeNode.getFoliageRadius() + MathHelper.floor((float)k / (float)foliageHeight * 3.5F);
         int n;
         if (k > 0 && l == i && (j & 1) == 0) {
            n = l + 1;
         } else {
            n = l;
         }

         this.generateSquare(world, random, config, new BlockPos(blockPos.getX(), j, blockPos.getZ()), n, leaves, 0, treeNode.isGiantTrunk(), box);
         i = l;
      }

   }

   public int getRandomHeight(Random random, int trunkHeight, TreeFeatureConfig config) {
      return this.crownHeight.getValue(random);
   }

   protected boolean isInvalidForLeaves(Random random, int baseHeight, int dx, int y, int dz, boolean giantTrunk) {
      if (baseHeight + y >= 7) {
         return true;
      } else {
         return baseHeight * baseHeight + y * y > dz * dz;
      }
   }
}
