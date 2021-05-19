package net.minecraft.network.packet.s2c.play;

import java.io.IOException;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.listener.ClientPlayPacketListener;

public class EntitiesDestroyS2CPacket implements Packet<ClientPlayPacketListener> {
   private int[] entityIds;

   public EntitiesDestroyS2CPacket() {
   }

   public EntitiesDestroyS2CPacket(int... entityIds) {
      this.entityIds = entityIds;
   }

   public void read(PacketByteBuf buf) throws IOException {
      this.entityIds = new int[buf.readVarInt()];

      for(int i = 0; i < this.entityIds.length; ++i) {
         this.entityIds[i] = buf.readVarInt();
      }

   }

   public void write(PacketByteBuf buf) throws IOException {
      buf.writeVarInt(this.entityIds.length);
      int[] var2 = this.entityIds;
      int var3 = var2.length;

      for(int var4 = 0; var4 < var3; ++var4) {
         int i = var2[var4];
         buf.writeVarInt(i);
      }

   }

   public void apply(ClientPlayPacketListener clientPlayPacketListener) {
      clientPlayPacketListener.onEntitiesDestroy(this);
   }

   @Environment(EnvType.CLIENT)
   public int[] getEntityIds() {
      return this.entityIds;
   }
}
