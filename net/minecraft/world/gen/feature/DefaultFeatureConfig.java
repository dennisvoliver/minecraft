package net.minecraft.world.gen.feature;

import com.mojang.serialization.Codec;

public class DefaultFeatureConfig implements FeatureConfig {
   public static final Codec<DefaultFeatureConfig> CODEC = Codec.unit(() -> {
      return INSTANCE;
   });
   public static final DefaultFeatureConfig INSTANCE = new DefaultFeatureConfig();
}
