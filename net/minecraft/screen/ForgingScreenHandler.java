package net.minecraft.screen;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.CraftingResultInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import org.jetbrains.annotations.Nullable;

public abstract class ForgingScreenHandler extends ScreenHandler {
   protected final CraftingResultInventory output = new CraftingResultInventory();
   protected final Inventory input = new SimpleInventory(2) {
      public void markDirty() {
         super.markDirty();
         ForgingScreenHandler.this.onContentChanged(this);
      }
   };
   protected final ScreenHandlerContext context;
   protected final PlayerEntity player;

   protected abstract boolean canTakeOutput(PlayerEntity player, boolean present);

   protected abstract ItemStack onTakeOutput(PlayerEntity player, ItemStack stack);

   protected abstract boolean canUse(BlockState state);

   public ForgingScreenHandler(@Nullable ScreenHandlerType<?> type, int syncId, PlayerInventory playerInventory, ScreenHandlerContext context) {
      super(type, syncId);
      this.context = context;
      this.player = playerInventory.player;
      this.addSlot(new Slot(this.input, 0, 27, 47));
      this.addSlot(new Slot(this.input, 1, 76, 47));
      this.addSlot(new Slot(this.output, 2, 134, 47) {
         public boolean canInsert(ItemStack stack) {
            return false;
         }

         public boolean canTakeItems(PlayerEntity playerEntity) {
            return ForgingScreenHandler.this.canTakeOutput(playerEntity, this.hasStack());
         }

         public ItemStack onTakeItem(PlayerEntity player, ItemStack stack) {
            return ForgingScreenHandler.this.onTakeOutput(player, stack);
         }
      });

      int k;
      for(k = 0; k < 3; ++k) {
         for(int j = 0; j < 9; ++j) {
            this.addSlot(new Slot(playerInventory, j + k * 9 + 9, 8 + j * 18, 84 + k * 18));
         }
      }

      for(k = 0; k < 9; ++k) {
         this.addSlot(new Slot(playerInventory, k, 8 + k * 18, 142));
      }

   }

   public abstract void updateResult();

   public void onContentChanged(Inventory inventory) {
      super.onContentChanged(inventory);
      if (inventory == this.input) {
         this.updateResult();
      }

   }

   public void close(PlayerEntity player) {
      super.close(player);
      this.context.run((world, blockPos) -> {
         this.dropInventory(player, world, this.input);
      });
   }

   public boolean canUse(PlayerEntity player) {
      return (Boolean)this.context.run((world, blockPos) -> {
         return !this.canUse(world.getBlockState(blockPos)) ? false : player.squaredDistanceTo((double)blockPos.getX() + 0.5D, (double)blockPos.getY() + 0.5D, (double)blockPos.getZ() + 0.5D) <= 64.0D;
      }, true);
   }

   protected boolean method_30025(ItemStack itemStack) {
      return false;
   }

   public ItemStack transferSlot(PlayerEntity player, int index) {
      ItemStack itemStack = ItemStack.EMPTY;
      Slot slot = (Slot)this.slots.get(index);
      if (slot != null && slot.hasStack()) {
         ItemStack itemStack2 = slot.getStack();
         itemStack = itemStack2.copy();
         if (index == 2) {
            if (!this.insertItem(itemStack2, 3, 39, true)) {
               return ItemStack.EMPTY;
            }

            slot.onStackChanged(itemStack2, itemStack);
         } else if (index != 0 && index != 1) {
            if (index >= 3 && index < 39) {
               int i = this.method_30025(itemStack) ? 1 : 0;
               if (!this.insertItem(itemStack2, i, 2, false)) {
                  return ItemStack.EMPTY;
               }
            }
         } else if (!this.insertItem(itemStack2, 3, 39, false)) {
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
}
