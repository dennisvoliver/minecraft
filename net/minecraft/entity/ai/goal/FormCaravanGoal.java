package net.minecraft.entity.ai.goal;

import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.decoration.LeashKnotEntity;
import net.minecraft.entity.passive.LlamaEntity;
import net.minecraft.util.math.Vec3d;

public class FormCaravanGoal extends Goal {
   public final LlamaEntity llama;
   private double speed;
   private int counter;

   public FormCaravanGoal(LlamaEntity llama, double speed) {
      this.llama = llama;
      this.speed = speed;
      this.setControls(EnumSet.of(Goal.Control.MOVE));
   }

   public boolean canStart() {
      if (!this.llama.isLeashed() && !this.llama.isFollowing()) {
         List<Entity> list = this.llama.world.getOtherEntities(this.llama, this.llama.getBoundingBox().expand(9.0D, 4.0D, 9.0D), (entity) -> {
            EntityType<?> entityType = entity.getType();
            return entityType == EntityType.LLAMA || entityType == EntityType.TRADER_LLAMA;
         });
         LlamaEntity llamaEntity = null;
         double d = Double.MAX_VALUE;
         Iterator var5 = list.iterator();

         Entity entity2;
         LlamaEntity llamaEntity3;
         double f;
         while(var5.hasNext()) {
            entity2 = (Entity)var5.next();
            llamaEntity3 = (LlamaEntity)entity2;
            if (llamaEntity3.isFollowing() && !llamaEntity3.hasFollower()) {
               f = this.llama.squaredDistanceTo(llamaEntity3);
               if (!(f > d)) {
                  d = f;
                  llamaEntity = llamaEntity3;
               }
            }
         }

         if (llamaEntity == null) {
            var5 = list.iterator();

            while(var5.hasNext()) {
               entity2 = (Entity)var5.next();
               llamaEntity3 = (LlamaEntity)entity2;
               if (llamaEntity3.isLeashed() && !llamaEntity3.hasFollower()) {
                  f = this.llama.squaredDistanceTo(llamaEntity3);
                  if (!(f > d)) {
                     d = f;
                     llamaEntity = llamaEntity3;
                  }
               }
            }
         }

         if (llamaEntity == null) {
            return false;
         } else if (d < 4.0D) {
            return false;
         } else if (!llamaEntity.isLeashed() && !this.canFollow(llamaEntity, 1)) {
            return false;
         } else {
            this.llama.follow(llamaEntity);
            return true;
         }
      } else {
         return false;
      }
   }

   public boolean shouldContinue() {
      if (this.llama.isFollowing() && this.llama.getFollowing().isAlive() && this.canFollow(this.llama, 0)) {
         double d = this.llama.squaredDistanceTo(this.llama.getFollowing());
         if (d > 676.0D) {
            if (this.speed <= 3.0D) {
               this.speed *= 1.2D;
               this.counter = 40;
               return true;
            }

            if (this.counter == 0) {
               return false;
            }
         }

         if (this.counter > 0) {
            --this.counter;
         }

         return true;
      } else {
         return false;
      }
   }

   public void stop() {
      this.llama.stopFollowing();
      this.speed = 2.1D;
   }

   public void tick() {
      if (this.llama.isFollowing()) {
         if (!(this.llama.getHoldingEntity() instanceof LeashKnotEntity)) {
            LlamaEntity llamaEntity = this.llama.getFollowing();
            double d = (double)this.llama.distanceTo(llamaEntity);
            float f = 2.0F;
            Vec3d vec3d = (new Vec3d(llamaEntity.getX() - this.llama.getX(), llamaEntity.getY() - this.llama.getY(), llamaEntity.getZ() - this.llama.getZ())).normalize().multiply(Math.max(d - 2.0D, 0.0D));
            this.llama.getNavigation().startMovingTo(this.llama.getX() + vec3d.x, this.llama.getY() + vec3d.y, this.llama.getZ() + vec3d.z, this.speed);
         }
      }
   }

   private boolean canFollow(LlamaEntity llama, int length) {
      if (length > 8) {
         return false;
      } else if (llama.isFollowing()) {
         if (llama.getFollowing().isLeashed()) {
            return true;
         } else {
            LlamaEntity var10001 = llama.getFollowing();
            ++length;
            return this.canFollow(var10001, length);
         }
      } else {
         return false;
      }
   }
}
