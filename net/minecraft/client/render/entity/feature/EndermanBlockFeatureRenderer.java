package net.minecraft.client.render.entity.feature;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.model.EndermanEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.util.math.Vector3f;
import net.minecraft.entity.mob.EndermanEntity;

@Environment(EnvType.CLIENT)
public class EndermanBlockFeatureRenderer extends FeatureRenderer<EndermanEntity, EndermanEntityModel<EndermanEntity>> {
   public EndermanBlockFeatureRenderer(FeatureRendererContext<EndermanEntity, EndermanEntityModel<EndermanEntity>> featureRendererContext) {
      super(featureRendererContext);
   }

   public void render(MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, EndermanEntity endermanEntity, float f, float g, float h, float j, float k, float l) {
      BlockState blockState = endermanEntity.getCarriedBlock();
      if (blockState != null) {
         matrixStack.push();
         matrixStack.translate(0.0D, 0.6875D, -0.75D);
         matrixStack.multiply(Vector3f.POSITIVE_X.getDegreesQuaternion(20.0F));
         matrixStack.multiply(Vector3f.POSITIVE_Y.getDegreesQuaternion(45.0F));
         matrixStack.translate(0.25D, 0.1875D, 0.25D);
         float m = 0.5F;
         matrixStack.scale(-0.5F, -0.5F, 0.5F);
         matrixStack.multiply(Vector3f.POSITIVE_Y.getDegreesQuaternion(90.0F));
         MinecraftClient.getInstance().getBlockRenderManager().renderBlockAsEntity(blockState, matrixStack, vertexConsumerProvider, i, OverlayTexture.DEFAULT_UV);
         matrixStack.pop();
      }
   }
}
