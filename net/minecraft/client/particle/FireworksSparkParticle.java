package net.minecraft.client.particle;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.item.FireworkItem;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.particle.DefaultParticleType;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class FireworksSparkParticle {
   @Environment(EnvType.CLIENT)
   public static class ExplosionFactory implements ParticleFactory<DefaultParticleType> {
      private final SpriteProvider spriteProvider;

      public ExplosionFactory(SpriteProvider spriteProvider) {
         this.spriteProvider = spriteProvider;
      }

      public Particle createParticle(DefaultParticleType defaultParticleType, ClientWorld clientWorld, double d, double e, double f, double g, double h, double i) {
         FireworksSparkParticle.Explosion explosion = new FireworksSparkParticle.Explosion(clientWorld, d, e, f, g, h, i, MinecraftClient.getInstance().particleManager, this.spriteProvider);
         explosion.setColorAlpha(0.99F);
         return explosion;
      }
   }

   @Environment(EnvType.CLIENT)
   public static class FlashFactory implements ParticleFactory<DefaultParticleType> {
      private final SpriteProvider spriteProvider;

      public FlashFactory(SpriteProvider spriteProvider) {
         this.spriteProvider = spriteProvider;
      }

      public Particle createParticle(DefaultParticleType defaultParticleType, ClientWorld clientWorld, double d, double e, double f, double g, double h, double i) {
         FireworksSparkParticle.Flash flash = new FireworksSparkParticle.Flash(clientWorld, d, e, f);
         flash.setSprite(this.spriteProvider);
         return flash;
      }
   }

   @Environment(EnvType.CLIENT)
   public static class Flash extends SpriteBillboardParticle {
      private Flash(ClientWorld world, double x, double y, double z) {
         super(world, x, y, z);
         this.maxAge = 4;
      }

      public ParticleTextureSheet getType() {
         return ParticleTextureSheet.PARTICLE_SHEET_TRANSLUCENT;
      }

      public void buildGeometry(VertexConsumer vertexConsumer, Camera camera, float tickDelta) {
         this.setColorAlpha(0.6F - ((float)this.age + tickDelta - 1.0F) * 0.25F * 0.5F);
         super.buildGeometry(vertexConsumer, camera, tickDelta);
      }

      public float getSize(float tickDelta) {
         return 7.1F * MathHelper.sin(((float)this.age + tickDelta - 1.0F) * 0.25F * 3.1415927F);
      }
   }

   @Environment(EnvType.CLIENT)
   static class Explosion extends AnimatedParticle {
      private boolean trail;
      private boolean flicker;
      private final ParticleManager particleManager;
      private float field_3801;
      private float field_3800;
      private float field_3799;
      private boolean field_3802;

      private Explosion(ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ, ParticleManager particleManager, SpriteProvider spriteProvider) {
         super(world, x, y, z, spriteProvider, -0.004F);
         this.velocityX = velocityX;
         this.velocityY = velocityY;
         this.velocityZ = velocityZ;
         this.particleManager = particleManager;
         this.scale *= 0.75F;
         this.maxAge = 48 + this.random.nextInt(12);
         this.setSpriteForAge(spriteProvider);
      }

      public void setTrail(boolean trail) {
         this.trail = trail;
      }

      public void setFlicker(boolean flicker) {
         this.flicker = flicker;
      }

      public void buildGeometry(VertexConsumer vertexConsumer, Camera camera, float tickDelta) {
         if (!this.flicker || this.age < this.maxAge / 3 || (this.age + this.maxAge) / 3 % 2 == 0) {
            super.buildGeometry(vertexConsumer, camera, tickDelta);
         }

      }

      public void tick() {
         super.tick();
         if (this.trail && this.age < this.maxAge / 2 && (this.age + this.maxAge) % 2 == 0) {
            FireworksSparkParticle.Explosion explosion = new FireworksSparkParticle.Explosion(this.world, this.x, this.y, this.z, 0.0D, 0.0D, 0.0D, this.particleManager, this.spriteProvider);
            explosion.setColorAlpha(0.99F);
            explosion.setColor(this.colorRed, this.colorGreen, this.colorBlue);
            explosion.age = explosion.maxAge / 2;
            if (this.field_3802) {
               explosion.field_3802 = true;
               explosion.field_3801 = this.field_3801;
               explosion.field_3800 = this.field_3800;
               explosion.field_3799 = this.field_3799;
            }

            explosion.flicker = this.flicker;
            this.particleManager.addParticle(explosion);
         }

      }
   }

   @Environment(EnvType.CLIENT)
   public static class FireworkParticle extends NoRenderParticle {
      private int age;
      private final ParticleManager particleManager;
      private ListTag explosions;
      private boolean flicker;

      public FireworkParticle(ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ, ParticleManager particleManager, @Nullable CompoundTag tag) {
         super(world, x, y, z);
         this.velocityX = velocityX;
         this.velocityY = velocityY;
         this.velocityZ = velocityZ;
         this.particleManager = particleManager;
         this.maxAge = 8;
         if (tag != null) {
            this.explosions = tag.getList("Explosions", 10);
            if (this.explosions.isEmpty()) {
               this.explosions = null;
            } else {
               this.maxAge = this.explosions.size() * 2 - 1;

               for(int i = 0; i < this.explosions.size(); ++i) {
                  CompoundTag compoundTag = this.explosions.getCompound(i);
                  if (compoundTag.getBoolean("Flicker")) {
                     this.flicker = true;
                     this.maxAge += 15;
                     break;
                  }
               }
            }
         }

      }

      public void tick() {
         boolean bl5;
         if (this.age == 0 && this.explosions != null) {
            bl5 = this.isFar();
            boolean bl2 = false;
            if (this.explosions.size() >= 3) {
               bl2 = true;
            } else {
               for(int i = 0; i < this.explosions.size(); ++i) {
                  CompoundTag compoundTag = this.explosions.getCompound(i);
                  if (FireworkItem.Type.byId(compoundTag.getByte("Type")) == FireworkItem.Type.LARGE_BALL) {
                     bl2 = true;
                     break;
                  }
               }
            }

            SoundEvent soundEvent2;
            if (bl2) {
               soundEvent2 = bl5 ? SoundEvents.ENTITY_FIREWORK_ROCKET_LARGE_BLAST_FAR : SoundEvents.ENTITY_FIREWORK_ROCKET_LARGE_BLAST;
            } else {
               soundEvent2 = bl5 ? SoundEvents.ENTITY_FIREWORK_ROCKET_BLAST_FAR : SoundEvents.ENTITY_FIREWORK_ROCKET_BLAST;
            }

            this.world.playSound(this.x, this.y, this.z, soundEvent2, SoundCategory.AMBIENT, 20.0F, 0.95F + this.random.nextFloat() * 0.1F, true);
         }

         if (this.age % 2 == 0 && this.explosions != null && this.age / 2 < this.explosions.size()) {
            int j = this.age / 2;
            CompoundTag compoundTag2 = this.explosions.getCompound(j);
            FireworkItem.Type type = FireworkItem.Type.byId(compoundTag2.getByte("Type"));
            boolean bl3 = compoundTag2.getBoolean("Trail");
            boolean bl4 = compoundTag2.getBoolean("Flicker");
            int[] is = compoundTag2.getIntArray("Colors");
            int[] js = compoundTag2.getIntArray("FadeColors");
            if (is.length == 0) {
               is = new int[]{DyeColor.BLACK.getFireworkColor()};
            }

            switch(type) {
            case SMALL_BALL:
            default:
               this.explodeBall(0.25D, 2, is, js, bl3, bl4);
               break;
            case LARGE_BALL:
               this.explodeBall(0.5D, 4, is, js, bl3, bl4);
               break;
            case STAR:
               this.explodeStar(0.5D, new double[][]{{0.0D, 1.0D}, {0.3455D, 0.309D}, {0.9511D, 0.309D}, {0.3795918367346939D, -0.12653061224489795D}, {0.6122448979591837D, -0.8040816326530612D}, {0.0D, -0.35918367346938773D}}, is, js, bl3, bl4, false);
               break;
            case CREEPER:
               this.explodeStar(0.5D, new double[][]{{0.0D, 0.2D}, {0.2D, 0.2D}, {0.2D, 0.6D}, {0.6D, 0.6D}, {0.6D, 0.2D}, {0.2D, 0.2D}, {0.2D, 0.0D}, {0.4D, 0.0D}, {0.4D, -0.6D}, {0.2D, -0.6D}, {0.2D, -0.4D}, {0.0D, -0.4D}}, is, js, bl3, bl4, true);
               break;
            case BURST:
               this.explodeBurst(is, js, bl3, bl4);
            }

            int k = is[0];
            float f = (float)((k & 16711680) >> 16) / 255.0F;
            float g = (float)((k & '\uff00') >> 8) / 255.0F;
            float h = (float)((k & 255) >> 0) / 255.0F;
            Particle particle = this.particleManager.addParticle(ParticleTypes.FLASH, this.x, this.y, this.z, 0.0D, 0.0D, 0.0D);
            particle.setColor(f, g, h);
         }

         ++this.age;
         if (this.age > this.maxAge) {
            if (this.flicker) {
               bl5 = this.isFar();
               SoundEvent soundEvent3 = bl5 ? SoundEvents.ENTITY_FIREWORK_ROCKET_TWINKLE_FAR : SoundEvents.ENTITY_FIREWORK_ROCKET_TWINKLE;
               this.world.playSound(this.x, this.y, this.z, soundEvent3, SoundCategory.AMBIENT, 20.0F, 0.9F + this.random.nextFloat() * 0.15F, true);
            }

            this.markDead();
         }

      }

      private boolean isFar() {
         MinecraftClient minecraftClient = MinecraftClient.getInstance();
         return minecraftClient.gameRenderer.getCamera().getPos().squaredDistanceTo(this.x, this.y, this.z) >= 256.0D;
      }

      private void addExplosionParticle(double x, double y, double z, double velocityX, double velocityY, double velocityZ, int[] colors, int[] fadeColors, boolean trail, boolean flicker) {
         FireworksSparkParticle.Explosion explosion = (FireworksSparkParticle.Explosion)this.particleManager.addParticle(ParticleTypes.FIREWORK, x, y, z, velocityX, velocityY, velocityZ);
         explosion.setTrail(trail);
         explosion.setFlicker(flicker);
         explosion.setColorAlpha(0.99F);
         int i = this.random.nextInt(colors.length);
         explosion.setColor(colors[i]);
         if (fadeColors.length > 0) {
            explosion.setTargetColor(Util.getRandom(fadeColors, this.random));
         }

      }

      private void explodeBall(double size, int amount, int[] colors, int[] fadeColors, boolean trail, boolean flicker) {
         double d = this.x;
         double e = this.y;
         double f = this.z;

         for(int i = -amount; i <= amount; ++i) {
            for(int j = -amount; j <= amount; ++j) {
               for(int k = -amount; k <= amount; ++k) {
                  double g = (double)j + (this.random.nextDouble() - this.random.nextDouble()) * 0.5D;
                  double h = (double)i + (this.random.nextDouble() - this.random.nextDouble()) * 0.5D;
                  double l = (double)k + (this.random.nextDouble() - this.random.nextDouble()) * 0.5D;
                  double m = (double)MathHelper.sqrt(g * g + h * h + l * l) / size + this.random.nextGaussian() * 0.05D;
                  this.addExplosionParticle(d, e, f, g / m, h / m, l / m, colors, fadeColors, trail, flicker);
                  if (i != -amount && i != amount && j != -amount && j != amount) {
                     k += amount * 2 - 1;
                  }
               }
            }
         }

      }

      private void explodeStar(double size, double[][] pattern, int[] colors, int[] fadeColors, boolean trail, boolean flicker, boolean keepShape) {
         double d = pattern[0][0];
         double e = pattern[0][1];
         this.addExplosionParticle(this.x, this.y, this.z, d * size, e * size, 0.0D, colors, fadeColors, trail, flicker);
         float f = this.random.nextFloat() * 3.1415927F;
         double g = keepShape ? 0.034D : 0.34D;

         for(int i = 0; i < 3; ++i) {
            double h = (double)f + (double)((float)i * 3.1415927F) * g;
            double j = d;
            double k = e;

            for(int l = 1; l < pattern.length; ++l) {
               double m = pattern[l][0];
               double n = pattern[l][1];

               for(double o = 0.25D; o <= 1.0D; o += 0.25D) {
                  double p = MathHelper.lerp(o, j, m) * size;
                  double q = MathHelper.lerp(o, k, n) * size;
                  double r = p * Math.sin(h);
                  p *= Math.cos(h);

                  for(double s = -1.0D; s <= 1.0D; s += 2.0D) {
                     this.addExplosionParticle(this.x, this.y, this.z, p * s, q, r * s, colors, fadeColors, trail, flicker);
                  }
               }

               j = m;
               k = n;
            }
         }

      }

      private void explodeBurst(int[] colors, int[] fadeColors, boolean trail, boolean flocker) {
         double d = this.random.nextGaussian() * 0.05D;
         double e = this.random.nextGaussian() * 0.05D;

         for(int i = 0; i < 70; ++i) {
            double f = this.velocityX * 0.5D + this.random.nextGaussian() * 0.15D + d;
            double g = this.velocityZ * 0.5D + this.random.nextGaussian() * 0.15D + e;
            double h = this.velocityY * 0.5D + this.random.nextDouble() * 0.5D;
            this.addExplosionParticle(this.x, this.y, this.z, f, h, g, colors, fadeColors, trail, flocker);
         }

      }
   }
}
