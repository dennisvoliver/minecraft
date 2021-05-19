package net.minecraft.enchantment;

import net.minecraft.entity.EquipmentSlot;

public class AquaAffinityEnchantment extends Enchantment {
   public AquaAffinityEnchantment(Enchantment.Rarity weight, EquipmentSlot... slotTypes) {
      super(weight, EnchantmentTarget.ARMOR_HEAD, slotTypes);
   }

   public int getMinPower(int level) {
      return 1;
   }

   public int getMaxPower(int level) {
      return this.getMinPower(level) + 40;
   }

   public int getMaxLevel() {
      return 1;
   }
}
