package net.minecraft.world.gen.surfacebuilder;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;

public abstract class SurfaceBuilder<C extends SurfaceConfig> {
   private static final BlockState DIRT;
   private static final BlockState GRASS_BLOCK;
   private static final BlockState PODZOL;
   private static final BlockState GRAVEL;
   private static final BlockState STONE;
   private static final BlockState COARSE_DIRT;
   private static final BlockState SAND;
   private static final BlockState RED_SAND;
   private static final BlockState WHITE_TERRACOTTA;
   private static final BlockState MYCELIUM;
   private static final BlockState SOUL_SAND;
   private static final BlockState NETHERRACK;
   private static final BlockState END_STONE;
   private static final BlockState CRIMSON_NYLIUM;
   private static final BlockState WARPED_NYLIUM;
   private static final BlockState NETHER_WART_BLOCK;
   private static final BlockState WARPED_WART_BLOCK;
   private static final BlockState BLACKSTONE;
   private static final BlockState BASALT;
   private static final BlockState MAGMA_BLOCK;
   public static final TernarySurfaceConfig PODZOL_CONFIG;
   public static final TernarySurfaceConfig GRAVEL_CONFIG;
   public static final TernarySurfaceConfig GRASS_CONFIG;
   public static final TernarySurfaceConfig STONE_CONFIG;
   public static final TernarySurfaceConfig COARSE_DIRT_CONFIG;
   public static final TernarySurfaceConfig SAND_CONFIG;
   public static final TernarySurfaceConfig GRASS_SAND_UNDERWATER_CONFIG;
   public static final TernarySurfaceConfig SAND_SAND_UNDERWATER_CONFIG;
   public static final TernarySurfaceConfig BADLANDS_CONFIG;
   public static final TernarySurfaceConfig MYCELIUM_CONFIG;
   public static final TernarySurfaceConfig NETHER_CONFIG;
   public static final TernarySurfaceConfig SOUL_SAND_CONFIG;
   public static final TernarySurfaceConfig END_CONFIG;
   public static final TernarySurfaceConfig CRIMSON_NYLIUM_CONFIG;
   public static final TernarySurfaceConfig WARPED_NYLIUM_CONFIG;
   public static final TernarySurfaceConfig BASALT_DELTA_CONFIG;
   public static final SurfaceBuilder<TernarySurfaceConfig> DEFAULT;
   public static final SurfaceBuilder<TernarySurfaceConfig> MOUNTAIN;
   public static final SurfaceBuilder<TernarySurfaceConfig> SHATTERED_SAVANNA;
   public static final SurfaceBuilder<TernarySurfaceConfig> GRAVELLY_MOUNTAIN;
   public static final SurfaceBuilder<TernarySurfaceConfig> GIANT_TREE_TAIGA;
   public static final SurfaceBuilder<TernarySurfaceConfig> SWAMP;
   public static final SurfaceBuilder<TernarySurfaceConfig> BADLANDS;
   public static final SurfaceBuilder<TernarySurfaceConfig> WOODED_BADLANDS;
   public static final SurfaceBuilder<TernarySurfaceConfig> ERODED_BADLANDS;
   public static final SurfaceBuilder<TernarySurfaceConfig> FROZEN_OCEAN;
   public static final SurfaceBuilder<TernarySurfaceConfig> NETHER;
   public static final SurfaceBuilder<TernarySurfaceConfig> NETHER_FOREST;
   public static final SurfaceBuilder<TernarySurfaceConfig> SOUL_SAND_VALLEY;
   public static final SurfaceBuilder<TernarySurfaceConfig> BASALT_DELTAS;
   public static final SurfaceBuilder<TernarySurfaceConfig> NOPE;
   private final Codec<ConfiguredSurfaceBuilder<C>> codec;

   private static <C extends SurfaceConfig, F extends SurfaceBuilder<C>> F register(String id, F surfaceBuilder) {
      return (SurfaceBuilder)Registry.register(Registry.SURFACE_BUILDER, (String)id, surfaceBuilder);
   }

   public SurfaceBuilder(Codec<C> codec) {
      this.codec = codec.fieldOf("config").xmap(this::withConfig, ConfiguredSurfaceBuilder::getConfig).codec();
   }

   public Codec<ConfiguredSurfaceBuilder<C>> getCodec() {
      return this.codec;
   }

   public ConfiguredSurfaceBuilder<C> withConfig(C config) {
      return new ConfiguredSurfaceBuilder(this, config);
   }

   public abstract void generate(Random random, Chunk chunk, Biome biome, int x, int z, int height, double noise, BlockState defaultBlock, BlockState defaultFluid, int seaLevel, long seed, C surfaceBlocks);

   public void initSeed(long seed) {
   }

