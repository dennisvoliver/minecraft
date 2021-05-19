package net.minecraft.world.gen.decorator;

import com.mojang.serialization.Codec;
import java.util.Random;
import java.util.stream.Stream;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.gen.CountConfig;

public abstract class Decorator<DC extends DecoratorConfig> {
   public static final Decorator<NopeDecoratorConfig> NOPE;
   public static final Decorator<ChanceDecoratorConfig> CHANCE;
   public static final Decorator<CountConfig> COUNT;
   public static final Decorator<CountNoiseDecoratorConfig> COUNT_NOISE;
   public static final Decorator<CountNoiseBiasedDecoratorConfig> COUNT_NOISE_BIASED;
   public static final Decorator<CountExtraDecoratorConfig> COUNT_EXTRA;
   public static final Decorator<NopeDecoratorConfig> SQUARE;
   public static final Decorator<NopeDecoratorConfig> HEIGHTMAP;
   public static final Decorator<NopeDecoratorConfig> HEIGHTMAP_SPREAD_DOUBLE;
   public static final Decorator<NopeDecoratorConfig> TOP_SOLID_HEIGHTMAP;
   public static final Decorator<NopeDecoratorConfig> HEIGHTMAP_WORLD_SURFACE;
   public static final Decorator<RangeDecoratorConfig> RANGE;
   public static final Decorator<RangeDecoratorConfig> RANGE_BIASED;
   public static final Decorator<RangeDecoratorConfig> RANGE_VERY_BIASED;
   public static final Decorator<DepthAverageDecoratorConfig> DEPTH_AVERAGE;
   public static final Decorator<NopeDecoratorConfig> SPREAD_32_ABOVE;
   public static final Decorator<CarvingMaskDecoratorConfig> CARVING_MASK;
   public static final Decorator<CountConfig> FIRE;
   public static final Decorator<NopeDecoratorConfig> MAGMA;
   public static final Decorator<NopeDecoratorConfig> EMERALD_ORE;
   public static final Decorator<ChanceDecoratorConfig> LAVA_LAKE;
   public static final Decorator<ChanceDecoratorConfig> WATER_LAKE;
   public static final Decorator<CountConfig> GLOWSTONE;
   public static final Decorator<NopeDecoratorConfig> END_GATEWAY;
   public static final Decorator<NopeDecoratorConfig> DARK_OAK_TREE;
   public static final Decorator<NopeDecoratorConfig> ICEBERG;
   public static final Decorator<NopeDecoratorConfig> END_ISLAND;
   public static final Decorator<DecoratedDecoratorConfig> DECORATED;
   public static final Decorator<CountConfig> COUNT_MULTILAYER;
   private final Codec<ConfiguredDecorator<DC>> codec;

   private static <T extends DecoratorConfig, G extends Decorator<T>> G register(String registryName, G decorator) {
      return (Decorator)Registry.register(Registry.DECORATOR, (String)registryName, decorator);
   }

   public Decorator(Codec<DC> configCodec) {
      this.codec = configCodec.fieldOf("config").xmap((decoratorConfig) -> {
         return new ConfiguredDecorator(this, decoratorConfig);
      }, ConfiguredDecorator::getConfig).codec();
   }

   public ConfiguredDecorator<DC> configure(DC config) {
      return new ConfiguredDecorator(this, config);
   }

   public Codec<ConfiguredDecorator<DC>> getCodec() {
      return this.codec;
   }

   public abstract Stream<BlockPos> getPositions(DecoratorContext context, Random random, DC config, BlockPos pos);

   public String toString() {
      return this.getClass().getSimpleName() + "@" + Integer.toHexString(this.hashCode());
   }

   static {
      NOPE = register("nope", new NopeDecorator(NopeDecoratorConfig.CODEC));
      CHANCE = register("chance", new ChanceDecorator(ChanceDecoratorConfig.CODEC));
      COUNT = register("count", new CountDecorator(CountConfig.CODEC));
      COUNT_NOISE = register("count_noise", new CountNoiseDecorator(CountNoiseDecoratorConfig.CODEC));
      COUNT_NOISE_BIASED = register("count_noise_biased", new CountNoiseBiasedDecorator(CountNoiseBiasedDecoratorConfig.CODEC));
      COUNT_EXTRA = register("count_extra", new CountExtraDecorator(CountExtraDecoratorConfig.CODEC));
      SQUARE = register("square", new SquareDecorator(NopeDecoratorConfig.CODEC));
      HEIGHTMAP = register("heightmap", new MotionBlockingHeightmapDecorator(NopeDecoratorConfig.CODEC));
      HEIGHTMAP_SPREAD_DOUBLE = register("heightmap_spread_double", new HeightmapSpreadDoubleDecorator(NopeDecoratorConfig.CODEC));
      TOP_SOLID_HEIGHTMAP = register("top_solid_heightmap", new TopSolidHeightmapDecorator(NopeDecoratorConfig.CODEC));
      HEIGHTMAP_WORLD_SURFACE = register("heightmap_world_surface", new HeightmapWorldSurfaceDecorator(NopeDecoratorConfig.CODEC));
      RANGE = register("range", new RangeDecorator(RangeDecoratorConfig.CODEC));
      RANGE_BIASED = register("range_biased", new RangeBiasedDecorator(RangeDecoratorConfig.CODEC));
      RANGE_VERY_BIASED = register("range_very_biased", new RangeVeryBiasedDecorator(RangeDecoratorConfig.CODEC));
      DEPTH_AVERAGE = register("depth_average", new DepthAverageDecorator(DepthAverageDecoratorConfig.CODEC));
      SPREAD_32_ABOVE = register("spread_32_above", new Spread32AboveDecorator(NopeDecoratorConfig.CODEC));
      CARVING_MASK = register("carving_mask", new CarvingMaskDecorator(CarvingMaskDecoratorConfig.CODEC));
      FIRE = register("fire", new FireDecorator(CountConfig.CODEC));
      MAGMA = register("magma", new MagmaDecorator(NopeDecoratorConfig.CODEC));
      EMERALD_ORE = register("emerald_ore", new EmeraldOreDecorator(NopeDecoratorConfig.CODEC));
      LAVA_LAKE = register("lava_lake", new LavaLakeDecorator(ChanceDecoratorConfig.CODEC));
      WATER_LAKE = register("water_lake", new WaterLakeDecorator(ChanceDecoratorConfig.CODEC));
      GLOWSTONE = register("glowstone", new GlowstoneDecorator(CountConfig.CODEC));
      END_GATEWAY = register("end_gateway", new EndGatewayDecorator(NopeDecoratorConfig.CODEC));
      DARK_OAK_TREE = register("dark_oak_tree", new DarkOakTreeDecorator(NopeDecoratorConfig.CODEC));
      ICEBERG = register("iceberg", new IcebergDecorator(NopeDecoratorConfig.CODEC));
      END_ISLAND = register("end_island", new EndIslandDecorator(NopeDecoratorConfig.CODEC));
      DECORATED = register("decorated", new DecoratedDecorator(DecoratedDecoratorConfig.CODEC));
      COUNT_MULTILAYER = register("count_multilayer", new CountMultilayerDecorator(CountConfig.CODEC));
   }
}
