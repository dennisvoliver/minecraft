package net.minecraft.client.render.entity.model;

import com.google.common.collect.ImmutableList;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.ModelPart;
import net.minecraft.entity.mob.RavagerEntity;
import net.minecraft.util.math.MathHelper;

@Environment(EnvType.CLIENT)
public class RavagerEntityModel extends CompositeEntityModel<RavagerEntity> {
   private final ModelPart head;
   private final ModelPart jaw;
   private final ModelPart torso;
   private final ModelPart rightBackLeg;
   private final ModelPart leftBackLeg;
   private final ModelPart rightFrontLeg;
   private final ModelPart leftFrontLeg;
   private final ModelPart neck;

   public RavagerEntityModel() {
      this.textureWidth = 128;
      this.textureHeight = 128;
      int i = true;
      float f = 0.0F;
      this.neck = new ModelPart(this);
      this.neck.setPivot(0.0F, -7.0F, -1.5F);
      this.neck.setTextureOffset(68, 73).addCuboid(-5.0F, -1.0F, -18.0F, 10.0F, 10.0F, 18.0F, 0.0F);
      this.head = new ModelPart(this);
      this.head.setPivot(0.0F, 16.0F, -17.0F);
      this.head.setTextureOffset(0, 0).addCuboid(-8.0F, -20.0F, -14.0F, 16.0F, 20.0F, 16.0F, 0.0F);
      this.head.setTextureOffset(0, 0).addCuboid(-2.0F, -6.0F, -18.0F, 4.0F, 8.0F, 4.0F, 0.0F);
      ModelPart modelPart = new ModelPart(this);
      modelPart.setPivot(-10.0F, -14.0F, -8.0F);
      modelPart.setTextureOffset(74, 55).addCuboid(0.0F, -14.0F, -2.0F, 2.0F, 14.0F, 4.0F, 0.0F);
      modelPart.pitch = 1.0995574F;
      this.head.addChild(modelPart);
      ModelPart modelPart2 = new ModelPart(this);
      modelPart2.mirror = true;
      modelPart2.setPivot(8.0F, -14.0F, -8.0F);
      modelPart2.setTextureOffset(74, 55).addCuboid(0.0F, -14.0F, -2.0F, 2.0F, 14.0F, 4.0F, 0.0F);
      modelPart2.pitch = 1.0995574F;
      this.head.addChild(modelPart2);
      this.jaw = new ModelPart(this);
      this.jaw.setPivot(0.0F, -2.0F, 2.0F);
      this.jaw.setTextureOffset(0, 36).addCuboid(-8.0F, 0.0F, -16.0F, 16.0F, 3.0F, 16.0F, 0.0F);
      this.head.addChild(this.jaw);
      this.neck.addChild(this.head);
      this.torso = new ModelPart(this);
      this.torso.setTextureOffset(0, 55).addCuboid(-7.0F, -10.0F, -7.0F, 14.0F, 16.0F, 20.0F, 0.0F);
      this.torso.setTextureOffset(0, 91).addCuboid(-6.0F, 6.0F, -7.0F, 12.0F, 13.0F, 18.0F, 0.0F);
      this.torso.setPivot(0.0F, 1.0F, 2.0F);
      this.rightBackLeg = new ModelPart(this, 96, 0);
      this.rightBackLeg.addCuboid(-4.0F, 0.0F, -4.0F, 8.0F, 37.0F, 8.0F, 0.0F);
      this.rightBackLeg.setPivot(-8.0F, -13.0F, 18.0F);
      this.leftBackLeg = new ModelPart(this, 96, 0);
      this.leftBackLeg.mirror = true;
      this.leftBackLeg.addCuboid(-4.0F, 0.0F, -4.0F, 8.0F, 37.0F, 8.0F, 0.0F);
      this.leftBackLeg.setPivot(8.0F, -13.0F, 18.0F);
      this.rightFrontLeg = new ModelPart(this, 64, 0);
      this.rightFrontLeg.addCuboid(-4.0F, 0.0F, -4.0F, 8.0F, 37.0F, 8.0F, 0.0F);
      this.rightFrontLeg.setPivot(-8.0F, -13.0F, -5.0F);
      this.leftFrontLeg = new ModelPart(this, 64, 0);
      this.leftFrontLeg.mirror = true;
      this.leftFrontLeg.addCuboid(-4.0F, 0.0F, -4.0F, 8.0F, 37.0F, 8.0F, 0.0F);
      this.leftFrontLeg.setPivot(8.0F, -13.0F, -5.0F);
   }

   public Iterable<ModelPart> getParts() {
      return ImmutableList.of(this.neck, this.torso, this.rightBackLeg, this.leftBackLeg, this.rightFrontLeg, this.leftFrontLeg);
   }

   public void setAngles(RavagerEntity ravagerEntity, float f, float g, float h, float i, float j) {
      this.head.pitch = j * 0.017453292F;
      this.head.yaw = i * 0.017453292F;
      this.torso.pitch = 1.5707964F;
      float k = 0.4F * g;
      this.rightBackLeg.pitch = MathHelper.cos(f * 0.6662F) * k;
      this.leftBackLeg.pitch = MathHelper.cos(f * 0.6662F + 3.1415927F) * k;
      this.rightFrontLeg.pitch = MathHelper.cos(f * 0.6662F + 3.1415927F) * k;
      this.leftFrontLeg.pitch = MathHelper.cos(f * 0.6662F) * k;
   }

   public void animateModel(RavagerEntity ravagerEntity, float f, float g, float h) {
      super.animateModel(ravagerEntity, f, g, h);
      int i = ravagerEntity.getStunTick();
      int j = ravagerEntity.getRoarTick();
      int k = true;
      int l = ravagerEntity.getAttackTick();
      int m = true;
      float n;
      float o;
      float u;
      if (l > 0) {
         n = MathHelper.method_24504((float)l - h, 10.0F);
         o = (1.0F + n) * 0.5F;
         float p = o * o * o * 12.0F;
         u = p * MathHelper.sin(this.neck.pitch);
         this.neck.pivotZ = -6.5F + p;
         this.neck.pivotY = -7.0F - u;
         float r = MathHelper.sin(((float)l - h) / 10.0F * 3.1415927F * 0.25F);
         this.jaw.pitch = 1.5707964F * r;
         if (l > 5) {
            this.jaw.pitch = MathHelper.sin(((float)(-4 + l) - h) / 4.0F) * 3.1415927F * 0.4F;
         } else {
            this.jaw.pitch = 0.15707964F * MathHelper.sin(3.1415927F * ((float)l - h) / 10.0F);
         }
      } else {
         n = -1.0F;
         o = -1.0F * MathHelper.sin(this.neck.pitch);
         this.neck.pivotX = 0.0F;
         this.neck.pivotY = -7.0F - o;
         this.neck.pivotZ = 5.5F;
         boolean bl = i > 0;
         this.neck.pitch = bl ? 0.21991149F : 0.0F;
         this.jaw.pitch = 3.1415927F * (bl ? 0.05F : 0.01F);
         if (bl) {
            double d = (double)i / 40.0D;
            this.neck.pivotX = (float)Math.sin(d * 10.0D) * 3.0F;
         } else if (j > 0) {
            u = MathHelper.sin(((float)(20 - j) - h) / 20.0F * 3.1415927F * 0.25F);
            this.jaw.pitch = 1.5707964F * u;
         }
      }

   }
}
