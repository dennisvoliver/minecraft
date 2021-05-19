package net.minecraft.block;

import java.util.Random;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

public class FernBlock extends PlantBlock implements Fertilizable {
   protected static final VoxelShape SHAPE = Block.createCuboidShape(2.0D, 0.0D, 2.0D, 14.0D, 13.0D, 14.0D);

   protected FernBlock(AbstractBlock.Settings settings) {
      super(settings);
   }

   public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
      return SHAPE;
   }

   public boolean isFertilizable(BlockView world, BlockPos pos, BlockState state, boolean isClient) {
      return true;
   }

   public boolean canGrow(World world, Random random, BlockPos pos, BlockState state) {
      return true;
   }

   public void grow(ServerWorld world, Random random, BlockPos pos, BlockState state) {
      TallPlantBlock tallPlantBlock = (TallPlantBlock)((TallPlantBlock)(this == Blocks.FERN ? Blocks.LARGE_FERN : Blocks.TALL_GRASS));
      if (tallPlantBlock.getDefaultState().canPlaceAt(world, pos) && world.isAir(pos.up())) {
         tallPlantBlock.placeAt(world, pos, 2);
      }

   }

   public AbstractBlock.OffsetType getOffsetType() {
      return AbstractBlock.OffsetType.XYZ;
   }
}
