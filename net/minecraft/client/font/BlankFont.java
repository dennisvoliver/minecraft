package net.minecraft.client.font;

import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.ints.IntSets;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class BlankFont implements Font {
   @Nullable
   public RenderableGlyph getGlyph(int codePoint) {
      return BlankGlyph.INSTANCE;
   }

   public IntSet getProvidedGlyphs() {
      return IntSets.EMPTY_SET;
   }
}
