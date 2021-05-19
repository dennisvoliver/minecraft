package net.minecraft.client.particle;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.util.math.MathHelper;

@Environment(EnvType.CLIENT)
public class RedDustParticle extends SpriteBillboardParticle {
   private final SpriteProvider spriteProvider;

   private RedDustParticle(ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ, DustParticleEffect dustParticleEffect, SpriteProvider spriteProvider) {
      super(world, x, y, z, velocityX, velocityY, velocityZ);
      this.spriteProvider = spriteProvider;
      this.velocityX *= 0.10000000149011612D;
      this.velocityY *= 0.10000000149011612D;
      this.velocityZ *= 0.10000000149011612D;
      float f = (float)Math.random() * 0.4F + 0.6F;
      this.colorRed = ((float)(Math.random() * 0.20000000298023224D) + 0.8F) * dustParticleEffect.getRed() * f;
      this.colorGreen = ((float)(Math.random() * 0.20000000298023224D) + 0.8F) * dustParticleEffect.getGreen() * f;
      this.colorBlue = ((float)(Math.random() * 0.20000000298023224D) + 0.8F) * dustParticleEffect.getBlue() * f;
      this.scale *= 0.75F * dustParticleEffect.getScale();
      int i = (int)(8.0D / (Math.random() * 0.8D + 0.2D));
      this.maxAge = (int)Math.max((float)i * dustParticleEffect.getScale(), 1.0F);
      this.setSpriteForAge(spriteProvider);
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
         this.setSpriteForAge(this.spriteProvider);
         this.move(this.velocityX, this.velocityY, this.velocityZ);
         if (this.y == this.prevPosY) {
            this.velocityX *= 1.1D;
            this.velocityZ *= 1.1D;
         }

         this.velocityX *= 0.9599999785423279D;
         this.velocityY *= 0.9599999785423279D;
         this.velocityZ *= 0.9599999785423279D;
         if (this.onGround) {
            this.velocityX *= 0.699999988079071D;
            this.velocityZ *= 0.699999988079071D;
         }

      }
   }

   @Environment(EnvType.CLIENT)
   public static class Factory implements ParticleFactory<DustParticleEffect> {
      private final SpriteProvider spriteProvider;

      public Factory(SpriteProvider spriteProvider) {
         this.spriteProvider = spriteProvider;
      }

      public Particle createParticle(DustParticleEffect dustParticleEffect, ClientWorld clientWorld, double d, double e, double f, double g, double h, double i) {
         return new RedDustParticle(clientWorld, d, e, f, g, h, i, dustParticleEffect, this.spriteProvider);
      }
   }
}
