package net.minecraft.world.gen.chunk;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.datafixers.util.Function6;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Map.Entry;
import java.util.function.Supplier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.Util;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryLookupCodec;
import net.minecraft.world.Heightmap;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeKeys;
import net.minecraft.world.biome.GenerationSettings;
import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.gen.feature.ConfiguredFeatures;
import net.minecraft.world.gen.feature.ConfiguredStructureFeature;
import net.minecraft.world.gen.feature.ConfiguredStructureFeatures;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.FillLayerFeatureConfig;
import net.minecraft.world.gen.feature.StructureFeature;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class FlatChunkGeneratorConfig {
   private static final Logger LOGGER = LogManager.getLogger();
   public static final Codec<FlatChunkGeneratorConfig> CODEC = RecordCodecBuilder.create((instance) -> {
      return instance.group(RegistryLookupCodec.of(Registry.BIOME_KEY).forGetter((flatChunkGeneratorConfig) -> {
         return flatChunkGeneratorConfig.biomeRegistry;
      }), StructuresConfig.CODEC.fieldOf("structures").forGetter(FlatChunkGeneratorConfig::getStructuresConfig), FlatChunkGeneratorLayer.CODEC.listOf().fieldOf("layers").forGetter(FlatChunkGeneratorConfig::getLayers), Codec.BOOL.fieldOf("lakes").orElse(false).forGetter((flatChunkGeneratorConfig) -> {
         return flatChunkGeneratorConfig.hasLakes;
      }), Codec.BOOL.fieldOf("features").orElse(false).forGetter((flatChunkGeneratorConfig) -> {
         return flatChunkGeneratorConfig.hasFeatures;
      }), Biome.REGISTRY_CODEC.optionalFieldOf("biome").orElseGet(Optional::empty).forGetter((flatChunkGeneratorConfig) -> {
         return Optional.of(flatChunkGeneratorConfig.biome);
      })).apply(instance, (Function6)(FlatChunkGeneratorConfig::new));
   }).stable();
   private static final Map<StructureFeature<?>, ConfiguredStructureFeature<?, ?>> STRUCTURE_TO_FEATURES = (Map)Util.make(Maps.newHashMap(), (hashMap) -> {
      hashMap.put(StructureFeature.MINESHAFT, ConfiguredStructureFeatures.MINESHAFT);
      hashMap.put(StructureFeature.VILLAGE, ConfiguredStructureFeatures.VILLAGE_PLAINS);
      hashMap.put(StructureFeature.STRONGHOLD, ConfiguredStructureFeatures.STRONGHOLD);
      hashMap.put(StructureFeature.SWAMP_HUT, ConfiguredStructureFeatures.SWAMP_HUT);
      hashMap.put(StructureFeature.DESERT_PYRAMID, ConfiguredStructureFeatures.DESERT_PYRAMID);
      hashMap.put(StructureFeature.JUNGLE_PYRAMID, ConfiguredStructureFeatures.JUNGLE_PYRAMID);
      hashMap.put(StructureFeature.IGLOO, ConfiguredStructureFeatures.IGLOO);
      hashMap.put(StructureFeature.OCEAN_RUIN, ConfiguredStructureFeatures.OCEAN_RUIN_COLD);
      hashMap.put(StructureFeature.SHIPWRECK, ConfiguredStructureFeatures.SHIPWRECK);
      hashMap.put(StructureFeature.MONUMENT, ConfiguredStructureFeatures.MONUMENT);
      hashMap.put(StructureFeature.END_CITY, ConfiguredStructureFeatures.END_CITY);
      hashMap.put(StructureFeature.MANSION, ConfiguredStructureFeatures.MANSION);
      hashMap.put(StructureFeature.FORTRESS, ConfiguredStructureFeatures.FORTRESS);
      hashMap.put(StructureFeature.PILLAGER_OUTPOST, ConfiguredStructureFeatures.PILLAGER_OUTPOST);
      hashMap.put(StructureFeature.RUINED_PORTAL, ConfiguredStructureFeatures.RUINED_PORTAL);
      hashMap.put(StructureFeature.BASTION_REMNANT, ConfiguredStructureFeatures.BASTION_REMNANT);
   });
   private final Registry<Biome> biomeRegistry;
   private final StructuresConfig structuresConfig;
   private final List<FlatChunkGeneratorLayer> layers;
   private Supplier<Biome> biome;
   private final BlockState[] layerBlocks;
   private boolean hasNoTerrain;
   private boolean hasFeatures;
   private boolean hasLakes;

   public FlatChunkGeneratorConfig(Registry<Biome> biomeRegistry, StructuresConfig structuresConfig, List<FlatChunkGeneratorLayer> layers, boolean hasLakes, boolean hasFeatures, Optional<Supplier<Biome>> biome) {
      this(structuresConfig, biomeRegistry);
      if (hasLakes) {
         this.enableLakes();
      }

      if (hasFeatures) {
         this.enableFeatures();
      }

      this.layers.addAll(layers);
      this.updateLayerBlocks();
      if (!biome.isPresent()) {
         LOGGER.error("Unknown biome, defaulting to plains");
         this.biome = () -> {
            return (Biome)biomeRegistry.getOrThrow(BiomeKeys.PLAINS);
         };
      } else {
         this.biome = (Supplier)biome.get();
      }

   }

   public FlatChunkGeneratorConfig(StructuresConfig structuresConfig, Registry<Biome> biomeRegistry) {
      this.layers = Lists.newArrayList();
      this.layerBlocks = new BlockState[256];
      this.hasFeatures = false;
      this.hasLakes = false;
      this.biomeRegistry = biomeRegistry;
      this.structuresConfig = structuresConfig;
      this.biome = () -> {
         return (Biome)biomeRegistry.getOrThrow(BiomeKeys.PLAINS);
      };
   }

   @Environment(EnvType.CLIENT)
   public FlatChunkGeneratorConfig withStructuresConfig(StructuresConfig structuresConfig) {
      return this.method_29965(this.layers, structuresConfig);
   }

   @Environment(EnvType.CLIENT)
   public FlatChunkGeneratorConfig method_29965(List<FlatChunkGeneratorLayer> list, StructuresConfig structuresConfig) {
      FlatChunkGeneratorConfig flatChunkGeneratorConfig = new FlatChunkGeneratorConfig(structuresConfig, this.biomeRegistry);
      Iterator var4 = list.iterator();

      while(var4.hasNext()) {
         FlatChunkGeneratorLayer flatChunkGeneratorLayer = (FlatChunkGeneratorLayer)var4.next();
         flatChunkGeneratorConfig.layers.add(new FlatChunkGeneratorLayer(flatChunkGeneratorLayer.getThickness(), flatChunkGeneratorLayer.getBlockState().getBlock()));
         flatChunkGeneratorConfig.updateLayerBlocks();
      }

      flatChunkGeneratorConfig.setBiome(this.biome);
      if (this.hasFeatures) {
         flatChunkGeneratorConfig.enableFeatures();
      }

      if (this.hasLakes) {
         flatChunkGeneratorConfig.enableLakes();
      }

      return flatChunkGeneratorConfig;
   }

   public void enableFeatures() {
      this.hasFeatures = true;
   }

   public void enableLakes() {
      this.hasLakes = true;
   }

   public Biome createBiome() {
      Biome biome = this.getBiome();
      GenerationSettings generationSettings = biome.getGenerationSettings();
      GenerationSettings.Builder builder = (new GenerationSettings.Builder()).surfaceBuilder(generationSettings.getSurfaceBuilder());
      if (this.hasLakes) {
         builder.feature(GenerationStep.Feature.LAKES, ConfiguredFeatures.LAKE_WATER);
         builder.feature(GenerationStep.Feature.LAKES, ConfiguredFeatures.LAKE_LAVA);
      }

      Iterator var4 = this.structuresConfig.getStructures().entrySet().iterator();

      while(var4.hasNext()) {
         Entry<StructureFeature<?>, StructureConfig> entry = (Entry)var4.next();
         builder.structureFeature(generationSettings.method_30978((ConfiguredStructureFeature)STRUCTURE_TO_FEATURES.get(entry.getKey())));
      }

      boolean bl = (!this.hasNoTerrain || this.biomeRegistry.getKey(biome).equals(Optional.of(BiomeKeys.THE_VOID))) && this.hasFeatures;
      int i;
      if (bl) {
         List<List<Supplier<ConfiguredFeature<?, ?>>>> list = generationSettings.getFeatures();

         for(i = 0; i < list.size(); ++i) {
            if (i != GenerationStep.Feature.UNDERGROUND_STRUCTURES.ordinal() && i != GenerationStep.Feature.SURFACE_STRUCTURES.ordinal()) {
               List<Supplier<ConfiguredFeature<?, ?>>> list2 = (List)list.get(i);
               Iterator var8 = list2.iterator();

               while(var8.hasNext()) {
                  Supplier<ConfiguredFeature<?, ?>> supplier = (Supplier)var8.next();
                  builder.feature(i, supplier);
               }
            }
         }
      }

      BlockState[] blockStates = this.getLayerBlocks();

      for(i = 0; i < blockStates.length; ++i) {
         BlockState blockState = blockStates[i];
         if (blockState != null && !Heightmap.Type.MOTION_BLOCKING.getBlockPredicate().test(blockState)) {
            this.layerBlocks[i] = null;
            builder.feature(GenerationStep.Feature.TOP_LAYER_MODIFICATION, Feature.FILL_LAYER.configure(new FillLayerFeatureConfig(i, blockState)));
         }
      }

      return (new Biome.Builder()).precipitation(biome.getPrecipitation()).category(biome.getCategory()).depth(biome.getDepth()).scale(biome.getScale()).temperature(biome.getTemperature()).downfall(biome.getDownfall()).effects(biome.getEffects()).generationSettings(builder.build()).spawnSettings(biome.getSpawnSettings()).build();
   }

   public StructuresConfig getStructuresConfig() {
      return this.structuresConfig;
   }

   public Biome getBiome() {
      return (Biome)this.biome.get();
   }

   @Environment(EnvType.CLIENT)
   public void setBiome(Supplier<Biome> biome) {
      this.biome = biome;
   }

   public List<FlatChunkGeneratorLayer> getLayers() {
      return this.layers;
   }

   public BlockState[] getLayerBlocks() {
      return this.layerBlocks;
   }

   public void updateLayerBlocks() {
      Arrays.fill(this.layerBlocks, 0, this.layerBlocks.length, (Object)null);
      int i = 0;

      FlatChunkGeneratorLayer flatChunkGeneratorLayer;
      for(Iterator var2 = this.layers.iterator(); var2.hasNext(); i += flatChunkGeneratorLayer.getThickness()) {
         flatChunkGeneratorLayer = (FlatChunkGeneratorLayer)var2.next();
         flatChunkGeneratorLayer.setStartY(i);
      }

      this.hasNoTerrain = true;
      Iterator var5 = this.layers.iterator();

      while(var5.hasNext()) {
         FlatChunkGeneratorLayer flatChunkGeneratorLayer2 = (FlatChunkGeneratorLayer)var5.next();

         for(int j = flatChunkGeneratorLayer2.getStartY(); j < flatChunkGeneratorLayer2.getStartY() + flatChunkGeneratorLayer2.getThickness(); ++j) {
            BlockState blockState = flatChunkGeneratorLayer2.getBlockState();
            if (!blockState.isOf(Blocks.AIR)) {
               this.hasNoTerrain = false;
               this.layerBlocks[j] = blockState;
            }
         }
      }

   }

   public static FlatChunkGeneratorConfig getDefaultConfig(Registry<Biome> biomeRegistry) {
      StructuresConfig structuresConfig = new StructuresConfig(Optional.of(StructuresConfig.DEFAULT_STRONGHOLD), Maps.newHashMap(ImmutableMap.of(StructureFeature.VILLAGE, StructuresConfig.DEFAULT_STRUCTURES.get(StructureFeature.VILLAGE))));
      FlatChunkGeneratorConfig flatChunkGeneratorConfig = new FlatChunkGeneratorConfig(structuresConfig, biomeRegistry);
      flatChunkGeneratorConfig.biome = () -> {
         return (Biome)biomeRegistry.getOrThrow(BiomeKeys.PLAINS);
      };
      flatChunkGeneratorConfig.getLayers().add(new FlatChunkGeneratorLayer(1, Blocks.BEDROCK));
      flatChunkGeneratorConfig.getLayers().add(new FlatChunkGeneratorLayer(2, Blocks.DIRT));
      flatChunkGeneratorConfig.getLayers().add(new FlatChunkGeneratorLayer(1, Blocks.GRASS_BLOCK));
      flatChunkGeneratorConfig.updateLayerBlocks();
      return flatChunkGeneratorConfig;
   }
}
