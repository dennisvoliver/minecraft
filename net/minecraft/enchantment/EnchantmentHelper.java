package net.minecraft.enchantment;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Map.Entry;
import java.util.function.Predicate;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityGroup;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.EnchantedBookItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.collection.WeightedPicker;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.registry.Registry;
import org.apache.commons.lang3.mutable.MutableFloat;
import org.apache.commons.lang3.mutable.MutableInt;
import org.jetbrains.annotations.Nullable;

public class EnchantmentHelper {
   /**
    * Gets the level of an enchantment on an item stack.
    */
   public static int getLevel(Enchantment enchantment, ItemStack stack) {
      if (stack.isEmpty()) {
         return 0;
      } else {
         Identifier identifier = Registry.ENCHANTMENT.getId(enchantment);
         ListTag listTag = stack.getEnchantments();

         for(int i = 0; i < listTag.size(); ++i) {
            CompoundTag compoundTag = listTag.getCompound(i);
            Identifier identifier2 = Identifier.tryParse(compoundTag.getString("id"));
            if (identifier2 != null && identifier2.equals(identifier)) {
               return MathHelper.clamp(compoundTag.getInt("lvl"), 0, 255);
            }
         }

         return 0;
      }
   }

   /**
    * Gets the enchantments on an item stack.
    * 
    * <p>For enchanted books, it retrieves from the item stack's stored than
    * regular enchantments.
    * 
    * @see net.minecraft.item.ItemStack#getEnchantments()
    * @see net.minecraft.item.EnchantedBookItem#getEnchantmentTag(net.minecraft.item.ItemStack)
    */
   public static Map<Enchantment, Integer> get(ItemStack stack) {
      ListTag listTag = stack.getItem() == Items.ENCHANTED_BOOK ? EnchantedBookItem.getEnchantmentTag(stack) : stack.getEnchantments();
      return fromTag(listTag);
   }

   /**
    * Loads enchantments from an NBT list.
    */
   public static Map<Enchantment, Integer> fromTag(ListTag tag) {
      Map<Enchantment, Integer> map = Maps.newLinkedHashMap();

      for(int i = 0; i < tag.size(); ++i) {
         CompoundTag compoundTag = tag.getCompound(i);
         Registry.ENCHANTMENT.getOrEmpty(Identifier.tryParse(compoundTag.getString("id"))).ifPresent((enchantment) -> {
            Integer var10000 = (Integer)map.put(enchantment, compoundTag.getInt("lvl"));
         });
      }

      return map;
   }

   /**
    * Sets the enchantments on an item stack.
    * 
    * <p>For enchanted books, it sets the enchantments to the item stack's
    * stored enchantments than regular enchantments.
    * 
    * @see net.minecraft.item.ItemStack#getEnchantments()
    * @see net.minecraft.item.EnchantedBookItem#getEnchantmentTag(net.minecraft.item.ItemStack)
    */
   public static void set(Map<Enchantment, Integer> enchantments, ItemStack stack) {
      ListTag listTag = new ListTag();
      Iterator var3 = enchantments.entrySet().iterator();

      while(var3.hasNext()) {
         Entry<Enchantment, Integer> entry = (Entry)var3.next();
         Enchantment enchantment = (Enchantment)entry.getKey();
         if (enchantment != null) {
            int i = (Integer)entry.getValue();
            CompoundTag compoundTag = new CompoundTag();
            compoundTag.putString("id", String.valueOf(Registry.ENCHANTMENT.getId(enchantment)));
            compoundTag.putShort("lvl", (short)i);
            listTag.add(compoundTag);
            if (stack.getItem() == Items.ENCHANTED_BOOK) {
               EnchantedBookItem.addEnchantment(stack, new EnchantmentLevelEntry(enchantment, i));
            }
         }
      }

      if (listTag.isEmpty()) {
         stack.removeSubTag("Enchantments");
      } else if (stack.getItem() != Items.ENCHANTED_BOOK) {
         stack.putSubTag("Enchantments", listTag);
      }

   }

   private static void forEachEnchantment(EnchantmentHelper.Consumer consumer, ItemStack stack) {
      if (!stack.isEmpty()) {
         ListTag listTag = stack.getEnchantments();

         for(int i = 0; i < listTag.size(); ++i) {
            String string = listTag.getCompound(i).getString("id");
            int j = listTag.getCompound(i).getInt("lvl");
            Registry.ENCHANTMENT.getOrEmpty(Identifier.tryParse(string)).ifPresent((enchantment) -> {
               consumer.accept(enchantment, j);
            });
         }

      }
   }

