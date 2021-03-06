package net.minecraft.network.packet.c2s.play;

import java.io.IOException;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.listener.ServerPlayPacketListener;

public class UpdateDifficultyLockC2SPacket implements Packet<ServerPlayPacketListener> {
   private boolean difficultyLocked;

   public UpdateDifficultyLockC2SPacket() {
   }

   @Environment(EnvType.CLIENT)
   public UpdateDifficultyLockC2SPacket(boolean difficultyLocked) {
      this.difficultyLocked = difficultyLocked;
   }

   public void apply(ServerPlayPacketListener serverPlayPacketListener) {
      serverPlayPacketListener.onUpdateDifficultyLock(this);
   }

   public void read(PacketByteBuf buf) throws IOException {
      this.difficultyLocked = buf.readBoolean();
   }

   public void write(PacketByteBuf buf) throws IOException {
      buf.writeBoolean(this.difficultyLocked);
   }

   public boolean isDifficultyLocked() {
      return this.difficultyLocked;
   }
}
