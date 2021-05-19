package net.minecraft.enchantment;

import net.minecraft.entity.EquipmentSlot;

public class InfinityEnchantment extends Enchantment {
   public InfinityEnchantment(Enchantment.Rarity weight, EquipmentSlot... slotTypes) {
      super(weight, EnchantmentTarget.BOW, slotTypes);
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
      return other instanceof MendingEnchantment ? false : super.canAccept(other);
   }
}
