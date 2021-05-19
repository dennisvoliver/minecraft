package net.minecraft.datafixer.fix;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import java.util.Objects;
import java.util.Optional;
import net.minecraft.datafixer.TypeReferences;

public abstract class PointOfInterestRenameFix extends DataFix {
   public PointOfInterestRenameFix(Schema outputSchema, boolean changesType) {
      super(outputSchema, changesType);
   }

   protected TypeRewriteRule makeRule() {
      Type<Pair<String, Dynamic<?>>> type = DSL.named(TypeReferences.POI_CHUNK.typeName(), DSL.remainderType());
      if (!Objects.equals(type, this.getInputSchema().getType(TypeReferences.POI_CHUNK))) {
         throw new IllegalStateException("Poi type is not what was expected.");
      } else {
         return this.fixTypeEverywhere("POI rename", type, (dynamicOps) -> {
            return (pair) -> {
               return pair.mapSecond(this::method_23299);
            };
         });
      }
   }

   private <T> Dynamic<T> method_23299(Dynamic<T> dynamic) {
      return dynamic.update("Sections", (dynamicx) -> {
         return dynamicx.updateMapValues((pair) -> {
            return pair.mapSecond((dynamic) -> {
               return dynamic.update("Records", (dynamicx) -> {
                  return (Dynamic)DataFixUtils.orElse(this.method_23304(dynamicx), dynamicx);
               });
            });
         });
      });
   }

   private <T> Optional<Dynamic<T>> method_23304(Dynamic<T> dynamic) {
      return dynamic.asStreamOpt().map((stream) -> {
         return dynamic.createList(stream.map((dynamicx) -> {
            return dynamicx.update("type", (dynamic) -> {
               DataResult var10000 = dynamic.asString().map(this::rename);
               dynamic.getClass();
               return (Dynamic)DataFixUtils.orElse(var10000.map(dynamic::createString).result(), dynamic);
            });
         }));
      }).result();
   }

   protected abstract String rename(String input);
}
