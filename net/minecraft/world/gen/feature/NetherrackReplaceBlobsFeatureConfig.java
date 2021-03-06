package net.minecraft.world.gen.feature;

import com.mojang.datafixers.util.Function3;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.block.BlockState;
import net.minecraft.world.gen.UniformIntDistribution;

public class NetherrackReplaceBlobsFeatureConfig implements FeatureConfig {
   public static final Codec<NetherrackReplaceBlobsFeatureConfig> CODEC = RecordCodecBuilder.create((instance) -> {
      return instance.group(BlockState.CODEC.fieldOf("target").forGetter((netherrackReplaceBlobsFeatureConfig) -> {
         return netherrackReplaceBlobsFeatureConfig.target;
      }), BlockState.CODEC.fieldOf("state").forGetter((netherrackReplaceBlobsFeatureConfig) -> {
         return netherrackReplaceBlobsFeatureConfig.state;
      }), UniformIntDistribution.CODEC.fieldOf("radius").forGetter((netherrackReplaceBlobsFeatureConfig) -> {
         return netherrackReplaceBlobsFeatureConfig.radius;
      })).apply(instance, (Function3)(NetherrackReplaceBlobsFeatureConfig::new));
   });
   public final BlockState target;
   public final BlockState state;
   private final UniformIntDistribution radius;

   public NetherrackReplaceBlobsFeatureConfig(BlockState target, BlockState state, UniformIntDistribution radius) {
      this.target = target;
      this.state = state;
      this.radius = radius;
   }

   public UniformIntDistribution getRadius() {
      return this.radius;
   }
}
