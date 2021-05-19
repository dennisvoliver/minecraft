package net.minecraft.advancement.criterion;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.JsonObject;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import net.minecraft.advancement.PlayerAdvancementTracker;
import net.minecraft.loot.context.LootContext;
import net.minecraft.predicate.entity.AdvancementEntityPredicateDeserializer;
import net.minecraft.predicate.entity.EntityPredicate;
import net.minecraft.server.network.ServerPlayerEntity;

public abstract class AbstractCriterion<T extends AbstractCriterionConditions> implements Criterion<T> {
   private final Map<PlayerAdvancementTracker, Set<Criterion.ConditionsContainer<T>>> progressions = Maps.newIdentityHashMap();

   public final void beginTrackingCondition(PlayerAdvancementTracker manager, Criterion.ConditionsContainer<T> conditionsContainer) {
      ((Set)this.progressions.computeIfAbsent(manager, (playerAdvancementTracker) -> {
         return Sets.newHashSet();
      })).add(conditionsContainer);
   }

   public final void endTrackingCondition(PlayerAdvancementTracker manager, Criterion.ConditionsContainer<T> conditionsContainer) {
      Set<Criterion.ConditionsContainer<T>> set = (Set)this.progressions.get(manager);
      if (set != null) {
         set.remove(conditionsContainer);
         if (set.isEmpty()) {
            this.progressions.remove(manager);
         }
      }

   }

   public final void endTracking(PlayerAdvancementTracker tracker) {
      this.progressions.remove(tracker);
   }

   protected abstract T conditionsFromJson(JsonObject obj, EntityPredicate.Extended playerPredicate, AdvancementEntityPredicateDeserializer predicateDeserializer);

   public final T conditionsFromJson(JsonObject jsonObject, AdvancementEntityPredicateDeserializer advancementEntityPredicateDeserializer) {
      EntityPredicate.Extended extended = EntityPredicate.Extended.getInJson(jsonObject, "player", advancementEntityPredicateDeserializer);
      return this.conditionsFromJson(jsonObject, extended, advancementEntityPredicateDeserializer);
   }

   protected void test(ServerPlayerEntity player, Predicate<T> tester) {
      PlayerAdvancementTracker playerAdvancementTracker = player.getAdvancementTracker();
      Set<Criterion.ConditionsContainer<T>> set = (Set)this.progressions.get(playerAdvancementTracker);
      if (set != null && !set.isEmpty()) {
         LootContext lootContext = EntityPredicate.createAdvancementEntityLootContext(player, player);
         List<Criterion.ConditionsContainer<T>> list = null;
         Iterator var7 = set.iterator();

         Criterion.ConditionsContainer conditionsContainer2;
         while(var7.hasNext()) {
            conditionsContainer2 = (Criterion.ConditionsContainer)var7.next();
            T abstractCriterionConditions = (AbstractCriterionConditions)conditionsContainer2.getConditions();
            if (abstractCriterionConditions.getPlayerPredicate().test(lootContext) && tester.test(abstractCriterionConditions)) {
               if (list == null) {
                  list = Lists.newArrayList();
               }

               list.add(conditionsContainer2);
            }
         }

         if (list != null) {
            var7 = list.iterator();

            while(var7.hasNext()) {
               conditionsContainer2 = (Criterion.ConditionsContainer)var7.next();
               conditionsContainer2.grant(playerAdvancementTracker);
            }
         }

      }
   }
}
