package net.minecraft.world.gen.chunk;

import com.google.common.collect.Maps;
import com.mojang.datafixers.util.Function8;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.Identifier;
import net.minecraft.util.dynamic.RegistryElementCodec;
import net.minecraft.util.registry.BuiltinRegistries;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.gen.feature.StructureFeature;

public final class ChunkGeneratorSettings {
   public static final Codec<ChunkGeneratorSettings> CODEC = RecordCodecBuilder.create((instance) -> {
      return instance.group(StructuresConfig.CODEC.fieldOf("structures").forGetter(ChunkGeneratorSettings::getStructuresConfig), GenerationShapeConfig.CODEC.fieldOf("noise").forGetter(ChunkGeneratorSettings::getGenerationShapeConfig), BlockState.CODEC.fieldOf("default_block").forGetter(ChunkGeneratorSettings::getDefaultBlock), BlockState.CODEC.fieldOf("default_fluid").forGetter(ChunkGeneratorSettings::getDefaultFluid), Codec.intRange(-20, 276).fieldOf("bedrock_roof_position").forGetter(ChunkGeneratorSettings::getBedrockCeilingY), Codec.intRange(-20, 276).fieldOf("bedrock_floor_position").forGetter(ChunkGeneratorSettings::getBedrockFloorY), Codec.intRange(0, 255).fieldOf("sea_level").forGetter(ChunkGeneratorSettings::getSeaLevel), Codec.BOOL.fieldOf("disable_mob_generation").forGetter(ChunkGeneratorSettings::isMobGenerationDisabled)).apply(instance, (Function8)(ChunkGeneratorSettings::new));
   });
   public static final Codec<Supplier<ChunkGeneratorSettings>> REGISTRY_CODEC;
   private final StructuresConfig structuresConfig;
   private final GenerationShapeConfig generationShapeConfig;
   private final BlockState defaultBlock;
   private final BlockState defaultFluid;
   private final int bedrockCeilingY;
   private final int bedrockFloorY;
   private final int seaLevel;
   private final boolean mobGenerationDisabled;
   public static final RegistryKey<ChunkGeneratorSettings> OVERWORLD;
   public static final RegistryKey<ChunkGeneratorSettings> AMPLIFIED;
   public static final RegistryKey<ChunkGeneratorSettings> NETHER;
   public static final RegistryKey<ChunkGeneratorSettings> END;
   public static final RegistryKey<ChunkGeneratorSettings> CAVES;
   public static final RegistryKey<ChunkGeneratorSettings> FLOATING_ISLANDS;
   private static final ChunkGeneratorSettings INSTANCE;

   private ChunkGeneratorSettings(StructuresConfig structuresConfig, GenerationShapeConfig generationShapeConfig, BlockState defaultBlock, BlockState defaultFluid, int bedrockCeilingY, int bedrockFloorY, int seaLevel, boolean mobGenerationDisabled) {
      this.structuresConfig = structuresConfig;
      this.generationShapeConfig = generationShapeConfig;
      this.defaultBlock = defaultBlock;
      this.defaultFluid = defaultFluid;
      this.bedrockCeilingY = bedrockCeilingY;
      this.bedrockFloorY = bedrockFloorY;
      this.seaLevel = seaLevel;
      this.mobGenerationDisabled = mobGenerationDisabled;
   }

   public StructuresConfig getStructuresConfig() {
      return this.structuresConfig;
   }

   public GenerationShapeConfig getGenerationShapeConfig() {
      return this.generationShapeConfig;
   }

   public BlockState getDefaultBlock() {
      return this.defaultBlock;
   }

   public BlockState getDefaultFluid() {
      return this.defaultFluid;
   }

   /**
    * Returns the Y level of the bedrock ceiling.
    * 
    * <p>If a number less than 1 is returned, the ceiling will not be generated.
    */
   public int getBedrockCeilingY() {
      return this.bedrockCeilingY;
   }

   /**
    * Returns the Y level of the bedrock floor.
    * 
    * <p>If a number greater than 255 is returned, the floor will not be generated.
    */
   public int getBedrockFloorY() {
      return this.bedrockFloorY;
   }

   public int getSeaLevel() {
      return this.seaLevel;
   }

   /**
    * Whether entities will be generated during chunk population.
    * 
    * <p>It does not control whether spawns will occur during gameplay.
    */
   @Deprecated
   protected boolean isMobGenerationDisabled() {
      return this.mobGenerationDisabled;
   }

   public boolean equals(RegistryKey<ChunkGeneratorSettings> registryKey) {
      return Objects.equals(this, BuiltinRegistries.CHUNK_GENERATOR_SETTINGS.get(registryKey));
   }

