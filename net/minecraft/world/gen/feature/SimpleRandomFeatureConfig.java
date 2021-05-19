package net.minecraft.world.gen.feature;

import com.mojang.serialization.Codec;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class SimpleRandomFeatureConfig implements FeatureConfig {
   public static final Codec<SimpleRandomFeatureConfig> CODEC;
   public final List<Supplier<ConfiguredFeature<?, ?>>> features;

   public SimpleRandomFeatureConfig(List<Supplier<ConfiguredFeature<?, ?>>> features) {
      this.features = features;
   }

   public Stream<ConfiguredFeature<?, ?>> method_30649() {
      return this.features.stream().flatMap((supplier) -> {
         return ((ConfiguredFeature)supplier.get()).method_30648();
      });
   }

   static {
      CODEC = ConfiguredFeature.field_26756.fieldOf("features").xmap(SimpleRandomFeatureConfig::new, (simpleRandomFeatureConfig) -> {
         return simpleRandomFeatureConfig.features;
      }).codec();
   }
}
