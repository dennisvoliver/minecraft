package net.minecraft.datafixer.fix;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import net.minecraft.datafixer.TypeReferences;

public class BlockEntityBannerColorFix extends ChoiceFix {
   public BlockEntityBannerColorFix(Schema outputSchema, boolean changesType) {
      super(outputSchema, changesType, "BlockEntityBannerColorFix", TypeReferences.BLOCK_ENTITY, "minecraft:banner");
   }

   public Dynamic<?> fixBannerColor(Dynamic<?> dynamic) {
      dynamic = dynamic.update("Base", (dynamicx) -> {
         return dynamicx.createInt(15 - dynamicx.asInt(0));
      });
      dynamic = dynamic.update("Patterns", (dynamicx) -> {
         DataResult var10000 = dynamicx.asStreamOpt().map((stream) -> {
            return stream.map((dynamic) -> {
               return dynamic.update("Color", (dynamicx) -> {
                  return dynamicx.createInt(15 - dynamicx.asInt(0));
               });
            });
         });
         dynamicx.getClass();
         return (Dynamic)DataFixUtils.orElse(var10000.map(dynamicx::createList).result(), dynamicx);
      });
      return dynamic;
   }

   protected Typed<?> transform(Typed<?> inputType) {
      return inputType.update(DSL.remainderFinder(), this::fixBannerColor);
   }
}
