package net.minecraft.data.server.recipe;

import com.google.gson.JsonObject;
import java.util.function.Consumer;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.SpecialRecipeSerializer;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

public class ComplexRecipeJsonFactory {
   private final SpecialRecipeSerializer<?> serializer;

   public ComplexRecipeJsonFactory(SpecialRecipeSerializer<?> serializer) {
      this.serializer = serializer;
   }

   public static ComplexRecipeJsonFactory create(SpecialRecipeSerializer<?> serializer) {
      return new ComplexRecipeJsonFactory(serializer);
   }

   public void offerTo(Consumer<RecipeJsonProvider> exporter, final String recipeId) {
      exporter.accept(new RecipeJsonProvider() {
         public void serialize(JsonObject json) {
         }

         public RecipeSerializer<?> getSerializer() {
            return ComplexRecipeJsonFactory.this.serializer;
         }

         public Identifier getRecipeId() {
            return new Identifier(recipeId);
         }

         @Nullable
         public JsonObject toAdvancementJson() {
            return null;
         }

         public Identifier getAdvancementId() {
            return new Identifier("");
         }
      });
   }
}
