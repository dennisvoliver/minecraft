package net.minecraft.entity.ai.brain.sensor;

import com.google.common.collect.ImmutableSet;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.server.world.ServerWorld;

public class NearestPlayersSensor extends Sensor<LivingEntity> {
   public Set<MemoryModuleType<?>> getOutputMemoryModules() {
      return ImmutableSet.of(MemoryModuleType.NEAREST_PLAYERS, MemoryModuleType.NEAREST_VISIBLE_PLAYER, MemoryModuleType.NEAREST_VISIBLE_TARGETABLE_PLAYER);
   }

   protected void sense(ServerWorld world, LivingEntity entity) {
      Stream var10000 = world.getPlayers().stream().filter(EntityPredicates.EXCEPT_SPECTATOR).filter((serverPlayerEntity) -> {
         return entity.isInRange(serverPlayerEntity, 16.0D);
      });
      entity.getClass();
      List<PlayerEntity> list = (List)var10000.sorted(Comparator.comparingDouble(entity::squaredDistanceTo)).collect(Collectors.toList());
      Brain<?> brain = entity.getBrain();
      brain.remember(MemoryModuleType.NEAREST_PLAYERS, (Object)list);
      List<PlayerEntity> list2 = (List)list.stream().filter((playerEntity) -> {
         return method_30954(entity, playerEntity);
      }).collect(Collectors.toList());
      brain.remember(MemoryModuleType.NEAREST_VISIBLE_PLAYER, (Object)(list2.isEmpty() ? null : (PlayerEntity)list2.get(0)));
      Optional<PlayerEntity> optional = list2.stream().filter(EntityPredicates.EXCEPT_CREATIVE_SPECTATOR_OR_PEACEFUL).findFirst();
      brain.remember(MemoryModuleType.NEAREST_VISIBLE_TARGETABLE_PLAYER, optional);
   }
}
