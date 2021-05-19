package net.minecraft.client.render.entity.model;

import com.google.common.collect.ImmutableList;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.ModelPart;
import net.minecraft.entity.Entity;

@Environment(EnvType.CLIENT)
public class LeashKnotEntityModel<T extends Entity> extends CompositeEntityModel<T> {
   private final ModelPart leashKnot;

   public LeashKnotEntityModel() {
      this.textureWidth = 32;
      this.textureHeight = 32;
      this.leashKnot = new ModelPart(this, 0, 0);
      this.leashKnot.addCuboid(-3.0F, -6.0F, -3.0F, 6.0F, 8.0F, 6.0F, 0.0F);
      this.leashKnot.setPivot(0.0F, 0.0F, 0.0F);
   }

   public Iterable<ModelPart> getParts() {
      return ImmutableList.of(this.leashKnot);
   }

   public void setAngles(T entity, float limbAngle, float limbDistance, float animationProgress, float headYaw, float headPitch) {
      this.leashKnot.yaw = headYaw * 0.017453292F;
      this.leashKnot.pitch = headPitch * 0.017453292F;
   }
}
