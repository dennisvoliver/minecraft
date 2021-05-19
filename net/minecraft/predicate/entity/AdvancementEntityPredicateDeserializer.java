package net.minecraft.predicate.entity;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import net.minecraft.loot.LootGsons;
import net.minecraft.loot.LootTableReporter;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.condition.LootConditionManager;
import net.minecraft.loot.context.LootContextType;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class AdvancementEntityPredicateDeserializer {
   private static final Logger LOGGER = LogManager.getLogger();
   private final Identifier advancementId;
   private final LootConditionManager conditionManager;
   private final Gson gson = LootGsons.getConditionGsonBuilder().create();

   public AdvancementEntityPredicateDeserializer(Identifier advancementId, LootConditionManager conditionManager) {
      this.advancementId = advancementId;
      this.conditionManager = conditionManager;
   }

   public final LootCondition[] loadConditions(JsonArray array, String key, LootContextType contextType) {
      LootCondition[] lootConditions = (LootCondition[])this.gson.fromJson((JsonElement)array, (Class)LootCondition[].class);
      LootTableReporter lootTableReporter = new LootTableReporter(contextType, this.conditionManager::get, (identifier) -> {
         return null;
      });
      LootCondition[] var6 = lootConditions;
      int var7 = lootConditions.length;

      for(int var8 = 0; var8 < var7; ++var8) {
         LootCondition lootCondition = var6[var8];
         lootCondition.validate(lootTableReporter);
         lootTableReporter.getMessages().forEach((string2, string3) -> {
            LOGGER.warn((String)"Found validation problem in advancement trigger {}/{}: {}", (Object)key, string2, string3);
         });
      }

      return lootConditions;
   }

   public Identifier getAdvancementId() {
      return this.advancementId;
   }
}
