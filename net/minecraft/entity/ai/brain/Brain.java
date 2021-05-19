package net.minecraft.entity.ai.brain;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.collect.UnmodifiableIterator;
import com.google.common.collect.ImmutableList.Builder;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.Map.Entry;
import java.util.function.Supplier;
import java.util.stream.Stream;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.sensor.Sensor;
import net.minecraft.entity.ai.brain.sensor.SensorType;
import net.minecraft.entity.ai.brain.task.Task;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Util;
import net.minecraft.util.registry.Registry;
import org.apache.commons.lang3.mutable.MutableObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

public class Brain<E extends LivingEntity> {
   private static final Logger LOGGER = LogManager.getLogger();
   private final Supplier<Codec<Brain<E>>> codecSupplier;
   private final Map<MemoryModuleType<?>, Optional<? extends Memory<?>>> memories = Maps.newHashMap();
   private final Map<SensorType<? extends Sensor<? super E>>, Sensor<? super E>> sensors = Maps.newLinkedHashMap();
   private final Map<Integer, Map<Activity, Set<Task<? super E>>>> tasks = Maps.newTreeMap();
   private Schedule schedule;
   private final Map<Activity, Set<Pair<MemoryModuleType<?>, MemoryModuleState>>> requiredActivityMemories;
   /**
    * The map from activities to the memories to forget after the activity is
    * completed.
    */
   private final Map<Activity, Set<MemoryModuleType<?>>> forgettingActivityMemories;
   private Set<Activity> coreActivities;
   private final Set<Activity> possibleActivities;
   private Activity defaultActivity;
   private long activityStartTime;

   public static <E extends LivingEntity> Brain.Profile<E> createProfile(Collection<? extends MemoryModuleType<?>> memoryModules, Collection<? extends SensorType<? extends Sensor<? super E>>> sensors) {
      return new Brain.Profile(memoryModules, sensors);
   }

   public static <E extends LivingEntity> Codec<Brain<E>> createBrainCodec(final Collection<? extends MemoryModuleType<?>> memoryModules, final Collection<? extends SensorType<? extends Sensor<? super E>>> sensors) {
      final MutableObject<Codec<Brain<E>>> mutableObject = new MutableObject();
      mutableObject.setValue((new MapCodec<Brain<E>>() {
         public <T> Stream<T> keys(DynamicOps<T> dynamicOps) {
            return memoryModules.stream().flatMap((memoryModuleType) -> {
               return Util.stream(memoryModuleType.getCodec().map((codec) -> {
                  return Registry.MEMORY_MODULE_TYPE.getId(memoryModuleType);
               }));
            }).map((identifier) -> {
               return dynamicOps.createString(identifier.toString());
            });
         }

         public <T> DataResult<Brain<E>> decode(DynamicOps<T> dynamicOps, MapLike<T> mapLike) {
            MutableObject<DataResult<Builder<Brain.MemoryEntry<?>>>> mutableObjectx = new MutableObject(DataResult.success(ImmutableList.builder()));
            mapLike.entries().forEach((pair) -> {
               DataResult<MemoryModuleType<?>> dataResult = Registry.MEMORY_MODULE_TYPE.parse(dynamicOps, pair.getFirst());
               DataResult<? extends Brain.MemoryEntry<?>> dataResult2 = dataResult.flatMap((memoryModuleType) -> {
                  return this.method_28320(memoryModuleType, dynamicOps, pair.getSecond());
               });
               mutableObjectx.setValue(((DataResult)mutableObjectx.getValue()).apply2(Builder::add, dataResult2));
            });
            DataResult var10000 = (DataResult)mutableObjectx.getValue();
            Logger var10001 = Brain.LOGGER;
            var10001.getClass();
            ImmutableList<Brain.MemoryEntry<?>> immutableList = (ImmutableList)var10000.resultOrPartial(var10001::error).map(Builder::build).orElseGet(ImmutableList::of);
            Collection var10002 = memoryModules;
            Collection var10003 = sensors;
            MutableObject var10005 = mutableObject;
            var10005.getClass();
            return DataResult.success(new Brain(var10002, var10003, immutableList, var10005::getValue));
         }

         private <T, U> DataResult<Brain.MemoryEntry<U>> method_28320(MemoryModuleType<U> memoryModuleType, DynamicOps<T> dynamicOps, T object) {
            return ((DataResult)memoryModuleType.getCodec().map(DataResult::success).orElseGet(() -> {
               return DataResult.error("No codec for memory: " + memoryModuleType);
            })).flatMap((codec) -> {
               return codec.parse(dynamicOps, object);
            }).map((memory) -> {
               return new Brain.MemoryEntry(memoryModuleType, Optional.of(memory));
            });
         }

         public <T> RecordBuilder<T> encode(Brain<E> brain, DynamicOps<T> dynamicOps, RecordBuilder<T> recordBuilder) {
            brain.streamMemories().forEach((memoryEntry) -> {
               memoryEntry.serialize(dynamicOps, recordBuilder);
            });
            return recordBuilder;
         }
      }).fieldOf("memories").codec());
      return (Codec)mutableObject.getValue();
   }

