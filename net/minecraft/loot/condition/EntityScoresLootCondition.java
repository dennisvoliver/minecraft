package net.minecraft.loot.condition;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import net.minecraft.entity.Entity;
import net.minecraft.loot.UniformLootTableRange;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameter;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.JsonSerializer;

public class EntityScoresLootCondition implements LootCondition {
   private final Map<String, UniformLootTableRange> scores;
   private final LootContext.EntityTarget target;

   private EntityScoresLootCondition(Map<String, UniformLootTableRange> scores, LootContext.EntityTarget target) {
      this.scores = ImmutableMap.copyOf(scores);
      this.target = target;
   }

   public LootConditionType getType() {
      return LootConditionTypes.ENTITY_SCORES;
   }

   public Set<LootContextParameter<?>> getRequiredParameters() {
      return ImmutableSet.of(this.target.getParameter());
   }

   public boolean test(LootContext lootContext) {
      Entity entity = (Entity)lootContext.get(this.target.getParameter());
      if (entity == null) {
         return false;
      } else {
         Scoreboard scoreboard = entity.world.getScoreboard();
         Iterator var4 = this.scores.entrySet().iterator();

         Entry entry;
         do {
            if (!var4.hasNext()) {
               return true;
            }

            entry = (Entry)var4.next();
         } while(this.entityScoreIsInRange(entity, scoreboard, (String)entry.getKey(), (UniformLootTableRange)entry.getValue()));

         return false;
      }
   }

   protected boolean entityScoreIsInRange(Entity entity, Scoreboard scoreboard, String objective, UniformLootTableRange scoreRange) {
      ScoreboardObjective scoreboardObjective = scoreboard.getNullableObjective(objective);
      if (scoreboardObjective == null) {
         return false;
      } else {
         String string = entity.getEntityName();
         return !scoreboard.playerHasObjective(string, scoreboardObjective) ? false : scoreRange.contains(scoreboard.getPlayerScore(string, scoreboardObjective).getScore());
      }
   }

   public static class Serializer implements JsonSerializer<EntityScoresLootCondition> {
      public void toJson(JsonObject jsonObject, EntityScoresLootCondition entityScoresLootCondition, JsonSerializationContext jsonSerializationContext) {
         JsonObject jsonObject2 = new JsonObject();
         Iterator var5 = entityScoresLootCondition.scores.entrySet().iterator();

         while(var5.hasNext()) {
            Entry<String, UniformLootTableRange> entry = (Entry)var5.next();
            jsonObject2.add((String)entry.getKey(), jsonSerializationContext.serialize(entry.getValue()));
         }

         jsonObject.add("scores", jsonObject2);
         jsonObject.add("entity", jsonSerializationContext.serialize(entityScoresLootCondition.target));
      }

      public EntityScoresLootCondition fromJson(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext) {
         Set<Entry<String, JsonElement>> set = JsonHelper.getObject(jsonObject, "scores").entrySet();
         Map<String, UniformLootTableRange> map = Maps.newLinkedHashMap();
         Iterator var5 = set.iterator();

         while(var5.hasNext()) {
            Entry<String, JsonElement> entry = (Entry)var5.next();
            map.put(entry.getKey(), JsonHelper.deserialize((JsonElement)entry.getValue(), "score", jsonDeserializationContext, UniformLootTableRange.class));
         }

         return new EntityScoresLootCondition(map, (LootContext.EntityTarget)JsonHelper.deserialize(jsonObject, "entity", jsonDeserializationContext, LootContext.EntityTarget.class));
      }
   }
}
