package net.minecraft.datafixer.fix;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import net.minecraft.datafixer.TypeReferences;

public class WorldUuidFix extends AbstractUuidFix {
   public WorldUuidFix(Schema outputSchema) {
      super(outputSchema, TypeReferences.LEVEL);
   }

   protected TypeRewriteRule makeRule() {
      return this.fixTypeEverywhereTyped("LevelUUIDFix", this.getInputSchema().getType(this.typeReference), (typed) -> {
         return typed.updateTyped(DSL.remainderFinder(), (typedx) -> {
            return typedx.update(DSL.remainderFinder(), (dynamic) -> {
               dynamic = this.method_26061(dynamic);
               dynamic = this.method_26060(dynamic);
               dynamic = this.method_26057(dynamic);
               return dynamic;
            });
         });
      });
   }

   private Dynamic<?> method_26057(Dynamic<?> dynamic) {
      return (Dynamic)updateStringUuid(dynamic, "WanderingTraderId", "WanderingTraderId").orElse(dynamic);
   }

   private Dynamic<?> method_26060(Dynamic<?> dynamic) {
      return dynamic.update("DimensionData", (dynamicx) -> {
         return dynamicx.updateMapValues((pair) -> {
            return pair.mapSecond((dynamic) -> {
               return dynamic.update("DragonFight", (dynamicx) -> {
                  return (Dynamic)updateRegularMostLeast(dynamicx, "DragonUUID", "Dragon").orElse(dynamicx);
               });
            });
         });
      });
   }

   private Dynamic<?> method_26061(Dynamic<?> dynamic) {
      return dynamic.update("CustomBossEvents", (dynamicx) -> {
         return dynamicx.updateMapValues((pair) -> {
            return pair.mapSecond((dynamic) -> {
               return dynamic.update("Players", (dynamic2) -> {
                  return dynamic.createList(dynamic2.asStream().map((dynamicx) -> {
                     return (Dynamic)createArrayFromCompoundUuid(dynamicx).orElseGet(() -> {
                        LOGGER.warn("CustomBossEvents contains invalid UUIDs.");
                        return dynamicx;
                     });
                  }));
               });
            });
         });
      });
   }
}
