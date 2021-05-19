package net.minecraft.enchantment;

import net.minecraft.entity.EquipmentSlot;

public class MultishotEnchantment extends Enchantment {
   public MultishotEnchantment(Enchantment.Rarity weight, EquipmentSlot... slotTypes) {
      super(weight, EnchantmentTarget.CROSSBOW, slotTypes);
   }

   public int getMinPower(int level) {
      return 20;
   }

   public int getMaxPower(int level) {
      return 50;
   }

   public int getMaxLevel() {
      return 1;
   }

   public boolean canAccept(Enchantment other) {
      return super.canAccept(other) && other != Enchantments.PIERCING;
   }
}
