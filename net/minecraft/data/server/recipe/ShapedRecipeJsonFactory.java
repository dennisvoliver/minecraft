package net.minecraft.data.server.recipe;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.function.Consumer;
import net.minecraft.advancement.Advancement;
import net.minecraft.advancement.AdvancementRewards;
import net.minecraft.advancement.CriterionMerger;
import net.minecraft.advancement.criterion.CriterionConditions;
import net.minecraft.advancement.criterion.RecipeUnlockedCriterion;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.tag.Tag;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

public class ShapedRecipeJsonFactory {
   private static final Logger LOGGER = LogManager.getLogger();
   private final Item output;
   private final int outputCount;
   private final List<String> pattern = Lists.newArrayList();
   private final Map<Character, Ingredient> inputs = Maps.newLinkedHashMap();
   private final Advancement.Task builder = Advancement.Task.create();
   private String group;

   public ShapedRecipeJsonFactory(ItemConvertible output, int outputCount) {
      this.output = output.asItem();
      this.outputCount = outputCount;
   }

   public static ShapedRecipeJsonFactory create(ItemConvertible output) {
      return create(output, 1);
   }

   public static ShapedRecipeJsonFactory create(ItemConvertible output, int outputCount) {
      return new ShapedRecipeJsonFactory(output, outputCount);
   }

   public ShapedRecipeJsonFactory input(Character c, Tag<Item> tag) {
      return this.input(c, Ingredient.fromTag(tag));
   }

   public ShapedRecipeJsonFactory input(Character c, ItemConvertible itemProvider) {
      return this.input(c, Ingredient.ofItems(itemProvider));
   }

   public ShapedRecipeJsonFactory input(Character c, Ingredient ingredient) {
      if (this.inputs.containsKey(c)) {
         throw new IllegalArgumentException("Symbol '" + c + "' is already defined!");
      } else if (c == ' ') {
         throw new IllegalArgumentException("Symbol ' ' (whitespace) is reserved and cannot be defined");
      } else {
         this.inputs.put(c, ingredient);
         return this;
      }
   }

   public ShapedRecipeJsonFactory pattern(String patternStr) {
      if (!this.pattern.isEmpty() && patternStr.length() != ((String)this.pattern.get(0)).length()) {
         throw new IllegalArgumentException("Pattern must be the same width on every line!");
      } else {
         this.pattern.add(patternStr);
         return this;
      }
   }

   public ShapedRecipeJsonFactory criterion(String criterionName, CriterionConditions conditions) {
      this.builder.criterion(criterionName, conditions);
      return this;
   }

   public ShapedRecipeJsonFactory group(String group) {
      this.group = group;
      return this;
   }

   public void offerTo(Consumer<RecipeJsonProvider> exporter) {
      this.offerTo(exporter, Registry.ITEM.getId(this.output));
   }

   public void offerTo(Consumer<RecipeJsonProvider> exporter, String recipeIdStr) {
      Identifier identifier = Registry.ITEM.getId(this.output);
      if ((new Identifier(recipeIdStr)).equals(identifier)) {
         throw new IllegalStateException("Shaped Recipe " + recipeIdStr + " should remove its 'save' argument");
      } else {
         this.offerTo(exporter, new Identifier(recipeIdStr));
      }
   }

   public void offerTo(Consumer<RecipeJsonProvider> exporter, Identifier recipeId) {
      this.validate(recipeId);
      this.builder.parent(new Identifier("recipes/root")).criterion("has_the_recipe", (CriterionConditions)RecipeUnlockedCriterion.create(recipeId)).rewards(AdvancementRewards.Builder.recipe(recipeId)).criteriaMerger(CriterionMerger.OR);
      exporter.accept(new ShapedRecipeJsonFactory.ShapedRecipeJsonProvider(recipeId, this.output, this.outputCount, this.group == null ? "" : this.group, this.pattern, this.inputs, this.builder, new Identifier(recipeId.getNamespace(), "recipes/" + this.output.getGroup().getName() + "/" + recipeId.getPath())));
   }

   private void validate(Identifier recipeId) {
      if (this.pattern.isEmpty()) {
         throw new IllegalStateException("No pattern is defined for shaped recipe " + recipeId + "!");
      } else {
         Set<Character> set = Sets.newHashSet((Iterable)this.inputs.keySet());
         set.remove(' ');
         Iterator var3 = this.pattern.iterator();

         while(var3.hasNext()) {
            String string = (String)var3.next();

            for(int i = 0; i < string.length(); ++i) {
               char c = string.charAt(i);
               if (!this.inputs.containsKey(c) && c != ' ') {
                  throw new IllegalStateException("Pattern in recipe " + recipeId + " uses undefined symbol '" + c + "'");
               }

               set.remove(c);
            }
         }

         if (!set.isEmpty()) {
            throw new IllegalStateException("Ingredients are defined but not used in pattern for recipe " + recipeId);
         } else if (this.pattern.size() == 1 && ((String)this.pattern.get(0)).length() == 1) {
            throw new IllegalStateException("Shaped recipe " + recipeId + " only takes in a single item - should it be a shapeless recipe instead?");
         } else if (this.builder.getCriteria().isEmpty()) {
            throw new IllegalStateException("No way of obtaining recipe " + recipeId);
         }
      }
   }

   class ShapedRecipeJsonProvider implements RecipeJsonProvider {
      private final Identifier recipeId;
      private final Item output;
      private final int resultCount;
      private final String group;
      private final List<String> pattern;
      private final Map<Character, Ingredient> inputs;
      private final Advancement.Task builder;
      private final Identifier advancementId;

      public ShapedRecipeJsonProvider(Identifier recipeId, Item output, int outputCount, String group, List<String> pattern, Map<Character, Ingredient> inputs, Advancement.Task builder, Identifier advancementId) {
         this.recipeId = recipeId;
         this.output = output;
         this.resultCount = outputCount;
         this.group = group;
         this.pattern = pattern;
         this.inputs = inputs;
         this.builder = builder;
         this.advancementId = advancementId;
      }

      public void serialize(JsonObject json) {
         if (!this.group.isEmpty()) {
            json.addProperty("group", this.group);
         }

         JsonArray jsonArray = new JsonArray();
         Iterator var3 = this.pattern.iterator();

         while(var3.hasNext()) {
            String string = (String)var3.next();
            jsonArray.add(string);
         }

         json.add("pattern", jsonArray);
         JsonObject jsonObject = new JsonObject();
         Iterator var7 = this.inputs.entrySet().iterator();

         while(var7.hasNext()) {
            Entry<Character, Ingredient> entry = (Entry)var7.next();
            jsonObject.add(String.valueOf(entry.getKey()), ((Ingredient)entry.getValue()).toJson());
         }

         json.add("key", jsonObject);
         JsonObject jsonObject2 = new JsonObject();
         jsonObject2.addProperty("item", Registry.ITEM.getId(this.output).toString());
         if (this.resultCount > 1) {
            jsonObject2.addProperty("count", (Number)this.resultCount);
         }

         json.add("result", jsonObject2);
      }

      public RecipeSerializer<?> getSerializer() {
         return RecipeSerializer.SHAPED;
      }

      public Identifier getRecipeId() {
         return this.recipeId;
      }

      @Nullable
      public JsonObject toAdvancementJson() {
         return this.builder.toJson();
      }

      @Nullable
      public Identifier getAdvancementId() {
         return this.advancementId;
      }
   }
}
