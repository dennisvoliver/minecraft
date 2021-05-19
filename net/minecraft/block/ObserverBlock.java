package net.minecraft.block;

import java.util.Random;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;

public class ObserverBlock extends FacingBlock {
   public static final BooleanProperty POWERED;

   public ObserverBlock(AbstractBlock.Settings settings) {
      super(settings);
      this.setDefaultState((BlockState)((BlockState)((BlockState)this.stateManager.getDefaultState()).with(FACING, Direction.SOUTH)).with(POWERED, false));
   }

   protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
      builder.add(FACING, POWERED);
   }

   public BlockState rotate(BlockState state, BlockRotation rotation) {
      return (BlockState)state.with(FACING, rotation.rotate((Direction)state.get(FACING)));
   }

   public BlockState mirror(BlockState state, BlockMirror mirror) {
      return state.rotate(mirror.getRotation((Direction)state.get(FACING)));
   }

   public void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
      if ((Boolean)state.get(POWERED)) {
         world.setBlockState(pos, (BlockState)state.with(POWERED, false), 2);
      } else {
         world.setBlockState(pos, (BlockState)state.with(POWERED, true), 2);
         world.getBlockTickScheduler().schedule(pos, this, 2);
      }

      this.updateNeighbors(world, pos, state);
   }

   public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState newState, WorldAccess world, BlockPos pos, BlockPos posFrom) {
      if (state.get(FACING) == direction && !(Boolean)state.get(POWERED)) {
         this.scheduleTick(world, pos);
      }

      return super.getStateForNeighborUpdate(state, direction, newState, world, pos, posFrom);
   }

   private void scheduleTick(WorldAccess world, BlockPos pos) {
      if (!world.isClient() && !world.getBlockTickScheduler().isScheduled(pos, this)) {
         world.getBlockTickScheduler().schedule(pos, this, 2);
      }

   }

   protected void updateNeighbors(World world, BlockPos pos, BlockState state) {
      Direction direction = (Direction)state.get(FACING);
      BlockPos blockPos = pos.offset(direction.getOpposite());
      world.updateNeighbor(blockPos, this, pos);
      world.updateNeighborsExcept(blockPos, this, direction);
   }

   public boolean emitsRedstonePower(BlockState state) {
      return true;
   }

   public int getStrongRedstonePower(BlockState state, BlockView world, BlockPos pos, Direction direction) {
      return state.getWeakRedstonePower(world, pos, direction);
   }

   public int getWeakRedstonePower(BlockState state, BlockView world, BlockPos pos, Direction direction) {
      return (Boolean)state.get(POWERED) && state.get(FACING) == direction ? 15 : 0;
   }

   public void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState oldState, boolean notify) {
      if (!state.isOf(oldState.getBlock())) {
         if (!world.isClient() && (Boolean)state.get(POWERED) && !world.getBlockTickScheduler().isScheduled(pos, this)) {
            BlockState blockState = (BlockState)state.with(POWERED, false);
            world.setBlockState(pos, blockState, 18);
            this.updateNeighbors(world, pos, blockState);
         }

      }
   }

   public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
      if (!state.isOf(newState.getBlock())) {
         if (!world.isClient && (Boolean)state.get(POWERED) && world.getBlockTickScheduler().isScheduled(pos, this)) {
            this.updateNeighbors(world, pos, (BlockState)state.with(POWERED, false));
         }

      }
   }

   public BlockState getPlacementState(ItemPlacementContext ctx) {
      return (BlockState)this.getDefaultState().with(FACING, ctx.getPlayerLookDirection().getOpposite().getOpposite());
   }

   static {
      POWERED = Properties.POWERED;
   }
}
