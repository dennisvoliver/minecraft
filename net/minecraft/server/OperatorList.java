package net.minecraft.server;

import com.google.gson.JsonObject;
import com.mojang.authlib.GameProfile;
import java.io.File;
import java.util.Iterator;

public class OperatorList extends ServerConfigList<GameProfile, OperatorEntry> {
   public OperatorList(File file) {
      super(file);
   }

   protected ServerConfigEntry<GameProfile> fromJson(JsonObject json) {
      return new OperatorEntry(json);
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

   public boolean isOp(GameProfile profile) {
      OperatorEntry operatorEntry = (OperatorEntry)this.get(profile);
      return operatorEntry != null ? operatorEntry.canBypassPlayerLimit() : false;
   }

   protected String toString(GameProfile gameProfile) {
      return gameProfile.getId().toString();
   }
}
