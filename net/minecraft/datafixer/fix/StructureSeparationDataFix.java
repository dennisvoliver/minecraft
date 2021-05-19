package net.minecraft.datafixer.fix;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.collect.ImmutableMap.Builder;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.util.Function3;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicLike;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.OptionalDynamic;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.datafixer.TypeReferences;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.apache.commons.lang3.mutable.MutableInt;

public class StructureSeparationDataFix extends DataFix {
   private static final ImmutableMap<String, StructureSeparationDataFix.Information> STRUCTURE_SPACING = ImmutableMap.builder().put("minecraft:village", new StructureSeparationDataFix.Information(32, 8, 10387312)).put("minecraft:desert_pyramid", new StructureSeparationDataFix.Information(32, 8, 14357617)).put("minecraft:igloo", new StructureSeparationDataFix.Information(32, 8, 14357618)).put("minecraft:jungle_pyramid", new StructureSeparationDataFix.Information(32, 8, 14357619)).put("minecraft:swamp_hut", new StructureSeparationDataFix.Information(32, 8, 14357620)).put("minecraft:pillager_outpost", new StructureSeparationDataFix.Information(32, 8, 165745296)).put("minecraft:monument", new StructureSeparationDataFix.Information(32, 5, 10387313)).put("minecraft:endcity", new StructureSeparationDataFix.Information(20, 11, 10387313)).put("minecraft:mansion", new StructureSeparationDataFix.Information(80, 20, 10387319)).build();

   public StructureSeparationDataFix(Schema schema) {
      super(schema, true);
   }

   protected TypeRewriteRule makeRule() {
      return this.fixTypeEverywhereTyped("WorldGenSettings building", this.getInputSchema().getType(TypeReferences.CHUNK_GENERATOR_SETTINGS), (typed) -> {
         return typed.update(DSL.remainderFinder(), StructureSeparationDataFix::method_28271);
      });
   }

   private static <T> Dynamic<T> method_28268(long l, DynamicLike<T> dynamicLike, Dynamic<T> dynamic, Dynamic<T> dynamic2) {
      return dynamicLike.createMap(ImmutableMap.of(dynamicLike.createString("type"), dynamicLike.createString("minecraft:noise"), dynamicLike.createString("biome_source"), dynamic2, dynamicLike.createString("seed"), dynamicLike.createLong(l), dynamicLike.createString("settings"), dynamic));
   }

   private static <T> Dynamic<T> method_28272(Dynamic<T> dynamic, long l, boolean bl, boolean bl2) {
      Builder<Dynamic<T>, Dynamic<T>> builder = ImmutableMap.builder().put(dynamic.createString("type"), dynamic.createString("minecraft:vanilla_layered")).put(dynamic.createString("seed"), dynamic.createLong(l)).put(dynamic.createString("large_biomes"), dynamic.createBoolean(bl2));
      if (bl) {
         builder.put(dynamic.createString("legacy_biome_init_layer"), dynamic.createBoolean(bl));
      }

      return dynamic.createMap(builder.build());
   }

