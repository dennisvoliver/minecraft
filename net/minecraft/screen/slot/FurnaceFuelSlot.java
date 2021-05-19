package net.minecraft.screen.slot;

import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.AbstractFurnaceScreenHandler;

public class FurnaceFuelSlot extends Slot {
   private final AbstractFurnaceScreenHandler handler;

   public FurnaceFuelSlot(AbstractFurnaceScreenHandler handler, Inventory inventory, int index, int x, int y) {
      super(inventory, index, x, y);
      this.handler = handler;
   }

   public boolean canInsert(ItemStack stack) {
      return this.handler.isFuel(stack) || isBucket(stack);
   }

   public int getMaxItemCount(ItemStack stack) {
      return isBucket(stack) ? 1 : super.getMaxItemCount(stack);
   }

   public static boolean isBucket(ItemStack stack) {
      return stack.getItem() == Items.BUCKET;
   }
}
