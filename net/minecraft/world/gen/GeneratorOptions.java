package net.minecraft.world.gen;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.datafixers.kinds.App;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Iterator;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.Properties;
import java.util.Random;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.function.Supplier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.registry.DynamicRegistryManager;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.util.registry.SimpleRegistry;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.VanillaLayeredBiomeSource;
import net.minecraft.world.dimension.DimensionOptions;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.chunk.ChunkGeneratorSettings;
import net.minecraft.world.gen.chunk.DebugChunkGenerator;
import net.minecraft.world.gen.chunk.FlatChunkGenerator;
import net.minecraft.world.gen.chunk.FlatChunkGeneratorConfig;
import net.minecraft.world.gen.chunk.NoiseChunkGenerator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class GeneratorOptions {
   public static final Codec<GeneratorOptions> CODEC = RecordCodecBuilder.create((instance) -> {
      return instance.group(Codec.LONG.fieldOf("seed").stable().forGetter(GeneratorOptions::getSeed), Codec.BOOL.fieldOf("generate_features").orElse(true).stable().forGetter(GeneratorOptions::shouldGenerateStructures), Codec.BOOL.fieldOf("bonus_chest").orElse(false).stable().forGetter(GeneratorOptions::hasBonusChest), SimpleRegistry.createRegistryCodec(Registry.DIMENSION_OPTIONS, Lifecycle.stable(), DimensionOptions.CODEC).xmap(DimensionOptions::method_29569, Function.identity()).fieldOf("dimensions").forGetter(GeneratorOptions::getDimensions), Codec.STRING.optionalFieldOf("legacy_custom_options").stable().forGetter((generatorOptions) -> {
         return generatorOptions.legacyCustomOptions;
      })).apply(instance, (App)instance.stable(GeneratorOptions::new));
   }).comapFlatMap(GeneratorOptions::method_28610, Function.identity());
   private static final Logger LOGGER = LogManager.getLogger();
   private final long seed;
   private final boolean generateStructures;
   private final boolean bonusChest;
   private final SimpleRegistry<DimensionOptions> options;
   private final Optional<String> legacyCustomOptions;

   private DataResult<GeneratorOptions> method_28610() {
      DimensionOptions dimensionOptions = (DimensionOptions)this.options.get(DimensionOptions.OVERWORLD);
      if (dimensionOptions == null) {
         return DataResult.error("Overworld settings missing");
      } else {
         return this.method_28611() ? DataResult.success(this, Lifecycle.stable()) : DataResult.success(this);
      }
   }

   private boolean method_28611() {
      return DimensionOptions.method_29567(this.seed, this.options);
   }

   public GeneratorOptions(long seed, boolean generateStructures, boolean bonusChest, SimpleRegistry<DimensionOptions> simpleRegistry) {
      this(seed, generateStructures, bonusChest, simpleRegistry, Optional.empty());
      DimensionOptions dimensionOptions = (DimensionOptions)simpleRegistry.get(DimensionOptions.OVERWORLD);
      if (dimensionOptions == null) {
         throw new IllegalStateException("Overworld settings missing");
      }
   }

   private GeneratorOptions(long seed, boolean generateStructures, boolean bonusChest, SimpleRegistry<DimensionOptions> simpleRegistry, Optional<String> legacyCustomOptions) {
      this.seed = seed;
      this.generateStructures = generateStructures;
      this.bonusChest = bonusChest;
      this.options = simpleRegistry;
      this.legacyCustomOptions = legacyCustomOptions;
   }

   public static GeneratorOptions method_31112(DynamicRegistryManager dynamicRegistryManager) {
      Registry<Biome> registry = dynamicRegistryManager.get(Registry.BIOME_KEY);
      int i = "North Carolina".hashCode();
      Registry<DimensionType> registry2 = dynamicRegistryManager.get(Registry.DIMENSION_TYPE_KEY);
      Registry<ChunkGeneratorSettings> registry3 = dynamicRegistryManager.get(Registry.NOISE_SETTINGS_WORLDGEN);
      return new GeneratorOptions((long)i, true, true, method_28608(registry2, DimensionType.createDefaultDimensionOptions(registry2, registry, registry3, (long)i), createOverworldGenerator(registry, registry3, (long)i)));
   }

   public static GeneratorOptions getDefaultOptions(Registry<DimensionType> registry, Registry<Biome> registry2, Registry<ChunkGeneratorSettings> registry3) {
      long l = (new Random()).nextLong();
      return new GeneratorOptions(l, true, false, method_28608(registry, DimensionType.createDefaultDimensionOptions(registry, registry2, registry3, l), createOverworldGenerator(registry2, registry3, l)));
   }

   public static NoiseChunkGenerator createOverworldGenerator(Registry<Biome> biomeRegistry, Registry<ChunkGeneratorSettings> chunkGeneratorSettingsRegistry, long seed) {
      return new NoiseChunkGenerator(new VanillaLayeredBiomeSource(seed, false, false, biomeRegistry), seed, () -> {
         return (ChunkGeneratorSettings)chunkGeneratorSettingsRegistry.getOrThrow(ChunkGeneratorSettings.OVERWORLD);
      });
   }

   public long getSeed() {
      return this.seed;
   }

   public boolean shouldGenerateStructures() {
      return this.generateStructures;
   }

   public boolean hasBonusChest() {
      return this.bonusChest;
   }

   public static SimpleRegistry<DimensionOptions> method_28608(Registry<DimensionType> registry, SimpleRegistry<DimensionOptions> simpleRegistry, ChunkGenerator chunkGenerator) {
      DimensionOptions dimensionOptions = (DimensionOptions)simpleRegistry.get(DimensionOptions.OVERWORLD);
      Supplier<DimensionType> supplier = () -> {
         return dimensionOptions == null ? (DimensionType)registry.getOrThrow(DimensionType.OVERWORLD_REGISTRY_KEY) : dimensionOptions.getDimensionType();
      };
      return method_29962(simpleRegistry, supplier, chunkGenerator);
   }

   public static SimpleRegistry<DimensionOptions> method_29962(SimpleRegistry<DimensionOptions> simpleRegistry, Supplier<DimensionType> supplier, ChunkGenerator chunkGenerator) {
      SimpleRegistry<DimensionOptions> simpleRegistry2 = new SimpleRegistry(Registry.DIMENSION_OPTIONS, Lifecycle.experimental());
      simpleRegistry2.add(DimensionOptions.OVERWORLD, new DimensionOptions(supplier, chunkGenerator), Lifecycle.stable());
      Iterator var4 = simpleRegistry.getEntries().iterator();

      while(var4.hasNext()) {
         Entry<RegistryKey<DimensionOptions>, DimensionOptions> entry = (Entry)var4.next();
         RegistryKey<DimensionOptions> registryKey = (RegistryKey)entry.getKey();
         if (registryKey != DimensionOptions.OVERWORLD) {
            simpleRegistry2.add(registryKey, entry.getValue(), simpleRegistry.getEntryLifecycle(entry.getValue()));
         }
      }

      return simpleRegistry2;
   }

   public SimpleRegistry<DimensionOptions> getDimensions() {
      return this.options;
   }

   public ChunkGenerator getChunkGenerator() {
      DimensionOptions dimensionOptions = (DimensionOptions)this.options.get(DimensionOptions.OVERWORLD);
      if (dimensionOptions == null) {
         throw new IllegalStateException("Overworld settings missing");
      } else {
         return dimensionOptions.getChunkGenerator();
      }
   }

   public ImmutableSet<RegistryKey<World>> getWorlds() {
      return (ImmutableSet)this.getDimensions().getEntries().stream().map((entry) -> {
         return RegistryKey.of(Registry.DIMENSION, ((RegistryKey)entry.getKey()).getValue());
      }).collect(ImmutableSet.toImmutableSet());
   }

   public boolean isDebugWorld() {
      return this.getChunkGenerator() instanceof DebugChunkGenerator;
   }

   public boolean isFlatWorld() {
      return this.getChunkGenerator() instanceof FlatChunkGenerator;
   }

   @Environment(EnvType.CLIENT)
   public boolean isLegacyCustomizedType() {
      return this.legacyCustomOptions.isPresent();
   }

   public GeneratorOptions withBonusChest() {
      return new GeneratorOptions(this.seed, this.generateStructures, true, this.options, this.legacyCustomOptions);
   }

   @Environment(EnvType.CLIENT)
   public GeneratorOptions toggleGenerateStructures() {
      return new GeneratorOptions(this.seed, !this.generateStructures, this.bonusChest, this.options);
   }

   @Environment(EnvType.CLIENT)
   public GeneratorOptions toggleBonusChest() {
      return new GeneratorOptions(this.seed, this.generateStructures, !this.bonusChest, this.options);
   }

   public static GeneratorOptions fromProperties(DynamicRegistryManager dynamicRegistryManager, Properties properties) {
      String string = (String)MoreObjects.firstNonNull((String)properties.get("generator-settings"), "");
      properties.put("generator-settings", string);
      String string2 = (String)MoreObjects.firstNonNull((String)properties.get("level-seed"), "");
      properties.put("level-seed", string2);
      String string3 = (String)properties.get("generate-structures");
      boolean bl = string3 == null || Boolean.parseBoolean(string3);
      properties.put("generate-structures", Objects.toString(bl));
      String string4 = (String)properties.get("level-type");
      String string5 = (String)Optional.ofNullable(string4).map((stringx) -> {
         return stringx.toLowerCase(Locale.ROOT);
      }).orElse("default");
      properties.put("level-type", string5);
      long l = (new Random()).nextLong();
      if (!string2.isEmpty()) {
         try {
            long m = Long.parseLong(string2);
            if (m != 0L) {
               l = m;
            }
         } catch (NumberFormatException var18) {
            l = (long)string2.hashCode();
         }
      }

      Registry<DimensionType> registry = dynamicRegistryManager.get(Registry.DIMENSION_TYPE_KEY);
      Registry<Biome> registry2 = dynamicRegistryManager.get(Registry.BIOME_KEY);
      Registry<ChunkGeneratorSettings> registry3 = dynamicRegistryManager.get(Registry.NOISE_SETTINGS_WORLDGEN);
      SimpleRegistry<DimensionOptions> simpleRegistry = DimensionType.createDefaultDimensionOptions(registry, registry2, registry3, l);
      byte var15 = -1;
      switch(string5.hashCode()) {
      case -1100099890:
         if (string5.equals("largebiomes")) {
            var15 = 3;
         }
         break;
      case 3145593:
         if (string5.equals("flat")) {
            var15 = 0;
         }
         break;
      case 1045526590:
         if (string5.equals("debug_all_block_states")) {
            var15 = 1;
         }
         break;
      case 1271599715:
         if (string5.equals("amplified")) {
            var15 = 2;
         }
      }

      switch(var15) {
      case 0:
         JsonObject jsonObject = !string.isEmpty() ? JsonHelper.deserialize(string) : new JsonObject();
         Dynamic<JsonElement> dynamic = new Dynamic(JsonOps.INSTANCE, jsonObject);
         DataResult var10009 = FlatChunkGeneratorConfig.CODEC.parse(dynamic);
         Logger var10010 = LOGGER;
         var10010.getClass();
         return new GeneratorOptions(l, bl, false, method_28608(registry, simpleRegistry, new FlatChunkGenerator((FlatChunkGeneratorConfig)var10009.resultOrPartial(var10010::error).orElseGet(() -> {
            return FlatChunkGeneratorConfig.getDefaultConfig(registry2);
         }))));
      case 1:
         return new GeneratorOptions(l, bl, false, method_28608(registry, simpleRegistry, new DebugChunkGenerator(registry2)));
      case 2:
         return new GeneratorOptions(l, bl, false, method_28608(registry, simpleRegistry, new NoiseChunkGenerator(new VanillaLayeredBiomeSource(l, false, false, registry2), l, () -> {
            return (ChunkGeneratorSettings)registry3.getOrThrow(ChunkGeneratorSettings.AMPLIFIED);
         })));
      case 3:
         return new GeneratorOptions(l, bl, false, method_28608(registry, simpleRegistry, new NoiseChunkGenerator(new VanillaLayeredBiomeSource(l, false, true, registry2), l, () -> {
            return (ChunkGeneratorSettings)registry3.getOrThrow(ChunkGeneratorSettings.OVERWORLD);
         })));
      default:
         return new GeneratorOptions(l, bl, false, method_28608(registry, simpleRegistry, createOverworldGenerator(registry2, registry3, l)));
      }
   }

   @Environment(EnvType.CLIENT)
   public GeneratorOptions withHardcore(boolean hardcore, OptionalLong seed) {
      long l = seed.orElse(this.seed);
      SimpleRegistry simpleRegistry2;
      if (seed.isPresent()) {
         simpleRegistry2 = new SimpleRegistry(Registry.DIMENSION_OPTIONS, Lifecycle.experimental());
         long m = seed.getAsLong();
         Iterator var9 = this.options.getEntries().iterator();

         while(var9.hasNext()) {
            Entry<RegistryKey<DimensionOptions>, DimensionOptions> entry = (Entry)var9.next();
            RegistryKey<DimensionOptions> registryKey = (RegistryKey)entry.getKey();
            simpleRegistry2.add(registryKey, new DimensionOptions(((DimensionOptions)entry.getValue()).getDimensionTypeSupplier(), ((DimensionOptions)entry.getValue()).getChunkGenerator().withSeed(m)), this.options.getEntryLifecycle(entry.getValue()));
         }
      } else {
         simpleRegistry2 = this.options;
      }

      GeneratorOptions generatorOptions2;
      if (this.isDebugWorld()) {
         generatorOptions2 = new GeneratorOptions(l, false, false, simpleRegistry2);
      } else {
         generatorOptions2 = new GeneratorOptions(l, this.shouldGenerateStructures(), this.hasBonusChest() && !hardcore, simpleRegistry2);
      }

      return generatorOptions2;
   }
}
