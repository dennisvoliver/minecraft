package net.minecraft.client.font;

import com.google.common.collect.Maps;
import com.google.gson.JsonObject;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class UnicodeTextureFont implements Font {
   private static final Logger LOGGER = LogManager.getLogger();
   private final ResourceManager resourceManager;
   private final byte[] sizes;
   private final String template;
   private final Map<Identifier, NativeImage> images = Maps.newHashMap();

   public UnicodeTextureFont(ResourceManager resourceManager, byte[] sizes, String template) {
      this.resourceManager = resourceManager;
      this.sizes = sizes;
      this.template = template;

      label324:
      for(int i = 0; i < 256; ++i) {
         int j = i * 256;
         Identifier identifier = this.getImageId(j);

         try {
            Resource resource = this.resourceManager.getResource(identifier);
            Throwable var8 = null;

            try {
               NativeImage nativeImage = NativeImage.read(NativeImage.Format.ABGR, resource.getInputStream());
               Throwable var10 = null;

               try {
                  if (nativeImage.getWidth() == 256 && nativeImage.getHeight() == 256) {
                     int k = 0;

                     while(true) {
                        if (k >= 256) {
                           continue label324;
                        }

                        byte b = sizes[j + k];
                        if (b != 0 && getStart(b) > getEnd(b)) {
                           sizes[j + k] = 0;
                        }

                        ++k;
                     }
                  }
               } catch (Throwable var39) {
                  var10 = var39;
                  throw var39;
               } finally {
                  if (nativeImage != null) {
                     if (var10 != null) {
                        try {
                           nativeImage.close();
                        } catch (Throwable var38) {
                           var10.addSuppressed(var38);
                        }
                     } else {
                        nativeImage.close();
                     }
                  }

               }
            } catch (Throwable var41) {
               var8 = var41;
               throw var41;
            } finally {
               if (resource != null) {
                  if (var8 != null) {
                     try {
                        resource.close();
                     } catch (Throwable var37) {
                        var8.addSuppressed(var37);
                     }
                  } else {
                     resource.close();
                  }
               }

            }
         } catch (IOException var43) {
         }

         Arrays.fill(sizes, j, j + 256, (byte)0);
      }

   }

   public void close() {
      this.images.values().forEach(NativeImage::close);
   }

   private Identifier getImageId(int codePoint) {
      Identifier identifier = new Identifier(String.format(this.template, String.format("%02x", codePoint / 256)));
      return new Identifier(identifier.getNamespace(), "textures/" + identifier.getPath());
   }

   @Nullable
   public RenderableGlyph getGlyph(int codePoint) {
      if (codePoint >= 0 && codePoint <= 65535) {
         byte b = this.sizes[codePoint];
         if (b != 0) {
            NativeImage nativeImage = (NativeImage)this.images.computeIfAbsent(this.getImageId(codePoint), this::getGlyphImage);
            if (nativeImage != null) {
               int i = getStart(b);
               return new UnicodeTextureFont.UnicodeTextureGlyph(codePoint % 16 * 16 + i, (codePoint & 255) / 16 * 16, getEnd(b) - i, 16, nativeImage);
            }
         }

         return null;
      } else {
         return null;
      }
   }

   public IntSet getProvidedGlyphs() {
      IntSet intSet = new IntOpenHashSet();

      for(int i = 0; i < 65535; ++i) {
         if (this.sizes[i] != 0) {
            intSet.add(i);
         }
      }

      return intSet;
   }

   @Nullable
   private NativeImage getGlyphImage(Identifier glyphId) {
      try {
         Resource resource = this.resourceManager.getResource(glyphId);
         Throwable var3 = null;

         NativeImage var4;
         try {
            var4 = NativeImage.read(NativeImage.Format.ABGR, resource.getInputStream());
         } catch (Throwable var14) {
            var3 = var14;
            throw var14;
         } finally {
            if (resource != null) {
               if (var3 != null) {
                  try {
                     resource.close();
                  } catch (Throwable var13) {
                     var3.addSuppressed(var13);
                  }
               } else {
                  resource.close();
               }
            }

         }

         return var4;
      } catch (IOException var16) {
         LOGGER.error((String)"Couldn't load texture {}", (Object)glyphId, (Object)var16);
         return null;
      }
   }

   private static int getStart(byte size) {
      return size >> 4 & 15;
   }

   private static int getEnd(byte size) {
      return (size & 15) + 1;
   }

   @Environment(EnvType.CLIENT)
   static class UnicodeTextureGlyph implements RenderableGlyph {
      private final int width;
      private final int height;
      private final int unpackSkipPixels;
      private final int unpackSkipRows;
      private final NativeImage image;

      private UnicodeTextureGlyph(int x, int y, int width, int height, NativeImage image) {
         this.width = width;
         this.height = height;
         this.unpackSkipPixels = x;
         this.unpackSkipRows = y;
         this.image = image;
      }

      public float getOversample() {
         return 2.0F;
      }

      public int getWidth() {
         return this.width;
      }

      public int getHeight() {
         return this.height;
      }

      public float getAdvance() {
         return (float)(this.width / 2 + 1);
      }

      public void upload(int x, int y) {
         this.image.upload(0, x, y, this.unpackSkipPixels, this.unpackSkipRows, this.width, this.height, false, false);
      }

      public boolean hasColor() {
         return this.image.getFormat().getChannelCount() > 1;
      }

      public float getShadowOffset() {
         return 0.5F;
      }

      public float getBoldOffset() {
         return 0.5F;
      }
   }

   @Environment(EnvType.CLIENT)
   public static class Loader implements FontLoader {
      private final Identifier sizes;
      private final String template;

      public Loader(Identifier sizes, String template) {
         this.sizes = sizes;
         this.template = template;
      }

      public static FontLoader fromJson(JsonObject json) {
         return new UnicodeTextureFont.Loader(new Identifier(JsonHelper.getString(json, "sizes")), JsonHelper.getString(json, "template"));
      }

      @Nullable
      public Font load(ResourceManager manager) {
         try {
            Resource resource = MinecraftClient.getInstance().getResourceManager().getResource(this.sizes);
            Throwable var3 = null;

            UnicodeTextureFont var5;
            try {
               byte[] bs = new byte[65536];
               resource.getInputStream().read(bs);
               var5 = new UnicodeTextureFont(manager, bs, this.template);
            } catch (Throwable var15) {
               var3 = var15;
               throw var15;
            } finally {
               if (resource != null) {
                  if (var3 != null) {
                     try {
                        resource.close();
                     } catch (Throwable var14) {
                        var3.addSuppressed(var14);
                     }
                  } else {
                     resource.close();
                  }
               }

            }

            return var5;
         } catch (IOException var17) {
            UnicodeTextureFont.LOGGER.error((String)"Cannot load {}, unicode glyphs will not render correctly", (Object)this.sizes);
            return null;
         }
      }
   }
}
