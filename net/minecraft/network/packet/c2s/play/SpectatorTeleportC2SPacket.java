package net.minecraft.network.packet.c2s.play;

import java.io.IOException;
import java.util.UUID;
import net.minecraft.entity.Entity;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.listener.ServerPlayPacketListener;
import net.minecraft.server.world.ServerWorld;
import org.jetbrains.annotations.Nullable;

public class SpectatorTeleportC2SPacket implements Packet<ServerPlayPacketListener> {
   private UUID targetUuid;

   public SpectatorTeleportC2SPacket() {
   }

   public SpectatorTeleportC2SPacket(UUID targetUuid) {
      this.targetUuid = targetUuid;
   }

   public void read(PacketByteBuf buf) throws IOException {
      this.targetUuid = buf.readUuid();
   }

   public void write(PacketByteBuf buf) throws IOException {
      buf.writeUuid(this.targetUuid);
   }

   public void apply(ServerPlayPacketListener serverPlayPacketListener) {
      serverPlayPacketListener.onSpectatorTeleport(this);
   }

   @Nullable
   public Entity getTarget(ServerWorld world) {
      return world.getEntity(this.targetUuid);
   }
}
