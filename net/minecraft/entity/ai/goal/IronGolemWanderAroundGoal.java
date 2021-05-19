package net.minecraft.entity.ai.goal;

import java.util.List;
import java.util.stream.Collectors;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.TargetFinder;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.poi.PointOfInterest;
import net.minecraft.world.poi.PointOfInterestStorage;
import org.jetbrains.annotations.Nullable;

public class IronGolemWanderAroundGoal extends WanderAroundGoal {
   public IronGolemWanderAroundGoal(PathAwareEntity pathAwareEntity, double d) {
      super(pathAwareEntity, d, 240, false);
   }

   @Nullable
   protected Vec3d getWanderTarget() {
      float f = this.mob.world.random.nextFloat();
      if (this.mob.world.random.nextFloat() < 0.3F) {
         return this.method_27925();
      } else {
         Vec3d vec3d2;
         if (f < 0.7F) {
            vec3d2 = this.method_27926();
            if (vec3d2 == null) {
               vec3d2 = this.method_27927();
            }
         } else {
            vec3d2 = this.method_27927();
            if (vec3d2 == null) {
               vec3d2 = this.method_27926();
            }
         }

         return vec3d2 == null ? this.method_27925() : vec3d2;
      }
   }

   @Nullable
   private Vec3d method_27925() {
      return TargetFinder.findGroundTarget(this.mob, 10, 7);
   }

   @Nullable
   private Vec3d method_27926() {
      ServerWorld serverWorld = (ServerWorld)this.mob.world;
      List<VillagerEntity> list = serverWorld.getEntitiesByType(EntityType.VILLAGER, this.mob.getBoundingBox().expand(32.0D), this::method_27922);
      if (list.isEmpty()) {
         return null;
      } else {
         VillagerEntity villagerEntity = (VillagerEntity)list.get(this.mob.world.random.nextInt(list.size()));
         Vec3d vec3d = villagerEntity.getPos();
         return TargetFinder.method_27929(this.mob, 10, 7, vec3d);
      }
   }

   @Nullable
   private Vec3d method_27927() {
      ChunkSectionPos chunkSectionPos = this.method_27928();
      if (chunkSectionPos == null) {
         return null;
      } else {
         BlockPos blockPos = this.method_27923(chunkSectionPos);
         return blockPos == null ? null : TargetFinder.method_27929(this.mob, 10, 7, Vec3d.ofBottomCenter(blockPos));
      }
   }

   @Nullable
   private ChunkSectionPos method_27928() {
      ServerWorld serverWorld = (ServerWorld)this.mob.world;
      List<ChunkSectionPos> list = (List)ChunkSectionPos.stream((ChunkSectionPos)ChunkSectionPos.from((Entity)this.mob), 2).filter((chunkSectionPos) -> {
         return serverWorld.getOccupiedPointOfInterestDistance(chunkSectionPos) == 0;
      }).collect(Collectors.toList());
      return list.isEmpty() ? null : (ChunkSectionPos)list.get(serverWorld.random.nextInt(list.size()));
   }

   @Nullable
   private BlockPos method_27923(ChunkSectionPos chunkSectionPos) {
      ServerWorld serverWorld = (ServerWorld)this.mob.world;
      PointOfInterestStorage pointOfInterestStorage = serverWorld.getPointOfInterestStorage();
      List<BlockPos> list = (List)pointOfInterestStorage.getInCircle((pointOfInterestType) -> {
         return true;
      }, chunkSectionPos.getCenterPos(), 8, PointOfInterestStorage.OccupationStatus.IS_OCCUPIED).map(PointOfInterest::getPos).collect(Collectors.toList());
      return list.isEmpty() ? null : (BlockPos)list.get(serverWorld.random.nextInt(list.size()));
   }

   private boolean method_27922(VillagerEntity villagerEntity) {
      return villagerEntity.canSummonGolem(this.mob.world.getTime());
   }
}
