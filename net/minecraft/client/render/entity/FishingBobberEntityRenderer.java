package net.minecraft.client.render.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.util.math.Vector3f;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.FishingBobberEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Arm;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Matrix3f;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Vec3d;

@Environment(EnvType.CLIENT)
public class FishingBobberEntityRenderer extends EntityRenderer<FishingBobberEntity> {
   private static final Identifier TEXTURE = new Identifier("textures/entity/fishing_hook.png");
   private static final RenderLayer LAYER;

   public FishingBobberEntityRenderer(EntityRenderDispatcher entityRenderDispatcher) {
      super(entityRenderDispatcher);
   }

   public void render(FishingBobberEntity fishingBobberEntity, float f, float g, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i) {
      PlayerEntity playerEntity = fishingBobberEntity.getPlayerOwner();
      if (playerEntity != null) {
         matrixStack.push();
         matrixStack.push();
         matrixStack.scale(0.5F, 0.5F, 0.5F);
         matrixStack.multiply(this.dispatcher.getRotation());
         matrixStack.multiply(Vector3f.POSITIVE_Y.getDegreesQuaternion(180.0F));
         MatrixStack.Entry entry = matrixStack.peek();
         Matrix4f matrix4f = entry.getModel();
         Matrix3f matrix3f = entry.getNormal();
         VertexConsumer vertexConsumer = vertexConsumerProvider.getBuffer(LAYER);
         method_23840(vertexConsumer, matrix4f, matrix3f, i, 0.0F, 0, 0, 1);
         method_23840(vertexConsumer, matrix4f, matrix3f, i, 1.0F, 0, 1, 1);
         method_23840(vertexConsumer, matrix4f, matrix3f, i, 1.0F, 1, 1, 0);
         method_23840(vertexConsumer, matrix4f, matrix3f, i, 0.0F, 1, 0, 0);
         matrixStack.pop();
         int j = playerEntity.getMainArm() == Arm.RIGHT ? 1 : -1;
         ItemStack itemStack = playerEntity.getMainHandStack();
         if (itemStack.getItem() != Items.FISHING_ROD) {
            j = -j;
         }

         float h = playerEntity.getHandSwingProgress(g);
         float k = MathHelper.sin(MathHelper.sqrt(h) * 3.1415927F);
         float l = MathHelper.lerp(g, playerEntity.prevBodyYaw, playerEntity.bodyYaw) * 0.017453292F;
         double d = (double)MathHelper.sin(l);
         double e = (double)MathHelper.cos(l);
         double m = (double)j * 0.35D;
         double n = 0.8D;
         double t;
         double u;
         double v;
         float w;
         double x;
         if ((this.dispatcher.gameOptions == null || this.dispatcher.gameOptions.getPerspective().isFirstPerson()) && playerEntity == MinecraftClient.getInstance().player) {
            x = this.dispatcher.gameOptions.fov;
            x /= 100.0D;
            Vec3d vec3d = new Vec3d((double)j * -0.36D * x, -0.045D * x, 0.4D);
            vec3d = vec3d.rotateX(-MathHelper.lerp(g, playerEntity.prevPitch, playerEntity.pitch) * 0.017453292F);
            vec3d = vec3d.rotateY(-MathHelper.lerp(g, playerEntity.prevYaw, playerEntity.yaw) * 0.017453292F);
            vec3d = vec3d.rotateY(k * 0.5F);
            vec3d = vec3d.rotateX(-k * 0.7F);
            t = MathHelper.lerp((double)g, playerEntity.prevX, playerEntity.getX()) + vec3d.x;
            u = MathHelper.lerp((double)g, playerEntity.prevY, playerEntity.getY()) + vec3d.y;
            v = MathHelper.lerp((double)g, playerEntity.prevZ, playerEntity.getZ()) + vec3d.z;
            w = playerEntity.getStandingEyeHeight();
         } else {
            t = MathHelper.lerp((double)g, playerEntity.prevX, playerEntity.getX()) - e * m - d * 0.8D;
            u = playerEntity.prevY + (double)playerEntity.getStandingEyeHeight() + (playerEntity.getY() - playerEntity.prevY) * (double)g - 0.45D;
            v = MathHelper.lerp((double)g, playerEntity.prevZ, playerEntity.getZ()) - d * m + e * 0.8D;
            w = playerEntity.isInSneakingPose() ? -0.1875F : 0.0F;
         }

         x = MathHelper.lerp((double)g, fishingBobberEntity.prevX, fishingBobberEntity.getX());
         double y = MathHelper.lerp((double)g, fishingBobberEntity.prevY, fishingBobberEntity.getY()) + 0.25D;
         double z = MathHelper.lerp((double)g, fishingBobberEntity.prevZ, fishingBobberEntity.getZ());
         float aa = (float)(t - x);
         float ab = (float)(u - y) + w;
         float ac = (float)(v - z);
         VertexConsumer vertexConsumer2 = vertexConsumerProvider.getBuffer(RenderLayer.getLines());
         Matrix4f matrix4f2 = matrixStack.peek().getModel();
         int ad = true;

         for(int ae = 0; ae < 16; ++ae) {
            method_23172(aa, ab, ac, vertexConsumer2, matrix4f2, method_23954(ae, 16));
            method_23172(aa, ab, ac, vertexConsumer2, matrix4f2, method_23954(ae + 1, 16));
         }

         matrixStack.pop();
         super.render(fishingBobberEntity, f, g, matrixStack, vertexConsumerProvider, i);
      }
   }

   private static float method_23954(int i, int j) {
      return (float)i / (float)j;
   }

   private static void method_23840(VertexConsumer vertexConsumer, Matrix4f matrix4f, Matrix3f matrix3f, int i, float f, int j, int k, int l) {
      vertexConsumer.vertex(matrix4f, f - 0.5F, (float)j - 0.5F, 0.0F).color(255, 255, 255, 255).texture((float)k, (float)l).overlay(OverlayTexture.DEFAULT_UV).light(i).normal(matrix3f, 0.0F, 1.0F, 0.0F).next();
   }

   private static void method_23172(float f, float g, float h, VertexConsumer vertexConsumer, Matrix4f matrix4f, float i) {
      vertexConsumer.vertex(matrix4f, f * i, g * (i * i + i) * 0.5F + 0.25F, h * i).color(0, 0, 0, 255).next();
   }

   public Identifier getTexture(FishingBobberEntity fishingBobberEntity) {
      return TEXTURE;
   }

   static {
      LAYER = RenderLayer.getEntityCutout(TEXTURE);
   }
}
