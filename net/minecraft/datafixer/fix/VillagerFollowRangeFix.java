package net.minecraft.datafixer.fix;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import net.minecraft.datafixer.TypeReferences;

public class VillagerFollowRangeFix extends ChoiceFix {
   public VillagerFollowRangeFix(Schema schema) {
      super(schema, false, "Villager Follow Range Fix", TypeReferences.ENTITY, "minecraft:villager");
   }

   protected Typed<?> transform(Typed<?> inputType) {
      return inputType.update(DSL.remainderFinder(), VillagerFollowRangeFix::method_27914);
   }

   private static Dynamic<?> method_27914(Dynamic<?> dynamic) {
      return dynamic.update("Attributes", (dynamic2) -> {
         return dynamic.createList(dynamic2.asStream().map((dynamicx) -> {
            return dynamicx.get("Name").asString("").equals("generic.follow_range") && dynamicx.get("Base").asDouble(0.0D) == 16.0D ? dynamicx.set("Base", dynamicx.createDouble(48.0D)) : dynamicx;
         }));
      });
   }
}
