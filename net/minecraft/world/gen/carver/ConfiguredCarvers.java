package net.minecraft.world.gen.carver;

import net.minecraft.util.registry.BuiltinRegistries;
import net.minecraft.world.gen.ProbabilityConfig;

public class ConfiguredCarvers {
   public static final ConfiguredCarver<ProbabilityConfig> CAVE;
   public static final ConfiguredCarver<ProbabilityConfig> CANYON;
   public static final ConfiguredCarver<ProbabilityConfig> OCEAN_CAVE;
   public static final ConfiguredCarver<ProbabilityConfig> UNDERWATER_CANYON;
   public static final ConfiguredCarver<ProbabilityConfig> UNDERWATER_CAVE;
   public static final ConfiguredCarver<ProbabilityConfig> NETHER_CAVE;

   private static <WC extends CarverConfig> ConfiguredCarver<WC> register(String id, ConfiguredCarver<WC> configuredCarver) {
      return (ConfiguredCarver)BuiltinRegistries.add(BuiltinRegistries.CONFIGURED_CARVER, (String)id, configuredCarver);
   }

   static {
      CAVE = register("cave", Carver.CAVE.configure(new ProbabilityConfig(0.14285715F)));
      CANYON = register("canyon", Carver.CANYON.configure(new ProbabilityConfig(0.02F)));
      OCEAN_CAVE = register("ocean_cave", Carver.CAVE.configure(new ProbabilityConfig(0.06666667F)));
      UNDERWATER_CANYON = register("underwater_canyon", Carver.UNDERWATER_CANYON.configure(new ProbabilityConfig(0.02F)));
      UNDERWATER_CAVE = register("underwater_cave", Carver.UNDERWATER_CAVE.configure(new ProbabilityConfig(0.06666667F)));
      NETHER_CAVE = register("nether_cave", Carver.NETHER_CAVE.configure(new ProbabilityConfig(0.2F)));
   }
}
