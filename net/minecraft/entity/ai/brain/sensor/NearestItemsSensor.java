package net.minecraft.entity.ai.brain.sensor;

import com.google.common.collect.ImmutableSet;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.server.world.ServerWorld;

public class NearestItemsSensor extends Sensor<MobEntity> {
   public Set<MemoryModuleType<?>> getOutputMemoryModules() {
      return ImmutableSet.of(MemoryModuleType.NEAREST_VISIBLE_WANTED_ITEM);
   }

   protected void sense(ServerWorld serverWorld, MobEntity mobEntity) {
      Brain<?> brain = mobEntity.getBrain();
      List<ItemEntity> list = serverWorld.getEntitiesByClass(ItemEntity.class, mobEntity.getBoundingBox().expand(8.0D, 4.0D, 8.0D), (itemEntity) -> {
         return true;
      });
      mobEntity.getClass();
      list.sort(Comparator.comparingDouble(mobEntity::squaredDistanceTo));
      Stream var10000 = list.stream().filter((itemEntity) -> {
         return mobEntity.canGather(itemEntity.getStack());
      }).filter((itemEntity) -> {
         return itemEntity.isInRange(mobEntity, 9.0D);
      });
      mobEntity.getClass();
      Optional<ItemEntity> optional = var10000.filter(mobEntity::canSee).findFirst();
      brain.remember(MemoryModuleType.NEAREST_VISIBLE_WANTED_ITEM, optional);
   }
}
