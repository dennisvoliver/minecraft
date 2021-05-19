package net.minecraft.datafixer.fix;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import java.util.Optional;
import net.minecraft.datafixer.TypeReferences;

public class VillagerGossipFix extends ChoiceFix {
   public VillagerGossipFix(Schema outputSchema, String choiceType) {
      super(outputSchema, false, "Gossip for for " + choiceType, TypeReferences.ENTITY, choiceType);
   }

   protected Typed<?> transform(Typed<?> inputType) {
      return inputType.update(DSL.remainderFinder(), (dynamic) -> {
         return dynamic.update("Gossips", (dynamicx) -> {
            Optional var10000 = dynamicx.asStreamOpt().result().map((stream) -> {
               return stream.map((dynamic) -> {
                  return (Dynamic)AbstractUuidFix.updateRegularMostLeast(dynamic, "Target", "Target").orElse(dynamic);
               });
            });
            dynamicx.getClass();
            return (Dynamic)DataFixUtils.orElse(var10000.map(dynamicx::createList), dynamicx);
         });
      });
   }
}
