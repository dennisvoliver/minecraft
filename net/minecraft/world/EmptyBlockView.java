package net.minecraft.world;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

public enum EmptyBlockView implements BlockView {
   INSTANCE;

   @Nullable
   public BlockEntity getBlockEntity(BlockPos pos) {
      return null;
   }

   public BlockState getBlockState(BlockPos pos) {
      return Blocks.AIR.getDefaultState();
   }

   public FluidState getFluidState(BlockPos pos) {
      return Fluids.EMPTY.getDefaultState();
   }
}