   public Brain(Collection<? extends MemoryModuleType<?>> memories, Collection<? extends SensorType<? extends Sensor<? super E>>> sensors, ImmutableList<Brain.MemoryEntry<?>> memoryEntries, Supplier<Codec<Brain<E>>> codecSupplier) {
      this.schedule = Schedule.EMPTY;
      this.requiredActivityMemories = Maps.newHashMap();
      this.forgettingActivityMemories = Maps.newHashMap();
      this.coreActivities = Sets.newHashSet();
      this.possibleActivities = Sets.newHashSet();
      this.defaultActivity = Activity.IDLE;
      this.activityStartTime = -9999L;
      this.codecSupplier = codecSupplier;
      Iterator var5 = memories.iterator();

      while(var5.hasNext()) {
         MemoryModuleType<?> memoryModuleType = (MemoryModuleType)var5.next();
         this.memories.put(memoryModuleType, Optional.empty());
      }

      var5 = sensors.iterator();

      while(var5.hasNext()) {
         SensorType<? extends Sensor<? super E>> sensorType = (SensorType)var5.next();
         this.sensors.put(sensorType, sensorType.create());
      }

      var5 = this.sensors.values().iterator();

      while(var5.hasNext()) {
         Sensor<? super E> sensor = (Sensor)var5.next();
         Iterator var7 = sensor.getOutputMemoryModules().iterator();

         while(var7.hasNext()) {
            MemoryModuleType<?> memoryModuleType2 = (MemoryModuleType)var7.next();
            this.memories.put(memoryModuleType2, Optional.empty());
         }
      }

      UnmodifiableIterator var9 = memoryEntries.iterator();

      while(var9.hasNext()) {
         Brain.MemoryEntry<?> memoryEntry = (Brain.MemoryEntry)var9.next();
         memoryEntry.apply(this);
      }

   }

   public <T> DataResult<T> encode(DynamicOps<T> ops) {
      return ((Codec)this.codecSupplier.get()).encodeStart(ops, this);
   }

   private Stream<Brain.MemoryEntry<?>> streamMemories() {
      return this.memories.entrySet().stream().map((entry) -> {
         return Brain.MemoryEntry.of((MemoryModuleType)entry.getKey(), (Optional)entry.getValue());
      });
   }

   public boolean hasMemoryModule(MemoryModuleType<?> type) {
      return this.isMemoryInState(type, MemoryModuleState.VALUE_PRESENT);
   }

   public <U> void forget(MemoryModuleType<U> type) {
      this.remember(type, Optional.empty());
   }

   public <U> void remember(MemoryModuleType<U> type, @Nullable U value) {
      this.remember(type, Optional.ofNullable(value));
   }

   public <U> void remember(MemoryModuleType<U> type, U value, long startTime) {
      this.setMemory(type, Optional.of(Memory.timed(value, startTime)));
   }

   public <U> void remember(MemoryModuleType<U> type, Optional<? extends U> value) {
      this.setMemory(type, value.map(Memory::method_28355));
   }

   private <U> void setMemory(MemoryModuleType<U> type, Optional<? extends Memory<?>> memory) {
      if (this.memories.containsKey(type)) {
         if (memory.isPresent() && this.isEmptyCollection(((Memory)memory.get()).getValue())) {
            this.forget(type);
         } else {
            this.memories.put(type, memory);
         }
      }

   }

   public <U> Optional<U> getOptionalMemory(MemoryModuleType<U> type) {
      return ((Optional)this.memories.get(type)).map(Memory::getValue);
   }

