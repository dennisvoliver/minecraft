package net.minecraft.network.packet.s2c.play;

import java.io.IOException;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.listener.ClientPlayPacketListener;
import org.jetbrains.annotations.Nullable;

public class TagQueryResponseS2CPacket implements Packet<ClientPlayPacketListener> {
   private int transactionId;
   @Nullable
   private CompoundTag tag;

   public TagQueryResponseS2CPacket() {
   }

   public TagQueryResponseS2CPacket(int transactionId, @Nullable CompoundTag tag) {
      this.transactionId = transactionId;
      this.tag = tag;
   }

   public void read(PacketByteBuf buf) throws IOException {
      this.transactionId = buf.readVarInt();
      this.tag = buf.readCompoundTag();
   }

   public void write(PacketByteBuf buf) throws IOException {
      buf.writeVarInt(this.transactionId);
      buf.writeCompoundTag(this.tag);
   }

   public void apply(ClientPlayPacketListener clientPlayPacketListener) {
      clientPlayPacketListener.onTagQuery(this);
   }

   @Environment(EnvType.CLIENT)
   public int getTransactionId() {
      return this.transactionId;
   }

   @Nullable
   @Environment(EnvType.CLIENT)
   public CompoundTag getTag() {
      return this.tag;
   }

   public boolean isWritingErrorSkippable() {
      return true;
   }
}
