package net.minecraft.enchantment;

import net.minecraft.entity.EquipmentSlot;

public class SoulSpeedEnchantment extends Enchantment {
   public SoulSpeedEnchantment(Enchantment.Rarity weight, EquipmentSlot... slotTypes) {
      super(weight, EnchantmentTarget.ARMOR_FEET, slotTypes);
   }

   public int getMinPower(int level) {
      return level * 10;
   }

   public int getMaxPower(int level) {
      return this.getMinPower(level) + 15;
   }

   public boolean isTreasure() {
      return true;
   }

   public boolean isAvailableForEnchantedBookOffer() {
      return false;
   }

   public boolean isAvailableForRandomSelection() {
      return false;
   }

   public int getMaxLevel() {
      return 3;
   }
}
