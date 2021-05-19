package net.minecraft.loot.function;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Streams;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.function.UnaryOperator;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameter;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.text.Text;
import net.minecraft.util.JsonHelper;
import org.jetbrains.annotations.Nullable;

public class SetLoreLootFunction extends ConditionalLootFunction {
   private final boolean replace;
   private final List<Text> lore;
   @Nullable
   private final LootContext.EntityTarget entity;

   public SetLoreLootFunction(LootCondition[] conditions, boolean replace, List<Text> lore, @Nullable LootContext.EntityTarget entity) {
      super(conditions);
      this.replace = replace;
      this.lore = ImmutableList.copyOf((Collection)lore);
      this.entity = entity;
   }

   public LootFunctionType getType() {
      return LootFunctionTypes.SET_LORE;
   }

   public Set<LootContextParameter<?>> getRequiredParameters() {
      return this.entity != null ? ImmutableSet.of(this.entity.getParameter()) : ImmutableSet.of();
   }

   public ItemStack process(ItemStack stack, LootContext context) {
      ListTag listTag = this.getLoreForMerge(stack, !this.lore.isEmpty());
      if (listTag != null) {
         if (this.replace) {
            listTag.clear();
         }

         UnaryOperator<Text> unaryOperator = SetNameLootFunction.applySourceEntity(context, this.entity);
         this.lore.stream().map(unaryOperator).map(Text.Serializer::toJson).map(StringTag::of).forEach(listTag::add);
      }

      return stack;
   }

   @Nullable
   private ListTag getLoreForMerge(ItemStack stack, boolean otherLoreExists) {
      CompoundTag compoundTag3;
      if (stack.hasTag()) {
         compoundTag3 = stack.getTag();
      } else {
         if (!otherLoreExists) {
            return null;
         }

         compoundTag3 = new CompoundTag();
         stack.setTag(compoundTag3);
      }

      CompoundTag compoundTag6;
      if (compoundTag3.contains("display", 10)) {
         compoundTag6 = compoundTag3.getCompound("display");
      } else {
         if (!otherLoreExists) {
            return null;
         }

         compoundTag6 = new CompoundTag();
         compoundTag3.put("display", compoundTag6);
      }

      if (compoundTag6.contains("Lore", 9)) {
         return compoundTag6.getList("Lore", 8);
      } else if (otherLoreExists) {
         ListTag listTag = new ListTag();
         compoundTag6.put("Lore", listTag);
         return listTag;
      } else {
         return null;
      }
   }

   public static class Serializer extends ConditionalLootFunction.Serializer<SetLoreLootFunction> {
      public void toJson(JsonObject jsonObject, SetLoreLootFunction setLoreLootFunction, JsonSerializationContext jsonSerializationContext) {
         super.toJson(jsonObject, (ConditionalLootFunction)setLoreLootFunction, jsonSerializationContext);
         jsonObject.addProperty("replace", setLoreLootFunction.replace);
         JsonArray jsonArray = new JsonArray();
         Iterator var5 = setLoreLootFunction.lore.iterator();

         while(var5.hasNext()) {
            Text text = (Text)var5.next();
            jsonArray.add(Text.Serializer.toJsonTree(text));
         }

         jsonObject.add("lore", jsonArray);
         if (setLoreLootFunction.entity != null) {
            jsonObject.add("entity", jsonSerializationContext.serialize(setLoreLootFunction.entity));
         }

      }

      public SetLoreLootFunction fromJson(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext, LootCondition[] lootConditions) {
         boolean bl = JsonHelper.getBoolean(jsonObject, "replace", false);
         List<Text> list = (List)Streams.stream((Iterable)JsonHelper.getArray(jsonObject, "lore")).map(Text.Serializer::fromJson).collect(ImmutableList.toImmutableList());
         LootContext.EntityTarget entityTarget = (LootContext.EntityTarget)JsonHelper.deserialize(jsonObject, "entity", (Object)null, jsonDeserializationContext, LootContext.EntityTarget.class);
         return new SetLoreLootFunction(lootConditions, bl, list, entityTarget);
      }
   }
}
