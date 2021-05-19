package net.minecraft.datafixer.schema;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.templates.TypeTemplate;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.datafixer.TypeReferences;

public class Schema2501 extends IdentifierNormalizingSchema {
   public Schema2501(int i, Schema schema) {
      super(i, schema);
   }

   private static void targetRecipeUsedField(Schema schema, Map<String, Supplier<TypeTemplate>> map, String name) {
      schema.register(map, name, () -> {
         return DSL.optionalFields("Items", DSL.list(TypeReferences.ITEM_STACK.in(schema)), "RecipesUsed", DSL.compoundList(TypeReferences.RECIPE.in(schema), DSL.constType(DSL.intType())));
      });
   }

   public Map<String, Supplier<TypeTemplate>> registerBlockEntities(Schema schema) {
      Map<String, Supplier<TypeTemplate>> map = super.registerBlockEntities(schema);
      targetRecipeUsedField(schema, map, "minecraft:furnace");
      targetRecipeUsedField(schema, map, "minecraft:smoker");
      targetRecipeUsedField(schema, map, "minecraft:blast_furnace");
      return map;
   }
}
