package net.minecraft.world.gen.feature;

import com.mojang.serialization.Codec;
import java.util.Iterator;
import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.chunk.ChunkGenerator;

public class ForestRockFeature extends Feature<SingleStateFeatureConfig> {
   public ForestRockFeature(Codec<SingleStateFeatureConfig> codec) {
      super(codec);
   }

   public boolean generate(StructureWorldAccess structureWorldAccess, ChunkGenerator chunkGenerator, Random random, BlockPos blockPos, SingleStateFeatureConfig singleStateFeatureConfig) {
      for(; blockPos.getY() > 3; blockPos = blockPos.down()) {
         if (!structureWorldAccess.isAir(blockPos.down())) {
            Block block = structureWorldAccess.getBlockState(blockPos.down()).getBlock();
            if (isSoil(block) || isStone(block)) {
               break;
            }
         }
      }

      if (blockPos.getY() <= 3) {
         return false;
      } else {
         for(int i = 0; i < 3; ++i) {
            int j = random.nextInt(2);
            int k = random.nextInt(2);
            int l = random.nextInt(2);
            float f = (float)(j + k + l) * 0.333F + 0.5F;
            Iterator var11 = BlockPos.iterate(blockPos.add(-j, -k, -l), blockPos.add(j, k, l)).iterator();

            while(var11.hasNext()) {
               BlockPos blockPos2 = (BlockPos)var11.next();
               if (blockPos2.getSquaredDistance(blockPos) <= (double)(f * f)) {
                  structureWorldAccess.setBlockState(blockPos2, singleStateFeatureConfig.state, 4);
               }
            }

            blockPos = blockPos.add(-1 + random.nextInt(2), -random.nextInt(2), -1 + random.nextInt(2));
         }

         return true;
      }
   }
}
