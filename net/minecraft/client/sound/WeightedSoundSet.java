package net.minecraft.client.sound;

import com.google.common.collect.Lists;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class WeightedSoundSet implements SoundContainer<Sound> {
   private final List<SoundContainer<Sound>> sounds = Lists.newArrayList();
   private final Random random = new Random();
   private final Identifier id;
   @Nullable
   private final Text subtitle;

   public WeightedSoundSet(Identifier id, @Nullable String subtitle) {
      this.id = id;
      this.subtitle = subtitle == null ? null : new TranslatableText(subtitle);
   }

   public int getWeight() {
      int i = 0;

      SoundContainer soundContainer;
      for(Iterator var2 = this.sounds.iterator(); var2.hasNext(); i += soundContainer.getWeight()) {
         soundContainer = (SoundContainer)var2.next();
      }

      return i;
   }

   public Sound getSound() {
      int i = this.getWeight();
      if (!this.sounds.isEmpty() && i != 0) {
         int j = this.random.nextInt(i);
         Iterator var3 = this.sounds.iterator();

         SoundContainer soundContainer;
         do {
            if (!var3.hasNext()) {
               return SoundManager.MISSING_SOUND;
            }

            soundContainer = (SoundContainer)var3.next();
            j -= soundContainer.getWeight();
         } while(j >= 0);

         return (Sound)soundContainer.getSound();
      } else {
         return SoundManager.MISSING_SOUND;
      }
   }

   public void add(SoundContainer<Sound> soundContainer) {
      this.sounds.add(soundContainer);
   }

   @Nullable
   public Text getSubtitle() {
      return this.subtitle;
   }

   public void preload(SoundSystem soundSystem) {
      Iterator var2 = this.sounds.iterator();

      while(var2.hasNext()) {
         SoundContainer<Sound> soundContainer = (SoundContainer)var2.next();
         soundContainer.preload(soundSystem);
      }

   }
}
