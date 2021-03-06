package net.minecraft.entity.ai.pathing;

import net.minecraft.block.AbstractRailBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.tag.BlockTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.chunk.ChunkCache;
import org.jetbrains.annotations.Nullable;

public class AmphibiousPathNodeMaker extends LandPathNodeMaker {
   private float oldWalkablePenalty;
   private float oldWaterBorderPenalty;

   public void init(ChunkCache cachedWorld, MobEntity entity) {
      super.init(cachedWorld, entity);
      entity.setPathfindingPenalty(PathNodeType.WATER, 0.0F);
      this.oldWalkablePenalty = entity.getPathfindingPenalty(PathNodeType.WALKABLE);
      entity.setPathfindingPenalty(PathNodeType.WALKABLE, 6.0F);
      this.oldWaterBorderPenalty = entity.getPathfindingPenalty(PathNodeType.WATER_BORDER);
      entity.setPathfindingPenalty(PathNodeType.WATER_BORDER, 4.0F);
   }

   public void clear() {
      this.entity.setPathfindingPenalty(PathNodeType.WALKABLE, this.oldWalkablePenalty);
      this.entity.setPathfindingPenalty(PathNodeType.WATER_BORDER, this.oldWaterBorderPenalty);
      super.clear();
   }

   public PathNode getStart() {
      return this.getNode(MathHelper.floor(this.entity.getBoundingBox().minX), MathHelper.floor(this.entity.getBoundingBox().minY + 0.5D), MathHelper.floor(this.entity.getBoundingBox().minZ));
   }

   public TargetPathNode getNode(double x, double y, double z) {
      return new TargetPathNode(this.getNode(MathHelper.floor(x), MathHelper.floor(y + 0.5D), MathHelper.floor(z)));
   }

   public int getSuccessors(PathNode[] successors, PathNode node) {
      int i = 0;
      int j = true;
      BlockPos blockPos = new BlockPos(node.x, node.y, node.z);
      double d = this.getFeetY(blockPos);
      PathNode pathNode = this.getPathNode(node.x, node.y, node.z + 1, 1, d);
      PathNode pathNode2 = this.getPathNode(node.x - 1, node.y, node.z, 1, d);
      PathNode pathNode3 = this.getPathNode(node.x + 1, node.y, node.z, 1, d);
      PathNode pathNode4 = this.getPathNode(node.x, node.y, node.z - 1, 1, d);
      PathNode pathNode5 = this.getPathNode(node.x, node.y + 1, node.z, 0, d);
      PathNode pathNode6 = this.getPathNode(node.x, node.y - 1, node.z, 1, d);
      if (pathNode != null && !pathNode.visited) {
         successors[i++] = pathNode;
      }

      if (pathNode2 != null && !pathNode2.visited) {
         successors[i++] = pathNode2;
      }

      if (pathNode3 != null && !pathNode3.visited) {
         successors[i++] = pathNode3;
      }

      if (pathNode4 != null && !pathNode4.visited) {
         successors[i++] = pathNode4;
      }

      if (pathNode5 != null && !pathNode5.visited) {
         successors[i++] = pathNode5;
      }

      if (pathNode6 != null && !pathNode6.visited) {
         successors[i++] = pathNode6;
      }

      boolean bl = pathNode4 == null || pathNode4.type == PathNodeType.OPEN || pathNode4.penalty != 0.0F;
      boolean bl2 = pathNode == null || pathNode.type == PathNodeType.OPEN || pathNode.penalty != 0.0F;
      boolean bl3 = pathNode3 == null || pathNode3.type == PathNodeType.OPEN || pathNode3.penalty != 0.0F;
      boolean bl4 = pathNode2 == null || pathNode2.type == PathNodeType.OPEN || pathNode2.penalty != 0.0F;
      PathNode pathNode10;
      if (bl && bl4) {
         pathNode10 = this.getPathNode(node.x - 1, node.y, node.z - 1, 1, d);
         if (pathNode10 != null && !pathNode10.visited) {
            successors[i++] = pathNode10;
         }
      }

      if (bl && bl3) {
         pathNode10 = this.getPathNode(node.x + 1, node.y, node.z - 1, 1, d);
         if (pathNode10 != null && !pathNode10.visited) {
            successors[i++] = pathNode10;
         }
      }

      if (bl2 && bl4) {
         pathNode10 = this.getPathNode(node.x - 1, node.y, node.z + 1, 1, d);
         if (pathNode10 != null && !pathNode10.visited) {
            successors[i++] = pathNode10;
         }
      }

      if (bl2 && bl3) {
         pathNode10 = this.getPathNode(node.x + 1, node.y, node.z + 1, 1, d);
         if (pathNode10 != null && !pathNode10.visited) {
            successors[i++] = pathNode10;
         }
      }

      return i;
   }

