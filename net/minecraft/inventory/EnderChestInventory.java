package net.minecraft.inventory;

import net.minecraft.block.entity.EnderChestBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;

public class EnderChestInventory extends SimpleInventory {
   private EnderChestBlockEntity activeBlockEntity;

   public EnderChestInventory() {
      super(27);
   }

   public void setActiveBlockEntity(EnderChestBlockEntity blockEntity) {
      this.activeBlockEntity = blockEntity;
   }

   public void readTags(ListTag tags) {
      int j;
      for(j = 0; j < this.size(); ++j) {
         this.setStack(j, ItemStack.EMPTY);
      }

      for(j = 0; j < tags.size(); ++j) {
         CompoundTag compoundTag = tags.getCompound(j);
         int k = compoundTag.getByte("Slot") & 255;
         if (k >= 0 && k < this.size()) {
            this.setStack(k, ItemStack.fromTag(compoundTag));
         }
      }

   }

   public ListTag getTags() {
      ListTag listTag = new ListTag();

      for(int i = 0; i < this.size(); ++i) {
         ItemStack itemStack = this.getStack(i);
         if (!itemStack.isEmpty()) {
            CompoundTag compoundTag = new CompoundTag();
            compoundTag.putByte("Slot", (byte)i);
            itemStack.toTag(compoundTag);
            listTag.add(compoundTag);
         }
      }

      return listTag;
   }

   public boolean canPlayerUse(PlayerEntity player) {
      return this.activeBlockEntity != null && !this.activeBlockEntity.canPlayerUse(player) ? false : super.canPlayerUse(player);
   }

   public void onOpen(PlayerEntity player) {
      if (this.activeBlockEntity != null) {
         this.activeBlockEntity.onOpen();
      }

      super.onOpen(player);
   }

   public void onClose(PlayerEntity player) {
      if (this.activeBlockEntity != null) {
         this.activeBlockEntity.onClose();
      }

      super.onClose(player);
      this.activeBlockEntity = null;
   }
}
