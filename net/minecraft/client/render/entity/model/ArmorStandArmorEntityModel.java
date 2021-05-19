package net.minecraft.client.render.entity.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.decoration.ArmorStandEntity;

@Environment(EnvType.CLIENT)
public class ArmorStandArmorEntityModel extends BipedEntityModel<ArmorStandEntity> {
   public ArmorStandArmorEntityModel(float f) {
      this(f, 64, 32);
   }

   protected ArmorStandArmorEntityModel(float scale, int textureWidth, int textureHeight) {
      super(scale, 0.0F, textureWidth, textureHeight);
   }

   public void setAngles(ArmorStandEntity armorStandEntity, float f, float g, float h, float i, float j) {
      this.head.pitch = 0.017453292F * armorStandEntity.getHeadRotation().getPitch();
      this.head.yaw = 0.017453292F * armorStandEntity.getHeadRotation().getYaw();
      this.head.roll = 0.017453292F * armorStandEntity.getHeadRotation().getRoll();
      this.head.setPivot(0.0F, 1.0F, 0.0F);
      this.torso.pitch = 0.017453292F * armorStandEntity.getBodyRotation().getPitch();
      this.torso.yaw = 0.017453292F * armorStandEntity.getBodyRotation().getYaw();
      this.torso.roll = 0.017453292F * armorStandEntity.getBodyRotation().getRoll();
      this.leftArm.pitch = 0.017453292F * armorStandEntity.getLeftArmRotation().getPitch();
      this.leftArm.yaw = 0.017453292F * armorStandEntity.getLeftArmRotation().getYaw();
      this.leftArm.roll = 0.017453292F * armorStandEntity.getLeftArmRotation().getRoll();
      this.rightArm.pitch = 0.017453292F * armorStandEntity.getRightArmRotation().getPitch();
      this.rightArm.yaw = 0.017453292F * armorStandEntity.getRightArmRotation().getYaw();
      this.rightArm.roll = 0.017453292F * armorStandEntity.getRightArmRotation().getRoll();
      this.leftLeg.pitch = 0.017453292F * armorStandEntity.getLeftLegRotation().getPitch();
      this.leftLeg.yaw = 0.017453292F * armorStandEntity.getLeftLegRotation().getYaw();
      this.leftLeg.roll = 0.017453292F * armorStandEntity.getLeftLegRotation().getRoll();
      this.leftLeg.setPivot(1.9F, 11.0F, 0.0F);
      this.rightLeg.pitch = 0.017453292F * armorStandEntity.getRightLegRotation().getPitch();
      this.rightLeg.yaw = 0.017453292F * armorStandEntity.getRightLegRotation().getYaw();
      this.rightLeg.roll = 0.017453292F * armorStandEntity.getRightLegRotation().getRoll();
      this.rightLeg.setPivot(-1.9F, 11.0F, 0.0F);
      this.helmet.copyPositionAndRotation(this.head);
   }
}
