package net.minecraft.entity.ai.goal;

import net.minecraft.block.BlockState;
import net.minecraft.block.DoorBlock;
import net.minecraft.entity.ai.NavigationConditions;
import net.minecraft.entity.ai.pathing.MobNavigation;
import net.minecraft.entity.ai.pathing.Path;
import net.minecraft.entity.ai.pathing.PathNode;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.util.math.BlockPos;

public abstract class DoorInteractGoal extends Goal {
   protected MobEntity mob;
   protected BlockPos doorPos;
   protected boolean doorValid;
   private boolean shouldStop;
   private float xOffset;
   private float zOffset;

   public DoorInteractGoal(MobEntity mob) {
      this.doorPos = BlockPos.ORIGIN;
      this.mob = mob;
      if (!NavigationConditions.hasMobNavigation(mob)) {
         throw new IllegalArgumentException("Unsupported mob type for DoorInteractGoal");
      }
   }

   protected boolean isDoorOpen() {
      if (!this.doorValid) {
         return false;
      } else {
         BlockState blockState = this.mob.world.getBlockState(this.doorPos);
         if (!(blockState.getBlock() instanceof DoorBlock)) {
            this.doorValid = false;
            return false;
         } else {
            return (Boolean)blockState.get(DoorBlock.OPEN);
         }
      }
   }

   protected void setDoorOpen(boolean open) {
      if (this.doorValid) {
         BlockState blockState = this.mob.world.getBlockState(this.doorPos);
         if (blockState.getBlock() instanceof DoorBlock) {
            ((DoorBlock)blockState.getBlock()).setOpen(this.mob.world, blockState, this.doorPos, open);
         }
      }

   }

   public boolean canStart() {
      if (!NavigationConditions.hasMobNavigation(this.mob)) {
         return false;
      } else if (!this.mob.horizontalCollision) {
         return false;
      } else {
         MobNavigation mobNavigation = (MobNavigation)this.mob.getNavigation();
         Path path = mobNavigation.getCurrentPath();
         if (path != null && !path.isFinished() && mobNavigation.canEnterOpenDoors()) {
            for(int i = 0; i < Math.min(path.getCurrentNodeIndex() + 2, path.getLength()); ++i) {
               PathNode pathNode = path.getNode(i);
               this.doorPos = new BlockPos(pathNode.x, pathNode.y + 1, pathNode.z);
               if (!(this.mob.squaredDistanceTo((double)this.doorPos.getX(), this.mob.getY(), (double)this.doorPos.getZ()) > 2.25D)) {
                  this.doorValid = DoorBlock.isWoodenDoor(this.mob.world, this.doorPos);
                  if (this.doorValid) {
                     return true;
                  }
               }
            }

            this.doorPos = this.mob.getBlockPos().up();
            this.doorValid = DoorBlock.isWoodenDoor(this.mob.world, this.doorPos);
            return this.doorValid;
         } else {
            return false;
         }
      }
   }

   public boolean shouldContinue() {
      return !this.shouldStop;
   }

   public void start() {
      this.shouldStop = false;
      this.xOffset = (float)((double)this.doorPos.getX() + 0.5D - this.mob.getX());
      this.zOffset = (float)((double)this.doorPos.getZ() + 0.5D - this.mob.getZ());
   }

   public void tick() {
      float f = (float)((double)this.doorPos.getX() + 0.5D - this.mob.getX());
      float g = (float)((double)this.doorPos.getZ() + 0.5D - this.mob.getZ());
      float h = this.xOffset * f + this.zOffset * g;
      if (h < 0.0F) {
         this.shouldStop = true;
      }

   }
}