   private static void forEachEnchantment(EnchantmentHelper.Consumer consumer, Iterable<ItemStack> stacks) {
      Iterator var2 = stacks.iterator();

      while(var2.hasNext()) {
         ItemStack itemStack = (ItemStack)var2.next();
         forEachEnchantment(consumer, itemStack);
      }

   }

   public static int getProtectionAmount(Iterable<ItemStack> equipment, DamageSource source) {
      MutableInt mutableInt = new MutableInt();
      forEachEnchantment((enchantment, level) -> {
         mutableInt.add(enchantment.getProtectionAmount(level, source));
      }, equipment);
      return mutableInt.intValue();
   }

   public static float getAttackDamage(ItemStack stack, EntityGroup group) {
      MutableFloat mutableFloat = new MutableFloat();
      forEachEnchantment((enchantment, level) -> {
         mutableFloat.add(enchantment.getAttackDamage(level, group));
      }, stack);
      return mutableFloat.floatValue();
   }

   public static float getSweepingMultiplier(LivingEntity entity) {
      int i = getEquipmentLevel(Enchantments.SWEEPING, entity);
      return i > 0 ? SweepingEnchantment.getMultiplier(i) : 0.0F;
   }

   public static void onUserDamaged(LivingEntity user, Entity attacker) {
      EnchantmentHelper.Consumer consumer = (enchantment, level) -> {
         enchantment.onUserDamaged(user, attacker, level);
      };
      if (user != null) {
         forEachEnchantment(consumer, user.getItemsEquipped());
      }

      if (attacker instanceof PlayerEntity) {
         forEachEnchantment(consumer, user.getMainHandStack());
      }

   }

   public static void onTargetDamaged(LivingEntity user, Entity target) {
      EnchantmentHelper.Consumer consumer = (enchantment, level) -> {
         enchantment.onTargetDamaged(user, target, level);
      };
      if (user != null) {
         forEachEnchantment(consumer, user.getItemsEquipped());
      }

      if (user instanceof PlayerEntity) {
         forEachEnchantment(consumer, user.getMainHandStack());
      }

   }

   /**
    * Returns the highest level of the passed enchantment in the enchantment's
    * applicable equipment slots' item stacks.
    * 
    * @param enchantment the enchantment
    * @param entity the entity whose equipment slots are checked
    */
   public static int getEquipmentLevel(Enchantment enchantment, LivingEntity entity) {
      Iterable<ItemStack> iterable = enchantment.getEquipment(entity).values();
      if (iterable == null) {
         return 0;
      } else {
         int i = 0;
         Iterator var4 = iterable.iterator();

         while(var4.hasNext()) {
            ItemStack itemStack = (ItemStack)var4.next();
            int j = getLevel(enchantment, itemStack);
            if (j > i) {
               i = j;
            }
         }

         return i;
      }
   }

   public static int getKnockback(LivingEntity entity) {
      return getEquipmentLevel(Enchantments.KNOCKBACK, entity);
   }

   public static int getFireAspect(LivingEntity entity) {
      return getEquipmentLevel(Enchantments.FIRE_ASPECT, entity);
   }

   public static int getRespiration(LivingEntity entity) {
      return getEquipmentLevel(Enchantments.RESPIRATION, entity);
   }

   public static int getDepthStrider(LivingEntity entity) {
      return getEquipmentLevel(Enchantments.DEPTH_STRIDER, entity);
   }

   public static int getEfficiency(LivingEntity entity) {
      return getEquipmentLevel(Enchantments.EFFICIENCY, entity);
   }

   public static int getLuckOfTheSea(ItemStack stack) {
      return getLevel(Enchantments.LUCK_OF_THE_SEA, stack);
   }

   public static int getLure(ItemStack stack) {
      return getLevel(Enchantments.LURE, stack);
   }

   public static int getLooting(LivingEntity entity) {
      return getEquipmentLevel(Enchantments.LOOTING, entity);
   }

   public static boolean hasAquaAffinity(LivingEntity entity) {
      return getEquipmentLevel(Enchantments.AQUA_AFFINITY, entity) > 0;
   }

