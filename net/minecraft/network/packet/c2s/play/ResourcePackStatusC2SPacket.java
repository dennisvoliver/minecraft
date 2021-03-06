package net.minecraft.network.packet.c2s.play;

import java.io.IOException;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.listener.ServerPlayPacketListener;

public class ResourcePackStatusC2SPacket implements Packet<ServerPlayPacketListener> {
   private ResourcePackStatusC2SPacket.Status status;

   public ResourcePackStatusC2SPacket() {
   }

   public ResourcePackStatusC2SPacket(ResourcePackStatusC2SPacket.Status status) {
      this.status = status;
   }

   public void read(PacketByteBuf buf) throws IOException {
      this.status = (ResourcePackStatusC2SPacket.Status)buf.readEnumConstant(ResourcePackStatusC2SPacket.Status.class);
   }

   public void write(PacketByteBuf buf) throws IOException {
      buf.writeEnumConstant(this.status);
   }

   public void apply(ServerPlayPacketListener serverPlayPacketListener) {
      serverPlayPacketListener.onResourcePackStatus(this);
   }

   public static enum Status {
      SUCCESSFULLY_LOADED,
      DECLINED,
      FAILED_DOWNLOAD,
      ACCEPTED;
   }
}
