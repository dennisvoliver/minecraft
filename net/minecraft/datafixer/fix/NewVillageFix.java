package net.minecraft.datafixer.fix;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.types.templates.CompoundList.CompoundListType;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import net.minecraft.datafixer.TypeReferences;
import net.minecraft.datafixer.schema.IdentifierNormalizingSchema;

public class NewVillageFix extends DataFix {
   public NewVillageFix(Schema outputSchema, boolean changesType) {
      super(outputSchema, changesType);
   }

   protected TypeRewriteRule makeRule() {
      CompoundListType<String, ?> compoundListType = DSL.compoundList(DSL.string(), this.getInputSchema().getType(TypeReferences.STRUCTURE_FEATURE));
      OpticFinder<? extends List<? extends Pair<String, ?>>> opticFinder = compoundListType.finder();
      return this.method_17334(compoundListType);
   }

   private <SF> TypeRewriteRule method_17334(CompoundListType<String, SF> compoundListType) {
      Type<?> type = this.getInputSchema().getType(TypeReferences.CHUNK);
      Type<?> type2 = this.getInputSchema().getType(TypeReferences.STRUCTURE_FEATURE);
      OpticFinder<?> opticFinder = type.findField("Level");
      OpticFinder<?> opticFinder2 = opticFinder.type().findField("Structures");
      OpticFinder<?> opticFinder3 = opticFinder2.type().findField("Starts");
      OpticFinder<List<Pair<String, SF>>> opticFinder4 = compoundListType.finder();
      return TypeRewriteRule.seq(this.fixTypeEverywhereTyped("NewVillageFix", type, (typed) -> {
         return typed.updateTyped(opticFinder, (typedx) -> {
            return typedx.updateTyped(opticFinder2, (typed) -> {
               return typed.updateTyped(opticFinder3, (typedx) -> {
                  return typedx.update(opticFinder4, (list) -> {
                     return (List)list.stream().filter((pair) -> {
                        return !Objects.equals(pair.getFirst(), "Village");
                     }).map((pair) -> {
                        return pair.mapFirst((string) -> {
                           return string.equals("New_Village") ? "Village" : string;
                        });
                     }).collect(Collectors.toList());
                  });
               }).update(DSL.remainderFinder(), (dynamic) -> {
                  return dynamic.update("References", (dynamicx) -> {
                     Optional<? extends Dynamic<?>> optional = dynamicx.get("New_Village").result();
                     return ((Dynamic)DataFixUtils.orElse(optional.map((dynamic2) -> {
                        return dynamicx.remove("New_Village").set("Village", dynamic2);
                     }), dynamicx)).remove("Village");
                  });
               });
            });
         });
      }), this.fixTypeEverywhereTyped("NewVillageStartFix", type2, (typed) -> {
         return typed.update(DSL.remainderFinder(), (dynamic) -> {
            return dynamic.update("id", (dynamicx) -> {
               return Objects.equals(IdentifierNormalizingSchema.normalize(dynamicx.asString("")), "minecraft:new_village") ? dynamicx.createString("minecraft:village") : dynamicx;
            });
         });
      }));
   }
}
