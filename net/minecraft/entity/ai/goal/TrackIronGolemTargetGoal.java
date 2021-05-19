package net.minecraft.entity.ai.goal;

import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.TargetPredicate;
import net.minecraft.entity.passive.IronGolemEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Box;

public class TrackIronGolemTargetGoal extends TrackTargetGoal {
   private final IronGolemEntity golem;
   private LivingEntity target;
   private final TargetPredicate targetPredicate = (new TargetPredicate()).setBaseMaxDistance(64.0D);

   public TrackIronGolemTargetGoal(IronGolemEntity golem) {
      super(golem, false, true);
      this.golem = golem;
      this.setControls(EnumSet.of(Goal.Control.TARGET));
   }

   public boolean canStart() {
      Box box = this.golem.getBoundingBox().expand(10.0D, 8.0D, 10.0D);
      List<LivingEntity> list = this.golem.world.getTargets(VillagerEntity.class, this.targetPredicate, this.golem, box);
      List<PlayerEntity> list2 = this.golem.world.getPlayers(this.targetPredicate, this.golem, box);
      Iterator var4 = list.iterator();

      while(var4.hasNext()) {
         LivingEntity livingEntity = (LivingEntity)var4.next();
         VillagerEntity villagerEntity = (VillagerEntity)livingEntity;
         Iterator var7 = list2.iterator();

         while(var7.hasNext()) {
            PlayerEntity playerEntity = (PlayerEntity)var7.next();
            int i = villagerEntity.getReputation(playerEntity);
            if (i <= -100) {
               this.target = playerEntity;
            }
         }
      }

      if (this.target == null) {
         return false;
      } else if (!(this.target instanceof PlayerEntity) || !this.target.isSpectator() && !((PlayerEntity)this.target).isCreative()) {
         return true;
      } else {
         return false;
      }
   }

   public void start() {
      this.golem.setTarget(this.target);
      super.start();
   }
}
