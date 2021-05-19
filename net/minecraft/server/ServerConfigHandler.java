package net.minecraft.server;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.io.Files;
import com.mojang.authlib.Agent;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.ProfileLookupCallback;
import com.mojang.authlib.yggdrasil.ProfileNotFoundException;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.dedicated.MinecraftDedicatedServer;
import net.minecraft.util.ChatUtil;
import net.minecraft.util.WorldSavePath;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

public class ServerConfigHandler {
   private static final Logger LOGGER = LogManager.getLogger();
   public static final File BANNED_IPS_FILE = new File("banned-ips.txt");
   public static final File BANNED_PLAYERS_FILE = new File("banned-players.txt");
   public static final File OPERATORS_FILE = new File("ops.txt");
   public static final File WHITE_LIST_FILE = new File("white-list.txt");

   static List<String> processSimpleListFile(File file, Map<String, String[]> valueMap) throws IOException {
      List<String> list = Files.readLines(file, StandardCharsets.UTF_8);
      Iterator var3 = list.iterator();

      while(var3.hasNext()) {
         String string = (String)var3.next();
         string = string.trim();
         if (!string.startsWith("#") && string.length() >= 1) {
            String[] strings = string.split("\\|");
            valueMap.put(strings[0].toLowerCase(Locale.ROOT), strings);
         }
      }

      return list;
   }

   private static void lookupProfile(MinecraftServer server, Collection<String> bannedPlayers, ProfileLookupCallback callback) {
      String[] strings = (String[])bannedPlayers.stream().filter((stringx) -> {
         return !ChatUtil.isEmpty(stringx);
      }).toArray((i) -> {
         return new String[i];
      });
      if (server.isOnlineMode()) {
         server.getGameProfileRepo().findProfilesByNames(strings, Agent.MINECRAFT, callback);
      } else {
         String[] var4 = strings;
         int var5 = strings.length;

         for(int var6 = 0; var6 < var5; ++var6) {
            String string = var4[var6];
            UUID uUID = PlayerEntity.getUuidFromProfile(new GameProfile((UUID)null, string));
            GameProfile gameProfile = new GameProfile(uUID, string);
            callback.onProfileLookupSucceeded(gameProfile);
         }
      }

   }

   public static boolean convertBannedPlayers(final MinecraftServer server) {
      final BannedPlayerList bannedPlayerList = new BannedPlayerList(PlayerManager.BANNED_PLAYERS_FILE);
      if (BANNED_PLAYERS_FILE.exists() && BANNED_PLAYERS_FILE.isFile()) {
         if (bannedPlayerList.getFile().exists()) {
            try {
               bannedPlayerList.load();
            } catch (IOException var6) {
               LOGGER.warn((String)"Could not load existing file {}", (Object)bannedPlayerList.getFile().getName(), (Object)var6);
            }
         }

         try {
            final Map<String, String[]> map = Maps.newHashMap();
            processSimpleListFile(BANNED_PLAYERS_FILE, map);
            ProfileLookupCallback profileLookupCallback = new ProfileLookupCallback() {
               public void onProfileLookupSucceeded(GameProfile profile) {
                  server.getUserCache().add(profile);
                  String[] strings = (String[])map.get(profile.getName().toLowerCase(Locale.ROOT));
                  if (strings == null) {
                     ServerConfigHandler.LOGGER.warn((String)"Could not convert user banlist entry for {}", (Object)profile.getName());
                     throw new ServerConfigHandler.ServerConfigException("Profile not in the conversionlist");
                  } else {
                     Date date = strings.length > 1 ? ServerConfigHandler.parseDate(strings[1], (Date)null) : null;
                     String string = strings.length > 2 ? strings[2] : null;
                     Date date2 = strings.length > 3 ? ServerConfigHandler.parseDate(strings[3], (Date)null) : null;
                     String string2 = strings.length > 4 ? strings[4] : null;
                     bannedPlayerList.add(new BannedPlayerEntry(profile, date, string, date2, string2));
                  }
               }

               public void onProfileLookupFailed(GameProfile profile, Exception exception) {
                  ServerConfigHandler.LOGGER.warn((String)"Could not lookup user banlist entry for {}", (Object)profile.getName(), (Object)exception);
                  if (!(exception instanceof ProfileNotFoundException)) {
                     throw new ServerConfigHandler.ServerConfigException("Could not request user " + profile.getName() + " from backend systems", exception);
                  }
               }
            };
            lookupProfile(server, map.keySet(), profileLookupCallback);
            bannedPlayerList.save();
            markFileConverted(BANNED_PLAYERS_FILE);
            return true;
         } catch (IOException var4) {
            LOGGER.warn((String)"Could not read old user banlist to convert it!", (Throwable)var4);
            return false;
         } catch (ServerConfigHandler.ServerConfigException var5) {
            LOGGER.error((String)"Conversion failed, please try again later", (Throwable)var5);
            return false;
         }
      } else {
         return true;
      }
   }

