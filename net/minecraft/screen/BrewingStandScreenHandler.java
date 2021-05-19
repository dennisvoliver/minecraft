package net.minecraft.screen;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionUtil;
import net.minecraft.recipe.BrewingRecipeRegistry;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.network.ServerPlayerEntity;

public class BrewingStandScreenHandler extends ScreenHandler {
   private final Inventory inventory;
   private final PropertyDelegate propertyDelegate;
   private final Slot ingredientSlot;

   public BrewingStandScreenHandler(int syncId, PlayerInventory playerInventory) {
      this(syncId, playerInventory, new SimpleInventory(5), new ArrayPropertyDelegate(2));
   }

   public BrewingStandScreenHandler(int syncId, PlayerInventory playerInventory, Inventory inventory, PropertyDelegate propertyDelegate) {
      super(ScreenHandlerType.BREWING_STAND, syncId);
      checkSize(inventory, 5);
      checkDataCount(propertyDelegate, 2);
      this.inventory = inventory;
      this.propertyDelegate = propertyDelegate;
      this.addSlot(new BrewingStandScreenHandler.PotionSlot(inventory, 0, 56, 51));
      this.addSlot(new BrewingStandScreenHandler.PotionSlot(inventory, 1, 79, 58));
      this.addSlot(new BrewingStandScreenHandler.PotionSlot(inventory, 2, 102, 51));
      this.ingredientSlot = this.addSlot(new BrewingStandScreenHandler.IngredientSlot(inventory, 3, 79, 17));
      this.addSlot(new BrewingStandScreenHandler.FuelSlot(inventory, 4, 17, 17));
      this.addProperties(propertyDelegate);

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

   public boolean canUse(PlayerEntity player) {
      return this.inventory.canPlayerUse(player);
   }

   public ItemStack transferSlot(PlayerEntity player, int index) {
      ItemStack itemStack = ItemStack.EMPTY;
      Slot slot = (Slot)this.slots.get(index);
      if (slot != null && slot.hasStack()) {
         ItemStack itemStack2 = slot.getStack();
         itemStack = itemStack2.copy();
         if ((index < 0 || index > 2) && index != 3 && index != 4) {
            if (BrewingStandScreenHandler.FuelSlot.matches(itemStack)) {
               if (this.insertItem(itemStack2, 4, 5, false) || this.ingredientSlot.canInsert(itemStack2) && !this.insertItem(itemStack2, 3, 4, false)) {
                  return ItemStack.EMPTY;
               }
            } else if (this.ingredientSlot.canInsert(itemStack2)) {
               if (!this.insertItem(itemStack2, 3, 4, false)) {
                  return ItemStack.EMPTY;
               }
            } else if (BrewingStandScreenHandler.PotionSlot.matches(itemStack) && itemStack.getCount() == 1) {
               if (!this.insertItem(itemStack2, 0, 3, false)) {
                  return ItemStack.EMPTY;
               }
            } else if (index >= 5 && index < 32) {
               if (!this.insertItem(itemStack2, 32, 41, false)) {
                  return ItemStack.EMPTY;
               }
            } else if (index >= 32 && index < 41) {
               if (!this.insertItem(itemStack2, 5, 32, false)) {
                  return ItemStack.EMPTY;
               }
            } else if (!this.insertItem(itemStack2, 5, 41, false)) {
               return ItemStack.EMPTY;
            }
         } else {
            if (!this.insertItem(itemStack2, 5, 41, true)) {
               return ItemStack.EMPTY;
            }

            slot.onStackChanged(itemStack2, itemStack);
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

   @Environment(EnvType.CLIENT)
   public int getFuel() {
      return this.propertyDelegate.get(1);
   }

   @Environment(EnvType.CLIENT)
   public int getBrewTime() {
      return this.propertyDelegate.get(0);
   }

   static class FuelSlot extends Slot {
      public FuelSlot(Inventory inventory, int i, int j, int k) {
         super(inventory, i, j, k);
      }

      public boolean canInsert(ItemStack stack) {
         return matches(stack);
      }

      public static boolean matches(ItemStack stack) {
         return stack.getItem() == Items.BLAZE_POWDER;
      }

      public int getMaxItemCount() {
         return 64;
      }
   }

   static class IngredientSlot extends Slot {
      public IngredientSlot(Inventory inventory, int i, int j, int k) {
         super(inventory, i, j, k);
      }

      public boolean canInsert(ItemStack stack) {
         return BrewingRecipeRegistry.isValidIngredient(stack);
      }

      public int getMaxItemCount() {
         return 64;
      }
   }

   static class PotionSlot extends Slot {
      public PotionSlot(Inventory inventory, int i, int j, int k) {
         super(inventory, i, j, k);
      }

      public boolean canInsert(ItemStack stack) {
         return matches(stack);
      }

      public int getMaxItemCount() {
         return 1;
      }

      public ItemStack onTakeItem(PlayerEntity player, ItemStack stack) {
         Potion potion = PotionUtil.getPotion(stack);
         if (player instanceof ServerPlayerEntity) {
            Criteria.BREWED_POTION.trigger((ServerPlayerEntity)player, potion);
         }

         super.onTakeItem(player, stack);
         return stack;
      }

      public static boolean matches(ItemStack stack) {
         Item item = stack.getItem();
         return item == Items.POTION || item == Items.SPLASH_POTION || item == Items.LINGERING_POTION || item == Items.GLASS_BOTTLE;
      }
   }
}
