package net.minecraft.world.gen.feature;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.ModifiableWorld;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.TestableWorld;
import net.minecraft.world.gen.CountConfig;
import net.minecraft.world.gen.ProbabilityConfig;
import net.minecraft.world.gen.chunk.ChunkGenerator;

public abstract class Feature<FC extends FeatureConfig> {
   public static final Feature<DefaultFeatureConfig> NO_OP;
   public static final Feature<TreeFeatureConfig> TREE;
   public static final FlowerFeature<RandomPatchFeatureConfig> FLOWER;
   public static final FlowerFeature<RandomPatchFeatureConfig> NO_BONEMEAL_FLOWER;
   public static final Feature<RandomPatchFeatureConfig> RANDOM_PATCH;
   public static final Feature<BlockPileFeatureConfig> BLOCK_PILE;
   public static final Feature<SpringFeatureConfig> SPRING_FEATURE;
   public static final Feature<DefaultFeatureConfig> CHORUS_PLANT;
   public static final Feature<EmeraldOreFeatureConfig> EMERALD_ORE;
   public static final Feature<DefaultFeatureConfig> VOID_START_PLATFORM;
   public static final Feature<DefaultFeatureConfig> DESERT_WELL;
   public static final Feature<DefaultFeatureConfig> FOSSIL;
   public static final Feature<HugeMushroomFeatureConfig> HUGE_RED_MUSHROOM;
   public static final Feature<HugeMushroomFeatureConfig> HUGE_BROWN_MUSHROOM;
   public static final Feature<DefaultFeatureConfig> ICE_SPIKE;
   public static final Feature<DefaultFeatureConfig> GLOWSTONE_BLOB;
   public static final Feature<DefaultFeatureConfig> FREEZE_TOP_LAYER;
   public static final Feature<DefaultFeatureConfig> VINES;
   public static final Feature<DefaultFeatureConfig> MONSTER_ROOM;
   public static final Feature<DefaultFeatureConfig> BLUE_ICE;
   public static final Feature<SingleStateFeatureConfig> ICEBERG;
   public static final Feature<SingleStateFeatureConfig> FOREST_ROCK;
   public static final Feature<DiskFeatureConfig> DISK;
   public static final Feature<DiskFeatureConfig> ICE_PATCH;
   public static final Feature<SingleStateFeatureConfig> LAKE;
   public static final Feature<OreFeatureConfig> ORE;
   public static final Feature<EndSpikeFeatureConfig> END_SPIKE;
   public static final Feature<DefaultFeatureConfig> END_ISLAND;
   public static final Feature<EndGatewayFeatureConfig> END_GATEWAY;
   public static final SeagrassFeature SEAGRASS;
   public static final Feature<DefaultFeatureConfig> KELP;
   public static final Feature<DefaultFeatureConfig> CORAL_TREE;
   public static final Feature<DefaultFeatureConfig> CORAL_MUSHROOM;
   public static final Feature<DefaultFeatureConfig> CORAL_CLAW;
   public static final Feature<CountConfig> SEA_PICKLE;
   public static final Feature<SimpleBlockFeatureConfig> SIMPLE_BLOCK;
   public static final Feature<ProbabilityConfig> BAMBOO;
   public static final Feature<HugeFungusFeatureConfig> HUGE_FUNGUS;
   public static final Feature<BlockPileFeatureConfig> NETHER_FOREST_VEGETATION;
   public static final Feature<DefaultFeatureConfig> WEEPING_VINES;
   public static final Feature<DefaultFeatureConfig> TWISTING_VINES;
   public static final Feature<BasaltColumnsFeatureConfig> BASALT_COLUMNS;
   public static final Feature<DeltaFeatureConfig> DELTA_FEATURE;
   public static final Feature<NetherrackReplaceBlobsFeatureConfig> NETHERRACK_REPLACE_BLOBS;
   public static final Feature<FillLayerFeatureConfig> FILL_LAYER;
   public static final BonusChestFeature BONUS_CHEST;
   public static final Feature<DefaultFeatureConfig> BASALT_PILLAR;
   public static final Feature<OreFeatureConfig> NO_SURFACE_ORE;
   public static final Feature<RandomFeatureConfig> RANDOM_SELECTOR;
   public static final Feature<SimpleRandomFeatureConfig> SIMPLE_RANDOM_SELECTOR;
   public static final Feature<RandomBooleanFeatureConfig> RANDOM_BOOLEAN_SELECTOR;
   public static final Feature<DecoratedFeatureConfig> DECORATED;
   private final Codec<ConfiguredFeature<FC, Feature<FC>>> codec;

