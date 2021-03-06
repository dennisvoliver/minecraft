package net.minecraft.client.render.entity.feature;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.entity.model.CreeperEntityModel;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public class CreeperChargeFeatureRenderer extends EnergySwirlOverlayFeatureRenderer<CreeperEntity, CreeperEntityModel<CreeperEntity>> {
   private static final Identifier SKIN = new Identifier("textures/entity/creeper/creeper_armor.png");
   private final CreeperEntityModel<CreeperEntity> model = new CreeperEntityModel(2.0F);

   public CreeperChargeFeatureRenderer(FeatureRendererContext<CreeperEntity, CreeperEntityModel<CreeperEntity>> featureRendererContext) {
      super(featureRendererContext);
   }

   protected float getEnergySwirlX(float partialAge) {
      return partialAge * 0.01F;
   }

   protected Identifier getEnergySwirlTexture() {
      return SKIN;
   }

   protected EntityModel<CreeperEntity> getEnergySwirlModel() {
      return this.model;
   }
}