   static {
      DIRT = Blocks.DIRT.getDefaultState();
      GRASS_BLOCK = Blocks.GRASS_BLOCK.getDefaultState();
      PODZOL = Blocks.PODZOL.getDefaultState();
      GRAVEL = Blocks.GRAVEL.getDefaultState();
      STONE = Blocks.STONE.getDefaultState();
      COARSE_DIRT = Blocks.COARSE_DIRT.getDefaultState();
      SAND = Blocks.SAND.getDefaultState();
      RED_SAND = Blocks.RED_SAND.getDefaultState();
      WHITE_TERRACOTTA = Blocks.WHITE_TERRACOTTA.getDefaultState();
      MYCELIUM = Blocks.MYCELIUM.getDefaultState();
      SOUL_SAND = Blocks.SOUL_SAND.getDefaultState();
      NETHERRACK = Blocks.NETHERRACK.getDefaultState();
      END_STONE = Blocks.END_STONE.getDefaultState();
      CRIMSON_NYLIUM = Blocks.CRIMSON_NYLIUM.getDefaultState();
      WARPED_NYLIUM = Blocks.WARPED_NYLIUM.getDefaultState();
      NETHER_WART_BLOCK = Blocks.NETHER_WART_BLOCK.getDefaultState();
      WARPED_WART_BLOCK = Blocks.WARPED_WART_BLOCK.getDefaultState();
      BLACKSTONE = Blocks.BLACKSTONE.getDefaultState();
      BASALT = Blocks.BASALT.getDefaultState();
      MAGMA_BLOCK = Blocks.MAGMA_BLOCK.getDefaultState();
      PODZOL_CONFIG = new TernarySurfaceConfig(PODZOL, DIRT, GRAVEL);
      GRAVEL_CONFIG = new TernarySurfaceConfig(GRAVEL, GRAVEL, GRAVEL);
      GRASS_CONFIG = new TernarySurfaceConfig(GRASS_BLOCK, DIRT, GRAVEL);
      STONE_CONFIG = new TernarySurfaceConfig(STONE, STONE, GRAVEL);
      COARSE_DIRT_CONFIG = new TernarySurfaceConfig(COARSE_DIRT, DIRT, GRAVEL);
      SAND_CONFIG = new TernarySurfaceConfig(SAND, SAND, GRAVEL);
      GRASS_SAND_UNDERWATER_CONFIG = new TernarySurfaceConfig(GRASS_BLOCK, DIRT, SAND);
      SAND_SAND_UNDERWATER_CONFIG = new TernarySurfaceConfig(SAND, SAND, SAND);
      BADLANDS_CONFIG = new TernarySurfaceConfig(RED_SAND, WHITE_TERRACOTTA, GRAVEL);
      MYCELIUM_CONFIG = new TernarySurfaceConfig(MYCELIUM, DIRT, GRAVEL);
      NETHER_CONFIG = new TernarySurfaceConfig(NETHERRACK, NETHERRACK, NETHERRACK);
      SOUL_SAND_CONFIG = new TernarySurfaceConfig(SOUL_SAND, SOUL_SAND, SOUL_SAND);
      END_CONFIG = new TernarySurfaceConfig(END_STONE, END_STONE, END_STONE);
      CRIMSON_NYLIUM_CONFIG = new TernarySurfaceConfig(CRIMSON_NYLIUM, NETHERRACK, NETHER_WART_BLOCK);
      WARPED_NYLIUM_CONFIG = new TernarySurfaceConfig(WARPED_NYLIUM, NETHERRACK, WARPED_WART_BLOCK);
      BASALT_DELTA_CONFIG = new TernarySurfaceConfig(BLACKSTONE, BASALT, MAGMA_BLOCK);
      DEFAULT = register("default", new DefaultSurfaceBuilder(TernarySurfaceConfig.CODEC));
      MOUNTAIN = register("mountain", new MountainSurfaceBuilder(TernarySurfaceConfig.CODEC));
      SHATTERED_SAVANNA = register("shattered_savanna", new ShatteredSavannaSurfaceBuilder(TernarySurfaceConfig.CODEC));
      GRAVELLY_MOUNTAIN = register("gravelly_mountain", new GravellyMountainSurfaceBuilder(TernarySurfaceConfig.CODEC));
      GIANT_TREE_TAIGA = register("giant_tree_taiga", new GiantTreeTaigaSurfaceBuilder(TernarySurfaceConfig.CODEC));
      SWAMP = register("swamp", new SwampSurfaceBuilder(TernarySurfaceConfig.CODEC));
      BADLANDS = register("badlands", new BadlandsSurfaceBuilder(TernarySurfaceConfig.CODEC));
      WOODED_BADLANDS = register("wooded_badlands", new WoodedBadlandsSurfaceBuilder(TernarySurfaceConfig.CODEC));
      ERODED_BADLANDS = register("eroded_badlands", new ErodedBadlandsSurfaceBuilder(TernarySurfaceConfig.CODEC));
      FROZEN_OCEAN = register("frozen_ocean", new FrozenOceanSurfaceBuilder(TernarySurfaceConfig.CODEC));
      NETHER = register("nether", new NetherSurfaceBuilder(TernarySurfaceConfig.CODEC));
      NETHER_FOREST = register("nether_forest", new NetherForestSurfaceBuilder(TernarySurfaceConfig.CODEC));
      SOUL_SAND_VALLEY = register("soul_sand_valley", new SoulSandValleySurfaceBuilder(TernarySurfaceConfig.CODEC));
      BASALT_DELTAS = register("basalt_deltas", new BasaltDeltasSurfaceBuilder(TernarySurfaceConfig.CODEC));
      NOPE = register("nope", new NopeSurfaceBuilder(TernarySurfaceConfig.CODEC));
   }
}
