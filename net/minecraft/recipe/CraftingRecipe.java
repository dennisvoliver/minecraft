package net.minecraft.recipe;

import net.minecraft.inventory.CraftingInventory;

public interface CraftingRecipe extends Recipe<CraftingInventory> {
   default RecipeType<?> getType() {
      return RecipeType.CRAFTING;
   }
}
