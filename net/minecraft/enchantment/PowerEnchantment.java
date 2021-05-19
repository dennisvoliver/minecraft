package net.minecraft.enchantment;

import net.minecraft.entity.EquipmentSlot;

public class PowerEnchantment extends Enchantment {
   public PowerEnchantment(Enchantment.Rarity weight, EquipmentSlot... slotTypes) {
      super(weight, EnchantmentTarget.BOW, slotTypes);
   }

   public int getMinPower(int level) {
      return 1 + (level - 1) * 10;
   }

   public int getMaxPower(int level) {
      return this.getMinPower(level) + 15;
   }

   public int getMaxLevel() {
      return 5;
   }
}
