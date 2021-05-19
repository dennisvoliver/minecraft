package net.minecraft.world.chunk.light;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.LightType;
import net.minecraft.world.chunk.ChunkNibbleArray;
import net.minecraft.world.chunk.ChunkProvider;
import org.apache.commons.lang3.mutable.MutableInt;

public final class ChunkSkyLightProvider extends ChunkLightProvider<SkyLightStorage.Data, SkyLightStorage> {
   private static final Direction[] DIRECTIONS = Direction.values();
   private static final Direction[] HORIZONTAL_DIRECTIONS;

   public ChunkSkyLightProvider(ChunkProvider chunkProvider) {
      super(chunkProvider, LightType.SKY, new SkyLightStorage(chunkProvider));
   }

   protected int getPropagatedLevel(long sourceId, long targetId, int level) {
      if (targetId == Long.MAX_VALUE) {
         return 15;
      } else {
         if (sourceId == Long.MAX_VALUE) {
            if (!((SkyLightStorage)this.lightStorage).isTopmostBlock(targetId)) {
               return 15;
            }

            level = 0;
         }

         if (level >= 15) {
            return level;
         } else {
            MutableInt mutableInt = new MutableInt();
            BlockState blockState = this.getStateForLighting(targetId, mutableInt);
            if (mutableInt.getValue() >= 15) {
               return 15;
            } else {
               int i = BlockPos.unpackLongX(sourceId);
               int j = BlockPos.unpackLongY(sourceId);
               int k = BlockPos.unpackLongZ(sourceId);
               int l = BlockPos.unpackLongX(targetId);
               int m = BlockPos.unpackLongY(targetId);
               int n = BlockPos.unpackLongZ(targetId);
               boolean bl = i == l && k == n;
               int o = Integer.signum(l - i);
               int p = Integer.signum(m - j);
               int q = Integer.signum(n - k);
               Direction direction2;
               if (sourceId == Long.MAX_VALUE) {
                  direction2 = Direction.DOWN;
               } else {
                  direction2 = Direction.fromVector(o, p, q);
               }

               BlockState blockState2 = this.getStateForLighting(sourceId, (MutableInt)null);
               VoxelShape voxelShape;
               if (direction2 != null) {
                  voxelShape = this.getOpaqueShape(blockState2, sourceId, direction2);
                  VoxelShape voxelShape2 = this.getOpaqueShape(blockState, targetId, direction2.getOpposite());
                  if (VoxelShapes.unionCoversFullCube(voxelShape, voxelShape2)) {
                     return 15;
                  }
               } else {
                  voxelShape = this.getOpaqueShape(blockState2, sourceId, Direction.DOWN);
                  if (VoxelShapes.unionCoversFullCube(voxelShape, VoxelShapes.empty())) {
                     return 15;
                  }

                  int r = bl ? -1 : 0;
                  Direction direction3 = Direction.fromVector(o, r, q);
                  if (direction3 == null) {
                     return 15;
                  }

                  VoxelShape voxelShape4 = this.getOpaqueShape(blockState, targetId, direction3.getOpposite());
                  if (VoxelShapes.unionCoversFullCube(VoxelShapes.empty(), voxelShape4)) {
                     return 15;
                  }
               }

               boolean bl2 = sourceId == Long.MAX_VALUE || bl && j > m;
               return bl2 && level == 0 && mutableInt.getValue() == 0 ? 0 : level + Math.max(1, mutableInt.getValue());
            }
         }
      }
   }

   protected void propagateLevel(long id, int level, boolean decrease) {
      long l = ChunkSectionPos.fromBlockPos(id);
      int i = BlockPos.unpackLongY(id);
      int j = ChunkSectionPos.getLocalCoord(i);
      int k = ChunkSectionPos.getSectionCoord(i);
      int o;
      if (j != 0) {
         o = 0;
      } else {
         int n;
         for(n = 0; !((SkyLightStorage)this.lightStorage).hasSection(ChunkSectionPos.offset(l, 0, -n - 1, 0)) && ((SkyLightStorage)this.lightStorage).isAboveMinHeight(k - n - 1); ++n) {
         }

         o = n;
      }

      long p = BlockPos.add(id, 0, -1 - o * 16, 0);
      long q = ChunkSectionPos.fromBlockPos(p);
      if (l == q || ((SkyLightStorage)this.lightStorage).hasSection(q)) {
         this.propagateLevel(id, p, level, decrease);
      }

      long r = BlockPos.offset(id, Direction.UP);
      long s = ChunkSectionPos.fromBlockPos(r);
      if (l == s || ((SkyLightStorage)this.lightStorage).hasSection(s)) {
         this.propagateLevel(id, r, level, decrease);
      }

      Direction[] var19 = HORIZONTAL_DIRECTIONS;
      int var20 = var19.length;

      for(int var21 = 0; var21 < var20; ++var21) {
         Direction direction = var19[var21];
         int t = 0;

         while(true) {
            long u = BlockPos.add(id, direction.getOffsetX(), -t, direction.getOffsetZ());
            long v = ChunkSectionPos.fromBlockPos(u);
            if (l == v) {
               this.propagateLevel(id, u, level, decrease);
               break;
            }

            if (((SkyLightStorage)this.lightStorage).hasSection(v)) {
               this.propagateLevel(id, u, level, decrease);
            }

            ++t;
            if (t > o * 16) {
               break;
            }
         }
      }

   }

