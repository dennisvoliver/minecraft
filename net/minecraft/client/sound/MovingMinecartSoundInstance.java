package net.minecraft.client.sound;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.Entity;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.MathHelper;

/**
 * A sound instance played when a minecart is moving.
 */
@Environment(EnvType.CLIENT)
public class MovingMinecartSoundInstance extends MovingSoundInstance {
   private final AbstractMinecartEntity minecart;
   private float distance = 0.0F;

   public MovingMinecartSoundInstance(AbstractMinecartEntity minecart) {
      super(SoundEvents.ENTITY_MINECART_RIDING, SoundCategory.NEUTRAL);
      this.minecart = minecart;
      this.repeat = true;
      this.repeatDelay = 0;
      this.volume = 0.0F;
      this.x = (double)((float)minecart.getX());
      this.y = (double)((float)minecart.getY());
      this.z = (double)((float)minecart.getZ());
   }

   public boolean canPlay() {
      return !this.minecart.isSilent();
   }

   public boolean shouldAlwaysPlay() {
      return true;
   }

   public void tick() {
      if (this.minecart.removed) {
         this.setDone();
      } else {
         this.x = (double)((float)this.minecart.getX());
         this.y = (double)((float)this.minecart.getY());
         this.z = (double)((float)this.minecart.getZ());
         float f = MathHelper.sqrt(Entity.squaredHorizontalLength(this.minecart.getVelocity()));
         if ((double)f >= 0.01D) {
            this.distance = MathHelper.clamp(this.distance + 0.0025F, 0.0F, 1.0F);
            this.volume = MathHelper.lerp(MathHelper.clamp(f, 0.0F, 0.5F), 0.0F, 0.7F);
         } else {
            this.distance = 0.0F;
            this.volume = 0.0F;
         }

      }
   }
}
