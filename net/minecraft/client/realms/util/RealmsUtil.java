package net.minecraft.client.realms.util;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Maps;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.authlib.minecraft.MinecraftProfileTexture.Type;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import com.mojang.util.UUIDTypeAdapter;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;

@Environment(EnvType.CLIENT)
public class RealmsUtil {
   private static final YggdrasilAuthenticationService authenticationService = new YggdrasilAuthenticationService(MinecraftClient.getInstance().getNetworkProxy());
   private static final MinecraftSessionService sessionService;
   public static LoadingCache<String, GameProfile> gameProfileCache;

   public static String uuidToName(String uuid) throws Exception {
      GameProfile gameProfile = (GameProfile)gameProfileCache.get(uuid);
      return gameProfile.getName();
   }

   public static Map<Type, MinecraftProfileTexture> getTextures(String uuid) {
      try {
         GameProfile gameProfile = (GameProfile)gameProfileCache.get(uuid);
         return sessionService.getTextures(gameProfile, false);
      } catch (Exception var2) {
         return Maps.newHashMap();
      }
   }

   public static String convertToAgePresentation(long l) {
      if (l < 0L) {
         return "right now";
      } else {
         long m = l / 1000L;
         if (m < 60L) {
            return (m == 1L ? "1 second" : m + " seconds") + " ago";
         } else {
            long p;
            if (m < 3600L) {
               p = m / 60L;
               return (p == 1L ? "1 minute" : p + " minutes") + " ago";
            } else if (m < 86400L) {
               p = m / 3600L;
               return (p == 1L ? "1 hour" : p + " hours") + " ago";
            } else {
               p = m / 86400L;
               return (p == 1L ? "1 day" : p + " days") + " ago";
            }
         }
      }
   }

   public static String method_25282(Date date) {
      return convertToAgePresentation(System.currentTimeMillis() - date.getTime());
   }

   static {
      sessionService = authenticationService.createMinecraftSessionService();
      gameProfileCache = CacheBuilder.newBuilder().expireAfterWrite(60L, TimeUnit.MINUTES).build(new CacheLoader<String, GameProfile>() {
         public GameProfile load(String string) throws Exception {
            GameProfile gameProfile = RealmsUtil.sessionService.fillProfileProperties(new GameProfile(UUIDTypeAdapter.fromString(string), (String)null), false);
            if (gameProfile == null) {
               throw new Exception("Couldn't get profile");
            } else {
               return gameProfile;
            }
         }
      });
   }
}
