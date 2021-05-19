package net.minecraft.client.render.entity.feature;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.model.FoxEntityModel;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.util.math.Vector3f;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.passive.FoxEntity;
import net.minecraft.item.ItemStack;

@Environment(EnvType.CLIENT)
public class FoxHeldItemFeatureRenderer extends FeatureRenderer<FoxEntity, FoxEntityModel<FoxEntity>> {
   public FoxHeldItemFeatureRenderer(FeatureRendererContext<FoxEntity, FoxEntityModel<FoxEntity>> featureRendererContext) {
      super(featureRendererContext);
   }

   public void render(MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, FoxEntity foxEntity, float f, float g, float h, float j, float k, float l) {
      boolean bl = foxEntity.isSleeping();
      boolean bl2 = foxEntity.isBaby();
      matrixStack.push();
      float n;
      if (bl2) {
         n = 0.75F;
         matrixStack.scale(0.75F, 0.75F, 0.75F);
         matrixStack.translate(0.0D, 0.5D, 0.20937499403953552D);
      }

      matrixStack.translate((double)(((FoxEntityModel)this.getContextModel()).head.pivotX / 16.0F), (double)(((FoxEntityModel)this.getContextModel()).head.pivotY / 16.0F), (double)(((FoxEntityModel)this.getContextModel()).head.pivotZ / 16.0F));
      n = foxEntity.getHeadRoll(h);
      matrixStack.multiply(Vector3f.POSITIVE_Z.getRadialQuaternion(n));
      matrixStack.multiply(Vector3f.POSITIVE_Y.getDegreesQuaternion(k));
      matrixStack.multiply(Vector3f.POSITIVE_X.getDegreesQuaternion(l));
      if (foxEntity.isBaby()) {
         if (bl) {
            matrixStack.translate(0.4000000059604645D, 0.25999999046325684D, 0.15000000596046448D);
         } else {
            matrixStack.translate(0.05999999865889549D, 0.25999999046325684D, -0.5D);
         }
      } else if (bl) {
         matrixStack.translate(0.46000000834465027D, 0.25999999046325684D, 0.2199999988079071D);
      } else {
         matrixStack.translate(0.05999999865889549D, 0.27000001072883606D, -0.5D);
      }

      matrixStack.multiply(Vector3f.POSITIVE_X.getDegreesQuaternion(90.0F));
      if (bl) {
         matrixStack.multiply(Vector3f.POSITIVE_Z.getDegreesQuaternion(90.0F));
      }

      ItemStack itemStack = foxEntity.getEquippedStack(EquipmentSlot.MAINHAND);
      MinecraftClient.getInstance().getHeldItemRenderer().renderItem(foxEntity, itemStack, ModelTransformation.Mode.GROUND, false, matrixStack, vertexConsumerProvider, i);
      matrixStack.pop();
   }
}
