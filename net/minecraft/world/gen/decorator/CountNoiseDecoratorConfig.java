package net.minecraft.world.gen.decorator;

import com.mojang.datafixers.util.Function3;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public class CountNoiseDecoratorConfig implements DecoratorConfig {
   public static final Codec<CountNoiseDecoratorConfig> CODEC = RecordCodecBuilder.create((instance) -> {
      return instance.group(Codec.DOUBLE.fieldOf("noise_level").forGetter((countNoiseDecoratorConfig) -> {
         return countNoiseDecoratorConfig.noiseLevel;
      }), Codec.INT.fieldOf("below_noise").forGetter((countNoiseDecoratorConfig) -> {
         return countNoiseDecoratorConfig.belowNoise;
      }), Codec.INT.fieldOf("above_noise").forGetter((countNoiseDecoratorConfig) -> {
         return countNoiseDecoratorConfig.aboveNoise;
      })).apply(instance, (Function3)(CountNoiseDecoratorConfig::new));
   });
   public final double noiseLevel;
   public final int belowNoise;
   public final int aboveNoise;

   public CountNoiseDecoratorConfig(double noiseLevel, int belowNoise, int aboveNoise) {
      this.noiseLevel = noiseLevel;
      this.belowNoise = belowNoise;
      this.aboveNoise = aboveNoise;
   }
}
