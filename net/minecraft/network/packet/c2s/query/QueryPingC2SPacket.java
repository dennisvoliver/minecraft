package net.minecraft.network.packet.c2s.query;

import java.io.IOException;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.listener.ServerQueryPacketListener;

public class QueryPingC2SPacket implements Packet<ServerQueryPacketListener> {
   private long startTime;

   public QueryPingC2SPacket() {
   }

   @Environment(EnvType.CLIENT)
   public QueryPingC2SPacket(long startTime) {
      this.startTime = startTime;
   }

   public void read(PacketByteBuf buf) throws IOException {
      this.startTime = buf.readLong();
   }

   public void write(PacketByteBuf buf) throws IOException {
      buf.writeLong(this.startTime);
   }

   public void apply(ServerQueryPacketListener serverQueryPacketListener) {
      serverQueryPacketListener.onPing(this);
   }

   public long getStartTime() {
      return this.startTime;
   }
}
