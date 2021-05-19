package net.minecraft.client.font;

import it.unimi.dsi.fastutil.ints.IntArraySet;
import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.stream.IntStream;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.texture.NativeImage;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.stb.STBTTFontinfo;
import org.lwjgl.stb.STBTruetype;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

@Environment(EnvType.CLIENT)
public class TrueTypeFont implements Font {
   private final ByteBuffer field_21839;
   private final STBTTFontinfo info;
   private final float oversample;
   private final IntSet excludedCharacters = new IntArraySet();
   private final float shiftX;
   private final float shiftY;
   private final float scaleFactor;
   private final float ascent;

   public TrueTypeFont(ByteBuffer byteBuffer, STBTTFontinfo sTBTTFontinfo, float f, float g, float h, float i, String string) {
      this.field_21839 = byteBuffer;
      this.info = sTBTTFontinfo;
      this.oversample = g;
      IntStream var10000 = string.codePoints();
      IntSet var10001 = this.excludedCharacters;
      var10000.forEach(var10001::add);
      this.shiftX = h * g;
      this.shiftY = i * g;
      this.scaleFactor = STBTruetype.stbtt_ScaleForPixelHeight(sTBTTFontinfo, f * g);
      MemoryStack memoryStack = MemoryStack.stackPush();
      Throwable var9 = null;

      try {
         IntBuffer intBuffer = memoryStack.mallocInt(1);
         IntBuffer intBuffer2 = memoryStack.mallocInt(1);
         IntBuffer intBuffer3 = memoryStack.mallocInt(1);
         STBTruetype.stbtt_GetFontVMetrics(sTBTTFontinfo, intBuffer, intBuffer2, intBuffer3);
         this.ascent = (float)intBuffer.get(0) * this.scaleFactor;
      } catch (Throwable var20) {
         var9 = var20;
         throw var20;
      } finally {
         if (memoryStack != null) {
            if (var9 != null) {
               try {
                  memoryStack.close();
               } catch (Throwable var19) {
                  var9.addSuppressed(var19);
               }
            } else {
               memoryStack.close();
            }
         }

      }

   }

   @Nullable
   public TrueTypeFont.TtfGlyph getGlyph(int i) {
      if (this.excludedCharacters.contains(i)) {
         return null;
      } else {
         MemoryStack memoryStack = MemoryStack.stackPush();
         Throwable var3 = null;

         Object var9;
         try {
            IntBuffer intBuffer = memoryStack.mallocInt(1);
            IntBuffer intBuffer2 = memoryStack.mallocInt(1);
            IntBuffer intBuffer3 = memoryStack.mallocInt(1);
            IntBuffer intBuffer4 = memoryStack.mallocInt(1);
            int j = STBTruetype.stbtt_FindGlyphIndex(this.info, i);
            if (j != 0) {
               STBTruetype.stbtt_GetGlyphBitmapBoxSubpixel(this.info, j, this.scaleFactor, this.scaleFactor, this.shiftX, this.shiftY, intBuffer, intBuffer2, intBuffer3, intBuffer4);
               int k = intBuffer3.get(0) - intBuffer.get(0);
               int l = intBuffer4.get(0) - intBuffer2.get(0);
               IntBuffer intBuffer5;
               if (k != 0 && l != 0) {
                  intBuffer5 = memoryStack.mallocInt(1);
                  IntBuffer intBuffer6 = memoryStack.mallocInt(1);
                  STBTruetype.stbtt_GetGlyphHMetrics(this.info, j, intBuffer5, intBuffer6);
                  TrueTypeFont.TtfGlyph var13 = new TrueTypeFont.TtfGlyph(intBuffer.get(0), intBuffer3.get(0), -intBuffer2.get(0), -intBuffer4.get(0), (float)intBuffer5.get(0) * this.scaleFactor, (float)intBuffer6.get(0) * this.scaleFactor, j);
                  return var13;
               }

               intBuffer5 = null;
               return intBuffer5;
            }

            var9 = null;
         } catch (Throwable var24) {
            var3 = var24;
            throw var24;
         } finally {
            if (memoryStack != null) {
               if (var3 != null) {
                  try {
                     memoryStack.close();
                  } catch (Throwable var23) {
                     var3.addSuppressed(var23);
                  }
               } else {
                  memoryStack.close();
               }
            }

         }

         return (TrueTypeFont.TtfGlyph)var9;
      }
   }

   public void close() {
      this.info.free();
      MemoryUtil.memFree((Buffer)this.field_21839);
   }

   public IntSet getProvidedGlyphs() {
      return (IntSet)IntStream.range(0, 65535).filter((i) -> {
         return !this.excludedCharacters.contains(i);
      }).collect(IntOpenHashSet::new, IntCollection::add, IntCollection::addAll);
   }

   @Environment(EnvType.CLIENT)
   class TtfGlyph implements RenderableGlyph {
      private final int width;
      private final int height;
      private final float bearingX;
      private final float ascent;
      private final float advance;
      private final int glyphIndex;

      private TtfGlyph(int xMin, int xMax, int yMax, int yMin, float advance, float bearing, int index) {
         this.width = xMax - xMin;
         this.height = yMax - yMin;
         this.advance = advance / TrueTypeFont.this.oversample;
         this.bearingX = (bearing + (float)xMin + TrueTypeFont.this.shiftX) / TrueTypeFont.this.oversample;
         this.ascent = (TrueTypeFont.this.ascent - (float)yMax + TrueTypeFont.this.shiftY) / TrueTypeFont.this.oversample;
         this.glyphIndex = index;
      }

      public int getWidth() {
         return this.width;
      }

      public int getHeight() {
         return this.height;
      }

      public float getOversample() {
         return TrueTypeFont.this.oversample;
      }

      public float getAdvance() {
         return this.advance;
      }

      public float getBearingX() {
         return this.bearingX;
      }

      public float getAscent() {
         return this.ascent;
      }

      public void upload(int x, int y) {
         NativeImage nativeImage = new NativeImage(NativeImage.Format.LUMINANCE, this.width, this.height, false);
         nativeImage.makeGlyphBitmapSubpixel(TrueTypeFont.this.info, this.glyphIndex, this.width, this.height, TrueTypeFont.this.scaleFactor, TrueTypeFont.this.scaleFactor, TrueTypeFont.this.shiftX, TrueTypeFont.this.shiftY, 0, 0);
         nativeImage.upload(0, x, y, 0, 0, this.width, this.height, false, true);
      }

      public boolean hasColor() {
         return false;
      }
   }
}
