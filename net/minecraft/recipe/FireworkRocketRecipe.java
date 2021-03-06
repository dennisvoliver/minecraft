package net.minecraft.recipe;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

public class FireworkRocketRecipe extends SpecialCraftingRecipe {
   private static final Ingredient PAPER;
   private static final Ingredient DURATION_MODIFIER;
   private static final Ingredient FIREWORK_STAR;

   public FireworkRocketRecipe(Identifier identifier) {
      super(identifier);
   }

   public boolean matches(CraftingInventory craftingInventory, World world) {
      boolean bl = false;
      int i = 0;

      for(int j = 0; j < craftingInventory.size(); ++j) {
         ItemStack itemStack = craftingInventory.getStack(j);
         if (!itemStack.isEmpty()) {
            if (PAPER.test(itemStack)) {
               if (bl) {
                  return false;
               }

               bl = true;
            } else if (DURATION_MODIFIER.test(itemStack)) {
               ++i;
               if (i > 3) {
                  return false;
               }
            } else if (!FIREWORK_STAR.test(itemStack)) {
               return false;
            }
         }
      }

      return bl && i >= 1;
   }

   public ItemStack craft(CraftingInventory craftingInventory) {
      ItemStack itemStack = new ItemStack(Items.FIREWORK_ROCKET, 3);
      CompoundTag compoundTag = itemStack.getOrCreateSubTag("Fireworks");
      ListTag listTag = new ListTag();
      int i = 0;

      for(int j = 0; j < craftingInventory.size(); ++j) {
         ItemStack itemStack2 = craftingInventory.getStack(j);
         if (!itemStack2.isEmpty()) {
            if (DURATION_MODIFIER.test(itemStack2)) {
               ++i;
            } else if (FIREWORK_STAR.test(itemStack2)) {
               CompoundTag compoundTag2 = itemStack2.getSubTag("Explosion");
               if (compoundTag2 != null) {
                  listTag.add(compoundTag2);
               }
            }
         }
      }

      compoundTag.putByte("Flight", (byte)i);
      if (!listTag.isEmpty()) {
         compoundTag.put("Explosions", listTag);
      }

      return itemStack;
   }

   @Environment(EnvType.CLIENT)
   public boolean fits(int width, int height) {
      return width * height >= 2;
   }

   public ItemStack getOutput() {
      return new ItemStack(Items.FIREWORK_ROCKET);
   }

   public RecipeSerializer<?> getSerializer() {
      return RecipeSerializer.FIREWORK_ROCKET;
   }

   static {
      PAPER = Ingredient.ofItems(Items.PAPER);
      DURATION_MODIFIER = Ingredient.ofItems(Items.GUNPOWDER);
      FIREWORK_STAR = Ingredient.ofItems(Items.FIREWORK_STAR);
   }
}
