package net.minecraft.world.chunk.light;

import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import java.util.Arrays;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.LightType;
import net.minecraft.world.chunk.ChunkNibbleArray;
import net.minecraft.world.chunk.ChunkProvider;
import net.minecraft.world.chunk.ChunkToNibbleArrayMap;
import net.minecraft.world.chunk.ColumnChunkNibbleArray;

public class SkyLightStorage extends LightStorage<SkyLightStorage.Data> {
   private static final Direction[] LIGHT_REDUCTION_DIRECTIONS;
   private final LongSet field_15820 = new LongOpenHashSet();
   private final LongSet sectionsToUpdate = new LongOpenHashSet();
   private final LongSet sectionsToRemove = new LongOpenHashSet();
   private final LongSet enabledColumns = new LongOpenHashSet();
   private volatile boolean hasUpdates;

   protected SkyLightStorage(ChunkProvider chunkProvider) {
      super(LightType.SKY, chunkProvider, new SkyLightStorage.Data(new Long2ObjectOpenHashMap(), new Long2IntOpenHashMap(), Integer.MAX_VALUE));
   }

   protected int getLight(long blockPos) {
      long l = ChunkSectionPos.fromBlockPos(blockPos);
      int i = ChunkSectionPos.unpackY(l);
      SkyLightStorage.Data data = (SkyLightStorage.Data)this.uncachedStorage;
      int j = data.columnToTopSection.get(ChunkSectionPos.withZeroY(l));
      if (j != data.minSectionY && i < j) {
         ChunkNibbleArray chunkNibbleArray = this.getLightSection(data, l);
         if (chunkNibbleArray == null) {
            for(blockPos = BlockPos.removeChunkSectionLocalY(blockPos); chunkNibbleArray == null; chunkNibbleArray = this.getLightSection(data, l)) {
               l = ChunkSectionPos.offset(l, Direction.UP);
               ++i;
               if (i >= j) {
                  return 15;
               }

               blockPos = BlockPos.add(blockPos, 0, 16, 0);
            }
         }

         return chunkNibbleArray.get(ChunkSectionPos.getLocalCoord(BlockPos.unpackLongX(blockPos)), ChunkSectionPos.getLocalCoord(BlockPos.unpackLongY(blockPos)), ChunkSectionPos.getLocalCoord(BlockPos.unpackLongZ(blockPos)));
      } else {
         return 15;
      }
   }

   protected void onLoadSection(long sectionPos) {
      int i = ChunkSectionPos.unpackY(sectionPos);
      if (((SkyLightStorage.Data)this.storage).minSectionY > i) {
         ((SkyLightStorage.Data)this.storage).minSectionY = i;
         ((SkyLightStorage.Data)this.storage).columnToTopSection.defaultReturnValue(((SkyLightStorage.Data)this.storage).minSectionY);
      }

      long l = ChunkSectionPos.withZeroY(sectionPos);
      int j = ((SkyLightStorage.Data)this.storage).columnToTopSection.get(l);
      if (j < i + 1) {
         ((SkyLightStorage.Data)this.storage).columnToTopSection.put(l, i + 1);
         if (this.enabledColumns.contains(l)) {
            this.enqueueAddSection(sectionPos);
            if (j > ((SkyLightStorage.Data)this.storage).minSectionY) {
               long m = ChunkSectionPos.asLong(ChunkSectionPos.unpackX(sectionPos), j - 1, ChunkSectionPos.unpackZ(sectionPos));
               this.enqueueRemoveSection(m);
            }

            this.checkForUpdates();
         }
      }

   }

   private void enqueueRemoveSection(long sectionPos) {
      this.sectionsToRemove.add(sectionPos);
      this.sectionsToUpdate.remove(sectionPos);
   }

   private void enqueueAddSection(long sectionPos) {
      this.sectionsToUpdate.add(sectionPos);
      this.sectionsToRemove.remove(sectionPos);
   }

   private void checkForUpdates() {
      this.hasUpdates = !this.sectionsToUpdate.isEmpty() || !this.sectionsToRemove.isEmpty();
   }

   protected void onUnloadSection(long sectionPos) {
      long l = ChunkSectionPos.withZeroY(sectionPos);
      boolean bl = this.enabledColumns.contains(l);
      if (bl) {
         this.enqueueRemoveSection(sectionPos);
      }

      int i = ChunkSectionPos.unpackY(sectionPos);
      if (((SkyLightStorage.Data)this.storage).columnToTopSection.get(l) == i + 1) {
         long m;
         for(m = sectionPos; !this.hasSection(m) && this.isAboveMinHeight(i); m = ChunkSectionPos.offset(m, Direction.DOWN)) {
            --i;
         }

         if (this.hasSection(m)) {
            ((SkyLightStorage.Data)this.storage).columnToTopSection.put(l, i + 1);
            if (bl) {
               this.enqueueAddSection(m);
            }
         } else {
            ((SkyLightStorage.Data)this.storage).columnToTopSection.remove(l);
         }
      }

      if (bl) {
         this.checkForUpdates();
      }

   }

