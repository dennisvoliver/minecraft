package net.minecraft.client.render.block.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.entity.BellBlockEntity;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;

@Environment(EnvType.CLIENT)
public class BellBlockEntityRenderer extends BlockEntityRenderer<BellBlockEntity> {
   public static final SpriteIdentifier BELL_BODY_TEXTURE;
   private final ModelPart field_20816 = new ModelPart(32, 32, 0, 0);

   public BellBlockEntityRenderer(BlockEntityRenderDispatcher blockEntityRenderDispatcher) {
      super(blockEntityRenderDispatcher);
      this.field_20816.addCuboid(-3.0F, -6.0F, -3.0F, 6.0F, 7.0F, 6.0F);
      this.field_20816.setPivot(8.0F, 12.0F, 8.0F);
      ModelPart modelPart = new ModelPart(32, 32, 0, 13);
      modelPart.addCuboid(4.0F, 4.0F, 4.0F, 8.0F, 2.0F, 8.0F);
      modelPart.setPivot(-8.0F, -12.0F, -8.0F);
      this.field_20816.addChild(modelPart);
   }

   public void render(BellBlockEntity bellBlockEntity, float f, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, int j) {
      float g = (float)bellBlockEntity.ringTicks + f;
      float h = 0.0F;
      float k = 0.0F;
      if (bellBlockEntity.ringing) {
         float l = MathHelper.sin(g / 3.1415927F) / (4.0F + g / 3.0F);
         if (bellBlockEntity.lastSideHit == Direction.NORTH) {
            h = -l;
         } else if (bellBlockEntity.lastSideHit == Direction.SOUTH) {
            h = l;
         } else if (bellBlockEntity.lastSideHit == Direction.EAST) {
            k = -l;
         } else if (bellBlockEntity.lastSideHit == Direction.WEST) {
            k = l;
         }
      }

      this.field_20816.pitch = h;
      this.field_20816.roll = k;
      VertexConsumer vertexConsumer = BELL_BODY_TEXTURE.getVertexConsumer(vertexConsumerProvider, RenderLayer::getEntitySolid);
      this.field_20816.render(matrixStack, vertexConsumer, i, j);
   }

   static {
      BELL_BODY_TEXTURE = new SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE, new Identifier("entity/bell/bell_body"));
   }
}
