package net.minecraft.client.render.entity.model;

import com.google.common.collect.ImmutableList;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.ModelPart;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;

@Environment(EnvType.CLIENT)
public class SmallPufferfishEntityModel<T extends Entity> extends CompositeEntityModel<T> {
   private final ModelPart field_3505;
   private final ModelPart field_3507;
   private final ModelPart field_3506;
   private final ModelPart field_3504;
   private final ModelPart field_3503;
   private final ModelPart field_3508;

   public SmallPufferfishEntityModel() {
      this.textureWidth = 32;
      this.textureHeight = 32;
      int i = true;
      this.field_3505 = new ModelPart(this, 0, 27);
      this.field_3505.addCuboid(-1.5F, -2.0F, -1.5F, 3.0F, 2.0F, 3.0F);
      this.field_3505.setPivot(0.0F, 23.0F, 0.0F);
      this.field_3507 = new ModelPart(this, 24, 6);
      this.field_3507.addCuboid(-1.5F, 0.0F, -1.5F, 1.0F, 1.0F, 1.0F);
      this.field_3507.setPivot(0.0F, 20.0F, 0.0F);
      this.field_3506 = new ModelPart(this, 28, 6);
      this.field_3506.addCuboid(0.5F, 0.0F, -1.5F, 1.0F, 1.0F, 1.0F);
      this.field_3506.setPivot(0.0F, 20.0F, 0.0F);
      this.field_3508 = new ModelPart(this, -3, 0);
      this.field_3508.addCuboid(-1.5F, 0.0F, 0.0F, 3.0F, 0.0F, 3.0F);
      this.field_3508.setPivot(0.0F, 22.0F, 1.5F);
      this.field_3504 = new ModelPart(this, 25, 0);
      this.field_3504.addCuboid(-1.0F, 0.0F, 0.0F, 1.0F, 0.0F, 2.0F);
      this.field_3504.setPivot(-1.5F, 22.0F, -1.5F);
      this.field_3503 = new ModelPart(this, 25, 0);
      this.field_3503.addCuboid(0.0F, 0.0F, 0.0F, 1.0F, 0.0F, 2.0F);
      this.field_3503.setPivot(1.5F, 22.0F, -1.5F);
   }

   public Iterable<ModelPart> getParts() {
      return ImmutableList.of(this.field_3505, this.field_3507, this.field_3506, this.field_3508, this.field_3504, this.field_3503);
   }

   public void setAngles(T entity, float limbAngle, float limbDistance, float animationProgress, float headYaw, float headPitch) {
      this.field_3504.roll = -0.2F + 0.4F * MathHelper.sin(animationProgress * 0.2F);
      this.field_3503.roll = 0.2F - 0.4F * MathHelper.sin(animationProgress * 0.2F);
   }
}
