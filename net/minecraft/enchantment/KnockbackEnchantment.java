package net.minecraft.enchantment;

import net.minecraft.entity.EquipmentSlot;

public class KnockbackEnchantment extends Enchantment {
   protected KnockbackEnchantment(Enchantment.Rarity weight, EquipmentSlot... slot) {
      super(weight, EnchantmentTarget.WEAPON, slot);
   }

   public int getMinPower(int level) {
      return 5 + 20 * (level - 1);
   }

   public int getMaxPower(int level) {
      return super.getMinPower(level) + 50;
   }

   public int getMaxLevel() {
      return 2;
   }
}
