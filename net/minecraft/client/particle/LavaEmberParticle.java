package net.minecraft.client.particle;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.DefaultParticleType;
import net.minecraft.particle.ParticleTypes;

@Environment(EnvType.CLIENT)
public class LavaEmberParticle extends SpriteBillboardParticle {
   private LavaEmberParticle(ClientWorld world, double x, double y, double z) {
      super(world, x, y, z, 0.0D, 0.0D, 0.0D);
      this.velocityX *= 0.800000011920929D;
      this.velocityY *= 0.800000011920929D;
      this.velocityZ *= 0.800000011920929D;
      this.velocityY = (double)(this.random.nextFloat() * 0.4F + 0.05F);
      this.scale *= this.random.nextFloat() * 2.0F + 0.2F;
      this.maxAge = (int)(16.0D / (Math.random() * 0.8D + 0.2D));
   }

   public ParticleTextureSheet getType() {
      return ParticleTextureSheet.PARTICLE_SHEET_OPAQUE;
   }

   public int getColorMultiplier(float tint) {
      int i = super.getColorMultiplier(tint);
      int j = true;
      int k = i >> 16 & 255;
      return 240 | k << 16;
   }

   public float getSize(float tickDelta) {
      float f = ((float)this.age + tickDelta) / (float)this.maxAge;
      return this.scale * (1.0F - f * f);
   }

   public void tick() {
      this.prevPosX = this.x;
      this.prevPosY = this.y;
      this.prevPosZ = this.z;
      float f = (float)this.age / (float)this.maxAge;
      if (this.random.nextFloat() > f) {
         this.world.addParticle(ParticleTypes.SMOKE, this.x, this.y, this.z, this.velocityX, this.velocityY, this.velocityZ);
      }

      if (this.age++ >= this.maxAge) {
         this.markDead();
      } else {
         this.velocityY -= 0.03D;
         this.move(this.velocityX, this.velocityY, this.velocityZ);
         this.velocityX *= 0.9990000128746033D;
         this.velocityY *= 0.9990000128746033D;
         this.velocityZ *= 0.9990000128746033D;
         if (this.onGround) {
            this.velocityX *= 0.699999988079071D;
            this.velocityZ *= 0.699999988079071D;
         }

      }
   }

   @Environment(EnvType.CLIENT)
   public static class Factory implements ParticleFactory<DefaultParticleType> {
      private final SpriteProvider spriteProvider;

      public Factory(SpriteProvider spriteProvider) {
         this.spriteProvider = spriteProvider;
      }

      public Particle createParticle(DefaultParticleType defaultParticleType, ClientWorld clientWorld, double d, double e, double f, double g, double h, double i) {
         LavaEmberParticle lavaEmberParticle = new LavaEmberParticle(clientWorld, d, e, f);
         lavaEmberParticle.setSprite(this.spriteProvider);
         return lavaEmberParticle;
      }
   }
}
