package net.minecraft.loot.condition;

import com.google.common.collect.Lists;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import java.util.List;
import java.util.function.Predicate;
import net.minecraft.loot.LootTableReporter;
import net.minecraft.loot.context.LootContext;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.JsonSerializer;

public class AlternativeLootCondition implements LootCondition {
   private final LootCondition[] terms;
   private final Predicate<LootContext> predicate;

   private AlternativeLootCondition(LootCondition[] terms) {
      this.terms = terms;
      this.predicate = LootConditionTypes.joinOr(terms);
   }

   public LootConditionType getType() {
      return LootConditionTypes.ALTERNATIVE;
   }

   public final boolean test(LootContext lootContext) {
      return this.predicate.test(lootContext);
   }

   public void validate(LootTableReporter reporter) {
      LootCondition.super.validate(reporter);

      for(int i = 0; i < this.terms.length; ++i) {
         this.terms[i].validate(reporter.makeChild(".term[" + i + "]"));
      }

   }

   public static AlternativeLootCondition.Builder builder(LootCondition.Builder... terms) {
      return new AlternativeLootCondition.Builder(terms);
   }

   public static class Serializer implements JsonSerializer<AlternativeLootCondition> {
      public void toJson(JsonObject jsonObject, AlternativeLootCondition alternativeLootCondition, JsonSerializationContext jsonSerializationContext) {
         jsonObject.add("terms", jsonSerializationContext.serialize(alternativeLootCondition.terms));
      }

      public AlternativeLootCondition fromJson(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext) {
         LootCondition[] lootConditions = (LootCondition[])JsonHelper.deserialize(jsonObject, "terms", jsonDeserializationContext, LootCondition[].class);
         return new AlternativeLootCondition(lootConditions);
      }
   }

   public static class Builder implements LootCondition.Builder {
      private final List<LootCondition> terms = Lists.newArrayList();

      public Builder(LootCondition.Builder... terms) {
         LootCondition.Builder[] var2 = terms;
         int var3 = terms.length;

         for(int var4 = 0; var4 < var3; ++var4) {
            LootCondition.Builder builder = var2[var4];
            this.terms.add(builder.build());
         }

      }

      public AlternativeLootCondition.Builder or(LootCondition.Builder condition) {
         this.terms.add(condition.build());
         return this;
      }

      public LootCondition build() {
         return new AlternativeLootCondition((LootCondition[])this.terms.toArray(new LootCondition[0]));
      }
   }
}
