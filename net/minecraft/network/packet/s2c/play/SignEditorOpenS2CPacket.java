package net.minecraft.network.packet.s2c.play;

import java.io.IOException;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.util.math.BlockPos;

public class SignEditorOpenS2CPacket implements Packet<ClientPlayPacketListener> {
   private BlockPos pos;

   public SignEditorOpenS2CPacket() {
   }

   public SignEditorOpenS2CPacket(BlockPos pos) {
      this.pos = pos;
   }

   public void apply(ClientPlayPacketListener clientPlayPacketListener) {
      clientPlayPacketListener.onSignEditorOpen(this);
   }

   public void read(PacketByteBuf buf) throws IOException {
      this.pos = buf.readBlockPos();
   }

   public void write(PacketByteBuf buf) throws IOException {
      buf.writeBlockPos(this.pos);
   }

   @Environment(EnvType.CLIENT)
   public BlockPos getPos() {
      return this.pos;
   }
}
