package net.minecraft.world.gen.feature;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.chunk.ChunkGenerator;

public class IcePatchFeature extends DiskFeature {
   public IcePatchFeature(Codec<DiskFeatureConfig> codec) {
      super(codec);
   }

   public boolean generate(StructureWorldAccess structureWorldAccess, ChunkGenerator chunkGenerator, Random random, BlockPos blockPos, DiskFeatureConfig diskFeatureConfig) {
      while(structureWorldAccess.isAir(blockPos) && blockPos.getY() > 2) {
         blockPos = blockPos.down();
      }

      if (!structureWorldAccess.getBlockState(blockPos).isOf(Blocks.SNOW_BLOCK)) {
         return false;
      } else {
         return super.generate(structureWorldAccess, chunkGenerator, random, blockPos, diskFeatureConfig);
      }
   }
}
