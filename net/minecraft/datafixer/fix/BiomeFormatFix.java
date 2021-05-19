package net.minecraft.datafixer.fix;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.IntStream;
import net.minecraft.datafixer.TypeReferences;

public class BiomeFormatFix extends DataFix {
   public BiomeFormatFix(Schema outputSchema, boolean changesType) {
      super(outputSchema, changesType);
   }

   protected TypeRewriteRule makeRule() {
      Type<?> type = this.getInputSchema().getType(TypeReferences.CHUNK);
      OpticFinder<?> opticFinder = type.findField("Level");
      return this.fixTypeEverywhereTyped("Leaves fix", type, (typed) -> {
         return typed.updateTyped(opticFinder, (typedx) -> {
            return typedx.update(DSL.remainderFinder(), (dynamic) -> {
               Optional<IntStream> optional = dynamic.get("Biomes").asIntStreamOpt().result();
               if (!optional.isPresent()) {
                  return dynamic;
               } else {
                  int[] is = ((IntStream)optional.get()).toArray();
                  int[] js = new int[1024];

                  int n;
                  for(n = 0; n < 4; ++n) {
                     for(int j = 0; j < 4; ++j) {
                        int k = (j << 2) + 2;
                        int l = (n << 2) + 2;
                        int m = l << 4 | k;
                        js[n << 2 | j] = m < is.length ? is[m] : -1;
                     }
                  }

                  for(n = 1; n < 64; ++n) {
                     System.arraycopy(js, 0, js, n * 16, 16);
                  }

                  return dynamic.set("Biomes", dynamic.createIntList(Arrays.stream(js)));
               }
            });
         });
      });
   }
}
