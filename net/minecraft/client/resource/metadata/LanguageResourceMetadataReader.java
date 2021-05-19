package net.minecraft.client.resource.metadata;

import com.google.common.collect.Sets;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import java.util.Iterator;
import java.util.Set;
import java.util.Map.Entry;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.resource.language.LanguageDefinition;
import net.minecraft.resource.metadata.ResourceMetadataReader;
import net.minecraft.util.JsonHelper;

@Environment(EnvType.CLIENT)
public class LanguageResourceMetadataReader implements ResourceMetadataReader<LanguageResourceMetadata> {
   public LanguageResourceMetadata fromJson(JsonObject jsonObject) {
      Set<LanguageDefinition> set = Sets.newHashSet();
      Iterator var3 = jsonObject.entrySet().iterator();

      String string;
      String string2;
      String string3;
      boolean bl;
      do {
         if (!var3.hasNext()) {
            return new LanguageResourceMetadata(set);
         }

         Entry<String, JsonElement> entry = (Entry)var3.next();
         string = (String)entry.getKey();
         if (string.length() > 16) {
            throw new JsonParseException("Invalid language->'" + string + "': language code must not be more than " + 16 + " characters long");
         }

         JsonObject jsonObject2 = JsonHelper.asObject((JsonElement)entry.getValue(), "language");
         string2 = JsonHelper.getString(jsonObject2, "region");
         string3 = JsonHelper.getString(jsonObject2, "name");
         bl = JsonHelper.getBoolean(jsonObject2, "bidirectional", false);
         if (string2.isEmpty()) {
            throw new JsonParseException("Invalid language->'" + string + "'->region: empty value");
         }

         if (string3.isEmpty()) {
            throw new JsonParseException("Invalid language->'" + string + "'->name: empty value");
         }
      } while(set.add(new LanguageDefinition(string, string2, string3, bl)));

      throw new JsonParseException("Duplicate language->'" + string + "' defined");
   }

   public String getKey() {
      return "language";
   }
}
