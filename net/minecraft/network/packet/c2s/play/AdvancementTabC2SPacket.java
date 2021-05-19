package net.minecraft.network.packet.c2s.play;

import java.io.IOException;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.advancement.Advancement;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.listener.ServerPlayPacketListener;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

public class AdvancementTabC2SPacket implements Packet<ServerPlayPacketListener> {
   private AdvancementTabC2SPacket.Action action;
   private Identifier tabToOpen;

   public AdvancementTabC2SPacket() {
   }

   @Environment(EnvType.CLIENT)
   public AdvancementTabC2SPacket(AdvancementTabC2SPacket.Action action, @Nullable Identifier tab) {
      this.action = action;
      this.tabToOpen = tab;
   }

   @Environment(EnvType.CLIENT)
   public static AdvancementTabC2SPacket open(Advancement advancement) {
      return new AdvancementTabC2SPacket(AdvancementTabC2SPacket.Action.OPENED_TAB, advancement.getId());
   }

   @Environment(EnvType.CLIENT)
   public static AdvancementTabC2SPacket close() {
      return new AdvancementTabC2SPacket(AdvancementTabC2SPacket.Action.CLOSED_SCREEN, (Identifier)null);
   }

   public void read(PacketByteBuf buf) throws IOException {
      this.action = (AdvancementTabC2SPacket.Action)buf.readEnumConstant(AdvancementTabC2SPacket.Action.class);
      if (this.action == AdvancementTabC2SPacket.Action.OPENED_TAB) {
         this.tabToOpen = buf.readIdentifier();
      }

   }

   public void write(PacketByteBuf buf) throws IOException {
      buf.writeEnumConstant(this.action);
      if (this.action == AdvancementTabC2SPacket.Action.OPENED_TAB) {
         buf.writeIdentifier(this.tabToOpen);
      }

   }

   public void apply(ServerPlayPacketListener serverPlayPacketListener) {
      serverPlayPacketListener.onAdvancementTab(this);
   }

   public AdvancementTabC2SPacket.Action getAction() {
      return this.action;
   }

   public Identifier getTabToOpen() {
      return this.tabToOpen;
   }

   public static enum Action {
      OPENED_TAB,
      CLOSED_SCREEN;
   }
}
