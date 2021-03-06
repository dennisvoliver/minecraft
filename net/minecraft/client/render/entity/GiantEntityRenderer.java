package net.minecraft.client.render.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.entity.feature.ArmorFeatureRenderer;
import net.minecraft.client.render.entity.feature.HeldItemFeatureRenderer;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.model.GiantEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.mob.GiantEntity;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public class GiantEntityRenderer extends MobEntityRenderer<GiantEntity, BipedEntityModel<GiantEntity>> {
   private static final Identifier TEXTURE = new Identifier("textures/entity/zombie/zombie.png");
   private final float scale;

   public GiantEntityRenderer(EntityRenderDispatcher dispatcher, float f) {
      super(dispatcher, new GiantEntityModel(), 0.5F * f);
      this.scale = f;
      this.addFeature(new HeldItemFeatureRenderer(this));
      this.addFeature(new ArmorFeatureRenderer(this, new GiantEntityModel(0.5F, true), new GiantEntityModel(1.0F, true)));
   }

   protected void scale(GiantEntity giantEntity, MatrixStack matrixStack, float f) {
      matrixStack.scale(this.scale, this.scale, this.scale);
   }

   public Identifier getTexture(GiantEntity giantEntity) {
      return TEXTURE;
   }
}
