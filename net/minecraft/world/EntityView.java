package net.minecraft.world;

import com.google.common.collect.Lists;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Stream;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.TargetPredicate;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.util.function.BooleanBiFunction;
import net.minecraft.util.math.Box;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import org.jetbrains.annotations.Nullable;

public interface EntityView {
   /**
    * Computes a list of entities within some box, excluding the given entity, that satisfy the given predicate.
    * 
    * @return a list of entities within a box, excluding the given entity, all satisfying the given predicate
    * 
    * @param except the entity the box logically surrounds. This entity is ignored if it is inside the box.
    * @param box the box in which to search for entities
    * @param predicate a predicate which entities must satisfy in order to be included in the returned list.
    */
   List<Entity> getOtherEntities(@Nullable Entity except, Box box, @Nullable Predicate<? super Entity> predicate);

   /**
    * Computes a list of entities within some box whose runtime Java class is the same as or is
    * a subclass of the given class.
    * 
    * @return a list of entities within the box whose runtime class is a subclass of the given class
    * 
    * @param entityClass the class the list of entities must extend
    * @param box the box in which to search for entities
    * @param predicate a predicate which entities must satisfy in order to be included in the returned list
    */
   <T extends Entity> List<T> getEntitiesByClass(Class<? extends T> entityClass, Box box, @Nullable Predicate<? super T> predicate);

   default <T extends Entity> List<T> getEntitiesIncludingUngeneratedChunks(Class<? extends T> entityClass, Box box, @Nullable Predicate<? super T> predicate) {
      return this.getEntitiesByClass(entityClass, box, predicate);
   }

   List<? extends PlayerEntity> getPlayers();

   /**
    * Computes a list of entities within some box, excluding the given entity, that are not spectators.
    * 
    * @return a list of entities within a box, excluding the given entity
    * @see #getSurroundingEntities(Entity, Box, Predicate)
    * @see Entity#isSpectator()
    * 
    * @param except the entity the box logically surrounds. This entity is ignored if it is inside the box.
    * @param box the box in which to search for entities
    */
   default List<Entity> getOtherEntities(@Nullable Entity except, Box box) {
      return this.getOtherEntities(except, box, EntityPredicates.EXCEPT_SPECTATOR);
   }

   default boolean intersectsEntities(@Nullable Entity entity, VoxelShape shape) {
      if (shape.isEmpty()) {
         return true;
      } else {
         Iterator var3 = this.getOtherEntities(entity, shape.getBoundingBox()).iterator();

         Entity entity2;
         do {
            do {
               do {
                  do {
                     if (!var3.hasNext()) {
                        return true;
                     }

                     entity2 = (Entity)var3.next();
                  } while(entity2.removed);
               } while(!entity2.inanimate);
            } while(entity != null && entity2.isConnectedThroughVehicle(entity));
         } while(!VoxelShapes.matchesAnywhere(shape, VoxelShapes.cuboid(entity2.getBoundingBox()), BooleanBiFunction.AND));

         return false;
      }
   }

   default <T extends Entity> List<T> getNonSpectatingEntities(Class<? extends T> entityClass, Box box) {
      return this.getEntitiesByClass(entityClass, box, EntityPredicates.EXCEPT_SPECTATOR);
   }

   default <T extends Entity> List<T> getEntitiesIncludingUngeneratedChunks(Class<? extends T> entityClass, Box box) {
      return this.getEntitiesIncludingUngeneratedChunks(entityClass, box, EntityPredicates.EXCEPT_SPECTATOR);
   }

   default Stream<VoxelShape> getEntityCollisions(@Nullable Entity entity, Box box, Predicate<Entity> predicate) {
      if (box.getAverageSideLength() < 1.0E-7D) {
         return Stream.empty();
      } else {
         Box box2 = box.expand(1.0E-7D);
         return this.getOtherEntities(entity, box2, predicate.and((entityx) -> {
            boolean var10000;
            label25: {
               if (entityx.getBoundingBox().intersects(box2)) {
                  if (entity == null) {
                     if (entityx.isCollidable()) {
                        break label25;
                     }
                  } else if (entity.collidesWith(entityx)) {
                     break label25;
                  }
               }

               var10000 = false;
               return var10000;
            }

            var10000 = true;
            return var10000;
         })).stream().map(Entity::getBoundingBox).map(VoxelShapes::cuboid);
      }
   }

   @Nullable
   default PlayerEntity getClosestPlayer(double x, double y, double z, double maxDistance, @Nullable Predicate<Entity> targetPredicate) {
      double d = -1.0D;
      PlayerEntity playerEntity = null;
      Iterator var13 = this.getPlayers().iterator();

      while(true) {
         PlayerEntity playerEntity2;
         double e;
         do {
            do {
               do {
                  if (!var13.hasNext()) {
                     return playerEntity;
                  }

                  playerEntity2 = (PlayerEntity)var13.next();
               } while(targetPredicate != null && !targetPredicate.test(playerEntity2));

               e = playerEntity2.squaredDistanceTo(x, y, z);
            } while(!(maxDistance < 0.0D) && !(e < maxDistance * maxDistance));
         } while(d != -1.0D && !(e < d));

         d = e;
         playerEntity = playerEntity2;
      }
   }

