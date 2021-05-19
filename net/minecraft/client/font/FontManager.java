package net.minecraft.client.font;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceReloadListener;
import net.minecraft.resource.SinglePreparationResourceReloadListener;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.Util;
import net.minecraft.util.profiler.Profiler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Environment(EnvType.CLIENT)
public class FontManager implements AutoCloseable {
   private static final Logger LOGGER = LogManager.getLogger();
   public static final Identifier MISSING_STORAGE_ID = new Identifier("minecraft", "missing");
   private final FontStorage missingStorage;
   private final Map<Identifier, FontStorage> fontStorages = Maps.newHashMap();
   private final TextureManager textureManager;
   private Map<Identifier, Identifier> idOverrides = ImmutableMap.of();
   private final ResourceReloadListener resourceReloadListener = new SinglePreparationResourceReloadListener<Map<Identifier, List<Font>>>() {
      protected Map<Identifier, List<Font>> prepare(ResourceManager resourceManager, Profiler profiler) {
         profiler.startTick();
         Gson gson = (new GsonBuilder()).setPrettyPrinting().disableHtmlEscaping().create();
         Map<Identifier, List<Font>> map = Maps.newHashMap();
         Iterator var5 = resourceManager.findResources("font", (stringx) -> {
            return stringx.endsWith(".json");
         }).iterator();

         while(var5.hasNext()) {
            Identifier identifier = (Identifier)var5.next();
            String string = identifier.getPath();
            Identifier identifier2 = new Identifier(identifier.getNamespace(), string.substring("font/".length(), string.length() - ".json".length()));
            List<Font> list = (List)map.computeIfAbsent(identifier2, (identifierx) -> {
               return Lists.newArrayList((Object[])(new BlankFont()));
            });
            profiler.push(identifier2::toString);

            try {
               for(Iterator var10 = resourceManager.getAllResources(identifier).iterator(); var10.hasNext(); profiler.pop()) {
                  Resource resource = (Resource)var10.next();
                  profiler.push(resource::getResourcePackName);

                  try {
                     InputStream inputStream = resource.getInputStream();
                     Throwable var13 = null;

                     try {
                        Reader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
                        Throwable var15 = null;

                        try {
                           profiler.push("reading");
                           JsonArray jsonArray = JsonHelper.getArray((JsonObject)JsonHelper.deserialize(gson, (Reader)reader, (Class)JsonObject.class), "providers");
                           profiler.swap("parsing");

                           for(int i = jsonArray.size() - 1; i >= 0; --i) {
                              JsonObject jsonObject = JsonHelper.asObject(jsonArray.get(i), "providers[" + i + "]");

                              try {
                                 String string2 = JsonHelper.getString(jsonObject, "type");
                                 FontType fontType = FontType.byId(string2);
                                 profiler.push(string2);
                                 Font font = fontType.createLoader(jsonObject).load(resourceManager);
                                 if (font != null) {
                                    list.add(font);
                                 }

                                 profiler.pop();
                              } catch (RuntimeException var49) {
                                 FontManager.LOGGER.warn((String)"Unable to read definition '{}' in fonts.json in resourcepack: '{}': {}", (Object)identifier2, resource.getResourcePackName(), var49.getMessage());
                              }
                           }

                           profiler.pop();
                        } catch (Throwable var50) {
                           var15 = var50;
                           throw var50;
                        } finally {
                           if (reader != null) {
                              if (var15 != null) {
                                 try {
                                    reader.close();
                                 } catch (Throwable var48) {
                                    var15.addSuppressed(var48);
                                 }
                              } else {
                                 reader.close();
                              }
                           }

                        }
                     } catch (Throwable var52) {
                        var13 = var52;
                        throw var52;
                     } finally {
                        if (inputStream != null) {
                           if (var13 != null) {
                              try {
                                 inputStream.close();
                              } catch (Throwable var47) {
                                 var13.addSuppressed(var47);
                              }
                           } else {
                              inputStream.close();
                           }
                        }

                     }
                  } catch (RuntimeException var54) {
                     FontManager.LOGGER.warn((String)"Unable to load font '{}' in fonts.json in resourcepack: '{}': {}", (Object)identifier2, resource.getResourcePackName(), var54.getMessage());
                  }
               }
            } catch (IOException var55) {
               FontManager.LOGGER.warn((String)"Unable to load font '{}' in fonts.json: {}", (Object)identifier2, (Object)var55.getMessage());
            }

            profiler.push("caching");
            IntSet intSet = new IntOpenHashSet();
            Iterator var57 = list.iterator();

            while(var57.hasNext()) {
               Font font2 = (Font)var57.next();
               intSet.addAll(font2.getProvidedGlyphs());
            }

            intSet.forEach((ix) -> {
               if (ix != 32) {
                  Iterator var2 = Lists.reverse(list).iterator();

                  while(var2.hasNext()) {
                     Font font = (Font)var2.next();
                     if (font.getGlyph(ix) != null) {
                        break;
                     }
                  }

               }
            });
            profiler.pop();
            profiler.pop();
         }

         profiler.endTick();
         return map;
      }

      protected void apply(Map<Identifier, List<Font>> map, ResourceManager resourceManager, Profiler profiler) {
         profiler.startTick();
         profiler.push("closing");
         FontManager.this.fontStorages.values().forEach(FontStorage::close);
         FontManager.this.fontStorages.clear();
         profiler.swap("reloading");
         map.forEach((identifier, list) -> {
            FontStorage fontStorage = new FontStorage(FontManager.this.textureManager, identifier);
            fontStorage.setFonts(Lists.reverse(list));
            FontManager.this.fontStorages.put(identifier, fontStorage);
         });
         profiler.pop();
         profiler.endTick();
      }

      public String getName() {
         return "FontManager";
      }
   };

   public FontManager(TextureManager manager) {
      this.textureManager = manager;
      this.missingStorage = (FontStorage)Util.make(new FontStorage(manager, MISSING_STORAGE_ID), (fontStorage) -> {
         fontStorage.setFonts(Lists.newArrayList((Object[])(new BlankFont())));
      });
   }

   public void setIdOverrides(Map<Identifier, Identifier> overrides) {
      this.idOverrides = overrides;
   }

   public TextRenderer createTextRenderer() {
      return new TextRenderer((identifier) -> {
         return (FontStorage)this.fontStorages.getOrDefault(this.idOverrides.getOrDefault(identifier, identifier), this.missingStorage);
      });
   }

   public ResourceReloadListener getResourceReloadListener() {
      return this.resourceReloadListener;
   }

   public void close() {
      this.fontStorages.values().forEach(FontStorage::close);
      this.missingStorage.close();
   }
}
