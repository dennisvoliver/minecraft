package net.minecraft.client.render.entity.feature;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.model.WitchEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.util.math.Vector3f;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

@Environment(EnvType.CLIENT)
public class WitchHeldItemFeatureRenderer<T extends LivingEntity> extends VillagerHeldItemFeatureRenderer<T, WitchEntityModel<T>> {
   public WitchHeldItemFeatureRenderer(FeatureRendererContext<T, WitchEntityModel<T>> featureRendererContext) {
      super(featureRendererContext);
   }

   public void render(MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, T livingEntity, float f, float g, float h, float j, float k, float l) {
      ItemStack itemStack = livingEntity.getMainHandStack();
      matrixStack.push();
      if (itemStack.getItem() == Items.POTION) {
         ((WitchEntityModel)this.getContextModel()).getHead().rotate(matrixStack);
         ((WitchEntityModel)this.getContextModel()).getNose().rotate(matrixStack);
         matrixStack.translate(0.0625D, 0.25D, 0.0D);
         matrixStack.multiply(Vector3f.POSITIVE_Z.getDegreesQuaternion(180.0F));
         matrixStack.multiply(Vector3f.POSITIVE_X.getDegreesQuaternion(140.0F));
         matrixStack.multiply(Vector3f.POSITIVE_Z.getDegreesQuaternion(10.0F));
         matrixStack.translate(0.0D, -0.4000000059604645D, 0.4000000059604645D);
      }

      super.render(matrixStack, vertexConsumerProvider, i, livingEntity, f, g, h, j, k, l);
      matrixStack.pop();
   }
}
