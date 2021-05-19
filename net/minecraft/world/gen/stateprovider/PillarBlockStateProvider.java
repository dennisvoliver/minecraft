package net.minecraft.world.gen.stateprovider;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.PillarBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public class PillarBlockStateProvider extends BlockStateProvider {
   public static final Codec<PillarBlockStateProvider> CODEC;
   private final Block block;

   public PillarBlockStateProvider(Block block) {
      this.block = block;
   }

   protected BlockStateProviderType<?> getType() {
      return BlockStateProviderType.ROTATED_BLOCK_PROVIDER;
   }

   public BlockState getBlockState(Random random, BlockPos pos) {
      Direction.Axis axis = Direction.Axis.pickRandomAxis(random);
      return (BlockState)this.block.getDefaultState().with(PillarBlock.AXIS, axis);
   }

   static {
      CODEC = BlockState.CODEC.fieldOf("state").xmap(AbstractBlock.AbstractBlockState::getBlock, Block::getDefaultState).xmap(PillarBlockStateProvider::new, (provider) -> {
         return provider.block;
      }).codec();
   }
}