   @Nullable
   default PlayerEntity getClosestPlayer(Entity entity, double maxDistance) {
      return this.getClosestPlayer(entity.getX(), entity.getY(), entity.getZ(), maxDistance, false);
   }

   @Nullable
   default PlayerEntity getClosestPlayer(double x, double y, double z, double maxDistance, boolean ignoreCreative) {
      Predicate<Entity> predicate = ignoreCreative ? EntityPredicates.EXCEPT_CREATIVE_OR_SPECTATOR : EntityPredicates.EXCEPT_SPECTATOR;
      return this.getClosestPlayer(x, y, z, maxDistance, predicate);
   }

   default boolean isPlayerInRange(double x, double y, double z, double range) {
      Iterator var9 = this.getPlayers().iterator();

      double d;
      do {
         PlayerEntity playerEntity;
         do {
            do {
               if (!var9.hasNext()) {
                  return false;
               }

               playerEntity = (PlayerEntity)var9.next();
            } while(!EntityPredicates.EXCEPT_SPECTATOR.test(playerEntity));
         } while(!EntityPredicates.VALID_LIVING_ENTITY.test(playerEntity));

         d = playerEntity.squaredDistanceTo(x, y, z);
      } while(!(range < 0.0D) && !(d < range * range));

      return true;
   }

   @Nullable
   default PlayerEntity getClosestPlayer(TargetPredicate targetPredicate, LivingEntity entity) {
      return (PlayerEntity)this.getClosestEntity(this.getPlayers(), targetPredicate, entity, entity.getX(), entity.getY(), entity.getZ());
   }

   @Nullable
   default PlayerEntity getClosestPlayer(TargetPredicate targetPredicate, LivingEntity entity, double x, double y, double z) {
      return (PlayerEntity)this.getClosestEntity(this.getPlayers(), targetPredicate, entity, x, y, z);
   }

   @Nullable
   default PlayerEntity getClosestPlayer(TargetPredicate targetPredicate, double x, double y, double z) {
      return (PlayerEntity)this.getClosestEntity(this.getPlayers(), targetPredicate, (LivingEntity)null, x, y, z);
   }

   @Nullable
   default <T extends LivingEntity> T getClosestEntity(Class<? extends T> entityClass, TargetPredicate targetPredicate, @Nullable LivingEntity entity, double x, double y, double z, Box box) {
      return this.getClosestEntity(this.getEntitiesByClass(entityClass, box, (Predicate)null), targetPredicate, entity, x, y, z);
   }

   @Nullable
   default <T extends LivingEntity> T getClosestEntityIncludingUngeneratedChunks(Class<? extends T> entityClass, TargetPredicate targetPredicate, @Nullable LivingEntity entity, double x, double y, double z, Box box) {
      return this.getClosestEntity(this.getEntitiesIncludingUngeneratedChunks(entityClass, box, (Predicate)null), targetPredicate, entity, x, y, z);
   }

   @Nullable
   default <T extends LivingEntity> T getClosestEntity(List<? extends T> entityList, TargetPredicate targetPredicate, @Nullable LivingEntity entity, double x, double y, double z) {
      double d = -1.0D;
      T livingEntity = null;
      Iterator var13 = entityList.iterator();

      while(true) {
         LivingEntity livingEntity2;
         double e;
         do {
            do {
               if (!var13.hasNext()) {
                  return livingEntity;
               }

               livingEntity2 = (LivingEntity)var13.next();
            } while(!targetPredicate.test(entity, livingEntity2));

            e = livingEntity2.squaredDistanceTo(x, y, z);
         } while(d != -1.0D && !(e < d));

         d = e;
         livingEntity = livingEntity2;
      }
   }

   default List<PlayerEntity> getPlayers(TargetPredicate targetPredicate, LivingEntity entity, Box box) {
      List<PlayerEntity> list = Lists.newArrayList();
      Iterator var5 = this.getPlayers().iterator();

      while(var5.hasNext()) {
         PlayerEntity playerEntity = (PlayerEntity)var5.next();
         if (box.contains(playerEntity.getX(), playerEntity.getY(), playerEntity.getZ()) && targetPredicate.test(entity, playerEntity)) {
            list.add(playerEntity);
         }
      }

      return list;
   }

   default <T extends LivingEntity> List<T> getTargets(Class<? extends T> entityClass, TargetPredicate targetPredicate, LivingEntity targetingEntity, Box box) {
      List<T> list = this.getEntitiesByClass(entityClass, box, (Predicate)null);
      List<T> list2 = Lists.newArrayList();
      Iterator var7 = list.iterator();

      while(var7.hasNext()) {
         T livingEntity = (LivingEntity)var7.next();
         if (targetPredicate.test(targetingEntity, livingEntity)) {
            list2.add(livingEntity);
         }
      }

      return list2;
   }

   @Nullable
   default PlayerEntity getPlayerByUuid(UUID uuid) {
      for(int i = 0; i < this.getPlayers().size(); ++i) {
         PlayerEntity playerEntity = (PlayerEntity)this.getPlayers().get(i);
         if (uuid.equals(playerEntity.getUuid())) {
            return playerEntity;
         }
      }

      return null;
   }
}
