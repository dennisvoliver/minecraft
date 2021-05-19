package net.minecraft.loot;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import net.minecraft.loot.condition.LootConditionManager;
import net.minecraft.loot.context.LootContextType;
import net.minecraft.loot.context.LootContextTypes;
import net.minecraft.resource.JsonDataLoader;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class LootManager extends JsonDataLoader {
   private static final Logger LOGGER = LogManager.getLogger();
   private static final Gson GSON = LootGsons.getTableGsonBuilder().create();
   private Map<Identifier, LootTable> tables = ImmutableMap.of();
   private final LootConditionManager conditionManager;

   public LootManager(LootConditionManager conditionManager) {
      super(GSON, "loot_tables");
      this.conditionManager = conditionManager;
   }

   public LootTable getTable(Identifier id) {
      return (LootTable)this.tables.getOrDefault(id, LootTable.EMPTY);
   }

   protected void apply(Map<Identifier, JsonElement> map, ResourceManager resourceManager, Profiler profiler) {
      Builder<Identifier, LootTable> builder = ImmutableMap.builder();
      JsonElement jsonElement = (JsonElement)map.remove(LootTables.EMPTY);
      if (jsonElement != null) {
         LOGGER.warn((String)"Datapack tried to redefine {} loot table, ignoring", (Object)LootTables.EMPTY);
      }

      map.forEach((identifier, jsonElementx) -> {
         try {
            LootTable lootTable = (LootTable)GSON.fromJson(jsonElementx, LootTable.class);
            builder.put(identifier, lootTable);
         } catch (Exception var4) {
            LOGGER.error((String)"Couldn't parse loot table {}", (Object)identifier, (Object)var4);
         }

      });
      builder.put(LootTables.EMPTY, LootTable.EMPTY);
      ImmutableMap<Identifier, LootTable> immutableMap = builder.build();
      LootContextType var10002 = LootContextTypes.GENERIC;
      Function var10003 = this.conditionManager::get;
      immutableMap.getClass();
      LootTableReporter lootTableReporter = new LootTableReporter(var10002, var10003, immutableMap::get);
      immutableMap.forEach((identifier, lootTable) -> {
         validate(lootTableReporter, identifier, lootTable);
      });
      lootTableReporter.getMessages().forEach((key, value) -> {
         LOGGER.warn("Found validation problem in " + key + ": " + value);
      });
      this.tables = immutableMap;
   }

   public static void validate(LootTableReporter reporter, Identifier id, LootTable table) {
      table.validate(reporter.withContextType(table.getType()).withTable("{" + id + "}", id));
   }

   public static JsonElement toJson(LootTable table) {
      return GSON.toJsonTree(table);
   }

   public Set<Identifier> getTableIds() {
      return this.tables.keySet();
   }
}
