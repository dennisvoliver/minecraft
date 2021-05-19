package net.minecraft.client.particle;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.DefaultParticleType;
import net.minecraft.util.math.MathHelper;

@Environment(EnvType.CLIENT)
public class DragonBreathParticle extends SpriteBillboardParticle {
   private boolean reachedGround;
   private final SpriteProvider spriteProvider;

   private DragonBreathParticle(ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ, SpriteProvider spriteProvider) {
      super(world, x, y, z);
      this.velocityX = velocityX;
      this.velocityY = velocityY;
      this.velocityZ = velocityZ;
      this.colorRed = MathHelper.nextFloat(this.random, 0.7176471F, 0.8745098F);
      this.colorGreen = MathHelper.nextFloat(this.random, 0.0F, 0.0F);
      this.colorBlue = MathHelper.nextFloat(this.random, 0.8235294F, 0.9764706F);
      this.scale *= 0.75F;
      this.maxAge = (int)(20.0D / ((double)this.random.nextFloat() * 0.8D + 0.2D));
      this.reachedGround = false;
      this.collidesWithWorld = false;
      this.spriteProvider = spriteProvider;
      this.setSpriteForAge(spriteProvider);
   }

   public void tick() {
      this.prevPosX = this.x;
      this.prevPosY = this.y;
      this.prevPosZ = this.z;
      if (this.age++ >= this.maxAge) {
         this.markDead();
      } else {
         this.setSpriteForAge(this.spriteProvider);
         if (this.onGround) {
            this.velocityY = 0.0D;
            this.reachedGround = true;
         }

         if (this.reachedGround) {
            this.velocityY += 0.002D;
         }

         this.move(this.velocityX, this.velocityY, this.velocityZ);
         if (this.y == this.prevPosY) {
            this.velocityX *= 1.1D;
            this.velocityZ *= 1.1D;
         }

         this.velocityX *= 0.9599999785423279D;
         this.velocityZ *= 0.9599999785423279D;
         if (this.reachedGround) {
            this.velocityY *= 0.9599999785423279D;
         }

      }
   }

   public ParticleTextureSheet getType() {
      return ParticleTextureSheet.PARTICLE_SHEET_OPAQUE;
   }

   public float getSize(float tickDelta) {
      return this.scale * MathHelper.clamp(((float)this.age + tickDelta) / (float)this.maxAge * 32.0F, 0.0F, 1.0F);
   }

   @Environment(EnvType.CLIENT)
   public static class Factory implements ParticleFactory<DefaultParticleType> {
      private final SpriteProvider spriteProvider;

      public Factory(SpriteProvider spriteProvider) {
         this.spriteProvider = spriteProvider;
      }

      public Particle createParticle(DefaultParticleType defaultParticleType, ClientWorld clientWorld, double d, double e, double f, double g, double h, double i) {
         return new DragonBreathParticle(clientWorld, d, e, f, g, h, i, this.spriteProvider);
      }
   }
}
