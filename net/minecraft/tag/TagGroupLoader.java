package net.minecraft.tag;

import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Function;
import java.util.stream.Collectors;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class TagGroupLoader<T> {
   private static final Logger LOGGER = LogManager.getLogger();
   private static final Gson GSON = new Gson();
   private static final int JSON_EXTENSION_LENGTH = ".json".length();
   private final Function<Identifier, Optional<T>> registryGetter;
   private final String dataType;
   private final String entryType;

   public TagGroupLoader(Function<Identifier, Optional<T>> registryGetter, String dataType, String entryType) {
      this.registryGetter = registryGetter;
      this.dataType = dataType;
      this.entryType = entryType;
   }

   public CompletableFuture<Map<Identifier, Tag.Builder>> prepareReload(ResourceManager manager, Executor prepareExecutor) {
      return CompletableFuture.supplyAsync(() -> {
         Map<Identifier, Tag.Builder> map = Maps.newHashMap();
         Iterator var3 = manager.findResources(this.dataType, (stringx) -> {
            return stringx.endsWith(".json");
         }).iterator();

         while(var3.hasNext()) {
            Identifier identifier = (Identifier)var3.next();
            String string = identifier.getPath();
            Identifier identifier2 = new Identifier(identifier.getNamespace(), string.substring(this.dataType.length() + 1, string.length() - JSON_EXTENSION_LENGTH));

            try {
               Iterator var7 = manager.getAllResources(identifier).iterator();

               while(var7.hasNext()) {
                  Resource resource = (Resource)var7.next();

                  try {
                     InputStream inputStream = resource.getInputStream();
                     Throwable var10 = null;

                     try {
                        Reader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
                        Throwable var12 = null;

                        try {
                           JsonObject jsonObject = (JsonObject)JsonHelper.deserialize(GSON, (Reader)reader, (Class)JsonObject.class);
                           if (jsonObject == null) {
                              LOGGER.error((String)"Couldn't load {} tag list {} from {} in data pack {} as it is empty or null", (Object)this.entryType, identifier2, identifier, resource.getResourcePackName());
                           } else {
                              ((Tag.Builder)map.computeIfAbsent(identifier2, (identifierx) -> {
                                 return Tag.Builder.create();
                              })).read(jsonObject, resource.getResourcePackName());
                           }
                        } catch (Throwable var53) {
                           var12 = var53;
                           throw var53;
                        } finally {
                           if (reader != null) {
                              if (var12 != null) {
                                 try {
                                    reader.close();
                                 } catch (Throwable var52) {
                                    var12.addSuppressed(var52);
                                 }
                              } else {
                                 reader.close();
                              }
                           }

                        }
                     } catch (Throwable var55) {
                        var10 = var55;
                        throw var55;
                     } finally {
                        if (inputStream != null) {
                           if (var10 != null) {
                              try {
                                 inputStream.close();
                              } catch (Throwable var51) {
                                 var10.addSuppressed(var51);
                              }
                           } else {
                              inputStream.close();
                           }
                        }

                     }
                  } catch (RuntimeException | IOException var57) {
                     LOGGER.error((String)"Couldn't read {} tag list {} from {} in data pack {}", (Object)this.entryType, identifier2, identifier, resource.getResourcePackName(), var57);
                  } finally {
                     IOUtils.closeQuietly((Closeable)resource);
                  }
               }
            } catch (IOException var59) {
               LOGGER.error((String)"Couldn't read {} tag list {} from {}", (Object)this.entryType, identifier2, identifier, var59);
            }
         }

         return map;
      }, prepareExecutor);
   }

   public TagGroup<T> applyReload(Map<Identifier, Tag.Builder> tags) {
      Map<Identifier, Tag<T>> map = Maps.newHashMap();
      Function<Identifier, Tag<T>> function = map::get;
      Function function2 = (identifier) -> {
         return ((Optional)this.registryGetter.apply(identifier)).orElse((Object)null);
      };

      while(!tags.isEmpty()) {
         boolean bl = false;
         Iterator iterator = tags.entrySet().iterator();

         while(iterator.hasNext()) {
            Entry<Identifier, Tag.Builder> entry = (Entry)iterator.next();
            Optional<Tag<T>> optional = ((Tag.Builder)entry.getValue()).build(function, function2);
            if (optional.isPresent()) {
               map.put(entry.getKey(), optional.get());
               iterator.remove();
               bl = true;
            }
         }

         if (!bl) {
            break;
         }
      }

      tags.forEach((identifier, builder) -> {
         LOGGER.error((String)"Couldn't load {} tag {} as it is missing following references: {}", (Object)this.entryType, identifier, builder.streamUnresolvedEntries(function, function2).map(Objects::toString).collect(Collectors.joining(",")));
      });
      return TagGroup.create(map);
   }
}
