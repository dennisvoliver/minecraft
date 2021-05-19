package net.minecraft.world.gen.feature;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.block.BlockState;
import net.minecraft.tag.FluidTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.Heightmap;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.chunk.ChunkGenerator;

public class RandomPatchFeature extends Feature<RandomPatchFeatureConfig> {
   public RandomPatchFeature(Codec<RandomPatchFeatureConfig> codec) {
      super(codec);
   }

   public boolean generate(StructureWorldAccess structureWorldAccess, ChunkGenerator chunkGenerator, Random random, BlockPos blockPos, RandomPatchFeatureConfig randomPatchFeatureConfig) {
      BlockState blockState = randomPatchFeatureConfig.stateProvider.getBlockState(random, blockPos);
      BlockPos blockPos3;
      if (randomPatchFeatureConfig.project) {
         blockPos3 = structureWorldAccess.getTopPosition(Heightmap.Type.WORLD_SURFACE_WG, blockPos);
      } else {
         blockPos3 = blockPos;
      }

      int i = 0;
      BlockPos.Mutable mutable = new BlockPos.Mutable();

      for(int j = 0; j < randomPatchFeatureConfig.tries; ++j) {
         mutable.set((Vec3i)blockPos3, random.nextInt(randomPatchFeatureConfig.spreadX + 1) - random.nextInt(randomPatchFeatureConfig.spreadX + 1), random.nextInt(randomPatchFeatureConfig.spreadY + 1) - random.nextInt(randomPatchFeatureConfig.spreadY + 1), random.nextInt(randomPatchFeatureConfig.spreadZ + 1) - random.nextInt(randomPatchFeatureConfig.spreadZ + 1));
         BlockPos blockPos4 = mutable.down();
         BlockState blockState2 = structureWorldAccess.getBlockState(blockPos4);
         if ((structureWorldAccess.isAir(mutable) || randomPatchFeatureConfig.canReplace && structureWorldAccess.getBlockState(mutable).getMaterial().isReplaceable()) && blockState.canPlaceAt(structureWorldAccess, mutable) && (randomPatchFeatureConfig.whitelist.isEmpty() || randomPatchFeatureConfig.whitelist.contains(blockState2.getBlock())) && !randomPatchFeatureConfig.blacklist.contains(blockState2) && (!randomPatchFeatureConfig.needsWater || structureWorldAccess.getFluidState(blockPos4.west()).isIn(FluidTags.WATER) || structureWorldAccess.getFluidState(blockPos4.east()).isIn(FluidTags.WATER) || structureWorldAccess.getFluidState(blockPos4.north()).isIn(FluidTags.WATER) || structureWorldAccess.getFluidState(blockPos4.south()).isIn(FluidTags.WATER))) {
            randomPatchFeatureConfig.blockPlacer.generate(structureWorldAccess, mutable, blockState, random);
            ++i;
         }
      }

      return i > 0;
   }
}
