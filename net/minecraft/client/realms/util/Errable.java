package net.minecraft.client.realms.util;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;

@Environment(EnvType.CLIENT)
public interface Errable {
   void error(Text text);

   default void error(String string) {
      this.error((Text)(new LiteralText(string)));
   }
}