   private double getFeetY(BlockPos pos) {
      if (!this.entity.isTouchingWater()) {
         BlockPos blockPos = pos.down();
         VoxelShape voxelShape = this.cachedWorld.getBlockState(blockPos).getCollisionShape(this.cachedWorld, blockPos);
         return (double)blockPos.getY() + (voxelShape.isEmpty() ? 0.0D : voxelShape.getMax(Direction.Axis.Y));
      } else {
         return (double)pos.getY() + 0.5D;
      }
   }

   @Nullable
   private PathNode getPathNode(int x, int y, int z, int maxYStep, double prevFeetY) {
      PathNode pathNode = null;
      BlockPos blockPos = new BlockPos(x, y, z);
      double d = this.getFeetY(blockPos);
      if (d - prevFeetY > 1.125D) {
         return null;
      } else {
         PathNodeType pathNodeType = this.getNodeType(this.cachedWorld, x, y, z, this.entity, this.entityBlockXSize, this.entityBlockYSize, this.entityBlockZSize, false, false);
         float f = this.entity.getPathfindingPenalty(pathNodeType);
         double e = (double)this.entity.getWidth() / 2.0D;
         if (f >= 0.0F) {
            pathNode = this.getNode(x, y, z);
            pathNode.type = pathNodeType;
            pathNode.penalty = Math.max(pathNode.penalty, f);
         }

         if (pathNodeType != PathNodeType.WATER && pathNodeType != PathNodeType.WALKABLE) {
            if (pathNode == null && maxYStep > 0 && pathNodeType != PathNodeType.FENCE && pathNodeType != PathNodeType.UNPASSABLE_RAIL && pathNodeType != PathNodeType.TRAPDOOR) {
               pathNode = this.getPathNode(x, y + 1, z, maxYStep - 1, prevFeetY);
            }

            if (pathNodeType == PathNodeType.OPEN) {
               Box box = new Box((double)x - e + 0.5D, (double)y + 0.001D, (double)z - e + 0.5D, (double)x + e + 0.5D, (double)((float)y + this.entity.getHeight()), (double)z + e + 0.5D);
               if (!this.entity.world.isSpaceEmpty(this.entity, box)) {
                  return null;
               }

               PathNodeType pathNodeType2 = this.getNodeType(this.cachedWorld, x, y - 1, z, this.entity, this.entityBlockXSize, this.entityBlockYSize, this.entityBlockZSize, false, false);
               if (pathNodeType2 == PathNodeType.BLOCKED) {
                  pathNode = this.getNode(x, y, z);
                  pathNode.type = PathNodeType.WALKABLE;
                  pathNode.penalty = Math.max(pathNode.penalty, f);
                  return pathNode;
               }

               if (pathNodeType2 == PathNodeType.WATER) {
                  pathNode = this.getNode(x, y, z);
                  pathNode.type = PathNodeType.WATER;
                  pathNode.penalty = Math.max(pathNode.penalty, f);
                  return pathNode;
               }

               int var17 = 0;

               while(y > 0 && pathNodeType == PathNodeType.OPEN) {
                  --y;
                  if (var17++ >= this.entity.getSafeFallDistance()) {
                     return null;
                  }

                  pathNodeType = this.getNodeType(this.cachedWorld, x, y, z, this.entity, this.entityBlockXSize, this.entityBlockYSize, this.entityBlockZSize, false, false);
                  f = this.entity.getPathfindingPenalty(pathNodeType);
                  if (pathNodeType != PathNodeType.OPEN && f >= 0.0F) {
                     pathNode = this.getNode(x, y, z);
                     pathNode.type = pathNodeType;
                     pathNode.penalty = Math.max(pathNode.penalty, f);
                     break;
                  }

                  if (f < 0.0F) {
                     return null;
                  }
               }
            }

            return pathNode;
         } else {
            if (y < this.entity.world.getSeaLevel() - 10 && pathNode != null) {
               ++pathNode.penalty;
            }

            return pathNode;
         }
      }
   }

