package net.minecraft.client.render.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.LightType;

@Environment(EnvType.CLIENT)
public abstract class MobEntityRenderer<T extends MobEntity, M extends EntityModel<T>> extends LivingEntityRenderer<T, M> {
   public MobEntityRenderer(EntityRenderDispatcher entityRenderDispatcher, M entityModel, float f) {
      super(entityRenderDispatcher, entityModel, f);
   }

   protected boolean hasLabel(T mobEntity) {
      return super.hasLabel((LivingEntity)mobEntity) && (mobEntity.shouldRenderName() || mobEntity.hasCustomName() && mobEntity == this.dispatcher.targetedEntity);
   }

   public boolean shouldRender(T mobEntity, Frustum frustum, double d, double e, double f) {
      if (super.shouldRender(mobEntity, frustum, d, e, f)) {
         return true;
      } else {
         Entity entity = mobEntity.getHoldingEntity();
         return entity != null ? frustum.isVisible(entity.getVisibilityBoundingBox()) : false;
      }
   }

   public void render(T mobEntity, float f, float g, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i) {
      super.render((LivingEntity)mobEntity, f, g, matrixStack, vertexConsumerProvider, i);
      Entity entity = mobEntity.getHoldingEntity();
      if (entity != null) {
         this.method_4073(mobEntity, g, matrixStack, vertexConsumerProvider, entity);
      }
   }

   private <E extends Entity> void method_4073(T mobEntity, float f, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, E entity) {
      matrixStack.push();
      Vec3d vec3d = entity.method_30951(f);
      double d = (double)(MathHelper.lerp(f, mobEntity.bodyYaw, mobEntity.prevBodyYaw) * 0.017453292F) + 1.5707963267948966D;
      Vec3d vec3d2 = mobEntity.method_29919();
      double e = Math.cos(d) * vec3d2.z + Math.sin(d) * vec3d2.x;
      double g = Math.sin(d) * vec3d2.z - Math.cos(d) * vec3d2.x;
      double h = MathHelper.lerp((double)f, mobEntity.prevX, mobEntity.getX()) + e;
      double i = MathHelper.lerp((double)f, mobEntity.prevY, mobEntity.getY()) + vec3d2.y;
      double j = MathHelper.lerp((double)f, mobEntity.prevZ, mobEntity.getZ()) + g;
      matrixStack.translate(e, vec3d2.y, g);
      float k = (float)(vec3d.x - h);
      float l = (float)(vec3d.y - i);
      float m = (float)(vec3d.z - j);
      float n = 0.025F;
      VertexConsumer vertexConsumer = vertexConsumerProvider.getBuffer(RenderLayer.getLeash());
      Matrix4f matrix4f = matrixStack.peek().getModel();
      float o = MathHelper.fastInverseSqrt(k * k + m * m) * 0.025F / 2.0F;
      float p = m * o;
      float q = k * o;
      BlockPos blockPos = new BlockPos(mobEntity.getCameraPosVec(f));
      BlockPos blockPos2 = new BlockPos(entity.getCameraPosVec(f));
      int r = this.getBlockLight(mobEntity, blockPos);
      int s = this.dispatcher.getRenderer(entity).getBlockLight(entity, blockPos2);
      int t = mobEntity.world.getLightLevel(LightType.SKY, blockPos);
      int u = mobEntity.world.getLightLevel(LightType.SKY, blockPos2);
      method_23186(vertexConsumer, matrix4f, k, l, m, r, s, t, u, 0.025F, 0.025F, p, q);
      method_23186(vertexConsumer, matrix4f, k, l, m, r, s, t, u, 0.025F, 0.0F, p, q);
      matrixStack.pop();
   }

   public static void method_23186(VertexConsumer vertexConsumer, Matrix4f matrix4f, float f, float g, float h, int i, int j, int k, int l, float m, float n, float o, float p) {
      int q = true;

      for(int r = 0; r < 24; ++r) {
         float s = (float)r / 23.0F;
         int t = (int)MathHelper.lerp(s, (float)i, (float)j);
         int u = (int)MathHelper.lerp(s, (float)k, (float)l);
         int v = LightmapTextureManager.pack(t, u);
         method_23187(vertexConsumer, matrix4f, v, f, g, h, m, n, 24, r, false, o, p);
         method_23187(vertexConsumer, matrix4f, v, f, g, h, m, n, 24, r + 1, true, o, p);
      }

   }

   public static void method_23187(VertexConsumer vertexConsumer, Matrix4f matrix4f, int i, float f, float g, float h, float j, float k, int l, int m, boolean bl, float n, float o) {
      float p = 0.5F;
      float q = 0.4F;
      float r = 0.3F;
      if (m % 2 == 0) {
         p *= 0.7F;
         q *= 0.7F;
         r *= 0.7F;
      }

      float s = (float)m / (float)l;
      float t = f * s;
      float u = g > 0.0F ? g * s * s : g - g * (1.0F - s) * (1.0F - s);
      float v = h * s;
      if (!bl) {
         vertexConsumer.vertex(matrix4f, t + n, u + j - k, v - o).color(p, q, r, 1.0F).light(i).next();
      }

      vertexConsumer.vertex(matrix4f, t - n, u + k, v + o).color(p, q, r, 1.0F).light(i).next();
      if (bl) {
         vertexConsumer.vertex(matrix4f, t + n, u + j - k, v - o).color(p, q, r, 1.0F).light(i).next();
      }

   }
}
