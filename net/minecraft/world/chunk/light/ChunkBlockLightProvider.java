package net.minecraft.world.chunk.light;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.LightType;
import net.minecraft.world.chunk.ChunkNibbleArray;
import net.minecraft.world.chunk.ChunkProvider;
import org.apache.commons.lang3.mutable.MutableInt;

public final class ChunkBlockLightProvider extends ChunkLightProvider<BlockLightStorage.Data, BlockLightStorage> {
   private static final Direction[] DIRECTIONS = Direction.values();
   private final BlockPos.Mutable mutablePos = new BlockPos.Mutable();

   public ChunkBlockLightProvider(ChunkProvider chunkProvider) {
      super(chunkProvider, LightType.BLOCK, new BlockLightStorage(chunkProvider));
   }

   private int getLightSourceLuminance(long blockPos) {
      int i = BlockPos.unpackLongX(blockPos);
      int j = BlockPos.unpackLongY(blockPos);
      int k = BlockPos.unpackLongZ(blockPos);
      BlockView blockView = this.chunkProvider.getChunk(i >> 4, k >> 4);
      return blockView != null ? blockView.getLuminance(this.mutablePos.set(i, j, k)) : 0;
   }

   protected int getPropagatedLevel(long sourceId, long targetId, int level) {
      if (targetId == Long.MAX_VALUE) {
         return 15;
      } else if (sourceId == Long.MAX_VALUE) {
         return level + 15 - this.getLightSourceLuminance(targetId);
      } else if (level >= 15) {
         return level;
      } else {
         int i = Integer.signum(BlockPos.unpackLongX(targetId) - BlockPos.unpackLongX(sourceId));
         int j = Integer.signum(BlockPos.unpackLongY(targetId) - BlockPos.unpackLongY(sourceId));
         int k = Integer.signum(BlockPos.unpackLongZ(targetId) - BlockPos.unpackLongZ(sourceId));
         Direction direction = Direction.fromVector(i, j, k);
         if (direction == null) {
            return 15;
         } else {
            MutableInt mutableInt = new MutableInt();
            BlockState blockState = this.getStateForLighting(targetId, mutableInt);
            if (mutableInt.getValue() >= 15) {
               return 15;
            } else {
               BlockState blockState2 = this.getStateForLighting(sourceId, (MutableInt)null);
               VoxelShape voxelShape = this.getOpaqueShape(blockState2, sourceId, direction);
               VoxelShape voxelShape2 = this.getOpaqueShape(blockState, targetId, direction.getOpposite());
               return VoxelShapes.unionCoversFullCube(voxelShape, voxelShape2) ? 15 : level + Math.max(1, mutableInt.getValue());
            }
         }
      }
   }

   protected void propagateLevel(long id, int level, boolean decrease) {
      long l = ChunkSectionPos.fromBlockPos(id);
      Direction[] var7 = DIRECTIONS;
      int var8 = var7.length;

      for(int var9 = 0; var9 < var8; ++var9) {
         Direction direction = var7[var9];
         long m = BlockPos.offset(id, direction);
         long n = ChunkSectionPos.fromBlockPos(m);
         if (l == n || ((BlockLightStorage)this.lightStorage).hasSection(n)) {
            this.propagateLevel(id, m, level, decrease);
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
      ChunkNibbleArray chunkNibbleArray = ((BlockLightStorage)this.lightStorage).getLightSection(l, true);
      Direction[] var10 = DIRECTIONS;
      int var11 = var10.length;

      for(int var12 = 0; var12 < var11; ++var12) {
         Direction direction = var10[var12];
         long m = BlockPos.offset(id, direction);
         if (m != excludedId) {
            long n = ChunkSectionPos.fromBlockPos(m);
            ChunkNibbleArray chunkNibbleArray3;
            if (l == n) {
               chunkNibbleArray3 = chunkNibbleArray;
            } else {
               chunkNibbleArray3 = ((BlockLightStorage)this.lightStorage).getLightSection(n, true);
            }

            if (chunkNibbleArray3 != null) {
               int k = this.getPropagatedLevel(m, id, this.getCurrentLevelFromSection(chunkNibbleArray3, m));
               if (i > k) {
                  i = k;
               }

               if (i == 0) {
                  return i;
               }
            }
         }
      }

      return i;
   }

   public void addLightSource(BlockPos pos, int level) {
      ((BlockLightStorage)this.lightStorage).updateAll();
      this.updateLevel(Long.MAX_VALUE, pos.asLong(), 15 - level, true);
   }
}
