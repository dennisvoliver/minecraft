package net.minecraft.entity.ai.goal;

import java.util.EnumSet;
import net.minecraft.entity.ai.TargetPredicate;
import net.minecraft.entity.passive.IronGolemEntity;
import net.minecraft.entity.passive.VillagerEntity;

public class IronGolemLookGoal extends Goal {
   private static final TargetPredicate CLOSE_VILLAGER_PREDICATE = (new TargetPredicate()).setBaseMaxDistance(6.0D).includeTeammates().includeInvulnerable();
   private final IronGolemEntity golem;
   private VillagerEntity targetVillager;
   private int lookCountdown;

   public IronGolemLookGoal(IronGolemEntity golem) {
      this.golem = golem;
      this.setControls(EnumSet.of(Goal.Control.MOVE, Goal.Control.LOOK));
   }

   public boolean canStart() {
      if (!this.golem.world.isDay()) {
         return false;
      } else if (this.golem.getRandom().nextInt(8000) != 0) {
         return false;
      } else {
         this.targetVillager = (VillagerEntity)this.golem.world.getClosestEntity(VillagerEntity.class, CLOSE_VILLAGER_PREDICATE, this.golem, this.golem.getX(), this.golem.getY(), this.golem.getZ(), this.golem.getBoundingBox().expand(6.0D, 2.0D, 6.0D));
         return this.targetVillager != null;
      }
   }

   public boolean shouldContinue() {
      return this.lookCountdown > 0;
   }

   public void start() {
      this.lookCountdown = 400;
      this.golem.setLookingAtVillager(true);
   }

   public void stop() {
      this.golem.setLookingAtVillager(false);
      this.targetVillager = null;
   }

   public void tick() {
      this.golem.getLookControl().lookAt(this.targetVillager, 30.0F, 30.0F);
      --this.lookCountdown;
   }
}
