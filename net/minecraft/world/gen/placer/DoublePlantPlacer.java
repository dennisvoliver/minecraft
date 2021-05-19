package net.minecraft.world.gen.placer;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.block.BlockState;
import net.minecraft.block.TallPlantBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldAccess;

public class DoublePlantPlacer extends BlockPlacer {
   public static final Codec<DoublePlantPlacer> CODEC = Codec.unit(() -> {
      return INSTANCE;
   });
   public static final DoublePlantPlacer INSTANCE = new DoublePlantPlacer();

   protected BlockPlacerType<?> getType() {
      return BlockPlacerType.DOUBLE_PLANT_PLACER;
   }

   public void generate(WorldAccess world, BlockPos pos, BlockState state, Random random) {
      ((TallPlantBlock)state.getBlock()).placeAt(world, pos, 2);
   }
}
