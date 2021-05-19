package net.minecraft.world.chunk.light;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMaps;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap.Entry;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.LightType;
import net.minecraft.world.SectionDistanceLevelPropagator;
import net.minecraft.world.chunk.ChunkNibbleArray;
import net.minecraft.world.chunk.ChunkProvider;
import net.minecraft.world.chunk.ChunkToNibbleArrayMap;
import org.jetbrains.annotations.Nullable;

/**
 * LightStorage handles the access, storage and propagation of a specific kind of light within the world.
 * For example, separate instances will be used to store block light as opposed to sky light.
 * 
 * <p>The smallest unit within LightStorage is the section. Sections represent a cube of 16x16x16 blocks and their lighting data.
 * In turn, 16 sections stacked on top of each other form a column, which are analogous to the standard 16x256x16 world chunks.
 * 
 * <p>To avoid allocations, LightStorage packs all the coordinate arguments into single long values. Extra care should be taken
 * to ensure that the relevant types are being used where appropriate.
 * 
 * @see SkyLightStorage
 * @see BlockLightStorage
 */
public abstract class LightStorage<M extends ChunkToNibbleArrayMap<M>> extends SectionDistanceLevelPropagator {
   protected static final ChunkNibbleArray EMPTY = new ChunkNibbleArray();
   private static final Direction[] DIRECTIONS = Direction.values();
   private final LightType lightType;
   private final ChunkProvider chunkProvider;
   protected final LongSet readySections = new LongOpenHashSet();
   protected final LongSet markedNotReadySections = new LongOpenHashSet();
   protected final LongSet markedReadySections = new LongOpenHashSet();
   protected volatile M uncachedStorage;
   protected final M storage;
   protected final LongSet dirtySections = new LongOpenHashSet();
   protected final LongSet notifySections = new LongOpenHashSet();
   protected final Long2ObjectMap<ChunkNibbleArray> queuedSections = Long2ObjectMaps.synchronize(new Long2ObjectOpenHashMap());
   private final LongSet queuedEdgeSections = new LongOpenHashSet();
   private final LongSet columnsToRetain = new LongOpenHashSet();
   private final LongSet sectionsToRemove = new LongOpenHashSet();
   protected volatile boolean hasLightUpdates;

   protected LightStorage(LightType lightType, ChunkProvider chunkProvider, M lightData) {
      super(3, 16, 256);
      this.lightType = lightType;
      this.chunkProvider = chunkProvider;
      this.storage = lightData;
      this.uncachedStorage = lightData.copy();
      this.uncachedStorage.disableCache();
   }

   protected boolean hasSection(long sectionPos) {
      return this.getLightSection(sectionPos, true) != null;
   }

   @Nullable
   protected ChunkNibbleArray getLightSection(long sectionPos, boolean cached) {
      return this.getLightSection(cached ? this.storage : this.uncachedStorage, sectionPos);
   }

   @Nullable
   protected ChunkNibbleArray getLightSection(M storage, long sectionPos) {
      return storage.get(sectionPos);
   }

   @Nullable
   public ChunkNibbleArray getLightSection(long sectionPos) {
      ChunkNibbleArray chunkNibbleArray = (ChunkNibbleArray)this.queuedSections.get(sectionPos);
      return chunkNibbleArray != null ? chunkNibbleArray : this.getLightSection(sectionPos, false);
   }

   protected abstract int getLight(long blockPos);

   protected int get(long blockPos) {
      long l = ChunkSectionPos.fromBlockPos(blockPos);
      ChunkNibbleArray chunkNibbleArray = this.getLightSection(l, true);
      return chunkNibbleArray.get(ChunkSectionPos.getLocalCoord(BlockPos.unpackLongX(blockPos)), ChunkSectionPos.getLocalCoord(BlockPos.unpackLongY(blockPos)), ChunkSectionPos.getLocalCoord(BlockPos.unpackLongZ(blockPos)));
   }

   protected void set(long blockPos, int value) {
      long l = ChunkSectionPos.fromBlockPos(blockPos);
      if (this.dirtySections.add(l)) {
         this.storage.replaceWithCopy(l);
      }

      ChunkNibbleArray chunkNibbleArray = this.getLightSection(l, true);
      chunkNibbleArray.set(ChunkSectionPos.getLocalCoord(BlockPos.unpackLongX(blockPos)), ChunkSectionPos.getLocalCoord(BlockPos.unpackLongY(blockPos)), ChunkSectionPos.getLocalCoord(BlockPos.unpackLongZ(blockPos)), value);

      for(int i = -1; i <= 1; ++i) {
         for(int j = -1; j <= 1; ++j) {
            for(int k = -1; k <= 1; ++k) {
               this.notifySections.add(ChunkSectionPos.fromBlockPos(BlockPos.add(blockPos, j, k, i)));
            }
         }
      }

   }

