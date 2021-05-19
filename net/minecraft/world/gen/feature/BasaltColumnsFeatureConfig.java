package net.minecraft.world.gen.feature;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.function.BiFunction;
import net.minecraft.world.gen.UniformIntDistribution;

public class BasaltColumnsFeatureConfig implements FeatureConfig {
   public static final Codec<BasaltColumnsFeatureConfig> CODEC = RecordCodecBuilder.create((instance) -> {
      return instance.group(UniformIntDistribution.createValidatedCodec(0, 2, 1).fieldOf("reach").forGetter((basaltColumnsFeatureConfig) -> {
         return basaltColumnsFeatureConfig.reach;
      }), UniformIntDistribution.createValidatedCodec(1, 5, 5).fieldOf("height").forGetter((basaltColumnsFeatureConfig) -> {
         return basaltColumnsFeatureConfig.height;
      })).apply(instance, (BiFunction)(BasaltColumnsFeatureConfig::new));
   });
   private final UniformIntDistribution reach;
   private final UniformIntDistribution height;

   public BasaltColumnsFeatureConfig(UniformIntDistribution reach, UniformIntDistribution height) {
      this.reach = reach;
      this.height = height;
   }

   public UniformIntDistribution getReach() {
      return this.reach;
   }

   public UniformIntDistribution getHeight() {
      return this.height;
   }
}
