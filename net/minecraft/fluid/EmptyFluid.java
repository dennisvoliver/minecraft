package net.minecraft.fluid;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.WorldView;

public class EmptyFluid extends Fluid {
   public Item getBucketItem() {
      return Items.AIR;
   }

   public boolean canBeReplacedWith(FluidState state, BlockView world, BlockPos pos, Fluid fluid, Direction direction) {
      return true;
   }

   public Vec3d getVelocity(BlockView world, BlockPos pos, FluidState state) {
      return Vec3d.ZERO;
   }

   public int getTickRate(WorldView world) {
      return 0;
   }

   protected boolean isEmpty() {
      return true;
   }

   protected float getBlastResistance() {
      return 0.0F;
   }

   public float getHeight(FluidState state, BlockView world, BlockPos pos) {
      return 0.0F;
   }

   public float getHeight(FluidState state) {
      return 0.0F;
   }

   protected BlockState toBlockState(FluidState state) {
      return Blocks.AIR.getDefaultState();
   }

   public boolean isStill(FluidState state) {
      return false;
   }

   public int getLevel(FluidState state) {
      return 0;
   }

   public VoxelShape getShape(FluidState state, BlockView world, BlockPos pos) {
      return VoxelShapes.empty();
   }
}
