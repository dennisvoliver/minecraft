package net.minecraft.network.packet.c2s.login;

import java.io.IOException;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.listener.ServerLoginPacketListener;
import org.jetbrains.annotations.Nullable;

public class LoginQueryResponseC2SPacket implements Packet<ServerLoginPacketListener> {
   private int queryId;
   private PacketByteBuf response;

   public LoginQueryResponseC2SPacket() {
   }

   @Environment(EnvType.CLIENT)
   public LoginQueryResponseC2SPacket(int queryId, @Nullable PacketByteBuf response) {
      this.queryId = queryId;
      this.response = response;
   }

   public void read(PacketByteBuf buf) throws IOException {
      this.queryId = buf.readVarInt();
      if (buf.readBoolean()) {
         int i = buf.readableBytes();
         if (i < 0 || i > 1048576) {
            throw new IOException("Payload may not be larger than 1048576 bytes");
         }

         this.response = new PacketByteBuf(buf.readBytes(i));
      } else {
         this.response = null;
      }

   }

   public void write(PacketByteBuf buf) throws IOException {
      buf.writeVarInt(this.queryId);
      if (this.response != null) {
         buf.writeBoolean(true);
         buf.writeBytes(this.response.copy());
      } else {
         buf.writeBoolean(false);
      }

   }

   public void apply(ServerLoginPacketListener serverLoginPacketListener) {
      serverLoginPacketListener.onQueryResponse(this);
   }
}