   protected PathNodeType adjustNodeType(BlockView world, boolean canOpenDoors, boolean canEnterOpenDoors, BlockPos pos, PathNodeType type) {
      if (type == PathNodeType.RAIL && !(world.getBlockState(pos).getBlock() instanceof AbstractRailBlock) && !(world.getBlockState(pos.down()).getBlock() instanceof AbstractRailBlock)) {
         type = PathNodeType.UNPASSABLE_RAIL;
      }

      if (type == PathNodeType.DOOR_OPEN || type == PathNodeType.DOOR_WOOD_CLOSED || type == PathNodeType.DOOR_IRON_CLOSED) {
         type = PathNodeType.BLOCKED;
      }

      if (type == PathNodeType.LEAVES) {
         type = PathNodeType.BLOCKED;
      }

      return type;
   }

   public PathNodeType getDefaultNodeType(BlockView world, int x, int y, int z) {
      BlockPos.Mutable mutable = new BlockPos.Mutable();
      PathNodeType pathNodeType = getCommonNodeType(world, mutable.set(x, y, z));
      if (pathNodeType == PathNodeType.WATER) {
         Direction[] var12 = Direction.values();
         int var13 = var12.length;

         for(int var9 = 0; var9 < var13; ++var9) {
            Direction direction = var12[var9];
            PathNodeType pathNodeType2 = getCommonNodeType(world, mutable.set(x, y, z).move(direction));
            if (pathNodeType2 == PathNodeType.BLOCKED) {
               return PathNodeType.WATER_BORDER;
            }
         }

         return PathNodeType.WATER;
      } else {
         if (pathNodeType == PathNodeType.OPEN && y >= 1) {
            BlockState blockState = world.getBlockState(new BlockPos(x, y - 1, z));
            PathNodeType pathNodeType3 = getCommonNodeType(world, mutable.set(x, y - 1, z));
            if (pathNodeType3 != PathNodeType.WALKABLE && pathNodeType3 != PathNodeType.OPEN && pathNodeType3 != PathNodeType.LAVA) {
               pathNodeType = PathNodeType.WALKABLE;
            } else {
               pathNodeType = PathNodeType.OPEN;
            }

            if (pathNodeType3 == PathNodeType.DAMAGE_FIRE || blockState.isOf(Blocks.MAGMA_BLOCK) || blockState.isIn(BlockTags.CAMPFIRES)) {
               pathNodeType = PathNodeType.DAMAGE_FIRE;
            }

            if (pathNodeType3 == PathNodeType.DAMAGE_CACTUS) {
               pathNodeType = PathNodeType.DAMAGE_CACTUS;
            }

            if (pathNodeType3 == PathNodeType.DAMAGE_OTHER) {
               pathNodeType = PathNodeType.DAMAGE_OTHER;
            }
         }

         if (pathNodeType == PathNodeType.WALKABLE) {
            pathNodeType = getNodeTypeFromNeighbors(world, mutable.set(x, y, z), pathNodeType);
         }

         return pathNodeType;
      }
   }
}
