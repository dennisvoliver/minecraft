package net.minecraft.client.render.entity.model;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import java.util.Arrays;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.ModelPart;
import net.minecraft.entity.boss.WitherEntity;
import net.minecraft.util.math.MathHelper;

@Environment(EnvType.CLIENT)
public class WitherEntityModel<T extends WitherEntity> extends CompositeEntityModel<T> {
   private final ModelPart[] bodySegments;
   private final ModelPart[] heads;
   private final ImmutableList<ModelPart> parts;

   public WitherEntityModel(float scale) {
      this.textureWidth = 64;
      this.textureHeight = 64;
      this.bodySegments = new ModelPart[3];
      this.bodySegments[0] = new ModelPart(this, 0, 16);
      this.bodySegments[0].addCuboid(-10.0F, 3.9F, -0.5F, 20.0F, 3.0F, 3.0F, scale);
      this.bodySegments[1] = (new ModelPart(this)).setTextureSize(this.textureWidth, this.textureHeight);
      this.bodySegments[1].setPivot(-2.0F, 6.9F, -0.5F);
      this.bodySegments[1].setTextureOffset(0, 22).addCuboid(0.0F, 0.0F, 0.0F, 3.0F, 10.0F, 3.0F, scale);
      this.bodySegments[1].setTextureOffset(24, 22).addCuboid(-4.0F, 1.5F, 0.5F, 11.0F, 2.0F, 2.0F, scale);
      this.bodySegments[1].setTextureOffset(24, 22).addCuboid(-4.0F, 4.0F, 0.5F, 11.0F, 2.0F, 2.0F, scale);
      this.bodySegments[1].setTextureOffset(24, 22).addCuboid(-4.0F, 6.5F, 0.5F, 11.0F, 2.0F, 2.0F, scale);
      this.bodySegments[2] = new ModelPart(this, 12, 22);
      this.bodySegments[2].addCuboid(0.0F, 0.0F, 0.0F, 3.0F, 6.0F, 3.0F, scale);
      this.heads = new ModelPart[3];
      this.heads[0] = new ModelPart(this, 0, 0);
      this.heads[0].addCuboid(-4.0F, -4.0F, -4.0F, 8.0F, 8.0F, 8.0F, scale);
      this.heads[1] = new ModelPart(this, 32, 0);
      this.heads[1].addCuboid(-4.0F, -4.0F, -4.0F, 6.0F, 6.0F, 6.0F, scale);
      this.heads[1].pivotX = -8.0F;
      this.heads[1].pivotY = 4.0F;
      this.heads[2] = new ModelPart(this, 32, 0);
      this.heads[2].addCuboid(-4.0F, -4.0F, -4.0F, 6.0F, 6.0F, 6.0F, scale);
      this.heads[2].pivotX = 10.0F;
      this.heads[2].pivotY = 4.0F;
      Builder<ModelPart> builder = ImmutableList.builder();
      builder.addAll((Iterable)Arrays.asList(this.heads));
      builder.addAll((Iterable)Arrays.asList(this.bodySegments));
      this.parts = builder.build();
   }

   public ImmutableList<ModelPart> getParts() {
      return this.parts;
   }

   public void setAngles(T witherEntity, float f, float g, float h, float i, float j) {
      float k = MathHelper.cos(h * 0.1F);
      this.bodySegments[1].pitch = (0.065F + 0.05F * k) * 3.1415927F;
      this.bodySegments[2].setPivot(-2.0F, 6.9F + MathHelper.cos(this.bodySegments[1].pitch) * 10.0F, -0.5F + MathHelper.sin(this.bodySegments[1].pitch) * 10.0F);
      this.bodySegments[2].pitch = (0.265F + 0.1F * k) * 3.1415927F;
      this.heads[0].yaw = i * 0.017453292F;
      this.heads[0].pitch = j * 0.017453292F;
   }

   public void animateModel(T witherEntity, float f, float g, float h) {
      for(int i = 1; i < 3; ++i) {
         this.heads[i].yaw = (witherEntity.getHeadYaw(i - 1) - witherEntity.bodyYaw) * 0.017453292F;
         this.heads[i].pitch = witherEntity.getHeadPitch(i - 1) * 0.017453292F;
      }

   }
}
