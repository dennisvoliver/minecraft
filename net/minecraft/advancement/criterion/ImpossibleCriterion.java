package net.minecraft.advancement.criterion;

import com.google.gson.JsonObject;
import net.minecraft.advancement.PlayerAdvancementTracker;
import net.minecraft.predicate.entity.AdvancementEntityPredicateDeserializer;
import net.minecraft.predicate.entity.AdvancementEntityPredicateSerializer;
import net.minecraft.util.Identifier;

public class ImpossibleCriterion implements Criterion<ImpossibleCriterion.Conditions> {
   private static final Identifier ID = new Identifier("impossible");

   public Identifier getId() {
      return ID;
   }

   public void beginTrackingCondition(PlayerAdvancementTracker manager, Criterion.ConditionsContainer<ImpossibleCriterion.Conditions> conditionsContainer) {
   }

   public void endTrackingCondition(PlayerAdvancementTracker manager, Criterion.ConditionsContainer<ImpossibleCriterion.Conditions> conditionsContainer) {
   }

   public void endTracking(PlayerAdvancementTracker tracker) {
   }

   public ImpossibleCriterion.Conditions conditionsFromJson(JsonObject jsonObject, AdvancementEntityPredicateDeserializer advancementEntityPredicateDeserializer) {
      return new ImpossibleCriterion.Conditions();
   }

   public static class Conditions implements CriterionConditions {
      public Identifier getId() {
         return ImpossibleCriterion.ID;
      }

      public JsonObject toJson(AdvancementEntityPredicateSerializer predicateSerializer) {
         return new JsonObject();
      }
   }
}
