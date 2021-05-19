package net.minecraft.structure;

import com.google.common.collect.Lists;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FenceBlock;
import net.minecraft.block.StairsBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.MobSpawnerBlockEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.fluid.Fluids;
import net.minecraft.loot.LootTables;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import org.jetbrains.annotations.Nullable;

public class NetherFortressGenerator {
   private static final NetherFortressGenerator.PieceData[] ALL_BRIDGE_PIECES = new NetherFortressGenerator.PieceData[]{new NetherFortressGenerator.PieceData(NetherFortressGenerator.Bridge.class, 30, 0, true), new NetherFortressGenerator.PieceData(NetherFortressGenerator.BridgeCrossing.class, 10, 4), new NetherFortressGenerator.PieceData(NetherFortressGenerator.BridgeSmallCrossing.class, 10, 4), new NetherFortressGenerator.PieceData(NetherFortressGenerator.BridgeStairs.class, 10, 3), new NetherFortressGenerator.PieceData(NetherFortressGenerator.BridgePlatform.class, 5, 2), new NetherFortressGenerator.PieceData(NetherFortressGenerator.CorridorExit.class, 5, 1)};
   private static final NetherFortressGenerator.PieceData[] ALL_CORRIDOR_PIECES = new NetherFortressGenerator.PieceData[]{new NetherFortressGenerator.PieceData(NetherFortressGenerator.SmallCorridor.class, 25, 0, true), new NetherFortressGenerator.PieceData(NetherFortressGenerator.CorridorCrossing.class, 15, 5), new NetherFortressGenerator.PieceData(NetherFortressGenerator.CorridorRightTurn.class, 5, 10), new NetherFortressGenerator.PieceData(NetherFortressGenerator.CorridorLeftTurn.class, 5, 10), new NetherFortressGenerator.PieceData(NetherFortressGenerator.CorridorStairs.class, 10, 3, true), new NetherFortressGenerator.PieceData(NetherFortressGenerator.CorridorBalcony.class, 7, 2), new NetherFortressGenerator.PieceData(NetherFortressGenerator.CorridorNetherWartsRoom.class, 5, 2)};

   private static NetherFortressGenerator.Piece createPiece(NetherFortressGenerator.PieceData pieceData, List<StructurePiece> pieces, Random random, int x, int y, int z, Direction orientation, int chainLength) {
      Class<? extends NetherFortressGenerator.Piece> class_ = pieceData.pieceType;
      NetherFortressGenerator.Piece piece = null;
      if (class_ == NetherFortressGenerator.Bridge.class) {
         piece = NetherFortressGenerator.Bridge.create(pieces, random, x, y, z, orientation, chainLength);
      } else if (class_ == NetherFortressGenerator.BridgeCrossing.class) {
         piece = NetherFortressGenerator.BridgeCrossing.create(pieces, x, y, z, orientation, chainLength);
      } else if (class_ == NetherFortressGenerator.BridgeSmallCrossing.class) {
         piece = NetherFortressGenerator.BridgeSmallCrossing.create(pieces, x, y, z, orientation, chainLength);
      } else if (class_ == NetherFortressGenerator.BridgeStairs.class) {
         piece = NetherFortressGenerator.BridgeStairs.create(pieces, x, y, z, chainLength, orientation);
      } else if (class_ == NetherFortressGenerator.BridgePlatform.class) {
         piece = NetherFortressGenerator.BridgePlatform.create(pieces, x, y, z, chainLength, orientation);
      } else if (class_ == NetherFortressGenerator.CorridorExit.class) {
         piece = NetherFortressGenerator.CorridorExit.create(pieces, random, x, y, z, orientation, chainLength);
      } else if (class_ == NetherFortressGenerator.SmallCorridor.class) {
         piece = NetherFortressGenerator.SmallCorridor.create(pieces, x, y, z, orientation, chainLength);
      } else if (class_ == NetherFortressGenerator.CorridorRightTurn.class) {
         piece = NetherFortressGenerator.CorridorRightTurn.create(pieces, random, x, y, z, orientation, chainLength);
      } else if (class_ == NetherFortressGenerator.CorridorLeftTurn.class) {
         piece = NetherFortressGenerator.CorridorLeftTurn.create(pieces, random, x, y, z, orientation, chainLength);
      } else if (class_ == NetherFortressGenerator.CorridorStairs.class) {
         piece = NetherFortressGenerator.CorridorStairs.create(pieces, x, y, z, orientation, chainLength);
      } else if (class_ == NetherFortressGenerator.CorridorBalcony.class) {
         piece = NetherFortressGenerator.CorridorBalcony.create(pieces, x, y, z, orientation, chainLength);
      } else if (class_ == NetherFortressGenerator.CorridorCrossing.class) {
         piece = NetherFortressGenerator.CorridorCrossing.create(pieces, x, y, z, orientation, chainLength);
      } else if (class_ == NetherFortressGenerator.CorridorNetherWartsRoom.class) {
         piece = NetherFortressGenerator.CorridorNetherWartsRoom.create(pieces, x, y, z, orientation, chainLength);
      }

      return (NetherFortressGenerator.Piece)piece;
   }

   public static class CorridorBalcony extends NetherFortressGenerator.Piece {
      public CorridorBalcony(int chainLength, BlockBox boundingBox, Direction orientation) {
         super(StructurePieceType.NETHER_FORTRESS_CORRIDOR_BALCONY, chainLength);
         this.setOrientation(orientation);
         this.boundingBox = boundingBox;
      }

      public CorridorBalcony(StructureManager structureManager, CompoundTag compoundTag) {
         super(StructurePieceType.NETHER_FORTRESS_CORRIDOR_BALCONY, compoundTag);
      }

      public void fillOpenings(StructurePiece start, List<StructurePiece> pieces, Random random) {
         int i = 1;
         Direction direction = this.getFacing();
         if (direction == Direction.WEST || direction == Direction.NORTH) {
            i = 5;
         }

         this.fillNWOpening((NetherFortressGenerator.Start)start, pieces, random, 0, i, random.nextInt(8) > 0);
         this.fillSEOpening((NetherFortressGenerator.Start)start, pieces, random, 0, i, random.nextInt(8) > 0);
      }

      public static NetherFortressGenerator.CorridorBalcony create(List<StructurePiece> pieces, int x, int y, int z, Direction orientation, int chainLength) {
         BlockBox blockBox = BlockBox.rotated(x, y, z, -3, 0, 0, 9, 7, 9, orientation);
         return isInbounds(blockBox) && StructurePiece.getOverlappingPiece(pieces, blockBox) == null ? new NetherFortressGenerator.CorridorBalcony(chainLength, blockBox, orientation) : null;
      }

