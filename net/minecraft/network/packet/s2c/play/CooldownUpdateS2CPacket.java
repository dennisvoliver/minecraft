package net.minecraft.network.packet.s2c.play;

import java.io.IOException;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.item.Item;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.listener.ClientPlayPacketListener;

public class CooldownUpdateS2CPacket implements Packet<ClientPlayPacketListener> {
   private Item item;
   private int cooldown;

   public CooldownUpdateS2CPacket() {
   }

   public CooldownUpdateS2CPacket(Item item, int cooldown) {
      this.item = item;
      this.cooldown = cooldown;
   }

   public void read(PacketByteBuf buf) throws IOException {
      this.item = Item.byRawId(buf.readVarInt());
      this.cooldown = buf.readVarInt();
   }

   public void write(PacketByteBuf buf) throws IOException {
      buf.writeVarInt(Item.getRawId(this.item));
      buf.writeVarInt(this.cooldown);
   }

   public void apply(ClientPlayPacketListener clientPlayPacketListener) {
      clientPlayPacketListener.onCooldownUpdate(this);
   }

   @Environment(EnvType.CLIENT)
   public Item getItem() {
      return this.item;
   }

   @Environment(EnvType.CLIENT)
   public int getCooldown() {
      return this.cooldown;
   }
}
