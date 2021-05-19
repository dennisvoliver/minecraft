package net.minecraft.item;

import java.util.Iterator;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentLevelEntry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class EnchantedBookItem extends Item {
   public EnchantedBookItem(Item.Settings settings) {
      super(settings);
   }

   public boolean hasGlint(ItemStack stack) {
      return true;
   }

   public boolean isEnchantable(ItemStack stack) {
      return false;
   }

   public static ListTag getEnchantmentTag(ItemStack stack) {
      CompoundTag compoundTag = stack.getTag();
      return compoundTag != null ? compoundTag.getList("StoredEnchantments", 10) : new ListTag();
   }

   @Environment(EnvType.CLIENT)
   public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
      super.appendTooltip(stack, world, tooltip, context);
      ItemStack.appendEnchantments(tooltip, getEnchantmentTag(stack));
   }

   public static void addEnchantment(ItemStack stack, EnchantmentLevelEntry entry) {
      ListTag listTag = getEnchantmentTag(stack);
      boolean bl = true;
      Identifier identifier = Registry.ENCHANTMENT.getId(entry.enchantment);

      for(int i = 0; i < listTag.size(); ++i) {
         CompoundTag compoundTag = listTag.getCompound(i);
         Identifier identifier2 = Identifier.tryParse(compoundTag.getString("id"));
         if (identifier2 != null && identifier2.equals(identifier)) {
            if (compoundTag.getInt("lvl") < entry.level) {
               compoundTag.putShort("lvl", (short)entry.level);
            }

            bl = false;
            break;
         }
      }

      if (bl) {
         CompoundTag compoundTag2 = new CompoundTag();
         compoundTag2.putString("id", String.valueOf(identifier));
         compoundTag2.putShort("lvl", (short)entry.level);
         listTag.add(compoundTag2);
      }

      stack.getOrCreateTag().put("StoredEnchantments", listTag);
   }

   public static ItemStack forEnchantment(EnchantmentLevelEntry info) {
      ItemStack itemStack = new ItemStack(Items.ENCHANTED_BOOK);
      addEnchantment(itemStack, info);
      return itemStack;
   }

   public void appendStacks(ItemGroup group, DefaultedList<ItemStack> stacks) {
      Iterator var3;
      Enchantment enchantment;
      if (group == ItemGroup.SEARCH) {
         var3 = Registry.ENCHANTMENT.iterator();

         while(true) {
            do {
               if (!var3.hasNext()) {
                  return;
               }

               enchantment = (Enchantment)var3.next();
            } while(enchantment.type == null);

            for(int i = enchantment.getMinLevel(); i <= enchantment.getMaxLevel(); ++i) {
               stacks.add(forEnchantment(new EnchantmentLevelEntry(enchantment, i)));
            }
         }
      } else if (group.getEnchantments().length != 0) {
         var3 = Registry.ENCHANTMENT.iterator();

         while(var3.hasNext()) {
            enchantment = (Enchantment)var3.next();
            if (group.containsEnchantments(enchantment.type)) {
               stacks.add(forEnchantment(new EnchantmentLevelEntry(enchantment, enchantment.getMaxLevel())));
            }
         }
      }

   }
}
