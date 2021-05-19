package net.minecraft.screen;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;

public class GenericContainerScreenHandler extends ScreenHandler {
   private final Inventory inventory;
   private final int rows;

   private GenericContainerScreenHandler(ScreenHandlerType<?> type, int syncId, PlayerInventory playerInventory, int rows) {
      this(type, syncId, playerInventory, new SimpleInventory(9 * rows), rows);
   }

   public static GenericContainerScreenHandler createGeneric9x1(int syncId, PlayerInventory playerInventory) {
      return new GenericContainerScreenHandler(ScreenHandlerType.GENERIC_9X1, syncId, playerInventory, 1);
   }

   public static GenericContainerScreenHandler createGeneric9x2(int syncId, PlayerInventory playerInventory) {
      return new GenericContainerScreenHandler(ScreenHandlerType.GENERIC_9X2, syncId, playerInventory, 2);
   }

   public static GenericContainerScreenHandler createGeneric9x3(int syncId, PlayerInventory playerInventory) {
      return new GenericContainerScreenHandler(ScreenHandlerType.GENERIC_9X3, syncId, playerInventory, 3);
   }

   public static GenericContainerScreenHandler createGeneric9x4(int syncId, PlayerInventory playerInventory) {
      return new GenericContainerScreenHandler(ScreenHandlerType.GENERIC_9X4, syncId, playerInventory, 4);
   }

   public static GenericContainerScreenHandler createGeneric9x5(int syncId, PlayerInventory playerInventory) {
      return new GenericContainerScreenHandler(ScreenHandlerType.GENERIC_9X5, syncId, playerInventory, 5);
   }

   public static GenericContainerScreenHandler createGeneric9x6(int syncId, PlayerInventory playerInventory) {
      return new GenericContainerScreenHandler(ScreenHandlerType.GENERIC_9X6, syncId, playerInventory, 6);
   }

   public static GenericContainerScreenHandler createGeneric9x3(int syncId, PlayerInventory playerInventory, Inventory inventory) {
      return new GenericContainerScreenHandler(ScreenHandlerType.GENERIC_9X3, syncId, playerInventory, inventory, 3);
   }

   public static GenericContainerScreenHandler createGeneric9x6(int syncId, PlayerInventory playerInventory, Inventory inventory) {
      return new GenericContainerScreenHandler(ScreenHandlerType.GENERIC_9X6, syncId, playerInventory, inventory, 6);
   }

   public GenericContainerScreenHandler(ScreenHandlerType<?> type, int syncId, PlayerInventory playerInventory, Inventory inventory, int rows) {
      super(type, syncId);
      checkSize(inventory, rows * 9);
      this.inventory = inventory;
      this.rows = rows;
      inventory.onOpen(playerInventory.player);
      int i = (this.rows - 4) * 18;

      int n;
      int m;
      for(n = 0; n < this.rows; ++n) {
         for(m = 0; m < 9; ++m) {
            this.addSlot(new Slot(inventory, m + n * 9, 8 + m * 18, 18 + n * 18));
         }
      }

      for(n = 0; n < 3; ++n) {
         for(m = 0; m < 9; ++m) {
            this.addSlot(new Slot(playerInventory, m + n * 9 + 9, 8 + m * 18, 103 + n * 18 + i));
         }
      }

      for(n = 0; n < 9; ++n) {
         this.addSlot(new Slot(playerInventory, n, 8 + n * 18, 161 + i));
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
         if (index < this.rows * 9) {
            if (!this.insertItem(itemStack2, this.rows * 9, this.slots.size(), true)) {
               return ItemStack.EMPTY;
            }
         } else if (!this.insertItem(itemStack2, 0, this.rows * 9, false)) {
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

   public Inventory getInventory() {
      return this.inventory;
   }

   @Environment(EnvType.CLIENT)
   public int getRows() {
      return this.rows;
   }
}
