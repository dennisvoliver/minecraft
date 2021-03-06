package net.minecraft.client.render.entity.feature;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.ShulkerEntityRenderer;
import net.minecraft.client.render.entity.model.ShulkerEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.mob.ShulkerEntity;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Quaternion;

@Environment(EnvType.CLIENT)
public class ShulkerHeadFeatureRenderer extends FeatureRenderer<ShulkerEntity, ShulkerEntityModel<ShulkerEntity>> {
   public ShulkerHeadFeatureRenderer(FeatureRendererContext<ShulkerEntity, ShulkerEntityModel<ShulkerEntity>> featureRendererContext) {
      super(featureRendererContext);
   }

   public void render(MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, ShulkerEntity shulkerEntity, float f, float g, float h, float j, float k, float l) {
      matrixStack.push();
      matrixStack.translate(0.0D, 1.0D, 0.0D);
      matrixStack.scale(-1.0F, -1.0F, 1.0F);
      Quaternion quaternion = shulkerEntity.getAttachedFace().getOpposite().getRotationQuaternion();
      quaternion.conjugate();
      matrixStack.multiply(quaternion);
      matrixStack.scale(-1.0F, -1.0F, 1.0F);
      matrixStack.translate(0.0D, -1.0D, 0.0D);
      DyeColor dyeColor = shulkerEntity.getColor();
      Identifier identifier = dyeColor == null ? ShulkerEntityRenderer.TEXTURE : ShulkerEntityRenderer.COLORED_TEXTURES[dyeColor.getId()];
      VertexConsumer vertexConsumer = vertexConsumerProvider.getBuffer(RenderLayer.getEntitySolid(identifier));
      ((ShulkerEntityModel)this.getContextModel()).getHead().render(matrixStack, vertexConsumer, i, LivingEntityRenderer.getOverlay(shulkerEntity, 0.0F));
      matrixStack.pop();
   }
}
