package net.minecraft.client.render.entity.model;

import com.google.common.collect.ImmutableList;
import java.util.function.Function;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Arm;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

@Environment(EnvType.CLIENT)
public class BipedEntityModel<T extends LivingEntity> extends AnimalModel<T> implements ModelWithArms, ModelWithHead {
   public ModelPart head;
   public ModelPart helmet;
   public ModelPart torso;
   public ModelPart rightArm;
   public ModelPart leftArm;
   public ModelPart rightLeg;
   public ModelPart leftLeg;
   public BipedEntityModel.ArmPose leftArmPose;
   public BipedEntityModel.ArmPose rightArmPose;
   public boolean sneaking;
   public float leaningPitch;

   public BipedEntityModel(float scale) {
      this(RenderLayer::getEntityCutoutNoCull, scale, 0.0F, 64, 32);
   }

   protected BipedEntityModel(float scale, float pivotY, int textureWidth, int textureHeight) {
      this(RenderLayer::getEntityCutoutNoCull, scale, pivotY, textureWidth, textureHeight);
   }

   public BipedEntityModel(Function<Identifier, RenderLayer> texturedLayerFactory, float scale, float pivotY, int textureWidth, int textureHeight) {
      super(texturedLayerFactory, true, 16.0F, 0.0F, 2.0F, 2.0F, 24.0F);
      this.leftArmPose = BipedEntityModel.ArmPose.EMPTY;
      this.rightArmPose = BipedEntityModel.ArmPose.EMPTY;
      this.textureWidth = textureWidth;
      this.textureHeight = textureHeight;
      this.head = new ModelPart(this, 0, 0);
      this.head.addCuboid(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, scale);
      this.head.setPivot(0.0F, 0.0F + pivotY, 0.0F);
      this.helmet = new ModelPart(this, 32, 0);
      this.helmet.addCuboid(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, scale + 0.5F);
      this.helmet.setPivot(0.0F, 0.0F + pivotY, 0.0F);
      this.torso = new ModelPart(this, 16, 16);
      this.torso.addCuboid(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, scale);
      this.torso.setPivot(0.0F, 0.0F + pivotY, 0.0F);
      this.rightArm = new ModelPart(this, 40, 16);
      this.rightArm.addCuboid(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, scale);
      this.rightArm.setPivot(-5.0F, 2.0F + pivotY, 0.0F);
      this.leftArm = new ModelPart(this, 40, 16);
      this.leftArm.mirror = true;
      this.leftArm.addCuboid(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, scale);
      this.leftArm.setPivot(5.0F, 2.0F + pivotY, 0.0F);
      this.rightLeg = new ModelPart(this, 0, 16);
      this.rightLeg.addCuboid(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, scale);
      this.rightLeg.setPivot(-1.9F, 12.0F + pivotY, 0.0F);
      this.leftLeg = new ModelPart(this, 0, 16);
      this.leftLeg.mirror = true;
      this.leftLeg.addCuboid(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, scale);
      this.leftLeg.setPivot(1.9F, 12.0F + pivotY, 0.0F);
   }

   protected Iterable<ModelPart> getHeadParts() {
      return ImmutableList.of(this.head);
   }

   protected Iterable<ModelPart> getBodyParts() {
      return ImmutableList.of(this.torso, this.rightArm, this.leftArm, this.rightLeg, this.leftLeg, this.helmet);
   }

   public void animateModel(T livingEntity, float f, float g, float h) {
      this.leaningPitch = livingEntity.getLeaningPitch(h);
      super.animateModel(livingEntity, f, g, h);
   }

