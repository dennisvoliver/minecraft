package net.minecraft.enchantment;

import net.minecraft.entity.EntityGroup;
import net.minecraft.entity.EquipmentSlot;

public class ImpalingEnchantment extends Enchantment {
   public ImpalingEnchantment(Enchantment.Rarity weight, EquipmentSlot... slotTypes) {
      super(weight, EnchantmentTarget.TRIDENT, slotTypes);
   }

   public int getMinPower(int level) {
      return 1 + (level - 1) * 8;
   }

   public int getMaxPower(int level) {
      return this.getMinPower(level) + 20;
   }

   public int getMaxLevel() {
      return 5;
   }

   public float getAttackDamage(int level, EntityGroup group) {
      return group == EntityGroup.AQUATIC ? (float)level * 2.5F : 0.0F;
   }
}