   private static <C extends FeatureConfig, F extends Feature<C>> F register(String name, F feature) {
      return (Feature)Registry.register(Registry.FEATURE, (String)name, feature);
   }

   public Feature(Codec<FC> configCodec) {
      this.codec = configCodec.fieldOf("config").xmap((config) -> {
         return new ConfiguredFeature(this, config);
      }, (configuredFeature) -> {
         return configuredFeature.config;
      }).codec();
   }

   public Codec<ConfiguredFeature<FC, Feature<FC>>> getCodec() {
      return this.codec;
   }

   public ConfiguredFeature<FC, ?> configure(FC config) {
      return new ConfiguredFeature(this, config);
   }

   protected void setBlockState(ModifiableWorld world, BlockPos pos, BlockState state) {
      world.setBlockState(pos, state, 3);
   }

   public abstract boolean generate(StructureWorldAccess world, ChunkGenerator chunkGenerator, Random random, BlockPos pos, FC config);

   protected static boolean isStone(Block block) {
      return block == Blocks.STONE || block == Blocks.GRANITE || block == Blocks.DIORITE || block == Blocks.ANDESITE;
   }

   public static boolean isSoil(Block block) {
      return block == Blocks.DIRT || block == Blocks.GRASS_BLOCK || block == Blocks.PODZOL || block == Blocks.COARSE_DIRT || block == Blocks.MYCELIUM;
   }

   public static boolean isSoil(TestableWorld world, BlockPos pos) {
      return world.testBlockState(pos, (state) -> {
         return isSoil(state.getBlock());
      });
   }

   public static boolean isAir(TestableWorld world, BlockPos pos) {
      return world.testBlockState(pos, AbstractBlock.AbstractBlockState::isAir);
   }