   protected int getLevel(long id) {
      if (id == Long.MAX_VALUE) {
         return 2;
      } else if (this.readySections.contains(id)) {
         return 0;
      } else {
         return !this.sectionsToRemove.contains(id) && this.storage.containsKey(id) ? 1 : 2;
      }
   }

   protected int getInitialLevel(long id) {
      if (this.markedNotReadySections.contains(id)) {
         return 2;
      } else {
         return !this.readySections.contains(id) && !this.markedReadySections.contains(id) ? 2 : 0;
      }
   }

   protected void setLevel(long id, int level) {
      int i = this.getLevel(id);
      if (i != 0 && level == 0) {
         this.readySections.add(id);
         this.markedReadySections.remove(id);
      }

      if (i == 0 && level != 0) {
         this.readySections.remove(id);
         this.markedNotReadySections.remove(id);
      }

      if (i >= 2 && level != 2) {
         if (this.sectionsToRemove.contains(id)) {
            this.sectionsToRemove.remove(id);
         } else {
            this.storage.put(id, this.createSection(id));
            this.dirtySections.add(id);
            this.onLoadSection(id);

            for(int j = -1; j <= 1; ++j) {
               for(int k = -1; k <= 1; ++k) {
                  for(int l = -1; l <= 1; ++l) {
                     this.notifySections.add(ChunkSectionPos.fromBlockPos(BlockPos.add(id, k, l, j)));
                  }
               }
            }
         }
      }

      if (i != 2 && level >= 2) {
         this.sectionsToRemove.add(id);
      }

      this.hasLightUpdates = !this.sectionsToRemove.isEmpty();
   }

   protected ChunkNibbleArray createSection(long sectionPos) {
      ChunkNibbleArray chunkNibbleArray = (ChunkNibbleArray)this.queuedSections.get(sectionPos);
      return chunkNibbleArray != null ? chunkNibbleArray : new ChunkNibbleArray();
   }

   protected void removeSection(ChunkLightProvider<?, ?> storage, long sectionPos) {
      if (storage.getPendingUpdateCount() < 8192) {
         storage.removePendingUpdateIf((mx) -> {
            return ChunkSectionPos.fromBlockPos(mx) == sectionPos;
         });
      } else {
         int i = ChunkSectionPos.getBlockCoord(ChunkSectionPos.unpackX(sectionPos));
         int j = ChunkSectionPos.getBlockCoord(ChunkSectionPos.unpackY(sectionPos));
         int k = ChunkSectionPos.getBlockCoord(ChunkSectionPos.unpackZ(sectionPos));

         for(int l = 0; l < 16; ++l) {
            for(int m = 0; m < 16; ++m) {
               for(int n = 0; n < 16; ++n) {
                  long o = BlockPos.asLong(i + l, j + m, k + n);
                  storage.removePendingUpdate(o);
               }
            }
         }

      }
   }

   protected boolean hasLightUpdates() {
      return this.hasLightUpdates;
   }

   protected void updateLight(ChunkLightProvider<M, ?> lightProvider, boolean doSkylight, boolean skipEdgeLightPropagation) {
      if (this.hasLightUpdates() || !this.queuedSections.isEmpty()) {
         LongIterator var4 = this.sectionsToRemove.iterator();

         long o;
         ChunkNibbleArray chunkNibbleArray3;
         while(var4.hasNext()) {
            o = (Long)var4.next();
            this.removeSection(lightProvider, o);
            ChunkNibbleArray chunkNibbleArray = (ChunkNibbleArray)this.queuedSections.remove(o);
            chunkNibbleArray3 = this.storage.removeChunk(o);
            if (this.columnsToRetain.contains(ChunkSectionPos.withZeroY(o))) {
               if (chunkNibbleArray != null) {
                  this.queuedSections.put(o, chunkNibbleArray);
               } else if (chunkNibbleArray3 != null) {
                  this.queuedSections.put(o, chunkNibbleArray3);
               }
            }
         }

         this.storage.clearCache();
         var4 = this.sectionsToRemove.iterator();

         while(var4.hasNext()) {
            o = (Long)var4.next();
            this.onUnloadSection(o);
         }

         this.sectionsToRemove.clear();
         this.hasLightUpdates = false;
         ObjectIterator objectIterator = this.queuedSections.long2ObjectEntrySet().iterator();

         long q;
         Entry entry2;
         while(objectIterator.hasNext()) {
            entry2 = (Entry)objectIterator.next();
            q = entry2.getLongKey();
            if (this.hasSection(q)) {
               chunkNibbleArray3 = (ChunkNibbleArray)entry2.getValue();
               if (this.storage.get(q) != chunkNibbleArray3) {
                  this.removeSection(lightProvider, q);
                  this.storage.put(q, chunkNibbleArray3);
                  this.dirtySections.add(q);
               }
            }
         }

         this.storage.clearCache();
         if (!skipEdgeLightPropagation) {
            var4 = this.queuedSections.keySet().iterator();

            while(var4.hasNext()) {
               o = (Long)var4.next();
               this.updateSection(lightProvider, o);
            }
         } else {
            var4 = this.queuedEdgeSections.iterator();

            while(var4.hasNext()) {
               o = (Long)var4.next();
               this.updateSection(lightProvider, o);
            }
         }

         this.queuedEdgeSections.clear();
         objectIterator = this.queuedSections.long2ObjectEntrySet().iterator();

         while(objectIterator.hasNext()) {
            entry2 = (Entry)objectIterator.next();
            q = entry2.getLongKey();
            if (this.hasSection(q)) {
               objectIterator.remove();
            }
         }

      }
   }