   public static boolean convertBannedIps(MinecraftServer server) {
      BannedIpList bannedIpList = new BannedIpList(PlayerManager.BANNED_IPS_FILE);
      if (BANNED_IPS_FILE.exists() && BANNED_IPS_FILE.isFile()) {
         if (bannedIpList.getFile().exists()) {
            try {
               bannedIpList.load();
            } catch (IOException var11) {
               LOGGER.warn((String)"Could not load existing file {}", (Object)bannedIpList.getFile().getName(), (Object)var11);
            }
         }

         try {
            Map<String, String[]> map = Maps.newHashMap();
            processSimpleListFile(BANNED_IPS_FILE, map);
            Iterator var3 = map.keySet().iterator();

            while(var3.hasNext()) {
               String string = (String)var3.next();
               String[] strings = (String[])map.get(string);
               Date date = strings.length > 1 ? parseDate(strings[1], (Date)null) : null;
               String string2 = strings.length > 2 ? strings[2] : null;
               Date date2 = strings.length > 3 ? parseDate(strings[3], (Date)null) : null;
               String string3 = strings.length > 4 ? strings[4] : null;
               bannedIpList.add(new BannedIpEntry(string, date, string2, date2, string3));
            }

            bannedIpList.save();
            markFileConverted(BANNED_IPS_FILE);
            return true;
         } catch (IOException var10) {
            LOGGER.warn((String)"Could not parse old ip banlist to convert it!", (Throwable)var10);
            return false;
         }
      } else {
         return true;
      }
   }

   public static boolean convertOperators(final MinecraftServer server) {
      final OperatorList operatorList = new OperatorList(PlayerManager.OPERATORS_FILE);
      if (OPERATORS_FILE.exists() && OPERATORS_FILE.isFile()) {
         if (operatorList.getFile().exists()) {
            try {
               operatorList.load();
            } catch (IOException var6) {
               LOGGER.warn((String)"Could not load existing file {}", (Object)operatorList.getFile().getName(), (Object)var6);
            }
         }

         try {
            List<String> list = Files.readLines(OPERATORS_FILE, StandardCharsets.UTF_8);
            ProfileLookupCallback profileLookupCallback = new ProfileLookupCallback() {
               public void onProfileLookupSucceeded(GameProfile profile) {
                  server.getUserCache().add(profile);
                  operatorList.add(new OperatorEntry(profile, server.getOpPermissionLevel(), false));
               }

               public void onProfileLookupFailed(GameProfile profile, Exception exception) {
                  ServerConfigHandler.LOGGER.warn((String)"Could not lookup oplist entry for {}", (Object)profile.getName(), (Object)exception);
                  if (!(exception instanceof ProfileNotFoundException)) {
                     throw new ServerConfigHandler.ServerConfigException("Could not request user " + profile.getName() + " from backend systems", exception);
                  }
               }
            };
            lookupProfile(server, list, profileLookupCallback);
            operatorList.save();
            markFileConverted(OPERATORS_FILE);
            return true;
         } catch (IOException var4) {
            LOGGER.warn((String)"Could not read old oplist to convert it!", (Throwable)var4);
            return false;
         } catch (ServerConfigHandler.ServerConfigException var5) {
            LOGGER.error((String)"Conversion failed, please try again later", (Throwable)var5);
            return false;
         }
      } else {
         return true;
      }
   }