   private static ChunkGeneratorSettings register(RegistryKey<ChunkGeneratorSettings> registryKey, ChunkGeneratorSettings settings) {
      BuiltinRegistries.add(BuiltinRegistries.CHUNK_GENERATOR_SETTINGS, (Identifier)registryKey.getValue(), settings);
      return settings;
   }

   public static ChunkGeneratorSettings getInstance() {
      return INSTANCE;
   }

   private static ChunkGeneratorSettings createIslandSettings(StructuresConfig structuresConfig, BlockState defaultBlock, BlockState defaultFluid, Identifier id, boolean mobGenerationDisabled, boolean islandNoiseOverride) {
      return new ChunkGeneratorSettings(structuresConfig, new GenerationShapeConfig(128, new NoiseSamplingConfig(2.0D, 1.0D, 80.0D, 160.0D), new SlideConfig(-3000, 64, -46), new SlideConfig(-30, 7, 1), 2, 1, 0.0D, 0.0D, true, false, islandNoiseOverride, false), defaultBlock, defaultFluid, -10, -10, 0, mobGenerationDisabled);
   }

   private static ChunkGeneratorSettings createUndergroundSettings(StructuresConfig structuresConfig, BlockState defaultBlock, BlockState defaultFluid, Identifier id) {
      Map<StructureFeature<?>, StructureConfig> map = Maps.newHashMap(StructuresConfig.DEFAULT_STRUCTURES);
      map.put(StructureFeature.RUINED_PORTAL, new StructureConfig(25, 10, 34222645));
      return new ChunkGeneratorSettings(new StructuresConfig(Optional.ofNullable(structuresConfig.getStronghold()), map), new GenerationShapeConfig(128, new NoiseSamplingConfig(1.0D, 3.0D, 80.0D, 60.0D), new SlideConfig(120, 3, 0), new SlideConfig(320, 4, -1), 1, 2, 0.0D, 0.019921875D, false, false, false, false), defaultBlock, defaultFluid, 0, 0, 32, false);
   }

   private static ChunkGeneratorSettings createSurfaceSettings(StructuresConfig structuresConfig, boolean amplified, Identifier id) {
      double d = 0.9999999814507745D;
      return new ChunkGeneratorSettings(structuresConfig, new GenerationShapeConfig(256, new NoiseSamplingConfig(0.9999999814507745D, 0.9999999814507745D, 80.0D, 160.0D), new SlideConfig(-10, 3, 0), new SlideConfig(-30, 0, 0), 1, 2, 1.0D, -0.46875D, true, true, false, amplified), Blocks.STONE.getDefaultState(), Blocks.WATER.getDefaultState(), -10, 0, 63, false);
   }

   static {
      REGISTRY_CODEC = RegistryElementCodec.of(Registry.NOISE_SETTINGS_WORLDGEN, CODEC);
      OVERWORLD = RegistryKey.of(Registry.NOISE_SETTINGS_WORLDGEN, new Identifier("overworld"));
      AMPLIFIED = RegistryKey.of(Registry.NOISE_SETTINGS_WORLDGEN, new Identifier("amplified"));
      NETHER = RegistryKey.of(Registry.NOISE_SETTINGS_WORLDGEN, new Identifier("nether"));
      END = RegistryKey.of(Registry.NOISE_SETTINGS_WORLDGEN, new Identifier("end"));
      CAVES = RegistryKey.of(Registry.NOISE_SETTINGS_WORLDGEN, new Identifier("caves"));
      FLOATING_ISLANDS = RegistryKey.of(Registry.NOISE_SETTINGS_WORLDGEN, new Identifier("floating_islands"));
      INSTANCE = register(OVERWORLD, createSurfaceSettings(new StructuresConfig(true), false, OVERWORLD.getValue()));
      register(AMPLIFIED, createSurfaceSettings(new StructuresConfig(true), true, AMPLIFIED.getValue()));
      register(NETHER, createUndergroundSettings(new StructuresConfig(false), Blocks.NETHERRACK.getDefaultState(), Blocks.LAVA.getDefaultState(), NETHER.getValue()));
      register(END, createIslandSettings(new StructuresConfig(false), Blocks.END_STONE.getDefaultState(), Blocks.AIR.getDefaultState(), END.getValue(), true, true));
      register(CAVES, createUndergroundSettings(new StructuresConfig(true), Blocks.STONE.getDefaultState(), Blocks.WATER.getDefaultState(), CAVES.getValue()));
      register(FLOATING_ISLANDS, createIslandSettings(new StructuresConfig(true), Blocks.STONE.getDefaultState(), Blocks.WATER.getDefaultState(), FLOATING_ISLANDS.getValue(), false, false));
   }
}
