package net.minecraft.block;

import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import net.minecraft.world.WorldAccess;

public interface Waterloggable extends FluidDrainable, FluidFillable {
   default boolean canFillWithFluid(BlockView world, BlockPos pos, BlockState state, Fluid fluid) {
      return !(Boolean)state.get(Properties.WATERLOGGED) && fluid == Fluids.WATER;
   }

   default boolean tryFillWithFluid(WorldAccess world, BlockPos pos, BlockState state, FluidState fluidState) {
      if (!(Boolean)state.get(Properties.WATERLOGGED) && fluidState.getFluid() == Fluids.WATER) {
         if (!world.isClient()) {
            world.setBlockState(pos, (BlockState)state.with(Properties.WATERLOGGED, true), 3);
            world.getFluidTickScheduler().schedule(pos, fluidState.getFluid(), fluidState.getFluid().getTickRate(world));
         }

         return true;
      } else {
         return false;
      }
   }

   default Fluid tryDrainFluid(WorldAccess world, BlockPos pos, BlockState state) {
      if ((Boolean)state.get(Properties.WATERLOGGED)) {
         world.setBlockState(pos, (BlockState)state.with(Properties.WATERLOGGED, false), 3);
         return Fluids.WATER;
      } else {
         return Fluids.EMPTY;
      }
   }
}