   public static boolean convertWhitelist(final MinecraftServer server) {
      final Whitelist whitelist = new Whitelist(PlayerManager.WHITELIST_FILE);
      if (WHITE_LIST_FILE.exists() && WHITE_LIST_FILE.isFile()) {
         if (whitelist.getFile().exists()) {
            try {
               whitelist.load();
            } catch (IOException var6) {
               LOGGER.warn((String)"Could not load existing file {}", (Object)whitelist.getFile().getName(), (Object)var6);
            }
         }

         try {
            List<String> list = Files.readLines(WHITE_LIST_FILE, StandardCharsets.UTF_8);
            ProfileLookupCallback profileLookupCallback = new ProfileLookupCallback() {
               public void onProfileLookupSucceeded(GameProfile profile) {
                  server.getUserCache().add(profile);
                  whitelist.add(new WhitelistEntry(profile));
               }

               public void onProfileLookupFailed(GameProfile profile, Exception exception) {
                  ServerConfigHandler.LOGGER.warn((String)"Could not lookup user whitelist entry for {}", (Object)profile.getName(), (Object)exception);
                  if (!(exception instanceof ProfileNotFoundException)) {
                     throw new ServerConfigHandler.ServerConfigException("Could not request user " + profile.getName() + " from backend systems", exception);
                  }
               }
            };
            lookupProfile(server, list, profileLookupCallback);
            whitelist.save();
            markFileConverted(WHITE_LIST_FILE);
            return true;
         } catch (IOException var4) {
            LOGGER.warn((String)"Could not read old whitelist to convert it!", (Throwable)var4);
            return false;
         } catch (ServerConfigHandler.ServerConfigException var5) {
            LOGGER.error((String)"Conversion failed, please try again later", (Throwable)var5);
            return false;
         }
      } else {
         return true;
      }
   }

   @Nullable
   public static UUID getPlayerUuidByName(final MinecraftServer server, String name) {
      if (!ChatUtil.isEmpty(name) && name.length() <= 16) {
         GameProfile gameProfile = server.getUserCache().findByName(name);
         if (gameProfile != null && gameProfile.getId() != null) {
            return gameProfile.getId();
         } else if (!server.isSinglePlayer() && server.isOnlineMode()) {
            final List<GameProfile> list = Lists.newArrayList();
            ProfileLookupCallback profileLookupCallback = new ProfileLookupCallback() {
               public void onProfileLookupSucceeded(GameProfile profile) {
                  server.getUserCache().add(profile);
                  list.add(profile);
               }

               public void onProfileLookupFailed(GameProfile profile, Exception exception) {
                  ServerConfigHandler.LOGGER.warn((String)"Could not lookup user whitelist entry for {}", (Object)profile.getName(), (Object)exception);
               }
            };
            lookupProfile(server, Lists.newArrayList((Object[])(name)), profileLookupCallback);
            return !list.isEmpty() && ((GameProfile)list.get(0)).getId() != null ? ((GameProfile)list.get(0)).getId() : null;
         } else {
            return PlayerEntity.getUuidFromProfile(new GameProfile((UUID)null, name));
         }
      } else {
         try {
            return UUID.fromString(name);
         } catch (IllegalArgumentException var5) {
            return null;
         }
      }
   }

   public static boolean convertPlayerFiles(final MinecraftDedicatedServer minecraftServer) {
      final File file = getLevelPlayersFolder(minecraftServer);
      final File file2 = new File(file.getParentFile(), "playerdata");
      final File file3 = new File(file.getParentFile(), "unknownplayers");
      if (file.exists() && file.isDirectory()) {
         File[] files = file.listFiles();
         List<String> list = Lists.newArrayList();
         File[] var6 = files;
         int var7 = files.length;

         for(int var8 = 0; var8 < var7; ++var8) {
            File file4 = var6[var8];
            String string = file4.getName();
            if (string.toLowerCase(Locale.ROOT).endsWith(".dat")) {
               String string2 = string.substring(0, string.length() - ".dat".length());
               if (!string2.isEmpty()) {
                  list.add(string2);
               }
            }
         }

         try {
            final String[] strings = (String[])list.toArray(new String[list.size()]);
            ProfileLookupCallback profileLookupCallback = new ProfileLookupCallback() {
               public void onProfileLookupSucceeded(GameProfile profile) {
                  minecraftServer.getUserCache().add(profile);
                  UUID uUID = profile.getId();
                  if (uUID == null) {
                     throw new ServerConfigHandler.ServerConfigException("Missing UUID for user profile " + profile.getName());
                  } else {
                     this.convertPlayerFile(file2, this.getPlayerFileName(profile), uUID.toString());
                  }
               }

               public void onProfileLookupFailed(GameProfile profile, Exception exception) {
                  ServerConfigHandler.LOGGER.warn((String)"Could not lookup user uuid for {}", (Object)profile.getName(), (Object)exception);
                  if (exception instanceof ProfileNotFoundException) {
                     String string = this.getPlayerFileName(profile);
                     this.convertPlayerFile(file3, string, string);
                  } else {
                     throw new ServerConfigHandler.ServerConfigException("Could not request user " + profile.getName() + " from backend systems", exception);
                  }
               }

               private void convertPlayerFile(File playerDataFolder, String fileName, String uuid) {
                  File filex = new File(file, fileName + ".dat");
                  File file2x = new File(playerDataFolder, uuid + ".dat");
                  ServerConfigHandler.createDirectory(playerDataFolder);
                  if (!filex.renameTo(file2x)) {
                     throw new ServerConfigHandler.ServerConfigException("Could not convert file for " + fileName);
                  }
               }

               private String getPlayerFileName(GameProfile profile) {
                  String string = null;
                  String[] var3 = strings;
                  int var4 = var3.length;

                  for(int var5 = 0; var5 < var4; ++var5) {
                     String string2 = var3[var5];
                     if (string2 != null && string2.equalsIgnoreCase(profile.getName())) {
                        string = string2;
                        break;
                     }
                  }

                  if (string == null) {
                     throw new ServerConfigHandler.ServerConfigException("Could not find the filename for " + profile.getName() + " anymore");
                  } else {
                     return string;
                  }
               }
            };
            lookupProfile(minecraftServer, Lists.newArrayList((Object[])strings), profileLookupCallback);
            return true;
         } catch (ServerConfigHandler.ServerConfigException var12) {
            LOGGER.error((String)"Conversion failed, please try again later", (Throwable)var12);
            return false;
         }
      } else {
         return true;
      }
   }