   public <U> boolean method_29519(MemoryModuleType<U> memoryModuleType, U object) {
      return !this.hasMemoryModule(memoryModuleType) ? false : this.getOptionalMemory(memoryModuleType).filter((object2) -> {
         return object2.equals(object);
      }).isPresent();
   }

   public boolean isMemoryInState(MemoryModuleType<?> type, MemoryModuleState state) {
      Optional<? extends Memory<?>> optional = (Optional)this.memories.get(type);
      if (optional == null) {
         return false;
      } else {
         return state == MemoryModuleState.REGISTERED || state == MemoryModuleState.VALUE_PRESENT && optional.isPresent() || state == MemoryModuleState.VALUE_ABSENT && !optional.isPresent();
      }
   }

   public Schedule getSchedule() {
      return this.schedule;
   }

   public void setSchedule(Schedule schedule) {
      this.schedule = schedule;
   }

   public void setCoreActivities(Set<Activity> coreActivities) {
      this.coreActivities = coreActivities;
   }

   @Deprecated
   public List<Task<? super E>> getRunningTasks() {
      List<Task<? super E>> list = new ObjectArrayList();
      Iterator var2 = this.tasks.values().iterator();

      while(var2.hasNext()) {
         Map<Activity, Set<Task<? super E>>> map = (Map)var2.next();
         Iterator var4 = map.values().iterator();

         while(var4.hasNext()) {
            Set<Task<? super E>> set = (Set)var4.next();
            Iterator var6 = set.iterator();

            while(var6.hasNext()) {
               Task<? super E> task = (Task)var6.next();
               if (task.getStatus() == Task.Status.RUNNING) {
                  list.add(task);
               }
            }
         }
      }

      return list;
   }

   public void resetPossibleActivities() {
      this.resetPossibleActivities(this.defaultActivity);
   }

   public Optional<Activity> getFirstPossibleNonCoreActivity() {
      Iterator var1 = this.possibleActivities.iterator();

      Activity activity;
      do {
         if (!var1.hasNext()) {
            return Optional.empty();
         }

         activity = (Activity)var1.next();
      } while(this.coreActivities.contains(activity));

      return Optional.of(activity);
   }

   public void doExclusively(Activity activity) {
      if (this.canDoActivity(activity)) {
         this.resetPossibleActivities(activity);
      } else {
         this.resetPossibleActivities();
      }

   }

   private void resetPossibleActivities(Activity except) {
      if (!this.hasActivity(except)) {
         this.forgetIrrelevantMemories(except);
         this.possibleActivities.clear();
         this.possibleActivities.addAll(this.coreActivities);
         this.possibleActivities.add(except);
      }
   }

   private void forgetIrrelevantMemories(Activity except) {
      Iterator var2 = this.possibleActivities.iterator();

      while(true) {
         Set set;
         do {
            Activity activity;
            do {
               if (!var2.hasNext()) {
                  return;
               }

               activity = (Activity)var2.next();
            } while(activity == except);

            set = (Set)this.forgettingActivityMemories.get(activity);
         } while(set == null);

         Iterator var5 = set.iterator();

         while(var5.hasNext()) {
            MemoryModuleType<?> memoryModuleType = (MemoryModuleType)var5.next();
            this.forget(memoryModuleType);
         }
      }
   }

   public void refreshActivities(long timeOfDay, long time) {
      if (time - this.activityStartTime > 20L) {
         this.activityStartTime = time;
         Activity activity = this.getSchedule().getActivityForTime((int)(timeOfDay % 24000L));
         if (!this.possibleActivities.contains(activity)) {
            this.doExclusively(activity);
         }
      }

   }

   public void resetPossibleActivities(List<Activity> list) {
      Iterator var2 = list.iterator();

      while(var2.hasNext()) {
         Activity activity = (Activity)var2.next();
         if (this.canDoActivity(activity)) {
            this.resetPossibleActivities(activity);
            break;
         }
      }

   }

   public void setDefaultActivity(Activity activity) {
      this.defaultActivity = activity;
   }

   public void setTaskList(Activity activity, int begin, ImmutableList<? extends Task<? super E>> list) {
      this.setTaskList(activity, this.indexTaskList(begin, list));
   }

