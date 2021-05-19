package net.minecraft.world.gen.surfacebuilder;

import net.minecraft.block.Blocks;
import net.minecraft.util.registry.BuiltinRegistries;

public class ConfiguredSurfaceBuilders {
   public static final ConfiguredSurfaceBuilder<TernarySurfaceConfig> BADLANDS;
   public static final ConfiguredSurfaceBuilder<TernarySurfaceConfig> BASALT_DELTAS;
   public static final ConfiguredSurfaceBuilder<TernarySurfaceConfig> CRIMSON_FOREST;
   public static final ConfiguredSurfaceBuilder<TernarySurfaceConfig> DESERT;
   public static final ConfiguredSurfaceBuilder<TernarySurfaceConfig> END;
   public static final ConfiguredSurfaceBuilder<TernarySurfaceConfig> ERODED_BADLANDS;
   public static final ConfiguredSurfaceBuilder<TernarySurfaceConfig> FROZEN_OCEAN;
   public static final ConfiguredSurfaceBuilder<TernarySurfaceConfig> FULL_SAND;
   public static final ConfiguredSurfaceBuilder<TernarySurfaceConfig> GIANT_TREE_TAIGA;
   public static final ConfiguredSurfaceBuilder<TernarySurfaceConfig> GRASS;
   public static final ConfiguredSurfaceBuilder<TernarySurfaceConfig> GRAVELLY_MOUNTAIN;
   public static final ConfiguredSurfaceBuilder<TernarySurfaceConfig> ICE_SPIKES;
   public static final ConfiguredSurfaceBuilder<TernarySurfaceConfig> MOUNTAIN;
   public static final ConfiguredSurfaceBuilder<TernarySurfaceConfig> MYCELIUM;
   public static final ConfiguredSurfaceBuilder<TernarySurfaceConfig> NETHER;
   public static final ConfiguredSurfaceBuilder<TernarySurfaceConfig> NOPE;
   public static final ConfiguredSurfaceBuilder<TernarySurfaceConfig> OCEAN_SAND;
   public static final ConfiguredSurfaceBuilder<TernarySurfaceConfig> SHATTERED_SAVANNA;
   public static final ConfiguredSurfaceBuilder<TernarySurfaceConfig> SOUL_SAND_VALLEY;
   public static final ConfiguredSurfaceBuilder<TernarySurfaceConfig> STONE;
   public static final ConfiguredSurfaceBuilder<TernarySurfaceConfig> SWAMP;
   public static final ConfiguredSurfaceBuilder<TernarySurfaceConfig> WARPED_FOREST;
   public static final ConfiguredSurfaceBuilder<TernarySurfaceConfig> WOODED_BADLANDS;

   private static <SC extends SurfaceConfig> ConfiguredSurfaceBuilder<SC> register(String id, ConfiguredSurfaceBuilder<SC> configuredSurfaceBuilder) {
      return (ConfiguredSurfaceBuilder)BuiltinRegistries.add(BuiltinRegistries.CONFIGURED_SURFACE_BUILDER, (String)id, configuredSurfaceBuilder);
   }

   static {
      BADLANDS = register("badlands", SurfaceBuilder.BADLANDS.withConfig(SurfaceBuilder.BADLANDS_CONFIG));
      BASALT_DELTAS = register("basalt_deltas", SurfaceBuilder.BASALT_DELTAS.withConfig(SurfaceBuilder.BASALT_DELTA_CONFIG));
      CRIMSON_FOREST = register("crimson_forest", SurfaceBuilder.NETHER_FOREST.withConfig(SurfaceBuilder.CRIMSON_NYLIUM_CONFIG));
      DESERT = register("desert", SurfaceBuilder.DEFAULT.withConfig(SurfaceBuilder.SAND_CONFIG));
      END = register("end", SurfaceBuilder.DEFAULT.withConfig(SurfaceBuilder.END_CONFIG));
      ERODED_BADLANDS = register("eroded_badlands", SurfaceBuilder.ERODED_BADLANDS.withConfig(SurfaceBuilder.BADLANDS_CONFIG));
      FROZEN_OCEAN = register("frozen_ocean", SurfaceBuilder.FROZEN_OCEAN.withConfig(SurfaceBuilder.GRASS_CONFIG));
      FULL_SAND = register("full_sand", SurfaceBuilder.DEFAULT.withConfig(SurfaceBuilder.SAND_SAND_UNDERWATER_CONFIG));
      GIANT_TREE_TAIGA = register("giant_tree_taiga", SurfaceBuilder.GIANT_TREE_TAIGA.withConfig(SurfaceBuilder.GRASS_CONFIG));
      GRASS = register("grass", SurfaceBuilder.DEFAULT.withConfig(SurfaceBuilder.GRASS_CONFIG));
      GRAVELLY_MOUNTAIN = register("gravelly_mountain", SurfaceBuilder.GRAVELLY_MOUNTAIN.withConfig(SurfaceBuilder.GRASS_CONFIG));
      ICE_SPIKES = register("ice_spikes", SurfaceBuilder.DEFAULT.withConfig(new TernarySurfaceConfig(Blocks.SNOW_BLOCK.getDefaultState(), Blocks.DIRT.getDefaultState(), Blocks.GRAVEL.getDefaultState())));
      MOUNTAIN = register("mountain", SurfaceBuilder.MOUNTAIN.withConfig(SurfaceBuilder.GRASS_CONFIG));
      MYCELIUM = register("mycelium", SurfaceBuilder.DEFAULT.withConfig(SurfaceBuilder.MYCELIUM_CONFIG));
      NETHER = register("nether", SurfaceBuilder.NETHER.withConfig(SurfaceBuilder.NETHER_CONFIG));
      NOPE = register("nope", SurfaceBuilder.NOPE.withConfig(SurfaceBuilder.STONE_CONFIG));
      OCEAN_SAND = register("ocean_sand", SurfaceBuilder.DEFAULT.withConfig(SurfaceBuilder.GRASS_SAND_UNDERWATER_CONFIG));
      SHATTERED_SAVANNA = register("shattered_savanna", SurfaceBuilder.SHATTERED_SAVANNA.withConfig(SurfaceBuilder.GRASS_CONFIG));
      SOUL_SAND_VALLEY = register("soul_sand_valley", SurfaceBuilder.SOUL_SAND_VALLEY.withConfig(SurfaceBuilder.SOUL_SAND_CONFIG));
      STONE = register("stone", SurfaceBuilder.DEFAULT.withConfig(SurfaceBuilder.STONE_CONFIG));
      SWAMP = register("swamp", SurfaceBuilder.SWAMP.withConfig(SurfaceBuilder.GRASS_CONFIG));
      WARPED_FOREST = register("warped_forest", SurfaceBuilder.NETHER_FOREST.withConfig(SurfaceBuilder.WARPED_NYLIUM_CONFIG));
      WOODED_BADLANDS = register("wooded_badlands", SurfaceBuilder.WOODED_BADLANDS.withConfig(SurfaceBuilder.BADLANDS_CONFIG));
   }
}
