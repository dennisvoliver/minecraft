package net.minecraft.entity.ai.pathing;

import java.util.Iterator;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class MobNavigation extends EntityNavigation {
   private boolean avoidSunlight;

   public MobNavigation(MobEntity mobEntity, World world) {
      super(mobEntity, world);
   }

   protected PathNodeNavigator createPathNodeNavigator(int range) {
      this.nodeMaker = new LandPathNodeMaker();
      this.nodeMaker.setCanEnterOpenDoors(true);
      return new PathNodeNavigator(this.nodeMaker, range);
   }

   protected boolean isAtValidPosition() {
      return this.entity.isOnGround() || this.isInLiquid() || this.entity.hasVehicle();
   }

   protected Vec3d getPos() {
      return new Vec3d(this.entity.getX(), (double)this.getPathfindingY(), this.entity.getZ());
   }

   public Path findPathTo(BlockPos target, int distance) {
      BlockPos blockPos2;
      if (this.world.getBlockState(target).isAir()) {
         for(blockPos2 = target.down(); blockPos2.getY() > 0 && this.world.getBlockState(blockPos2).isAir(); blockPos2 = blockPos2.down()) {
         }

         if (blockPos2.getY() > 0) {
            return super.findPathTo(blockPos2.up(), distance);
         }

         while(blockPos2.getY() < this.world.getHeight() && this.world.getBlockState(blockPos2).isAir()) {
            blockPos2 = blockPos2.up();
         }

         target = blockPos2;
      }

      if (!this.world.getBlockState(target).getMaterial().isSolid()) {
         return super.findPathTo(target, distance);
      } else {
         for(blockPos2 = target.up(); blockPos2.getY() < this.world.getHeight() && this.world.getBlockState(blockPos2).getMaterial().isSolid(); blockPos2 = blockPos2.up()) {
         }

         return super.findPathTo(blockPos2, distance);
      }
   }

   public Path findPathTo(Entity entity, int distance) {
      return this.findPathTo(entity.getBlockPos(), distance);
   }

   /**
    * The y-position to act as if the entity is at for pathfinding purposes
    */
   private int getPathfindingY() {
      if (this.entity.isTouchingWater() && this.canSwim()) {
         int i = MathHelper.floor(this.entity.getY());
         Block block = this.world.getBlockState(new BlockPos(this.entity.getX(), (double)i, this.entity.getZ())).getBlock();
         int j = 0;

         do {
            if (block != Blocks.WATER) {
               return i;
            }

            ++i;
            block = this.world.getBlockState(new BlockPos(this.entity.getX(), (double)i, this.entity.getZ())).getBlock();
            ++j;
         } while(j <= 16);

         return MathHelper.floor(this.entity.getY());
      } else {
         return MathHelper.floor(this.entity.getY() + 0.5D);
      }
   }

   protected void adjustPath() {
      super.adjustPath();
      if (this.avoidSunlight) {
         if (this.world.isSkyVisible(new BlockPos(this.entity.getX(), this.entity.getY() + 0.5D, this.entity.getZ()))) {
            return;
         }

         for(int i = 0; i < this.currentPath.getLength(); ++i) {
            PathNode pathNode = this.currentPath.getNode(i);
            if (this.world.isSkyVisible(new BlockPos(pathNode.x, pathNode.y, pathNode.z))) {
               this.currentPath.setLength(i);
               return;
            }
         }
      }

   }

   protected boolean canPathDirectlyThrough(Vec3d origin, Vec3d target, int sizeX, int sizeY, int sizeZ) {
      int i = MathHelper.floor(origin.x);
      int j = MathHelper.floor(origin.z);
      double d = target.x - origin.x;
      double e = target.z - origin.z;
      double f = d * d + e * e;
      if (f < 1.0E-8D) {
         return false;
      } else {
         double g = 1.0D / Math.sqrt(f);
         d *= g;
         e *= g;
         sizeX += 2;
         sizeZ += 2;
         if (!this.allVisibleAreSafe(i, MathHelper.floor(origin.y), j, sizeX, sizeY, sizeZ, origin, d, e)) {
            return false;
         } else {
            sizeX -= 2;
            sizeZ -= 2;
            double h = 1.0D / Math.abs(d);
            double k = 1.0D / Math.abs(e);
            double l = (double)i - origin.x;
            double m = (double)j - origin.z;
            if (d >= 0.0D) {
               ++l;
            }

            if (e >= 0.0D) {
               ++m;
            }

            l /= d;
            m /= e;
            int n = d < 0.0D ? -1 : 1;
            int o = e < 0.0D ? -1 : 1;
            int p = MathHelper.floor(target.x);
            int q = MathHelper.floor(target.z);
            int r = p - i;
            int s = q - j;

            do {
               if (r * n <= 0 && s * o <= 0) {
                  return true;
               }

               if (l < m) {
                  l += h;
                  i += n;
                  r = p - i;
               } else {
                  m += k;
                  j += o;
                  s = q - j;
               }
            } while(this.allVisibleAreSafe(i, MathHelper.floor(origin.y), j, sizeX, sizeY, sizeZ, origin, d, e));

            return false;
         }
      }
   }

   private boolean allVisibleAreSafe(int centerX, int centerY, int centerZ, int xSize, int ySize, int zSize, Vec3d entityPos, double lookVecX, double lookVecZ) {
      int i = centerX - xSize / 2;
      int j = centerZ - zSize / 2;
      if (!this.allVisibleArePassable(i, centerY, j, xSize, ySize, zSize, entityPos, lookVecX, lookVecZ)) {
         return false;
      } else {
         for(int k = i; k < i + xSize; ++k) {
            for(int l = j; l < j + zSize; ++l) {
               double d = (double)k + 0.5D - entityPos.x;
               double e = (double)l + 0.5D - entityPos.z;
               if (!(d * lookVecX + e * lookVecZ < 0.0D)) {
                  PathNodeType pathNodeType = this.nodeMaker.getNodeType(this.world, k, centerY - 1, l, this.entity, xSize, ySize, zSize, true, true);
                  if (!this.canWalkOnPath(pathNodeType)) {
                     return false;
                  }

                  pathNodeType = this.nodeMaker.getNodeType(this.world, k, centerY, l, this.entity, xSize, ySize, zSize, true, true);
                  float f = this.entity.getPathfindingPenalty(pathNodeType);
                  if (f < 0.0F || f >= 8.0F) {
                     return false;
                  }

                  if (pathNodeType == PathNodeType.DAMAGE_FIRE || pathNodeType == PathNodeType.DANGER_FIRE || pathNodeType == PathNodeType.DAMAGE_OTHER) {
                     return false;
                  }
               }
            }
         }

         return true;
      }
   }

   protected boolean canWalkOnPath(PathNodeType pathType) {
      if (pathType == PathNodeType.WATER) {
         return false;
      } else if (pathType == PathNodeType.LAVA) {
         return false;
      } else {
         return pathType != PathNodeType.OPEN;
      }
   }

   /**
    * Checks whether all blocks in the box which are visible (in front of) the mob can be pathed through
    */
   private boolean allVisibleArePassable(int x, int y, int z, int xSize, int ySize, int zSize, Vec3d entityPos, double lookVecX, double lookVecZ) {
      Iterator var12 = BlockPos.iterate(new BlockPos(x, y, z), new BlockPos(x + xSize - 1, y + ySize - 1, z + zSize - 1)).iterator();

      BlockPos blockPos;
      double d;
      double e;
      do {
         if (!var12.hasNext()) {
            return true;
         }

         blockPos = (BlockPos)var12.next();
         d = (double)blockPos.getX() + 0.5D - entityPos.x;
         e = (double)blockPos.getZ() + 0.5D - entityPos.z;
      } while(d * lookVecX + e * lookVecZ < 0.0D || this.world.getBlockState(blockPos).canPathfindThrough(this.world, blockPos, NavigationType.LAND));

      return false;
   }

   public void setCanPathThroughDoors(boolean canPathThroughDoors) {
      this.nodeMaker.setCanOpenDoors(canPathThroughDoors);
   }

   public boolean canEnterOpenDoors() {
      return this.nodeMaker.canEnterOpenDoors();
   }

   public void setAvoidSunlight(boolean avoidSunlight) {
      this.avoidSunlight = avoidSunlight;
   }
}
