package net.minecraft.world.gen.placer;

import com.mojang.serialization.Codec;
import net.minecraft.util.registry.Registry;

public class BlockPlacerType<P extends BlockPlacer> {
   public static final BlockPlacerType<SimpleBlockPlacer> SIMPLE_BLOCK_PLACER;
   public static final BlockPlacerType<DoublePlantPlacer> DOUBLE_PLANT_PLACER;
   public static final BlockPlacerType<ColumnPlacer> COLUMN_PLACER;
   private final Codec<P> codec;

   private static <P extends BlockPlacer> BlockPlacerType<P> register(String id, Codec<P> codec) {
      return (BlockPlacerType)Registry.register(Registry.BLOCK_PLACER_TYPE, (String)id, new BlockPlacerType(codec));
   }

   private BlockPlacerType(Codec<P> codec) {
      this.codec = codec;
   }

   public Codec<P> getCodec() {
      return this.codec;
   }

   static {
      SIMPLE_BLOCK_PLACER = register("simple_block_placer", SimpleBlockPlacer.CODEC);
      DOUBLE_PLANT_PLACER = register("double_plant_placer", DoublePlantPlacer.CODEC);
      COLUMN_PLACER = register("column_placer", ColumnPlacer.CODEC);
   }
}
