package net.minecraft.datafixer.fix;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.serialization.Dynamic;
import net.minecraft.datafixer.TypeReferences;

public class HangingEntityFix extends DataFix {
   private static final int[][] OFFSETS = new int[][]{{0, 0, 1}, {-1, 0, 0}, {0, 0, -1}, {1, 0, 0}};

   public HangingEntityFix(Schema outputSchema, boolean changesType) {
      super(outputSchema, changesType);
   }

   private Dynamic<?> fixDecorationPosition(Dynamic<?> dynamic, boolean isPainting, boolean isItemFrame) {
      if ((isPainting || isItemFrame) && !dynamic.get("Facing").asNumber().result().isPresent()) {
         int j;
         if (dynamic.get("Direction").asNumber().result().isPresent()) {
            j = dynamic.get("Direction").asByte((byte)0) % OFFSETS.length;
            int[] is = OFFSETS[j];
            dynamic = dynamic.set("TileX", dynamic.createInt(dynamic.get("TileX").asInt(0) + is[0]));
            dynamic = dynamic.set("TileY", dynamic.createInt(dynamic.get("TileY").asInt(0) + is[1]));
            dynamic = dynamic.set("TileZ", dynamic.createInt(dynamic.get("TileZ").asInt(0) + is[2]));
            dynamic = dynamic.remove("Direction");
            if (isItemFrame && dynamic.get("ItemRotation").asNumber().result().isPresent()) {
               dynamic = dynamic.set("ItemRotation", dynamic.createByte((byte)(dynamic.get("ItemRotation").asByte((byte)0) * 2)));
            }
         } else {
            j = dynamic.get("Dir").asByte((byte)0) % OFFSETS.length;
            dynamic = dynamic.remove("Dir");
         }

         dynamic = dynamic.set("Facing", dynamic.createByte((byte)j));
      }

      return dynamic;
   }

   public TypeRewriteRule makeRule() {
      Type<?> type = this.getInputSchema().getChoiceType(TypeReferences.ENTITY, "Painting");
      OpticFinder<?> opticFinder = DSL.namedChoice("Painting", type);
      Type<?> type2 = this.getInputSchema().getChoiceType(TypeReferences.ENTITY, "ItemFrame");
      OpticFinder<?> opticFinder2 = DSL.namedChoice("ItemFrame", type2);
      Type<?> type3 = this.getInputSchema().getType(TypeReferences.ENTITY);
      TypeRewriteRule typeRewriteRule = this.fixTypeEverywhereTyped("EntityPaintingFix", type3, (typed) -> {
         return typed.updateTyped(opticFinder, type, (typedx) -> {
            return typedx.update(DSL.remainderFinder(), (dynamic) -> {
               return this.fixDecorationPosition(dynamic, true, false);
            });
         });
      });
      TypeRewriteRule typeRewriteRule2 = this.fixTypeEverywhereTyped("EntityItemFrameFix", type3, (typed) -> {
         return typed.updateTyped(opticFinder2, type2, (typedx) -> {
            return typedx.update(DSL.remainderFinder(), (dynamic) -> {
               return this.fixDecorationPosition(dynamic, false, true);
            });
         });
      });
      return TypeRewriteRule.seq(typeRewriteRule, typeRewriteRule2);
   }
}
