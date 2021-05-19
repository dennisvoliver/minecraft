package net.minecraft.block;

import java.util.Random;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;

public class SoulSandBlock extends Block {
   protected static final VoxelShape COLLISION_SHAPE = Block.createCuboidShape(0.0D, 0.0D, 0.0D, 16.0D, 14.0D, 16.0D);

   public SoulSandBlock(AbstractBlock.Settings settings) {
      super(settings);
   }

   public VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
      return COLLISION_SHAPE;
   }

   public VoxelShape getSidesShape(BlockState state, BlockView world, BlockPos pos) {
      return VoxelShapes.fullCube();
   }

   public VoxelShape getVisualShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
      return VoxelShapes.fullCube();
   }

   public void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
      BubbleColumnBlock.update(world, pos.up(), false);
   }

   public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState newState, WorldAccess world, BlockPos pos, BlockPos posFrom) {
      if (direction == Direction.UP && newState.isOf(Blocks.WATER)) {
         world.getBlockTickScheduler().schedule(pos, this, 20);
      }

      return super.getStateForNeighborUpdate(state, direction, newState, world, pos, posFrom);
   }

   public void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState oldState, boolean notify) {
      world.getBlockTickScheduler().schedule(pos, this, 20);
   }

   public boolean canPathfindThrough(BlockState state, BlockView world, BlockPos pos, NavigationType type) {
      return false;
   }
}
