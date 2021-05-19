package net.minecraft.world.gen.surfacebuilder;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.UnmodifiableIterator;
import com.google.common.collect.ImmutableMap.Builder;
import com.mojang.serialization.Codec;
import java.util.Comparator;
import java.util.Random;
import java.util.Map.Entry;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.noise.OctavePerlinNoiseSampler;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.ChunkRandom;

public abstract class AbstractNetherSurfaceBuilder extends SurfaceBuilder<TernarySurfaceConfig> {
   private long seed;
   private ImmutableMap<BlockState, OctavePerlinNoiseSampler> surfaceNoises = ImmutableMap.of();
   private ImmutableMap<BlockState, OctavePerlinNoiseSampler> underLavaNoises = ImmutableMap.of();
   private OctavePerlinNoiseSampler shoreNoise;

   public AbstractNetherSurfaceBuilder(Codec<TernarySurfaceConfig> codec) {
      super(codec);
   }

   public void generate(Random random, Chunk chunk, Biome biome, int i, int j, int k, double d, BlockState blockState, BlockState blockState2, int l, long m, TernarySurfaceConfig ternarySurfaceConfig) {
      int n = l + 1;
      int o = i & 15;
      int p = j & 15;
      int q = (int)(d / 3.0D + 3.0D + random.nextDouble() * 0.25D);
      int r = (int)(d / 3.0D + 3.0D + random.nextDouble() * 0.25D);
      double e = 0.03125D;
      boolean bl = this.shoreNoise.sample((double)i * 0.03125D, 109.0D, (double)j * 0.03125D) * 75.0D + random.nextDouble() > 0.0D;
      BlockState blockState3 = (BlockState)((Entry)this.underLavaNoises.entrySet().stream().max(Comparator.comparing((entry) -> {
         return ((OctavePerlinNoiseSampler)entry.getValue()).sample((double)i, (double)l, (double)j);
      })).get()).getKey();
      BlockState blockState4 = (BlockState)((Entry)this.surfaceNoises.entrySet().stream().max(Comparator.comparing((entry) -> {
         return ((OctavePerlinNoiseSampler)entry.getValue()).sample((double)i, (double)l, (double)j);
      })).get()).getKey();
      BlockPos.Mutable mutable = new BlockPos.Mutable();
      BlockState blockState5 = chunk.getBlockState(mutable.set(o, 128, p));

      for(int s = 127; s >= 0; --s) {
         mutable.set(o, s, p);
         BlockState blockState6 = chunk.getBlockState(mutable);
         int u;
         if (blockState5.isOf(blockState.getBlock()) && (blockState6.isAir() || blockState6 == blockState2)) {
            for(u = 0; u < q; ++u) {
               mutable.move(Direction.UP);
               if (!chunk.getBlockState(mutable).isOf(blockState.getBlock())) {
                  break;
               }

               chunk.setBlockState(mutable, blockState3, false);
            }

            mutable.set(o, s, p);
         }

         if ((blockState5.isAir() || blockState5 == blockState2) && blockState6.isOf(blockState.getBlock())) {
            for(u = 0; u < r && chunk.getBlockState(mutable).isOf(blockState.getBlock()); ++u) {
               if (bl && s >= n - 4 && s <= n + 1) {
                  chunk.setBlockState(mutable, this.getLavaShoreState(), false);
               } else {
                  chunk.setBlockState(mutable, blockState4, false);
               }

               mutable.move(Direction.DOWN);
            }
         }

         blockState5 = blockState6;
      }

   }

   public void initSeed(long seed) {
      if (this.seed != seed || this.shoreNoise == null || this.surfaceNoises.isEmpty() || this.underLavaNoises.isEmpty()) {
         this.surfaceNoises = createNoisesForStates(this.getSurfaceStates(), seed);
         this.underLavaNoises = createNoisesForStates(this.getUnderLavaStates(), seed + (long)this.surfaceNoises.size());
         this.shoreNoise = new OctavePerlinNoiseSampler(new ChunkRandom(seed + (long)this.surfaceNoises.size() + (long)this.underLavaNoises.size()), ImmutableList.of(0));
      }

      this.seed = seed;
   }

   private static ImmutableMap<BlockState, OctavePerlinNoiseSampler> createNoisesForStates(ImmutableList<BlockState> states, long seed) {
      Builder<BlockState, OctavePerlinNoiseSampler> builder = new Builder();

      for(UnmodifiableIterator var4 = states.iterator(); var4.hasNext(); ++seed) {
         BlockState blockState = (BlockState)var4.next();
         builder.put(blockState, new OctavePerlinNoiseSampler(new ChunkRandom(seed), ImmutableList.of(-4)));
      }

      return builder.build();
   }

   protected abstract ImmutableList<BlockState> getSurfaceStates();

   protected abstract ImmutableList<BlockState> getUnderLavaStates();

   /**
    * Returns the state that will make up the boundary between the land and the lava ocean.
    */
   protected abstract BlockState getLavaShoreState();
}