   protected void setColumnEnabled(long columnPos, boolean enabled) {
      this.updateAll();
      if (enabled && this.enabledColumns.add(columnPos)) {
         int i = ((SkyLightStorage.Data)this.storage).columnToTopSection.get(columnPos);
         if (i != ((SkyLightStorage.Data)this.storage).minSectionY) {
            long l = ChunkSectionPos.asLong(ChunkSectionPos.unpackX(columnPos), i - 1, ChunkSectionPos.unpackZ(columnPos));
            this.enqueueAddSection(l);
            this.checkForUpdates();
         }
      } else if (!enabled) {
         this.enabledColumns.remove(columnPos);
      }

   }

   protected boolean hasLightUpdates() {
      return super.hasLightUpdates() || this.hasUpdates;
   }

   protected ChunkNibbleArray createSection(long sectionPos) {
      ChunkNibbleArray chunkNibbleArray = (ChunkNibbleArray)this.queuedSections.get(sectionPos);
      if (chunkNibbleArray != null) {
         return chunkNibbleArray;
      } else {
         long l = ChunkSectionPos.offset(sectionPos, Direction.UP);
         int i = ((SkyLightStorage.Data)this.storage).columnToTopSection.get(ChunkSectionPos.withZeroY(sectionPos));
         if (i != ((SkyLightStorage.Data)this.storage).minSectionY && ChunkSectionPos.unpackY(l) < i) {
            ChunkNibbleArray chunkNibbleArray2;
            while((chunkNibbleArray2 = this.getLightSection(l, true)) == null) {
               l = ChunkSectionPos.offset(l, Direction.UP);
            }

            return new ChunkNibbleArray((new ColumnChunkNibbleArray(chunkNibbleArray2, 0)).asByteArray());
         } else {
            return new ChunkNibbleArray();
         }
      }
   }

   protected void updateLight(ChunkLightProvider<SkyLightStorage.Data, ?> lightProvider, boolean doSkylight, boolean skipEdgeLightPropagation) {
      super.updateLight(lightProvider, doSkylight, skipEdgeLightPropagation);
      if (doSkylight) {
         LongIterator var4;
         long l;
         int ag;
         int j;
         if (!this.sectionsToUpdate.isEmpty()) {
            var4 = this.sectionsToUpdate.iterator();

            label160:
            while(true) {
               while(true) {
                  do {
                     do {
                        do {
                           if (!var4.hasNext()) {
                              break label160;
                           }

                           l = (Long)var4.next();
                           ag = this.getLevel(l);
                        } while(ag == 2);
                     } while(this.sectionsToRemove.contains(l));
                  } while(!this.field_15820.add(l));

                  int k;
                  if (ag == 1) {
                     this.removeSection(lightProvider, l);
                     if (this.dirtySections.add(l)) {
                        ((SkyLightStorage.Data)this.storage).replaceWithCopy(l);
                     }

                     Arrays.fill(this.getLightSection(l, true).asByteArray(), (byte)-1);
                     j = ChunkSectionPos.getBlockCoord(ChunkSectionPos.unpackX(l));
                     k = ChunkSectionPos.getBlockCoord(ChunkSectionPos.unpackY(l));
                     int m = ChunkSectionPos.getBlockCoord(ChunkSectionPos.unpackZ(l));
                     Direction[] var11 = LIGHT_REDUCTION_DIRECTIONS;
                     int z = var11.length;

                     long ab;
                     for(int var13 = 0; var13 < z; ++var13) {
                        Direction direction = var11[var13];
                        ab = ChunkSectionPos.offset(l, direction);
                        if ((this.sectionsToRemove.contains(ab) || !this.field_15820.contains(ab) && !this.sectionsToUpdate.contains(ab)) && this.hasSection(ab)) {
                           for(int o = 0; o < 16; ++o) {
                              for(int p = 0; p < 16; ++p) {
                                 long w;
                                 long x;
                                 switch(direction) {
                                 case NORTH:
                                    w = BlockPos.asLong(j + o, k + p, m);
                                    x = BlockPos.asLong(j + o, k + p, m - 1);
                                    break;
                                 case SOUTH:
                                    w = BlockPos.asLong(j + o, k + p, m + 16 - 1);
                                    x = BlockPos.asLong(j + o, k + p, m + 16);
                                    break;
                                 case WEST:
                                    w = BlockPos.asLong(j, k + o, m + p);
                                    x = BlockPos.asLong(j - 1, k + o, m + p);
                                    break;
                                 default:
                                    w = BlockPos.asLong(j + 16 - 1, k + o, m + p);
                                    x = BlockPos.asLong(j + 16, k + o, m + p);
                                 }

                                 lightProvider.updateLevel(w, x, lightProvider.getPropagatedLevel(w, x, 0), true);
                              }
                           }
                        }
                     }

                     for(int y = 0; y < 16; ++y) {
                        for(z = 0; z < 16; ++z) {
                           long aa = BlockPos.asLong(ChunkSectionPos.getBlockCoord(ChunkSectionPos.unpackX(l)) + y, ChunkSectionPos.getBlockCoord(ChunkSectionPos.unpackY(l)), ChunkSectionPos.getBlockCoord(ChunkSectionPos.unpackZ(l)) + z);
                           ab = BlockPos.asLong(ChunkSectionPos.getBlockCoord(ChunkSectionPos.unpackX(l)) + y, ChunkSectionPos.getBlockCoord(ChunkSectionPos.unpackY(l)) - 1, ChunkSectionPos.getBlockCoord(ChunkSectionPos.unpackZ(l)) + z);
                           lightProvider.updateLevel(aa, ab, lightProvider.getPropagatedLevel(aa, ab, 0), true);
                        }
                     }
                  } else {
                     for(j = 0; j < 16; ++j) {
                        for(k = 0; k < 16; ++k) {
                           long ae = BlockPos.asLong(ChunkSectionPos.getBlockCoord(ChunkSectionPos.unpackX(l)) + j, ChunkSectionPos.getBlockCoord(ChunkSectionPos.unpackY(l)) + 16 - 1, ChunkSectionPos.getBlockCoord(ChunkSectionPos.unpackZ(l)) + k);
                           lightProvider.updateLevel(Long.MAX_VALUE, ae, 0, true);
                        }
                     }
                  }
               }
            }
         }

         this.sectionsToUpdate.clear();
         if (!this.sectionsToRemove.isEmpty()) {
            var4 = this.sectionsToRemove.iterator();

            label90:
            while(true) {
               do {
                  do {
                     if (!var4.hasNext()) {
                        break label90;
                     }

                     l = (Long)var4.next();
                  } while(!this.field_15820.remove(l));
               } while(!this.hasSection(l));

               for(ag = 0; ag < 16; ++ag) {
                  for(j = 0; j < 16; ++j) {
                     long ai = BlockPos.asLong(ChunkSectionPos.getBlockCoord(ChunkSectionPos.unpackX(l)) + ag, ChunkSectionPos.getBlockCoord(ChunkSectionPos.unpackY(l)) + 16 - 1, ChunkSectionPos.getBlockCoord(ChunkSectionPos.unpackZ(l)) + j);
                     lightProvider.updateLevel(Long.MAX_VALUE, ai, 15, false);
                  }
               }
            }
         }

         this.sectionsToRemove.clear();
         this.hasUpdates = false;
      }
   }