   public static boolean hasFrostWalker(LivingEntity entity) {
      return getEquipmentLevel(Enchantments.FROST_WALKER, entity) > 0;
   }

   public static boolean hasSoulSpeed(LivingEntity entity) {
      return getEquipmentLevel(Enchantments.SOUL_SPEED, entity) > 0;
   }

   public static boolean hasBindingCurse(ItemStack stack) {
      return getLevel(Enchantments.BINDING_CURSE, stack) > 0;
   }

   public static boolean hasVanishingCurse(ItemStack stack) {
      return getLevel(Enchantments.VANISHING_CURSE, stack) > 0;
   }

   public static int getLoyalty(ItemStack stack) {
      return getLevel(Enchantments.LOYALTY, stack);
   }

   public static int getRiptide(ItemStack stack) {
      return getLevel(Enchantments.RIPTIDE, stack);
   }

   public static boolean hasChanneling(ItemStack stack) {
      return getLevel(Enchantments.CHANNELING, stack) > 0;
   }

   /**
    * Returns a pair of an equipment slot and the item stack in the supplied
    * entity's slot, indicating the item stack has the enchantment supplied.
    * 
    * <p>If multiple equipment slots' item stacks are valid, a random pair is
    * returned.
    * 
    * @param enchantment the enchantment the equipped item stack must have
    * @param entity the entity to choose equipments from
    */
   @Nullable
   public static Entry<EquipmentSlot, ItemStack> chooseEquipmentWith(Enchantment enchantment, LivingEntity entity) {
      return chooseEquipmentWith(enchantment, entity, (stack) -> {
         return true;
      });
   }

   /**
    * Returns a pair of an equipment slot and the item stack in the supplied
    * entity's slot, indicating the item stack has the enchantment supplied
    * and fulfills the extra condition.
    * 
    * <p>If multiple equipment slots' item stacks are valid, a random pair is
    * returned.
    * 
    * @param enchantment the enchantment the equipped item stack must have
    * @param entity the entity to choose equipments from
    * @param condition extra conditions for the item stack to pass for selection
    */
   @Nullable
   public static Entry<EquipmentSlot, ItemStack> chooseEquipmentWith(Enchantment enchantment, LivingEntity entity, Predicate<ItemStack> condition) {
      Map<EquipmentSlot, ItemStack> map = enchantment.getEquipment(entity);
      if (map.isEmpty()) {
         return null;
      } else {
         List<Entry<EquipmentSlot, ItemStack>> list = Lists.newArrayList();
         Iterator var5 = map.entrySet().iterator();

         while(var5.hasNext()) {
            Entry<EquipmentSlot, ItemStack> entry = (Entry)var5.next();
            ItemStack itemStack = (ItemStack)entry.getValue();
            if (!itemStack.isEmpty() && getLevel(enchantment, itemStack) > 0 && condition.test(itemStack)) {
               list.add(entry);
            }
         }

         return list.isEmpty() ? null : (Entry)list.get(entity.getRandom().nextInt(list.size()));
      }
   }

   /**
    * Returns the required experience level for an enchanting option in the
    * enchanting table's screen, or the enchantment screen.
    * 
    * @param random the random, which guarantees consistent results with the same seed
    * @param slotIndex the index of the enchanting option
    * @param bookshelfCount the number of bookshelves
    * @param stack the item stack to enchant
    */
   public static int calculateRequiredExperienceLevel(Random random, int slotIndex, int bookshelfCount, ItemStack stack) {
      Item item = stack.getItem();
      int i = item.getEnchantability();
      if (i <= 0) {
         return 0;
      } else {
         if (bookshelfCount > 15) {
            bookshelfCount = 15;
         }

         int j = random.nextInt(8) + 1 + (bookshelfCount >> 1) + random.nextInt(bookshelfCount + 1);
         if (slotIndex == 0) {
            return Math.max(j / 3, 1);
         } else {
            return slotIndex == 1 ? j * 2 / 3 + 1 : Math.max(j, bookshelfCount * 2);
         }
      }
   }

