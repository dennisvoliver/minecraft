package net.minecraft.client.render.block.entity;

import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.AbstractSignBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SignBlock;
import net.minecraft.block.WallSignBlock;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.TexturedRenderLayers;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.util.math.Vector3f;
import net.minecraft.text.OrderedText;
import net.minecraft.util.SignType;
import net.minecraft.util.math.Direction;

@Environment(EnvType.CLIENT)
public class SignBlockEntityRenderer extends BlockEntityRenderer<SignBlockEntity> {
   private final SignBlockEntityRenderer.SignModel model = new SignBlockEntityRenderer.SignModel();

   public SignBlockEntityRenderer(BlockEntityRenderDispatcher blockEntityRenderDispatcher) {
      super(blockEntityRenderDispatcher);
   }

   public void render(SignBlockEntity signBlockEntity, float f, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, int j) {
      BlockState blockState = signBlockEntity.getCachedState();
      matrixStack.push();
      float g = 0.6666667F;
      float h;
      if (blockState.getBlock() instanceof SignBlock) {
         matrixStack.translate(0.5D, 0.5D, 0.5D);
         h = -((float)((Integer)blockState.get(SignBlock.ROTATION) * 360) / 16.0F);
         matrixStack.multiply(Vector3f.POSITIVE_Y.getDegreesQuaternion(h));
         this.model.foot.visible = true;
      } else {
         matrixStack.translate(0.5D, 0.5D, 0.5D);
         h = -((Direction)blockState.get(WallSignBlock.FACING)).asRotation();
         matrixStack.multiply(Vector3f.POSITIVE_Y.getDegreesQuaternion(h));
         matrixStack.translate(0.0D, -0.3125D, -0.4375D);
         this.model.foot.visible = false;
      }

      matrixStack.push();
      matrixStack.scale(0.6666667F, -0.6666667F, -0.6666667F);
      SpriteIdentifier spriteIdentifier = getModelTexture(blockState.getBlock());
      SignBlockEntityRenderer.SignModel var10002 = this.model;
      var10002.getClass();
      VertexConsumer vertexConsumer = spriteIdentifier.getVertexConsumer(vertexConsumerProvider, var10002::getLayer);
      this.model.field.render(matrixStack, vertexConsumer, i, j);
      this.model.foot.render(matrixStack, vertexConsumer, i, j);
      matrixStack.pop();
      TextRenderer textRenderer = this.dispatcher.getTextRenderer();
      float l = 0.010416667F;
      matrixStack.translate(0.0D, 0.3333333432674408D, 0.046666666865348816D);
      matrixStack.scale(0.010416667F, -0.010416667F, 0.010416667F);
      int m = signBlockEntity.getTextColor().getSignColor();
      double d = 0.4D;
      int n = (int)((double)NativeImage.getRed(m) * 0.4D);
      int o = (int)((double)NativeImage.getGreen(m) * 0.4D);
      int p = (int)((double)NativeImage.getBlue(m) * 0.4D);
      int q = NativeImage.getAbgrColor(0, p, o, n);
      int r = true;

      for(int s = 0; s < 4; ++s) {
         OrderedText orderedText = signBlockEntity.getTextBeingEditedOnRow(s, (text) -> {
            List<OrderedText> list = textRenderer.wrapLines(text, 90);
            return list.isEmpty() ? OrderedText.EMPTY : (OrderedText)list.get(0);
         });
         if (orderedText != null) {
            float t = (float)(-textRenderer.getWidth(orderedText) / 2);
            textRenderer.draw((OrderedText)orderedText, t, (float)(s * 10 - 20), q, false, matrixStack.peek().getModel(), vertexConsumerProvider, false, 0, i);
         }
      }

      matrixStack.pop();
   }

   public static SpriteIdentifier getModelTexture(Block block) {
      SignType signType2;
      if (block instanceof AbstractSignBlock) {
         signType2 = ((AbstractSignBlock)block).getSignType();
      } else {
         signType2 = SignType.OAK;
      }

      return TexturedRenderLayers.getSignTextureId(signType2);
   }

   @Environment(EnvType.CLIENT)
   public static final class SignModel extends Model {
      public final ModelPart field = new ModelPart(64, 32, 0, 0);
      public final ModelPart foot;

      public SignModel() {
         super(RenderLayer::getEntityCutoutNoCull);
         this.field.addCuboid(-12.0F, -14.0F, -1.0F, 24.0F, 12.0F, 2.0F, 0.0F);
         this.foot = new ModelPart(64, 32, 0, 14);
         this.foot.addCuboid(-1.0F, -2.0F, -1.0F, 2.0F, 14.0F, 2.0F, 0.0F);
      }

      public void render(MatrixStack matrices, VertexConsumer vertices, int light, int overlay, float red, float green, float blue, float alpha) {
         this.field.render(matrices, vertices, light, overlay, red, green, blue, alpha);
         this.foot.render(matrices, vertices, light, overlay, red, green, blue, alpha);
      }
   }
}
