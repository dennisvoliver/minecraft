package net.minecraft.world.gen.feature;

import com.mojang.datafixers.util.Function4;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.block.BlockState;
import net.minecraft.world.gen.UniformIntDistribution;

public class DeltaFeatureConfig implements FeatureConfig {
   public static final Codec<DeltaFeatureConfig> CODEC = RecordCodecBuilder.create((instance) -> {
      return instance.group(BlockState.CODEC.fieldOf("contents").forGetter((deltaFeatureConfig) -> {
         return deltaFeatureConfig.contents;
      }), BlockState.CODEC.fieldOf("rim").forGetter((deltaFeatureConfig) -> {
         return deltaFeatureConfig.rim;
      }), UniformIntDistribution.createValidatedCodec(0, 8, 8).fieldOf("size").forGetter((deltaFeatureConfig) -> {
         return deltaFeatureConfig.size;
      }), UniformIntDistribution.createValidatedCodec(0, 8, 8).fieldOf("rim_size").forGetter((deltaFeatureConfig) -> {
         return deltaFeatureConfig.rimSize;
      })).apply(instance, (Function4)(DeltaFeatureConfig::new));
   });
   private final BlockState contents;
   private final BlockState rim;
   private final UniformIntDistribution size;
   private final UniformIntDistribution rimSize;

   public DeltaFeatureConfig(BlockState contents, BlockState rim, UniformIntDistribution size, UniformIntDistribution rimSize) {
      this.contents = contents;
      this.rim = rim;
      this.size = size;
      this.rimSize = rimSize;
   }

   public BlockState getContents() {
      return this.contents;
   }

   public BlockState getRim() {
      return this.rim;
   }

   public UniformIntDistribution getSize() {
      return this.size;
   }

   public UniformIntDistribution getRimSize() {
      return this.rimSize;
   }
}