   static {
      NO_OP = register("no_op", new NoOpFeature(DefaultFeatureConfig.CODEC));
      TREE = register("tree", new TreeFeature(TreeFeatureConfig.CODEC));
      FLOWER = (FlowerFeature)register("flower", new DefaultFlowerFeature(RandomPatchFeatureConfig.CODEC));
      NO_BONEMEAL_FLOWER = (FlowerFeature)register("no_bonemeal_flower", new DefaultFlowerFeature(RandomPatchFeatureConfig.CODEC));
      RANDOM_PATCH = register("random_patch", new RandomPatchFeature(RandomPatchFeatureConfig.CODEC));
      BLOCK_PILE = register("block_pile", new AbstractPileFeature(BlockPileFeatureConfig.CODEC));
      SPRING_FEATURE = register("spring_feature", new SpringFeature(SpringFeatureConfig.CODEC));
      CHORUS_PLANT = register("chorus_plant", new ChorusPlantFeature(DefaultFeatureConfig.CODEC));
      EMERALD_ORE = register("emerald_ore", new EmeraldOreFeature(EmeraldOreFeatureConfig.CODEC));
      VOID_START_PLATFORM = register("void_start_platform", new VoidStartPlatformFeature(DefaultFeatureConfig.CODEC));
      DESERT_WELL = register("desert_well", new DesertWellFeature(DefaultFeatureConfig.CODEC));
      FOSSIL = register("fossil", new FossilFeature(DefaultFeatureConfig.CODEC));
      HUGE_RED_MUSHROOM = register("huge_red_mushroom", new HugeRedMushroomFeature(HugeMushroomFeatureConfig.CODEC));
      HUGE_BROWN_MUSHROOM = register("huge_brown_mushroom", new HugeBrownMushroomFeature(HugeMushroomFeatureConfig.CODEC));
      ICE_SPIKE = register("ice_spike", new IceSpikeFeature(DefaultFeatureConfig.CODEC));
      GLOWSTONE_BLOB = register("glowstone_blob", new GlowstoneBlobFeature(DefaultFeatureConfig.CODEC));
      FREEZE_TOP_LAYER = register("freeze_top_layer", new FreezeTopLayerFeature(DefaultFeatureConfig.CODEC));
      VINES = register("vines", new VinesFeature(DefaultFeatureConfig.CODEC));
      MONSTER_ROOM = register("monster_room", new DungeonFeature(DefaultFeatureConfig.CODEC));
      BLUE_ICE = register("blue_ice", new BlueIceFeature(DefaultFeatureConfig.CODEC));
      ICEBERG = register("iceberg", new IcebergFeature(SingleStateFeatureConfig.CODEC));
      FOREST_ROCK = register("forest_rock", new ForestRockFeature(SingleStateFeatureConfig.CODEC));
      DISK = register("disk", new UnderwaterDiskFeature(DiskFeatureConfig.CODEC));
      ICE_PATCH = register("ice_patch", new IcePatchFeature(DiskFeatureConfig.CODEC));
      LAKE = register("lake", new LakeFeature(SingleStateFeatureConfig.CODEC));
      ORE = register("ore", new OreFeature(OreFeatureConfig.CODEC));
      END_SPIKE = register("end_spike", new EndSpikeFeature(EndSpikeFeatureConfig.CODEC));
      END_ISLAND = register("end_island", new EndIslandFeature(DefaultFeatureConfig.CODEC));
      END_GATEWAY = register("end_gateway", new EndGatewayFeature(EndGatewayFeatureConfig.CODEC));
      SEAGRASS = (SeagrassFeature)register("seagrass", new SeagrassFeature(ProbabilityConfig.CODEC));
      KELP = register("kelp", new KelpFeature(DefaultFeatureConfig.CODEC));
      CORAL_TREE = register("coral_tree", new CoralTreeFeature(DefaultFeatureConfig.CODEC));
      CORAL_MUSHROOM = register("coral_mushroom", new CoralMushroomFeature(DefaultFeatureConfig.CODEC));
      CORAL_CLAW = register("coral_claw", new CoralClawFeature(DefaultFeatureConfig.CODEC));
      SEA_PICKLE = register("sea_pickle", new SeaPickleFeature(CountConfig.CODEC));
      SIMPLE_BLOCK = register("simple_block", new SimpleBlockFeature(SimpleBlockFeatureConfig.CODEC));
      BAMBOO = register("bamboo", new BambooFeature(ProbabilityConfig.CODEC));
      HUGE_FUNGUS = register("huge_fungus", new HugeFungusFeature(HugeFungusFeatureConfig.CODEC));
      NETHER_FOREST_VEGETATION = register("nether_forest_vegetation", new NetherForestVegetationFeature(BlockPileFeatureConfig.CODEC));
      WEEPING_VINES = register("weeping_vines", new WeepingVinesFeature(DefaultFeatureConfig.CODEC));
      TWISTING_VINES = register("twisting_vines", new TwistingVinesFeature(DefaultFeatureConfig.CODEC));
      BASALT_COLUMNS = register("basalt_columns", new BasaltColumnsFeature(BasaltColumnsFeatureConfig.CODEC));
      DELTA_FEATURE = register("delta_feature", new DeltaFeature(DeltaFeatureConfig.CODEC));
      NETHERRACK_REPLACE_BLOBS = register("netherrack_replace_blobs", new NetherrackReplaceBlobsFeature(NetherrackReplaceBlobsFeatureConfig.CODEC));
      FILL_LAYER = register("fill_layer", new FillLayerFeature(FillLayerFeatureConfig.CODEC));
      BONUS_CHEST = (BonusChestFeature)register("bonus_chest", new BonusChestFeature(DefaultFeatureConfig.CODEC));
      BASALT_PILLAR = register("basalt_pillar", new BasaltPillarFeature(DefaultFeatureConfig.CODEC));
      NO_SURFACE_ORE = register("no_surface_ore", new NoSurfaceOreFeature(OreFeatureConfig.CODEC));
      RANDOM_SELECTOR = register("random_selector", new RandomFeature(RandomFeatureConfig.CODEC));
      SIMPLE_RANDOM_SELECTOR = register("simple_random_selector", new SimpleRandomFeature(SimpleRandomFeatureConfig.CODEC));
      RANDOM_BOOLEAN_SELECTOR = register("random_boolean_selector", new RandomBooleanFeature(RandomBooleanFeatureConfig.CODEC));
      DECORATED = register("decorated", new DecoratedFeature(DecoratedFeatureConfig.CODEC));
   }
}
