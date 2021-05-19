package net.minecraft.screen;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;

public class Generic3x3ContainerScreenHandler extends ScreenHandler {
   private final Inventory inventory;

   public Generic3x3ContainerScreenHandler(int syncId, PlayerInventory playerInventory) {
      this(syncId, playerInventory, new SimpleInventory(9));
   }

   public Generic3x3ContainerScreenHandler(int syncId, PlayerInventory playerInventory, Inventory inventory) {
      super(ScreenHandlerType.GENERIC_3X3, syncId);
      checkSize(inventory, 9);
      this.inventory = inventory;
      inventory.onOpen(playerInventory.player);

      int m;
      int l;
      for(m = 0; m < 3; ++m) {
         for(l = 0; l < 3; ++l) {
            this.addSlot(new Slot(inventory, l + m * 3, 62 + l * 18, 17 + m * 18));
         }
      }

      for(m = 0; m < 3; ++m) {
         for(l = 0; l < 9; ++l) {
            this.addSlot(new Slot(playerInventory, l + m * 9 + 9, 8 + l * 18, 84 + m * 18));
         }
      }

      for(m = 0; m < 9; ++m) {
         this.addSlot(new Slot(playerInventory, m, 8 + m * 18, 142));
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
         if (index < 9) {
            if (!this.insertItem(itemStack2, 9, 45, true)) {
               return ItemStack.EMPTY;
            }
         } else if (!this.insertItem(itemStack2, 0, 9, false)) {
            return ItemStack.EMPTY;
         }

         if (itemStack2.isEmpty()) {
            slot.setStack(ItemStack.EMPTY);
         } else {
            slot.markDirty();
         }

         if (itemStack2.getCount() == itemStack.getCount()) {
            return ItemStack.EMPTY;
         }

         slot.onTakeItem(player, itemStack2);
      }

      return itemStack;
   }

   public void close(PlayerEntity player) {
      super.close(player);
      this.inventory.onClose(player);
   }
}
