package net.minecraft.client.render.entity.model;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import java.util.Arrays;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.ModelPart;
import net.minecraft.entity.mob.SlimeEntity;
import net.minecraft.util.math.MathHelper;

@Environment(EnvType.CLIENT)
public class MagmaCubeEntityModel<T extends SlimeEntity> extends CompositeEntityModel<T> {
   private final ModelPart[] field_3427 = new ModelPart[8];
   private final ModelPart innerCube;
   private final ImmutableList<ModelPart> parts;

   public MagmaCubeEntityModel() {
      for(int i = 0; i < this.field_3427.length; ++i) {
         int j = 0;
         int k = i;
         if (i == 2) {
            j = 24;
            k = 10;
         } else if (i == 3) {
            j = 24;
            k = 19;
         }

         this.field_3427[i] = new ModelPart(this, j, k);
         this.field_3427[i].addCuboid(-4.0F, (float)(16 + i), -4.0F, 8.0F, 1.0F, 8.0F);
      }

      this.innerCube = new ModelPart(this, 0, 16);
      this.innerCube.addCuboid(-2.0F, 18.0F, -2.0F, 4.0F, 4.0F, 4.0F);
      Builder<ModelPart> builder = ImmutableList.builder();
      builder.add((Object)this.innerCube);
      builder.addAll((Iterable)Arrays.asList(this.field_3427));
      this.parts = builder.build();
   }

   public void setAngles(T slimeEntity, float f, float g, float h, float i, float j) {
   }

   public void animateModel(T slimeEntity, float f, float g, float h) {
      float i = MathHelper.lerp(h, slimeEntity.lastStretch, slimeEntity.stretch);
      if (i < 0.0F) {
         i = 0.0F;
      }

      for(int j = 0; j < this.field_3427.length; ++j) {
         this.field_3427[j].pivotY = (float)(-(4 - j)) * i * 1.7F;
      }

   }

   public ImmutableList<ModelPart> getParts() {
      return this.parts;
   }
}
