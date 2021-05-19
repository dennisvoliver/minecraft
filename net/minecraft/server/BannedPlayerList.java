package net.minecraft.server;

import com.google.gson.JsonObject;
import com.mojang.authlib.GameProfile;
import java.io.File;
import java.util.Iterator;

public class BannedPlayerList extends ServerConfigList<GameProfile, BannedPlayerEntry> {
   public BannedPlayerList(File file) {
      super(file);
   }

   protected ServerConfigEntry<GameProfile> fromJson(JsonObject json) {
      return new BannedPlayerEntry(json);
   }

   public boolean contains(GameProfile gameProfile) {
      return this.contains(gameProfile);
   }

   public String[] getNames() {
      String[] strings = new String[this.values().size()];
      int i = 0;

      ServerConfigEntry serverConfigEntry;
      for(Iterator var3 = this.values().iterator(); var3.hasNext(); strings[i++] = ((GameProfile)serverConfigEntry.getKey()).getName()) {
         serverConfigEntry = (ServerConfigEntry)var3.next();
      }

      return strings;
   }

   protected String toString(GameProfile gameProfile) {
      return gameProfile.getId().toString();
   }
}
