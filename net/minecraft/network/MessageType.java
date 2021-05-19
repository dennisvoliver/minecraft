package net.minecraft.network;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

public enum MessageType {
   CHAT((byte)0, false),
   SYSTEM((byte)1, true),
   GAME_INFO((byte)2, true);

   private final byte id;
   private final boolean interruptsNarration;

   private MessageType(byte id, boolean interruptsNarration) {
      this.id = id;
      this.interruptsNarration = interruptsNarration;
   }

   public byte getId() {
      return this.id;
   }

   public static MessageType byId(byte id) {
      MessageType[] var1 = values();
      int var2 = var1.length;

      for(int var3 = 0; var3 < var2; ++var3) {
         MessageType messageType = var1[var3];
         if (id == messageType.id) {
            return messageType;
         }
      }

      return CHAT;
   }

   @Environment(EnvType.CLIENT)
   public boolean interruptsNarration() {
      return this.interruptsNarration;
   }
}
