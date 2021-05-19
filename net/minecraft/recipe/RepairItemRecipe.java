package net.minecraft.recipe;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.List;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;

public class RepairItemRecipe extends SpecialCraftingRecipe {
   public RepairItemRecipe(Identifier identifier) {
      super(identifier);
   }

   public boolean matches(CraftingInventory craftingInventory, World world) {
      List<ItemStack> list = Lists.newArrayList();

      for(int i = 0; i < craftingInventory.size(); ++i) {
         ItemStack itemStack = craftingInventory.getStack(i);
         if (!itemStack.isEmpty()) {
            list.add(itemStack);
            if (list.size() > 1) {
               ItemStack itemStack2 = (ItemStack)list.get(0);
               if (itemStack.getItem() != itemStack2.getItem() || itemStack2.getCount() != 1 || itemStack.getCount() != 1 || !itemStack2.getItem().isDamageable()) {
                  return false;
               }
            }
         }
      }

      return list.size() == 2;
   }

   public ItemStack craft(CraftingInventory craftingInventory) {
      List<ItemStack> list = Lists.newArrayList();

      ItemStack itemStack4;
      for(int i = 0; i < craftingInventory.size(); ++i) {
         itemStack4 = craftingInventory.getStack(i);
         if (!itemStack4.isEmpty()) {
            list.add(itemStack4);
            if (list.size() > 1) {
               ItemStack itemStack2 = (ItemStack)list.get(0);
               if (itemStack4.getItem() != itemStack2.getItem() || itemStack2.getCount() != 1 || itemStack4.getCount() != 1 || !itemStack2.getItem().isDamageable()) {
                  return ItemStack.EMPTY;
               }
            }
         }
      }

      if (list.size() == 2) {
         ItemStack itemStack3 = (ItemStack)list.get(0);
         itemStack4 = (ItemStack)list.get(1);
         if (itemStack3.getItem() == itemStack4.getItem() && itemStack3.getCount() == 1 && itemStack4.getCount() == 1 && itemStack3.getItem().isDamageable()) {
            Item item = itemStack3.getItem();
            int j = item.getMaxDamage() - itemStack3.getDamage();
            int k = item.getMaxDamage() - itemStack4.getDamage();
            int l = j + k + item.getMaxDamage() * 5 / 100;
            int m = item.getMaxDamage() - l;
            if (m < 0) {
               m = 0;
            }

            ItemStack itemStack5 = new ItemStack(itemStack3.getItem());
            itemStack5.setDamage(m);
            Map<Enchantment, Integer> map = Maps.newHashMap();
            Map<Enchantment, Integer> map2 = EnchantmentHelper.get(itemStack3);
            Map<Enchantment, Integer> map3 = EnchantmentHelper.get(itemStack4);
            Registry.ENCHANTMENT.stream().filter(Enchantment::isCursed).forEach((enchantment) -> {
               int i = Math.max((Integer)map2.getOrDefault(enchantment, 0), (Integer)map3.getOrDefault(enchantment, 0));
               if (i > 0) {
                  map.put(enchantment, i);
               }

            });
            if (!map.isEmpty()) {
               EnchantmentHelper.set(map, itemStack5);
            }

            return itemStack5;
         }
      }

      return ItemStack.EMPTY;
   }

   @Environment(EnvType.CLIENT)
   public boolean fits(int width, int height) {
      return width * height >= 2;
   }

   public RecipeSerializer<?> getSerializer() {
      return RecipeSerializer.REPAIR_ITEM;
   }
}