   private static <T> Dynamic<T> method_28271(Dynamic<T> dynamic) {
      DynamicOps<T> dynamicOps = dynamic.getOps();
      long l = dynamic.get("RandomSeed").asLong(0L);
      Optional<String> optional = dynamic.get("generatorName").asString().map((stringx) -> {
         return stringx.toLowerCase(Locale.ROOT);
      }).result();
      Optional<String> optional2 = (Optional)dynamic.get("legacy_custom_options").asString().result().map(Optional::of).orElseGet(() -> {
         return optional.equals(Optional.of("customized")) ? dynamic.get("generatorOptions").asString().result() : Optional.empty();
      });
      boolean bl = false;
      Dynamic dynamic13;
      if (optional.equals(Optional.of("customized"))) {
         dynamic13 = method_29916(dynamic, l);
      } else if (!optional.isPresent()) {
         dynamic13 = method_29916(dynamic, l);
      } else {
         String var8 = (String)optional.get();
         byte var9 = -1;
         switch(var8.hashCode()) {
         case -1378118590:
            if (var8.equals("buffet")) {
               var9 = 2;
            }
            break;
         case 3145593:
            if (var8.equals("flat")) {
               var9 = 0;
            }
            break;
         case 1045526590:
            if (var8.equals("debug_all_block_states")) {
               var9 = 1;
            }
         }

         switch(var9) {
         case 0:
            OptionalDynamic<T> optionalDynamic = dynamic.get("generatorOptions");
            Map<Dynamic<T>, Dynamic<T>> map = method_28275(dynamicOps, optionalDynamic);
            dynamic13 = dynamic.createMap(ImmutableMap.of(dynamic.createString("type"), dynamic.createString("minecraft:flat"), dynamic.createString("settings"), dynamic.createMap(ImmutableMap.of(dynamic.createString("structures"), dynamic.createMap(map), dynamic.createString("layers"), optionalDynamic.get("layers").result().orElseGet(() -> {
               return dynamic.createList(Stream.of(dynamic.createMap(ImmutableMap.of(dynamic.createString("height"), dynamic.createInt(1), dynamic.createString("block"), dynamic.createString("minecraft:bedrock"))), dynamic.createMap(ImmutableMap.of(dynamic.createString("height"), dynamic.createInt(2), dynamic.createString("block"), dynamic.createString("minecraft:dirt"))), dynamic.createMap(ImmutableMap.of(dynamic.createString("height"), dynamic.createInt(1), dynamic.createString("block"), dynamic.createString("minecraft:grass_block")))));
            }), dynamic.createString("biome"), dynamic.createString(optionalDynamic.get("biome").asString("minecraft:plains"))))));
            break;
         case 1:
            dynamic13 = dynamic.createMap(ImmutableMap.of(dynamic.createString("type"), dynamic.createString("minecraft:debug")));
            break;
         case 2:
            OptionalDynamic<T> optionalDynamic2 = dynamic.get("generatorOptions");
            OptionalDynamic<?> optionalDynamic3 = optionalDynamic2.get("chunk_generator");
            Optional<String> optional3 = optionalDynamic3.get("type").asString().result();
            Dynamic dynamic8;
            if (Objects.equals(optional3, Optional.of("minecraft:caves"))) {
               dynamic8 = dynamic.createString("minecraft:caves");
               bl = true;
            } else if (Objects.equals(optional3, Optional.of("minecraft:floating_islands"))) {
               dynamic8 = dynamic.createString("minecraft:floating_islands");
            } else {
               dynamic8 = dynamic.createString("minecraft:overworld");
            }

            Dynamic<T> dynamic9 = (Dynamic)optionalDynamic2.get("biome_source").result().orElseGet(() -> {
               return dynamic.createMap(ImmutableMap.of(dynamic.createString("type"), dynamic.createString("minecraft:fixed")));
            });
            Dynamic dynamic11;
            if (dynamic9.get("type").asString().result().equals(Optional.of("minecraft:fixed"))) {
               String string = (String)dynamic9.get("options").get("biomes").asStream().findFirst().flatMap((dynamicx) -> {
                  return dynamicx.asString().result();
               }).orElse("minecraft:ocean");
               dynamic11 = dynamic9.remove("options").set("biome", dynamic.createString(string));
            } else {
               dynamic11 = dynamic9;
            }

            dynamic13 = method_28268(l, dynamic, dynamic8, dynamic11);
            break;
         default:
            boolean bl2 = ((String)optional.get()).equals("default");
            boolean bl3 = ((String)optional.get()).equals("default_1_1") || bl2 && dynamic.get("generatorVersion").asInt(0) == 0;
            boolean bl4 = ((String)optional.get()).equals("amplified");
            boolean bl5 = ((String)optional.get()).equals("largebiomes");
            dynamic13 = method_28268(l, dynamic, dynamic.createString(bl4 ? "minecraft:amplified" : "minecraft:overworld"), method_28272(dynamic, l, bl3, bl5));
         }
      }

      boolean bl6 = dynamic.get("MapFeatures").asBoolean(true);
      boolean bl7 = dynamic.get("BonusChest").asBoolean(false);
      Builder<T, T> builder = ImmutableMap.builder();
      builder.put(dynamicOps.createString("seed"), dynamicOps.createLong(l));
      builder.put(dynamicOps.createString("generate_features"), dynamicOps.createBoolean(bl6));
      builder.put(dynamicOps.createString("bonus_chest"), dynamicOps.createBoolean(bl7));
      builder.put(dynamicOps.createString("dimensions"), method_29917(dynamic, l, dynamic13, bl));
      optional2.ifPresent((stringx) -> {
         builder.put(dynamicOps.createString("legacy_custom_options"), dynamicOps.createString(stringx));
      });
      return new Dynamic(dynamicOps, dynamicOps.createMap((Map)builder.build()));
   }

