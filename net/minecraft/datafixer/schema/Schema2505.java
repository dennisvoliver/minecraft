package net.minecraft.datafixer.schema;

import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.templates.TypeTemplate;
import java.util.Map;
import java.util.function.Supplier;

public class Schema2505 extends IdentifierNormalizingSchema {
   public Schema2505(int i, Schema schema) {
      super(i, schema);
   }

   protected static void updatePiglinItems(Schema schema, Map<String, Supplier<TypeTemplate>> map, String string) {
      schema.register(map, string, () -> {
         return Schema100.targetItems(schema);
      });
   }

   public Map<String, Supplier<TypeTemplate>> registerEntities(Schema schema) {
      Map<String, Supplier<TypeTemplate>> map = super.registerEntities(schema);
      updatePiglinItems(schema, map, "minecraft:piglin");
      return map;
   }
}
