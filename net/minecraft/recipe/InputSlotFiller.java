package net.minecraft.recipe;

import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntListIterator;
import java.util.Iterator;
import java.util.List;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.CraftFailedResponseS2CPacket;
import net.minecraft.screen.AbstractRecipeScreenHandler;
import net.minecraft.screen.CraftingScreenHandler;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.network.ServerPlayerEntity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

public class InputSlotFiller<C extends Inventory> implements RecipeGridAligner<Integer> {
   protected static final Logger LOGGER = LogManager.getLogger();
   protected final RecipeFinder recipeFinder = new RecipeFinder();
   protected PlayerInventory inventory;
   protected AbstractRecipeScreenHandler<C> craftingScreenHandler;

   public InputSlotFiller(AbstractRecipeScreenHandler<C> abstractRecipeScreenHandler) {
      this.craftingScreenHandler = abstractRecipeScreenHandler;
   }

   public void fillInputSlots(ServerPlayerEntity entity, @Nullable Recipe<C> recipe, boolean craftAll) {
      if (recipe != null && entity.getRecipeBook().contains(recipe)) {
         this.inventory = entity.inventory;
         if (this.canReturnInputs() || entity.isCreative()) {
            this.recipeFinder.clear();
            entity.inventory.populateRecipeFinder(this.recipeFinder);
            this.craftingScreenHandler.populateRecipeFinder(this.recipeFinder);
            if (this.recipeFinder.findRecipe(recipe, (IntList)null)) {
               this.fillInputSlots(recipe, craftAll);
            } else {
               this.returnInputs();
               entity.networkHandler.sendPacket(new CraftFailedResponseS2CPacket(entity.currentScreenHandler.syncId, recipe));
            }

            entity.inventory.markDirty();
         }
      }
   }

   protected void returnInputs() {
      for(int i = 0; i < this.craftingScreenHandler.getCraftingWidth() * this.craftingScreenHandler.getCraftingHeight() + 1; ++i) {
         if (i != this.craftingScreenHandler.getCraftingResultSlotIndex() || !(this.craftingScreenHandler instanceof CraftingScreenHandler) && !(this.craftingScreenHandler instanceof PlayerScreenHandler)) {
            this.returnSlot(i);
         }
      }

      this.craftingScreenHandler.clearCraftingSlots();
   }

   protected void returnSlot(int i) {
      ItemStack itemStack = this.craftingScreenHandler.getSlot(i).getStack();
      if (!itemStack.isEmpty()) {
         for(; itemStack.getCount() > 0; this.craftingScreenHandler.getSlot(i).takeStack(1)) {
            int j = this.inventory.getOccupiedSlotWithRoomForStack(itemStack);
            if (j == -1) {
               j = this.inventory.getEmptySlot();
            }

            ItemStack itemStack2 = itemStack.copy();
            itemStack2.setCount(1);
            if (!this.inventory.insertStack(j, itemStack2)) {
               LOGGER.error("Can't find any space for item in the inventory");
            }
         }

      }
   }

   protected void fillInputSlots(Recipe<C> recipe, boolean craftAll) {
      boolean bl = this.craftingScreenHandler.matches(recipe);
      int i = this.recipeFinder.countRecipeCrafts(recipe, (IntList)null);
      int j;
      if (bl) {
         for(j = 0; j < this.craftingScreenHandler.getCraftingHeight() * this.craftingScreenHandler.getCraftingWidth() + 1; ++j) {
            if (j != this.craftingScreenHandler.getCraftingResultSlotIndex()) {
               ItemStack itemStack = this.craftingScreenHandler.getSlot(j).getStack();
               if (!itemStack.isEmpty() && Math.min(i, itemStack.getMaxCount()) < itemStack.getCount() + 1) {
                  return;
               }
            }
         }
      }

      j = this.getAmountToFill(craftAll, i, bl);
      IntList intList = new IntArrayList();
      if (this.recipeFinder.findRecipe(recipe, intList, j)) {
         int l = j;
         IntListIterator var8 = intList.iterator();

         while(var8.hasNext()) {
            int m = (Integer)var8.next();
            int n = RecipeFinder.getStackFromId(m).getMaxCount();
            if (n < l) {
               l = n;
            }
         }

         if (this.recipeFinder.findRecipe(recipe, intList, l)) {
            this.returnInputs();
            this.alignRecipeToGrid(this.craftingScreenHandler.getCraftingWidth(), this.craftingScreenHandler.getCraftingHeight(), this.craftingScreenHandler.getCraftingResultSlotIndex(), recipe, intList.iterator(), l);
         }
      }

   }

