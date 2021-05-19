package net.minecraft.block.entity;

import net.minecraft.block.Block;
import net.minecraft.inventory.Inventory;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public interface Hopper extends Inventory {
   VoxelShape INSIDE_SHAPE = Block.createCuboidShape(2.0D, 11.0D, 2.0D, 14.0D, 16.0D, 14.0D);
   VoxelShape ABOVE_SHAPE = Block.createCuboidShape(0.0D, 16.0D, 0.0D, 16.0D, 32.0D, 16.0D);
   VoxelShape INPUT_AREA_SHAPE = VoxelShapes.union(INSIDE_SHAPE, ABOVE_SHAPE);

   default VoxelShape getInputAreaShape() {
      return INPUT_AREA_SHAPE;
   }

   @Nullable
   World getWorld();

   double getHopperX();

   double getHopperY();

   double getHopperZ();
}
