package net.minecraft.server.world;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Either;
import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import net.minecraft.util.math.ChunkPos;
import org.jetbrains.annotations.Nullable;

public class LevelPrioritizedQueue<T> {
   public static final int LEVEL_COUNT;
   private final List<Long2ObjectLinkedOpenHashMap<List<Optional<T>>>> levelToPosToElements;
   private volatile int firstNonEmptyLevel;
   private final String name;
   private final LongSet blockingChunks;
   private final int maxBlocking;

   public LevelPrioritizedQueue(String name, int maxSize) {
      this.levelToPosToElements = (List)IntStream.range(0, LEVEL_COUNT).mapToObj((i) -> {
         return new Long2ObjectLinkedOpenHashMap();
      }).collect(Collectors.toList());
      this.firstNonEmptyLevel = LEVEL_COUNT;
      this.blockingChunks = new LongOpenHashSet();
      this.name = name;
      this.maxBlocking = maxSize;
   }

   protected void updateLevel(int fromLevel, ChunkPos pos, int toLevel) {
      if (fromLevel < LEVEL_COUNT) {
         Long2ObjectLinkedOpenHashMap<List<Optional<T>>> long2ObjectLinkedOpenHashMap = (Long2ObjectLinkedOpenHashMap)this.levelToPosToElements.get(fromLevel);
         List<Optional<T>> list = (List)long2ObjectLinkedOpenHashMap.remove(pos.toLong());
         if (fromLevel == this.firstNonEmptyLevel) {
            while(this.firstNonEmptyLevel < LEVEL_COUNT && ((Long2ObjectLinkedOpenHashMap)this.levelToPosToElements.get(this.firstNonEmptyLevel)).isEmpty()) {
               ++this.firstNonEmptyLevel;
            }
         }

         if (list != null && !list.isEmpty()) {
            ((List)((Long2ObjectLinkedOpenHashMap)this.levelToPosToElements.get(toLevel)).computeIfAbsent(pos.toLong(), (l) -> {
               return Lists.newArrayList();
            })).addAll(list);
            this.firstNonEmptyLevel = Math.min(this.firstNonEmptyLevel, toLevel);
         }

      }
   }

   protected void add(Optional<T> element, long pos, int level) {
      ((List)((Long2ObjectLinkedOpenHashMap)this.levelToPosToElements.get(level)).computeIfAbsent(pos, (l) -> {
         return Lists.newArrayList();
      })).add(element);
      this.firstNonEmptyLevel = Math.min(this.firstNonEmptyLevel, level);
   }

   protected void remove(long pos, boolean removeElement) {
      Iterator var4 = this.levelToPosToElements.iterator();

      while(var4.hasNext()) {
         Long2ObjectLinkedOpenHashMap<List<Optional<T>>> long2ObjectLinkedOpenHashMap = (Long2ObjectLinkedOpenHashMap)var4.next();
         List<Optional<T>> list = (List)long2ObjectLinkedOpenHashMap.get(pos);
         if (list != null) {
            if (removeElement) {
               list.clear();
            } else {
               list.removeIf((optional) -> {
                  return !optional.isPresent();
               });
            }

            if (list.isEmpty()) {
               long2ObjectLinkedOpenHashMap.remove(pos);
            }
         }
      }

      while(this.firstNonEmptyLevel < LEVEL_COUNT && ((Long2ObjectLinkedOpenHashMap)this.levelToPosToElements.get(this.firstNonEmptyLevel)).isEmpty()) {
         ++this.firstNonEmptyLevel;
      }

      this.blockingChunks.remove(pos);
   }

   private Runnable createBlockingAdder(long pos) {
      return () -> {
         this.blockingChunks.add(pos);
      };
   }

   @Nullable
   public Stream<Either<T, Runnable>> poll() {
      if (this.blockingChunks.size() >= this.maxBlocking) {
         return null;
      } else if (this.firstNonEmptyLevel >= LEVEL_COUNT) {
         return null;
      } else {
         int i = this.firstNonEmptyLevel;
         Long2ObjectLinkedOpenHashMap<List<Optional<T>>> long2ObjectLinkedOpenHashMap = (Long2ObjectLinkedOpenHashMap)this.levelToPosToElements.get(i);
         long l = long2ObjectLinkedOpenHashMap.firstLongKey();

         List list;
         for(list = (List)long2ObjectLinkedOpenHashMap.removeFirst(); this.firstNonEmptyLevel < LEVEL_COUNT && ((Long2ObjectLinkedOpenHashMap)this.levelToPosToElements.get(this.firstNonEmptyLevel)).isEmpty(); ++this.firstNonEmptyLevel) {
         }

         return list.stream().map((optional) -> {
            return (Either)optional.map(Either::left).orElseGet(() -> {
               return Either.right(this.createBlockingAdder(l));
            });
         });
      }
   }

   public String toString() {
      return this.name + " " + this.firstNonEmptyLevel + "...";
   }

   @VisibleForTesting
   LongSet getBlockingChunks() {
      return new LongOpenHashSet(this.blockingChunks);
   }

   static {
      LEVEL_COUNT = ThreadedAnvilChunkStorage.MAX_LEVEL + 2;
   }
}
