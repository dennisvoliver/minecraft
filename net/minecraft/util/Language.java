package net.minecraft.util;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Map.Entry;
import java.util.function.BiConsumer;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.font.TextVisitFactory;
import net.minecraft.text.OrderedText;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Style;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class Language {
   private static final Logger LOGGER = LogManager.getLogger();
   private static final Gson GSON = new Gson();
   private static final Pattern TOKEN_PATTERN = Pattern.compile("%(\\d+\\$)?[\\d.]*[df]");
   private static volatile Language instance = create();

   private static Language create() {
      Builder<String, String> builder = ImmutableMap.builder();
      BiConsumer biConsumer = builder::put;

      try {
         InputStream inputStream = Language.class.getResourceAsStream("/assets/minecraft/lang/en_us.json");
         Throwable var3 = null;

         try {
            load(inputStream, biConsumer);
         } catch (Throwable var13) {
            var3 = var13;
            throw var13;
         } finally {
            if (inputStream != null) {
               if (var3 != null) {
                  try {
                     inputStream.close();
                  } catch (Throwable var12) {
                     var3.addSuppressed(var12);
                  }
               } else {
                  inputStream.close();
               }
            }

         }
      } catch (JsonParseException | IOException var15) {
         LOGGER.error((String)"Couldn't read strings from /assets/minecraft/lang/en_us.json", (Throwable)var15);
      }

      final Map<String, String> map = builder.build();
      return new Language() {
         public String get(String key) {
            return (String)map.getOrDefault(key, key);
         }

         public boolean hasTranslation(String key) {
            return map.containsKey(key);
         }

         @Environment(EnvType.CLIENT)
         public boolean isRightToLeft() {
            return false;
         }

         @Environment(EnvType.CLIENT)
         public OrderedText reorder(StringVisitable text) {
            return (visitor) -> {
               return text.visit((style, string) -> {
                  return TextVisitFactory.visitFormatted(string, style, visitor) ? Optional.empty() : StringVisitable.TERMINATE_VISIT;
               }, Style.EMPTY).isPresent();
            };
         }
      };
   }

   public static void load(InputStream inputStream, BiConsumer<String, String> entryConsumer) {
      JsonObject jsonObject = (JsonObject)GSON.fromJson((Reader)(new InputStreamReader(inputStream, StandardCharsets.UTF_8)), (Class)JsonObject.class);
      Iterator var3 = jsonObject.entrySet().iterator();

      while(var3.hasNext()) {
         Entry<String, JsonElement> entry = (Entry)var3.next();
         String string = TOKEN_PATTERN.matcher(JsonHelper.asString((JsonElement)entry.getValue(), (String)entry.getKey())).replaceAll("%$1s");
         entryConsumer.accept(entry.getKey(), string);
      }

   }

   public static Language getInstance() {
      return instance;
   }

   @Environment(EnvType.CLIENT)
   public static void setInstance(Language language) {
      instance = language;
   }

   public abstract String get(String key);

   public abstract boolean hasTranslation(String key);

   @Environment(EnvType.CLIENT)
   public abstract boolean isRightToLeft();

   @Environment(EnvType.CLIENT)
   public abstract OrderedText reorder(StringVisitable text);

   @Environment(EnvType.CLIENT)
   public List<OrderedText> reorder(List<StringVisitable> texts) {
      Stream var10000 = texts.stream();
      Language var10001 = getInstance();
      var10001.getClass();
      return (List)var10000.map(var10001::reorder).collect(ImmutableList.toImmutableList());
   }
}
