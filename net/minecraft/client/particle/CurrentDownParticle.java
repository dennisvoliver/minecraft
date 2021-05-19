package net.minecraft.client.particle;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.DefaultParticleType;
import net.minecraft.tag.FluidTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;

@Environment(EnvType.CLIENT)
public class CurrentDownParticle extends SpriteBillboardParticle {
   /**
    * The angle, in radians, of the horizontal acceleration of the particle.
    */
   private float accelerationAngle;

   private CurrentDownParticle(ClientWorld world, double x, double y, double z) {
      super(world, x, y, z);
      this.maxAge = (int)(Math.random() * 60.0D) + 30;
      this.collidesWithWorld = false;
      this.velocityX = 0.0D;
      this.velocityY = -0.05D;
      this.velocityZ = 0.0D;
      this.setBoundingBoxSpacing(0.02F, 0.02F);
      this.scale *= this.random.nextFloat() * 0.6F + 0.2F;
      this.gravityStrength = 0.002F;
   }

   public ParticleTextureSheet getType() {
      return ParticleTextureSheet.PARTICLE_SHEET_OPAQUE;
   }

   public void tick() {
      this.prevPosX = this.x;
      this.prevPosY = this.y;
      this.prevPosZ = this.z;
      if (this.age++ >= this.maxAge) {
         this.markDead();
      } else {
         float f = 0.6F;
         this.velocityX += (double)(0.6F * MathHelper.cos(this.accelerationAngle));
         this.velocityZ += (double)(0.6F * MathHelper.sin(this.accelerationAngle));
         this.velocityX *= 0.07D;
         this.velocityZ *= 0.07D;
         this.move(this.velocityX, this.velocityY, this.velocityZ);
         if (!this.world.getFluidState(new BlockPos(this.x, this.y, this.z)).isIn(FluidTags.WATER) || this.onGround) {
            this.markDead();
         }

         this.accelerationAngle = (float)((double)this.accelerationAngle + 0.08D);
      }
   }

   @Environment(EnvType.CLIENT)
   public static class Factory implements ParticleFactory<DefaultParticleType> {
      private final SpriteProvider spriteProvider;

      public Factory(SpriteProvider spriteProvider) {
         this.spriteProvider = spriteProvider;
      }

      public Particle createParticle(DefaultParticleType defaultParticleType, ClientWorld clientWorld, double d, double e, double f, double g, double h, double i) {
         CurrentDownParticle currentDownParticle = new CurrentDownParticle(clientWorld, d, e, f);
         currentDownParticle.setSprite(this.spriteProvider);
         return currentDownParticle;
      }
   }
}