   protected boolean isAboveMinHeight(int sectionY) {
      return sectionY >= ((SkyLightStorage.Data)this.storage).minSectionY;
   }

   protected boolean isTopmostBlock(long blockPos) {
      int i = BlockPos.unpackLongY(blockPos);
      if ((i & 15) != 15) {
         return false;
      } else {
         long l = ChunkSectionPos.fromBlockPos(blockPos);
         long m = ChunkSectionPos.withZeroY(l);
         if (!this.enabledColumns.contains(m)) {
            return false;
         } else {
            int j = ((SkyLightStorage.Data)this.storage).columnToTopSection.get(m);
            return ChunkSectionPos.getBlockCoord(j) == i + 16;
         }
      }
   }

   protected boolean isAtOrAboveTopmostSection(long sectionPos) {
      long l = ChunkSectionPos.withZeroY(sectionPos);
      int i = ((SkyLightStorage.Data)this.storage).columnToTopSection.get(l);
      return i == ((SkyLightStorage.Data)this.storage).minSectionY || ChunkSectionPos.unpackY(sectionPos) >= i;
   }

   protected boolean isSectionEnabled(long sectionPos) {
      long l = ChunkSectionPos.withZeroY(sectionPos);
      return this.enabledColumns.contains(l);
   }

   static {
      LIGHT_REDUCTION_DIRECTIONS = new Direction[]{Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST};
   }

   public static final class Data extends ChunkToNibbleArrayMap<SkyLightStorage.Data> {
      private int minSectionY;
      private final Long2IntOpenHashMap columnToTopSection;

      public Data(Long2ObjectOpenHashMap<ChunkNibbleArray> arrays, Long2IntOpenHashMap columnToTopSection, int minSectionY) {
         super(arrays);
         this.columnToTopSection = columnToTopSection;
         columnToTopSection.defaultReturnValue(minSectionY);
         this.minSectionY = minSectionY;
      }

      public SkyLightStorage.Data copy() {
         return new SkyLightStorage.Data(this.arrays.clone(), this.columnToTopSection.clone(), this.minSectionY);
      }
   }
}
