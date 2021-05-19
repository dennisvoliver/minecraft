package net.minecraft.client.gui.screen.recipebook;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import it.unimi.dsi.fastutil.ints.IntList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeFinder;
import net.minecraft.recipe.book.RecipeBook;

@Environment(EnvType.CLIENT)
public class RecipeResultCollection {
   private final List<Recipe<?>> recipes;
   private final boolean singleOutput;
   private final Set<Recipe<?>> craftableRecipes = Sets.newHashSet();
   private final Set<Recipe<?>> fittingRecipes = Sets.newHashSet();
   private final Set<Recipe<?>> unlockedRecipes = Sets.newHashSet();

   public RecipeResultCollection(List<Recipe<?>> list) {
      this.recipes = ImmutableList.copyOf((Collection)list);
      if (list.size() <= 1) {
         this.singleOutput = true;
      } else {
         this.singleOutput = shouldHaveSingleOutput(list);
      }

   }

   private static boolean shouldHaveSingleOutput(List<Recipe<?>> list) {
      int i = list.size();
      ItemStack itemStack = ((Recipe)list.get(0)).getOutput();

      for(int j = 1; j < i; ++j) {
         ItemStack itemStack2 = ((Recipe)list.get(j)).getOutput();
         if (!ItemStack.areItemsEqualIgnoreDamage(itemStack, itemStack2) || !ItemStack.areTagsEqual(itemStack, itemStack2)) {
            return false;
         }
      }

      return true;
   }

   public boolean isInitialized() {
      return !this.unlockedRecipes.isEmpty();
   }

   public void initialize(RecipeBook recipeBook) {
      Iterator var2 = this.recipes.iterator();

      while(var2.hasNext()) {
         Recipe<?> recipe = (Recipe)var2.next();
         if (recipeBook.contains(recipe)) {
            this.unlockedRecipes.add(recipe);
         }
      }

   }

   public void computeCraftables(RecipeFinder recipeFinder, int gridWidth, int gridHeight, RecipeBook recipeBook) {
      Iterator var5 = this.recipes.iterator();

      while(true) {
         while(var5.hasNext()) {
            Recipe<?> recipe = (Recipe)var5.next();
            boolean bl = recipe.fits(gridWidth, gridHeight) && recipeBook.contains(recipe);
            if (bl) {
               this.fittingRecipes.add(recipe);
            } else {
               this.fittingRecipes.remove(recipe);
            }

            if (bl && recipeFinder.findRecipe(recipe, (IntList)null)) {
               this.craftableRecipes.add(recipe);
            } else {
               this.craftableRecipes.remove(recipe);
            }
         }

         return;
      }
   }

   public boolean isCraftable(Recipe<?> recipe) {
      return this.craftableRecipes.contains(recipe);
   }

   public boolean hasCraftableRecipes() {
      return !this.craftableRecipes.isEmpty();
   }

   public boolean hasFittingRecipes() {
      return !this.fittingRecipes.isEmpty();
   }

   public List<Recipe<?>> getAllRecipes() {
      return this.recipes;
   }

   public List<Recipe<?>> getResults(boolean craftableOnly) {
      List<Recipe<?>> list = Lists.newArrayList();
      Set<Recipe<?>> set = craftableOnly ? this.craftableRecipes : this.fittingRecipes;
      Iterator var4 = this.recipes.iterator();

      while(var4.hasNext()) {
         Recipe<?> recipe = (Recipe)var4.next();
         if (set.contains(recipe)) {
            list.add(recipe);
         }
      }

      return list;
   }

   public List<Recipe<?>> getRecipes(boolean craftable) {
      List<Recipe<?>> list = Lists.newArrayList();
      Iterator var3 = this.recipes.iterator();

      while(var3.hasNext()) {
         Recipe<?> recipe = (Recipe)var3.next();
         if (this.fittingRecipes.contains(recipe) && this.craftableRecipes.contains(recipe) == craftable) {
            list.add(recipe);
         }
      }

      return list;
   }

   public boolean hasSingleOutput() {
      return this.singleOutput;
   }
}
