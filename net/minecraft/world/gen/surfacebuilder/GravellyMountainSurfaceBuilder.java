package net.minecraft.world.gen.surfacebuilder;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.block.BlockState;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;

public class GravellyMountainSurfaceBuilder extends SurfaceBuilder<TernarySurfaceConfig> {
   public GravellyMountainSurfaceBuilder(Codec<TernarySurfaceConfig> codec) {
      super(codec);
   }

   public void generate(Random random, Chunk chunk, Biome biome, int i, int j, int k, double d, BlockState blockState, BlockState blockState2, int l, long m, TernarySurfaceConfig ternarySurfaceConfig) {
      if (!(d < -1.0D) && !(d > 2.0D)) {
         if (d > 1.0D) {
            SurfaceBuilder.DEFAULT.generate(random, chunk, biome, i, j, k, d, blockState, blockState2, l, m, SurfaceBuilder.STONE_CONFIG);
         } else {
            SurfaceBuilder.DEFAULT.generate(random, chunk, biome, i, j, k, d, blockState, blockState2, l, m, SurfaceBuilder.GRASS_CONFIG);
         }
      } else {
         SurfaceBuilder.DEFAULT.generate(random, chunk, biome, i, j, k, d, blockState, blockState2, l, m, SurfaceBuilder.GRAVEL_CONFIG);
      }

   }
}