   protected static <T> Dynamic<T> method_29916(Dynamic<T> dynamic, long l) {
      return method_28268(l, dynamic, dynamic.createString("minecraft:overworld"), method_28272(dynamic, l, false, false));
   }

   protected static <T> T method_29917(Dynamic<T> dynamic, long l, Dynamic<T> dynamic2, boolean bl) {
      DynamicOps<T> dynamicOps = dynamic.getOps();
      return dynamicOps.createMap((Map)ImmutableMap.of(dynamicOps.createString("minecraft:overworld"), dynamicOps.createMap((Map)ImmutableMap.of(dynamicOps.createString("type"), dynamicOps.createString("minecraft:overworld" + (bl ? "_caves" : "")), dynamicOps.createString("generator"), dynamic2.getValue())), dynamicOps.createString("minecraft:the_nether"), dynamicOps.createMap((Map)ImmutableMap.of(dynamicOps.createString("type"), dynamicOps.createString("minecraft:the_nether"), dynamicOps.createString("generator"), method_28268(l, dynamic, dynamic.createString("minecraft:nether"), dynamic.createMap(ImmutableMap.of(dynamic.createString("type"), dynamic.createString("minecraft:multi_noise"), dynamic.createString("seed"), dynamic.createLong(l), dynamic.createString("preset"), dynamic.createString("minecraft:nether")))).getValue())), dynamicOps.createString("minecraft:the_end"), dynamicOps.createMap((Map)ImmutableMap.of(dynamicOps.createString("type"), dynamicOps.createString("minecraft:the_end"), dynamicOps.createString("generator"), method_28268(l, dynamic, dynamic.createString("minecraft:end"), dynamic.createMap(ImmutableMap.of(dynamic.createString("type"), dynamic.createString("minecraft:the_end"), dynamic.createString("seed"), dynamic.createLong(l)))).getValue()))));
   }

