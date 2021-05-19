package net.minecraft.world.gen.surfacebuilder;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;

public class WoodedBadlandsSurfaceBuilder extends BadlandsSurfaceBuilder {
   private static final BlockState WHITE_TERRACOTTA;
   private static final BlockState ORANGE_TERRACOTTA;
   private static final BlockState TERRACOTTA;

   public WoodedBadlandsSurfaceBuilder(Codec<TernarySurfaceConfig> codec) {
      super(codec);
   }

   public void generate(Random random, Chunk chunk, Biome biome, int i, int j, int k, double d, BlockState blockState, BlockState blockState2, int l, long m, TernarySurfaceConfig ternarySurfaceConfig) {
      int n = i & 15;
      int o = j & 15;
      BlockState blockState3 = WHITE_TERRACOTTA;
      SurfaceConfig surfaceConfig = biome.getGenerationSettings().getSurfaceConfig();
      BlockState blockState4 = surfaceConfig.getUnderMaterial();
      BlockState blockState5 = surfaceConfig.getTopMaterial();
      BlockState blockState6 = blockState4;
      int p = (int)(d / 3.0D + 3.0D + random.nextDouble() * 0.25D);
      boolean bl = Math.cos(d / 3.0D * 3.141592653589793D) > 0.0D;
      int q = -1;
      boolean bl2 = false;
      int r = 0;
      BlockPos.Mutable mutable = new BlockPos.Mutable();

      for(int s = k; s >= 0; --s) {
         if (r < 15) {
            mutable.set(n, s, o);
            BlockState blockState7 = chunk.getBlockState(mutable);
            if (blockState7.isAir()) {
               q = -1;
            } else if (blockState7.isOf(blockState.getBlock())) {
               if (q == -1) {
                  bl2 = false;
                  if (p <= 0) {
                     blockState3 = Blocks.AIR.getDefaultState();
                     blockState6 = blockState;
                  } else if (s >= l - 4 && s <= l + 1) {
                     blockState3 = WHITE_TERRACOTTA;
                     blockState6 = blockState4;
                  }

                  if (s < l && (blockState3 == null || blockState3.isAir())) {
                     blockState3 = blockState2;
                  }

                  q = p + Math.max(0, s - l);
                  if (s >= l - 1) {
                     if (s > 86 + p * 2) {
                        if (bl) {
                           chunk.setBlockState(mutable, Blocks.COARSE_DIRT.getDefaultState(), false);
                        } else {
                           chunk.setBlockState(mutable, Blocks.GRASS_BLOCK.getDefaultState(), false);
                        }
                     } else if (s > l + 3 + p) {
                        BlockState blockState10;
                        if (s >= 64 && s <= 127) {
                           if (bl) {
                              blockState10 = TERRACOTTA;
                           } else {
                              blockState10 = this.calculateLayerBlockState(i, s, j);
                           }
                        } else {
                           blockState10 = ORANGE_TERRACOTTA;
                        }

                        chunk.setBlockState(mutable, blockState10, false);
                     } else {
                        chunk.setBlockState(mutable, blockState5, false);
                        bl2 = true;
                     }
                  } else {
                     chunk.setBlockState(mutable, blockState6, false);
                     if (blockState6 == WHITE_TERRACOTTA) {
                        chunk.setBlockState(mutable, ORANGE_TERRACOTTA, false);
                     }
                  }
               } else if (q > 0) {
                  --q;
                  if (bl2) {
                     chunk.setBlockState(mutable, ORANGE_TERRACOTTA, false);
                  } else {
                     chunk.setBlockState(mutable, this.calculateLayerBlockState(i, s, j), false);
                  }
               }

               ++r;
            }
         }
      }

   }

   static {
      WHITE_TERRACOTTA = Blocks.WHITE_TERRACOTTA.getDefaultState();
      ORANGE_TERRACOTTA = Blocks.ORANGE_TERRACOTTA.getDefaultState();
      TERRACOTTA = Blocks.TERRACOTTA.getDefaultState();
   }
}
