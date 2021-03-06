package net.minecraft.datafixer.schema;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.templates.TypeTemplate;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.datafixer.TypeReferences;

public class Schema1451v6 extends IdentifierNormalizingSchema {
   public Schema1451v6(int i, Schema schema) {
      super(i, schema);
   }

   public void registerTypes(Schema schema, Map<String, Supplier<TypeTemplate>> entityTypes, Map<String, Supplier<TypeTemplate>> blockEntityTypes) {
      super.registerTypes(schema, entityTypes, blockEntityTypes);
      Supplier<TypeTemplate> supplier = () -> {
         return DSL.compoundList(TypeReferences.ITEM_NAME.in(schema), DSL.constType(DSL.intType()));
      };
      schema.registerType(false, TypeReferences.STATS, () -> {
         return DSL.optionalFields("stats", DSL.optionalFields("minecraft:mined", DSL.compoundList(TypeReferences.BLOCK_NAME.in(schema), DSL.constType(DSL.intType())), "minecraft:crafted", (TypeTemplate)supplier.get(), "minecraft:used", (TypeTemplate)supplier.get(), "minecraft:broken", (TypeTemplate)supplier.get(), "minecraft:picked_up", (TypeTemplate)supplier.get(), DSL.optionalFields("minecraft:dropped", (TypeTemplate)supplier.get(), "minecraft:killed", DSL.compoundList(TypeReferences.ENTITY_NAME.in(schema), DSL.constType(DSL.intType())), "minecraft:killed_by", DSL.compoundList(TypeReferences.ENTITY_NAME.in(schema), DSL.constType(DSL.intType())), "minecraft:custom", DSL.compoundList(DSL.constType(getIdentifierType()), DSL.constType(DSL.intType())))));
      });
   }
}
