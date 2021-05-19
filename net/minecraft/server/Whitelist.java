package net.minecraft.server;

import com.google.gson.JsonObject;
import com.mojang.authlib.GameProfile;
import java.io.File;
import java.util.Iterator;

public class Whitelist extends ServerConfigList<GameProfile, WhitelistEntry> {
   public Whitelist(File file) {
      super(file);
   }

   protected ServerConfigEntry<GameProfile> fromJson(JsonObject json) {
      return new WhitelistEntry(json);
   }

   public boolean isAllowed(GameProfile profile) {
      return this.contains(profile);
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
