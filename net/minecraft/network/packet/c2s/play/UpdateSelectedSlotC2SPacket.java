package net.minecraft.network.packet.c2s.play;

import java.io.IOException;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.listener.ServerPlayPacketListener;

public class UpdateSelectedSlotC2SPacket implements Packet<ServerPlayPacketListener> {
   private int selectedSlot;

   public UpdateSelectedSlotC2SPacket() {
   }

   @Environment(EnvType.CLIENT)
   public UpdateSelectedSlotC2SPacket(int selectedSlot) {
      this.selectedSlot = selectedSlot;
   }

   public void read(PacketByteBuf buf) throws IOException {
      this.selectedSlot = buf.readShort();
   }

   public void write(PacketByteBuf buf) throws IOException {
      buf.writeShort(this.selectedSlot);
   }

   public void apply(ServerPlayPacketListener serverPlayPacketListener) {
      serverPlayPacketListener.onUpdateSelectedSlot(this);
   }

   public int getSelectedSlot() {
      return this.selectedSlot;
   }
}
