package net.minecraft.sound;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.function.BiFunction;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

/**
 * Represents an "additions sound" for a biome.
 */
public class BiomeAdditionsSound {
   public static final Codec<BiomeAdditionsSound> CODEC = RecordCodecBuilder.create((instance) -> {
      return instance.group(SoundEvent.CODEC.fieldOf("sound").forGetter((biomeAdditionsSound) -> {
         return biomeAdditionsSound.sound;
      }), Codec.DOUBLE.fieldOf("tick_chance").forGetter((biomeAdditionsSound) -> {
         return biomeAdditionsSound.chance;
      })).apply(instance, (BiFunction)(BiomeAdditionsSound::new));
   });
   private SoundEvent sound;
   private double chance;

   public BiomeAdditionsSound(SoundEvent sound, double chance) {
      this.sound = sound;
      this.chance = chance;
   }

   @Environment(EnvType.CLIENT)
   public SoundEvent getSound() {
      return this.sound;
   }

   /**
    * Returns the chance of this addition sound to play at any tick.
    */
   @Environment(EnvType.CLIENT)
   public double getChance() {
      return this.chance;
   }
}
