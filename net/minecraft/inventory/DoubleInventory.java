package net.minecraft.inventory;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;

public class DoubleInventory implements Inventory {
   private final Inventory first;
   private final Inventory second;

   public DoubleInventory(Inventory first, Inventory second) {
      if (first == null) {
         first = second;
      }

      if (second == null) {
         second = first;
      }

      this.first = first;
      this.second = second;
   }

   public int size() {
      return this.first.size() + this.second.size();
   }

   public boolean isEmpty() {
      return this.first.isEmpty() && this.second.isEmpty();
   }

   public boolean isPart(Inventory inventory) {
      return this.first == inventory || this.second == inventory;
   }

   public ItemStack getStack(int slot) {
      return slot >= this.first.size() ? this.second.getStack(slot - this.first.size()) : this.first.getStack(slot);
   }

   public ItemStack removeStack(int slot, int amount) {
      return slot >= this.first.size() ? this.second.removeStack(slot - this.first.size(), amount) : this.first.removeStack(slot, amount);
   }

   public ItemStack removeStack(int slot) {
      return slot >= this.first.size() ? this.second.removeStack(slot - this.first.size()) : this.first.removeStack(slot);
   }

   public void setStack(int slot, ItemStack stack) {
      if (slot >= this.first.size()) {
         this.second.setStack(slot - this.first.size(), stack);
      } else {
         this.first.setStack(slot, stack);
      }

   }

   public int getMaxCountPerStack() {
      return this.first.getMaxCountPerStack();
   }

   public void markDirty() {
      this.first.markDirty();
      this.second.markDirty();
   }

   public boolean canPlayerUse(PlayerEntity player) {
      return this.first.canPlayerUse(player) && this.second.canPlayerUse(player);
   }

   public void onOpen(PlayerEntity player) {
      this.first.onOpen(player);
      this.second.onOpen(player);
   }

   public void onClose(PlayerEntity player) {
      this.first.onClose(player);
      this.second.onClose(player);
   }

   public boolean isValid(int slot, ItemStack stack) {
      return slot >= this.first.size() ? this.second.isValid(slot - this.first.size(), stack) : this.first.isValid(slot, stack);
   }

   public void clear() {
      this.first.clear();
      this.second.clear();
   }
}
