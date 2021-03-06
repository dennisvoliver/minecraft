package net.minecraft.client.gui.widget;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ScreenTexts;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;

@Environment(EnvType.CLIENT)
public class SoundSliderWidget extends OptionSliderWidget {
   private final SoundCategory category;

   public SoundSliderWidget(MinecraftClient client, int x, int y, SoundCategory category, int width) {
      super(client.options, x, y, width, 20, (double)client.options.getSoundVolume(category));
      this.category = category;
      this.updateMessage();
   }

   protected void updateMessage() {
      Text text = (float)this.value == (float)this.getYImage(false) ? ScreenTexts.OFF : new LiteralText((int)(this.value * 100.0D) + "%");
      this.setMessage((new TranslatableText("soundCategory." + this.category.getName())).append(": ").append((Text)text));
   }

   protected void applyValue() {
      this.options.setSoundVolume(this.category, (float)this.value);
      this.options.write();
   }
}