   private static <T> Map<Dynamic<T>, Dynamic<T>> method_28275(DynamicOps<T> dynamicOps, OptionalDynamic<T> optionalDynamic) {
      MutableInt mutableInt = new MutableInt(32);
      MutableInt mutableInt2 = new MutableInt(3);
      MutableInt mutableInt3 = new MutableInt(128);
      MutableBoolean mutableBoolean = new MutableBoolean(false);
      Map<String, StructureSeparationDataFix.Information> map = Maps.newHashMap();
      if (!optionalDynamic.result().isPresent()) {
         mutableBoolean.setTrue();
         map.put("minecraft:village", STRUCTURE_SPACING.get("minecraft:village"));
      }

      optionalDynamic.get("structures").flatMap(Dynamic::getMapValues).result().ifPresent((map2) -> {
         map2.forEach((dynamic, dynamic2) -> {
            dynamic2.getMapValues().result().ifPresent((map2) -> {
               map2.forEach((dynamic2, dynamic3) -> {
                  String string = dynamic.asString("");
                  String string2 = dynamic2.asString("");
                  String string3 = dynamic3.asString("");
                  byte var12;
                  if ("stronghold".equals(string)) {
                     mutableBoolean.setTrue();
                     var12 = -1;
                     switch(string2.hashCode()) {
                     case -895684237:
                        if (string2.equals("spread")) {
                           var12 = 1;
                        }
                        break;
                     case 94851343:
                        if (string2.equals("count")) {
                           var12 = 2;
                        }
                        break;
                     case 288459765:
                        if (string2.equals("distance")) {
                           var12 = 0;
                        }
                     }

                     switch(var12) {
                     case 0:
                        mutableInt.setValue(method_28280(string3, mutableInt.getValue(), 1));
                        return;
                     case 1:
                        mutableInt2.setValue(method_28280(string3, mutableInt2.getValue(), 1));
                        return;
                     case 2:
                        mutableInt3.setValue(method_28280(string3, mutableInt3.getValue(), 1));
                        return;
                     default:
                     }
                  } else {
                     var12 = -1;
                     switch(string2.hashCode()) {
                     case -2116852922:
                        if (string2.equals("separation")) {
                           var12 = 1;
                        }
                        break;
                     case -2012158909:
                        if (string2.equals("spacing")) {
                           var12 = 2;
                        }
                        break;
                     case 288459765:
                        if (string2.equals("distance")) {
                           var12 = 0;
                        }
                     }

                     switch(var12) {
                     case 0:
                        byte var15 = -1;
                        switch(string.hashCode()) {
                        case -1606796090:
                           if (string.equals("endcity")) {
                              var15 = 2;
                           }
                           break;
                        case -107033518:
                           if (string.equals("biome_1")) {
                              var15 = 1;
                           }
                           break;
                        case 460367020:
                           if (string.equals("village")) {
                              var15 = 0;
                           }
                           break;
                        case 835798799:
                           if (string.equals("mansion")) {
                              var15 = 3;
                           }
                        }

                        switch(var15) {
                        case 0:
                           method_28281(map, "minecraft:village", string3, 9);
                           return;
                        case 1:
                           method_28281(map, "minecraft:desert_pyramid", string3, 9);
                           method_28281(map, "minecraft:igloo", string3, 9);
                           method_28281(map, "minecraft:jungle_pyramid", string3, 9);
                           method_28281(map, "minecraft:swamp_hut", string3, 9);
                           method_28281(map, "minecraft:pillager_outpost", string3, 9);
                           return;
                        case 2:
                           method_28281(map, "minecraft:endcity", string3, 1);
                           return;
                        case 3:
                           method_28281(map, "minecraft:mansion", string3, 1);
                           return;
                        default:
                           return;
                        }
                     case 1:
                        if ("oceanmonument".equals(string)) {
                           StructureSeparationDataFix.Information information = (StructureSeparationDataFix.Information)map.getOrDefault("minecraft:monument", STRUCTURE_SPACING.get("minecraft:monument"));
                           int i = method_28280(string3, information.separation, 1);
                           map.put("minecraft:monument", new StructureSeparationDataFix.Information(i, information.separation, information.salt));
                        }

                        return;
                     case 2:
                        if ("oceanmonument".equals(string)) {
                           method_28281(map, "minecraft:monument", string3, 1);
                        }

                        return;
                     default:
                     }
                  }
               });
            });
         });
      });
      Builder<Dynamic<T>, Dynamic<T>> builder = ImmutableMap.builder();
      builder.put(optionalDynamic.createString("structures"), optionalDynamic.createMap((Map)map.entrySet().stream().collect(Collectors.toMap((entry) -> {
         return optionalDynamic.createString((String)entry.getKey());
      }, (entry) -> {
         return ((StructureSeparationDataFix.Information)entry.getValue()).method_28288(dynamicOps);
      }))));
      if (mutableBoolean.isTrue()) {
         builder.put(optionalDynamic.createString("stronghold"), optionalDynamic.createMap(ImmutableMap.of(optionalDynamic.createString("distance"), optionalDynamic.createInt(mutableInt.getValue()), optionalDynamic.createString("spread"), optionalDynamic.createInt(mutableInt2.getValue()), optionalDynamic.createString("count"), optionalDynamic.createInt(mutableInt3.getValue()))));
      }

      return builder.build();
   }

   private static int method_28279(String string, int i) {
      return NumberUtils.toInt(string, i);
   }

   private static int method_28280(String string, int i, int j) {
      return Math.max(j, method_28279(string, i));
   }

   private static void method_28281(Map<String, StructureSeparationDataFix.Information> map, String string, String string2, int i) {
      StructureSeparationDataFix.Information information = (StructureSeparationDataFix.Information)map.getOrDefault(string, STRUCTURE_SPACING.get(string));
      int j = method_28280(string2, information.spacing, i);
      map.put(string, new StructureSeparationDataFix.Information(j, information.separation, information.salt));
   }

   static final class Information {
      public static final Codec<StructureSeparationDataFix.Information> CODEC = RecordCodecBuilder.create((instance) -> {
         return instance.group(Codec.INT.fieldOf("spacing").forGetter((information) -> {
            return information.spacing;
         }), Codec.INT.fieldOf("separation").forGetter((information) -> {
            return information.separation;
         }), Codec.INT.fieldOf("salt").forGetter((information) -> {
            return information.salt;
         })).apply(instance, (Function3)(StructureSeparationDataFix.Information::new));
      });
      private final int spacing;
      private final int separation;
      private final int salt;

      public Information(int spacing, int separation, int salt) {
         this.spacing = spacing;
         this.separation = separation;
         this.salt = salt;
      }

      public <T> Dynamic<T> method_28288(DynamicOps<T> dynamicOps) {
         return new Dynamic(dynamicOps, CODEC.encodeStart(dynamicOps, this).result().orElse(dynamicOps.emptyMap()));
      }
   }
}
