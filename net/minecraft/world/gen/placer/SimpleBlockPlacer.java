package net.minecraft.world.gen.placer;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldAccess;

public class SimpleBlockPlacer extends BlockPlacer {
   public static final Codec<SimpleBlockPlacer> CODEC = Codec.unit(() -> {
      return INSTANCE;
   });
   public static final SimpleBlockPlacer INSTANCE = new SimpleBlockPlacer();

   protected BlockPlacerType<?> getType() {
      return BlockPlacerType.SIMPLE_BLOCK_PLACER;
   }

   public void generate(WorldAccess world, BlockPos pos, BlockState state, Random random) {
      world.setBlockState(pos, state, 2);
   }
}
