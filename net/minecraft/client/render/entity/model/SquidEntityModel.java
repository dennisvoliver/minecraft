package net.minecraft.client.render.entity.model;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import java.util.Arrays;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.ModelPart;
import net.minecraft.entity.Entity;

@Environment(EnvType.CLIENT)
public class SquidEntityModel<T extends Entity> extends CompositeEntityModel<T> {
   private final ModelPart head;
   private final ModelPart[] tentacles = new ModelPart[8];
   private final ImmutableList<ModelPart> parts;

   public SquidEntityModel() {
      int i = true;
      this.head = new ModelPart(this, 0, 0);
      this.head.addCuboid(-6.0F, -8.0F, -6.0F, 12.0F, 16.0F, 12.0F);
      ModelPart var10000 = this.head;
      var10000.pivotY += 8.0F;

      for(int j = 0; j < this.tentacles.length; ++j) {
         this.tentacles[j] = new ModelPart(this, 48, 0);
         double d = (double)j * 3.141592653589793D * 2.0D / (double)this.tentacles.length;
         float f = (float)Math.cos(d) * 5.0F;
         float g = (float)Math.sin(d) * 5.0F;
         this.tentacles[j].addCuboid(-1.0F, 0.0F, -1.0F, 2.0F, 18.0F, 2.0F);
         this.tentacles[j].pivotX = f;
         this.tentacles[j].pivotZ = g;
         this.tentacles[j].pivotY = 15.0F;
         d = (double)j * 3.141592653589793D * -2.0D / (double)this.tentacles.length + 1.5707963267948966D;
         this.tentacles[j].yaw = (float)d;
      }

      Builder<ModelPart> builder = ImmutableList.builder();
      builder.add((Object)this.head);
      builder.addAll((Iterable)Arrays.asList(this.tentacles));
      this.parts = builder.build();
   }

   public void setAngles(T entity, float limbAngle, float limbDistance, float animationProgress, float headYaw, float headPitch) {
      ModelPart[] var7 = this.tentacles;
      int var8 = var7.length;

      for(int var9 = 0; var9 < var8; ++var9) {
         ModelPart modelPart = var7[var9];
         modelPart.pitch = animationProgress;
      }

   }

   public Iterable<ModelPart> getParts() {
      return this.parts;
   }
}
