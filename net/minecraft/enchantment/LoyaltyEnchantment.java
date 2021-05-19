package net.minecraft.enchantment;

import net.minecraft.entity.EquipmentSlot;

public class LoyaltyEnchantment extends Enchantment {
   public LoyaltyEnchantment(Enchantment.Rarity weight, EquipmentSlot... slotTypes) {
      super(weight, EnchantmentTarget.TRIDENT, slotTypes);
   }

   public int getMinPower(int level) {
      return 5 + level * 7;
   }

   public int getMaxPower(int level) {
      return 50;
   }

   public int getMaxLevel() {
      return 3;
   }

   public boolean canAccept(Enchantment other) {
      return super.canAccept(other);
   }
}
