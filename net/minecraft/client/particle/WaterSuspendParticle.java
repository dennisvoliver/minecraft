package net.minecraft.client.particle;

import java.util.Random;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.DefaultParticleType;

@Environment(EnvType.CLIENT)
public class WaterSuspendParticle extends SpriteBillboardParticle {
   private WaterSuspendParticle(ClientWorld world, double x, double y, double z) {
      super(world, x, y - 0.125D, z);
      this.colorRed = 0.4F;
      this.colorGreen = 0.4F;
      this.colorBlue = 0.7F;
      this.setBoundingBoxSpacing(0.01F, 0.01F);
      this.scale *= this.random.nextFloat() * 0.6F + 0.2F;
      this.maxAge = (int)(16.0D / (Math.random() * 0.8D + 0.2D));
      this.collidesWithWorld = false;
   }

   private WaterSuspendParticle(ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
      super(world, x, y - 0.125D, z, velocityX, velocityY, velocityZ);
      this.setBoundingBoxSpacing(0.01F, 0.01F);
      this.scale *= this.random.nextFloat() * 0.6F + 0.6F;
      this.maxAge = (int)(16.0D / (Math.random() * 0.8D + 0.2D));
      this.collidesWithWorld = false;
   }

   public ParticleTextureSheet getType() {
      return ParticleTextureSheet.PARTICLE_SHEET_OPAQUE;
   }

   public void tick() {
      this.prevPosX = this.x;
      this.prevPosY = this.y;
      this.prevPosZ = this.z;
      if (this.maxAge-- <= 0) {
         this.markDead();
      } else {
         this.move(this.velocityX, this.velocityY, this.velocityZ);
      }
   }

   @Environment(EnvType.CLIENT)
   public static class WarpedSporeFactory implements ParticleFactory<DefaultParticleType> {
      private final SpriteProvider spriteProvider;

      public WarpedSporeFactory(SpriteProvider spriteProvider) {
         this.spriteProvider = spriteProvider;
      }

      public Particle createParticle(DefaultParticleType defaultParticleType, ClientWorld clientWorld, double d, double e, double f, double g, double h, double i) {
         double j = (double)clientWorld.random.nextFloat() * -1.9D * (double)clientWorld.random.nextFloat() * 0.1D;
         WaterSuspendParticle waterSuspendParticle = new WaterSuspendParticle(clientWorld, d, e, f, 0.0D, j, 0.0D);
         waterSuspendParticle.setSprite(this.spriteProvider);
         waterSuspendParticle.setColor(0.1F, 0.1F, 0.3F);
         waterSuspendParticle.setBoundingBoxSpacing(0.001F, 0.001F);
         return waterSuspendParticle;
      }
   }

   @Environment(EnvType.CLIENT)
   public static class CrimsonSporeFactory implements ParticleFactory<DefaultParticleType> {
      private final SpriteProvider spriteProvider;

      public CrimsonSporeFactory(SpriteProvider spriteProvider) {
         this.spriteProvider = spriteProvider;
      }

      public Particle createParticle(DefaultParticleType defaultParticleType, ClientWorld clientWorld, double d, double e, double f, double g, double h, double i) {
         Random random = clientWorld.random;
         double j = random.nextGaussian() * 9.999999974752427E-7D;
         double k = random.nextGaussian() * 9.999999747378752E-5D;
         double l = random.nextGaussian() * 9.999999974752427E-7D;
         WaterSuspendParticle waterSuspendParticle = new WaterSuspendParticle(clientWorld, d, e, f, j, k, l);
         waterSuspendParticle.setSprite(this.spriteProvider);
         waterSuspendParticle.setColor(0.9F, 0.4F, 0.5F);
         return waterSuspendParticle;
      }
   }

   @Environment(EnvType.CLIENT)
   public static class UnderwaterFactory implements ParticleFactory<DefaultParticleType> {
      private final SpriteProvider spriteProvider;

      public UnderwaterFactory(SpriteProvider spriteProvider) {
         this.spriteProvider = spriteProvider;
      }

      public Particle createParticle(DefaultParticleType defaultParticleType, ClientWorld clientWorld, double d, double e, double f, double g, double h, double i) {
         WaterSuspendParticle waterSuspendParticle = new WaterSuspendParticle(clientWorld, d, e, f);
         waterSuspendParticle.setSprite(this.spriteProvider);
         return waterSuspendParticle;
      }
   }
}
