package net.minecraft.entity.ai.goal;

import com.google.common.collect.Sets;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import net.minecraft.entity.ai.TargetFinder;
import net.minecraft.entity.raid.RaiderEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.village.raid.Raid;
import net.minecraft.village.raid.RaidManager;

public class MoveToRaidCenterGoal<T extends RaiderEntity> extends Goal {
   private final T actor;

   public MoveToRaidCenterGoal(T actor) {
      this.actor = actor;
      this.setControls(EnumSet.of(Goal.Control.MOVE));
   }

   public boolean canStart() {
      return this.actor.getTarget() == null && !this.actor.hasPassengers() && this.actor.hasActiveRaid() && !this.actor.getRaid().isFinished() && !((ServerWorld)this.actor.world).isNearOccupiedPointOfInterest(this.actor.getBlockPos());
   }

   public boolean shouldContinue() {
      return this.actor.hasActiveRaid() && !this.actor.getRaid().isFinished() && this.actor.world instanceof ServerWorld && !((ServerWorld)this.actor.world).isNearOccupiedPointOfInterest(this.actor.getBlockPos());
   }

   public void tick() {
      if (this.actor.hasActiveRaid()) {
         Raid raid = this.actor.getRaid();
         if (this.actor.age % 20 == 0) {
            this.includeFreeRaiders(raid);
         }

         if (!this.actor.isNavigating()) {
            Vec3d vec3d = TargetFinder.findTargetTowards(this.actor, 15, 4, Vec3d.ofBottomCenter(raid.getCenter()));
            if (vec3d != null) {
               this.actor.getNavigation().startMovingTo(vec3d.x, vec3d.y, vec3d.z, 1.0D);
            }
         }
      }

   }

   private void includeFreeRaiders(Raid raid) {
      if (raid.isActive()) {
         Set<RaiderEntity> set = Sets.newHashSet();
         List<RaiderEntity> list = this.actor.world.getEntitiesByClass(RaiderEntity.class, this.actor.getBoundingBox().expand(16.0D), (raiderEntityx) -> {
            return !raiderEntityx.hasActiveRaid() && RaidManager.isValidRaiderFor(raiderEntityx, raid);
         });
         set.addAll(list);
         Iterator var4 = set.iterator();

         while(var4.hasNext()) {
            RaiderEntity raiderEntity = (RaiderEntity)var4.next();
            raid.addRaider(raid.getGroupsSpawned(), raiderEntity, (BlockPos)null, true);
         }
      }

   }
}
