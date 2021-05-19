package net.minecraft.client.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class ModelUtil {
   public static float interpolateAngle(float angle1, float angle2, float progress) {
      float f;
      for(f = angle2 - angle1; f < -3.1415927F; f += 6.2831855F) {
      }

      while(f >= 3.1415927F) {
         f -= 6.2831855F;
      }

      return angle1 + progress * f;
   }
}
