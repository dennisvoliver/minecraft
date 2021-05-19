package net.minecraft.recipe;

import java.util.Iterator;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.FilledMapItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.map.MapIcon;
import net.minecraft.item.map.MapState;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.world.World;

public class MapExtendingRecipe extends ShapedRecipe {
   public MapExtendingRecipe(Identifier identifier) {
      super(identifier, "", 3, 3, DefaultedList.copyOf(Ingredient.EMPTY, Ingredient.ofItems(Items.PAPER), Ingredient.ofItems(Items.PAPER), Ingredient.ofItems(Items.PAPER), Ingredient.ofItems(Items.PAPER), Ingredient.ofItems(Items.FILLED_MAP), Ingredient.ofItems(Items.PAPER), Ingredient.ofItems(Items.PAPER), Ingredient.ofItems(Items.PAPER), Ingredient.ofItems(Items.PAPER)), new ItemStack(Items.MAP));
   }

   public boolean matches(CraftingInventory craftingInventory, World world) {
      if (!super.matches(craftingInventory, world)) {
         return false;
      } else {
         ItemStack itemStack = ItemStack.EMPTY;

         for(int i = 0; i < craftingInventory.size() && itemStack.isEmpty(); ++i) {
            ItemStack itemStack2 = craftingInventory.getStack(i);
            if (itemStack2.getItem() == Items.FILLED_MAP) {
               itemStack = itemStack2;
            }
         }

         if (itemStack.isEmpty()) {
            return false;
         } else {
            MapState mapState = FilledMapItem.getOrCreateMapState(itemStack, world);
            if (mapState == null) {
               return false;
            } else if (this.matches(mapState)) {
               return false;
            } else {
               return mapState.scale < 4;
            }
         }
      }
   }

   private boolean matches(MapState state) {
      if (state.icons != null) {
         Iterator var2 = state.icons.values().iterator();

         while(var2.hasNext()) {
            MapIcon mapIcon = (MapIcon)var2.next();
            if (mapIcon.getType() == MapIcon.Type.MANSION || mapIcon.getType() == MapIcon.Type.MONUMENT) {
               return true;
            }
         }
      }

      return false;
   }

   public ItemStack craft(CraftingInventory craftingInventory) {
      ItemStack itemStack = ItemStack.EMPTY;

      for(int i = 0; i < craftingInventory.size() && itemStack.isEmpty(); ++i) {
         ItemStack itemStack2 = craftingInventory.getStack(i);
         if (itemStack2.getItem() == Items.FILLED_MAP) {
            itemStack = itemStack2;
         }
      }

      itemStack = itemStack.copy();
      itemStack.setCount(1);
      itemStack.getOrCreateTag().putInt("map_scale_direction", 1);
      return itemStack;
   }

   public boolean isIgnoredInRecipeBook() {
      return true;
   }

   public RecipeSerializer<?> getSerializer() {
      return RecipeSerializer.MAP_EXTENDING;
   }
}
