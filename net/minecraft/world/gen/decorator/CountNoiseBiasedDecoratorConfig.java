package net.minecraft.world.gen.decorator;

import com.mojang.datafixers.util.Function3;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public class CountNoiseBiasedDecoratorConfig implements DecoratorConfig {
   public static final Codec<CountNoiseBiasedDecoratorConfig> CODEC = RecordCodecBuilder.create((instance) -> {
      return instance.group(Codec.INT.fieldOf("noise_to_count_ratio").forGetter((countNoiseBiasedDecoratorConfig) -> {
         return countNoiseBiasedDecoratorConfig.noiseToCountRatio;
      }), Codec.DOUBLE.fieldOf("noise_factor").forGetter((countNoiseBiasedDecoratorConfig) -> {
         return countNoiseBiasedDecoratorConfig.noiseFactor;
      }), Codec.DOUBLE.fieldOf("noise_offset").orElse(0.0D).forGetter((countNoiseBiasedDecoratorConfig) -> {
         return countNoiseBiasedDecoratorConfig.noiseOffset;
      })).apply(instance, (Function3)(CountNoiseBiasedDecoratorConfig::new));
   });
   public final int noiseToCountRatio;
   public final double noiseFactor;
   public final double noiseOffset;

   public CountNoiseBiasedDecoratorConfig(int noiseToCountRatio, double noiseFactor, double noiseOffset) {
      this.noiseToCountRatio = noiseToCountRatio;
      this.noiseFactor = noiseFactor;
      this.noiseOffset = noiseOffset;
   }
}
