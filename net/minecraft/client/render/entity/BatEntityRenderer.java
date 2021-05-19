package net.minecraft.client.render.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.entity.model.BatEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.passive.BatEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

@Environment(EnvType.CLIENT)
public class BatEntityRenderer extends MobEntityRenderer<BatEntity, BatEntityModel> {
   private static final Identifier TEXTURE = new Identifier("textures/entity/bat.png");

   public BatEntityRenderer(EntityRenderDispatcher entityRenderDispatcher) {
      super(entityRenderDispatcher, new BatEntityModel(), 0.25F);
   }

   public Identifier getTexture(BatEntity batEntity) {
      return TEXTURE;
   }

   protected void scale(BatEntity batEntity, MatrixStack matrixStack, float f) {
      matrixStack.scale(0.35F, 0.35F, 0.35F);
   }

   protected void setupTransforms(BatEntity batEntity, MatrixStack matrixStack, float f, float g, float h) {
      if (batEntity.isRoosting()) {
         matrixStack.translate(0.0D, -0.10000000149011612D, 0.0D);
      } else {
         matrixStack.translate(0.0D, (double)(MathHelper.cos(f * 0.3F) * 0.1F), 0.0D);
      }

      super.setupTransforms(batEntity, matrixStack, f, g, h);
   }
}