   public void acceptAlignedInput(Iterator<Integer> inputs, int slot, int amount, int gridX, int gridY) {
      Slot slot2 = this.craftingScreenHandler.getSlot(slot);
      ItemStack itemStack = RecipeFinder.getStackFromId((Integer)inputs.next());
      if (!itemStack.isEmpty()) {
         for(int i = 0; i < amount; ++i) {
            this.fillInputSlot(slot2, itemStack);
         }
      }

   }

   protected int getAmountToFill(boolean craftAll, int limit, boolean recipeInCraftingSlots) {
      int i = 1;
      if (craftAll) {
         i = limit;
      } else if (recipeInCraftingSlots) {
         i = 64;

         for(int j = 0; j < this.craftingScreenHandler.getCraftingWidth() * this.craftingScreenHandler.getCraftingHeight() + 1; ++j) {
            if (j != this.craftingScreenHandler.getCraftingResultSlotIndex()) {
               ItemStack itemStack = this.craftingScreenHandler.getSlot(j).getStack();
               if (!itemStack.isEmpty() && i > itemStack.getCount()) {
                  i = itemStack.getCount();
               }
            }
         }

         if (i < 64) {
            ++i;
         }
      }

      return i;
   }

   protected void fillInputSlot(Slot slot, ItemStack itemStack) {
      int i = this.inventory.method_7371(itemStack);
      if (i != -1) {
         ItemStack itemStack2 = this.inventory.getStack(i).copy();
         if (!itemStack2.isEmpty()) {
            if (itemStack2.getCount() > 1) {
               this.inventory.removeStack(i, 1);
            } else {
               this.inventory.removeStack(i);
            }

            itemStack2.setCount(1);
            if (slot.getStack().isEmpty()) {
               slot.setStack(itemStack2);
            } else {
               slot.getStack().increment(1);
            }

         }
      }
   }

   private boolean canReturnInputs() {
      List<ItemStack> list = Lists.newArrayList();
      int i = this.getFreeInventorySlots();

      for(int j = 0; j < this.craftingScreenHandler.getCraftingWidth() * this.craftingScreenHandler.getCraftingHeight() + 1; ++j) {
         if (j != this.craftingScreenHandler.getCraftingResultSlotIndex()) {
            ItemStack itemStack = this.craftingScreenHandler.getSlot(j).getStack().copy();
            if (!itemStack.isEmpty()) {
               int k = this.inventory.getOccupiedSlotWithRoomForStack(itemStack);
               if (k == -1 && list.size() <= i) {
                  Iterator var6 = list.iterator();

                  while(var6.hasNext()) {
                     ItemStack itemStack2 = (ItemStack)var6.next();
                     if (itemStack2.isItemEqualIgnoreDamage(itemStack) && itemStack2.getCount() != itemStack2.getMaxCount() && itemStack2.getCount() + itemStack.getCount() <= itemStack2.getMaxCount()) {
                        itemStack2.increment(itemStack.getCount());
                        itemStack.setCount(0);
                        break;
                     }
                  }

                  if (!itemStack.isEmpty()) {
                     if (list.size() >= i) {
                        return false;
                     }

                     list.add(itemStack);
                  }
               } else if (k == -1) {
                  return false;
               }
            }
         }
      }

      return true;
   }

   private int getFreeInventorySlots() {
      int i = 0;
      Iterator var2 = this.inventory.main.iterator();

      while(var2.hasNext()) {
         ItemStack itemStack = (ItemStack)var2.next();
         if (itemStack.isEmpty()) {
            ++i;
         }
      }

      return i;
   }
}
