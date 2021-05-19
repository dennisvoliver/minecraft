package net.minecraft.client.particle;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.world.ClientWorld;

@Environment(EnvType.CLIENT)
public abstract class AbstractSlowingParticle extends SpriteBillboardParticle {
   protected AbstractSlowingParticle(ClientWorld clientWorld, double d, double e, double f, double g, double h, double i) {
      super(clientWorld, d, e, f, g, h, i);
      this.velocityX = this.velocityX * 0.009999999776482582D + g;
      this.velocityY = this.velocityY * 0.009999999776482582D + h;
      this.velocityZ = this.velocityZ * 0.009999999776482582D + i;
      this.x += (double)((this.random.nextFloat() - this.random.nextFloat()) * 0.05F);
      this.y += (double)((this.random.nextFloat() - this.random.nextFloat()) * 0.05F);
      this.z += (double)((this.random.nextFloat() - this.random.nextFloat()) * 0.05F);
      this.maxAge = (int)(8.0D / (Math.random() * 0.8D + 0.2D)) + 4;
   }

   public void tick() {
      this.prevPosX = this.x;
      this.prevPosY = this.y;
      this.prevPosZ = this.z;
      if (this.age++ >= this.maxAge) {
         this.markDead();
      } else {
         this.move(this.velocityX, this.velocityY, this.velocityZ);
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
