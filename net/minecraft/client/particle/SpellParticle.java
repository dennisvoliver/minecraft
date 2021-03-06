package net.minecraft.client.particle;

import java.util.Random;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.DefaultParticleType;

@Environment(EnvType.CLIENT)
public class SpellParticle extends SpriteBillboardParticle {
   private static final Random RANDOM = new Random();
   private final SpriteProvider spriteProvider;

   private SpellParticle(ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ, SpriteProvider spriteProvider) {
      super(world, x, y, z, 0.5D - RANDOM.nextDouble(), velocityY, 0.5D - RANDOM.nextDouble());
      this.spriteProvider = spriteProvider;
      this.velocityY *= 0.20000000298023224D;
      if (velocityX == 0.0D && velocityZ == 0.0D) {
         this.velocityX *= 0.10000000149011612D;
         this.velocityZ *= 0.10000000149011612D;
      }

      this.scale *= 0.75F;
      this.maxAge = (int)(8.0D / (Math.random() * 0.8D + 0.2D));
      this.collidesWithWorld = false;
      this.setSpriteForAge(spriteProvider);
   }

   public ParticleTextureSheet getType() {
      return ParticleTextureSheet.PARTICLE_SHEET_TRANSLUCENT;
   }

   public void tick() {
      this.prevPosX = this.x;
      this.prevPosY = this.y;
      this.prevPosZ = this.z;
      if (this.age++ >= this.maxAge) {
         this.markDead();
      } else {
         this.setSpriteForAge(this.spriteProvider);
         this.velocityY += 0.004D;
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
   public static class InstantFactory implements ParticleFactory<DefaultParticleType> {
      private final SpriteProvider field_17872;

      public InstantFactory(SpriteProvider spriteProvider) {
         this.field_17872 = spriteProvider;
      }

      public Particle createParticle(DefaultParticleType defaultParticleType, ClientWorld clientWorld, double d, double e, double f, double g, double h, double i) {
         return new SpellParticle(clientWorld, d, e, f, g, h, i, this.field_17872);
      }
   }

   @Environment(EnvType.CLIENT)
   public static class WitchFactory implements ParticleFactory<DefaultParticleType> {
      private final SpriteProvider field_17875;

      public WitchFactory(SpriteProvider spriteProvider) {
         this.field_17875 = spriteProvider;
      }

      public Particle createParticle(DefaultParticleType defaultParticleType, ClientWorld clientWorld, double d, double e, double f, double g, double h, double i) {
         SpellParticle spellParticle = new SpellParticle(clientWorld, d, e, f, g, h, i, this.field_17875);
         float j = clientWorld.random.nextFloat() * 0.5F + 0.35F;
         spellParticle.setColor(1.0F * j, 0.0F * j, 1.0F * j);
         return spellParticle;
      }
   }

   @Environment(EnvType.CLIENT)
   public static class EntityAmbientFactory implements ParticleFactory<DefaultParticleType> {
      private final SpriteProvider spriteProvider;

      public EntityAmbientFactory(SpriteProvider spriteProvider) {
         this.spriteProvider = spriteProvider;
      }

      public Particle createParticle(DefaultParticleType defaultParticleType, ClientWorld clientWorld, double d, double e, double f, double g, double h, double i) {
         Particle particle = new SpellParticle(clientWorld, d, e, f, g, h, i, this.spriteProvider);
         particle.setColorAlpha(0.15F);
         particle.setColor((float)g, (float)h, (float)i);
         return particle;
      }
   }

   @Environment(EnvType.CLIENT)
   public static class EntityFactory implements ParticleFactory<DefaultParticleType> {
      private final SpriteProvider field_17873;

      public EntityFactory(SpriteProvider spriteProvider) {
         this.field_17873 = spriteProvider;
      }

      public Particle createParticle(DefaultParticleType defaultParticleType, ClientWorld clientWorld, double d, double e, double f, double g, double h, double i) {
         Particle particle = new SpellParticle(clientWorld, d, e, f, g, h, i, this.field_17873);
         particle.setColor((float)g, (float)h, (float)i);
         return particle;
      }
   }

   @Environment(EnvType.CLIENT)
   public static class DefaultFactory implements ParticleFactory<DefaultParticleType> {
      private final SpriteProvider spriteProvider;

      public DefaultFactory(SpriteProvider spriteProvider) {
         this.spriteProvider = spriteProvider;
      }

      public Particle createParticle(DefaultParticleType defaultParticleType, ClientWorld clientWorld, double d, double e, double f, double g, double h, double i) {
         return new SpellParticle(clientWorld, d, e, f, g, h, i, this.spriteProvider);
      }
   }
}
