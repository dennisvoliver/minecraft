package net.minecraft.world.gen.stateprovider;

import com.mojang.serialization.Codec;
import net.minecraft.util.registry.Registry;

public class BlockStateProviderType<P extends BlockStateProvider> {
   public static final BlockStateProviderType<SimpleBlockStateProvider> SIMPLE_STATE_PROVIDER;
   public static final BlockStateProviderType<WeightedBlockStateProvider> WEIGHTED_STATE_PROVIDER;
   public static final BlockStateProviderType<PlainsFlowerBlockStateProvider> PLAIN_FLOWER_PROVIDER;
   public static final BlockStateProviderType<ForestFlowerBlockStateProvider> FOREST_FLOWER_PROVIDER;
   public static final BlockStateProviderType<PillarBlockStateProvider> ROTATED_BLOCK_PROVIDER;
   private final Codec<P> codec;

   private static <P extends BlockStateProvider> BlockStateProviderType<P> register(String id, Codec<P> codec) {
      return (BlockStateProviderType)Registry.register(Registry.BLOCK_STATE_PROVIDER_TYPE, (String)id, new BlockStateProviderType(codec));
   }

   private BlockStateProviderType(Codec<P> codec) {
      this.codec = codec;
   }

   public Codec<P> getCodec() {
      return this.codec;
   }

   static {
      SIMPLE_STATE_PROVIDER = register("simple_state_provider", SimpleBlockStateProvider.CODEC);
      WEIGHTED_STATE_PROVIDER = register("weighted_state_provider", WeightedBlockStateProvider.CODEC);
      PLAIN_FLOWER_PROVIDER = register("plain_flower_provider", PlainsFlowerBlockStateProvider.CODEC);
      FOREST_FLOWER_PROVIDER = register("forest_flower_provider", ForestFlowerBlockStateProvider.CODEC);
      ROTATED_BLOCK_PROVIDER = register("rotated_block_provider", PillarBlockStateProvider.CODEC);
   }
}
