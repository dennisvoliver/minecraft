package net.minecraft.enchantment;

import net.minecraft.entity.EquipmentSlot;

public class SilkTouchEnchantment extends Enchantment {
   protected SilkTouchEnchantment(Enchantment.Rarity weight, EquipmentSlot... slotTypes) {
      super(weight, EnchantmentTarget.DIGGER, slotTypes);
   }

   public int getMinPower(int level) {
      return 15;
   }

   public int getMaxPower(int level) {
      return super.getMinPower(level) + 50;
   }

   public int getMaxLevel() {
      return 1;
   }

   public boolean canAccept(Enchantment other) {
      return super.canAccept(other) && other != Enchantments.FORTUNE;
   }
}
