package net.minecraft.block;

import java.util.Iterator;
import java.util.Random;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.tag.BlockTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.gen.feature.ConfiguredFeatures;

public class MushroomPlantBlock extends PlantBlock implements Fertilizable {
   protected static final VoxelShape SHAPE = Block.createCuboidShape(5.0D, 0.0D, 5.0D, 11.0D, 6.0D, 11.0D);

   public MushroomPlantBlock(AbstractBlock.Settings settings) {
      super(settings);
   }

   public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
      return SHAPE;
   }

   public void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
      if (random.nextInt(25) == 0) {
         int i = 5;
         int j = true;
         Iterator var7 = BlockPos.iterate(pos.add(-4, -1, -4), pos.add(4, 1, 4)).iterator();

         while(var7.hasNext()) {
            BlockPos blockPos = (BlockPos)var7.next();
            if (world.getBlockState(blockPos).isOf(this)) {
               --i;
               if (i <= 0) {
                  return;
               }
            }
         }

         BlockPos blockPos2 = pos.add(random.nextInt(3) - 1, random.nextInt(2) - random.nextInt(2), random.nextInt(3) - 1);

         for(int k = 0; k < 4; ++k) {
            if (world.isAir(blockPos2) && state.canPlaceAt(world, blockPos2)) {
               pos = blockPos2;
            }

            blockPos2 = pos.add(random.nextInt(3) - 1, random.nextInt(2) - random.nextInt(2), random.nextInt(3) - 1);
         }

         if (world.isAir(blockPos2) && state.canPlaceAt(world, blockPos2)) {
            world.setBlockState(blockPos2, state, 2);
         }
      }

   }

   protected boolean canPlantOnTop(BlockState floor, BlockView world, BlockPos pos) {
      return floor.isOpaqueFullCube(world, pos);
   }

   public boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
      BlockPos blockPos = pos.down();
      BlockState blockState = world.getBlockState(blockPos);
      if (blockState.isIn(BlockTags.MUSHROOM_GROW_BLOCK)) {
         return true;
      } else {
         return world.getBaseLightLevel(pos, 0) < 13 && this.canPlantOnTop(blockState, world, blockPos);
      }
   }

   public boolean trySpawningBigMushroom(ServerWorld serverWorld, BlockPos pos, BlockState state, Random random) {
      serverWorld.removeBlock(pos, false);
      ConfiguredFeature configuredFeature3;
      if (this == Blocks.BROWN_MUSHROOM) {
         configuredFeature3 = ConfiguredFeatures.HUGE_BROWN_MUSHROOM;
      } else {
         if (this != Blocks.RED_MUSHROOM) {
            serverWorld.setBlockState(pos, state, 3);
            return false;
         }

         configuredFeature3 = ConfiguredFeatures.HUGE_RED_MUSHROOM;
      }

      if (configuredFeature3.generate(serverWorld, serverWorld.getChunkManager().getChunkGenerator(), random, pos)) {
         return true;
      } else {
         serverWorld.setBlockState(pos, state, 3);
         return false;
      }
   }

   public boolean isFertilizable(BlockView world, BlockPos pos, BlockState state, boolean isClient) {
      return true;
   }

   public boolean canGrow(World world, Random random, BlockPos pos, BlockState state) {
      return (double)random.nextFloat() < 0.4D;
   }

   public void grow(ServerWorld world, Random random, BlockPos pos, BlockState state) {
      this.trySpawningBigMushroom(world, pos, state, random);
   }
}
