package net.minecraft.block;

import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;

public class GlazedTerracottaBlock extends HorizontalFacingBlock {
   public GlazedTerracottaBlock(AbstractBlock.Settings settings) {
      super(settings);
   }

   protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
      builder.add(FACING);
   }

   public BlockState getPlacementState(ItemPlacementContext ctx) {
      return (BlockState)this.getDefaultState().with(FACING, ctx.getPlayerFacing().getOpposite());
   }

   public PistonBehavior getPistonBehavior(BlockState state) {
      return PistonBehavior.PUSH_ONLY;
   }
}
