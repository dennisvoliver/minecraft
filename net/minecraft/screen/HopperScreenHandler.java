package net.minecraft.screen;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;

public class HopperScreenHandler extends ScreenHandler {
   private final Inventory inventory;

   public HopperScreenHandler(int syncId, PlayerInventory playerInventory) {
      this(syncId, playerInventory, new SimpleInventory(5));
   }

   public HopperScreenHandler(int syncId, PlayerInventory playerInventory, Inventory inventory) {
      super(ScreenHandlerType.HOPPER, syncId);
      this.inventory = inventory;
      checkSize(inventory, 5);
      inventory.onOpen(playerInventory.player);
      int i = true;

      int m;
      for(m = 0; m < 5; ++m) {
         this.addSlot(new Slot(inventory, m, 44 + m * 18, 20));
      }

      for(m = 0; m < 3; ++m) {
         for(int l = 0; l < 9; ++l) {
            this.addSlot(new Slot(playerInventory, l + m * 9 + 9, 8 + l * 18, m * 18 + 51));
         }
      }

      for(m = 0; m < 9; ++m) {
         this.addSlot(new Slot(playerInventory, m, 8 + m * 18, 109));
      }

   }

   public boolean canUse(PlayerEntity player) {
      return this.inventory.canPlayerUse(player);
   }

   public ItemStack transferSlot(PlayerEntity player, int index) {
      ItemStack itemStack = ItemStack.EMPTY;
      Slot slot = (Slot)this.slots.get(index);
      if (slot != null && slot.hasStack()) {
         ItemStack itemStack2 = slot.getStack();
         itemStack = itemStack2.copy();
         if (index < this.inventory.size()) {
            if (!this.insertItem(itemStack2, this.inventory.size(), this.slots.size(), true)) {
               return ItemStack.EMPTY;
            }
         } else if (!this.insertItem(itemStack2, 0, this.inventory.size(), false)) {
            return ItemStack.EMPTY;
         }

         if (itemStack2.isEmpty()) {
            slot.setStack(ItemStack.EMPTY);
         } else {
            slot.markDirty();
         }
      }

      return itemStack;
   }

   public void close(PlayerEntity player) {
      super.close(player);
      this.inventory.onClose(player);
   }
}
