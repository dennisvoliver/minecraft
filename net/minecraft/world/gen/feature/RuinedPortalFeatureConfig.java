package net.minecraft.world.gen.feature;

import com.mojang.serialization.Codec;

public class RuinedPortalFeatureConfig implements FeatureConfig {
   public static final Codec<RuinedPortalFeatureConfig> CODEC;
   public final RuinedPortalFeature.Type portalType;

   public RuinedPortalFeatureConfig(RuinedPortalFeature.Type portalType) {
      this.portalType = portalType;
   }

   static {
      CODEC = RuinedPortalFeature.Type.CODEC.fieldOf("portal_type").xmap(RuinedPortalFeatureConfig::new, (ruinedPortalFeatureConfig) -> {
         return ruinedPortalFeatureConfig.portalType;
      }).codec();
   }
}
