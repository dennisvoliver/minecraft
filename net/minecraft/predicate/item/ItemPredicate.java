package net.minecraft.predicate.item;

import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSyntaxException;
import java.util.List;
import java.util.Map;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.EnchantedBookItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionUtil;
import net.minecraft.predicate.NbtPredicate;
import net.minecraft.predicate.NumberRange;
import net.minecraft.tag.ServerTagManagerHolder;
import net.minecraft.tag.Tag;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.registry.Registry;
import org.jetbrains.annotations.Nullable;

public class ItemPredicate {
   public static final ItemPredicate ANY = new ItemPredicate();
   @Nullable
   private final Tag<Item> tag;
   @Nullable
   private final Item item;
   private final NumberRange.IntRange count;
   private final NumberRange.IntRange durability;
   private final EnchantmentPredicate[] enchantments;
   private final EnchantmentPredicate[] storedEnchantments;
   @Nullable
   private final Potion potion;
   private final NbtPredicate nbt;

   public ItemPredicate() {
      this.tag = null;
      this.item = null;
      this.potion = null;
      this.count = NumberRange.IntRange.ANY;
      this.durability = NumberRange.IntRange.ANY;
      this.enchantments = EnchantmentPredicate.ARRAY_OF_ANY;
      this.storedEnchantments = EnchantmentPredicate.ARRAY_OF_ANY;
      this.nbt = NbtPredicate.ANY;
   }

   public ItemPredicate(@Nullable Tag<Item> tag, @Nullable Item item, NumberRange.IntRange count, NumberRange.IntRange durability, EnchantmentPredicate[] enchantments, EnchantmentPredicate[] storedEnchantments, @Nullable Potion potion, NbtPredicate nbt) {
      this.tag = tag;
      this.item = item;
      this.count = count;
      this.durability = durability;
      this.enchantments = enchantments;
      this.storedEnchantments = storedEnchantments;
      this.potion = potion;
      this.nbt = nbt;
   }

   public boolean test(ItemStack stack) {
      if (this == ANY) {
         return true;
      } else if (this.tag != null && !this.tag.contains(stack.getItem())) {
         return false;
      } else if (this.item != null && stack.getItem() != this.item) {
         return false;
      } else if (!this.count.test(stack.getCount())) {
         return false;
      } else if (!this.durability.isDummy() && !stack.isDamageable()) {
         return false;
      } else if (!this.durability.test(stack.getMaxDamage() - stack.getDamage())) {
         return false;
      } else if (!this.nbt.test(stack)) {
         return false;
      } else {
         Map map2;
         EnchantmentPredicate[] var3;
         int var4;
         int var5;
         EnchantmentPredicate enchantmentPredicate2;
         if (this.enchantments.length > 0) {
            map2 = EnchantmentHelper.fromTag(stack.getEnchantments());
            var3 = this.enchantments;
            var4 = var3.length;

            for(var5 = 0; var5 < var4; ++var5) {
               enchantmentPredicate2 = var3[var5];
               if (!enchantmentPredicate2.test(map2)) {
                  return false;
               }
            }
         }

         if (this.storedEnchantments.length > 0) {
            map2 = EnchantmentHelper.fromTag(EnchantedBookItem.getEnchantmentTag(stack));
            var3 = this.storedEnchantments;
            var4 = var3.length;

            for(var5 = 0; var5 < var4; ++var5) {
               enchantmentPredicate2 = var3[var5];
               if (!enchantmentPredicate2.test(map2)) {
                  return false;
               }
            }
         }

         Potion potion = PotionUtil.getPotion(stack);
         return this.potion == null || this.potion == potion;
      }
   }

   public static ItemPredicate fromJson(@Nullable JsonElement el) {
      if (el != null && !el.isJsonNull()) {
         JsonObject jsonObject = JsonHelper.asObject(el, "item");
         NumberRange.IntRange intRange = NumberRange.IntRange.fromJson(jsonObject.get("count"));
         NumberRange.IntRange intRange2 = NumberRange.IntRange.fromJson(jsonObject.get("durability"));
         if (jsonObject.has("data")) {
            throw new JsonParseException("Disallowed data tag found");
         } else {
            NbtPredicate nbtPredicate = NbtPredicate.fromJson(jsonObject.get("nbt"));
            Item item = null;
            if (jsonObject.has("item")) {
               Identifier identifier = new Identifier(JsonHelper.getString(jsonObject, "item"));
               item = (Item)Registry.ITEM.getOrEmpty(identifier).orElseThrow(() -> {
                  return new JsonSyntaxException("Unknown item id '" + identifier + "'");
               });
            }

            Tag<Item> tag = null;
            if (jsonObject.has("tag")) {
               Identifier identifier2 = new Identifier(JsonHelper.getString(jsonObject, "tag"));
               tag = ServerTagManagerHolder.getTagManager().getItems().getTag(identifier2);
               if (tag == null) {
                  throw new JsonSyntaxException("Unknown item tag '" + identifier2 + "'");
               }
            }

            Potion potion = null;
            if (jsonObject.has("potion")) {
               Identifier identifier3 = new Identifier(JsonHelper.getString(jsonObject, "potion"));
               potion = (Potion)Registry.POTION.getOrEmpty(identifier3).orElseThrow(() -> {
                  return new JsonSyntaxException("Unknown potion '" + identifier3 + "'");
               });
            }

            EnchantmentPredicate[] enchantmentPredicates = EnchantmentPredicate.deserializeAll(jsonObject.get("enchantments"));
            EnchantmentPredicate[] enchantmentPredicates2 = EnchantmentPredicate.deserializeAll(jsonObject.get("stored_enchantments"));
            return new ItemPredicate(tag, item, intRange, intRange2, enchantmentPredicates, enchantmentPredicates2, potion, nbtPredicate);
         }
      } else {
         return ANY;
      }
   }