   public void setTaskList(Activity activity, int begin, ImmutableList<? extends Task<? super E>> tasks, MemoryModuleType<?> memoryType) {
      Set<Pair<MemoryModuleType<?>, MemoryModuleState>> set = ImmutableSet.of(Pair.of(memoryType, MemoryModuleState.VALUE_PRESENT));
      Set<MemoryModuleType<?>> set2 = ImmutableSet.of(memoryType);
      this.setTaskList(activity, this.indexTaskList(begin, tasks), set, set2);
   }

   public void setTaskList(Activity activity, ImmutableList<? extends Pair<Integer, ? extends Task<? super E>>> indexedTasks) {
      this.setTaskList(activity, indexedTasks, ImmutableSet.of(), Sets.newHashSet());
   }

   public void setTaskList(Activity activity, ImmutableList<? extends Pair<Integer, ? extends Task<? super E>>> indexedTasks, Set<Pair<MemoryModuleType<?>, MemoryModuleState>> requiredMemories) {
      this.setTaskList(activity, indexedTasks, requiredMemories, Sets.newHashSet());
   }

   private void setTaskList(Activity activity, ImmutableList<? extends Pair<Integer, ? extends Task<? super E>>> indexedTasks, Set<Pair<MemoryModuleType<?>, MemoryModuleState>> requiredMemories, Set<MemoryModuleType<?>> forgettingMemories) {
      this.requiredActivityMemories.put(activity, requiredMemories);
      if (!forgettingMemories.isEmpty()) {
         this.forgettingActivityMemories.put(activity, forgettingMemories);
      }

      UnmodifiableIterator var5 = indexedTasks.iterator();

      while(var5.hasNext()) {
         Pair<Integer, ? extends Task<? super E>> pair = (Pair)var5.next();
         ((Set)((Map)this.tasks.computeIfAbsent(pair.getFirst(), (integer) -> {
            return Maps.newHashMap();
         })).computeIfAbsent(activity, (activityx) -> {
            return Sets.newLinkedHashSet();
         })).add(pair.getSecond());
      }

   }

   public boolean hasActivity(Activity activity) {
      return this.possibleActivities.contains(activity);
   }

   public Brain<E> copy() {
      Brain<E> brain = new Brain(this.memories.keySet(), this.sensors.keySet(), ImmutableList.of(), this.codecSupplier);
      Iterator var2 = this.memories.entrySet().iterator();

      while(var2.hasNext()) {
         Entry<MemoryModuleType<?>, Optional<? extends Memory<?>>> entry = (Entry)var2.next();
         MemoryModuleType<?> memoryModuleType = (MemoryModuleType)entry.getKey();
         if (((Optional)entry.getValue()).isPresent()) {
            brain.memories.put(memoryModuleType, entry.getValue());
         }
      }

      return brain;
   }

   public void tick(ServerWorld world, E entity) {
      this.tickMemories();
      this.tickSensors(world, entity);
      this.startTasks(world, entity);
      this.updateTasks(world, entity);
   }

   private void tickSensors(ServerWorld world, E entity) {
      Iterator var3 = this.sensors.values().iterator();

      while(var3.hasNext()) {
         Sensor<? super E> sensor = (Sensor)var3.next();
         sensor.tick(world, entity);
      }

   }

   private void tickMemories() {
      Iterator var1 = this.memories.entrySet().iterator();

      while(var1.hasNext()) {
         Entry<MemoryModuleType<?>, Optional<? extends Memory<?>>> entry = (Entry)var1.next();
         if (((Optional)entry.getValue()).isPresent()) {
            Memory<?> memory = (Memory)((Optional)entry.getValue()).get();
            memory.tick();
            if (memory.isExpired()) {
               this.forget((MemoryModuleType)entry.getKey());
            }
         }
      }

   }

   public void stopAllTasks(ServerWorld world, E entity) {
      long l = entity.world.getTime();
      Iterator var5 = this.getRunningTasks().iterator();

      while(var5.hasNext()) {
         Task<? super E> task = (Task)var5.next();
         task.stop(world, entity, l);
      }

   }

