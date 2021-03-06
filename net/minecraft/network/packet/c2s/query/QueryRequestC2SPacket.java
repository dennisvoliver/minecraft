package net.minecraft.network.packet.c2s.query;

import java.io.IOException;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.listener.ServerQueryPacketListener;

public class QueryRequestC2SPacket implements Packet<ServerQueryPacketListener> {
   public void read(PacketByteBuf buf) throws IOException {
   }

   public void write(PacketByteBuf buf) throws IOException {
   }

   public void apply(ServerQueryPacketListener serverQueryPacketListener) {
      serverQueryPacketListener.onRequest(this);
   }
}
