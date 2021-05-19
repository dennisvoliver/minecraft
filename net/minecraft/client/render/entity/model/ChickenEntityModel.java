package net.minecraft.client.render.entity.model;

import com.google.common.collect.ImmutableList;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.ModelPart;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;

@Environment(EnvType.CLIENT)
public class ChickenEntityModel<T extends Entity> extends AnimalModel<T> {
   private final ModelPart head;
   private final ModelPart torso;
   private final ModelPart rightLeg;
   private final ModelPart leftLeg;
   private final ModelPart rightWing;
   private final ModelPart leftWing;
   private final ModelPart beak;
   private final ModelPart wattle;

   public ChickenEntityModel() {
      int i = true;
      this.head = new ModelPart(this, 0, 0);
      this.head.addCuboid(-2.0F, -6.0F, -2.0F, 4.0F, 6.0F, 3.0F, 0.0F);
      this.head.setPivot(0.0F, 15.0F, -4.0F);
      this.beak = new ModelPart(this, 14, 0);
      this.beak.addCuboid(-2.0F, -4.0F, -4.0F, 4.0F, 2.0F, 2.0F, 0.0F);
      this.beak.setPivot(0.0F, 15.0F, -4.0F);
      this.wattle = new ModelPart(this, 14, 4);
      this.wattle.addCuboid(-1.0F, -2.0F, -3.0F, 2.0F, 2.0F, 2.0F, 0.0F);
      this.wattle.setPivot(0.0F, 15.0F, -4.0F);
      this.torso = new ModelPart(this, 0, 9);
      this.torso.addCuboid(-3.0F, -4.0F, -3.0F, 6.0F, 8.0F, 6.0F, 0.0F);
      this.torso.setPivot(0.0F, 16.0F, 0.0F);
      this.rightLeg = new ModelPart(this, 26, 0);
      this.rightLeg.addCuboid(-1.0F, 0.0F, -3.0F, 3.0F, 5.0F, 3.0F);
      this.rightLeg.setPivot(-2.0F, 19.0F, 1.0F);
      this.leftLeg = new ModelPart(this, 26, 0);
      this.leftLeg.addCuboid(-1.0F, 0.0F, -3.0F, 3.0F, 5.0F, 3.0F);
      this.leftLeg.setPivot(1.0F, 19.0F, 1.0F);
      this.rightWing = new ModelPart(this, 24, 13);
      this.rightWing.addCuboid(0.0F, 0.0F, -3.0F, 1.0F, 4.0F, 6.0F);
      this.rightWing.setPivot(-4.0F, 13.0F, 0.0F);
      this.leftWing = new ModelPart(this, 24, 13);
      this.leftWing.addCuboid(-1.0F, 0.0F, -3.0F, 1.0F, 4.0F, 6.0F);
      this.leftWing.setPivot(4.0F, 13.0F, 0.0F);
   }

   protected Iterable<ModelPart> getHeadParts() {
      return ImmutableList.of(this.head, this.beak, this.wattle);
   }

   protected Iterable<ModelPart> getBodyParts() {
      return ImmutableList.of(this.torso, this.rightLeg, this.leftLeg, this.rightWing, this.leftWing);
   }

   public void setAngles(T entity, float limbAngle, float limbDistance, float animationProgress, float headYaw, float headPitch) {
      this.head.pitch = headPitch * 0.017453292F;
      this.head.yaw = headYaw * 0.017453292F;
      this.beak.pitch = this.head.pitch;
      this.beak.yaw = this.head.yaw;
      this.wattle.pitch = this.head.pitch;
      this.wattle.yaw = this.head.yaw;
      this.torso.pitch = 1.5707964F;
      this.rightLeg.pitch = MathHelper.cos(limbAngle * 0.6662F) * 1.4F * limbDistance;
      this.leftLeg.pitch = MathHelper.cos(limbAngle * 0.6662F + 3.1415927F) * 1.4F * limbDistance;
      this.rightWing.roll = animationProgress;
      this.leftWing.roll = -animationProgress;
   }
}
