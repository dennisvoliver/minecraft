package net.minecraft.client.particle;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.DefaultParticleType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

@Environment(EnvType.CLIENT)
public class RainSplashParticle extends SpriteBillboardParticle {
   protected RainSplashParticle(ClientWorld clientWorld, double d, double e, double f) {
      super(clientWorld, d, e, f, 0.0D, 0.0D, 0.0D);
      this.velocityX *= 0.30000001192092896D;
      this.velocityY = Math.random() * 0.20000000298023224D + 0.10000000149011612D;
      this.velocityZ *= 0.30000001192092896D;
      this.setBoundingBoxSpacing(0.01F, 0.01F);
      this.gravityStrength = 0.06F;
      this.maxAge = (int)(8.0D / (Math.random() * 0.8D + 0.2D));
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
         this.velocityY -= (double)this.gravityStrength;
         this.move(this.velocityX, this.velocityY, this.velocityZ);
         this.velocityX *= 0.9800000190734863D;
         this.velocityY *= 0.9800000190734863D;
         this.velocityZ *= 0.9800000190734863D;
         if (this.onGround) {
            if (Math.random() < 0.5D) {
               this.markDead();
            }

            this.velocityX *= 0.699999988079071D;
            this.velocityZ *= 0.699999988079071D;
         }

         BlockPos blockPos = new BlockPos(this.x, this.y, this.z);
         double d = Math.max(this.world.getBlockState(blockPos).getCollisionShape(this.world, blockPos).getEndingCoord(Direction.Axis.Y, this.x - (double)blockPos.getX(), this.z - (double)blockPos.getZ()), (double)this.world.getFluidState(blockPos).getHeight(this.world, blockPos));
         if (d > 0.0D && this.y < (double)blockPos.getY() + d) {
            this.markDead();
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
         RainSplashParticle rainSplashParticle = new RainSplashParticle(clientWorld, d, e, f);
         rainSplashParticle.setSprite(this.spriteProvider);
         return rainSplashParticle;
      }
   }
}
