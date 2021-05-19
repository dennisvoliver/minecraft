package net.minecraft.world.chunk;

import java.util.function.Predicate;
import java.util.stream.Stream;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.CollisionView;
import net.minecraft.world.World;
import net.minecraft.world.border.WorldBorder;
import org.jetbrains.annotations.Nullable;

public class ChunkCache implements BlockView, CollisionView {
   protected final int minX;
   protected final int minZ;
   protected final Chunk[][] chunks;
   protected boolean empty;
   protected final World world;

   public ChunkCache(World world, BlockPos minPos, BlockPos maxPos) {
      this.world = world;
      this.minX = minPos.getX() >> 4;
      this.minZ = minPos.getZ() >> 4;
      int i = maxPos.getX() >> 4;
      int j = maxPos.getZ() >> 4;
      this.chunks = new Chunk[i - this.minX + 1][j - this.minZ + 1];
      ChunkManager chunkManager = world.getChunkManager();
      this.empty = true;

      int m;
      int n;
      for(m = this.minX; m <= i; ++m) {
         for(n = this.minZ; n <= j; ++n) {
            this.chunks[m - this.minX][n - this.minZ] = chunkManager.getWorldChunk(m, n);
         }
      }

      for(m = minPos.getX() >> 4; m <= maxPos.getX() >> 4; ++m) {
         for(n = minPos.getZ() >> 4; n <= maxPos.getZ() >> 4; ++n) {
            Chunk chunk = this.chunks[m - this.minX][n - this.minZ];
            if (chunk != null && !chunk.areSectionsEmptyBetween(minPos.getY(), maxPos.getY())) {
               this.empty = false;
               return;
            }
         }
      }

   }

   private Chunk method_22354(BlockPos blockPos) {
      return this.method_22353(blockPos.getX() >> 4, blockPos.getZ() >> 4);
   }

   private Chunk method_22353(int i, int j) {
      int k = i - this.minX;
      int l = j - this.minZ;
      if (k >= 0 && k < this.chunks.length && l >= 0 && l < this.chunks[k].length) {
         Chunk chunk = this.chunks[k][l];
         return (Chunk)(chunk != null ? chunk : new EmptyChunk(this.world, new ChunkPos(i, j)));
      } else {
         return new EmptyChunk(this.world, new ChunkPos(i, j));
      }
   }

   public WorldBorder getWorldBorder() {
      return this.world.getWorldBorder();
   }

   public BlockView getExistingChunk(int chunkX, int chunkZ) {
      return this.method_22353(chunkX, chunkZ);
   }

   @Nullable
   public BlockEntity getBlockEntity(BlockPos pos) {
      Chunk chunk = this.method_22354(pos);
      return chunk.getBlockEntity(pos);
   }

   public BlockState getBlockState(BlockPos pos) {
      if (World.isOutOfBuildLimitVertically(pos)) {
         return Blocks.AIR.getDefaultState();
      } else {
         Chunk chunk = this.method_22354(pos);
         return chunk.getBlockState(pos);
      }
   }

   public Stream<VoxelShape> getEntityCollisions(@Nullable Entity entity, Box box, Predicate<Entity> predicate) {
      return Stream.empty();
   }

   public Stream<VoxelShape> getCollisions(@Nullable Entity entity, Box box, Predicate<Entity> predicate) {
      return this.getBlockCollisions(entity, box);
   }

   public FluidState getFluidState(BlockPos pos) {
      if (World.isOutOfBuildLimitVertically(pos)) {
         return Fluids.EMPTY.getDefaultState();
      } else {
         Chunk chunk = this.method_22354(pos);
         return chunk.getFluidState(pos);
      }
   }
}
