package net.minecraft.world.gen.surfacebuilder;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import java.util.Random;
import java.util.stream.IntStream;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.Material;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.noise.OctaveSimplexNoiseSampler;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.ChunkRandom;

public class FrozenOceanSurfaceBuilder extends SurfaceBuilder<TernarySurfaceConfig> {
   protected static final BlockState PACKED_ICE;
   protected static final BlockState SNOW_BLOCK;
   private static final BlockState AIR;
   private static final BlockState GRAVEL;
   private static final BlockState ICE;
   private OctaveSimplexNoiseSampler field_15644;
   private OctaveSimplexNoiseSampler field_15642;
   private long seed;

   public FrozenOceanSurfaceBuilder(Codec<TernarySurfaceConfig> codec) {
      super(codec);
   }

   public void generate(Random random, Chunk chunk, Biome biome, int i, int j, int k, double d, BlockState blockState, BlockState blockState2, int l, long m, TernarySurfaceConfig ternarySurfaceConfig) {
      double e = 0.0D;
      double f = 0.0D;
      BlockPos.Mutable mutable = new BlockPos.Mutable();
      float g = biome.getTemperature(mutable.set(i, 63, j));
      double h = Math.min(Math.abs(d), this.field_15644.sample((double)i * 0.1D, (double)j * 0.1D, false) * 15.0D);
      if (h > 1.8D) {
         double n = 0.09765625D;
         double o = Math.abs(this.field_15642.sample((double)i * 0.09765625D, (double)j * 0.09765625D, false));
         e = h * h * 1.2D;
         double p = Math.ceil(o * 40.0D) + 14.0D;
         if (e > p) {
            e = p;
         }

         if (g > 0.1F) {
            e -= 2.0D;
         }

         if (e > 2.0D) {
            f = (double)l - e - 7.0D;
            e += (double)l;
         } else {
            e = 0.0D;
         }
      }

      int q = i & 15;
      int r = j & 15;
      SurfaceConfig surfaceConfig = biome.getGenerationSettings().getSurfaceConfig();
      BlockState blockState3 = surfaceConfig.getUnderMaterial();
      BlockState blockState4 = surfaceConfig.getTopMaterial();
      BlockState blockState5 = blockState3;
      BlockState blockState6 = blockState4;
      int s = (int)(d / 3.0D + 3.0D + random.nextDouble() * 0.25D);
      int t = -1;
      int u = 0;
      int v = 2 + random.nextInt(4);
      int w = l + 18 + random.nextInt(10);

      for(int x = Math.max(k, (int)e + 1); x >= 0; --x) {
         mutable.set(q, x, r);
         if (chunk.getBlockState(mutable).isAir() && x < (int)e && random.nextDouble() > 0.01D) {
            chunk.setBlockState(mutable, PACKED_ICE, false);
         } else if (chunk.getBlockState(mutable).getMaterial() == Material.WATER && x > (int)f && x < l && f != 0.0D && random.nextDouble() > 0.15D) {
            chunk.setBlockState(mutable, PACKED_ICE, false);
         }

         BlockState blockState7 = chunk.getBlockState(mutable);
         if (blockState7.isAir()) {
            t = -1;
         } else if (!blockState7.isOf(blockState.getBlock())) {
            if (blockState7.isOf(Blocks.PACKED_ICE) && u <= v && x > w) {
               chunk.setBlockState(mutable, SNOW_BLOCK, false);
               ++u;
            }
         } else if (t == -1) {
            if (s <= 0) {
               blockState6 = AIR;
               blockState5 = blockState;
            } else if (x >= l - 4 && x <= l + 1) {
               blockState6 = blockState4;
               blockState5 = blockState3;
            }

            if (x < l && (blockState6 == null || blockState6.isAir())) {
               if (biome.getTemperature(mutable.set(i, x, j)) < 0.15F) {
                  blockState6 = ICE;
               } else {
                  blockState6 = blockState2;
               }
            }

            t = s;
            if (x >= l - 1) {
               chunk.setBlockState(mutable, blockState6, false);
            } else if (x < l - 7 - s) {
               blockState6 = AIR;
               blockState5 = blockState;
               chunk.setBlockState(mutable, GRAVEL, false);
            } else {
               chunk.setBlockState(mutable, blockState5, false);
            }
         } else if (t > 0) {
            --t;
            chunk.setBlockState(mutable, blockState5, false);
            if (t == 0 && blockState5.isOf(Blocks.SAND) && s > 1) {
               t = random.nextInt(4) + Math.max(0, x - 63);
               blockState5 = blockState5.isOf(Blocks.RED_SAND) ? Blocks.RED_SANDSTONE.getDefaultState() : Blocks.SANDSTONE.getDefaultState();
            }
         }
      }

   }

   public void initSeed(long seed) {
      if (this.seed != seed || this.field_15644 == null || this.field_15642 == null) {
         ChunkRandom chunkRandom = new ChunkRandom(seed);
         this.field_15644 = new OctaveSimplexNoiseSampler(chunkRandom, IntStream.rangeClosed(-3, 0));
         this.field_15642 = new OctaveSimplexNoiseSampler(chunkRandom, ImmutableList.of(0));
      }

      this.seed = seed;
   }

   static {
      PACKED_ICE = Blocks.PACKED_ICE.getDefaultState();
      SNOW_BLOCK = Blocks.SNOW_BLOCK.getDefaultState();
      AIR = Blocks.AIR.getDefaultState();
      GRAVEL = Blocks.GRAVEL.getDefaultState();
      ICE = Blocks.ICE.getDefaultState();
   }
}
