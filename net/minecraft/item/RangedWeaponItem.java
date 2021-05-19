package net.minecraft.item;

import java.util.function.Predicate;
import net.minecraft.entity.LivingEntity;
import net.minecraft.tag.ItemTags;
import net.minecraft.tag.Tag;
import net.minecraft.util.Hand;

public abstract class RangedWeaponItem extends Item {
   public static final Predicate<ItemStack> BOW_PROJECTILES = (stack) -> {
      return stack.getItem().isIn((Tag)ItemTags.ARROWS);
   };
   public static final Predicate<ItemStack> CROSSBOW_HELD_PROJECTILES;

   public RangedWeaponItem(Item.Settings settings) {
      super(settings);
   }

   public Predicate<ItemStack> getHeldProjectiles() {
      return this.getProjectiles();
   }

   public abstract Predicate<ItemStack> getProjectiles();

   public static ItemStack getHeldProjectile(LivingEntity entity, Predicate<ItemStack> predicate) {
      if (predicate.test(entity.getStackInHand(Hand.OFF_HAND))) {
         return entity.getStackInHand(Hand.OFF_HAND);
      } else {
         return predicate.test(entity.getStackInHand(Hand.MAIN_HAND)) ? entity.getStackInHand(Hand.MAIN_HAND) : ItemStack.EMPTY;
      }
   }

   public int getEnchantability() {
      return 1;
   }

   public abstract int getRange();

   static {
      CROSSBOW_HELD_PROJECTILES = BOW_PROJECTILES.or((stack) -> {
         return stack.getItem() == Items.FIREWORK_ROCKET;
      });
   }
}
