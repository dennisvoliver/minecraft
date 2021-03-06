package net.minecraft.client.render.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.mob.ElderGuardianEntity;
import net.minecraft.entity.mob.GuardianEntity;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public class ElderGuardianEntityRenderer extends GuardianEntityRenderer {
   public static final Identifier TEXTURE = new Identifier("textures/entity/guardian_elder.png");

   public ElderGuardianEntityRenderer(EntityRenderDispatcher entityRenderDispatcher) {
      super(entityRenderDispatcher, 1.2F);
   }

   protected void scale(GuardianEntity guardianEntity, MatrixStack matrixStack, float f) {
      matrixStack.scale(ElderGuardianEntity.SCALE, ElderGuardianEntity.SCALE, ElderGuardianEntity.SCALE);
   }

   public Identifier getTexture(GuardianEntity guardianEntity) {
      return TEXTURE;
   }
}
