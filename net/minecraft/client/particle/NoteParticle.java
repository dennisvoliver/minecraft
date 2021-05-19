package net.minecraft.client.particle;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.DefaultParticleType;
import net.minecraft.util.math.MathHelper;

@Environment(EnvType.CLIENT)
public class NoteParticle extends SpriteBillboardParticle {
   private NoteParticle(ClientWorld world, double x, double y, double z, double d) {
      super(world, x, y, z, 0.0D, 0.0D, 0.0D);
      this.velocityX *= 0.009999999776482582D;
      this.velocityY *= 0.009999999776482582D;
      this.velocityZ *= 0.009999999776482582D;
      this.velocityY += 0.2D;
      this.colorRed = Math.max(0.0F, MathHelper.sin(((float)d + 0.0F) * 6.2831855F) * 0.65F + 0.35F);
      this.colorGreen = Math.max(0.0F, MathHelper.sin(((float)d + 0.33333334F) * 6.2831855F) * 0.65F + 0.35F);
      this.colorBlue = Math.max(0.0F, MathHelper.sin(((float)d + 0.6666667F) * 6.2831855F) * 0.65F + 0.35F);
      this.scale *= 1.5F;
      this.maxAge = 6;
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

         this.velocityX *= 0.6600000262260437D;
         this.velocityY *= 0.6600000262260437D;
         this.velocityZ *= 0.6600000262260437D;
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
         NoteParticle noteParticle = new NoteParticle(clientWorld, d, e, f, g);
         noteParticle.setSprite(this.spriteProvider);
         return noteParticle;
      }
   }
}