   private void updateSection(ChunkLightProvider<M, ?> lightProvider, long sectionPos) {
      if (this.hasSection(sectionPos)) {
         int i = ChunkSectionPos.getBlockCoord(ChunkSectionPos.unpackX(sectionPos));
         int j = ChunkSectionPos.getBlockCoord(ChunkSectionPos.unpackY(sectionPos));
         int k = ChunkSectionPos.getBlockCoord(ChunkSectionPos.unpackZ(sectionPos));
         Direction[] var7 = DIRECTIONS;
         int var8 = var7.length;

         for(int var9 = 0; var9 < var8; ++var9) {
            Direction direction = var7[var9];
            long l = ChunkSectionPos.offset(sectionPos, direction);
            if (!this.queuedSections.containsKey(l) && this.hasSection(l)) {
               for(int m = 0; m < 16; ++m) {
                  for(int n = 0; n < 16; ++n) {
                     long y;
                     long z;
                     switch(direction) {
                     case DOWN:
                        y = BlockPos.asLong(i + n, j, k + m);
                        z = BlockPos.asLong(i + n, j - 1, k + m);
                        break;
                     case UP:
                        y = BlockPos.asLong(i + n, j + 16 - 1, k + m);
                        z = BlockPos.asLong(i + n, j + 16, k + m);
                        break;
                     case NORTH:
                        y = BlockPos.asLong(i + m, j + n, k);
                        z = BlockPos.asLong(i + m, j + n, k - 1);
                        break;
                     case SOUTH:
                        y = BlockPos.asLong(i + m, j + n, k + 16 - 1);
                        z = BlockPos.asLong(i + m, j + n, k + 16);
                        break;
                     case WEST:
                        y = BlockPos.asLong(i, j + m, k + n);
                        z = BlockPos.asLong(i - 1, j + m, k + n);
                        break;
                     default:
                        y = BlockPos.asLong(i + 16 - 1, j + m, k + n);
                        z = BlockPos.asLong(i + 16, j + m, k + n);
                     }

                     lightProvider.updateLevel(y, z, lightProvider.getPropagatedLevel(y, z, lightProvider.getLevel(y)), false);
                     lightProvider.updateLevel(z, y, lightProvider.getPropagatedLevel(z, y, lightProvider.getLevel(z)), false);
                  }
               }
            }
         }

      }
   }

   protected void onLoadSection(long sectionPos) {
   }

   protected void onUnloadSection(long sectionPos) {
   }

   protected void setColumnEnabled(long columnPos, boolean enabled) {
   }

   public void setRetainColumn(long sectionPos, boolean retain) {
      if (retain) {
         this.columnsToRetain.add(sectionPos);
      } else {
         this.columnsToRetain.remove(sectionPos);
      }

   }

   protected void enqueueSectionData(long sectionPos, @Nullable ChunkNibbleArray array, boolean bl) {
      if (array != null) {
         this.queuedSections.put(sectionPos, array);
         if (!bl) {
            this.queuedEdgeSections.add(sectionPos);
         }
      } else {
         this.queuedSections.remove(sectionPos);
      }

   }

   protected void setSectionStatus(long sectionPos, boolean notReady) {
      boolean bl = this.readySections.contains(sectionPos);
      if (!bl && !notReady) {
         this.markedReadySections.add(sectionPos);
         this.updateLevel(Long.MAX_VALUE, sectionPos, 0, true);
      }

      if (bl && notReady) {
         this.markedNotReadySections.add(sectionPos);
         this.updateLevel(Long.MAX_VALUE, sectionPos, 2, false);
      }

   }

   protected void updateAll() {
      if (this.hasPendingUpdates()) {
         this.applyPendingUpdates(Integer.MAX_VALUE);
      }

   }

   protected void notifyChanges() {
      if (!this.dirtySections.isEmpty()) {
         M chunkToNibbleArrayMap = this.storage.copy();
         chunkToNibbleArrayMap.disableCache();
         this.uncachedStorage = chunkToNibbleArrayMap;
         this.dirtySections.clear();
      }

      if (!this.notifySections.isEmpty()) {
         LongIterator longIterator = this.notifySections.iterator();

         while(longIterator.hasNext()) {
            long l = longIterator.nextLong();
            this.chunkProvider.onLightUpdate(this.lightType, ChunkSectionPos.from(l));
         }

         this.notifySections.clear();
      }

   }
}
