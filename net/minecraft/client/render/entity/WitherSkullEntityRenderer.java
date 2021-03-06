package net.minecraft.client.render.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.model.SkullEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.projectile.WitherSkullEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;

@Environment(EnvType.CLIENT)
public class WitherSkullEntityRenderer extends EntityRenderer<WitherSkullEntity> {
   private static final Identifier INVULNERABLE_TEXTURE = new Identifier("textures/entity/wither/wither_invulnerable.png");
   private static final Identifier TEXTURE = new Identifier("textures/entity/wither/wither.png");
   private final SkullEntityModel model = new SkullEntityModel();

   public WitherSkullEntityRenderer(EntityRenderDispatcher entityRenderDispatcher) {
      super(entityRenderDispatcher);
   }

   protected int getBlockLight(WitherSkullEntity witherSkullEntity, BlockPos blockPos) {
      return 15;
   }

   public void render(WitherSkullEntity witherSkullEntity, float f, float g, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i) {
      matrixStack.push();
      matrixStack.scale(-1.0F, -1.0F, 1.0F);
      float h = MathHelper.lerpAngle(witherSkullEntity.prevYaw, witherSkullEntity.yaw, g);
      float j = MathHelper.lerp(g, witherSkullEntity.prevPitch, witherSkullEntity.pitch);
      VertexConsumer vertexConsumer = vertexConsumerProvider.getBuffer(this.model.getLayer(this.getTexture(witherSkullEntity)));
      this.model.method_2821(0.0F, h, j);
      this.model.render(matrixStack, vertexConsumer, i, OverlayTexture.DEFAULT_UV, 1.0F, 1.0F, 1.0F, 1.0F);
      matrixStack.pop();
      super.render(witherSkullEntity, f, g, matrixStack, vertexConsumerProvider, i);
   }

   public Identifier getTexture(WitherSkullEntity witherSkullEntity) {
      return witherSkullEntity.isCharged() ? INVULNERABLE_TEXTURE : TEXTURE;
   }
}