   public void setAngles(T livingEntity, float f, float g, float h, float i, float j) {
      boolean bl = livingEntity.getRoll() > 4;
      boolean bl2 = livingEntity.isInSwimmingPose();
      this.head.yaw = i * 0.017453292F;
      if (bl) {
         this.head.pitch = -0.7853982F;
      } else if (this.leaningPitch > 0.0F) {
         if (bl2) {
            this.head.pitch = this.lerpAngle(this.leaningPitch, this.head.pitch, -0.7853982F);
         } else {
            this.head.pitch = this.lerpAngle(this.leaningPitch, this.head.pitch, j * 0.017453292F);
         }
      } else {
         this.head.pitch = j * 0.017453292F;
      }

      this.torso.yaw = 0.0F;
      this.rightArm.pivotZ = 0.0F;
      this.rightArm.pivotX = -5.0F;
      this.leftArm.pivotZ = 0.0F;
      this.leftArm.pivotX = 5.0F;
      float k = 1.0F;
      if (bl) {
         k = (float)livingEntity.getVelocity().lengthSquared();
         k /= 0.2F;
         k *= k * k;
      }

      if (k < 1.0F) {
         k = 1.0F;
      }

      this.rightArm.pitch = MathHelper.cos(f * 0.6662F + 3.1415927F) * 2.0F * g * 0.5F / k;
      this.leftArm.pitch = MathHelper.cos(f * 0.6662F) * 2.0F * g * 0.5F / k;
      this.rightArm.roll = 0.0F;
      this.leftArm.roll = 0.0F;
      this.rightLeg.pitch = MathHelper.cos(f * 0.6662F) * 1.4F * g / k;
      this.leftLeg.pitch = MathHelper.cos(f * 0.6662F + 3.1415927F) * 1.4F * g / k;
      this.rightLeg.yaw = 0.0F;
      this.leftLeg.yaw = 0.0F;
      this.rightLeg.roll = 0.0F;
      this.leftLeg.roll = 0.0F;
      ModelPart var10000;
      if (this.riding) {
         var10000 = this.rightArm;
         var10000.pitch += -0.62831855F;
         var10000 = this.leftArm;
         var10000.pitch += -0.62831855F;
         this.rightLeg.pitch = -1.4137167F;
         this.rightLeg.yaw = 0.31415927F;
         this.rightLeg.roll = 0.07853982F;
         this.leftLeg.pitch = -1.4137167F;
         this.leftLeg.yaw = -0.31415927F;
         this.leftLeg.roll = -0.07853982F;
      }

      this.rightArm.yaw = 0.0F;
      this.leftArm.yaw = 0.0F;
      boolean bl3 = livingEntity.getMainArm() == Arm.RIGHT;
      boolean bl4 = bl3 ? this.leftArmPose.method_30156() : this.rightArmPose.method_30156();
      if (bl3 != bl4) {
         this.method_30155(livingEntity);
         this.method_30154(livingEntity);
      } else {
         this.method_30154(livingEntity);
         this.method_30155(livingEntity);
      }

      this.method_29353(livingEntity, h);
      if (this.sneaking) {
         this.torso.pitch = 0.5F;
         var10000 = this.rightArm;
         var10000.pitch += 0.4F;
         var10000 = this.leftArm;
         var10000.pitch += 0.4F;
         this.rightLeg.pivotZ = 4.0F;
         this.leftLeg.pivotZ = 4.0F;
         this.rightLeg.pivotY = 12.2F;
         this.leftLeg.pivotY = 12.2F;
         this.head.pivotY = 4.2F;
         this.torso.pivotY = 3.2F;
         this.leftArm.pivotY = 5.2F;
         this.rightArm.pivotY = 5.2F;
      } else {
         this.torso.pitch = 0.0F;
         this.rightLeg.pivotZ = 0.1F;
         this.leftLeg.pivotZ = 0.1F;
         this.rightLeg.pivotY = 12.0F;
         this.leftLeg.pivotY = 12.0F;
         this.head.pivotY = 0.0F;
         this.torso.pivotY = 0.0F;
         this.leftArm.pivotY = 2.0F;
         this.rightArm.pivotY = 2.0F;
      }

      CrossbowPosing.method_29350(this.rightArm, this.leftArm, h);
      if (this.leaningPitch > 0.0F) {
         float l = f % 26.0F;
         Arm arm = this.getPreferredArm(livingEntity);
         float m = arm == Arm.RIGHT && this.handSwingProgress > 0.0F ? 0.0F : this.leaningPitch;
         float n = arm == Arm.LEFT && this.handSwingProgress > 0.0F ? 0.0F : this.leaningPitch;
         float p;
         if (l < 14.0F) {
            this.leftArm.pitch = this.lerpAngle(n, this.leftArm.pitch, 0.0F);
            this.rightArm.pitch = MathHelper.lerp(m, this.rightArm.pitch, 0.0F);
            this.leftArm.yaw = this.lerpAngle(n, this.leftArm.yaw, 3.1415927F);
            this.rightArm.yaw = MathHelper.lerp(m, this.rightArm.yaw, 3.1415927F);
            this.leftArm.roll = this.lerpAngle(n, this.leftArm.roll, 3.1415927F + 1.8707964F * this.method_2807(l) / this.method_2807(14.0F));
            this.rightArm.roll = MathHelper.lerp(m, this.rightArm.roll, 3.1415927F - 1.8707964F * this.method_2807(l) / this.method_2807(14.0F));
         } else if (l >= 14.0F && l < 22.0F) {
            p = (l - 14.0F) / 8.0F;
            this.leftArm.pitch = this.lerpAngle(n, this.leftArm.pitch, 1.5707964F * p);
            this.rightArm.pitch = MathHelper.lerp(m, this.rightArm.pitch, 1.5707964F * p);
            this.leftArm.yaw = this.lerpAngle(n, this.leftArm.yaw, 3.1415927F);
            this.rightArm.yaw = MathHelper.lerp(m, this.rightArm.yaw, 3.1415927F);
            this.leftArm.roll = this.lerpAngle(n, this.leftArm.roll, 5.012389F - 1.8707964F * p);
            this.rightArm.roll = MathHelper.lerp(m, this.rightArm.roll, 1.2707963F + 1.8707964F * p);
         } else if (l >= 22.0F && l < 26.0F) {
            p = (l - 22.0F) / 4.0F;
            this.leftArm.pitch = this.lerpAngle(n, this.leftArm.pitch, 1.5707964F - 1.5707964F * p);
            this.rightArm.pitch = MathHelper.lerp(m, this.rightArm.pitch, 1.5707964F - 1.5707964F * p);
            this.leftArm.yaw = this.lerpAngle(n, this.leftArm.yaw, 3.1415927F);
            this.rightArm.yaw = MathHelper.lerp(m, this.rightArm.yaw, 3.1415927F);
            this.leftArm.roll = this.lerpAngle(n, this.leftArm.roll, 3.1415927F);
            this.rightArm.roll = MathHelper.lerp(m, this.rightArm.roll, 3.1415927F);
         }

         p = 0.3F;
         float r = 0.33333334F;
         this.leftLeg.pitch = MathHelper.lerp(this.leaningPitch, this.leftLeg.pitch, 0.3F * MathHelper.cos(f * 0.33333334F + 3.1415927F));
         this.rightLeg.pitch = MathHelper.lerp(this.leaningPitch, this.rightLeg.pitch, 0.3F * MathHelper.cos(f * 0.33333334F));
      }

      this.helmet.copyPositionAndRotation(this.head);
   }

