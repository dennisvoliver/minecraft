package net.minecraft.client.render.entity.model;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import java.util.Arrays;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.ModelPart;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.util.math.MathHelper;

@Environment(EnvType.CLIENT)
public class BoatEntityModel extends CompositeEntityModel<BoatEntity> {
   private final ModelPart[] paddles = new ModelPart[2];
   private final ModelPart bottom;
   private final ImmutableList<ModelPart> parts;

   public BoatEntityModel() {
      ModelPart[] modelParts = new ModelPart[]{(new ModelPart(this, 0, 0)).setTextureSize(128, 64), (new ModelPart(this, 0, 19)).setTextureSize(128, 64), (new ModelPart(this, 0, 27)).setTextureSize(128, 64), (new ModelPart(this, 0, 35)).setTextureSize(128, 64), (new ModelPart(this, 0, 43)).setTextureSize(128, 64)};
      int i = true;
      int j = true;
      int k = true;
      int l = true;
      int m = true;
      modelParts[0].addCuboid(-14.0F, -9.0F, -3.0F, 28.0F, 16.0F, 3.0F, 0.0F);
      modelParts[0].setPivot(0.0F, 3.0F, 1.0F);
      modelParts[1].addCuboid(-13.0F, -7.0F, -1.0F, 18.0F, 6.0F, 2.0F, 0.0F);
      modelParts[1].setPivot(-15.0F, 4.0F, 4.0F);
      modelParts[2].addCuboid(-8.0F, -7.0F, -1.0F, 16.0F, 6.0F, 2.0F, 0.0F);
      modelParts[2].setPivot(15.0F, 4.0F, 0.0F);
      modelParts[3].addCuboid(-14.0F, -7.0F, -1.0F, 28.0F, 6.0F, 2.0F, 0.0F);
      modelParts[3].setPivot(0.0F, 4.0F, -9.0F);
      modelParts[4].addCuboid(-14.0F, -7.0F, -1.0F, 28.0F, 6.0F, 2.0F, 0.0F);
      modelParts[4].setPivot(0.0F, 4.0F, 9.0F);
      modelParts[0].pitch = 1.5707964F;
      modelParts[1].yaw = 4.712389F;
      modelParts[2].yaw = 1.5707964F;
      modelParts[3].yaw = 3.1415927F;
      this.paddles[0] = this.makePaddle(true);
      this.paddles[0].setPivot(3.0F, -5.0F, 9.0F);
      this.paddles[1] = this.makePaddle(false);
      this.paddles[1].setPivot(3.0F, -5.0F, -9.0F);
      this.paddles[1].yaw = 3.1415927F;
      this.paddles[0].roll = 0.19634955F;
      this.paddles[1].roll = 0.19634955F;
      this.bottom = (new ModelPart(this, 0, 0)).setTextureSize(128, 64);
      this.bottom.addCuboid(-14.0F, -9.0F, -3.0F, 28.0F, 16.0F, 3.0F, 0.0F);
      this.bottom.setPivot(0.0F, -3.0F, 1.0F);
      this.bottom.pitch = 1.5707964F;
      Builder<ModelPart> builder = ImmutableList.builder();
      builder.addAll((Iterable)Arrays.asList(modelParts));
      builder.addAll((Iterable)Arrays.asList(this.paddles));
      this.parts = builder.build();
   }

   public void setAngles(BoatEntity boatEntity, float f, float g, float h, float i, float j) {
      this.setPaddleAngle(boatEntity, 0, f);
      this.setPaddleAngle(boatEntity, 1, f);
   }

   public ImmutableList<ModelPart> getParts() {
      return this.parts;
   }

   public ModelPart getBottom() {
      return this.bottom;
   }

   protected ModelPart makePaddle(boolean isLeft) {
      ModelPart modelPart = (new ModelPart(this, 62, isLeft ? 0 : 20)).setTextureSize(128, 64);
      int i = true;
      int j = true;
      int k = true;
      float f = -5.0F;
      modelPart.addCuboid(-1.0F, 0.0F, -5.0F, 2.0F, 2.0F, 18.0F);
      modelPart.addCuboid(isLeft ? -1.001F : 0.001F, -3.0F, 8.0F, 1.0F, 6.0F, 7.0F);
      return modelPart;
   }

   protected void setPaddleAngle(BoatEntity boat, int paddle, float angle) {
      float f = boat.interpolatePaddlePhase(paddle, angle);
      ModelPart modelPart = this.paddles[paddle];
      modelPart.pitch = (float)MathHelper.clampedLerp(-1.0471975803375244D, -0.2617993950843811D, (double)((MathHelper.sin(-f) + 1.0F) / 2.0F));
      modelPart.yaw = (float)MathHelper.clampedLerp(-0.7853981852531433D, 0.7853981852531433D, (double)((MathHelper.sin(-f + 1.0F) + 1.0F) / 2.0F));
      if (paddle == 1) {
         modelPart.yaw = 3.1415927F - modelPart.yaw;
      }

   }
}
