package net.minecraft.world.gen.feature;

import com.mojang.serialization.Codec;

public class ShipwreckFeatureConfig implements FeatureConfig {
   public static final Codec<ShipwreckFeatureConfig> CODEC;
   public final boolean isBeached;

   public ShipwreckFeatureConfig(boolean isBeached) {
      this.isBeached = isBeached;
   }

   static {
      CODEC = Codec.BOOL.fieldOf("is_beached").orElse(false).xmap(ShipwreckFeatureConfig::new, (shipwreckFeatureConfig) -> {
         return shipwreckFeatureConfig.isBeached;
      }).codec();
   }
}
