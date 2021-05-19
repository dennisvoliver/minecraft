package net.minecraft.world.gen.feature;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.function.BiFunction;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class RandomBooleanFeatureConfig implements FeatureConfig {
   public static final Codec<RandomBooleanFeatureConfig> CODEC = RecordCodecBuilder.create((instance) -> {
      return instance.group(ConfiguredFeature.REGISTRY_CODEC.fieldOf("feature_true").forGetter((randomBooleanFeatureConfig) -> {
         return randomBooleanFeatureConfig.featureTrue;
      }), ConfiguredFeature.REGISTRY_CODEC.fieldOf("feature_false").forGetter((randomBooleanFeatureConfig) -> {
         return randomBooleanFeatureConfig.featureFalse;
      })).apply(instance, (BiFunction)(RandomBooleanFeatureConfig::new));
   });
   public final Supplier<ConfiguredFeature<?, ?>> featureTrue;
   public final Supplier<ConfiguredFeature<?, ?>> featureFalse;

   public RandomBooleanFeatureConfig(Supplier<ConfiguredFeature<?, ?>> featureTrue, Supplier<ConfiguredFeature<?, ?>> featureFalse) {
      this.featureTrue = featureTrue;
      this.featureFalse = featureFalse;
   }

   public Stream<ConfiguredFeature<?, ?>> method_30649() {
      return Stream.concat(((ConfiguredFeature)this.featureTrue.get()).method_30648(), ((ConfiguredFeature)this.featureFalse.get()).method_30648());
   }
}
