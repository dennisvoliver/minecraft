package net.minecraft.enchantment;

import net.minecraft.entity.EquipmentSlot;

public class VanishingCurseEnchantment extends Enchantment {
   public VanishingCurseEnchantment(Enchantment.Rarity weight, EquipmentSlot... slotTypes) {
      super(weight, EnchantmentTarget.VANISHABLE, slotTypes);
   }

   public int getMinPower(int level) {
      return 25;
   }

   public int getMaxPower(int level) {
      return 50;
   }

   public int getMaxLevel() {
      return 1;
   }

   public boolean isTreasure() {
      return true;
   }

   public boolean isCursed() {
      return true;
   }
}
