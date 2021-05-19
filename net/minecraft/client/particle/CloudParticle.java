package net.minecraft.client.particle;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.DefaultParticleType;
import net.minecraft.util.math.MathHelper;

@Environment(EnvType.CLIENT)
public class CloudParticle extends SpriteBillboardParticle {
   private final SpriteProvider spriteProvider;

   private CloudParticle(ClientWorld world, double x, double y, double z, double d, double e, double f, SpriteProvider spriteProvider) {
      super(world, x, y, z, 0.0D, 0.0D, 0.0D);
      this.spriteProvider = spriteProvider;
      float g = 2.5F;
      this.velocityX *= 0.10000000149011612D;
      this.velocityY *= 0.10000000149011612D;
      this.velocityZ *= 0.10000000149011612D;
      this.velocityX += d;
      this.velocityY += e;
      this.velocityZ += f;
      float h = 1.0F - (float)(Math.random() * 0.30000001192092896D);
      this.colorRed = h;
      this.colorGreen = h;
      this.colorBlue = h;
      this.scale *= 1.875F;
      int i = (int)(8.0D / (Math.random() * 0.8D + 0.3D));
      this.maxAge = (int)Math.max((float)i * 2.5F, 1.0F);
      this.collidesWithWorld = false;
      this.setSpriteForAge(spriteProvider);
   }

   public ParticleTextureSheet getType() {
      return ParticleTextureSheet.PARTICLE_SHEET_TRANSLUCENT;
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
         this.velocityX *= 0.9599999785423279D;
         this.velocityY *= 0.9599999785423279D;
         this.velocityZ *= 0.9599999785423279D;
         PlayerEntity playerEntity = this.world.getClosestPlayer(this.x, this.y, this.z, 2.0D, false);
         if (playerEntity != null) {
            double d = playerEntity.getY();
            if (this.y > d) {
               this.y += (d - this.y) * 0.2D;
               this.velocityY += (playerEntity.getVelocity().y - this.velocityY) * 0.2D;
               this.setPos(this.x, this.y, this.z);
            }
         }

         if (this.onGround) {
            this.velocityX *= 0.699999988079071D;
            this.velocityZ *= 0.699999988079071D;
         }

      }
   }

   @Environment(EnvType.CLIENT)
   public static class SneezeFactory implements ParticleFactory<DefaultParticleType> {
      private final SpriteProvider spriteProvider;

      public SneezeFactory(SpriteProvider spriteProvider) {
         this.spriteProvider = spriteProvider;
      }

      public Particle createParticle(DefaultParticleType defaultParticleType, ClientWorld clientWorld, double d, double e, double f, double g, double h, double i) {
         Particle particle = new CloudParticle(clientWorld, d, e, f, g, h, i, this.spriteProvider);
         particle.setColor(200.0F, 50.0F, 120.0F);
         particle.setColorAlpha(0.4F);
         return particle;
      }
   }

   @Environment(EnvType.CLIENT)
   public static class CloudFactory implements ParticleFactory<DefaultParticleType> {
      private final SpriteProvider spriteProvider;

      public CloudFactory(SpriteProvider spriteProvider) {
         this.spriteProvider = spriteProvider;
      }

      public Particle createParticle(DefaultParticleType defaultParticleType, ClientWorld clientWorld, double d, double e, double f, double g, double h, double i) {
         return new CloudParticle(clientWorld, d, e, f, g, h, i, this.spriteProvider);
      }
   }
}
