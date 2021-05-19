package net.minecraft.world.gen.trunk;

import com.mojang.serialization.Codec;
import net.minecraft.util.registry.Registry;

public class TrunkPlacerType<P extends TrunkPlacer> {
   public static final TrunkPlacerType<StraightTrunkPlacer> STRAIGHT_TRUNK_PLACER;
   public static final TrunkPlacerType<ForkingTrunkPlacer> FORKING_TRUNK_PLACER;
   public static final TrunkPlacerType<GiantTrunkPlacer> GIANT_TRUNK_PLACER;
   public static final TrunkPlacerType<MegaJungleTrunkPlacer> MEGA_JUNGLE_TRUNK_PLACER;
   public static final TrunkPlacerType<DarkOakTrunkPlacer> DARK_OAK_TRUNK_PLACER;
   public static final TrunkPlacerType<LargeOakTrunkPlacer> FANCY_TRUNK_PLACER;
   private final Codec<P> codec;

   private static <P extends TrunkPlacer> TrunkPlacerType<P> register(String id, Codec<P> codec) {
      return (TrunkPlacerType)Registry.register(Registry.TRUNK_PLACER_TYPE, (String)id, new TrunkPlacerType(codec));
   }

   private TrunkPlacerType(Codec<P> codec) {
      this.codec = codec;
   }

   public Codec<P> getCodec() {
      return this.codec;
   }

   static {
      STRAIGHT_TRUNK_PLACER = register("straight_trunk_placer", StraightTrunkPlacer.CODEC);
      FORKING_TRUNK_PLACER = register("forking_trunk_placer", ForkingTrunkPlacer.CODEC);
      GIANT_TRUNK_PLACER = register("giant_trunk_placer", GiantTrunkPlacer.CODEC);
      MEGA_JUNGLE_TRUNK_PLACER = register("mega_jungle_trunk_placer", MegaJungleTrunkPlacer.CODEC);
      DARK_OAK_TRUNK_PLACER = register("dark_oak_trunk_placer", DarkOakTrunkPlacer.CODEC);
      FANCY_TRUNK_PLACER = register("fancy_trunk_placer", LargeOakTrunkPlacer.CODEC);
   }
}
