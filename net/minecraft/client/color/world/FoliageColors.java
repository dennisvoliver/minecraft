package net.minecraft.client.color.world;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class FoliageColors {
   private static int[] colorMap = new int[65536];

   public static void setColorMap(int[] pixels) {
      colorMap = pixels;
   }

   public static int getColor(double temperature, double humidity) {
      humidity *= temperature;
      int i = (int)((1.0D - temperature) * 255.0D);
      int j = (int)((1.0D - humidity) * 255.0D);
      return colorMap[j << 8 | i];
   }

   public static int getSpruceColor() {
      return 6396257;
   }

   public static int getBirchColor() {
      return 8431445;
   }

   public static int getDefaultColor() {
      return 4764952;
   }
}
