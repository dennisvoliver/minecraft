package net.minecraft.network.packet.s2c.play;

import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import java.io.IOException;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.listener.ClientPlayPacketListener;

public class EntityEquipmentUpdateS2CPacket implements Packet<ClientPlayPacketListener> {
   private int id;
   private final List<Pair<EquipmentSlot, ItemStack>> equipmentList;

   public EntityEquipmentUpdateS2CPacket() {
      this.equipmentList = Lists.newArrayList();
   }

   public EntityEquipmentUpdateS2CPacket(int id, List<Pair<EquipmentSlot, ItemStack>> equipmentList) {
      this.id = id;
      this.equipmentList = equipmentList;
   }

   public void read(PacketByteBuf buf) throws IOException {
      this.id = buf.readVarInt();
      EquipmentSlot[] equipmentSlots = EquipmentSlot.values();

      byte i;
      do {
         i = buf.readByte();
         EquipmentSlot equipmentSlot = equipmentSlots[i & 127];
         ItemStack itemStack = buf.readItemStack();
         this.equipmentList.add(Pair.of(equipmentSlot, itemStack));
      } while((i & -128) != 0);

   }

   public void write(PacketByteBuf buf) throws IOException {
      buf.writeVarInt(this.id);
      int i = this.equipmentList.size();

      for(int j = 0; j < i; ++j) {
         Pair<EquipmentSlot, ItemStack> pair = (Pair)this.equipmentList.get(j);
         EquipmentSlot equipmentSlot = (EquipmentSlot)pair.getFirst();
         boolean bl = j != i - 1;
         int k = equipmentSlot.ordinal();
         buf.writeByte(bl ? k | -128 : k);
         buf.writeItemStack((ItemStack)pair.getSecond());
      }

   }

   public void apply(ClientPlayPacketListener clientPlayPacketListener) {
      clientPlayPacketListener.onEquipmentUpdate(this);
   }

   @Environment(EnvType.CLIENT)
   public int getId() {
      return this.id;
   }

   @Environment(EnvType.CLIENT)
   public List<Pair<EquipmentSlot, ItemStack>> getEquipmentList() {
      return this.equipmentList;
   }
}
