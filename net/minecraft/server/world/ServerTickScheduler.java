package net.minecraft.server.world;

import com.google.common.collect.Lists;
import com.google.common.collect.Queues;
import com.google.common.collect.Sets;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.util.Identifier;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.crash.CrashReportSection;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.ScheduledTick;
import net.minecraft.world.TickPriority;
import net.minecraft.world.TickScheduler;
import org.jetbrains.annotations.Nullable;

public class ServerTickScheduler<T> implements TickScheduler<T> {
   protected final Predicate<T> invalidObjPredicate;
   private final Function<T, Identifier> idToName;
   private final Set<ScheduledTick<T>> scheduledTickActions = Sets.newHashSet();
   private final TreeSet<ScheduledTick<T>> scheduledTickActionsInOrder = Sets.newTreeSet(ScheduledTick.getComparator());
   private final ServerWorld world;
   private final Queue<ScheduledTick<T>> currentTickActions = Queues.newArrayDeque();
   private final List<ScheduledTick<T>> consumedTickActions = Lists.newArrayList();
   private final Consumer<ScheduledTick<T>> tickConsumer;

   public ServerTickScheduler(ServerWorld world, Predicate<T> invalidObjPredicate, Function<T, Identifier> idToName, Consumer<ScheduledTick<T>> consumer) {
      this.invalidObjPredicate = invalidObjPredicate;
      this.idToName = idToName;
      this.world = world;
      this.tickConsumer = consumer;
   }

   public void tick() {
      int i = this.scheduledTickActionsInOrder.size();
      if (i != this.scheduledTickActions.size()) {
         throw new IllegalStateException("TickNextTick list out of synch");
      } else {
         if (i > 65536) {
            i = 65536;
         }

         ServerChunkManager serverChunkManager = this.world.getChunkManager();
         Iterator<ScheduledTick<T>> iterator = this.scheduledTickActionsInOrder.iterator();
         this.world.getProfiler().push("cleaning");

         ScheduledTick scheduledTick2;
         while(i > 0 && iterator.hasNext()) {
            scheduledTick2 = (ScheduledTick)iterator.next();
            if (scheduledTick2.time > this.world.getTime()) {
               break;
            }

            if (serverChunkManager.shouldTickBlock(scheduledTick2.pos)) {
               iterator.remove();
               this.scheduledTickActions.remove(scheduledTick2);
               this.currentTickActions.add(scheduledTick2);
               --i;
            }
         }

         this.world.getProfiler().swap("ticking");

         while((scheduledTick2 = (ScheduledTick)this.currentTickActions.poll()) != null) {
            if (serverChunkManager.shouldTickBlock(scheduledTick2.pos)) {
               try {
                  this.consumedTickActions.add(scheduledTick2);
                  this.tickConsumer.accept(scheduledTick2);
               } catch (Throwable var8) {
                  CrashReport crashReport = CrashReport.create(var8, "Exception while ticking");
                  CrashReportSection crashReportSection = crashReport.addElement("Block being ticked");
                  CrashReportSection.addBlockInfo(crashReportSection, scheduledTick2.pos, (BlockState)null);
                  throw new CrashException(crashReport);
               }
            } else {
               this.schedule(scheduledTick2.pos, scheduledTick2.getObject(), 0);
            }
         }

         this.world.getProfiler().pop();
         this.consumedTickActions.clear();
         this.currentTickActions.clear();
      }
   }

   public boolean isTicking(BlockPos pos, T object) {
      return this.currentTickActions.contains(new ScheduledTick(pos, object));
   }

   public List<ScheduledTick<T>> getScheduledTicksInChunk(ChunkPos chunkPos, boolean updateState, boolean getStaleTicks) {
      int i = (chunkPos.x << 4) - 2;
      int j = i + 16 + 2;
      int k = (chunkPos.z << 4) - 2;
      int l = k + 16 + 2;
      return this.getScheduledTicks(new BlockBox(i, 0, k, j, 256, l), updateState, getStaleTicks);
   }

