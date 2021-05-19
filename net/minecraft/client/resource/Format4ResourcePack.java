package net.minecraft.client.resource;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.datafixers.util.Pair;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.enums.ChestType;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.resource.ResourcePack;
import net.minecraft.resource.ResourceType;
import net.minecraft.resource.metadata.ResourceMetadataReader;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class Format4ResourcePack implements ResourcePack {
   private static final Map<String, Pair<ChestType, Identifier>> NEW_TO_OLD_CHEST_TEXTURES = (Map)Util.make(Maps.newHashMap(), (hashMap) -> {
      hashMap.put("textures/entity/chest/normal_left.png", new Pair(ChestType.LEFT, new Identifier("textures/entity/chest/normal_double.png")));
      hashMap.put("textures/entity/chest/normal_right.png", new Pair(ChestType.RIGHT, new Identifier("textures/entity/chest/normal_double.png")));
      hashMap.put("textures/entity/chest/normal.png", new Pair(ChestType.SINGLE, new Identifier("textures/entity/chest/normal.png")));
      hashMap.put("textures/entity/chest/trapped_left.png", new Pair(ChestType.LEFT, new Identifier("textures/entity/chest/trapped_double.png")));
      hashMap.put("textures/entity/chest/trapped_right.png", new Pair(ChestType.RIGHT, new Identifier("textures/entity/chest/trapped_double.png")));
      hashMap.put("textures/entity/chest/trapped.png", new Pair(ChestType.SINGLE, new Identifier("textures/entity/chest/trapped.png")));
      hashMap.put("textures/entity/chest/christmas_left.png", new Pair(ChestType.LEFT, new Identifier("textures/entity/chest/christmas_double.png")));
      hashMap.put("textures/entity/chest/christmas_right.png", new Pair(ChestType.RIGHT, new Identifier("textures/entity/chest/christmas_double.png")));
      hashMap.put("textures/entity/chest/christmas.png", new Pair(ChestType.SINGLE, new Identifier("textures/entity/chest/christmas.png")));
      hashMap.put("textures/entity/chest/ender.png", new Pair(ChestType.SINGLE, new Identifier("textures/entity/chest/ender.png")));
   });
   private static final List<String> BANNER_PATTERN_TYPES = Lists.newArrayList((Object[])("base", "border", "bricks", "circle", "creeper", "cross", "curly_border", "diagonal_left", "diagonal_right", "diagonal_up_left", "diagonal_up_right", "flower", "globe", "gradient", "gradient_up", "half_horizontal", "half_horizontal_bottom", "half_vertical", "half_vertical_right", "mojang", "rhombus", "skull", "small_stripes", "square_bottom_left", "square_bottom_right", "square_top_left", "square_top_right", "straight_cross", "stripe_bottom", "stripe_center", "stripe_downleft", "stripe_downright", "stripe_left", "stripe_middle", "stripe_right", "stripe_top", "triangle_bottom", "triangle_top", "triangles_bottom", "triangles_top"));
   private static final Set<String> SHIELD_PATTERN_TEXTURES;
   private static final Set<String> BANNER_PATTERN_TEXTURES;
   public static final Identifier OLD_SHIELD_BASE_TEXTURE;
   public static final Identifier OLD_BANNER_BASE_TEXTURE;
   public static final Identifier IRON_GOLEM_TEXTURE;
   private final ResourcePack parent;

   public Format4ResourcePack(ResourcePack parent) {
      this.parent = parent;
   }

   public InputStream openRoot(String fileName) throws IOException {
      return this.parent.openRoot(fileName);
   }

   public boolean contains(ResourceType type, Identifier id) {
      if (!"minecraft".equals(id.getNamespace())) {
         return this.parent.contains(type, id);
      } else {
         String string = id.getPath();
         if ("textures/misc/enchanted_item_glint.png".equals(string)) {
            return false;
         } else if ("textures/entity/iron_golem/iron_golem.png".equals(string)) {
            return this.parent.contains(type, IRON_GOLEM_TEXTURE);
         } else if (!"textures/entity/conduit/wind.png".equals(string) && !"textures/entity/conduit/wind_vertical.png".equals(string)) {
            if (SHIELD_PATTERN_TEXTURES.contains(string)) {
               return this.parent.contains(type, OLD_SHIELD_BASE_TEXTURE) && this.parent.contains(type, id);
            } else if (!BANNER_PATTERN_TEXTURES.contains(string)) {
               Pair<ChestType, Identifier> pair = (Pair)NEW_TO_OLD_CHEST_TEXTURES.get(string);
               return pair != null && this.parent.contains(type, (Identifier)pair.getSecond()) ? true : this.parent.contains(type, id);
            } else {
               return this.parent.contains(type, OLD_BANNER_BASE_TEXTURE) && this.parent.contains(type, id);
            }
         } else {
            return false;
         }
      }
   }

   public InputStream open(ResourceType type, Identifier id) throws IOException {
      if (!"minecraft".equals(id.getNamespace())) {
         return this.parent.open(type, id);
      } else {
         String string = id.getPath();
         if ("textures/entity/iron_golem/iron_golem.png".equals(string)) {
            return this.parent.open(type, IRON_GOLEM_TEXTURE);
         } else {
            InputStream inputStream2;
            if (SHIELD_PATTERN_TEXTURES.contains(string)) {
               inputStream2 = openCroppedStream(this.parent.open(type, OLD_SHIELD_BASE_TEXTURE), this.parent.open(type, id), 64, 2, 2, 12, 22);
               if (inputStream2 != null) {
                  return inputStream2;
               }
            } else if (BANNER_PATTERN_TEXTURES.contains(string)) {
               inputStream2 = openCroppedStream(this.parent.open(type, OLD_BANNER_BASE_TEXTURE), this.parent.open(type, id), 64, 0, 0, 42, 41);
               if (inputStream2 != null) {
                  return inputStream2;
               }
            } else {
               if (!"textures/entity/enderdragon/dragon.png".equals(string) && !"textures/entity/enderdragon/dragon_exploding.png".equals(string)) {
                  if (!"textures/entity/conduit/closed_eye.png".equals(string) && !"textures/entity/conduit/open_eye.png".equals(string)) {
                     Pair<ChestType, Identifier> pair = (Pair)NEW_TO_OLD_CHEST_TEXTURES.get(string);
                     if (pair != null) {
                        ChestType chestType = (ChestType)pair.getFirst();
                        InputStream inputStream3 = this.parent.open(type, (Identifier)pair.getSecond());
                        if (chestType == ChestType.SINGLE) {
                           return cropSingleChestTexture(inputStream3);
                        }

                        if (chestType == ChestType.LEFT) {
                           return cropLeftChestTexture(inputStream3);
                        }

                        if (chestType == ChestType.RIGHT) {
                           return cropRightChestTexture(inputStream3);
                        }
                     }

                     return this.parent.open(type, id);
                  }

                  return method_24199(this.parent.open(type, id));
               }

               NativeImage nativeImage = NativeImage.read(this.parent.open(type, id));
               Throwable var5 = null;

               try {
                  int i = nativeImage.getWidth() / 256;

                  for(int j = 88 * i; j < 200 * i; ++j) {
                     for(int k = 56 * i; k < 112 * i; ++k) {
                        nativeImage.setPixelColor(k, j, 0);
                     }
                  }

                  ByteArrayInputStream var22 = new ByteArrayInputStream(nativeImage.getBytes());
                  return var22;
               } catch (Throwable var16) {
                  var5 = var16;
                  throw var16;
               } finally {
                  if (nativeImage != null) {
                     if (var5 != null) {
                        try {
                           nativeImage.close();
                        } catch (Throwable var15) {
                           var5.addSuppressed(var15);
                        }
                     } else {
                        nativeImage.close();
                     }
                  }

               }
            }

            return this.parent.open(type, id);
         }
      }
   }

   @Nullable
   public static InputStream openCroppedStream(InputStream inputStream, InputStream inputStream2, int i, int j, int k, int l, int m) throws IOException {
      NativeImage nativeImage = NativeImage.read(inputStream);
      Throwable var8 = null;

      try {
         NativeImage nativeImage2 = NativeImage.read(inputStream2);
         Throwable var10 = null;

         try {
            int n = nativeImage.getWidth();
            int o = nativeImage.getHeight();
            if (n != nativeImage2.getWidth() || o != nativeImage2.getHeight()) {
               return null;
            } else {
               NativeImage nativeImage3 = new NativeImage(n, o, true);
               Throwable var14 = null;

               try {
                  int p = n / i;

                  for(int q = k * p; q < m * p; ++q) {
                     for(int r = j * p; r < l * p; ++r) {
                        int s = NativeImage.getRed(nativeImage2.getPixelColor(r, q));
                        int t = nativeImage.getPixelColor(r, q);
                        nativeImage3.setPixelColor(r, q, NativeImage.getAbgrColor(s, NativeImage.getBlue(t), NativeImage.getGreen(t), NativeImage.getRed(t)));
                     }
                  }

                  ByteArrayInputStream var71 = new ByteArrayInputStream(nativeImage3.getBytes());
                  return var71;
               } catch (Throwable var65) {
                  var14 = var65;
                  throw var65;
               } finally {
                  if (nativeImage3 != null) {
                     if (var14 != null) {
                        try {
                           nativeImage3.close();
                        } catch (Throwable var64) {
                           var14.addSuppressed(var64);
                        }
                     } else {
                        nativeImage3.close();
                     }
                  }

               }
            }
         } catch (Throwable var67) {
            var10 = var67;
            throw var67;
         } finally {
            if (nativeImage2 != null) {
               if (var10 != null) {
                  try {
                     nativeImage2.close();
                  } catch (Throwable var63) {
                     var10.addSuppressed(var63);
                  }
               } else {
                  nativeImage2.close();
               }
            }

         }
      } catch (Throwable var69) {
         var8 = var69;
         throw var69;
      } finally {
         if (nativeImage != null) {
            if (var8 != null) {
               try {
                  nativeImage.close();
               } catch (Throwable var62) {
                  var8.addSuppressed(var62);
               }
            } else {
               nativeImage.close();
            }
         }

      }
   }

   public static InputStream method_24199(InputStream inputStream) throws IOException {
      NativeImage nativeImage = NativeImage.read(inputStream);
      Throwable var2 = null;

      Object var7;
      try {
         int i = nativeImage.getWidth();
         int j = nativeImage.getHeight();
         NativeImage nativeImage2 = new NativeImage(2 * i, 2 * j, true);
         Throwable var6 = null;

         try {
            loadBytes(nativeImage, nativeImage2, 0, 0, 0, 0, i, j, 1, false, false);
            var7 = new ByteArrayInputStream(nativeImage2.getBytes());
         } catch (Throwable var30) {
            var7 = var30;
            var6 = var30;
            throw var30;
         } finally {
            if (nativeImage2 != null) {
               if (var6 != null) {
                  try {
                     nativeImage2.close();
                  } catch (Throwable var29) {
                     var6.addSuppressed(var29);
                  }
               } else {
                  nativeImage2.close();
               }
            }

         }
      } catch (Throwable var32) {
         var2 = var32;
         throw var32;
      } finally {
         if (nativeImage != null) {
            if (var2 != null) {
               try {
                  nativeImage.close();
               } catch (Throwable var28) {
                  var2.addSuppressed(var28);
               }
            } else {
               nativeImage.close();
            }
         }

      }

      return (InputStream)var7;
   }

   public static InputStream cropLeftChestTexture(InputStream inputStream) throws IOException {
      NativeImage nativeImage = NativeImage.read(inputStream);
      Throwable var2 = null;

      ByteArrayInputStream var8;
      try {
         int i = nativeImage.getWidth();
         int j = nativeImage.getHeight();
         NativeImage nativeImage2 = new NativeImage(i / 2, j, true);
         Throwable var6 = null;

         try {
            int k = j / 64;
            loadBytes(nativeImage, nativeImage2, 29, 0, 29, 0, 15, 14, k, false, true);
            loadBytes(nativeImage, nativeImage2, 59, 0, 14, 0, 15, 14, k, false, true);
            loadBytes(nativeImage, nativeImage2, 29, 14, 43, 14, 15, 5, k, true, true);
            loadBytes(nativeImage, nativeImage2, 44, 14, 29, 14, 14, 5, k, true, true);
            loadBytes(nativeImage, nativeImage2, 58, 14, 14, 14, 15, 5, k, true, true);
            loadBytes(nativeImage, nativeImage2, 29, 19, 29, 19, 15, 14, k, false, true);
            loadBytes(nativeImage, nativeImage2, 59, 19, 14, 19, 15, 14, k, false, true);
            loadBytes(nativeImage, nativeImage2, 29, 33, 43, 33, 15, 10, k, true, true);
            loadBytes(nativeImage, nativeImage2, 44, 33, 29, 33, 14, 10, k, true, true);
            loadBytes(nativeImage, nativeImage2, 58, 33, 14, 33, 15, 10, k, true, true);
            loadBytes(nativeImage, nativeImage2, 2, 0, 2, 0, 1, 1, k, false, true);
            loadBytes(nativeImage, nativeImage2, 4, 0, 1, 0, 1, 1, k, false, true);
            loadBytes(nativeImage, nativeImage2, 2, 1, 3, 1, 1, 4, k, true, true);
            loadBytes(nativeImage, nativeImage2, 3, 1, 2, 1, 1, 4, k, true, true);
            loadBytes(nativeImage, nativeImage2, 4, 1, 1, 1, 1, 4, k, true, true);
            var8 = new ByteArrayInputStream(nativeImage2.getBytes());
         } catch (Throwable var31) {
            var6 = var31;
            throw var31;
         } finally {
            if (nativeImage2 != null) {
               if (var6 != null) {
                  try {
                     nativeImage2.close();
                  } catch (Throwable var30) {
                     var6.addSuppressed(var30);
                  }
               } else {
                  nativeImage2.close();
               }
            }

         }
      } catch (Throwable var33) {
         var2 = var33;
         throw var33;
      } finally {
         if (nativeImage != null) {
            if (var2 != null) {
               try {
                  nativeImage.close();
               } catch (Throwable var29) {
                  var2.addSuppressed(var29);
               }
            } else {
               nativeImage.close();
            }
         }

      }

      return var8;
   }

   public static InputStream cropRightChestTexture(InputStream inputStream) throws IOException {
      NativeImage nativeImage = NativeImage.read(inputStream);
      Throwable var2 = null;

      ByteArrayInputStream var8;
      try {
         int i = nativeImage.getWidth();
         int j = nativeImage.getHeight();
         NativeImage nativeImage2 = new NativeImage(i / 2, j, true);
         Throwable var6 = null;

         try {
            int k = j / 64;
            loadBytes(nativeImage, nativeImage2, 14, 0, 29, 0, 15, 14, k, false, true);
            loadBytes(nativeImage, nativeImage2, 44, 0, 14, 0, 15, 14, k, false, true);
            loadBytes(nativeImage, nativeImage2, 0, 14, 0, 14, 14, 5, k, true, true);
            loadBytes(nativeImage, nativeImage2, 14, 14, 43, 14, 15, 5, k, true, true);
            loadBytes(nativeImage, nativeImage2, 73, 14, 14, 14, 15, 5, k, true, true);
            loadBytes(nativeImage, nativeImage2, 14, 19, 29, 19, 15, 14, k, false, true);
            loadBytes(nativeImage, nativeImage2, 44, 19, 14, 19, 15, 14, k, false, true);
            loadBytes(nativeImage, nativeImage2, 0, 33, 0, 33, 14, 10, k, true, true);
            loadBytes(nativeImage, nativeImage2, 14, 33, 43, 33, 15, 10, k, true, true);
            loadBytes(nativeImage, nativeImage2, 73, 33, 14, 33, 15, 10, k, true, true);
            loadBytes(nativeImage, nativeImage2, 1, 0, 2, 0, 1, 1, k, false, true);
            loadBytes(nativeImage, nativeImage2, 3, 0, 1, 0, 1, 1, k, false, true);
            loadBytes(nativeImage, nativeImage2, 0, 1, 0, 1, 1, 4, k, true, true);
            loadBytes(nativeImage, nativeImage2, 1, 1, 3, 1, 1, 4, k, true, true);
            loadBytes(nativeImage, nativeImage2, 5, 1, 1, 1, 1, 4, k, true, true);
            var8 = new ByteArrayInputStream(nativeImage2.getBytes());
         } catch (Throwable var31) {
            var6 = var31;
            throw var31;
         } finally {
            if (nativeImage2 != null) {
               if (var6 != null) {
                  try {
                     nativeImage2.close();
                  } catch (Throwable var30) {
                     var6.addSuppressed(var30);
                  }
               } else {
                  nativeImage2.close();
               }
            }

         }
      } catch (Throwable var33) {
         var2 = var33;
         throw var33;
      } finally {
         if (nativeImage != null) {
            if (var2 != null) {
               try {
                  nativeImage.close();
               } catch (Throwable var29) {
                  var2.addSuppressed(var29);
               }
            } else {
               nativeImage.close();
            }
         }

      }

      return var8;
   }

   public static InputStream cropSingleChestTexture(InputStream inputStream) throws IOException {
      NativeImage nativeImage = NativeImage.read(inputStream);
      Throwable var2 = null;

      ByteArrayInputStream var8;
      try {
         int i = nativeImage.getWidth();
         int j = nativeImage.getHeight();
         NativeImage nativeImage2 = new NativeImage(i, j, true);
         Throwable var6 = null;

         try {
            int k = j / 64;
            loadBytes(nativeImage, nativeImage2, 14, 0, 28, 0, 14, 14, k, false, true);
            loadBytes(nativeImage, nativeImage2, 28, 0, 14, 0, 14, 14, k, false, true);
            loadBytes(nativeImage, nativeImage2, 0, 14, 0, 14, 14, 5, k, true, true);
            loadBytes(nativeImage, nativeImage2, 14, 14, 42, 14, 14, 5, k, true, true);
            loadBytes(nativeImage, nativeImage2, 28, 14, 28, 14, 14, 5, k, true, true);
            loadBytes(nativeImage, nativeImage2, 42, 14, 14, 14, 14, 5, k, true, true);
            loadBytes(nativeImage, nativeImage2, 14, 19, 28, 19, 14, 14, k, false, true);
            loadBytes(nativeImage, nativeImage2, 28, 19, 14, 19, 14, 14, k, false, true);
            loadBytes(nativeImage, nativeImage2, 0, 33, 0, 33, 14, 10, k, true, true);
            loadBytes(nativeImage, nativeImage2, 14, 33, 42, 33, 14, 10, k, true, true);
            loadBytes(nativeImage, nativeImage2, 28, 33, 28, 33, 14, 10, k, true, true);
            loadBytes(nativeImage, nativeImage2, 42, 33, 14, 33, 14, 10, k, true, true);
            loadBytes(nativeImage, nativeImage2, 1, 0, 3, 0, 2, 1, k, false, true);
            loadBytes(nativeImage, nativeImage2, 3, 0, 1, 0, 2, 1, k, false, true);
            loadBytes(nativeImage, nativeImage2, 0, 1, 0, 1, 1, 4, k, true, true);
            loadBytes(nativeImage, nativeImage2, 1, 1, 4, 1, 2, 4, k, true, true);
            loadBytes(nativeImage, nativeImage2, 3, 1, 3, 1, 1, 4, k, true, true);
            loadBytes(nativeImage, nativeImage2, 4, 1, 1, 1, 2, 4, k, true, true);
            var8 = new ByteArrayInputStream(nativeImage2.getBytes());
         } catch (Throwable var31) {
            var6 = var31;
            throw var31;
         } finally {
            if (nativeImage2 != null) {
               if (var6 != null) {
                  try {
                     nativeImage2.close();
                  } catch (Throwable var30) {
                     var6.addSuppressed(var30);
                  }
               } else {
                  nativeImage2.close();
               }
            }

         }
      } catch (Throwable var33) {
         var2 = var33;
         throw var33;
      } finally {
         if (nativeImage != null) {
            if (var2 != null) {
               try {
                  nativeImage.close();
               } catch (Throwable var29) {
                  var2.addSuppressed(var29);
               }
            } else {
               nativeImage.close();
            }
         }

      }

      return var8;
   }

   public Collection<Identifier> findResources(ResourceType type, String namespace, String prefix, int maxDepth, Predicate<String> pathFilter) {
      return this.parent.findResources(type, namespace, prefix, maxDepth, pathFilter);
   }

   public Set<String> getNamespaces(ResourceType type) {
      return this.parent.getNamespaces(type);
   }

   @Nullable
   public <T> T parseMetadata(ResourceMetadataReader<T> metaReader) throws IOException {
      return this.parent.parseMetadata(metaReader);
   }

   public String getName() {
      return this.parent.getName();
   }

   public void close() {
      this.parent.close();
   }

   private static void loadBytes(NativeImage source, NativeImage target, int i, int j, int k, int l, int m, int n, int o, boolean bl, boolean bl2) {
      n *= o;
      m *= o;
      k *= o;
      l *= o;
      i *= o;
      j *= o;

      for(int p = 0; p < n; ++p) {
         for(int q = 0; q < m; ++q) {
            target.setPixelColor(k + q, l + p, source.getPixelColor(i + (bl ? m - 1 - q : q), j + (bl2 ? n - 1 - p : p)));
         }
      }

   }

   static {
      SHIELD_PATTERN_TEXTURES = (Set)BANNER_PATTERN_TYPES.stream().map((string) -> {
         return "textures/entity/shield/" + string + ".png";
      }).collect(Collectors.toSet());
      BANNER_PATTERN_TEXTURES = (Set)BANNER_PATTERN_TYPES.stream().map((string) -> {
         return "textures/entity/banner/" + string + ".png";
      }).collect(Collectors.toSet());
      OLD_SHIELD_BASE_TEXTURE = new Identifier("textures/entity/shield_base.png");
      OLD_BANNER_BASE_TEXTURE = new Identifier("textures/entity/banner_base.png");
      IRON_GOLEM_TEXTURE = new Identifier("textures/entity/iron_golem.png");
   }
}
