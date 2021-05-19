package net.minecraft.world.gen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.function.Function;
import net.minecraft.world.gen.carver.CarverConfig;
import net.minecraft.world.gen.feature.FeatureConfig;

public class ProbabilityConfig implements CarverConfig, FeatureConfig {
   public static final Codec<ProbabilityConfig> CODEC = RecordCodecBuilder.create((instance) -> {
      return instance.group(Codec.floatRange(0.0F, 1.0F).fieldOf("probability").forGetter((probabilityConfig) -> {
         return probabilityConfig.probability;
      })).apply(instance, (Function)(ProbabilityConfig::new));
   });
   public final float probability;

   public ProbabilityConfig(float probability) {
      this.probability = probability;
   }
}
