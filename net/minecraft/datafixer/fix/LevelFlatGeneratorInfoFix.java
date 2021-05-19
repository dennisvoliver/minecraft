package net.minecraft.datafixer.fix;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Splitter;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import net.minecraft.datafixer.TypeReferences;
import org.apache.commons.lang3.math.NumberUtils;

public class LevelFlatGeneratorInfoFix extends DataFix {
   private static final Splitter SPLIT_ON_SEMICOLON = Splitter.on(';').limit(5);
   private static final Splitter SPLIT_ON_COMMA = Splitter.on(',');
   private static final Splitter SPLIT_ON_LOWER_X = Splitter.on('x').limit(2);
   private static final Splitter SPLIT_ON_ASTERISK = Splitter.on('*').limit(2);
   private static final Splitter SPLIT_ON_COLON = Splitter.on(':').limit(3);

   public LevelFlatGeneratorInfoFix(Schema outputSchema, boolean changesType) {
      super(outputSchema, changesType);
   }

   public TypeRewriteRule makeRule() {
      return this.fixTypeEverywhereTyped("LevelFlatGeneratorInfoFix", this.getInputSchema().getType(TypeReferences.LEVEL), (typed) -> {
         return typed.update(DSL.remainderFinder(), this::fixGeneratorOptions);
      });
   }

   private Dynamic<?> fixGeneratorOptions(Dynamic<?> dynamic) {
      return dynamic.get("generatorName").asString("").equalsIgnoreCase("flat") ? dynamic.update("generatorOptions", (dynamicx) -> {
         DataResult var10000 = dynamicx.asString().map(this::fixFlatGeneratorOptions);
         dynamicx.getClass();
         return (Dynamic)DataFixUtils.orElse(var10000.map(dynamicx::createString).result(), dynamicx);
      }) : dynamic;
   }

   @VisibleForTesting
   String fixFlatGeneratorOptions(String generatorOptions) {
      if (generatorOptions.isEmpty()) {
         return "minecraft:bedrock,2*minecraft:dirt,minecraft:grass_block;1;village";
      } else {
         Iterator<String> iterator = SPLIT_ON_SEMICOLON.split(generatorOptions).iterator();
         String string = (String)iterator.next();
         int j;
         String string3;
         if (iterator.hasNext()) {
            j = NumberUtils.toInt(string, 0);
            string3 = (String)iterator.next();
         } else {
            j = 0;
            string3 = string;
         }

         if (j >= 0 && j <= 3) {
            StringBuilder stringBuilder = new StringBuilder();
            Splitter splitter = j < 3 ? SPLIT_ON_LOWER_X : SPLIT_ON_ASTERISK;
            stringBuilder.append((String)StreamSupport.stream(SPLIT_ON_COMMA.split(string3).spliterator(), false).map((stringx) -> {
               List<String> list = splitter.splitToList(stringx);
               int k;
               String string3;
               if (list.size() == 2) {
                  k = NumberUtils.toInt((String)list.get(0));
                  string3 = (String)list.get(1);
               } else {
                  k = 1;
                  string3 = (String)list.get(0);
               }

               List<String> list2 = SPLIT_ON_COLON.splitToList(string3);
               int l = ((String)list2.get(0)).equals("minecraft") ? 1 : 0;
               String string4 = (String)list2.get(l);
               int m = j == 3 ? EntityBlockStateFix.getNumericalBlockId("minecraft:" + string4) : NumberUtils.toInt(string4, 0);
               int n = l + 1;
               int o = list2.size() > n ? NumberUtils.toInt((String)list2.get(n), 0) : 0;
               return (k == 1 ? "" : k + "*") + BlockStateFlattening.lookupState(m << 4 | o).get("Name").asString("");
            }).collect(Collectors.joining(",")));

            while(iterator.hasNext()) {
               stringBuilder.append(';').append((String)iterator.next());
            }

            return stringBuilder.toString();
         } else {
            return "minecraft:bedrock,2*minecraft:dirt,minecraft:grass_block;1;village";
         }
      }
   }
}
