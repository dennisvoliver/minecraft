package net.minecraft.client.render.entity.feature;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.model.HorseEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.passive.HorseBaseEntity;
import net.minecraft.entity.passive.HorseEntity;
import net.minecraft.item.DyeableHorseArmorItem;
import net.minecraft.item.HorseArmorItem;
import net.minecraft.item.ItemStack;

@Environment(EnvType.CLIENT)
public class HorseArmorFeatureRenderer extends FeatureRenderer<HorseEntity, HorseEntityModel<HorseEntity>> {
   private final HorseEntityModel<HorseEntity> model = new HorseEntityModel(0.1F);

   public HorseArmorFeatureRenderer(FeatureRendererContext<HorseEntity, HorseEntityModel<HorseEntity>> featureRendererContext) {
      super(featureRendererContext);
   }

   public void render(MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, HorseEntity horseEntity, float f, float g, float h, float j, float k, float l) {
      ItemStack itemStack = horseEntity.getArmorType();
      if (itemStack.getItem() instanceof HorseArmorItem) {
         HorseArmorItem horseArmorItem = (HorseArmorItem)itemStack.getItem();
         ((HorseEntityModel)this.getContextModel()).copyStateTo(this.model);
         this.model.animateModel((HorseBaseEntity)horseEntity, f, g, h);
         this.model.setAngles((HorseBaseEntity)horseEntity, f, g, j, k, l);
         float q;
         float r;
         float s;
         if (horseArmorItem instanceof DyeableHorseArmorItem) {
            int m = ((DyeableHorseArmorItem)horseArmorItem).getColor(itemStack);
            q = (float)(m >> 16 & 255) / 255.0F;
            r = (float)(m >> 8 & 255) / 255.0F;
            s = (float)(m & 255) / 255.0F;
         } else {
            q = 1.0F;
            r = 1.0F;
            s = 1.0F;
         }

         VertexConsumer vertexConsumer = vertexConsumerProvider.getBuffer(RenderLayer.getEntityCutoutNoCull(horseArmorItem.getEntityTexture()));
         this.model.render(matrixStack, vertexConsumer, i, OverlayTexture.DEFAULT_UV, q, r, s, 1.0F);
      }
   }
}