   private void method_30154(T livingEntity) {
      switch(this.rightArmPose) {
      case EMPTY:
         this.rightArm.yaw = 0.0F;
         break;
      case BLOCK:
         this.rightArm.pitch = this.rightArm.pitch * 0.5F - 0.9424779F;
         this.rightArm.yaw = -0.5235988F;
         break;
      case ITEM:
         this.rightArm.pitch = this.rightArm.pitch * 0.5F - 0.31415927F;
         this.rightArm.yaw = 0.0F;
         break;
      case THROW_SPEAR:
         this.rightArm.pitch = this.rightArm.pitch * 0.5F - 3.1415927F;
         this.rightArm.yaw = 0.0F;
         break;
      case BOW_AND_ARROW:
         this.rightArm.yaw = -0.1F + this.head.yaw;
         this.leftArm.yaw = 0.1F + this.head.yaw + 0.4F;
         this.rightArm.pitch = -1.5707964F + this.head.pitch;
         this.leftArm.pitch = -1.5707964F + this.head.pitch;
         break;
      case CROSSBOW_CHARGE:
         CrossbowPosing.charge(this.rightArm, this.leftArm, livingEntity, true);
         break;
      case CROSSBOW_HOLD:
         CrossbowPosing.hold(this.rightArm, this.leftArm, this.head, true);
      }

   }

   private void method_30155(T livingEntity) {
      switch(this.leftArmPose) {
      case EMPTY:
         this.leftArm.yaw = 0.0F;
         break;
      case BLOCK:
         this.leftArm.pitch = this.leftArm.pitch * 0.5F - 0.9424779F;
         this.leftArm.yaw = 0.5235988F;
         break;
      case ITEM:
         this.leftArm.pitch = this.leftArm.pitch * 0.5F - 0.31415927F;
         this.leftArm.yaw = 0.0F;
         break;
      case THROW_SPEAR:
         this.leftArm.pitch = this.leftArm.pitch * 0.5F - 3.1415927F;
         this.leftArm.yaw = 0.0F;
         break;
      case BOW_AND_ARROW:
         this.rightArm.yaw = -0.1F + this.head.yaw - 0.4F;
         this.leftArm.yaw = 0.1F + this.head.yaw;
         this.rightArm.pitch = -1.5707964F + this.head.pitch;
         this.leftArm.pitch = -1.5707964F + this.head.pitch;
         break;
      case CROSSBOW_CHARGE:
         CrossbowPosing.charge(this.rightArm, this.leftArm, livingEntity, false);
         break;
      case CROSSBOW_HOLD:
         CrossbowPosing.hold(this.rightArm, this.leftArm, this.head, false);
      }

   }

