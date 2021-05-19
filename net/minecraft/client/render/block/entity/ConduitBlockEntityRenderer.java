package net.minecraft.client.render.block.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.entity.ConduitBlockEntity;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.util.math.Vector3f;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Quaternion;

@Environment(EnvType.CLIENT)
public class ConduitBlockEntityRenderer extends BlockEntityRenderer<ConduitBlockEntity> {
   public static final SpriteIdentifier BASE_TEXTURE;
   public static final SpriteIdentifier CAGE_TEXTURE;
   public static final SpriteIdentifier WIND_TEXTURE;
   public static final SpriteIdentifier WIND_VERTICAL_TEXTURE;
   public static final SpriteIdentifier OPEN_EYE_TEXTURE;
   public static final SpriteIdentifier CLOSED_EYE_TEXTURE;
   private final ModelPart field_20823 = new ModelPart(16, 16, 0, 0);
   private final ModelPart field_20824;
   private final ModelPart field_20825;
   private final ModelPart field_20826;

   public ConduitBlockEntityRenderer(BlockEntityRenderDispatcher blockEntityRenderDispatcher) {
      super(blockEntityRenderDispatcher);
      this.field_20823.addCuboid(-4.0F, -4.0F, 0.0F, 8.0F, 8.0F, 0.0F, 0.01F);
      this.field_20824 = new ModelPart(64, 32, 0, 0);
      this.field_20824.addCuboid(-8.0F, -8.0F, -8.0F, 16.0F, 16.0F, 16.0F);
      this.field_20825 = new ModelPart(32, 16, 0, 0);
      this.field_20825.addCuboid(-3.0F, -3.0F, -3.0F, 6.0F, 6.0F, 6.0F);
      this.field_20826 = new ModelPart(32, 16, 0, 0);
      this.field_20826.addCuboid(-4.0F, -4.0F, -4.0F, 8.0F, 8.0F, 8.0F);
   }

   public void render(ConduitBlockEntity conduitBlockEntity, float f, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, int j) {
      float g = (float)conduitBlockEntity.ticks + f;
      float k;
      if (!conduitBlockEntity.isActive()) {
         k = conduitBlockEntity.getRotation(0.0F);
         VertexConsumer vertexConsumer = BASE_TEXTURE.getVertexConsumer(vertexConsumerProvider, RenderLayer::getEntitySolid);
         matrixStack.push();
         matrixStack.translate(0.5D, 0.5D, 0.5D);
         matrixStack.multiply(Vector3f.POSITIVE_Y.getDegreesQuaternion(k));
         this.field_20825.render(matrixStack, vertexConsumer, i, j);
         matrixStack.pop();
      } else {
         k = conduitBlockEntity.getRotation(f) * 57.295776F;
         float l = MathHelper.sin(g * 0.1F) / 2.0F + 0.5F;
         l += l * l;
         matrixStack.push();
         matrixStack.translate(0.5D, (double)(0.3F + l * 0.2F), 0.5D);
         Vector3f vector3f = new Vector3f(0.5F, 1.0F, 0.5F);
         vector3f.normalize();
         matrixStack.multiply(new Quaternion(vector3f, k, true));
         this.field_20826.render(matrixStack, CAGE_TEXTURE.getVertexConsumer(vertexConsumerProvider, RenderLayer::getEntityCutoutNoCull), i, j);
         matrixStack.pop();
         int m = conduitBlockEntity.ticks / 66 % 3;
         matrixStack.push();
         matrixStack.translate(0.5D, 0.5D, 0.5D);
         if (m == 1) {
            matrixStack.multiply(Vector3f.POSITIVE_X.getDegreesQuaternion(90.0F));
         } else if (m == 2) {
            matrixStack.multiply(Vector3f.POSITIVE_Z.getDegreesQuaternion(90.0F));
         }

         VertexConsumer vertexConsumer2 = (m == 1 ? WIND_VERTICAL_TEXTURE : WIND_TEXTURE).getVertexConsumer(vertexConsumerProvider, RenderLayer::getEntityCutoutNoCull);
         this.field_20824.render(matrixStack, vertexConsumer2, i, j);
         matrixStack.pop();
         matrixStack.push();
         matrixStack.translate(0.5D, 0.5D, 0.5D);
         matrixStack.scale(0.875F, 0.875F, 0.875F);
         matrixStack.multiply(Vector3f.POSITIVE_X.getDegreesQuaternion(180.0F));
         matrixStack.multiply(Vector3f.POSITIVE_Z.getDegreesQuaternion(180.0F));
         this.field_20824.render(matrixStack, vertexConsumer2, i, j);
         matrixStack.pop();
         Camera camera = this.dispatcher.camera;
         matrixStack.push();
         matrixStack.translate(0.5D, (double)(0.3F + l * 0.2F), 0.5D);
         matrixStack.scale(0.5F, 0.5F, 0.5F);
         float n = -camera.getYaw();
         matrixStack.multiply(Vector3f.POSITIVE_Y.getDegreesQuaternion(n));
         matrixStack.multiply(Vector3f.POSITIVE_X.getDegreesQuaternion(camera.getPitch()));
         matrixStack.multiply(Vector3f.POSITIVE_Z.getDegreesQuaternion(180.0F));
         float o = 1.3333334F;
         matrixStack.scale(1.3333334F, 1.3333334F, 1.3333334F);
         this.field_20823.render(matrixStack, (conduitBlockEntity.isEyeOpen() ? OPEN_EYE_TEXTURE : CLOSED_EYE_TEXTURE).getVertexConsumer(vertexConsumerProvider, RenderLayer::getEntityCutoutNoCull), i, j);
         matrixStack.pop();
      }
   }

   static {
      BASE_TEXTURE = new SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE, new Identifier("entity/conduit/base"));
      CAGE_TEXTURE = new SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE, new Identifier("entity/conduit/cage"));
      WIND_TEXTURE = new SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE, new Identifier("entity/conduit/wind"));
      WIND_VERTICAL_TEXTURE = new SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE, new Identifier("entity/conduit/wind_vertical"));
      OPEN_EYE_TEXTURE = new SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE, new Identifier("entity/conduit/open_eye"));
      CLOSED_EYE_TEXTURE = new SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE, new Identifier("entity/conduit/closed_eye"));
   }
}
