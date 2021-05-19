package net.minecraft.entity.ai.goal;

import java.util.List;
import java.util.function.Predicate;
import net.minecraft.entity.passive.SchoolingFishEntity;

public class FollowGroupLeaderGoal extends Goal {
   private final SchoolingFishEntity fish;
   private int moveDelay;
   private int checkSurroundingDelay;

   public FollowGroupLeaderGoal(SchoolingFishEntity fish) {
      this.fish = fish;
      this.checkSurroundingDelay = this.getSurroundingSearchDelay(fish);
   }

   protected int getSurroundingSearchDelay(SchoolingFishEntity fish) {
      return 200 + fish.getRandom().nextInt(200) % 20;
   }

   public boolean canStart() {
      if (this.fish.hasOtherFishInGroup()) {
         return false;
      } else if (this.fish.hasLeader()) {
         return true;
      } else if (this.checkSurroundingDelay > 0) {
         --this.checkSurroundingDelay;
         return false;
      } else {
         this.checkSurroundingDelay = this.getSurroundingSearchDelay(this.fish);
         Predicate<SchoolingFishEntity> predicate = (schoolingFishEntityx) -> {
            return schoolingFishEntityx.canHaveMoreFishInGroup() || !schoolingFishEntityx.hasLeader();
         };
         List<SchoolingFishEntity> list = this.fish.world.getEntitiesByClass(this.fish.getClass(), this.fish.getBoundingBox().expand(8.0D, 8.0D, 8.0D), predicate);
         SchoolingFishEntity schoolingFishEntity = (SchoolingFishEntity)list.stream().filter(SchoolingFishEntity::canHaveMoreFishInGroup).findAny().orElse(this.fish);
         schoolingFishEntity.pullInOtherFish(list.stream().filter((schoolingFishEntityx) -> {
            return !schoolingFishEntityx.hasLeader();
         }));
         return this.fish.hasLeader();
      }
   }

   public boolean shouldContinue() {
      return this.fish.hasLeader() && this.fish.isCloseEnoughToLeader();
   }

   public void start() {
      this.moveDelay = 0;
   }

   public void stop() {
      this.fish.leaveGroup();
   }

   public void tick() {
      if (--this.moveDelay <= 0) {
         this.moveDelay = 10;
         this.fish.moveTowardLeader();
      }
   }
}