   protected void method_29353(T livingEntity, float f) {
      if (!(this.handSwingProgress <= 0.0F)) {
         Arm arm = this.getPreferredArm(livingEntity);
         ModelPart modelPart = this.getArm(arm);
         float g = this.handSwingProgress;
         this.torso.yaw = MathHelper.sin(MathHelper.sqrt(g) * 6.2831855F) * 0.2F;
         ModelPart var10000;
         if (arm == Arm.LEFT) {
            var10000 = this.torso;
            var10000.yaw *= -1.0F;
         }

         this.rightArm.pivotZ = MathHelper.sin(this.torso.yaw) * 5.0F;
         this.rightArm.pivotX = -MathHelper.cos(this.torso.yaw) * 5.0F;
         this.leftArm.pivotZ = -MathHelper.sin(this.torso.yaw) * 5.0F;
         this.leftArm.pivotX = MathHelper.cos(this.torso.yaw) * 5.0F;
         var10000 = this.rightArm;
         var10000.yaw += this.torso.yaw;
         var10000 = this.leftArm;
         var10000.yaw += this.torso.yaw;
         var10000 = this.leftArm;
         var10000.pitch += this.torso.yaw;
         g = 1.0F - this.handSwingProgress;
         g *= g;
         g *= g;
         g = 1.0F - g;
         float h = MathHelper.sin(g * 3.1415927F);
         float i = MathHelper.sin(this.handSwingProgress * 3.1415927F) * -(this.head.pitch - 0.7F) * 0.75F;
         modelPart.pitch = (float)((double)modelPart.pitch - ((double)h * 1.2D + (double)i));
         modelPart.yaw += this.torso.yaw * 2.0F;
         modelPart.roll += MathHelper.sin(this.handSwingProgress * 3.1415927F) * -0.4F;
      }
   }

   protected float lerpAngle(float f, float g, float h) {
      float i = (h - g) % 6.2831855F;
      if (i < -3.1415927F) {
         i += 6.2831855F;
      }

      if (i >= 3.1415927F) {
         i -= 6.2831855F;
      }

      return g + f * i;
   }

   private float method_2807(float f) {
      return -65.0F * f + f * f;
   }

   public void setAttributes(BipedEntityModel<T> bipedEntityModel) {
      super.copyStateTo(bipedEntityModel);
      bipedEntityModel.leftArmPose = this.leftArmPose;
      bipedEntityModel.rightArmPose = this.rightArmPose;
      bipedEntityModel.sneaking = this.sneaking;
      bipedEntityModel.head.copyPositionAndRotation(this.head);
      bipedEntityModel.helmet.copyPositionAndRotation(this.helmet);
      bipedEntityModel.torso.copyPositionAndRotation(this.torso);
      bipedEntityModel.rightArm.copyPositionAndRotation(this.rightArm);
      bipedEntityModel.leftArm.copyPositionAndRotation(this.leftArm);
      bipedEntityModel.rightLeg.copyPositionAndRotation(this.rightLeg);
      bipedEntityModel.leftLeg.copyPositionAndRotation(this.leftLeg);
   }

   public void setVisible(boolean visible) {
      this.head.visible = visible;
      this.helmet.visible = visible;
      this.torso.visible = visible;
      this.rightArm.visible = visible;
      this.leftArm.visible = visible;
      this.rightLeg.visible = visible;
      this.leftLeg.visible = visible;
   }

   public void setArmAngle(Arm arm, MatrixStack matrices) {
      this.getArm(arm).rotate(matrices);
   }

   protected ModelPart getArm(Arm arm) {
      return arm == Arm.LEFT ? this.leftArm : this.rightArm;
   }

   public ModelPart getHead() {
      return this.head;
   }

   protected Arm getPreferredArm(T entity) {
      Arm arm = entity.getMainArm();
      return entity.preferredHand == Hand.MAIN_HAND ? arm : arm.getOpposite();
   }

   @Environment(EnvType.CLIENT)
   public static enum ArmPose {
      EMPTY(false),
      ITEM(false),
      BLOCK(false),
      BOW_AND_ARROW(true),
      THROW_SPEAR(false),
      CROSSBOW_CHARGE(true),
      CROSSBOW_HOLD(true);

      private final boolean field_25722;

      private ArmPose(boolean bl) {
         this.field_25722 = bl;
      }

      public boolean method_30156() {
         return this.field_25722;
      }
   }
}
