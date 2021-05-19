package net.minecraft.enchantment;

import net.minecraft.entity.EquipmentSlot;

public class DepthStriderEnchantment extends Enchantment {
   public DepthStriderEnchantment(Enchantment.Rarity weight, EquipmentSlot... slotTypes) {
      super(weight, EnchantmentTarget.ARMOR_FEET, slotTypes);
   }

   public int getMinPower(int level) {
      return level * 10;
   }

   public int getMaxPower(int level) {
      return this.getMinPower(level) + 15;
   }

   public int getMaxLevel() {
      return 3;
   }

   public boolean canAccept(Enchantment other) {
      return super.canAccept(other) && other != Enchantments.FROST_WALKER;
   }
}
