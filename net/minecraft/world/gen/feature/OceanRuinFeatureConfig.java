package net.minecraft.world.gen.feature;

import com.mojang.datafixers.util.Function3;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public class OceanRuinFeatureConfig implements FeatureConfig {
   public static final Codec<OceanRuinFeatureConfig> CODEC = RecordCodecBuilder.create((instance) -> {
      return instance.group(OceanRuinFeature.BiomeType.CODEC.fieldOf("biome_temp").forGetter((oceanRuinFeatureConfig) -> {
         return oceanRuinFeatureConfig.biomeType;
      }), Codec.floatRange(0.0F, 1.0F).fieldOf("large_probability").forGetter((oceanRuinFeatureConfig) -> {
         return oceanRuinFeatureConfig.largeProbability;
      }), Codec.floatRange(0.0F, 1.0F).fieldOf("cluster_probability").forGetter((oceanRuinFeatureConfig) -> {
         return oceanRuinFeatureConfig.clusterProbability;
      })).apply(instance, (Function3)(OceanRuinFeatureConfig::new));
   });
   public final OceanRuinFeature.BiomeType biomeType;
   public final float largeProbability;
   public final float clusterProbability;

   public OceanRuinFeatureConfig(OceanRuinFeature.BiomeType biomeType, float largeProbability, float clusterProbability) {
      this.biomeType = biomeType;
      this.largeProbability = largeProbability;
      this.clusterProbability = clusterProbability;
   }
}
