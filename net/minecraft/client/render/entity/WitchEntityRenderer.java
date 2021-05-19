package net.minecraft.client.render.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.feature.WitchHeldItemFeatureRenderer;
import net.minecraft.client.render.entity.model.WitchEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.WitchEntity;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public class WitchEntityRenderer extends MobEntityRenderer<WitchEntity, WitchEntityModel<WitchEntity>> {
   private static final Identifier TEXTURE = new Identifier("textures/entity/witch.png");

   public WitchEntityRenderer(EntityRenderDispatcher entityRenderDispatcher) {
      super(entityRenderDispatcher, new WitchEntityModel(0.0F), 0.5F);
      this.addFeature(new WitchHeldItemFeatureRenderer(this));
   }

   public void render(WitchEntity witchEntity, float f, float g, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i) {
      ((WitchEntityModel)this.model).setLiftingNose(!witchEntity.getMainHandStack().isEmpty());
      super.render((MobEntity)witchEntity, f, g, matrixStack, vertexConsumerProvider, i);
   }

   public Identifier getTexture(WitchEntity witchEntity) {
      return TEXTURE;
   }

   protected void scale(WitchEntity witchEntity, MatrixStack matrixStack, float f) {
      float g = 0.9375F;
      matrixStack.scale(0.9375F, 0.9375F, 0.9375F);
   }
}
