package net.minecraft.block;

import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.WorldAccess;

public class SnowyBlock extends Block {
   public static final BooleanProperty SNOWY;

   protected SnowyBlock(AbstractBlock.Settings settings) {
      super(settings);
      this.setDefaultState((BlockState)((BlockState)this.stateManager.getDefaultState()).with(SNOWY, false));
   }

   public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState newState, WorldAccess world, BlockPos pos, BlockPos posFrom) {
      return direction != Direction.UP ? super.getStateForNeighborUpdate(state, direction, newState, world, pos, posFrom) : (BlockState)state.with(SNOWY, newState.isOf(Blocks.SNOW_BLOCK) || newState.isOf(Blocks.SNOW));
   }

   public BlockState getPlacementState(ItemPlacementContext ctx) {
      BlockState blockState = ctx.getWorld().getBlockState(ctx.getBlockPos().up());
      return (BlockState)this.getDefaultState().with(SNOWY, blockState.isOf(Blocks.SNOW_BLOCK) || blockState.isOf(Blocks.SNOW));
   }

   protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
      builder.add(SNOWY);
   }

   static {
      SNOWY = Properties.SNOWY;
   }
}
