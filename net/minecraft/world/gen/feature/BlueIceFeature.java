package net.minecraft.world.gen.feature;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.Material;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.chunk.ChunkGenerator;

public class BlueIceFeature extends Feature<DefaultFeatureConfig> {
   public BlueIceFeature(Codec<DefaultFeatureConfig> codec) {
      super(codec);
   }

   public boolean generate(StructureWorldAccess structureWorldAccess, ChunkGenerator chunkGenerator, Random random, BlockPos blockPos, DefaultFeatureConfig defaultFeatureConfig) {
      if (blockPos.getY() > structureWorldAccess.getSeaLevel() - 1) {
         return false;
      } else if (!structureWorldAccess.getBlockState(blockPos).isOf(Blocks.WATER) && !structureWorldAccess.getBlockState(blockPos.down()).isOf(Blocks.WATER)) {
         return false;
      } else {
         boolean bl = false;
         Direction[] var7 = Direction.values();
         int j = var7.length;

         int k;
         for(k = 0; k < j; ++k) {
            Direction direction = var7[k];
            if (direction != Direction.DOWN && structureWorldAccess.getBlockState(blockPos.offset(direction)).isOf(Blocks.PACKED_ICE)) {
               bl = true;
               break;
            }
         }

         if (!bl) {
            return false;
         } else {
            structureWorldAccess.setBlockState(blockPos, Blocks.BLUE_ICE.getDefaultState(), 2);

            for(int i = 0; i < 200; ++i) {
               j = random.nextInt(5) - random.nextInt(6);
               k = 3;
               if (j < 2) {
                  k += j / 2;
               }

               if (k >= 1) {
                  BlockPos blockPos2 = blockPos.add(random.nextInt(k) - random.nextInt(k), j, random.nextInt(k) - random.nextInt(k));
                  BlockState blockState = structureWorldAccess.getBlockState(blockPos2);
                  if (blockState.getMaterial() == Material.AIR || blockState.isOf(Blocks.WATER) || blockState.isOf(Blocks.PACKED_ICE) || blockState.isOf(Blocks.ICE)) {
                     Direction[] var12 = Direction.values();
                     int var13 = var12.length;

                     for(int var14 = 0; var14 < var13; ++var14) {
                        Direction direction2 = var12[var14];
                        BlockState blockState2 = structureWorldAccess.getBlockState(blockPos2.offset(direction2));
                        if (blockState2.isOf(Blocks.BLUE_ICE)) {
                           structureWorldAccess.setBlockState(blockPos2, Blocks.BLUE_ICE.getDefaultState(), 2);
                           break;
                        }
                     }
                  }
               }
            }

            return true;
         }
      }
   }
}
