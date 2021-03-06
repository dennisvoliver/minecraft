package net.minecraft.client.render.entity.model;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import java.util.Random;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.ModelPart;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;

@Environment(EnvType.CLIENT)
public class GhastEntityModel<T extends Entity> extends CompositeEntityModel<T> {
   private final ModelPart[] tentacles = new ModelPart[9];
   private final ImmutableList<ModelPart> parts;

   public GhastEntityModel() {
      Builder<ModelPart> builder = ImmutableList.builder();
      ModelPart modelPart = new ModelPart(this, 0, 0);
      modelPart.addCuboid(-8.0F, -8.0F, -8.0F, 16.0F, 16.0F, 16.0F);
      modelPart.pivotY = 17.6F;
      builder.add((Object)modelPart);
      Random random = new Random(1660L);

      for(int i = 0; i < this.tentacles.length; ++i) {
         this.tentacles[i] = new ModelPart(this, 0, 0);
         float f = (((float)(i % 3) - (float)(i / 3 % 2) * 0.5F + 0.25F) / 2.0F * 2.0F - 1.0F) * 5.0F;
         float g = ((float)(i / 3) / 2.0F * 2.0F - 1.0F) * 5.0F;
         int j = random.nextInt(7) + 8;
         this.tentacles[i].addCuboid(-1.0F, 0.0F, -1.0F, 2.0F, (float)j, 2.0F);
         this.tentacles[i].pivotX = f;
         this.tentacles[i].pivotZ = g;
         this.tentacles[i].pivotY = 24.6F;
         builder.add((Object)this.tentacles[i]);
      }

      this.parts = builder.build();
   }

   public void setAngles(T entity, float limbAngle, float limbDistance, float animationProgress, float headYaw, float headPitch) {
      for(int i = 0; i < this.tentacles.length; ++i) {
         this.tentacles[i].pitch = 0.2F * MathHelper.sin(animationProgress * 0.3F + (float)i) + 0.4F;
      }

   }

   public Iterable<ModelPart> getParts() {
      return this.parts;
   }
}
