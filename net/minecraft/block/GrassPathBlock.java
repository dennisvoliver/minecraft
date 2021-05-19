package net.minecraft.block;

import java.util.Random;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;

public class GrassPathBlock extends Block {
   protected static final VoxelShape SHAPE;

   protected GrassPathBlock(AbstractBlock.Settings settings) {
      super(settings);
   }

   public boolean hasSidedTransparency(BlockState state) {
      return true;
   }

   public BlockState getPlacementState(ItemPlacementContext ctx) {
      return !this.getDefaultState().canPlaceAt(ctx.getWorld(), ctx.getBlockPos()) ? Block.pushEntitiesUpBeforeBlockChange(this.getDefaultState(), Blocks.DIRT.getDefaultState(), ctx.getWorld(), ctx.getBlockPos()) : super.getPlacementState(ctx);
   }

   public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState newState, WorldAccess world, BlockPos pos, BlockPos posFrom) {
      if (direction == Direction.UP && !state.canPlaceAt(world, pos)) {
         world.getBlockTickScheduler().schedule(pos, this, 1);
      }

      return super.getStateForNeighborUpdate(state, direction, newState, world, pos, posFrom);
   }

   public void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
      FarmlandBlock.setToDirt(state, world, pos);
   }

   public boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
      BlockState blockState = world.getBlockState(pos.up());
      return !blockState.getMaterial().isSolid() || blockState.getBlock() instanceof FenceGateBlock;
   }

   public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
      return SHAPE;
   }

   public boolean canPathfindThrough(BlockState state, BlockView world, BlockPos pos, NavigationType type) {
      return false;
   }

   static {
      SHAPE = FarmlandBlock.SHAPE;
   }
}
