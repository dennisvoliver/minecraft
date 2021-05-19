package net.minecraft.client.particle;

import java.util.Random;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.DefaultParticleType;

@Environment(EnvType.CLIENT)
public class WhiteAshParticle extends AscendingParticle {
   protected WhiteAshParticle(ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ, float scaleMultiplier, SpriteProvider spriteProvider) {
      super(world, x, y, z, 0.1F, -0.1F, 0.1F, velocityX, velocityY, velocityZ, scaleMultiplier, spriteProvider, 0.0F, 20, -5.0E-4D, false);
      this.colorRed = 0.7294118F;
      this.colorGreen = 0.69411767F;
      this.colorBlue = 0.7607843F;
   }

   @Environment(EnvType.CLIENT)
   public static class Factory implements ParticleFactory<DefaultParticleType> {
      private final SpriteProvider spriteProvider;

      public Factory(SpriteProvider spriteProvider) {
         this.spriteProvider = spriteProvider;
      }

      public Particle createParticle(DefaultParticleType defaultParticleType, ClientWorld clientWorld, double d, double e, double f, double g, double h, double i) {
         Random random = clientWorld.random;
         double j = (double)random.nextFloat() * -1.9D * (double)random.nextFloat() * 0.1D;
         double k = (double)random.nextFloat() * -0.5D * (double)random.nextFloat() * 0.1D * 5.0D;
         double l = (double)random.nextFloat() * -1.9D * (double)random.nextFloat() * 0.1D;
         return new WhiteAshParticle(clientWorld, d, e, f, j, k, l, 1.0F, this.spriteProvider);
      }
   }
}