      public boolean generate(StructureWorldAccess structureWorldAccess, StructureAccessor structureAccessor, ChunkGenerator chunkGenerator, Random random, BlockBox boundingBox, ChunkPos chunkPos, BlockPos blockPos) {
         BlockState blockState = (BlockState)((BlockState)Blocks.NETHER_BRICK_FENCE.getDefaultState().with(FenceBlock.NORTH, true)).with(FenceBlock.SOUTH, true);
         BlockState blockState2 = (BlockState)((BlockState)Blocks.NETHER_BRICK_FENCE.getDefaultState().with(FenceBlock.WEST, true)).with(FenceBlock.EAST, true);
         this.fillWithOutline(structureWorldAccess, boundingBox, 0, 0, 0, 8, 1, 8, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
         this.fillWithOutline(structureWorldAccess, boundingBox, 0, 2, 0, 8, 5, 8, Blocks.AIR.getDefaultState(), Blocks.AIR.getDefaultState(), false);
         this.fillWithOutline(structureWorldAccess, boundingBox, 0, 6, 0, 8, 6, 5, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
         this.fillWithOutline(structureWorldAccess, boundingBox, 0, 2, 0, 2, 5, 0, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
         this.fillWithOutline(structureWorldAccess, boundingBox, 6, 2, 0, 8, 5, 0, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
         this.fillWithOutline(structureWorldAccess, boundingBox, 1, 3, 0, 1, 4, 0, blockState2, blockState2, false);
         this.fillWithOutline(structureWorldAccess, boundingBox, 7, 3, 0, 7, 4, 0, blockState2, blockState2, false);
         this.fillWithOutline(structureWorldAccess, boundingBox, 0, 2, 4, 8, 2, 8, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
         this.fillWithOutline(structureWorldAccess, boundingBox, 1, 1, 4, 2, 2, 4, Blocks.AIR.getDefaultState(), Blocks.AIR.getDefaultState(), false);
         this.fillWithOutline(structureWorldAccess, boundingBox, 6, 1, 4, 7, 2, 4, Blocks.AIR.getDefaultState(), Blocks.AIR.getDefaultState(), false);
         this.fillWithOutline(structureWorldAccess, boundingBox, 1, 3, 8, 7, 3, 8, blockState2, blockState2, false);
         this.addBlock(structureWorldAccess, (BlockState)((BlockState)Blocks.NETHER_BRICK_FENCE.getDefaultState().with(FenceBlock.EAST, true)).with(FenceBlock.SOUTH, true), 0, 3, 8, boundingBox);
         this.addBlock(structureWorldAccess, (BlockState)((BlockState)Blocks.NETHER_BRICK_FENCE.getDefaultState().with(FenceBlock.WEST, true)).with(FenceBlock.SOUTH, true), 8, 3, 8, boundingBox);
         this.fillWithOutline(structureWorldAccess, boundingBox, 0, 3, 6, 0, 3, 7, blockState, blockState, false);
         this.fillWithOutline(structureWorldAccess, boundingBox, 8, 3, 6, 8, 3, 7, blockState, blockState, false);
         this.fillWithOutline(structureWorldAccess, boundingBox, 0, 3, 4, 0, 5, 5, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
         this.fillWithOutline(structureWorldAccess, boundingBox, 8, 3, 4, 8, 5, 5, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
         this.fillWithOutline(structureWorldAccess, boundingBox, 1, 3, 5, 2, 5, 5, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
         this.fillWithOutline(structureWorldAccess, boundingBox, 6, 3, 5, 7, 5, 5, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
         this.fillWithOutline(structureWorldAccess, boundingBox, 1, 4, 5, 1, 5, 5, blockState2, blockState2, false);
         this.fillWithOutline(structureWorldAccess, boundingBox, 7, 4, 5, 7, 5, 5, blockState2, blockState2, false);

         for(int i = 0; i <= 5; ++i) {
            for(int j = 0; j <= 8; ++j) {
               this.fillDownwards(structureWorldAccess, Blocks.NETHER_BRICKS.getDefaultState(), j, -1, i, boundingBox);
            }
         }

         return true;
      }
   }

   public static class CorridorStairs extends NetherFortressGenerator.Piece {
      public CorridorStairs(int chainLength, BlockBox boundingBox, Direction orientation) {
         super(StructurePieceType.NETHER_FORTRESS_CORRIDOR_STAIRS, chainLength);
         this.setOrientation(orientation);
         this.boundingBox = boundingBox;
      }

      public CorridorStairs(StructureManager structureManager, CompoundTag compoundTag) {
         super(StructurePieceType.NETHER_FORTRESS_CORRIDOR_STAIRS, compoundTag);
      }

      public void fillOpenings(StructurePiece start, List<StructurePiece> pieces, Random random) {
         this.fillForwardOpening((NetherFortressGenerator.Start)start, pieces, random, 1, 0, true);
      }

      public static NetherFortressGenerator.CorridorStairs create(List<StructurePiece> pieces, int x, int y, int z, Direction orientation, int chainLength) {
         BlockBox blockBox = BlockBox.rotated(x, y, z, -1, -7, 0, 5, 14, 10, orientation);
         return isInbounds(blockBox) && StructurePiece.getOverlappingPiece(pieces, blockBox) == null ? new NetherFortressGenerator.CorridorStairs(chainLength, blockBox, orientation) : null;
      }

      public boolean generate(StructureWorldAccess structureWorldAccess, StructureAccessor structureAccessor, ChunkGenerator chunkGenerator, Random random, BlockBox boundingBox, ChunkPos chunkPos, BlockPos blockPos) {
         BlockState blockState = (BlockState)Blocks.NETHER_BRICK_STAIRS.getDefaultState().with(StairsBlock.FACING, Direction.SOUTH);
         BlockState blockState2 = (BlockState)((BlockState)Blocks.NETHER_BRICK_FENCE.getDefaultState().with(FenceBlock.NORTH, true)).with(FenceBlock.SOUTH, true);

         for(int i = 0; i <= 9; ++i) {
            int j = Math.max(1, 7 - i);
            int k = Math.min(Math.max(j + 5, 14 - i), 13);
            int l = i;
            this.fillWithOutline(structureWorldAccess, boundingBox, 0, 0, i, 4, j, i, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
            this.fillWithOutline(structureWorldAccess, boundingBox, 1, j + 1, i, 3, k - 1, i, Blocks.AIR.getDefaultState(), Blocks.AIR.getDefaultState(), false);
            if (i <= 6) {
               this.addBlock(structureWorldAccess, blockState, 1, j + 1, i, boundingBox);
               this.addBlock(structureWorldAccess, blockState, 2, j + 1, i, boundingBox);
               this.addBlock(structureWorldAccess, blockState, 3, j + 1, i, boundingBox);
            }

            this.fillWithOutline(structureWorldAccess, boundingBox, 0, k, i, 4, k, i, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
            this.fillWithOutline(structureWorldAccess, boundingBox, 0, j + 1, i, 0, k - 1, i, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
            this.fillWithOutline(structureWorldAccess, boundingBox, 4, j + 1, i, 4, k - 1, i, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
            if ((i & 1) == 0) {
               this.fillWithOutline(structureWorldAccess, boundingBox, 0, j + 2, i, 0, j + 3, i, blockState2, blockState2, false);
               this.fillWithOutline(structureWorldAccess, boundingBox, 4, j + 2, i, 4, j + 3, i, blockState2, blockState2, false);
            }

            for(int m = 0; m <= 4; ++m) {
               this.fillDownwards(structureWorldAccess, Blocks.NETHER_BRICKS.getDefaultState(), m, -1, l, boundingBox);
            }
         }

         return true;
      }
   }

   public static class CorridorLeftTurn extends NetherFortressGenerator.Piece {
      private boolean containsChest;

      public CorridorLeftTurn(int chainLength, Random random, BlockBox boundingBox, Direction orientation) {
         super(StructurePieceType.NETHER_FORTRESS_CORRIDOR_LEFT_TURN, chainLength);
         this.setOrientation(orientation);
         this.boundingBox = boundingBox;
         this.containsChest = random.nextInt(3) == 0;
      }

      public CorridorLeftTurn(StructureManager structureManager, CompoundTag compoundTag) {
         super(StructurePieceType.NETHER_FORTRESS_CORRIDOR_LEFT_TURN, compoundTag);
         this.containsChest = compoundTag.getBoolean("Chest");
      }

      protected void toNbt(CompoundTag tag) {
         super.toNbt(tag);
         tag.putBoolean("Chest", this.containsChest);
      }

      public void fillOpenings(StructurePiece start, List<StructurePiece> pieces, Random random) {
         this.fillNWOpening((NetherFortressGenerator.Start)start, pieces, random, 0, 1, true);
      }

      public static NetherFortressGenerator.CorridorLeftTurn create(List<StructurePiece> pieces, Random random, int x, int y, int z, Direction orientation, int chainLength) {
         BlockBox blockBox = BlockBox.rotated(x, y, z, -1, 0, 0, 5, 7, 5, orientation);
         return isInbounds(blockBox) && StructurePiece.getOverlappingPiece(pieces, blockBox) == null ? new NetherFortressGenerator.CorridorLeftTurn(chainLength, random, blockBox, orientation) : null;
      }

      public boolean generate(StructureWorldAccess structureWorldAccess, StructureAccessor structureAccessor, ChunkGenerator chunkGenerator, Random random, BlockBox boundingBox, ChunkPos chunkPos, BlockPos blockPos) {
         this.fillWithOutline(structureWorldAccess, boundingBox, 0, 0, 0, 4, 1, 4, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
         this.fillWithOutline(structureWorldAccess, boundingBox, 0, 2, 0, 4, 5, 4, Blocks.AIR.getDefaultState(), Blocks.AIR.getDefaultState(), false);
         BlockState blockState = (BlockState)((BlockState)Blocks.NETHER_BRICK_FENCE.getDefaultState().with(FenceBlock.WEST, true)).with(FenceBlock.EAST, true);
         BlockState blockState2 = (BlockState)((BlockState)Blocks.NETHER_BRICK_FENCE.getDefaultState().with(FenceBlock.NORTH, true)).with(FenceBlock.SOUTH, true);
         this.fillWithOutline(structureWorldAccess, boundingBox, 4, 2, 0, 4, 5, 4, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
         this.fillWithOutline(structureWorldAccess, boundingBox, 4, 3, 1, 4, 4, 1, blockState2, blockState2, false);
         this.fillWithOutline(structureWorldAccess, boundingBox, 4, 3, 3, 4, 4, 3, blockState2, blockState2, false);
         this.fillWithOutline(structureWorldAccess, boundingBox, 0, 2, 0, 0, 5, 0, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
         this.fillWithOutline(structureWorldAccess, boundingBox, 0, 2, 4, 3, 5, 4, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
         this.fillWithOutline(structureWorldAccess, boundingBox, 1, 3, 4, 1, 4, 4, blockState, blockState, false);
         this.fillWithOutline(structureWorldAccess, boundingBox, 3, 3, 4, 3, 4, 4, blockState, blockState, false);
         if (this.containsChest && boundingBox.contains(new BlockPos(this.applyXTransform(3, 3), this.applyYTransform(2), this.applyZTransform(3, 3)))) {
            this.containsChest = false;
            this.addChest(structureWorldAccess, boundingBox, random, 3, 2, 3, LootTables.NETHER_BRIDGE_CHEST);
         }

         this.fillWithOutline(structureWorldAccess, boundingBox, 0, 6, 0, 4, 6, 4, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);

         for(int i = 0; i <= 4; ++i) {
            for(int j = 0; j <= 4; ++j) {
               this.fillDownwards(structureWorldAccess, Blocks.NETHER_BRICKS.getDefaultState(), i, -1, j, boundingBox);
            }
         }

         return true;
      }
   }

   public static class CorridorRightTurn extends NetherFortressGenerator.Piece {
      private boolean containsChest;

      public CorridorRightTurn(int chainLength, Random random, BlockBox boundingBox, Direction orientation) {
         super(StructurePieceType.NETHER_FORTRESS_CORRIDOR_RIGHT_TURN, chainLength);
         this.setOrientation(orientation);
         this.boundingBox = boundingBox;
         this.containsChest = random.nextInt(3) == 0;
      }

      public CorridorRightTurn(StructureManager structureManager, CompoundTag compoundTag) {
         super(StructurePieceType.NETHER_FORTRESS_CORRIDOR_RIGHT_TURN, compoundTag);
         this.containsChest = compoundTag.getBoolean("Chest");
      }

      protected void toNbt(CompoundTag tag) {
         super.toNbt(tag);
         tag.putBoolean("Chest", this.containsChest);
      }

      public void fillOpenings(StructurePiece start, List<StructurePiece> pieces, Random random) {
         this.fillSEOpening((NetherFortressGenerator.Start)start, pieces, random, 0, 1, true);
      }

      public static NetherFortressGenerator.CorridorRightTurn create(List<StructurePiece> pieces, Random random, int x, int y, int z, Direction orientation, int chainLength) {
         BlockBox blockBox = BlockBox.rotated(x, y, z, -1, 0, 0, 5, 7, 5, orientation);
         return isInbounds(blockBox) && StructurePiece.getOverlappingPiece(pieces, blockBox) == null ? new NetherFortressGenerator.CorridorRightTurn(chainLength, random, blockBox, orientation) : null;
      }

      public boolean generate(StructureWorldAccess structureWorldAccess, StructureAccessor structureAccessor, ChunkGenerator chunkGenerator, Random random, BlockBox boundingBox, ChunkPos chunkPos, BlockPos blockPos) {
         this.fillWithOutline(structureWorldAccess, boundingBox, 0, 0, 0, 4, 1, 4, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
         this.fillWithOutline(structureWorldAccess, boundingBox, 0, 2, 0, 4, 5, 4, Blocks.AIR.getDefaultState(), Blocks.AIR.getDefaultState(), false);
         BlockState blockState = (BlockState)((BlockState)Blocks.NETHER_BRICK_FENCE.getDefaultState().with(FenceBlock.WEST, true)).with(FenceBlock.EAST, true);
         BlockState blockState2 = (BlockState)((BlockState)Blocks.NETHER_BRICK_FENCE.getDefaultState().with(FenceBlock.NORTH, true)).with(FenceBlock.SOUTH, true);
         this.fillWithOutline(structureWorldAccess, boundingBox, 0, 2, 0, 0, 5, 4, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
         this.fillWithOutline(structureWorldAccess, boundingBox, 0, 3, 1, 0, 4, 1, blockState2, blockState2, false);
         this.fillWithOutline(structureWorldAccess, boundingBox, 0, 3, 3, 0, 4, 3, blockState2, blockState2, false);
         this.fillWithOutline(structureWorldAccess, boundingBox, 4, 2, 0, 4, 5, 0, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
         this.fillWithOutline(structureWorldAccess, boundingBox, 1, 2, 4, 4, 5, 4, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
         this.fillWithOutline(structureWorldAccess, boundingBox, 1, 3, 4, 1, 4, 4, blockState, blockState, false);
         this.fillWithOutline(structureWorldAccess, boundingBox, 3, 3, 4, 3, 4, 4, blockState, blockState, false);
         if (this.containsChest && boundingBox.contains(new BlockPos(this.applyXTransform(1, 3), this.applyYTransform(2), this.applyZTransform(1, 3)))) {
            this.containsChest = false;
            this.addChest(structureWorldAccess, boundingBox, random, 1, 2, 3, LootTables.NETHER_BRIDGE_CHEST);
         }

         this.fillWithOutline(structureWorldAccess, boundingBox, 0, 6, 0, 4, 6, 4, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);

         for(int i = 0; i <= 4; ++i) {
            for(int j = 0; j <= 4; ++j) {
               this.fillDownwards(structureWorldAccess, Blocks.NETHER_BRICKS.getDefaultState(), i, -1, j, boundingBox);
            }
         }

         return true;
      }
   }

   public static class CorridorCrossing extends NetherFortressGenerator.Piece {
      public CorridorCrossing(int chainLength, BlockBox boundingBox, Direction orientation) {
         super(StructurePieceType.NETHER_FORTRESS_CORRIDOR_CROSSING, chainLength);
         this.setOrientation(orientation);
         this.boundingBox = boundingBox;
      }

      public CorridorCrossing(StructureManager structureManager, CompoundTag compoundTag) {
         super(StructurePieceType.NETHER_FORTRESS_CORRIDOR_CROSSING, compoundTag);
      }

      public void fillOpenings(StructurePiece start, List<StructurePiece> pieces, Random random) {
         this.fillForwardOpening((NetherFortressGenerator.Start)start, pieces, random, 1, 0, true);
         this.fillNWOpening((NetherFortressGenerator.Start)start, pieces, random, 0, 1, true);
         this.fillSEOpening((NetherFortressGenerator.Start)start, pieces, random, 0, 1, true);
      }

      public static NetherFortressGenerator.CorridorCrossing create(List<StructurePiece> pieces, int x, int y, int z, Direction orientation, int chainLength) {
         BlockBox blockBox = BlockBox.rotated(x, y, z, -1, 0, 0, 5, 7, 5, orientation);
         return isInbounds(blockBox) && StructurePiece.getOverlappingPiece(pieces, blockBox) == null ? new NetherFortressGenerator.CorridorCrossing(chainLength, blockBox, orientation) : null;
      }

      public boolean generate(StructureWorldAccess structureWorldAccess, StructureAccessor structureAccessor, ChunkGenerator chunkGenerator, Random random, BlockBox boundingBox, ChunkPos chunkPos, BlockPos blockPos) {
         this.fillWithOutline(structureWorldAccess, boundingBox, 0, 0, 0, 4, 1, 4, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
         this.fillWithOutline(structureWorldAccess, boundingBox, 0, 2, 0, 4, 5, 4, Blocks.AIR.getDefaultState(), Blocks.AIR.getDefaultState(), false);
         this.fillWithOutline(structureWorldAccess, boundingBox, 0, 2, 0, 0, 5, 0, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
         this.fillWithOutline(structureWorldAccess, boundingBox, 4, 2, 0, 4, 5, 0, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
         this.fillWithOutline(structureWorldAccess, boundingBox, 0, 2, 4, 0, 5, 4, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
         this.fillWithOutline(structureWorldAccess, boundingBox, 4, 2, 4, 4, 5, 4, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
         this.fillWithOutline(structureWorldAccess, boundingBox, 0, 6, 0, 4, 6, 4, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);

         for(int i = 0; i <= 4; ++i) {
            for(int j = 0; j <= 4; ++j) {
               this.fillDownwards(structureWorldAccess, Blocks.NETHER_BRICKS.getDefaultState(), i, -1, j, boundingBox);
            }
         }

         return true;
      }
   }

   public static class SmallCorridor extends NetherFortressGenerator.Piece {
      public SmallCorridor(int chainLength, BlockBox boundingBox, Direction orientation) {
         super(StructurePieceType.NETHER_FORTRESS_SMALL_CORRIDOR, chainLength);
         this.setOrientation(orientation);
         this.boundingBox = boundingBox;
      }

      public SmallCorridor(StructureManager structureManager, CompoundTag compoundTag) {
         super(StructurePieceType.NETHER_FORTRESS_SMALL_CORRIDOR, compoundTag);
      }

      public void fillOpenings(StructurePiece start, List<StructurePiece> pieces, Random random) {
         this.fillForwardOpening((NetherFortressGenerator.Start)start, pieces, random, 1, 0, true);
      }

      public static NetherFortressGenerator.SmallCorridor create(List<StructurePiece> pieces, int x, int y, int z, Direction orientation, int chainLength) {
         BlockBox blockBox = BlockBox.rotated(x, y, z, -1, 0, 0, 5, 7, 5, orientation);
         return isInbounds(blockBox) && StructurePiece.getOverlappingPiece(pieces, blockBox) == null ? new NetherFortressGenerator.SmallCorridor(chainLength, blockBox, orientation) : null;
      }

      public boolean generate(StructureWorldAccess structureWorldAccess, StructureAccessor structureAccessor, ChunkGenerator chunkGenerator, Random random, BlockBox boundingBox, ChunkPos chunkPos, BlockPos blockPos) {
         this.fillWithOutline(structureWorldAccess, boundingBox, 0, 0, 0, 4, 1, 4, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
         this.fillWithOutline(structureWorldAccess, boundingBox, 0, 2, 0, 4, 5, 4, Blocks.AIR.getDefaultState(), Blocks.AIR.getDefaultState(), false);
         BlockState blockState = (BlockState)((BlockState)Blocks.NETHER_BRICK_FENCE.getDefaultState().with(FenceBlock.NORTH, true)).with(FenceBlock.SOUTH, true);
         this.fillWithOutline(structureWorldAccess, boundingBox, 0, 2, 0, 0, 5, 4, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
         this.fillWithOutline(structureWorldAccess, boundingBox, 4, 2, 0, 4, 5, 4, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
         this.fillWithOutline(structureWorldAccess, boundingBox, 0, 3, 1, 0, 4, 1, blockState, blockState, false);
         this.fillWithOutline(structureWorldAccess, boundingBox, 0, 3, 3, 0, 4, 3, blockState, blockState, false);
         this.fillWithOutline(structureWorldAccess, boundingBox, 4, 3, 1, 4, 4, 1, blockState, blockState, false);
         this.fillWithOutline(structureWorldAccess, boundingBox, 4, 3, 3, 4, 4, 3, blockState, blockState, false);
         this.fillWithOutline(structureWorldAccess, boundingBox, 0, 6, 0, 4, 6, 4, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);

         for(int i = 0; i <= 4; ++i) {
            for(int j = 0; j <= 4; ++j) {
               this.fillDownwards(structureWorldAccess, Blocks.NETHER_BRICKS.getDefaultState(), i, -1, j, boundingBox);
            }
         }

         return true;
      }
   }

   public static class CorridorNetherWartsRoom extends NetherFortressGenerator.Piece {
      public CorridorNetherWartsRoom(int chainLength, BlockBox boundingBox, Direction orientation) {
         super(StructurePieceType.NETHER_FORTRESS_CORRIDOR_NETHER_WARTS_ROOM, chainLength);
         this.setOrientation(orientation);
         this.boundingBox = boundingBox;
      }

      public CorridorNetherWartsRoom(StructureManager structureManager, CompoundTag compoundTag) {
         super(StructurePieceType.NETHER_FORTRESS_CORRIDOR_NETHER_WARTS_ROOM, compoundTag);
      }

      public void fillOpenings(StructurePiece start, List<StructurePiece> pieces, Random random) {
         this.fillForwardOpening((NetherFortressGenerator.Start)start, pieces, random, 5, 3, true);
         this.fillForwardOpening((NetherFortressGenerator.Start)start, pieces, random, 5, 11, true);
      }

      public static NetherFortressGenerator.CorridorNetherWartsRoom create(List<StructurePiece> pieces, int x, int y, int z, Direction orientation, int chainlength) {
         BlockBox blockBox = BlockBox.rotated(x, y, z, -5, -3, 0, 13, 14, 13, orientation);
         return isInbounds(blockBox) && StructurePiece.getOverlappingPiece(pieces, blockBox) == null ? new NetherFortressGenerator.CorridorNetherWartsRoom(chainlength, blockBox, orientation) : null;
      }

      public boolean generate(StructureWorldAccess structureWorldAccess, StructureAccessor structureAccessor, ChunkGenerator chunkGenerator, Random random, BlockBox boundingBox, ChunkPos chunkPos, BlockPos blockPos) {
         this.fillWithOutline(structureWorldAccess, boundingBox, 0, 3, 0, 12, 4, 12, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
         this.fillWithOutline(structureWorldAccess, boundingBox, 0, 5, 0, 12, 13, 12, Blocks.AIR.getDefaultState(), Blocks.AIR.getDefaultState(), false);
         this.fillWithOutline(structureWorldAccess, boundingBox, 0, 5, 0, 1, 12, 12, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
         this.fillWithOutline(structureWorldAccess, boundingBox, 11, 5, 0, 12, 12, 12, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
         this.fillWithOutline(structureWorldAccess, boundingBox, 2, 5, 11, 4, 12, 12, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
         this.fillWithOutline(structureWorldAccess, boundingBox, 8, 5, 11, 10, 12, 12, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
         this.fillWithOutline(structureWorldAccess, boundingBox, 5, 9, 11, 7, 12, 12, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
         this.fillWithOutline(structureWorldAccess, boundingBox, 2, 5, 0, 4, 12, 1, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
         this.fillWithOutline(structureWorldAccess, boundingBox, 8, 5, 0, 10, 12, 1, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
         this.fillWithOutline(structureWorldAccess, boundingBox, 5, 9, 0, 7, 12, 1, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
         this.fillWithOutline(structureWorldAccess, boundingBox, 2, 11, 2, 10, 12, 10, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
         BlockState blockState = (BlockState)((BlockState)Blocks.NETHER_BRICK_FENCE.getDefaultState().with(FenceBlock.WEST, true)).with(FenceBlock.EAST, true);
         BlockState blockState2 = (BlockState)((BlockState)Blocks.NETHER_BRICK_FENCE.getDefaultState().with(FenceBlock.NORTH, true)).with(FenceBlock.SOUTH, true);
         BlockState blockState3 = (BlockState)blockState2.with(FenceBlock.WEST, true);
         BlockState blockState4 = (BlockState)blockState2.with(FenceBlock.EAST, true);

         int j;
         for(j = 1; j <= 11; j += 2) {
            this.fillWithOutline(structureWorldAccess, boundingBox, j, 10, 0, j, 11, 0, blockState, blockState, false);
            this.fillWithOutline(structureWorldAccess, boundingBox, j, 10, 12, j, 11, 12, blockState, blockState, false);
            this.fillWithOutline(structureWorldAccess, boundingBox, 0, 10, j, 0, 11, j, blockState2, blockState2, false);
            this.fillWithOutline(structureWorldAccess, boundingBox, 12, 10, j, 12, 11, j, blockState2, blockState2, false);
            this.addBlock(structureWorldAccess, Blocks.NETHER_BRICKS.getDefaultState(), j, 13, 0, boundingBox);
            this.addBlock(structureWorldAccess, Blocks.NETHER_BRICKS.getDefaultState(), j, 13, 12, boundingBox);
            this.addBlock(structureWorldAccess, Blocks.NETHER_BRICKS.getDefaultState(), 0, 13, j, boundingBox);
            this.addBlock(structureWorldAccess, Blocks.NETHER_BRICKS.getDefaultState(), 12, 13, j, boundingBox);
            if (j != 11) {
               this.addBlock(structureWorldAccess, blockState, j + 1, 13, 0, boundingBox);
               this.addBlock(structureWorldAccess, blockState, j + 1, 13, 12, boundingBox);
               this.addBlock(structureWorldAccess, blockState2, 0, 13, j + 1, boundingBox);
               this.addBlock(structureWorldAccess, blockState2, 12, 13, j + 1, boundingBox);
            }
         }

         this.addBlock(structureWorldAccess, (BlockState)((BlockState)Blocks.NETHER_BRICK_FENCE.getDefaultState().with(FenceBlock.NORTH, true)).with(FenceBlock.EAST, true), 0, 13, 0, boundingBox);
         this.addBlock(structureWorldAccess, (BlockState)((BlockState)Blocks.NETHER_BRICK_FENCE.getDefaultState().with(FenceBlock.SOUTH, true)).with(FenceBlock.EAST, true), 0, 13, 12, boundingBox);
         this.addBlock(structureWorldAccess, (BlockState)((BlockState)Blocks.NETHER_BRICK_FENCE.getDefaultState().with(FenceBlock.SOUTH, true)).with(FenceBlock.WEST, true), 12, 13, 12, boundingBox);
         this.addBlock(structureWorldAccess, (BlockState)((BlockState)Blocks.NETHER_BRICK_FENCE.getDefaultState().with(FenceBlock.NORTH, true)).with(FenceBlock.WEST, true), 12, 13, 0, boundingBox);

         for(j = 3; j <= 9; j += 2) {
            this.fillWithOutline(structureWorldAccess, boundingBox, 1, 7, j, 1, 8, j, blockState3, blockState3, false);
            this.fillWithOutline(structureWorldAccess, boundingBox, 11, 7, j, 11, 8, j, blockState4, blockState4, false);
         }

         BlockState blockState5 = (BlockState)Blocks.NETHER_BRICK_STAIRS.getDefaultState().with(StairsBlock.FACING, Direction.NORTH);

         int k;
         int q;
         for(k = 0; k <= 6; ++k) {
            int l = k + 4;

            for(q = 5; q <= 7; ++q) {
               this.addBlock(structureWorldAccess, blockState5, q, 5 + k, l, boundingBox);
            }

            if (l >= 5 && l <= 8) {
               this.fillWithOutline(structureWorldAccess, boundingBox, 5, 5, l, 7, k + 4, l, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
            } else if (l >= 9 && l <= 10) {
               this.fillWithOutline(structureWorldAccess, boundingBox, 5, 8, l, 7, k + 4, l, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
            }

            if (k >= 1) {
               this.fillWithOutline(structureWorldAccess, boundingBox, 5, 6 + k, l, 7, 9 + k, l, Blocks.AIR.getDefaultState(), Blocks.AIR.getDefaultState(), false);
            }
         }

         for(k = 5; k <= 7; ++k) {
            this.addBlock(structureWorldAccess, blockState5, k, 12, 11, boundingBox);
         }

         this.fillWithOutline(structureWorldAccess, boundingBox, 5, 6, 7, 5, 7, 7, blockState4, blockState4, false);
         this.fillWithOutline(structureWorldAccess, boundingBox, 7, 6, 7, 7, 7, 7, blockState3, blockState3, false);
         this.fillWithOutline(structureWorldAccess, boundingBox, 5, 13, 12, 7, 13, 12, Blocks.AIR.getDefaultState(), Blocks.AIR.getDefaultState(), false);
         this.fillWithOutline(structureWorldAccess, boundingBox, 2, 5, 2, 3, 5, 3, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
         this.fillWithOutline(structureWorldAccess, boundingBox, 2, 5, 9, 3, 5, 10, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
         this.fillWithOutline(structureWorldAccess, boundingBox, 2, 5, 4, 2, 5, 8, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
         this.fillWithOutline(structureWorldAccess, boundingBox, 9, 5, 2, 10, 5, 3, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
         this.fillWithOutline(structureWorldAccess, boundingBox, 9, 5, 9, 10, 5, 10, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
         this.fillWithOutline(structureWorldAccess, boundingBox, 10, 5, 4, 10, 5, 8, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
         BlockState blockState6 = (BlockState)blockState5.with(StairsBlock.FACING, Direction.EAST);
         BlockState blockState7 = (BlockState)blockState5.with(StairsBlock.FACING, Direction.WEST);
         this.addBlock(structureWorldAccess, blockState7, 4, 5, 2, boundingBox);
         this.addBlock(structureWorldAccess, blockState7, 4, 5, 3, boundingBox);
         this.addBlock(structureWorldAccess, blockState7, 4, 5, 9, boundingBox);
         this.addBlock(structureWorldAccess, blockState7, 4, 5, 10, boundingBox);
         this.addBlock(structureWorldAccess, blockState6, 8, 5, 2, boundingBox);
         this.addBlock(structureWorldAccess, blockState6, 8, 5, 3, boundingBox);
         this.addBlock(structureWorldAccess, blockState6, 8, 5, 9, boundingBox);
         this.addBlock(structureWorldAccess, blockState6, 8, 5, 10, boundingBox);
         this.fillWithOutline(structureWorldAccess, boundingBox, 3, 4, 4, 4, 4, 8, Blocks.SOUL_SAND.getDefaultState(), Blocks.SOUL_SAND.getDefaultState(), false);
         this.fillWithOutline(structureWorldAccess, boundingBox, 8, 4, 4, 9, 4, 8, Blocks.SOUL_SAND.getDefaultState(), Blocks.SOUL_SAND.getDefaultState(), false);
         this.fillWithOutline(structureWorldAccess, boundingBox, 3, 5, 4, 4, 5, 8, Blocks.NETHER_WART.getDefaultState(), Blocks.NETHER_WART.getDefaultState(), false);
         this.fillWithOutline(structureWorldAccess, boundingBox, 8, 5, 4, 9, 5, 8, Blocks.NETHER_WART.getDefaultState(), Blocks.NETHER_WART.getDefaultState(), false);
         this.fillWithOutline(structureWorldAccess, boundingBox, 4, 2, 0, 8, 2, 12, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
         this.fillWithOutline(structureWorldAccess, boundingBox, 0, 2, 4, 12, 2, 8, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
         this.fillWithOutline(structureWorldAccess, boundingBox, 4, 0, 0, 8, 1, 3, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
         this.fillWithOutline(structureWorldAccess, boundingBox, 4, 0, 9, 8, 1, 12, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
         this.fillWithOutline(structureWorldAccess, boundingBox, 0, 0, 4, 3, 1, 8, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
         this.fillWithOutline(structureWorldAccess, boundingBox, 9, 0, 4, 12, 1, 8, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);

         int r;
         for(q = 4; q <= 8; ++q) {
            for(r = 0; r <= 2; ++r) {
               this.fillDownwards(structureWorldAccess, Blocks.NETHER_BRICKS.getDefaultState(), q, -1, r, boundingBox);
               this.fillDownwards(structureWorldAccess, Blocks.NETHER_BRICKS.getDefaultState(), q, -1, 12 - r, boundingBox);
            }
         }

         for(q = 0; q <= 2; ++q) {
            for(r = 4; r <= 8; ++r) {
               this.fillDownwards(structureWorldAccess, Blocks.NETHER_BRICKS.getDefaultState(), q, -1, r, boundingBox);
               this.fillDownwards(structureWorldAccess, Blocks.NETHER_BRICKS.getDefaultState(), 12 - q, -1, r, boundingBox);
            }
         }

         return true;
      }
   }

   public static class CorridorExit extends NetherFortressGenerator.Piece {
      public CorridorExit(int chainLength, Random random, BlockBox boundingBox, Direction orientation) {
         super(StructurePieceType.NETHER_FORTRESS_CORRIDOR_EXIT, chainLength);
         this.setOrientation(orientation);
         this.boundingBox = boundingBox;
      }

      public CorridorExit(StructureManager structureManager, CompoundTag compoundTag) {
         super(StructurePieceType.NETHER_FORTRESS_CORRIDOR_EXIT, compoundTag);
      }

      public void fillOpenings(StructurePiece start, List<StructurePiece> pieces, Random random) {
         this.fillForwardOpening((NetherFortressGenerator.Start)start, pieces, random, 5, 3, true);
      }

      public static NetherFortressGenerator.CorridorExit create(List<StructurePiece> pieces, Random random, int x, int y, int z, Direction orientation, int chainLength) {
         BlockBox blockBox = BlockBox.rotated(x, y, z, -5, -3, 0, 13, 14, 13, orientation);
         return isInbounds(blockBox) && StructurePiece.getOverlappingPiece(pieces, blockBox) == null ? new NetherFortressGenerator.CorridorExit(chainLength, random, blockBox, orientation) : null;
      }

      public boolean generate(StructureWorldAccess structureWorldAccess, StructureAccessor structureAccessor, ChunkGenerator chunkGenerator, Random random, BlockBox boundingBox, ChunkPos chunkPos, BlockPos blockPos) {
         this.fillWithOutline(structureWorldAccess, boundingBox, 0, 3, 0, 12, 4, 12, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
         this.fillWithOutline(structureWorldAccess, boundingBox, 0, 5, 0, 12, 13, 12, Blocks.AIR.getDefaultState(), Blocks.AIR.getDefaultState(), false);
         this.fillWithOutline(structureWorldAccess, boundingBox, 0, 5, 0, 1, 12, 12, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
         this.fillWithOutline(structureWorldAccess, boundingBox, 11, 5, 0, 12, 12, 12, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
         this.fillWithOutline(structureWorldAccess, boundingBox, 2, 5, 11, 4, 12, 12, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
         this.fillWithOutline(structureWorldAccess, boundingBox, 8, 5, 11, 10, 12, 12, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
         this.fillWithOutline(structureWorldAccess, boundingBox, 5, 9, 11, 7, 12, 12, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
         this.fillWithOutline(structureWorldAccess, boundingBox, 2, 5, 0, 4, 12, 1, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
         this.fillWithOutline(structureWorldAccess, boundingBox, 8, 5, 0, 10, 12, 1, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
         this.fillWithOutline(structureWorldAccess, boundingBox, 5, 9, 0, 7, 12, 1, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
         this.fillWithOutline(structureWorldAccess, boundingBox, 2, 11, 2, 10, 12, 10, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
         this.fillWithOutline(structureWorldAccess, boundingBox, 5, 8, 0, 7, 8, 0, Blocks.NETHER_BRICK_FENCE.getDefaultState(), Blocks.NETHER_BRICK_FENCE.getDefaultState(), false);
         BlockState blockState = (BlockState)((BlockState)Blocks.NETHER_BRICK_FENCE.getDefaultState().with(FenceBlock.WEST, true)).with(FenceBlock.EAST, true);
         BlockState blockState2 = (BlockState)((BlockState)Blocks.NETHER_BRICK_FENCE.getDefaultState().with(FenceBlock.NORTH, true)).with(FenceBlock.SOUTH, true);

         int m;
         for(m = 1; m <= 11; m += 2) {
            this.fillWithOutline(structureWorldAccess, boundingBox, m, 10, 0, m, 11, 0, blockState, blockState, false);
            this.fillWithOutline(structureWorldAccess, boundingBox, m, 10, 12, m, 11, 12, blockState, blockState, false);
            this.fillWithOutline(structureWorldAccess, boundingBox, 0, 10, m, 0, 11, m, blockState2, blockState2, false);
            this.fillWithOutline(structureWorldAccess, boundingBox, 12, 10, m, 12, 11, m, blockState2, blockState2, false);
            this.addBlock(structureWorldAccess, Blocks.NETHER_BRICKS.getDefaultState(), m, 13, 0, boundingBox);
            this.addBlock(structureWorldAccess, Blocks.NETHER_BRICKS.getDefaultState(), m, 13, 12, boundingBox);
            this.addBlock(structureWorldAccess, Blocks.NETHER_BRICKS.getDefaultState(), 0, 13, m, boundingBox);
            this.addBlock(structureWorldAccess, Blocks.NETHER_BRICKS.getDefaultState(), 12, 13, m, boundingBox);
            if (m != 11) {
               this.addBlock(structureWorldAccess, blockState, m + 1, 13, 0, boundingBox);
               this.addBlock(structureWorldAccess, blockState, m + 1, 13, 12, boundingBox);
               this.addBlock(structureWorldAccess, blockState2, 0, 13, m + 1, boundingBox);
               this.addBlock(structureWorldAccess, blockState2, 12, 13, m + 1, boundingBox);
            }
         }

         this.addBlock(structureWorldAccess, (BlockState)((BlockState)Blocks.NETHER_BRICK_FENCE.getDefaultState().with(FenceBlock.NORTH, true)).with(FenceBlock.EAST, true), 0, 13, 0, boundingBox);
         this.addBlock(structureWorldAccess, (BlockState)((BlockState)Blocks.NETHER_BRICK_FENCE.getDefaultState().with(FenceBlock.SOUTH, true)).with(FenceBlock.EAST, true), 0, 13, 12, boundingBox);
         this.addBlock(structureWorldAccess, (BlockState)((BlockState)Blocks.NETHER_BRICK_FENCE.getDefaultState().with(FenceBlock.SOUTH, true)).with(FenceBlock.WEST, true), 12, 13, 12, boundingBox);
         this.addBlock(structureWorldAccess, (BlockState)((BlockState)Blocks.NETHER_BRICK_FENCE.getDefaultState().with(FenceBlock.NORTH, true)).with(FenceBlock.WEST, true), 12, 13, 0, boundingBox);

         for(m = 3; m <= 9; m += 2) {
            this.fillWithOutline(structureWorldAccess, boundingBox, 1, 7, m, 1, 8, m, (BlockState)blockState2.with(FenceBlock.WEST, true), (BlockState)blockState2.with(FenceBlock.WEST, true), false);
            this.fillWithOutline(structureWorldAccess, boundingBox, 11, 7, m, 11, 8, m, (BlockState)blockState2.with(FenceBlock.EAST, true), (BlockState)blockState2.with(FenceBlock.EAST, true), false);
         }

         this.fillWithOutline(structureWorldAccess, boundingBox, 4, 2, 0, 8, 2, 12, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
         this.fillWithOutline(structureWorldAccess, boundingBox, 0, 2, 4, 12, 2, 8, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
         this.fillWithOutline(structureWorldAccess, boundingBox, 4, 0, 0, 8, 1, 3, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
         this.fillWithOutline(structureWorldAccess, boundingBox, 4, 0, 9, 8, 1, 12, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
         this.fillWithOutline(structureWorldAccess, boundingBox, 0, 0, 4, 3, 1, 8, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
         this.fillWithOutline(structureWorldAccess, boundingBox, 9, 0, 4, 12, 1, 8, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);

         int n;
         for(m = 4; m <= 8; ++m) {
            for(n = 0; n <= 2; ++n) {
               this.fillDownwards(structureWorldAccess, Blocks.NETHER_BRICKS.getDefaultState(), m, -1, n, boundingBox);
               this.fillDownwards(structureWorldAccess, Blocks.NETHER_BRICKS.getDefaultState(), m, -1, 12 - n, boundingBox);
            }
         }

         for(m = 0; m <= 2; ++m) {
            for(n = 4; n <= 8; ++n) {
               this.fillDownwards(structureWorldAccess, Blocks.NETHER_BRICKS.getDefaultState(), m, -1, n, boundingBox);
               this.fillDownwards(structureWorldAccess, Blocks.NETHER_BRICKS.getDefaultState(), 12 - m, -1, n, boundingBox);
            }
         }

         this.fillWithOutline(structureWorldAccess, boundingBox, 5, 5, 5, 7, 5, 7, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
         this.fillWithOutline(structureWorldAccess, boundingBox, 6, 1, 6, 6, 4, 6, Blocks.AIR.getDefaultState(), Blocks.AIR.getDefaultState(), false);
         this.addBlock(structureWorldAccess, Blocks.NETHER_BRICKS.getDefaultState(), 6, 0, 6, boundingBox);
         this.addBlock(structureWorldAccess, Blocks.LAVA.getDefaultState(), 6, 5, 6, boundingBox);
         BlockPos blockPos2 = new BlockPos(this.applyXTransform(6, 6), this.applyYTransform(5), this.applyZTransform(6, 6));
         if (boundingBox.contains(blockPos2)) {
            structureWorldAccess.getFluidTickScheduler().schedule(blockPos2, Fluids.LAVA, 0);
         }

         return true;
      }
   }

   public static class BridgePlatform extends NetherFortressGenerator.Piece {
      private boolean hasBlazeSpawner;

      public BridgePlatform(int chainLength, BlockBox boundingBox, Direction orientation) {
         super(StructurePieceType.NETHER_FORTRESS_BRIDGE_PLATFORM, chainLength);
         this.setOrientation(orientation);
         this.boundingBox = boundingBox;
      }

      public BridgePlatform(StructureManager structureManager, CompoundTag compoundTag) {
         super(StructurePieceType.NETHER_FORTRESS_BRIDGE_PLATFORM, compoundTag);
         this.hasBlazeSpawner = compoundTag.getBoolean("Mob");
      }

      protected void toNbt(CompoundTag tag) {
         super.toNbt(tag);
         tag.putBoolean("Mob", this.hasBlazeSpawner);
      }

      public static NetherFortressGenerator.BridgePlatform create(List<StructurePiece> pieces, int x, int y, int z, int chainLength, Direction orientation) {
         BlockBox blockBox = BlockBox.rotated(x, y, z, -2, 0, 0, 7, 8, 9, orientation);
         return isInbounds(blockBox) && StructurePiece.getOverlappingPiece(pieces, blockBox) == null ? new NetherFortressGenerator.BridgePlatform(chainLength, blockBox, orientation) : null;
      }

      public boolean generate(StructureWorldAccess structureWorldAccess, StructureAccessor structureAccessor, ChunkGenerator chunkGenerator, Random random, BlockBox boundingBox, ChunkPos chunkPos, BlockPos blockPos) {
         this.fillWithOutline(structureWorldAccess, boundingBox, 0, 2, 0, 6, 7, 7, Blocks.AIR.getDefaultState(), Blocks.AIR.getDefaultState(), false);
         this.fillWithOutline(structureWorldAccess, boundingBox, 1, 0, 0, 5, 1, 7, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
         this.fillWithOutline(structureWorldAccess, boundingBox, 1, 2, 1, 5, 2, 7, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
         this.fillWithOutline(structureWorldAccess, boundingBox, 1, 3, 2, 5, 3, 7, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
         this.fillWithOutline(structureWorldAccess, boundingBox, 1, 4, 3, 5, 4, 7, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
         this.fillWithOutline(structureWorldAccess, boundingBox, 1, 2, 0, 1, 4, 2, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
         this.fillWithOutline(structureWorldAccess, boundingBox, 5, 2, 0, 5, 4, 2, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
         this.fillWithOutline(structureWorldAccess, boundingBox, 1, 5, 2, 1, 5, 3, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
         this.fillWithOutline(structureWorldAccess, boundingBox, 5, 5, 2, 5, 5, 3, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
         this.fillWithOutline(structureWorldAccess, boundingBox, 0, 5, 3, 0, 5, 8, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
         this.fillWithOutline(structureWorldAccess, boundingBox, 6, 5, 3, 6, 5, 8, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
         this.fillWithOutline(structureWorldAccess, boundingBox, 1, 5, 8, 5, 5, 8, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
         BlockState blockState = (BlockState)((BlockState)Blocks.NETHER_BRICK_FENCE.getDefaultState().with(FenceBlock.WEST, true)).with(FenceBlock.EAST, true);
         BlockState blockState2 = (BlockState)((BlockState)Blocks.NETHER_BRICK_FENCE.getDefaultState().with(FenceBlock.NORTH, true)).with(FenceBlock.SOUTH, true);
         this.addBlock(structureWorldAccess, (BlockState)Blocks.NETHER_BRICK_FENCE.getDefaultState().with(FenceBlock.WEST, true), 1, 6, 3, boundingBox);
         this.addBlock(structureWorldAccess, (BlockState)Blocks.NETHER_BRICK_FENCE.getDefaultState().with(FenceBlock.EAST, true), 5, 6, 3, boundingBox);
         this.addBlock(structureWorldAccess, (BlockState)((BlockState)Blocks.NETHER_BRICK_FENCE.getDefaultState().with(FenceBlock.EAST, true)).with(FenceBlock.NORTH, true), 0, 6, 3, boundingBox);
         this.addBlock(structureWorldAccess, (BlockState)((BlockState)Blocks.NETHER_BRICK_FENCE.getDefaultState().with(FenceBlock.WEST, true)).with(FenceBlock.NORTH, true), 6, 6, 3, boundingBox);
         this.fillWithOutline(structureWorldAccess, boundingBox, 0, 6, 4, 0, 6, 7, blockState2, blockState2, false);
         this.fillWithOutline(structureWorldAccess, boundingBox, 6, 6, 4, 6, 6, 7, blockState2, blockState2, false);
         this.addBlock(structureWorldAccess, (BlockState)((BlockState)Blocks.NETHER_BRICK_FENCE.getDefaultState().with(FenceBlock.EAST, true)).with(FenceBlock.SOUTH, true), 0, 6, 8, boundingBox);
         this.addBlock(structureWorldAccess, (BlockState)((BlockState)Blocks.NETHER_BRICK_FENCE.getDefaultState().with(FenceBlock.WEST, true)).with(FenceBlock.SOUTH, true), 6, 6, 8, boundingBox);
         this.fillWithOutline(structureWorldAccess, boundingBox, 1, 6, 8, 5, 6, 8, blockState, blockState, false);
         this.addBlock(structureWorldAccess, (BlockState)Blocks.NETHER_BRICK_FENCE.getDefaultState().with(FenceBlock.EAST, true), 1, 7, 8, boundingBox);
         this.fillWithOutline(structureWorldAccess, boundingBox, 2, 7, 8, 4, 7, 8, blockState, blockState, false);
         this.addBlock(structureWorldAccess, (BlockState)Blocks.NETHER_BRICK_FENCE.getDefaultState().with(FenceBlock.WEST, true), 5, 7, 8, boundingBox);
         this.addBlock(structureWorldAccess, (BlockState)Blocks.NETHER_BRICK_FENCE.getDefaultState().with(FenceBlock.EAST, true), 2, 8, 8, boundingBox);
         this.addBlock(structureWorldAccess, blockState, 3, 8, 8, boundingBox);
         this.addBlock(structureWorldAccess, (BlockState)Blocks.NETHER_BRICK_FENCE.getDefaultState().with(FenceBlock.WEST, true), 4, 8, 8, boundingBox);
         if (!this.hasBlazeSpawner) {
            BlockPos blockPos2 = new BlockPos(this.applyXTransform(3, 5), this.applyYTransform(5), this.applyZTransform(3, 5));
            if (boundingBox.contains(blockPos2)) {
               this.hasBlazeSpawner = true;
               structureWorldAccess.setBlockState(blockPos2, Blocks.SPAWNER.getDefaultState(), 2);
               BlockEntity blockEntity = structureWorldAccess.getBlockEntity(blockPos2);
               if (blockEntity instanceof MobSpawnerBlockEntity) {
                  ((MobSpawnerBlockEntity)blockEntity).getLogic().setEntityId(EntityType.BLAZE);
               }
            }
         }

         for(int i = 0; i <= 6; ++i) {
            for(int j = 0; j <= 6; ++j) {
               this.fillDownwards(structureWorldAccess, Blocks.NETHER_BRICKS.getDefaultState(), i, -1, j, boundingBox);
            }
         }

         return true;
      }
   }

   public static class BridgeStairs extends NetherFortressGenerator.Piece {
      public BridgeStairs(int chainLength, BlockBox boundingBox, Direction orientation) {
         super(StructurePieceType.NETHER_FORTRESS_BRIDGE_STAIRS, chainLength);
         this.setOrientation(orientation);
         this.boundingBox = boundingBox;
      }

      public BridgeStairs(StructureManager structureManager, CompoundTag compoundTag) {
         super(StructurePieceType.NETHER_FORTRESS_BRIDGE_STAIRS, compoundTag);
      }

      public void fillOpenings(StructurePiece start, List<StructurePiece> pieces, Random random) {
         this.fillSEOpening((NetherFortressGenerator.Start)start, pieces, random, 6, 2, false);
      }

      public static NetherFortressGenerator.BridgeStairs create(List<StructurePiece> pieces, int x, int y, int z, int chainlength, Direction orientation) {
         BlockBox blockBox = BlockBox.rotated(x, y, z, -2, 0, 0, 7, 11, 7, orientation);
         return isInbounds(blockBox) && StructurePiece.getOverlappingPiece(pieces, blockBox) == null ? new NetherFortressGenerator.BridgeStairs(chainlength, blockBox, orientation) : null;
      }

      public boolean generate(StructureWorldAccess structureWorldAccess, StructureAccessor structureAccessor, ChunkGenerator chunkGenerator, Random random, BlockBox boundingBox, ChunkPos chunkPos, BlockPos blockPos) {
         this.fillWithOutline(structureWorldAccess, boundingBox, 0, 0, 0, 6, 1, 6, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
         this.fillWithOutline(structureWorldAccess, boundingBox, 0, 2, 0, 6, 10, 6, Blocks.AIR.getDefaultState(), Blocks.AIR.getDefaultState(), false);
         this.fillWithOutline(structureWorldAccess, boundingBox, 0, 2, 0, 1, 8, 0, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
         this.fillWithOutline(structureWorldAccess, boundingBox, 5, 2, 0, 6, 8, 0, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
         this.fillWithOutline(structureWorldAccess, boundingBox, 0, 2, 1, 0, 8, 6, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
         this.fillWithOutline(structureWorldAccess, boundingBox, 6, 2, 1, 6, 8, 6, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
         this.fillWithOutline(structureWorldAccess, boundingBox, 1, 2, 6, 5, 8, 6, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
         BlockState blockState = (BlockState)((BlockState)Blocks.NETHER_BRICK_FENCE.getDefaultState().with(FenceBlock.WEST, true)).with(FenceBlock.EAST, true);
         BlockState blockState2 = (BlockState)((BlockState)Blocks.NETHER_BRICK_FENCE.getDefaultState().with(FenceBlock.NORTH, true)).with(FenceBlock.SOUTH, true);
         this.fillWithOutline(structureWorldAccess, boundingBox, 0, 3, 2, 0, 5, 4, blockState2, blockState2, false);
         this.fillWithOutline(structureWorldAccess, boundingBox, 6, 3, 2, 6, 5, 2, blockState2, blockState2, false);
         this.fillWithOutline(structureWorldAccess, boundingBox, 6, 3, 4, 6, 5, 4, blockState2, blockState2, false);
         this.addBlock(structureWorldAccess, Blocks.NETHER_BRICKS.getDefaultState(), 5, 2, 5, boundingBox);
         this.fillWithOutline(structureWorldAccess, boundingBox, 4, 2, 5, 4, 3, 5, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
         this.fillWithOutline(structureWorldAccess, boundingBox, 3, 2, 5, 3, 4, 5, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
         this.fillWithOutline(structureWorldAccess, boundingBox, 2, 2, 5, 2, 5, 5, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
         this.fillWithOutline(structureWorldAccess, boundingBox, 1, 2, 5, 1, 6, 5, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
         this.fillWithOutline(structureWorldAccess, boundingBox, 1, 7, 1, 5, 7, 4, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
         this.fillWithOutline(structureWorldAccess, boundingBox, 6, 8, 2, 6, 8, 4, Blocks.AIR.getDefaultState(), Blocks.AIR.getDefaultState(), false);
         this.fillWithOutline(structureWorldAccess, boundingBox, 2, 6, 0, 4, 8, 0, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
         this.fillWithOutline(structureWorldAccess, boundingBox, 2, 5, 0, 4, 5, 0, blockState, blockState, false);

         for(int i = 0; i <= 6; ++i) {
            for(int j = 0; j <= 6; ++j) {
               this.fillDownwards(structureWorldAccess, Blocks.NETHER_BRICKS.getDefaultState(), i, -1, j, boundingBox);
            }
         }

         return true;
      }
   }

   public static class BridgeSmallCrossing extends NetherFortressGenerator.Piece {
      public BridgeSmallCrossing(int chainLength, BlockBox boundingBox, Direction orientation) {
         super(StructurePieceType.NETHER_FORTRESS_BRIDGE_SMALL_CROSSING, chainLength);
         this.setOrientation(orientation);
         this.boundingBox = boundingBox;
      }

      public BridgeSmallCrossing(StructureManager structureManager, CompoundTag compoundTag) {
         super(StructurePieceType.NETHER_FORTRESS_BRIDGE_SMALL_CROSSING, compoundTag);
      }

      public void fillOpenings(StructurePiece start, List<StructurePiece> pieces, Random random) {
         this.fillForwardOpening((NetherFortressGenerator.Start)start, pieces, random, 2, 0, false);
         this.fillNWOpening((NetherFortressGenerator.Start)start, pieces, random, 0, 2, false);
         this.fillSEOpening((NetherFortressGenerator.Start)start, pieces, random, 0, 2, false);
      }

      public static NetherFortressGenerator.BridgeSmallCrossing create(List<StructurePiece> pieces, int x, int y, int z, Direction orientation, int chainLength) {
         BlockBox blockBox = BlockBox.rotated(x, y, z, -2, 0, 0, 7, 9, 7, orientation);
         return isInbounds(blockBox) && StructurePiece.getOverlappingPiece(pieces, blockBox) == null ? new NetherFortressGenerator.BridgeSmallCrossing(chainLength, blockBox, orientation) : null;
      }

      public boolean generate(StructureWorldAccess structureWorldAccess, StructureAccessor structureAccessor, ChunkGenerator chunkGenerator, Random random, BlockBox boundingBox, ChunkPos chunkPos, BlockPos blockPos) {
         this.fillWithOutline(structureWorldAccess, boundingBox, 0, 0, 0, 6, 1, 6, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
         this.fillWithOutline(structureWorldAccess, boundingBox, 0, 2, 0, 6, 7, 6, Blocks.AIR.getDefaultState(), Blocks.AIR.getDefaultState(), false);
         this.fillWithOutline(structureWorldAccess, boundingBox, 0, 2, 0, 1, 6, 0, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
         this.fillWithOutline(structureWorldAccess, boundingBox, 0, 2, 6, 1, 6, 6, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
         this.fillWithOutline(structureWorldAccess, boundingBox, 5, 2, 0, 6, 6, 0, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
         this.fillWithOutline(structureWorldAccess, boundingBox, 5, 2, 6, 6, 6, 6, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
         this.fillWithOutline(structureWorldAccess, boundingBox, 0, 2, 0, 0, 6, 1, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
         this.fillWithOutline(structureWorldAccess, boundingBox, 0, 2, 5, 0, 6, 6, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
         this.fillWithOutline(structureWorldAccess, boundingBox, 6, 2, 0, 6, 6, 1, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
         this.fillWithOutline(structureWorldAccess, boundingBox, 6, 2, 5, 6, 6, 6, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
         BlockState blockState = (BlockState)((BlockState)Blocks.NETHER_BRICK_FENCE.getDefaultState().with(FenceBlock.WEST, true)).with(FenceBlock.EAST, true);
         BlockState blockState2 = (BlockState)((BlockState)Blocks.NETHER_BRICK_FENCE.getDefaultState().with(FenceBlock.NORTH, true)).with(FenceBlock.SOUTH, true);
         this.fillWithOutline(structureWorldAccess, boundingBox, 2, 6, 0, 4, 6, 0, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
         this.fillWithOutline(structureWorldAccess, boundingBox, 2, 5, 0, 4, 5, 0, blockState, blockState, false);
         this.fillWithOutline(structureWorldAccess, boundingBox, 2, 6, 6, 4, 6, 6, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
         this.fillWithOutline(structureWorldAccess, boundingBox, 2, 5, 6, 4, 5, 6, blockState, blockState, false);
         this.fillWithOutline(structureWorldAccess, boundingBox, 0, 6, 2, 0, 6, 4, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
         this.fillWithOutline(structureWorldAccess, boundingBox, 0, 5, 2, 0, 5, 4, blockState2, blockState2, false);
         this.fillWithOutline(structureWorldAccess, boundingBox, 6, 6, 2, 6, 6, 4, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
         this.fillWithOutline(structureWorldAccess, boundingBox, 6, 5, 2, 6, 5, 4, blockState2, blockState2, false);

         for(int i = 0; i <= 6; ++i) {
            for(int j = 0; j <= 6; ++j) {
               this.fillDownwards(structureWorldAccess, Blocks.NETHER_BRICKS.getDefaultState(), i, -1, j, boundingBox);
            }
         }

         return true;
      }
   }

   public static class BridgeCrossing extends NetherFortressGenerator.Piece {
      public BridgeCrossing(int chainLength, BlockBox boundingBox, Direction orientation) {
         super(StructurePieceType.NETHER_FORTRESS_BRIDGE_CROSSING, chainLength);
         this.setOrientation(orientation);
         this.boundingBox = boundingBox;
      }

      protected BridgeCrossing(Random random, int x, int z) {
         super(StructurePieceType.NETHER_FORTRESS_BRIDGE_CROSSING, 0);
         this.setOrientation(Direction.Type.HORIZONTAL.random(random));
         if (this.getFacing().getAxis() == Direction.Axis.Z) {
            this.boundingBox = new BlockBox(x, 64, z, x + 19 - 1, 73, z + 19 - 1);
         } else {
            this.boundingBox = new BlockBox(x, 64, z, x + 19 - 1, 73, z + 19 - 1);
         }

      }

      protected BridgeCrossing(StructurePieceType structurePieceType, CompoundTag compoundTag) {
         super(structurePieceType, compoundTag);
      }

      public BridgeCrossing(StructureManager structureManager, CompoundTag compoundTag) {
         this(StructurePieceType.NETHER_FORTRESS_BRIDGE_CROSSING, compoundTag);
      }

      public void fillOpenings(StructurePiece start, List<StructurePiece> pieces, Random random) {
         this.fillForwardOpening((NetherFortressGenerator.Start)start, pieces, random, 8, 3, false);
         this.fillNWOpening((NetherFortressGenerator.Start)start, pieces, random, 3, 8, false);
         this.fillSEOpening((NetherFortressGenerator.Start)start, pieces, random, 3, 8, false);
      }

      public static NetherFortressGenerator.BridgeCrossing create(List<StructurePiece> pieces, int x, int y, int z, Direction orientation, int chainLength) {
         BlockBox blockBox = BlockBox.rotated(x, y, z, -8, -3, 0, 19, 10, 19, orientation);
         return isInbounds(blockBox) && StructurePiece.getOverlappingPiece(pieces, blockBox) == null ? new NetherFortressGenerator.BridgeCrossing(chainLength, blockBox, orientation) : null;
      }

      public boolean generate(StructureWorldAccess structureWorldAccess, StructureAccessor structureAccessor, ChunkGenerator chunkGenerator, Random random, BlockBox boundingBox, ChunkPos chunkPos, BlockPos blockPos) {
         this.fillWithOutline(structureWorldAccess, boundingBox, 7, 3, 0, 11, 4, 18, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
         this.fillWithOutline(structureWorldAccess, boundingBox, 0, 3, 7, 18, 4, 11, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
         this.fillWithOutline(structureWorldAccess, boundingBox, 8, 5, 0, 10, 7, 18, Blocks.AIR.getDefaultState(), Blocks.AIR.getDefaultState(), false);
         this.fillWithOutline(structureWorldAccess, boundingBox, 0, 5, 8, 18, 7, 10, Blocks.AIR.getDefaultState(), Blocks.AIR.getDefaultState(), false);
         this.fillWithOutline(structureWorldAccess, boundingBox, 7, 5, 0, 7, 5, 7, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
         this.fillWithOutline(structureWorldAccess, boundingBox, 7, 5, 11, 7, 5, 18, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
         this.fillWithOutline(structureWorldAccess, boundingBox, 11, 5, 0, 11, 5, 7, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
         this.fillWithOutline(structureWorldAccess, boundingBox, 11, 5, 11, 11, 5, 18, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
         this.fillWithOutline(structureWorldAccess, boundingBox, 0, 5, 7, 7, 5, 7, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
         this.fillWithOutline(structureWorldAccess, boundingBox, 11, 5, 7, 18, 5, 7, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
         this.fillWithOutline(structureWorldAccess, boundingBox, 0, 5, 11, 7, 5, 11, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
         this.fillWithOutline(structureWorldAccess, boundingBox, 11, 5, 11, 18, 5, 11, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
         this.fillWithOutline(structureWorldAccess, boundingBox, 7, 2, 0, 11, 2, 5, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
         this.fillWithOutline(structureWorldAccess, boundingBox, 7, 2, 13, 11, 2, 18, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
         this.fillWithOutline(structureWorldAccess, boundingBox, 7, 0, 0, 11, 1, 3, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
         this.fillWithOutline(structureWorldAccess, boundingBox, 7, 0, 15, 11, 1, 18, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);

         int k;
         int l;
         for(k = 7; k <= 11; ++k) {
            for(l = 0; l <= 2; ++l) {
               this.fillDownwards(structureWorldAccess, Blocks.NETHER_BRICKS.getDefaultState(), k, -1, l, boundingBox);
               this.fillDownwards(structureWorldAccess, Blocks.NETHER_BRICKS.getDefaultState(), k, -1, 18 - l, boundingBox);
            }
         }

         this.fillWithOutline(structureWorldAccess, boundingBox, 0, 2, 7, 5, 2, 11, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
         this.fillWithOutline(structureWorldAccess, boundingBox, 13, 2, 7, 18, 2, 11, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
         this.fillWithOutline(structureWorldAccess, boundingBox, 0, 0, 7, 3, 1, 11, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
         this.fillWithOutline(structureWorldAccess, boundingBox, 15, 0, 7, 18, 1, 11, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);

         for(k = 0; k <= 2; ++k) {
            for(l = 7; l <= 11; ++l) {
               this.fillDownwards(structureWorldAccess, Blocks.NETHER_BRICKS.getDefaultState(), k, -1, l, boundingBox);
               this.fillDownwards(structureWorldAccess, Blocks.NETHER_BRICKS.getDefaultState(), 18 - k, -1, l, boundingBox);
            }
         }

         return true;
      }
   }

   public static class BridgeEnd extends NetherFortressGenerator.Piece {
      private final int seed;

      public BridgeEnd(int chainLength, Random random, BlockBox boundingBox, Direction orientation) {
         super(StructurePieceType.NETHER_FORTRESS_BRIDGE_END, chainLength);
         this.setOrientation(orientation);
         this.boundingBox = boundingBox;
         this.seed = random.nextInt();
      }

      public BridgeEnd(StructureManager structureManager, CompoundTag compoundTag) {
         super(StructurePieceType.NETHER_FORTRESS_BRIDGE_END, compoundTag);
         this.seed = compoundTag.getInt("Seed");
      }

      public static NetherFortressGenerator.BridgeEnd create(List<StructurePiece> pieces, Random random, int x, int y, int z, Direction orientation, int chainLength) {
         BlockBox blockBox = BlockBox.rotated(x, y, z, -1, -3, 0, 5, 10, 8, orientation);
         return isInbounds(blockBox) && StructurePiece.getOverlappingPiece(pieces, blockBox) == null ? new NetherFortressGenerator.BridgeEnd(chainLength, random, blockBox, orientation) : null;
      }

      protected void toNbt(CompoundTag tag) {
         super.toNbt(tag);
         tag.putInt("Seed", this.seed);
      }

      public boolean generate(StructureWorldAccess structureWorldAccess, StructureAccessor structureAccessor, ChunkGenerator chunkGenerator, Random random, BlockBox boundingBox, ChunkPos chunkPos, BlockPos blockPos) {
         Random random2 = new Random((long)this.seed);

         int p;
         int q;
         int r;
         for(p = 0; p <= 4; ++p) {
            for(q = 3; q <= 4; ++q) {
               r = random2.nextInt(8);
               this.fillWithOutline(structureWorldAccess, boundingBox, p, q, 0, p, q, r, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
            }
         }

         p = random2.nextInt(8);
         this.fillWithOutline(structureWorldAccess, boundingBox, 0, 5, 0, 0, 5, p, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
         p = random2.nextInt(8);
         this.fillWithOutline(structureWorldAccess, boundingBox, 4, 5, 0, 4, 5, p, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);

         for(p = 0; p <= 4; ++p) {
            q = random2.nextInt(5);
            this.fillWithOutline(structureWorldAccess, boundingBox, p, 2, 0, p, 2, q, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
         }

         for(p = 0; p <= 4; ++p) {
            for(q = 0; q <= 1; ++q) {
               r = random2.nextInt(3);
               this.fillWithOutline(structureWorldAccess, boundingBox, p, q, 0, p, q, r, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
            }
         }

         return true;
      }
   }

   public static class Bridge extends NetherFortressGenerator.Piece {
      public Bridge(int chainLength, Random random, BlockBox boundingBox, Direction orientation) {
         super(StructurePieceType.NETHER_FORTRESS_BRIDGE, chainLength);
         this.setOrientation(orientation);
         this.boundingBox = boundingBox;
      }

      public Bridge(StructureManager structureManager, CompoundTag compoundTag) {
         super(StructurePieceType.NETHER_FORTRESS_BRIDGE, compoundTag);
      }

      public void fillOpenings(StructurePiece start, List<StructurePiece> pieces, Random random) {
         this.fillForwardOpening((NetherFortressGenerator.Start)start, pieces, random, 1, 3, false);
      }

      public static NetherFortressGenerator.Bridge create(List<StructurePiece> pieces, Random random, int x, int y, int z, Direction orientation, int chainLength) {
         BlockBox blockBox = BlockBox.rotated(x, y, z, -1, -3, 0, 5, 10, 19, orientation);
         return isInbounds(blockBox) && StructurePiece.getOverlappingPiece(pieces, blockBox) == null ? new NetherFortressGenerator.Bridge(chainLength, random, blockBox, orientation) : null;
      }

      public boolean generate(StructureWorldAccess structureWorldAccess, StructureAccessor structureAccessor, ChunkGenerator chunkGenerator, Random random, BlockBox boundingBox, ChunkPos chunkPos, BlockPos blockPos) {
         this.fillWithOutline(structureWorldAccess, boundingBox, 0, 3, 0, 4, 4, 18, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
         this.fillWithOutline(structureWorldAccess, boundingBox, 1, 5, 0, 3, 7, 18, Blocks.AIR.getDefaultState(), Blocks.AIR.getDefaultState(), false);
         this.fillWithOutline(structureWorldAccess, boundingBox, 0, 5, 0, 0, 5, 18, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
         this.fillWithOutline(structureWorldAccess, boundingBox, 4, 5, 0, 4, 5, 18, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
         this.fillWithOutline(structureWorldAccess, boundingBox, 0, 2, 0, 4, 2, 5, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
         this.fillWithOutline(structureWorldAccess, boundingBox, 0, 2, 13, 4, 2, 18, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
         this.fillWithOutline(structureWorldAccess, boundingBox, 0, 0, 0, 4, 1, 3, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
         this.fillWithOutline(structureWorldAccess, boundingBox, 0, 0, 15, 4, 1, 18, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);

         for(int i = 0; i <= 4; ++i) {
            for(int j = 0; j <= 2; ++j) {
               this.fillDownwards(structureWorldAccess, Blocks.NETHER_BRICKS.getDefaultState(), i, -1, j, boundingBox);
               this.fillDownwards(structureWorldAccess, Blocks.NETHER_BRICKS.getDefaultState(), i, -1, 18 - j, boundingBox);
            }
         }

         BlockState blockState = (BlockState)((BlockState)Blocks.NETHER_BRICK_FENCE.getDefaultState().with(FenceBlock.NORTH, true)).with(FenceBlock.SOUTH, true);
         BlockState blockState2 = (BlockState)blockState.with(FenceBlock.EAST, true);
         BlockState blockState3 = (BlockState)blockState.with(FenceBlock.WEST, true);
         this.fillWithOutline(structureWorldAccess, boundingBox, 0, 1, 1, 0, 4, 1, blockState2, blockState2, false);
         this.fillWithOutline(structureWorldAccess, boundingBox, 0, 3, 4, 0, 4, 4, blockState2, blockState2, false);
         this.fillWithOutline(structureWorldAccess, boundingBox, 0, 3, 14, 0, 4, 14, blockState2, blockState2, false);
         this.fillWithOutline(structureWorldAccess, boundingBox, 0, 1, 17, 0, 4, 17, blockState2, blockState2, false);
         this.fillWithOutline(structureWorldAccess, boundingBox, 4, 1, 1, 4, 4, 1, blockState3, blockState3, false);
         this.fillWithOutline(structureWorldAccess, boundingBox, 4, 3, 4, 4, 4, 4, blockState3, blockState3, false);
         this.fillWithOutline(structureWorldAccess, boundingBox, 4, 3, 14, 4, 4, 14, blockState3, blockState3, false);
         this.fillWithOutline(structureWorldAccess, boundingBox, 4, 1, 17, 4, 4, 17, blockState3, blockState3, false);
         return true;
      }
   }

   public static class Start extends NetherFortressGenerator.BridgeCrossing {
      public NetherFortressGenerator.PieceData lastPiece;
      public List<NetherFortressGenerator.PieceData> bridgePieces;
      public List<NetherFortressGenerator.PieceData> corridorPieces;
      public final List<StructurePiece> pieces = Lists.newArrayList();

      public Start(Random random, int i, int j) {
         super(random, i, j);
         this.bridgePieces = Lists.newArrayList();
         NetherFortressGenerator.PieceData[] var4 = NetherFortressGenerator.ALL_BRIDGE_PIECES;
         int var5 = var4.length;

         int var6;
         NetherFortressGenerator.PieceData pieceData2;
         for(var6 = 0; var6 < var5; ++var6) {
            pieceData2 = var4[var6];
            pieceData2.generatedCount = 0;
            this.bridgePieces.add(pieceData2);
         }

         this.corridorPieces = Lists.newArrayList();
         var4 = NetherFortressGenerator.ALL_CORRIDOR_PIECES;
         var5 = var4.length;

         for(var6 = 0; var6 < var5; ++var6) {
            pieceData2 = var4[var6];
            pieceData2.generatedCount = 0;
            this.corridorPieces.add(pieceData2);
         }

      }

      public Start(StructureManager structureManager, CompoundTag compoundTag) {
         super(StructurePieceType.NETHER_FORTRESS_START, compoundTag);
      }
   }

   abstract static class Piece extends StructurePiece {
      protected Piece(StructurePieceType structurePieceType, int i) {
         super(structurePieceType, i);
      }

      public Piece(StructurePieceType structurePieceType, CompoundTag compoundTag) {
         super(structurePieceType, compoundTag);
      }

      protected void toNbt(CompoundTag tag) {
      }

      private int checkRemainingPieces(List<NetherFortressGenerator.PieceData> possiblePieces) {
         boolean bl = false;
         int i = 0;

         NetherFortressGenerator.PieceData pieceData;
         for(Iterator var4 = possiblePieces.iterator(); var4.hasNext(); i += pieceData.weight) {
            pieceData = (NetherFortressGenerator.PieceData)var4.next();
            if (pieceData.limit > 0 && pieceData.generatedCount < pieceData.limit) {
               bl = true;
            }
         }

         return bl ? i : -1;
      }

      private NetherFortressGenerator.Piece pickPiece(NetherFortressGenerator.Start start, List<NetherFortressGenerator.PieceData> possiblePieces, List<StructurePiece> pieces, Random random, int x, int y, int z, Direction orientation, int chainLength) {
         int i = this.checkRemainingPieces(possiblePieces);
         boolean bl = i > 0 && chainLength <= 30;
         int j = 0;

         while(j < 5 && bl) {
            ++j;
            int k = random.nextInt(i);
            Iterator var14 = possiblePieces.iterator();

            while(var14.hasNext()) {
               NetherFortressGenerator.PieceData pieceData = (NetherFortressGenerator.PieceData)var14.next();
               k -= pieceData.weight;
               if (k < 0) {
                  if (!pieceData.canGenerate(chainLength) || pieceData == start.lastPiece && !pieceData.repeatable) {
                     break;
                  }

                  NetherFortressGenerator.Piece piece = NetherFortressGenerator.createPiece(pieceData, pieces, random, x, y, z, orientation, chainLength);
                  if (piece != null) {
                     ++pieceData.generatedCount;
                     start.lastPiece = pieceData;
                     if (!pieceData.canGenerate()) {
                        possiblePieces.remove(pieceData);
                     }

                     return piece;
                  }
               }
            }
         }

         return NetherFortressGenerator.BridgeEnd.create(pieces, random, x, y, z, orientation, chainLength);
      }

      private StructurePiece pieceGenerator(NetherFortressGenerator.Start start, List<StructurePiece> pieces, Random random, int x, int y, int z, @Nullable Direction orientation, int chainLength, boolean inside) {
         if (Math.abs(x - start.getBoundingBox().minX) <= 112 && Math.abs(z - start.getBoundingBox().minZ) <= 112) {
            List<NetherFortressGenerator.PieceData> list = start.bridgePieces;
            if (inside) {
               list = start.corridorPieces;
            }

            StructurePiece structurePiece = this.pickPiece(start, list, pieces, random, x, y, z, orientation, chainLength + 1);
            if (structurePiece != null) {
               pieces.add(structurePiece);
               start.pieces.add(structurePiece);
            }

            return structurePiece;
         } else {
            return NetherFortressGenerator.BridgeEnd.create(pieces, random, x, y, z, orientation, chainLength);
         }
      }

      @Nullable
      protected StructurePiece fillForwardOpening(NetherFortressGenerator.Start start, List<StructurePiece> pieces, Random random, int leftRightOffset, int heightOffset, boolean inside) {
         Direction direction = this.getFacing();
         if (direction != null) {
            switch(direction) {
            case NORTH:
               return this.pieceGenerator(start, pieces, random, this.boundingBox.minX + leftRightOffset, this.boundingBox.minY + heightOffset, this.boundingBox.minZ - 1, direction, this.getChainLength(), inside);
            case SOUTH:
               return this.pieceGenerator(start, pieces, random, this.boundingBox.minX + leftRightOffset, this.boundingBox.minY + heightOffset, this.boundingBox.maxZ + 1, direction, this.getChainLength(), inside);
            case WEST:
               return this.pieceGenerator(start, pieces, random, this.boundingBox.minX - 1, this.boundingBox.minY + heightOffset, this.boundingBox.minZ + leftRightOffset, direction, this.getChainLength(), inside);
            case EAST:
               return this.pieceGenerator(start, pieces, random, this.boundingBox.maxX + 1, this.boundingBox.minY + heightOffset, this.boundingBox.minZ + leftRightOffset, direction, this.getChainLength(), inside);
            }
         }

         return null;
      }

      @Nullable
      protected StructurePiece fillNWOpening(NetherFortressGenerator.Start start, List<StructurePiece> pieces, Random random, int heightOffset, int leftRightOffset, boolean inside) {
         Direction direction = this.getFacing();
         if (direction != null) {
            switch(direction) {
            case NORTH:
               return this.pieceGenerator(start, pieces, random, this.boundingBox.minX - 1, this.boundingBox.minY + heightOffset, this.boundingBox.minZ + leftRightOffset, Direction.WEST, this.getChainLength(), inside);
            case SOUTH:
               return this.pieceGenerator(start, pieces, random, this.boundingBox.minX - 1, this.boundingBox.minY + heightOffset, this.boundingBox.minZ + leftRightOffset, Direction.WEST, this.getChainLength(), inside);
            case WEST:
               return this.pieceGenerator(start, pieces, random, this.boundingBox.minX + leftRightOffset, this.boundingBox.minY + heightOffset, this.boundingBox.minZ - 1, Direction.NORTH, this.getChainLength(), inside);
            case EAST:
               return this.pieceGenerator(start, pieces, random, this.boundingBox.minX + leftRightOffset, this.boundingBox.minY + heightOffset, this.boundingBox.minZ - 1, Direction.NORTH, this.getChainLength(), inside);
            }
         }

         return null;
      }

      @Nullable
      protected StructurePiece fillSEOpening(NetherFortressGenerator.Start start, List<StructurePiece> pieces, Random random, int heightOffset, int leftRightOffset, boolean inside) {
         Direction direction = this.getFacing();
         if (direction != null) {
            switch(direction) {
            case NORTH:
               return this.pieceGenerator(start, pieces, random, this.boundingBox.maxX + 1, this.boundingBox.minY + heightOffset, this.boundingBox.minZ + leftRightOffset, Direction.EAST, this.getChainLength(), inside);
            case SOUTH:
               return this.pieceGenerator(start, pieces, random, this.boundingBox.maxX + 1, this.boundingBox.minY + heightOffset, this.boundingBox.minZ + leftRightOffset, Direction.EAST, this.getChainLength(), inside);
            case WEST:
               return this.pieceGenerator(start, pieces, random, this.boundingBox.minX + leftRightOffset, this.boundingBox.minY + heightOffset, this.boundingBox.maxZ + 1, Direction.SOUTH, this.getChainLength(), inside);
            case EAST:
               return this.pieceGenerator(start, pieces, random, this.boundingBox.minX + leftRightOffset, this.boundingBox.minY + heightOffset, this.boundingBox.maxZ + 1, Direction.SOUTH, this.getChainLength(), inside);
            }
         }

         return null;
      }

      protected static boolean isInbounds(BlockBox boundingBox) {
         return boundingBox != null && boundingBox.minY > 10;
      }
   }

   static class PieceData {
      public final Class<? extends NetherFortressGenerator.Piece> pieceType;
      public final int weight;
      public int generatedCount;
      public final int limit;
      public final boolean repeatable;

      public PieceData(Class<? extends NetherFortressGenerator.Piece> pieceType, int weight, int limit, boolean repeatable) {
         this.pieceType = pieceType;
         this.weight = weight;
         this.limit = limit;
         this.repeatable = repeatable;
      }

      public PieceData(Class<? extends NetherFortressGenerator.Piece> pieceType, int weight, int limit) {
         this(pieceType, weight, limit, false);
      }

      public boolean canGenerate(int chainLength) {
         return this.limit == 0 || this.generatedCount < this.limit;
      }

      public boolean canGenerate() {
         return this.limit == 0 || this.generatedCount < this.limit;
      }
   }
}
