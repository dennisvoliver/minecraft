package net.minecraft.enchantment;

import net.minecraft.entity.EquipmentSlot;

public class ChannelingEnchantment extends Enchantment {
   public ChannelingEnchantment(Enchantment.Rarity weight, EquipmentSlot... slotTypes) {
      super(weight, EnchantmentTarget.TRIDENT, slotTypes);
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

   public boolean canAccept(Enchantment other) {
      return super.canAccept(other);
   }
}