   public List<ScheduledTick<T>> getScheduledTicks(BlockBox bounds, boolean updateState, boolean getStaleTicks) {
      List<ScheduledTick<T>> list = this.transferTicksInBounds((List)null, this.scheduledTickActionsInOrder, bounds, updateState);
      if (updateState && list != null) {
         this.scheduledTickActions.removeAll(list);
      }

      list = this.transferTicksInBounds(list, this.currentTickActions, bounds, updateState);
      if (!getStaleTicks) {
         list = this.transferTicksInBounds(list, this.consumedTickActions, bounds, updateState);
      }

      return list == null ? Collections.emptyList() : list;
   }

   @Nullable
   private List<ScheduledTick<T>> transferTicksInBounds(@Nullable List<ScheduledTick<T>> dst, Collection<ScheduledTick<T>> src, BlockBox bounds, boolean move) {
      Iterator iterator = src.iterator();

      while(iterator.hasNext()) {
         ScheduledTick<T> scheduledTick = (ScheduledTick)iterator.next();
         BlockPos blockPos = scheduledTick.pos;
         if (blockPos.getX() >= bounds.minX && blockPos.getX() < bounds.maxX && blockPos.getZ() >= bounds.minZ && blockPos.getZ() < bounds.maxZ) {
            if (move) {
               iterator.remove();
            }

            if (dst == null) {
               dst = Lists.newArrayList();
            }

            ((List)dst).add(scheduledTick);
         }
      }

      return (List)dst;
   }

   public void copyScheduledTicks(BlockBox box, BlockPos offset) {
      List<ScheduledTick<T>> list = this.getScheduledTicks(box, false, false);
      Iterator var4 = list.iterator();

      while(var4.hasNext()) {
         ScheduledTick<T> scheduledTick = (ScheduledTick)var4.next();
         if (box.contains(scheduledTick.pos)) {
            BlockPos blockPos = scheduledTick.pos.add(offset);
            T object = scheduledTick.getObject();
            this.addScheduledTick(new ScheduledTick(blockPos, object, scheduledTick.time, scheduledTick.priority));
         }
      }

   }

   public ListTag toTag(ChunkPos chunkPos) {
      List<ScheduledTick<T>> list = this.getScheduledTicksInChunk(chunkPos, false, true);
      return serializeScheduledTicks(this.idToName, list, this.world.getTime());
   }

   private static <T> ListTag serializeScheduledTicks(Function<T, Identifier> identifierProvider, Iterable<ScheduledTick<T>> scheduledTicks, long time) {
      ListTag listTag = new ListTag();
      Iterator var5 = scheduledTicks.iterator();

      while(var5.hasNext()) {
         ScheduledTick<T> scheduledTick = (ScheduledTick)var5.next();
         CompoundTag compoundTag = new CompoundTag();
         compoundTag.putString("i", ((Identifier)identifierProvider.apply(scheduledTick.getObject())).toString());
         compoundTag.putInt("x", scheduledTick.pos.getX());
         compoundTag.putInt("y", scheduledTick.pos.getY());
         compoundTag.putInt("z", scheduledTick.pos.getZ());
         compoundTag.putInt("t", (int)(scheduledTick.time - time));
         compoundTag.putInt("p", scheduledTick.priority.getIndex());
         listTag.add(compoundTag);
      }

      return listTag;
   }

   public boolean isScheduled(BlockPos pos, T object) {
      return this.scheduledTickActions.contains(new ScheduledTick(pos, object));
   }

   public void schedule(BlockPos pos, T object, int delay, TickPriority priority) {
      if (!this.invalidObjPredicate.test(object)) {
         this.addScheduledTick(new ScheduledTick(pos, object, (long)delay + this.world.getTime(), priority));
      }

   }

   private void addScheduledTick(ScheduledTick<T> scheduledTick) {
      if (!this.scheduledTickActions.contains(scheduledTick)) {
         this.scheduledTickActions.add(scheduledTick);
         this.scheduledTickActionsInOrder.add(scheduledTick);
      }

   }

   public int getTicks() {
      return this.scheduledTickActions.size();
   }
}
