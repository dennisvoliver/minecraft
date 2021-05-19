package net.minecraft.client.particle;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.DefaultParticleType;
import net.minecraft.util.math.MathHelper;

@Environment(EnvType.CLIENT)
public class EmotionParticle extends SpriteBillboardParticle {
   private EmotionParticle(ClientWorld world, double x, double y, double z) {
      super(world, x, y, z, 0.0D, 0.0D, 0.0D);
      this.velocityX *= 0.009999999776482582D;
      this.velocityY *= 0.009999999776482582D;
      this.velocityZ *= 0.009999999776482582D;
      this.velocityY += 0.1D;
      this.scale *= 1.5F;
      this.maxAge = 16;
      this.collidesWithWorld = false;
   }

   public ParticleTextureSheet getType() {
      return ParticleTextureSheet.PARTICLE_SHEET_OPAQUE;
   }

   public float getSize(float tickDelta) {
      return this.scale * MathHelper.clamp(((float)this.age + tickDelta) / (float)this.maxAge * 32.0F, 0.0F, 1.0F);
   }

   public void tick() {
      this.prevPosX = this.x;
      this.prevPosY = this.y;
      this.prevPosZ = this.z;
      if (this.age++ >= this.maxAge) {
         this.markDead();
      } else {
         this.move(this.velocityX, this.velocityY, this.velocityZ);
         if (this.y == this.prevPosY) {
            this.velocityX *= 1.1D;
            this.velocityZ *= 1.1D;
         }

         this.velocityX *= 0.8600000143051147D;
         this.velocityY *= 0.8600000143051147D;
         this.velocityZ *= 0.8600000143051147D;
         if (this.onGround) {
            this.velocityX *= 0.699999988079071D;
            this.velocityZ *= 0.699999988079071D;
         }

      }
   }

   @Environment(EnvType.CLIENT)
   public static class AngryVillagerFactory implements ParticleFactory<DefaultParticleType> {
      private final SpriteProvider spriteProvider;

      public AngryVillagerFactory(SpriteProvider spriteProvider) {
         this.spriteProvider = spriteProvider;
      }

      public Particle createParticle(DefaultParticleType defaultParticleType, ClientWorld clientWorld, double d, double e, double f, double g, double h, double i) {
         EmotionParticle emotionParticle = new EmotionParticle(clientWorld, d, e + 0.5D, f);
         emotionParticle.setSprite(this.spriteProvider);
         emotionParticle.setColor(1.0F, 1.0F, 1.0F);
         return emotionParticle;
      }
   }

   @Environment(EnvType.CLIENT)
   public static class HeartFactory implements ParticleFactory<DefaultParticleType> {
      private final SpriteProvider spriteProvider;

      public HeartFactory(SpriteProvider spriteProvider) {
         this.spriteProvider = spriteProvider;
      }

      public Particle createParticle(DefaultParticleType defaultParticleType, ClientWorld clientWorld, double d, double e, double f, double g, double h, double i) {
         EmotionParticle emotionParticle = new EmotionParticle(clientWorld, d, e, f);
         emotionParticle.setSprite(this.spriteProvider);
         return emotionParticle;
      }
   }
}
