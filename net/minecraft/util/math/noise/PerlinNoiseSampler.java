package net.minecraft.util.math.noise;

import java.util.Random;
import net.minecraft.util.math.MathHelper;

public final class PerlinNoiseSampler {
   private final byte[] permutations;
   public final double originX;
   public final double originY;
   public final double originZ;

   public PerlinNoiseSampler(Random random) {
      this.originX = random.nextDouble() * 256.0D;
      this.originY = random.nextDouble() * 256.0D;
      this.originZ = random.nextDouble() * 256.0D;
      this.permutations = new byte[256];

      int j;
      for(j = 0; j < 256; ++j) {
         this.permutations[j] = (byte)j;
      }

      for(j = 0; j < 256; ++j) {
         int k = random.nextInt(256 - j);
         byte b = this.permutations[j];
         this.permutations[j] = this.permutations[j + k];
         this.permutations[j + k] = b;
      }

   }

   public double sample(double x, double y, double z, double d, double e) {
      double f = x + this.originX;
      double g = y + this.originY;
      double h = z + this.originZ;
      int i = MathHelper.floor(f);
      int j = MathHelper.floor(g);
      int k = MathHelper.floor(h);
      double l = f - (double)i;
      double m = g - (double)j;
      double n = h - (double)k;
      double o = MathHelper.perlinFade(l);
      double p = MathHelper.perlinFade(m);
      double q = MathHelper.perlinFade(n);
      double t;
      if (d != 0.0D) {
         double r = Math.min(e, m);
         t = (double)MathHelper.floor(r / d) * d;
      } else {
         t = 0.0D;
      }

      return this.sample(i, j, k, l, m - t, n, o, p, q);
   }

   private static double grad(int hash, double x, double y, double z) {
      int i = hash & 15;
      return SimplexNoiseSampler.dot(SimplexNoiseSampler.gradients[i], x, y, z);
   }

   private int getGradient(int hash) {
      return this.permutations[hash & 255] & 255;
   }

   public double sample(int sectionX, int sectionY, int sectionZ, double localX, double localY, double localZ, double fadeLocalX, double fadeLocalY, double fadeLocalZ) {
      int i = this.getGradient(sectionX) + sectionY;
      int j = this.getGradient(i) + sectionZ;
      int k = this.getGradient(i + 1) + sectionZ;
      int l = this.getGradient(sectionX + 1) + sectionY;
      int m = this.getGradient(l) + sectionZ;
      int n = this.getGradient(l + 1) + sectionZ;
      double d = grad(this.getGradient(j), localX, localY, localZ);
      double e = grad(this.getGradient(m), localX - 1.0D, localY, localZ);
      double f = grad(this.getGradient(k), localX, localY - 1.0D, localZ);
      double g = grad(this.getGradient(n), localX - 1.0D, localY - 1.0D, localZ);
      double h = grad(this.getGradient(j + 1), localX, localY, localZ - 1.0D);
      double o = grad(this.getGradient(m + 1), localX - 1.0D, localY, localZ - 1.0D);
      double p = grad(this.getGradient(k + 1), localX, localY - 1.0D, localZ - 1.0D);
      double q = grad(this.getGradient(n + 1), localX - 1.0D, localY - 1.0D, localZ - 1.0D);
      return MathHelper.lerp3(fadeLocalX, fadeLocalY, fadeLocalZ, d, e, f, g, h, o, p, q);
   }
}
