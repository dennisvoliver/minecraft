package net.minecraft.advancement.criterion;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import net.minecraft.potion.Potion;
import net.minecraft.predicate.entity.AdvancementEntityPredicateDeserializer;
import net.minecraft.predicate.entity.AdvancementEntityPredicateSerializer;
import net.minecraft.predicate.entity.EntityPredicate;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.registry.Registry;
import org.jetbrains.annotations.Nullable;

public class BrewedPotionCriterion extends AbstractCriterion<BrewedPotionCriterion.Conditions> {
   private static final Identifier ID = new Identifier("brewed_potion");

   public Identifier getId() {
      return ID;
   }

   public BrewedPotionCriterion.Conditions conditionsFromJson(JsonObject jsonObject, EntityPredicate.Extended extended, AdvancementEntityPredicateDeserializer advancementEntityPredicateDeserializer) {
      Potion potion = null;
      if (jsonObject.has("potion")) {
         Identifier identifier = new Identifier(JsonHelper.getString(jsonObject, "potion"));
         potion = (Potion)Registry.POTION.getOrEmpty(identifier).orElseThrow(() -> {
            return new JsonSyntaxException("Unknown potion '" + identifier + "'");
         });
      }

      return new BrewedPotionCriterion.Conditions(extended, potion);
   }

   public void trigger(ServerPlayerEntity player, Potion potion) {
      this.test(player, (conditions) -> {
         return conditions.matches(potion);
      });
   }

   public static class Conditions extends AbstractCriterionConditions {
      private final Potion potion;

      public Conditions(EntityPredicate.Extended player, @Nullable Potion potion) {
         super(BrewedPotionCriterion.ID, player);
         this.potion = potion;
      }

      public static BrewedPotionCriterion.Conditions any() {
         return new BrewedPotionCriterion.Conditions(EntityPredicate.Extended.EMPTY, (Potion)null);
      }

      public boolean matches(Potion potion) {
         return this.potion == null || this.potion == potion;
      }

      public JsonObject toJson(AdvancementEntityPredicateSerializer predicateSerializer) {
         JsonObject jsonObject = super.toJson(predicateSerializer);
         if (this.potion != null) {
            jsonObject.addProperty("potion", Registry.POTION.getId(this.potion).toString());
         }

         return jsonObject;
      }
   }
}
