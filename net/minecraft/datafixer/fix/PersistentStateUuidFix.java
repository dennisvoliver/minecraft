package net.minecraft.datafixer.fix;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import net.minecraft.datafixer.TypeReferences;

public class PersistentStateUuidFix extends AbstractUuidFix {
   public PersistentStateUuidFix(Schema outputSchema) {
      super(outputSchema, TypeReferences.SAVED_DATA);
   }

   protected TypeRewriteRule makeRule() {
      return this.fixTypeEverywhereTyped("SavedDataUUIDFix", this.getInputSchema().getType(this.typeReference), (typed) -> {
         return typed.updateTyped(typed.getType().findField("data"), (typedx) -> {
            return typedx.update(DSL.remainderFinder(), (dynamic) -> {
               return dynamic.update("Raids", (dynamicx) -> {
                  return dynamicx.createList(dynamicx.asStream().map((dynamic) -> {
                     return dynamic.update("HeroesOfTheVillage", (dynamicx) -> {
                        return dynamicx.createList(dynamicx.asStream().map((dynamic) -> {
                           return (Dynamic)createArrayFromMostLeastTags(dynamic, "UUIDMost", "UUIDLeast").orElseGet(() -> {
                              LOGGER.warn("HeroesOfTheVillage contained invalid UUIDs.");
                              return dynamic;
                           });
                        }));
                     });
                  }));
               });
            });
         });
      });
   }
}
