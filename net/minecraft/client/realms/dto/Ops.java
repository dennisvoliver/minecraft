package net.minecraft.client.realms.dto;

import com.google.common.collect.Sets;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.util.Iterator;
import java.util.Set;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class Ops extends ValueObject {
   public Set<String> ops = Sets.newHashSet();

   public static Ops parse(String json) {
      Ops ops = new Ops();
      JsonParser jsonParser = new JsonParser();

      try {
         JsonElement jsonElement = jsonParser.parse(json);
         JsonObject jsonObject = jsonElement.getAsJsonObject();
         JsonElement jsonElement2 = jsonObject.get("ops");
         if (jsonElement2.isJsonArray()) {
            Iterator var6 = jsonElement2.getAsJsonArray().iterator();

            while(var6.hasNext()) {
               JsonElement jsonElement3 = (JsonElement)var6.next();
               ops.ops.add(jsonElement3.getAsString());
            }
         }
      } catch (Exception var8) {
      }

      return ops;
   }
}