   private void startTasks(ServerWorld world, E entity) {
      long l = world.getTime();
      Iterator var5 = this.tasks.values().iterator();

      label34:
      while(var5.hasNext()) {
         Map<Activity, Set<Task<? super E>>> map = (Map)var5.next();
         Iterator var7 = map.entrySet().iterator();

         while(true) {
            Entry entry;
            Activity activity;
            do {
               if (!var7.hasNext()) {
                  continue label34;
               }

               entry = (Entry)var7.next();
               activity = (Activity)entry.getKey();
            } while(!this.possibleActivities.contains(activity));

            Set<Task<? super E>> set = (Set)entry.getValue();
            Iterator var11 = set.iterator();

            while(var11.hasNext()) {
               Task<? super E> task = (Task)var11.next();
               if (task.getStatus() == Task.Status.STOPPED) {
                  task.tryStarting(world, entity, l);
               }
            }
         }
      }

   }

   private void updateTasks(ServerWorld world, E entity) {
      long l = world.getTime();
      Iterator var5 = this.getRunningTasks().iterator();

      while(var5.hasNext()) {
         Task<? super E> task = (Task)var5.next();
         task.tick(world, entity, l);
      }

   }

   private boolean canDoActivity(Activity activity) {
      if (!this.requiredActivityMemories.containsKey(activity)) {
         return false;
      } else {
         Iterator var2 = ((Set)this.requiredActivityMemories.get(activity)).iterator();

         MemoryModuleType memoryModuleType;
         MemoryModuleState memoryModuleState;
         do {
            if (!var2.hasNext()) {
               return true;
            }

            Pair<MemoryModuleType<?>, MemoryModuleState> pair = (Pair)var2.next();
            memoryModuleType = (MemoryModuleType)pair.getFirst();
            memoryModuleState = (MemoryModuleState)pair.getSecond();
         } while(this.isMemoryInState(memoryModuleType, memoryModuleState));

         return false;
      }
   }

   private boolean isEmptyCollection(Object value) {
      return value instanceof Collection && ((Collection)value).isEmpty();
   }

   /**
    * @param begin The beginning of the index of tasks, exclusive
    */
   ImmutableList<? extends Pair<Integer, ? extends Task<? super E>>> indexTaskList(int begin, ImmutableList<? extends Task<? super E>> tasks) {
      int i = begin;
      Builder<Pair<Integer, ? extends Task<? super E>>> builder = ImmutableList.builder();
      UnmodifiableIterator var5 = tasks.iterator();

      while(var5.hasNext()) {
         Task<? super E> task = (Task)var5.next();
         builder.add((Object)Pair.of(i++, task));
      }

      return builder.build();
   }

   static final class MemoryEntry<U> {
      private final MemoryModuleType<U> type;
      private final Optional<? extends Memory<U>> data;

      private static <U> Brain.MemoryEntry<U> of(MemoryModuleType<U> type, Optional<? extends Memory<?>> data) {
         return new Brain.MemoryEntry(type, data);
      }

      private MemoryEntry(MemoryModuleType<U> type, Optional<? extends Memory<U>> data) {
         this.type = type;
         this.data = data;
      }

      private void apply(Brain<?> brain) {
         brain.setMemory(this.type, this.data);
      }

      public <T> void serialize(DynamicOps<T> ops, RecordBuilder<T> builder) {
         this.type.getCodec().ifPresent((codec) -> {
            this.data.ifPresent((memory) -> {
               builder.add(Registry.MEMORY_MODULE_TYPE.encodeStart(ops, this.type), codec.encodeStart(ops, memory));
            });
         });
      }
   }

   /**
    * A simple profile of a brain. Indicates what types of memory modules and
    * sensors a brain can have.
    */
   public static final class Profile<E extends LivingEntity> {
      private final Collection<? extends MemoryModuleType<?>> memoryModules;
      private final Collection<? extends SensorType<? extends Sensor<? super E>>> sensors;
      private final Codec<Brain<E>> codec;

      private Profile(Collection<? extends MemoryModuleType<?>> memoryModules, Collection<? extends SensorType<? extends Sensor<? super E>>> sensors) {
         this.memoryModules = memoryModules;
         this.sensors = sensors;
         this.codec = Brain.createBrainCodec(memoryModules, sensors);
      }

      public Brain<E> deserialize(Dynamic<?> data) {
         DataResult var10000 = this.codec.parse(data);
         Logger var10001 = Brain.LOGGER;
         var10001.getClass();
         return (Brain)var10000.resultOrPartial(var10001::error).orElseGet(() -> {
            return new Brain(this.memoryModules, this.sensors, ImmutableList.of(), () -> {
               return this.codec;
            });
         });
      }
   }
}
