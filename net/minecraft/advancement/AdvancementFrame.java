package net.minecraft.advancement;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;

public enum AdvancementFrame {
   TASK("task", 0, Formatting.GREEN),
   CHALLENGE("challenge", 26, Formatting.DARK_PURPLE),
   GOAL("goal", 52, Formatting.GREEN);

   private final String id;
   private final int textureV;
   private final Formatting titleFormat;
   private final Text toastText;

   private AdvancementFrame(String id, int texV, Formatting titleFormat) {
      this.id = id;
      this.textureV = texV;
      this.titleFormat = titleFormat;
      this.toastText = new TranslatableText("advancements.toast." + id);
   }

   public String getId() {
      return this.id;
   }

   @Environment(EnvType.CLIENT)
   public int getTextureV() {
      return this.textureV;
   }

   public static AdvancementFrame forName(String name) {
      AdvancementFrame[] var1 = values();
      int var2 = var1.length;

      for(int var3 = 0; var3 < var2; ++var3) {
         AdvancementFrame advancementFrame = var1[var3];
         if (advancementFrame.id.equals(name)) {
            return advancementFrame;
         }
      }

      throw new IllegalArgumentException("Unknown frame type '" + name + "'");
   }

   public Formatting getTitleFormat() {
      return this.titleFormat;
   }

   @Environment(EnvType.CLIENT)
   public Text getToastText() {
      return this.toastText;
   }
}
