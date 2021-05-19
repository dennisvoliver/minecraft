package net.minecraft.block;

import java.util.Random;
import net.minecraft.block.enums.DoubleBlockHalf;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.tag.FluidTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import org.jetbrains.annotations.Nullable;

public class SeagrassBlock extends PlantBlock implements Fertilizable, FluidFillable {
   protected static final VoxelShape SHAPE = Block.createCuboidShape(2.0D, 0.0D, 2.0D, 14.0D, 12.0D, 14.0D);

   protected SeagrassBlock(AbstractBlock.Settings settings) {
      super(settings);
   }

   public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
      return SHAPE;
   }

   protected boolean canPlantOnTop(BlockState floor, BlockView world, BlockPos pos) {
      return floor.isSideSolidFullSquare(world, pos, Direction.UP) && !floor.isOf(Blocks.MAGMA_BLOCK);
   }

   @Nullable
   public BlockState getPlacementState(ItemPlacementContext ctx) {
      FluidState fluidState = ctx.getWorld().getFluidState(ctx.getBlockPos());
      return fluidState.isIn(FluidTags.WATER) && fluidState.getLevel() == 8 ? super.getPlacementState(ctx) : null;
   }

   public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState newState, WorldAccess world, BlockPos pos, BlockPos posFrom) {
      BlockState blockState = super.getStateForNeighborUpdate(state, direction, newState, world, pos, posFrom);
      if (!blockState.isAir()) {
         world.getFluidTickScheduler().schedule(pos, Fluids.WATER, Fluids.WATER.getTickRate(world));
      }

      return blockState;
   }

   public boolean isFertilizable(BlockView world, BlockPos pos, BlockState state, boolean isClient) {
      return true;
   }

   public boolean canGrow(World world, Random random, BlockPos pos, BlockState state) {
      return true;
   }

   public FluidState getFluidState(BlockState state) {
      return Fluids.WATER.getStill(false);
   }

   public void grow(ServerWorld world, Random random, BlockPos pos, BlockState state) {
      BlockState blockState = Blocks.TALL_SEAGRASS.getDefaultState();
      BlockState blockState2 = (BlockState)blockState.with(TallSeagrassBlock.HALF, DoubleBlockHalf.UPPER);
      BlockPos blockPos = pos.up();
      if (world.getBlockState(blockPos).isOf(Blocks.WATER)) {
         world.setBlockState(pos, blockState, 2);
         world.setBlockState(blockPos, blockState2, 2);
      }

   }

   public boolean canFillWithFluid(BlockView world, BlockPos pos, BlockState state, Fluid fluid) {
      return false;
   }

   public boolean tryFillWithFluid(WorldAccess world, BlockPos pos, BlockState state, FluidState fluidState) {
      return false;
   }
}
