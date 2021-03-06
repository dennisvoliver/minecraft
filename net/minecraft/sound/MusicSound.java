package net.minecraft.sound;

import com.mojang.datafixers.util.Function4;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

public class MusicSound {
   public static final Codec<MusicSound> CODEC = RecordCodecBuilder.create((instance) -> {
      return instance.group(SoundEvent.CODEC.fieldOf("sound").forGetter((musicSound) -> {
         return musicSound.sound;
      }), Codec.INT.fieldOf("min_delay").forGetter((musicSound) -> {
         return musicSound.minDelay;
      }), Codec.INT.fieldOf("max_delay").forGetter((musicSound) -> {
         return musicSound.maxDelay;
      }), Codec.BOOL.fieldOf("replace_current_music").forGetter((musicSound) -> {
         return musicSound.replaceCurrentMusic;
      })).apply(instance, (Function4)(MusicSound::new));
   });
   private final SoundEvent sound;
   private final int minDelay;
   private final int maxDelay;
   private final boolean replaceCurrentMusic;

   public MusicSound(SoundEvent sound, int minDelay, int maxDelay, boolean replaceCurrentMusic) {
      this.sound = sound;
      this.minDelay = minDelay;
      this.maxDelay = maxDelay;
      this.replaceCurrentMusic = replaceCurrentMusic;
   }

   @Environment(EnvType.CLIENT)
   public SoundEvent getSound() {
      return this.sound;
   }

   @Environment(EnvType.CLIENT)
   public int getMinDelay() {
      return this.minDelay;
   }

   @Environment(EnvType.CLIENT)
   public int getMaxDelay() {
      return this.maxDelay;
   }

   @Environment(EnvType.CLIENT)
   public boolean shouldReplaceCurrentMusic() {
      return this.replaceCurrentMusic;
   }
}