   private static void createDirectory(File directory) {
      if (directory.exists()) {
         if (!directory.isDirectory()) {
            throw new ServerConfigHandler.ServerConfigException("Can't create directory " + directory.getName() + " in world save directory.");
         }
      } else if (!directory.mkdirs()) {
         throw new ServerConfigHandler.ServerConfigException("Can't create directory " + directory.getName() + " in world save directory.");
      }
   }

   public static boolean checkSuccess(MinecraftServer server) {
      boolean bl = checkListConversionSuccess();
      bl = bl && checkPlayerConversionSuccess(server);
      return bl;
   }

   private static boolean checkListConversionSuccess() {
      boolean bl = false;
      if (BANNED_PLAYERS_FILE.exists() && BANNED_PLAYERS_FILE.isFile()) {
         bl = true;
      }

      boolean bl2 = false;
      if (BANNED_IPS_FILE.exists() && BANNED_IPS_FILE.isFile()) {
         bl2 = true;
      }

      boolean bl3 = false;
      if (OPERATORS_FILE.exists() && OPERATORS_FILE.isFile()) {
         bl3 = true;
      }

      boolean bl4 = false;
      if (WHITE_LIST_FILE.exists() && WHITE_LIST_FILE.isFile()) {
         bl4 = true;
      }

      if (!bl && !bl2 && !bl3 && !bl4) {
         return true;
      } else {
         LOGGER.warn("**** FAILED TO START THE SERVER AFTER ACCOUNT CONVERSION!");
         LOGGER.warn("** please remove the following files and restart the server:");
         if (bl) {
            LOGGER.warn((String)"* {}", (Object)BANNED_PLAYERS_FILE.getName());
         }

         if (bl2) {
            LOGGER.warn((String)"* {}", (Object)BANNED_IPS_FILE.getName());
         }

         if (bl3) {
            LOGGER.warn((String)"* {}", (Object)OPERATORS_FILE.getName());
         }

         if (bl4) {
            LOGGER.warn((String)"* {}", (Object)WHITE_LIST_FILE.getName());
         }

         return false;
      }
   }

   private static boolean checkPlayerConversionSuccess(MinecraftServer server) {
      File file = getLevelPlayersFolder(server);
      if (!file.exists() || !file.isDirectory() || file.list().length <= 0 && file.delete()) {
         return true;
      } else {
         LOGGER.warn("**** DETECTED OLD PLAYER DIRECTORY IN THE WORLD SAVE");
         LOGGER.warn("**** THIS USUALLY HAPPENS WHEN THE AUTOMATIC CONVERSION FAILED IN SOME WAY");
         LOGGER.warn((String)"** please restart the server and if the problem persists, remove the directory '{}'", (Object)file.getPath());
         return false;
      }
   }

   private static File getLevelPlayersFolder(MinecraftServer server) {
      return server.getSavePath(WorldSavePath.PLAYERS).toFile();
   }

   private static void markFileConverted(File file) {
      File file2 = new File(file.getName() + ".converted");
      file.renameTo(file2);
   }

   private static Date parseDate(String dateString, Date fallback) {
      Date date2;
      try {
         date2 = BanEntry.DATE_FORMAT.parse(dateString);
      } catch (ParseException var4) {
         date2 = fallback;
      }

      return date2;
   }

   static class ServerConfigException extends RuntimeException {
      private ServerConfigException(String title, Throwable other) {
         super(title, other);
      }

      private ServerConfigException(String title) {
         super(title);
      }
   }
}
