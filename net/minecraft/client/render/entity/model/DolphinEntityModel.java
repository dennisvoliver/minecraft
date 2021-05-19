package net.minecraft.client.render.entity.model;

import com.google.common.collect.ImmutableList;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.ModelPart;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;

@Environment(EnvType.CLIENT)
public class DolphinEntityModel<T extends Entity> extends CompositeEntityModel<T> {
   private final ModelPart body;
   private final ModelPart tail;
   private final ModelPart flukes;

   public DolphinEntityModel() {
      this.textureWidth = 64;
      this.textureHeight = 64;
      float f = 18.0F;
      float g = -8.0F;
      this.body = new ModelPart(this, 22, 0);
      this.body.addCuboid(-4.0F, -7.0F, 0.0F, 8.0F, 7.0F, 13.0F);
      this.body.setPivot(0.0F, 22.0F, -5.0F);
      ModelPart modelPart = new ModelPart(this, 51, 0);
      modelPart.addCuboid(-0.5F, 0.0F, 8.0F, 1.0F, 4.0F, 5.0F);
      modelPart.pitch = 1.0471976F;
      this.body.addChild(modelPart);
      ModelPart modelPart2 = new ModelPart(this, 48, 20);
      modelPart2.mirror = true;
      modelPart2.addCuboid(-0.5F, -4.0F, 0.0F, 1.0F, 4.0F, 7.0F);
      modelPart2.setPivot(2.0F, -2.0F, 4.0F);
      modelPart2.pitch = 1.0471976F;
      modelPart2.roll = 2.0943952F;
      this.body.addChild(modelPart2);
      ModelPart modelPart3 = new ModelPart(this, 48, 20);
      modelPart3.addCuboid(-0.5F, -4.0F, 0.0F, 1.0F, 4.0F, 7.0F);
      modelPart3.setPivot(-2.0F, -2.0F, 4.0F);
      modelPart3.pitch = 1.0471976F;
      modelPart3.roll = -2.0943952F;
      this.body.addChild(modelPart3);
      this.tail = new ModelPart(this, 0, 19);
      this.tail.addCuboid(-2.0F, -2.5F, 0.0F, 4.0F, 5.0F, 11.0F);
      this.tail.setPivot(0.0F, -2.5F, 11.0F);
      this.tail.pitch = -0.10471976F;
      this.body.addChild(this.tail);
      this.flukes = new ModelPart(this, 19, 20);
      this.flukes.addCuboid(-5.0F, -0.5F, 0.0F, 10.0F, 1.0F, 6.0F);
      this.flukes.setPivot(0.0F, 0.0F, 9.0F);
      this.flukes.pitch = 0.0F;
      this.tail.addChild(this.flukes);
      ModelPart modelPart4 = new ModelPart(this, 0, 0);
      modelPart4.addCuboid(-4.0F, -3.0F, -3.0F, 8.0F, 7.0F, 6.0F);
      modelPart4.setPivot(0.0F, -4.0F, -3.0F);
      ModelPart modelPart5 = new ModelPart(this, 0, 13);
      modelPart5.addCuboid(-1.0F, 2.0F, -7.0F, 2.0F, 2.0F, 4.0F);
      modelPart4.addChild(modelPart5);
      this.body.addChild(modelPart4);
   }

   public Iterable<ModelPart> getParts() {
      return ImmutableList.of(this.body);
   }

   public void setAngles(T entity, float limbAngle, float limbDistance, float animationProgress, float headYaw, float headPitch) {
      this.body.pitch = headPitch * 0.017453292F;
      this.body.yaw = headYaw * 0.017453292F;
      if (Entity.squaredHorizontalLength(entity.getVelocity()) > 1.0E-7D) {
         ModelPart var10000 = this.body;
         var10000.pitch += -0.05F + -0.05F * MathHelper.cos(animationProgress * 0.3F);
         this.tail.pitch = -0.1F * MathHelper.cos(animationProgress * 0.3F);
         this.flukes.pitch = -0.2F * MathHelper.cos(animationProgress * 0.3F);
      }

   }
}
