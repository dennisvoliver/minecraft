package net.minecraft.world.gen.stateprovider;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;

public class SimpleBlockStateProvider extends BlockStateProvider {
   public static final Codec<SimpleBlockStateProvider> CODEC;
   private final BlockState state;

   public SimpleBlockStateProvider(BlockState state) {
      this.state = state;
   }

   protected BlockStateProviderType<?> getType() {
      return BlockStateProviderType.SIMPLE_STATE_PROVIDER;
   }

   public BlockState getBlockState(Random random, BlockPos pos) {
      return this.state;
   }

   static {
      CODEC = BlockState.CODEC.fieldOf("state").xmap(SimpleBlockStateProvider::new, (simpleBlockStateProvider) -> {
         return simpleBlockStateProvider.state;
      }).codec();
   }
}
