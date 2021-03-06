package net.minecraft.client.render.entity.model;

import com.google.common.collect.ImmutableList;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.ModelPart;
import net.minecraft.entity.Entity;

@Environment(EnvType.CLIENT)
public class ShulkerBulletEntityModel<T extends Entity> extends CompositeEntityModel<T> {
   private final ModelPart bullet;

   public ShulkerBulletEntityModel() {
      this.textureWidth = 64;
      this.textureHeight = 32;
      this.bullet = new ModelPart(this);
      this.bullet.setTextureOffset(0, 0).addCuboid(-4.0F, -4.0F, -1.0F, 8.0F, 8.0F, 2.0F, 0.0F);
      this.bullet.setTextureOffset(0, 10).addCuboid(-1.0F, -4.0F, -4.0F, 2.0F, 8.0F, 8.0F, 0.0F);
      this.bullet.setTextureOffset(20, 0).addCuboid(-4.0F, -1.0F, -4.0F, 8.0F, 2.0F, 8.0F, 0.0F);
      this.bullet.setPivot(0.0F, 0.0F, 0.0F);
   }

   public Iterable<ModelPart> getParts() {
      return ImmutableList.of(this.bullet);
   }

   public void setAngles(T entity, float limbAngle, float limbDistance, float animationProgress, float headYaw, float headPitch) {
      this.bullet.yaw = headYaw * 0.017453292F;
      this.bullet.pitch = headPitch * 0.017453292F;
   }
}
