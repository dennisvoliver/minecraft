package net.minecraft.enchantment;

import net.minecraft.entity.EquipmentSlot;

public class PiercingEnchantment extends Enchantment {
   public PiercingEnchantment(Enchantment.Rarity weight, EquipmentSlot... slotTypes) {
      super(weight, EnchantmentTarget.CROSSBOW, slotTypes);
   }

   public int getMinPower(int level) {
      return 1 + (level - 1) * 10;
   }

   public int getMaxPower(int level) {
      return 50;
   }

   public int getMaxLevel() {
      return 4;
   }

   public boolean canAccept(Enchantment other) {
      return super.canAccept(other) && other != Enchantments.MULTISHOT;
   }
}
