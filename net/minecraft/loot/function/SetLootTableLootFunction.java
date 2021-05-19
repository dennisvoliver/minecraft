package net.minecraft.loot.function;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.LootTableReporter;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.context.LootContext;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;

public class SetLootTableLootFunction extends ConditionalLootFunction {
   private final Identifier id;
   private final long seed;

   private SetLootTableLootFunction(LootCondition[] conditions, Identifier id, long seed) {
      super(conditions);
      this.id = id;
      this.seed = seed;
   }

   public LootFunctionType getType() {
      return LootFunctionTypes.SET_LOOT_TABLE;
   }

   public ItemStack process(ItemStack stack, LootContext context) {
      if (stack.isEmpty()) {
         return stack;
      } else {
         CompoundTag compoundTag = new CompoundTag();
         compoundTag.putString("LootTable", this.id.toString());
         if (this.seed != 0L) {
            compoundTag.putLong("LootTableSeed", this.seed);
         }

         stack.getOrCreateTag().put("BlockEntityTag", compoundTag);
         return stack;
      }
   }

   public void validate(LootTableReporter reporter) {
      if (reporter.hasTable(this.id)) {
         reporter.report("Table " + this.id + " is recursively called");
      } else {
         super.validate(reporter);
         LootTable lootTable = reporter.getTable(this.id);
         if (lootTable == null) {
            reporter.report("Unknown loot table called " + this.id);
         } else {
            lootTable.validate(reporter.withTable("->{" + this.id + "}", this.id));
         }

      }
   }

   public static class Serializer extends ConditionalLootFunction.Serializer<SetLootTableLootFunction> {
      public void toJson(JsonObject jsonObject, SetLootTableLootFunction setLootTableLootFunction, JsonSerializationContext jsonSerializationContext) {
         super.toJson(jsonObject, (ConditionalLootFunction)setLootTableLootFunction, jsonSerializationContext);
         jsonObject.addProperty("name", setLootTableLootFunction.id.toString());
         if (setLootTableLootFunction.seed != 0L) {
            jsonObject.addProperty("seed", (Number)setLootTableLootFunction.seed);
         }

      }

      public SetLootTableLootFunction fromJson(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext, LootCondition[] lootConditions) {
         Identifier identifier = new Identifier(JsonHelper.getString(jsonObject, "name"));
         long l = JsonHelper.getLong(jsonObject, "seed", 0L);
         return new SetLootTableLootFunction(lootConditions, identifier, l);
      }
   }
}
