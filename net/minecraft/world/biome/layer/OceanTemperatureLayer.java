package net.minecraft.world.biome.layer;

import net.minecraft.util.math.noise.PerlinNoiseSampler;
import net.minecraft.world.biome.layer.type.InitLayer;
import net.minecraft.world.biome.layer.util.LayerRandomnessSource;

public enum OceanTemperatureLayer implements InitLayer {
   INSTANCE;

   public int sample(LayerRandomnessSource context, int x, int y) {
      PerlinNoiseSampler perlinNoiseSampler = context.getNoiseSampler();
      double d = perlinNoiseSampler.sample((double)x / 8.0D, (double)y / 8.0D, 0.0D, 0.0D, 0.0D);
      if (d > 0.4D) {
         return 44;
      } else if (d > 0.2D) {
         return 45;
      } else if (d < -0.4D) {
         return 10;
      } else {
         return d < -0.2D ? 46 : 0;
      }
   }
}
