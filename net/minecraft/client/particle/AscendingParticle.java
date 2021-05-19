package net.minecraft.client.particle;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.MathHelper;

@Environment(EnvType.CLIENT)
public class AscendingParticle extends SpriteBillboardParticle {
   private final SpriteProvider spriteProvider;
   private final double ascendingAcceleration;

   protected AscendingParticle(ClientWorld world, double x, double y, double z, float randomVelocityXMultiplier, float randomVelocityYMultiplier, float randomVelocityZMultiplier, double velocityX, double velocityY, double velocityZ, float scaleMultiplier, SpriteProvider spriteProvider, float colorMultiplier, int baseMaxAge, double ascendingAcceleration, boolean collidesWithWorld) {
      super(world, x, y, z, 0.0D, 0.0D, 0.0D);
      this.ascendingAcceleration = ascendingAcceleration;
      this.spriteProvider = spriteProvider;
      this.velocityX *= (double)randomVelocityXMultiplier;
      this.velocityY *= (double)randomVelocityYMultiplier;
      this.velocityZ *= (double)randomVelocityZMultiplier;
      this.velocityX += velocityX;
      this.velocityY += velocityY;
      this.velocityZ += velocityZ;
      float f = world.random.nextFloat() * colorMultiplier;
      this.colorRed = f;
      this.colorGreen = f;
      this.colorBlue = f;
      this.scale *= 0.75F * scaleMultiplier;
      this.maxAge = (int)((double)baseMaxAge / ((double)world.random.nextFloat() * 0.8D + 0.2D));
      this.maxAge = (int)((float)this.maxAge * scaleMultiplier);
      this.maxAge = Math.max(this.maxAge, 1);
      this.setSpriteForAge(spriteProvider);
      this.collidesWithWorld = collidesWithWorld;
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
         this.velocityY += this.ascendingAcceleration;
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
}
