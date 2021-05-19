package net.minecraft.world.gen.feature;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.function.BiFunction;

public class MineshaftFeatureConfig implements FeatureConfig {
   public static final Codec<MineshaftFeatureConfig> CODEC = RecordCodecBuilder.create((instance) -> {
      return instance.group(Codec.floatRange(0.0F, 1.0F).fieldOf("probability").forGetter((mineshaftFeatureConfig) -> {
         return mineshaftFeatureConfig.probability;
      }), MineshaftFeature.Type.CODEC.fieldOf("type").forGetter((mineshaftFeatureConfig) -> {
         return mineshaftFeatureConfig.type;
      })).apply(instance, (BiFunction)(MineshaftFeatureConfig::new));
   });
   public final float probability;
   public final MineshaftFeature.Type type;

   public MineshaftFeatureConfig(float probability, MineshaftFeature.Type type) {
      this.probability = probability;
      this.type = type;
   }
}
