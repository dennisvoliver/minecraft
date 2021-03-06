package net.minecraft.client.render.entity.model;

import com.google.common.collect.ImmutableList;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.ModelPart;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;

@Environment(EnvType.CLIENT)
public class EvokerFangsEntityModel<T extends Entity> extends CompositeEntityModel<T> {
   private final ModelPart field_3374 = new ModelPart(this, 0, 0);
   private final ModelPart field_3376;
   private final ModelPart field_3375;

   public EvokerFangsEntityModel() {
      this.field_3374.setPivot(-5.0F, 22.0F, -5.0F);
      this.field_3374.addCuboid(0.0F, 0.0F, 0.0F, 10.0F, 12.0F, 10.0F);
      this.field_3376 = new ModelPart(this, 40, 0);
      this.field_3376.setPivot(1.5F, 22.0F, -4.0F);
      this.field_3376.addCuboid(0.0F, 0.0F, 0.0F, 4.0F, 14.0F, 8.0F);
      this.field_3375 = new ModelPart(this, 40, 0);
      this.field_3375.setPivot(-1.5F, 22.0F, 4.0F);
      this.field_3375.addCuboid(0.0F, 0.0F, 0.0F, 4.0F, 14.0F, 8.0F);
   }

   public void setAngles(T entity, float limbAngle, float limbDistance, float animationProgress, float headYaw, float headPitch) {
      float f = limbAngle * 2.0F;
      if (f > 1.0F) {
         f = 1.0F;
      }

      f = 1.0F - f * f * f;
      this.field_3376.roll = 3.1415927F - f * 0.35F * 3.1415927F;
      this.field_3375.roll = 3.1415927F + f * 0.35F * 3.1415927F;
      this.field_3375.yaw = 3.1415927F;
      float g = (limbAngle + MathHelper.sin(limbAngle * 2.7F)) * 0.6F * 12.0F;
      this.field_3376.pivotY = 24.0F - g;
      this.field_3375.pivotY = this.field_3376.pivotY;
      this.field_3374.pivotY = this.field_3376.pivotY;
   }

   public Iterable<ModelPart> getParts() {
      return ImmutableList.of(this.field_3374, this.field_3376, this.field_3375);
   }
}
