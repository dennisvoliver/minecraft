package net.minecraft.client.render.entity.model;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import java.util.Arrays;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.ModelPart;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;

@Environment(EnvType.CLIENT)
public class BlazeEntityModel<T extends Entity> extends CompositeEntityModel<T> {
   private final ModelPart[] rods;
   private final ModelPart head = new ModelPart(this, 0, 0);
   private final ImmutableList<ModelPart> parts;

   public BlazeEntityModel() {
      this.head.addCuboid(-4.0F, -4.0F, -4.0F, 8.0F, 8.0F, 8.0F);
      this.rods = new ModelPart[12];

      for(int i = 0; i < this.rods.length; ++i) {
         this.rods[i] = new ModelPart(this, 0, 16);
         this.rods[i].addCuboid(0.0F, 0.0F, 0.0F, 2.0F, 8.0F, 2.0F);
      }

      Builder<ModelPart> builder = ImmutableList.builder();
      builder.add((Object)this.head);
      builder.addAll((Iterable)Arrays.asList(this.rods));
      this.parts = builder.build();
   }

   public Iterable<ModelPart> getParts() {
      return this.parts;
   }

   public void setAngles(T entity, float limbAngle, float limbDistance, float animationProgress, float headYaw, float headPitch) {
      float f = animationProgress * 3.1415927F * -0.1F;

      int k;
      for(k = 0; k < 4; ++k) {
         this.rods[k].pivotY = -2.0F + MathHelper.cos(((float)(k * 2) + animationProgress) * 0.25F);
         this.rods[k].pivotX = MathHelper.cos(f) * 9.0F;
         this.rods[k].pivotZ = MathHelper.sin(f) * 9.0F;
         ++f;
      }

      f = 0.7853982F + animationProgress * 3.1415927F * 0.03F;

      for(k = 4; k < 8; ++k) {
         this.rods[k].pivotY = 2.0F + MathHelper.cos(((float)(k * 2) + animationProgress) * 0.25F);
         this.rods[k].pivotX = MathHelper.cos(f) * 7.0F;
         this.rods[k].pivotZ = MathHelper.sin(f) * 7.0F;
         ++f;
      }

      f = 0.47123894F + animationProgress * 3.1415927F * -0.05F;

      for(k = 8; k < 12; ++k) {
         this.rods[k].pivotY = 11.0F + MathHelper.cos(((float)k * 1.5F + animationProgress) * 0.5F);
         this.rods[k].pivotX = MathHelper.cos(f) * 5.0F;
         this.rods[k].pivotZ = MathHelper.sin(f) * 5.0F;
         ++f;
      }

      this.head.yaw = headYaw * 0.017453292F;
      this.head.pitch = headPitch * 0.017453292F;
   }
}
