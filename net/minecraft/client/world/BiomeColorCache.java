package net.minecraft.client.world;

import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import java.util.Arrays;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.IntSupplier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;

@Environment(EnvType.CLIENT)
public class BiomeColorCache {
   private final ThreadLocal<BiomeColorCache.Last> last = ThreadLocal.withInitial(() -> {
      return new BiomeColorCache.Last();
   });
   private final Long2ObjectLinkedOpenHashMap<int[]> colors = new Long2ObjectLinkedOpenHashMap(256, 0.25F);
   private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

   public int getBiomeColor(BlockPos pos, IntSupplier colorFactory) {
      int i = pos.getX() >> 4;
      int j = pos.getZ() >> 4;
      BiomeColorCache.Last last = (BiomeColorCache.Last)this.last.get();
      if (last.x != i || last.z != j) {
         last.x = i;
         last.z = j;
         last.colors = this.getColorArray(i, j);
      }

      int k = pos.getX() & 15;
      int l = pos.getZ() & 15;
      int m = l << 4 | k;
      int n = last.colors[m];
      if (n != -1) {
         return n;
      } else {
         int o = colorFactory.getAsInt();
         last.colors[m] = o;
         return o;
      }
   }

   public void reset(int chunkX, int chunkZ) {
      try {
         this.lock.writeLock().lock();

         for(int i = -1; i <= 1; ++i) {
            for(int j = -1; j <= 1; ++j) {
               long l = ChunkPos.toLong(chunkX + i, chunkZ + j);
               this.colors.remove(l);
            }
         }
      } finally {
         this.lock.writeLock().unlock();
      }

   }

   public void reset() {
      try {
         this.lock.writeLock().lock();
         this.colors.clear();
      } finally {
         this.lock.writeLock().unlock();
      }

   }

   private int[] getColorArray(int chunkX, int chunkZ) {
      long l = ChunkPos.toLong(chunkX, chunkZ);
      this.lock.readLock().lock();

      int[] js;
      try {
         js = (int[])this.colors.get(l);
      } finally {
         this.lock.readLock().unlock();
      }

      if (js != null) {
         return js;
      } else {
         int[] ks = new int[256];
         Arrays.fill(ks, -1);

         try {
            this.lock.writeLock().lock();
            if (this.colors.size() >= 256) {
               this.colors.removeFirst();
            }

            this.colors.put(l, ks);
         } finally {
            this.lock.writeLock().unlock();
         }

         return ks;
      }
   }

   @Environment(EnvType.CLIENT)
   static class Last {
      public int x;
      public int z;
      public int[] colors;

      private Last() {
         this.x = Integer.MIN_VALUE;
         this.z = Integer.MIN_VALUE;
      }
   }
}
