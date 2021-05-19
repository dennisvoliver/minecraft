package net.minecraft.client.particle;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.DefaultParticleType;

@Environment(EnvType.CLIENT)
public class FishingParticle extends SpriteBillboardParticle {
   private final SpriteProvider spriteProvider;

   private FishingParticle(ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ, SpriteProvider spriteProvider) {
      super(world, x, y, z, 0.0D, 0.0D, 0.0D);
      this.spriteProvider = spriteProvider;
      this.velocityX *= 0.30000001192092896D;
      this.velocityY = Math.random() * 0.20000000298023224D + 0.10000000149011612D;
      this.velocityZ *= 0.30000001192092896D;
      this.setBoundingBoxSpacing(0.01F, 0.01F);
      this.maxAge = (int)(8.0D / (Math.random() * 0.8D + 0.2D));
      this.setSpriteForAge(spriteProvider);
      this.gravityStrength = 0.0F;
      this.velocityX = velocityX;
      this.velocityY = velocityY;
      this.velocityZ = velocityZ;
   }

   public ParticleTextureSheet getType() {
      return ParticleTextureSheet.PARTICLE_SHEET_OPAQUE;
   }

   public void tick() {
      this.prevPosX = this.x;
      this.prevPosY = this.y;
      this.prevPosZ = this.z;
      int i = 60 - this.maxAge;
      if (this.maxAge-- <= 0) {
         this.markDead();
      } else {
         this.velocityY -= (double)this.gravityStrength;
         this.move(this.velocityX, this.velocityY, this.velocityZ);
         this.velocityX *= 0.9800000190734863D;
         this.velocityY *= 0.9800000190734863D;
         this.velocityZ *= 0.9800000190734863D;
         float f = (float)i * 0.001F;
         this.setBoundingBoxSpacing(f, f);
         this.setSprite(this.spriteProvider.getSprite(i % 4, 4));
      }
   }

   @Environment(EnvType.CLIENT)
   public static class Factory implements ParticleFactory<DefaultParticleType> {
      private final SpriteProvider spriteProvider;

      public Factory(SpriteProvider spriteProvider) {
         this.spriteProvider = spriteProvider;
      }

      public Particle createParticle(DefaultParticleType defaultParticleType, ClientWorld clientWorld, double d, double e, double f, double g, double h, double i) {
         return new FishingParticle(clientWorld, d, e, f, g, h, i, this.spriteProvider);
      }
   }
}