   protected int recalculateLevel(long id, long excludedId, int maxLevel) {
      int i = maxLevel;
      if (Long.MAX_VALUE != excludedId) {
         int j = this.getPropagatedLevel(Long.MAX_VALUE, id, 0);
         if (maxLevel > j) {
            i = j;
         }

         if (i == 0) {
            return i;
         }
      }

      long l = ChunkSectionPos.fromBlockPos(id);
      ChunkNibbleArray chunkNibbleArray = ((SkyLightStorage)this.lightStorage).getLightSection(l, true);
      Direction[] var10 = DIRECTIONS;
      int var11 = var10.length;

      for(int var12 = 0; var12 < var11; ++var12) {
         Direction direction = var10[var12];
         long m = BlockPos.offset(id, direction);
         long n = ChunkSectionPos.fromBlockPos(m);
         ChunkNibbleArray chunkNibbleArray3;
         if (l == n) {
            chunkNibbleArray3 = chunkNibbleArray;
         } else {
            chunkNibbleArray3 = ((SkyLightStorage)this.lightStorage).getLightSection(n, true);
         }

         if (chunkNibbleArray3 != null) {
            if (m != excludedId) {
               int k = this.getPropagatedLevel(m, id, this.getCurrentLevelFromSection(chunkNibbleArray3, m));
               if (i > k) {
                  i = k;
               }

               if (i == 0) {
                  return i;
               }
            }
         } else if (direction != Direction.DOWN) {
            for(m = BlockPos.removeChunkSectionLocalY(m); !((SkyLightStorage)this.lightStorage).hasSection(n) && !((SkyLightStorage)this.lightStorage).isAtOrAboveTopmostSection(n); m = BlockPos.add(m, 0, 16, 0)) {
               n = ChunkSectionPos.offset(n, Direction.UP);
            }

            ChunkNibbleArray chunkNibbleArray4 = ((SkyLightStorage)this.lightStorage).getLightSection(n, true);
            if (m != excludedId) {
               int p;
               if (chunkNibbleArray4 != null) {
                  p = this.getPropagatedLevel(m, id, this.getCurrentLevelFromSection(chunkNibbleArray4, m));
               } else {
                  p = ((SkyLightStorage)this.lightStorage).isSectionEnabled(n) ? 0 : 15;
               }

               if (i > p) {
                  i = p;
               }

               if (i == 0) {
                  return i;
               }
            }
         }
      }

      return i;
   }

   protected void resetLevel(long id) {
      ((SkyLightStorage)this.lightStorage).updateAll();
      long l = ChunkSectionPos.fromBlockPos(id);
      if (((SkyLightStorage)this.lightStorage).hasSection(l)) {
         super.resetLevel(id);
      } else {
         for(id = BlockPos.removeChunkSectionLocalY(id); !((SkyLightStorage)this.lightStorage).hasSection(l) && !((SkyLightStorage)this.lightStorage).isAtOrAboveTopmostSection(l); id = BlockPos.add(id, 0, 16, 0)) {
            l = ChunkSectionPos.offset(l, Direction.UP);
         }

         if (((SkyLightStorage)this.lightStorage).hasSection(l)) {
            super.resetLevel(id);
         }
      }

   }

   @Environment(EnvType.CLIENT)
   public String displaySectionLevel(long sectionPos) {
      return super.displaySectionLevel(sectionPos) + (((SkyLightStorage)this.lightStorage).isAtOrAboveTopmostSection(sectionPos) ? "*" : "");
   }

   static {
      HORIZONTAL_DIRECTIONS = new Direction[]{Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST};
   }
}
