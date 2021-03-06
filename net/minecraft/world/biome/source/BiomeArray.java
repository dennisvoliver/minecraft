package net.minecraft.world.biome.source;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.collection.IndexedIterable;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.biome.Biome;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

public class BiomeArray implements BiomeAccess.Storage {
   private static final Logger LOGGER = LogManager.getLogger();
   private static final int HORIZONTAL_SECTION_COUNT = (int)Math.round(Math.log(16.0D) / Math.log(2.0D)) - 2;
   private static final int VERTICAL_SECTION_COUNT = (int)Math.round(Math.log(256.0D) / Math.log(2.0D)) - 2;
   public static final int DEFAULT_LENGTH;
   public static final int HORIZONTAL_BIT_MASK;
   public static final int VERTICAL_BIT_MASK;
   private final IndexedIterable<Biome> field_25831;
   private final Biome[] data;

   public BiomeArray(IndexedIterable<Biome> indexedIterable, Biome[] biomes) {
      this.field_25831 = indexedIterable;
      this.data = biomes;
   }

   private BiomeArray(IndexedIterable<Biome> indexedIterable) {
      this(indexedIterable, new Biome[DEFAULT_LENGTH]);
   }

   @Environment(EnvType.CLIENT)
   public BiomeArray(IndexedIterable<Biome> indexedIterable, int[] is) {
      this(indexedIterable);

      for(int i = 0; i < this.data.length; ++i) {
         int j = is[i];
         Biome biome = (Biome)indexedIterable.get(j);
         if (biome == null) {
            LOGGER.warn("Received invalid biome id: " + j);
            this.data[i] = (Biome)indexedIterable.get(0);
         } else {
            this.data[i] = biome;
         }
      }

   }

   public BiomeArray(IndexedIterable<Biome> indexedIterable, ChunkPos chunkPos, BiomeSource biomeSource) {
      this(indexedIterable);
      int i = chunkPos.getStartX() >> 2;
      int j = chunkPos.getStartZ() >> 2;

      for(int k = 0; k < this.data.length; ++k) {
         int l = k & HORIZONTAL_BIT_MASK;
         int m = k >> HORIZONTAL_SECTION_COUNT + HORIZONTAL_SECTION_COUNT & VERTICAL_BIT_MASK;
         int n = k >> HORIZONTAL_SECTION_COUNT & HORIZONTAL_BIT_MASK;
         this.data[k] = biomeSource.getBiomeForNoiseGen(i + l, m, j + n);
      }

   }

   public BiomeArray(IndexedIterable<Biome> indexedIterable, ChunkPos chunkPos, BiomeSource biomeSource, @Nullable int[] is) {
      this(indexedIterable);
      int i = chunkPos.getStartX() >> 2;
      int j = chunkPos.getStartZ() >> 2;
      int k;
      int l;
      int m;
      int n;
      if (is != null) {
         for(k = 0; k < is.length; ++k) {
            this.data[k] = (Biome)indexedIterable.get(is[k]);
            if (this.data[k] == null) {
               l = k & HORIZONTAL_BIT_MASK;
               m = k >> HORIZONTAL_SECTION_COUNT + HORIZONTAL_SECTION_COUNT & VERTICAL_BIT_MASK;
               n = k >> HORIZONTAL_SECTION_COUNT & HORIZONTAL_BIT_MASK;
               this.data[k] = biomeSource.getBiomeForNoiseGen(i + l, m, j + n);
            }
         }
      } else {
         for(k = 0; k < this.data.length; ++k) {
            l = k & HORIZONTAL_BIT_MASK;
            m = k >> HORIZONTAL_SECTION_COUNT + HORIZONTAL_SECTION_COUNT & VERTICAL_BIT_MASK;
            n = k >> HORIZONTAL_SECTION_COUNT & HORIZONTAL_BIT_MASK;
            this.data[k] = biomeSource.getBiomeForNoiseGen(i + l, m, j + n);
         }
      }

   }

   public int[] toIntArray() {
      int[] is = new int[this.data.length];

      for(int i = 0; i < this.data.length; ++i) {
         is[i] = this.field_25831.getRawId(this.data[i]);
      }

      return is;
   }

   public Biome getBiomeForNoiseGen(int biomeX, int biomeY, int biomeZ) {
      int i = biomeX & HORIZONTAL_BIT_MASK;
      int j = MathHelper.clamp(biomeY, 0, VERTICAL_BIT_MASK);
      int k = biomeZ & HORIZONTAL_BIT_MASK;
      return this.data[j << HORIZONTAL_SECTION_COUNT + HORIZONTAL_SECTION_COUNT | k << HORIZONTAL_SECTION_COUNT | i];
   }

   static {
      DEFAULT_LENGTH = 1 << HORIZONTAL_SECTION_COUNT + HORIZONTAL_SECTION_COUNT + VERTICAL_SECTION_COUNT;
      HORIZONTAL_BIT_MASK = (1 << HORIZONTAL_SECTION_COUNT) - 1;
      VERTICAL_BIT_MASK = (1 << VERTICAL_SECTION_COUNT) - 1;
   }
}
