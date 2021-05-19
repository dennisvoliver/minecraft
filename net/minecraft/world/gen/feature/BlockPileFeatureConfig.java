package net.minecraft.world.gen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.world.gen.stateprovider.BlockStateProvider;

public class BlockPileFeatureConfig implements FeatureConfig {
   public static final Codec<BlockPileFeatureConfig> CODEC;
   public final BlockStateProvider stateProvider;

   public BlockPileFeatureConfig(BlockStateProvider stateProvider) {
      this.stateProvider = stateProvider;
   }

   static {
      CODEC = BlockStateProvider.TYPE_CODEC.fieldOf("state_provider").xmap(BlockPileFeatureConfig::new, (blockPileFeatureConfig) -> {
         return blockPileFeatureConfig.stateProvider;
      }).codec();
   }
}
