package net.minecraft.datafixer.schema;

import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.templates.TypeTemplate;
import java.util.Map;
import java.util.function.Supplier;

public class Schema700 extends Schema {
   public Schema700(int versionKey, Schema parent) {
      super(versionKey, parent);
   }

   protected static void method_5288(Schema schema, Map<String, Supplier<TypeTemplate>> map, String string) {
      schema.register(map, string, () -> {
         return Schema100.targetItems(schema);
      });
   }

   public Map<String, Supplier<TypeTemplate>> registerEntities(Schema schema) {
      Map<String, Supplier<TypeTemplate>> map = super.registerEntities(schema);
      method_5288(schema, map, "ElderGuardian");
      return map;
   }
}
