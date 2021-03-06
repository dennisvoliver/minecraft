package net.minecraft;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.bridge.game.GameVersion;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.UUID;
import net.minecraft.util.JsonHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MinecraftVersion implements GameVersion {
   private static final Logger LOGGER = LogManager.getLogger();
   public static final GameVersion field_25319 = new MinecraftVersion();
   private final String id;
   private final String name;
   private final boolean stable;
   private final int worldVersion;
   private final int protocolVersion;
   private final int packVersion;
   private final Date buildTime;
   private final String releaseTarget;

   private MinecraftVersion() {
      this.id = UUID.randomUUID().toString().replaceAll("-", "");
      this.name = "1.16.5";
      this.stable = true;
      this.worldVersion = 2586;
      this.protocolVersion = SharedConstants.method_31372();
      this.packVersion = 6;
      this.buildTime = new Date();
      this.releaseTarget = "1.16.5";
   }

   private MinecraftVersion(JsonObject jsonObject) {
      this.id = JsonHelper.getString(jsonObject, "id");
      this.name = JsonHelper.getString(jsonObject, "name");
      this.releaseTarget = JsonHelper.getString(jsonObject, "release_target");
      this.stable = JsonHelper.getBoolean(jsonObject, "stable");
      this.worldVersion = JsonHelper.getInt(jsonObject, "world_version");
      this.protocolVersion = JsonHelper.getInt(jsonObject, "protocol_version");
      this.packVersion = JsonHelper.getInt(jsonObject, "pack_version");
      this.buildTime = Date.from(ZonedDateTime.parse(JsonHelper.getString(jsonObject, "build_time")).toInstant());
   }

   public static GameVersion create() {
      try {
         InputStream inputStream = MinecraftVersion.class.getResourceAsStream("/version.json");
         Throwable var1 = null;

         Object var4;
         try {
            if (inputStream == null) {
               LOGGER.warn("Missing version information!");
               GameVersion var35 = field_25319;
               return var35;
            }

            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            Throwable var3 = null;

            try {
               var4 = new MinecraftVersion(JsonHelper.deserialize((Reader)inputStreamReader));
            } catch (Throwable var30) {
               var4 = var30;
               var3 = var30;
               throw var30;
            } finally {
               if (inputStreamReader != null) {
                  if (var3 != null) {
                     try {
                        inputStreamReader.close();
                     } catch (Throwable var29) {
                        var3.addSuppressed(var29);
                     }
                  } else {
                     inputStreamReader.close();
                  }
               }

            }
         } catch (Throwable var32) {
            var1 = var32;
            throw var32;
         } finally {
            if (inputStream != null) {
               if (var1 != null) {
                  try {
                     inputStream.close();
                  } catch (Throwable var28) {
                     var1.addSuppressed(var28);
                  }
               } else {
                  inputStream.close();
               }
            }

         }

         return (GameVersion)var4;
      } catch (JsonParseException | IOException var34) {
         throw new IllegalStateException("Game version information is corrupt", var34);
      }
   }

   public String getId() {
      return this.id;
   }

   public String getName() {
      return this.name;
   }

   public String getReleaseTarget() {
      return this.releaseTarget;
   }

   public int getWorldVersion() {
      return this.worldVersion;
   }

   public int getProtocolVersion() {
      return this.protocolVersion;
   }

   public int getPackVersion() {
      return this.packVersion;
   }

   public Date getBuildTime() {
      return this.buildTime;
   }

   public boolean isStable() {
      return this.stable;
   }
}
