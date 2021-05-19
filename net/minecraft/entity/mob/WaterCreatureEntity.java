package net.minecraft.entity.mob;

import net.minecraft.entity.EntityGroup;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.pathing.PathNodeType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;

public abstract class WaterCreatureEntity extends PathAwareEntity {
   protected WaterCreatureEntity(EntityType<? extends WaterCreatureEntity> entityType, World world) {
      super(entityType, world);
      this.setPathfindingPenalty(PathNodeType.WATER, 0.0F);
   }

   public boolean canBreatheInWater() {
      return true;
   }

   public EntityGroup getGroup() {
      return EntityGroup.AQUATIC;
   }

   public boolean canSpawn(WorldView world) {
      return world.intersectsEntities(this);
   }

   public int getMinAmbientSoundDelay() {
      return 120;
   }

   protected int getCurrentExperience(PlayerEntity player) {
      return 1 + this.world.random.nextInt(3);
   }

   protected void tickWaterBreathingAir(int air) {
      if (this.isAlive() && !this.isInsideWaterOrBubbleColumn()) {
         this.setAir(air - 1);
         if (this.getAir() == -20) {
            this.setAir(0);
            this.damage(DamageSource.DROWN, 2.0F);
         }
      } else {
         this.setAir(300);
      }

   }

   public void baseTick() {
      int i = this.getAir();
      super.baseTick();
      this.tickWaterBreathingAir(i);
   }

   public boolean canFly() {
      return false;
   }

   public boolean canBeLeashedBy(PlayerEntity player) {
      return false;
   }
}
