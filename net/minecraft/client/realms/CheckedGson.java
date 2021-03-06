package net.minecraft.client.realms;

import com.google.gson.Gson;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

/**
 * Checks so that only intended pojos are passed to the GSON (handles
 * serialization after obfuscation).
 */
@Environment(EnvType.CLIENT)
public class CheckedGson {
   private final Gson GSON = new Gson();

   public String toJson(RealmsSerializable serializable) {
      return this.GSON.toJson((Object)serializable);
   }

   public <T extends RealmsSerializable> T fromJson(String json, Class<T> type) {
      return (RealmsSerializable)this.GSON.fromJson(json, type);
   }
}