   public JsonElement toJson() {
      if (this == ANY) {
         return JsonNull.INSTANCE;
      } else {
         JsonObject jsonObject = new JsonObject();
         if (this.item != null) {
            jsonObject.addProperty("item", Registry.ITEM.getId(this.item).toString());
         }

         if (this.tag != null) {
            jsonObject.addProperty("tag", ServerTagManagerHolder.getTagManager().getItems().getTagId(this.tag).toString());
         }

         jsonObject.add("count", this.count.toJson());
         jsonObject.add("durability", this.durability.toJson());
         jsonObject.add("nbt", this.nbt.toJson());
         JsonArray jsonArray2;
         EnchantmentPredicate[] var3;
         int var4;
         int var5;
         EnchantmentPredicate enchantmentPredicate2;
         if (this.enchantments.length > 0) {
            jsonArray2 = new JsonArray();
            var3 = this.enchantments;
            var4 = var3.length;

            for(var5 = 0; var5 < var4; ++var5) {
               enchantmentPredicate2 = var3[var5];
               jsonArray2.add(enchantmentPredicate2.serialize());
            }

            jsonObject.add("enchantments", jsonArray2);
         }

         if (this.storedEnchantments.length > 0) {
            jsonArray2 = new JsonArray();
            var3 = this.storedEnchantments;
            var4 = var3.length;

            for(var5 = 0; var5 < var4; ++var5) {
               enchantmentPredicate2 = var3[var5];
               jsonArray2.add(enchantmentPredicate2.serialize());
            }

            jsonObject.add("stored_enchantments", jsonArray2);
         }

         if (this.potion != null) {
            jsonObject.addProperty("potion", Registry.POTION.getId(this.potion).toString());
         }

         return jsonObject;
      }
   }

   public static ItemPredicate[] deserializeAll(@Nullable JsonElement el) {
      if (el != null && !el.isJsonNull()) {
         JsonArray jsonArray = JsonHelper.asArray(el, "items");
         ItemPredicate[] itemPredicates = new ItemPredicate[jsonArray.size()];

         for(int i = 0; i < itemPredicates.length; ++i) {
            itemPredicates[i] = fromJson(jsonArray.get(i));
         }

         return itemPredicates;
      } else {
         return new ItemPredicate[0];
      }
   }

   public static class Builder {
      private final List<EnchantmentPredicate> enchantments = Lists.newArrayList();
      private final List<EnchantmentPredicate> storedEnchantments = Lists.newArrayList();
      @Nullable
      private Item item;
      @Nullable
      private Tag<Item> tag;
      private NumberRange.IntRange count;
      private NumberRange.IntRange durability;
      @Nullable
      private Potion potion;
      private NbtPredicate nbt;

      private Builder() {
         this.count = NumberRange.IntRange.ANY;
         this.durability = NumberRange.IntRange.ANY;
         this.nbt = NbtPredicate.ANY;
      }

      public static ItemPredicate.Builder create() {
         return new ItemPredicate.Builder();
      }

      public ItemPredicate.Builder item(ItemConvertible item) {
         this.item = item.asItem();
         return this;
      }

      public ItemPredicate.Builder tag(Tag<Item> tag) {
         this.tag = tag;
         return this;
      }

      public ItemPredicate.Builder nbt(CompoundTag nbt) {
         this.nbt = new NbtPredicate(nbt);
         return this;
      }

      public ItemPredicate.Builder enchantment(EnchantmentPredicate enchantment) {
         this.enchantments.add(enchantment);
         return this;
      }

      public ItemPredicate build() {
         return new ItemPredicate(this.tag, this.item, this.count, this.durability, (EnchantmentPredicate[])this.enchantments.toArray(EnchantmentPredicate.ARRAY_OF_ANY), (EnchantmentPredicate[])this.storedEnchantments.toArray(EnchantmentPredicate.ARRAY_OF_ANY), this.potion, this.nbt);
      }
   }
}
