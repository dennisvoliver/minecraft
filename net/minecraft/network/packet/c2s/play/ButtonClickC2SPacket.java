package net.minecraft.network.packet.c2s.play;

import java.io.IOException;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.listener.ServerPlayPacketListener;

public class ButtonClickC2SPacket implements Packet<ServerPlayPacketListener> {
   private int syncId;
   private int buttonId;

   public ButtonClickC2SPacket() {
   }

   @Environment(EnvType.CLIENT)
   public ButtonClickC2SPacket(int syncId, int buttonId) {
      this.syncId = syncId;
      this.buttonId = buttonId;
   }

   public void apply(ServerPlayPacketListener serverPlayPacketListener) {
      serverPlayPacketListener.onButtonClick(this);
   }

   public void read(PacketByteBuf buf) throws IOException {
      this.syncId = buf.readByte();
      this.buttonId = buf.readByte();
   }

   public void write(PacketByteBuf buf) throws IOException {
      buf.writeByte(this.syncId);
      buf.writeByte(this.buttonId);
   }

   public int getSyncId() {
      return this.syncId;
   }

   public int getButtonId() {
      return this.buttonId;
   }
}