   /**
    * Enchants the {@code target} item stack and returns it.
    * 
    * @param random the seed
    * @param target the item stack to enchant
    * @param level the experience level
    * @param treasureAllowed whether treasure enchantments may appear
    */
   public static ItemStack enchant(Random random, ItemStack target, int level, boolean treasureAllowed) {
      List<EnchantmentLevelEntry> list = generateEnchantments(random, target, level, treasureAllowed);
      boolean bl = target.getItem() == Items.BOOK;
      if (bl) {
         target = new ItemStack(Items.ENCHANTED_BOOK);
      }

      Iterator var6 = list.iterator();

      while(var6.hasNext()) {
         EnchantmentLevelEntry enchantmentLevelEntry = (EnchantmentLevelEntry)var6.next();
         if (bl) {
            EnchantedBookItem.addEnchantment(target, enchantmentLevelEntry);
         } else {
            target.addEnchantment(enchantmentLevelEntry.enchantment, enchantmentLevelEntry.level);
         }
      }

      return target;
   }

   /**
    * Generate the enchantments for enchanting the {@code stack}.
    */
   public static List<EnchantmentLevelEntry> generateEnchantments(Random random, ItemStack stack, int level, boolean treasureAllowed) {
      List<EnchantmentLevelEntry> list = Lists.newArrayList();
      Item item = stack.getItem();
      int i = item.getEnchantability();
      if (i <= 0) {
         return list;
      } else {
         level += 1 + random.nextInt(i / 4 + 1) + random.nextInt(i / 4 + 1);
         float f = (random.nextFloat() + random.nextFloat() - 1.0F) * 0.15F;
         level = MathHelper.clamp(Math.round((float)level + (float)level * f), 1, Integer.MAX_VALUE);
         List<EnchantmentLevelEntry> list2 = getPossibleEntries(level, stack, treasureAllowed);
         if (!list2.isEmpty()) {
            list.add(WeightedPicker.getRandom(random, list2));

            while(random.nextInt(50) <= level) {
               removeConflicts(list2, (EnchantmentLevelEntry)Util.getLast(list));
               if (list2.isEmpty()) {
                  break;
               }

               list.add(WeightedPicker.getRandom(random, list2));
               level /= 2;
            }
         }

         return list;
      }
   }

   /**
    * Remove entries conflicting with the picked entry from the possible
    * entries.
    * 
    * @param possibleEntries the possible entries
    * @param pickedEntry the picked entry
    */
   public static void removeConflicts(List<EnchantmentLevelEntry> possibleEntries, EnchantmentLevelEntry pickedEntry) {
      Iterator iterator = possibleEntries.iterator();

      while(iterator.hasNext()) {
         if (!pickedEntry.enchantment.canCombine(((EnchantmentLevelEntry)iterator.next()).enchantment)) {
            iterator.remove();
         }
      }

   }

   /**
    * Returns whether the {@code candidate} enchantment is compatible with the
    * {@code existing} enchantments.
    */
   public static boolean isCompatible(Collection<Enchantment> existing, Enchantment candidate) {
      Iterator var2 = existing.iterator();

      Enchantment enchantment;
      do {
         if (!var2.hasNext()) {
            return true;
         }

         enchantment = (Enchantment)var2.next();
      } while(enchantment.canCombine(candidate));

      return false;
   }

   /**
    * Gets all the possible entries for enchanting the {@code stack} at the
    * given {@code power}.
    */
   public static List<EnchantmentLevelEntry> getPossibleEntries(int power, ItemStack stack, boolean treasureAllowed) {
      List<EnchantmentLevelEntry> list = Lists.newArrayList();
      Item item = stack.getItem();
      boolean bl = stack.getItem() == Items.BOOK;
      Iterator var6 = Registry.ENCHANTMENT.iterator();

      while(true) {
         while(true) {
            Enchantment enchantment;
            do {
               do {
                  do {
                     if (!var6.hasNext()) {
                        return list;
                     }

                     enchantment = (Enchantment)var6.next();
                  } while(enchantment.isTreasure() && !treasureAllowed);
               } while(!enchantment.isAvailableForRandomSelection());
            } while(!enchantment.type.isAcceptableItem(item) && !bl);

            for(int i = enchantment.getMaxLevel(); i > enchantment.getMinLevel() - 1; --i) {
               if (power >= enchantment.getMinPower(i) && power <= enchantment.getMaxPower(i)) {
                  list.add(new EnchantmentLevelEntry(enchantment, i));
                  break;
               }
            }
         }
      }
   }

   @FunctionalInterface
   interface Consumer {
      void accept(Enchantment enchantment, int level);
   }
}
