package net.minecraft.network.packet.s2c.play;

import java.io.IOException;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.text.Text;
import net.minecraft.util.registry.Registry;
import org.jetbrains.annotations.Nullable;

public class OpenScreenS2CPacket implements Packet<ClientPlayPacketListener> {
   private int syncId;
   private int screenHandlerId;
   private Text name;

   public OpenScreenS2CPacket() {
   }

   public OpenScreenS2CPacket(int syncId, ScreenHandlerType<?> type, Text name) {
      this.syncId = syncId;
      this.screenHandlerId = Registry.SCREEN_HANDLER.getRawId(type);
      this.name = name;
   }

   public void read(PacketByteBuf buf) throws IOException {
      this.syncId = buf.readVarInt();
      this.screenHandlerId = buf.readVarInt();
      this.name = buf.readText();
   }

   public void write(PacketByteBuf buf) throws IOException {
      buf.writeVarInt(this.syncId);
      buf.writeVarInt(this.screenHandlerId);
      buf.writeText(this.name);
   }

   public void apply(ClientPlayPacketListener clientPlayPacketListener) {
      clientPlayPacketListener.onOpenScreen(this);
   }

   @Environment(EnvType.CLIENT)
   public int getSyncId() {
      return this.syncId;
   }

   @Nullable
   @Environment(EnvType.CLIENT)
   public ScreenHandlerType<?> getScreenHandlerType() {
      return (ScreenHandlerType)Registry.SCREEN_HANDLER.get(this.screenHandlerId);
   }

   @Environment(EnvType.CLIENT)
   public Text getName() {
      return this.name;
   }
}
