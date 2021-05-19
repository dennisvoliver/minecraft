package net.minecraft.datafixer.fix;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;
import it.unimi.dsi.fastutil.shorts.ShortArrayList;
import it.unimi.dsi.fastutil.shorts.ShortList;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import net.minecraft.datafixer.TypeReferences;

public class ChunkToProtoChunkFix extends DataFix {
   public ChunkToProtoChunkFix(Schema outputSchema, boolean changesType) {
      super(outputSchema, changesType);
   }

   public TypeRewriteRule makeRule() {
      Type<?> type = this.getInputSchema().getType(TypeReferences.CHUNK);
      Type<?> type2 = this.getOutputSchema().getType(TypeReferences.CHUNK);
      Type<?> type3 = type.findFieldType("Level");
      Type<?> type4 = type2.findFieldType("Level");
      Type<?> type5 = type3.findFieldType("TileTicks");
      OpticFinder<?> opticFinder = DSL.fieldFinder("Level", type3);
      OpticFinder<?> opticFinder2 = DSL.fieldFinder("TileTicks", type5);
      return TypeRewriteRule.seq(this.fixTypeEverywhereTyped("ChunkToProtoChunkFix", type, this.getOutputSchema().getType(TypeReferences.CHUNK), (typed) -> {
         return typed.updateTyped(opticFinder, type4, (typedx) -> {
            Optional<? extends Stream<? extends Dynamic<?>>> optional = typedx.getOptionalTyped(opticFinder2).flatMap((typed) -> {
               return typed.write().result();
            }).flatMap((dynamicx) -> {
               return dynamicx.asStreamOpt().result();
            });
            Dynamic<?> dynamic = (Dynamic)typedx.get(DSL.remainderFinder());
            boolean bl = dynamic.get("TerrainPopulated").asBoolean(false) && (!dynamic.get("LightPopulated").asNumber().result().isPresent() || dynamic.get("LightPopulated").asBoolean(false));
            dynamic = dynamic.set("Status", dynamic.createString(bl ? "mobs_spawned" : "empty"));
            dynamic = dynamic.set("hasLegacyStructureData", dynamic.createBoolean(true));
            Dynamic dynamic4;
            if (bl) {
               Optional<ByteBuffer> optional2 = dynamic.get("Biomes").asByteBufferOpt().result();
               if (optional2.isPresent()) {
                  ByteBuffer byteBuffer = (ByteBuffer)optional2.get();
                  int[] is = new int[256];

                  for(int i = 0; i < is.length; ++i) {
                     if (i < byteBuffer.capacity()) {
                        is[i] = byteBuffer.get(i) & 255;
                     }
                  }

                  dynamic = dynamic.set("Biomes", dynamic.createIntList(Arrays.stream(is)));
               }

               List<ShortList> list = (List)IntStream.range(0, 16).mapToObj((ix) -> {
                  return new ShortArrayList();
               }).collect(Collectors.toList());
               if (optional.isPresent()) {
                  ((Stream)optional.get()).forEach((dynamicx) -> {
                     int i = dynamicx.get("x").asInt(0);
                     int j = dynamicx.get("y").asInt(0);
                     int k = dynamicx.get("z").asInt(0);
                     short s = method_15675(i, j, k);
                     ((ShortList)list.get(j >> 4)).add(s);
                  });
                  dynamic = dynamic.set("ToBeTicked", dynamic.createList(list.stream().map((shortList) -> {
                     Stream var10001 = shortList.stream();
                     dynamic.getClass();
                     return dynamic.createList(var10001.map(dynamic::createShort));
                  })));
               }

               dynamic4 = (Dynamic)DataFixUtils.orElse(typedx.set(DSL.remainderFinder(), (Object)dynamic).write().result(), dynamic);
            } else {
               dynamic4 = dynamic;
            }

            return (Typed)((Pair)type4.readTyped(dynamic4).result().orElseThrow(() -> {
               return new IllegalStateException("Could not read the new chunk");
            })).getFirst();
         });
      }), this.writeAndRead("Structure biome inject", this.getInputSchema().getType(TypeReferences.STRUCTURE_FEATURE), this.getOutputSchema().getType(TypeReferences.STRUCTURE_FEATURE)));
   }

   private static short method_15675(int i, int j, int k) {
      return (short)(i & 15 | (j & 15) << 4 | (k & 15) << 8);
   }
}
