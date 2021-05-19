package net.minecraft.entity.ai.brain.sensor;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.server.world.ServerWorld;

public class VillagerBabiesSensor extends Sensor<LivingEntity> {
   public Set<MemoryModuleType<?>> getOutputMemoryModules() {
      return ImmutableSet.of(MemoryModuleType.VISIBLE_VILLAGER_BABIES);
   }

   protected void sense(ServerWorld world, LivingEntity entity) {
      entity.getBrain().remember(MemoryModuleType.VISIBLE_VILLAGER_BABIES, (Object)this.getVisibleVillagerBabies(entity));
   }

   private List<LivingEntity> getVisibleVillagerBabies(LivingEntity entities) {
      return (List)this.getVisibleMobs(entities).stream().filter(this::isVillagerBaby).collect(Collectors.toList());
   }

   private boolean isVillagerBaby(LivingEntity entity) {
      return entity.getType() == EntityType.VILLAGER && entity.isBaby();
   }

   private List<LivingEntity> getVisibleMobs(LivingEntity entity) {
      return (List)entity.getBrain().getOptionalMemory(MemoryModuleType.VISIBLE_MOBS).orElse(Lists.newArrayList());
   }
}
