package net.minecraft.block.sapling;

import java.util.Random;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.gen.feature.ConfiguredFeatures;
import net.minecraft.world.gen.feature.TreeFeatureConfig;
import org.jetbrains.annotations.Nullable;

public class BirchSaplingGenerator extends SaplingGenerator {
   @Nullable
   protected ConfiguredFeature<TreeFeatureConfig, ?> createTreeFeature(Random random, boolean bl) {
      return bl ? ConfiguredFeatures.BIRCH_BEES_005 : ConfiguredFeatures.BIRCH;
   }
}
