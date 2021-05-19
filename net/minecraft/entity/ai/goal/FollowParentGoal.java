package net.minecraft.entity.ai.goal;

import java.util.Iterator;
import java.util.List;
import net.minecraft.entity.passive.AnimalEntity;

public class FollowParentGoal extends Goal {
   private final AnimalEntity animal;
   private AnimalEntity parent;
   private final double speed;
   private int delay;

   public FollowParentGoal(AnimalEntity animal, double speed) {
      this.animal = animal;
      this.speed = speed;
   }

   public boolean canStart() {
      if (this.animal.getBreedingAge() >= 0) {
         return false;
      } else {
         List<AnimalEntity> list = this.animal.world.getNonSpectatingEntities(this.animal.getClass(), this.animal.getBoundingBox().expand(8.0D, 4.0D, 8.0D));
         AnimalEntity animalEntity = null;
         double d = Double.MAX_VALUE;
         Iterator var5 = list.iterator();

         while(var5.hasNext()) {
            AnimalEntity animalEntity2 = (AnimalEntity)var5.next();
            if (animalEntity2.getBreedingAge() >= 0) {
               double e = this.animal.squaredDistanceTo(animalEntity2);
               if (!(e > d)) {
                  d = e;
                  animalEntity = animalEntity2;
               }
            }
         }

         if (animalEntity == null) {
            return false;
         } else if (d < 9.0D) {
            return false;
         } else {
            this.parent = animalEntity;
            return true;
         }
      }
   }

   public boolean shouldContinue() {
      if (this.animal.getBreedingAge() >= 0) {
         return false;
      } else if (!this.parent.isAlive()) {
         return false;
      } else {
         double d = this.animal.squaredDistanceTo(this.parent);
         return !(d < 9.0D) && !(d > 256.0D);
      }
   }

   public void start() {
      this.delay = 0;
   }

   public void stop() {
      this.parent = null;
   }

   public void tick() {
      if (--this.delay <= 0) {
         this.delay = 10;
         this.animal.getNavigation().startMovingTo(this.parent, this.speed);
      }
   }
}
