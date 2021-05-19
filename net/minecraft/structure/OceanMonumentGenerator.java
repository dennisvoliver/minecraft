package net.minecraft.structure;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityData;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.mob.ElderGuardianEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.chunk.ChunkGenerator;

public class OceanMonumentGenerator {
   static class DoubleYZRoomFactory implements OceanMonumentGenerator.PieceFactory {
      private DoubleYZRoomFactory() {
      }

      public boolean canGenerate(OceanMonumentGenerator.PieceSetting setting) {
         if (setting.neighborPresences[Direction.NORTH.getId()] && !setting.neighbors[Direction.NORTH.getId()].used && setting.neighborPresences[Direction.UP.getId()] && !setting.neighbors[Direction.UP.getId()].used) {
            OceanMonumentGenerator.PieceSetting pieceSetting = setting.neighbors[Direction.NORTH.getId()];
            return pieceSetting.neighborPresences[Direction.UP.getId()] && !pieceSetting.neighbors[Direction.UP.getId()].used;
         } else {
            return false;
         }
      }

      public OceanMonumentGenerator.Piece generate(Direction direction, OceanMonumentGenerator.PieceSetting setting, Random random) {
         setting.used = true;
         setting.neighbors[Direction.NORTH.getId()].used = true;
         setting.neighbors[Direction.UP.getId()].used = true;
         setting.neighbors[Direction.NORTH.getId()].neighbors[Direction.UP.getId()].used = true;
         return new OceanMonumentGenerator.DoubleYZRoom(direction, setting);
      }
   }

   static class DoubleXYRoomFactory implements OceanMonumentGenerator.PieceFactory {
      private DoubleXYRoomFactory() {
      }

      public boolean canGenerate(OceanMonumentGenerator.PieceSetting setting) {
         if (setting.neighborPresences[Direction.EAST.getId()] && !setting.neighbors[Direction.EAST.getId()].used && setting.neighborPresences[Direction.UP.getId()] && !setting.neighbors[Direction.UP.getId()].used) {
            OceanMonumentGenerator.PieceSetting pieceSetting = setting.neighbors[Direction.EAST.getId()];
            return pieceSetting.neighborPresences[Direction.UP.getId()] && !pieceSetting.neighbors[Direction.UP.getId()].used;
         } else {
            return false;
         }
      }

      public OceanMonumentGenerator.Piece generate(Direction direction, OceanMonumentGenerator.PieceSetting setting, Random random) {
         setting.used = true;
         setting.neighbors[Direction.EAST.getId()].used = true;
         setting.neighbors[Direction.UP.getId()].used = true;
         setting.neighbors[Direction.EAST.getId()].neighbors[Direction.UP.getId()].used = true;
         return new OceanMonumentGenerator.DoubleXYRoom(direction, setting);
      }
   }

   static class DoubleZRoomFactory implements OceanMonumentGenerator.PieceFactory {
      private DoubleZRoomFactory() {
      }

      public boolean canGenerate(OceanMonumentGenerator.PieceSetting setting) {
         return setting.neighborPresences[Direction.NORTH.getId()] && !setting.neighbors[Direction.NORTH.getId()].used;
      }

      public OceanMonumentGenerator.Piece generate(Direction direction, OceanMonumentGenerator.PieceSetting setting, Random random) {
         OceanMonumentGenerator.PieceSetting pieceSetting = setting;
         if (!setting.neighborPresences[Direction.NORTH.getId()] || setting.neighbors[Direction.NORTH.getId()].used) {
            pieceSetting = setting.neighbors[Direction.SOUTH.getId()];
         }

         pieceSetting.used = true;
         pieceSetting.neighbors[Direction.NORTH.getId()].used = true;
         return new OceanMonumentGenerator.DoubleZRoom(direction, pieceSetting);
      }
   }

   static class DoubleXRoomFactory implements OceanMonumentGenerator.PieceFactory {
      private DoubleXRoomFactory() {
      }

      public boolean canGenerate(OceanMonumentGenerator.PieceSetting setting) {
         return setting.neighborPresences[Direction.EAST.getId()] && !setting.neighbors[Direction.EAST.getId()].used;
      }

      public OceanMonumentGenerator.Piece generate(Direction direction, OceanMonumentGenerator.PieceSetting setting, Random random) {
         setting.used = true;
         setting.neighbors[Direction.EAST.getId()].used = true;
         return new OceanMonumentGenerator.DoubleXRoom(direction, setting);
      }
   }

   static class DoubleYRoomFactory implements OceanMonumentGenerator.PieceFactory {
      private DoubleYRoomFactory() {
      }

      public boolean canGenerate(OceanMonumentGenerator.PieceSetting setting) {
         return setting.neighborPresences[Direction.UP.getId()] && !setting.neighbors[Direction.UP.getId()].used;
      }

      public OceanMonumentGenerator.Piece generate(Direction direction, OceanMonumentGenerator.PieceSetting setting, Random random) {
         setting.used = true;
         setting.neighbors[Direction.UP.getId()].used = true;
         return new OceanMonumentGenerator.DoubleYRoom(direction, setting);
      }
   }

   static class SimpleRoomTopFactory implements OceanMonumentGenerator.PieceFactory {
      private SimpleRoomTopFactory() {
      }

      public boolean canGenerate(OceanMonumentGenerator.PieceSetting setting) {
         return !setting.neighborPresences[Direction.WEST.getId()] && !setting.neighborPresences[Direction.EAST.getId()] && !setting.neighborPresences[Direction.NORTH.getId()] && !setting.neighborPresences[Direction.SOUTH.getId()] && !setting.neighborPresences[Direction.UP.getId()];
      }

      public OceanMonumentGenerator.Piece generate(Direction direction, OceanMonumentGenerator.PieceSetting setting, Random random) {
         setting.used = true;
         return new OceanMonumentGenerator.SimpleRoomTop(direction, setting);
      }
   }

   static class SimpleRoomFactory implements OceanMonumentGenerator.PieceFactory {
      private SimpleRoomFactory() {
      }

      public boolean canGenerate(OceanMonumentGenerator.PieceSetting setting) {
         return true;
      }

      public OceanMonumentGenerator.Piece generate(Direction direction, OceanMonumentGenerator.PieceSetting setting, Random random) {
         setting.used = true;
         return new OceanMonumentGenerator.SimpleRoom(direction, setting, random);
      }
   }

   interface PieceFactory {
      boolean canGenerate(OceanMonumentGenerator.PieceSetting setting);

      OceanMonumentGenerator.Piece generate(Direction direction, OceanMonumentGenerator.PieceSetting setting, Random random);
   }

   static class PieceSetting {
      private final int roomIndex;
      private final OceanMonumentGenerator.PieceSetting[] neighbors = new OceanMonumentGenerator.PieceSetting[6];
      private final boolean[] neighborPresences = new boolean[6];
      private boolean used;
      private boolean field_14484;
      private int field_14483;

      public PieceSetting(int index) {
         this.roomIndex = index;
      }

      public void setNeighbor(Direction direction, OceanMonumentGenerator.PieceSetting pieceSetting) {
         this.neighbors[direction.getId()] = pieceSetting;
         pieceSetting.neighbors[direction.getOpposite().getId()] = this;
      }

      public void checkNeighborStates() {
         for(int i = 0; i < 6; ++i) {
            this.neighborPresences[i] = this.neighbors[i] != null;
         }

      }

      public boolean method_14783(int i) {
         if (this.field_14484) {
            return true;
         } else {
            this.field_14483 = i;

            for(int j = 0; j < 6; ++j) {
               if (this.neighbors[j] != null && this.neighborPresences[j] && this.neighbors[j].field_14483 != i && this.neighbors[j].method_14783(i)) {
                  return true;
               }
            }

            return false;
         }
      }

      public boolean isAboveLevelThree() {
         return this.roomIndex >= 75;
      }

      public int countNeighbors() {
         int i = 0;

         for(int j = 0; j < 6; ++j) {
            if (this.neighborPresences[j]) {
               ++i;
            }
         }

         return i;
      }
   }

   public static class Penthouse extends OceanMonumentGenerator.Piece {
      public Penthouse(Direction direction, BlockBox blockBox) {
         super(StructurePieceType.OCEAN_MONUMENT_PENTHOUSE, direction, blockBox);
      }

      public Penthouse(StructureManager structureManager, CompoundTag compoundTag) {
         super(StructurePieceType.OCEAN_MONUMENT_PENTHOUSE, compoundTag);
      }

      public boolean generate(StructureWorldAccess structureWorldAccess, StructureAccessor structureAccessor, ChunkGenerator chunkGenerator, Random random, BlockBox boundingBox, ChunkPos chunkPos, BlockPos blockPos) {
         this.fillWithOutline(structureWorldAccess, boundingBox, 2, -1, 2, 11, -1, 11, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
         this.fillWithOutline(structureWorldAccess, boundingBox, 0, -1, 0, 1, -1, 11, PRISMARINE, PRISMARINE, false);
         this.fillWithOutline(structureWorldAccess, boundingBox, 12, -1, 0, 13, -1, 11, PRISMARINE, PRISMARINE, false);
         this.fillWithOutline(structureWorldAccess, boundingBox, 2, -1, 0, 11, -1, 1, PRISMARINE, PRISMARINE, false);
         this.fillWithOutline(structureWorldAccess, boundingBox, 2, -1, 12, 11, -1, 13, PRISMARINE, PRISMARINE, false);
         this.fillWithOutline(structureWorldAccess, boundingBox, 0, 0, 0, 0, 0, 13, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
         this.fillWithOutline(structureWorldAccess, boundingBox, 13, 0, 0, 13, 0, 13, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
         this.fillWithOutline(structureWorldAccess, boundingBox, 1, 0, 0, 12, 0, 0, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
         this.fillWithOutline(structureWorldAccess, boundingBox, 1, 0, 13, 12, 0, 13, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);

         for(int i = 2; i <= 11; i += 3) {
            this.addBlock(structureWorldAccess, SEA_LANTERN, 0, 0, i, boundingBox);
            this.addBlock(structureWorldAccess, SEA_LANTERN, 13, 0, i, boundingBox);
            this.addBlock(structureWorldAccess, SEA_LANTERN, i, 0, 0, boundingBox);
         }

         this.fillWithOutline(structureWorldAccess, boundingBox, 2, 0, 3, 4, 0, 9, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
         this.fillWithOutline(structureWorldAccess, boundingBox, 9, 0, 3, 11, 0, 9, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
         this.fillWithOutline(structureWorldAccess, boundingBox, 4, 0, 9, 9, 0, 11, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
         this.addBlock(structureWorldAccess, PRISMARINE_BRICKS, 5, 0, 8, boundingBox);
         this.addBlock(structureWorldAccess, PRISMARINE_BRICKS, 8, 0, 8, boundingBox);
         this.addBlock(structureWorldAccess, PRISMARINE_BRICKS, 10, 0, 10, boundingBox);
         this.addBlock(structureWorldAccess, PRISMARINE_BRICKS, 3, 0, 10, boundingBox);
         this.fillWithOutline(structureWorldAccess, boundingBox, 3, 0, 3, 3, 0, 7, DARK_PRISMARINE, DARK_PRISMARINE, false);
         this.fillWithOutline(structureWorldAccess, boundingBox, 10, 0, 3, 10, 0, 7, DARK_PRISMARINE, DARK_PRISMARINE, false);
         this.fillWithOutline(structureWorldAccess, boundingBox, 6, 0, 10, 7, 0, 10, DARK_PRISMARINE, DARK_PRISMARINE, false);
         int j = 3;

         for(int k = 0; k < 2; ++k) {
            for(int l = 2; l <= 8; l += 3) {
               this.fillWithOutline(structureWorldAccess, boundingBox, j, 0, l, j, 2, l, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            }

            j = 10;
         }

         this.fillWithOutline(structureWorldAccess, boundingBox, 5, 0, 10, 5, 2, 10, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
         this.fillWithOutline(structureWorldAccess, boundingBox, 8, 0, 10, 8, 2, 10, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
         this.fillWithOutline(structureWorldAccess, boundingBox, 6, -1, 7, 7, -1, 8, DARK_PRISMARINE, DARK_PRISMARINE, false);
         this.setAirAndWater(structureWorldAccess, boundingBox, 6, -1, 3, 7, -1, 4);
         this.method_14772(structureWorldAccess, boundingBox, 6, 1, 6);
         return true;
      }
   }

   public static class WingRoom extends OceanMonumentGenerator.Piece {
      private int field_14481;

      public WingRoom(Direction direction, BlockBox blockBox, int i) {
         super(StructurePieceType.OCEAN_MONUMENT_WING_ROOM, direction, blockBox);
         this.field_14481 = i & 1;
      }

      public WingRoom(StructureManager structureManager, CompoundTag compoundTag) {
         super(StructurePieceType.OCEAN_MONUMENT_WING_ROOM, compoundTag);
      }

      public boolean generate(StructureWorldAccess structureWorldAccess, StructureAccessor structureAccessor, ChunkGenerator chunkGenerator, Random random, BlockBox boundingBox, ChunkPos chunkPos, BlockPos blockPos) {
         if (this.field_14481 == 0) {
            int j;
            for(j = 0; j < 4; ++j) {
               this.fillWithOutline(structureWorldAccess, boundingBox, 10 - j, 3 - j, 20 - j, 12 + j, 3 - j, 20, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            }

            this.fillWithOutline(structureWorldAccess, boundingBox, 7, 0, 6, 15, 0, 16, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            this.fillWithOutline(structureWorldAccess, boundingBox, 6, 0, 6, 6, 3, 20, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            this.fillWithOutline(structureWorldAccess, boundingBox, 16, 0, 6, 16, 3, 20, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            this.fillWithOutline(structureWorldAccess, boundingBox, 7, 1, 7, 7, 1, 20, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            this.fillWithOutline(structureWorldAccess, boundingBox, 15, 1, 7, 15, 1, 20, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            this.fillWithOutline(structureWorldAccess, boundingBox, 7, 1, 6, 9, 3, 6, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            this.fillWithOutline(structureWorldAccess, boundingBox, 13, 1, 6, 15, 3, 6, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            this.fillWithOutline(structureWorldAccess, boundingBox, 8, 1, 7, 9, 1, 7, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            this.fillWithOutline(structureWorldAccess, boundingBox, 13, 1, 7, 14, 1, 7, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            this.fillWithOutline(structureWorldAccess, boundingBox, 9, 0, 5, 13, 0, 5, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            this.fillWithOutline(structureWorldAccess, boundingBox, 10, 0, 7, 12, 0, 7, DARK_PRISMARINE, DARK_PRISMARINE, false);
            this.fillWithOutline(structureWorldAccess, boundingBox, 8, 0, 10, 8, 0, 12, DARK_PRISMARINE, DARK_PRISMARINE, false);
            this.fillWithOutline(structureWorldAccess, boundingBox, 14, 0, 10, 14, 0, 12, DARK_PRISMARINE, DARK_PRISMARINE, false);

            for(j = 18; j >= 7; j -= 3) {
               this.addBlock(structureWorldAccess, SEA_LANTERN, 6, 3, j, boundingBox);
               this.addBlock(structureWorldAccess, SEA_LANTERN, 16, 3, j, boundingBox);
            }

            this.addBlock(structureWorldAccess, SEA_LANTERN, 10, 0, 10, boundingBox);
            this.addBlock(structureWorldAccess, SEA_LANTERN, 12, 0, 10, boundingBox);
            this.addBlock(structureWorldAccess, SEA_LANTERN, 10, 0, 12, boundingBox);
            this.addBlock(structureWorldAccess, SEA_LANTERN, 12, 0, 12, boundingBox);
            this.addBlock(structureWorldAccess, SEA_LANTERN, 8, 3, 6, boundingBox);
            this.addBlock(structureWorldAccess, SEA_LANTERN, 14, 3, 6, boundingBox);
            this.addBlock(structureWorldAccess, PRISMARINE_BRICKS, 4, 2, 4, boundingBox);
            this.addBlock(structureWorldAccess, SEA_LANTERN, 4, 1, 4, boundingBox);
            this.addBlock(structureWorldAccess, PRISMARINE_BRICKS, 4, 0, 4, boundingBox);
            this.addBlock(structureWorldAccess, PRISMARINE_BRICKS, 18, 2, 4, boundingBox);
            this.addBlock(structureWorldAccess, SEA_LANTERN, 18, 1, 4, boundingBox);
            this.addBlock(structureWorldAccess, PRISMARINE_BRICKS, 18, 0, 4, boundingBox);
            this.addBlock(structureWorldAccess, PRISMARINE_BRICKS, 4, 2, 18, boundingBox);
            this.addBlock(structureWorldAccess, SEA_LANTERN, 4, 1, 18, boundingBox);
            this.addBlock(structureWorldAccess, PRISMARINE_BRICKS, 4, 0, 18, boundingBox);
            this.addBlock(structureWorldAccess, PRISMARINE_BRICKS, 18, 2, 18, boundingBox);
            this.addBlock(structureWorldAccess, SEA_LANTERN, 18, 1, 18, boundingBox);
            this.addBlock(structureWorldAccess, PRISMARINE_BRICKS, 18, 0, 18, boundingBox);
            this.addBlock(structureWorldAccess, PRISMARINE_BRICKS, 9, 7, 20, boundingBox);
            this.addBlock(structureWorldAccess, PRISMARINE_BRICKS, 13, 7, 20, boundingBox);
            this.fillWithOutline(structureWorldAccess, boundingBox, 6, 0, 21, 7, 4, 21, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            this.fillWithOutline(structureWorldAccess, boundingBox, 15, 0, 21, 16, 4, 21, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            this.method_14772(structureWorldAccess, boundingBox, 11, 2, 16);
         } else if (this.field_14481 == 1) {
            this.fillWithOutline(structureWorldAccess, boundingBox, 9, 3, 18, 13, 3, 20, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            this.fillWithOutline(structureWorldAccess, boundingBox, 9, 0, 18, 9, 2, 18, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            this.fillWithOutline(structureWorldAccess, boundingBox, 13, 0, 18, 13, 2, 18, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            int k = 9;
            int l = true;
            int m = true;

            int p;
            for(p = 0; p < 2; ++p) {
               this.addBlock(structureWorldAccess, PRISMARINE_BRICKS, k, 6, 20, boundingBox);
               this.addBlock(structureWorldAccess, SEA_LANTERN, k, 5, 20, boundingBox);
               this.addBlock(structureWorldAccess, PRISMARINE_BRICKS, k, 4, 20, boundingBox);
               k = 13;
            }

            this.fillWithOutline(structureWorldAccess, boundingBox, 7, 3, 7, 15, 3, 14, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            k = 10;

            for(p = 0; p < 2; ++p) {
               this.fillWithOutline(structureWorldAccess, boundingBox, k, 0, 10, k, 6, 10, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
               this.fillWithOutline(structureWorldAccess, boundingBox, k, 0, 12, k, 6, 12, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
               this.addBlock(structureWorldAccess, SEA_LANTERN, k, 0, 10, boundingBox);
               this.addBlock(structureWorldAccess, SEA_LANTERN, k, 0, 12, boundingBox);
               this.addBlock(structureWorldAccess, SEA_LANTERN, k, 4, 10, boundingBox);
               this.addBlock(structureWorldAccess, SEA_LANTERN, k, 4, 12, boundingBox);
               k = 12;
            }

            k = 8;

            for(p = 0; p < 2; ++p) {
               this.fillWithOutline(structureWorldAccess, boundingBox, k, 0, 7, k, 2, 7, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
               this.fillWithOutline(structureWorldAccess, boundingBox, k, 0, 14, k, 2, 14, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
               k = 14;
            }

            this.fillWithOutline(structureWorldAccess, boundingBox, 8, 3, 8, 8, 3, 13, DARK_PRISMARINE, DARK_PRISMARINE, false);
            this.fillWithOutline(structureWorldAccess, boundingBox, 14, 3, 8, 14, 3, 13, DARK_PRISMARINE, DARK_PRISMARINE, false);
            this.method_14772(structureWorldAccess, boundingBox, 11, 5, 13);
         }

         return true;
      }
   }

   public static class CoreRoom extends OceanMonumentGenerator.Piece {
      public CoreRoom(Direction direction, OceanMonumentGenerator.PieceSetting pieceSetting) {
         super(StructurePieceType.OCEAN_MONUMENT_CORE_ROOM, 1, direction, pieceSetting, 2, 2, 2);
      }

      public CoreRoom(StructureManager structureManager, CompoundTag compoundTag) {
         super(StructurePieceType.OCEAN_MONUMENT_CORE_ROOM, compoundTag);
      }

      public boolean generate(StructureWorldAccess structureWorldAccess, StructureAccessor structureAccessor, ChunkGenerator chunkGenerator, Random random, BlockBox boundingBox, ChunkPos chunkPos, BlockPos blockPos) {
         this.method_14771(structureWorldAccess, boundingBox, 1, 8, 0, 14, 8, 14, PRISMARINE);
         int i = true;
         BlockState blockState2 = PRISMARINE_BRICKS;
         this.fillWithOutline(structureWorldAccess, boundingBox, 0, 7, 0, 0, 7, 15, blockState2, blockState2, false);
         this.fillWithOutline(structureWorldAccess, boundingBox, 15, 7, 0, 15, 7, 15, blockState2, blockState2, false);
         this.fillWithOutline(structureWorldAccess, boundingBox, 1, 7, 0, 15, 7, 0, blockState2, blockState2, false);
         this.fillWithOutline(structureWorldAccess, boundingBox, 1, 7, 15, 14, 7, 15, blockState2, blockState2, false);

         int l;
         for(l = 1; l <= 6; ++l) {
            blockState2 = PRISMARINE_BRICKS;
            if (l == 2 || l == 6) {
               blockState2 = PRISMARINE;
            }

            for(int k = 0; k <= 15; k += 15) {
               this.fillWithOutline(structureWorldAccess, boundingBox, k, l, 0, k, l, 1, blockState2, blockState2, false);
               this.fillWithOutline(structureWorldAccess, boundingBox, k, l, 6, k, l, 9, blockState2, blockState2, false);
               this.fillWithOutline(structureWorldAccess, boundingBox, k, l, 14, k, l, 15, blockState2, blockState2, false);
            }

            this.fillWithOutline(structureWorldAccess, boundingBox, 1, l, 0, 1, l, 0, blockState2, blockState2, false);
            this.fillWithOutline(structureWorldAccess, boundingBox, 6, l, 0, 9, l, 0, blockState2, blockState2, false);
            this.fillWithOutline(structureWorldAccess, boundingBox, 14, l, 0, 14, l, 0, blockState2, blockState2, false);
            this.fillWithOutline(structureWorldAccess, boundingBox, 1, l, 15, 14, l, 15, blockState2, blockState2, false);
         }

         this.fillWithOutline(structureWorldAccess, boundingBox, 6, 3, 6, 9, 6, 9, DARK_PRISMARINE, DARK_PRISMARINE, false);
         this.fillWithOutline(structureWorldAccess, boundingBox, 7, 4, 7, 8, 5, 8, Blocks.GOLD_BLOCK.getDefaultState(), Blocks.GOLD_BLOCK.getDefaultState(), false);

         for(l = 3; l <= 6; l += 3) {
            for(int m = 6; m <= 9; m += 3) {
               this.addBlock(structureWorldAccess, SEA_LANTERN, m, l, 6, boundingBox);
               this.addBlock(structureWorldAccess, SEA_LANTERN, m, l, 9, boundingBox);
            }
         }

         this.fillWithOutline(structureWorldAccess, boundingBox, 5, 1, 6, 5, 2, 6, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
         this.fillWithOutline(structureWorldAccess, boundingBox, 5, 1, 9, 5, 2, 9, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
         this.fillWithOutline(structureWorldAccess, boundingBox, 10, 1, 6, 10, 2, 6, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
         this.fillWithOutline(structureWorldAccess, boundingBox, 10, 1, 9, 10, 2, 9, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
         this.fillWithOutline(structureWorldAccess, boundingBox, 6, 1, 5, 6, 2, 5, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
         this.fillWithOutline(structureWorldAccess, boundingBox, 9, 1, 5, 9, 2, 5, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
         this.fillWithOutline(structureWorldAccess, boundingBox, 6, 1, 10, 6, 2, 10, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
         this.fillWithOutline(structureWorldAccess, boundingBox, 9, 1, 10, 9, 2, 10, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
         this.fillWithOutline(structureWorldAccess, boundingBox, 5, 2, 5, 5, 6, 5, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
         this.fillWithOutline(structureWorldAccess, boundingBox, 5, 2, 10, 5, 6, 10, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
         this.fillWithOutline(structureWorldAccess, boundingBox, 10, 2, 5, 10, 6, 5, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
         this.fillWithOutline(structureWorldAccess, boundingBox, 10, 2, 10, 10, 6, 10, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
         this.fillWithOutline(structureWorldAccess, boundingBox, 5, 7, 1, 5, 7, 6, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
         this.fillWithOutline(structureWorldAccess, boundingBox, 10, 7, 1, 10, 7, 6, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
         this.fillWithOutline(structureWorldAccess, boundingBox, 5, 7, 9, 5, 7, 14, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
         this.fillWithOutline(structureWorldAccess, boundingBox, 10, 7, 9, 10, 7, 14, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
         this.fillWithOutline(structureWorldAccess, boundingBox, 1, 7, 5, 6, 7, 5, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
         this.fillWithOutline(structureWorldAccess, boundingBox, 1, 7, 10, 6, 7, 10, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
         this.fillWithOutline(structureWorldAccess, boundingBox, 9, 7, 5, 14, 7, 5, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
         this.fillWithOutline(structureWorldAccess, boundingBox, 9, 7, 10, 14, 7, 10, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
         this.fillWithOutline(structureWorldAccess, boundingBox, 2, 1, 2, 2, 1, 3, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
         this.fillWithOutline(structureWorldAccess, boundingBox, 3, 1, 2, 3, 1, 2, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
         this.fillWithOutline(structureWorldAccess, boundingBox, 13, 1, 2, 13, 1, 3, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
         this.fillWithOutline(structureWorldAccess, boundingBox, 12, 1, 2, 12, 1, 2, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
         this.fillWithOutline(structureWorldAccess, boundingBox, 2, 1, 12, 2, 1, 13, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
         this.fillWithOutline(structureWorldAccess, boundingBox, 3, 1, 13, 3, 1, 13, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
         this.fillWithOutline(structureWorldAccess, boundingBox, 13, 1, 12, 13, 1, 13, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
         this.fillWithOutline(structureWorldAccess, boundingBox, 12, 1, 13, 12, 1, 13, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
         return true;
      }
   }

   public static class DoubleYZRoom extends OceanMonumentGenerator.Piece {
      public DoubleYZRoom(Direction direction, OceanMonumentGenerator.PieceSetting pieceSetting) {
         super(StructurePieceType.OCEAN_MONUMENT_DOUBLE_Y_Z_ROOM, 1, direction, pieceSetting, 1, 2, 2);
      }

      public DoubleYZRoom(StructureManager structureManager, CompoundTag compoundTag) {
         super(StructurePieceType.OCEAN_MONUMENT_DOUBLE_Y_Z_ROOM, compoundTag);
      }

      public boolean generate(StructureWorldAccess structureWorldAccess, StructureAccessor structureAccessor, ChunkGenerator chunkGenerator, Random random, BlockBox boundingBox, ChunkPos chunkPos, BlockPos blockPos) {
         OceanMonumentGenerator.PieceSetting pieceSetting = this.setting.neighbors[Direction.NORTH.getId()];
         OceanMonumentGenerator.PieceSetting pieceSetting2 = this.setting;
         OceanMonumentGenerator.PieceSetting pieceSetting3 = pieceSetting.neighbors[Direction.UP.getId()];
         OceanMonumentGenerator.PieceSetting pieceSetting4 = pieceSetting2.neighbors[Direction.UP.getId()];
         if (this.setting.roomIndex / 25 > 0) {
            this.method_14774(structureWorldAccess, boundingBox, 0, 8, pieceSetting.neighborPresences[Direction.DOWN.getId()]);
            this.method_14774(structureWorldAccess, boundingBox, 0, 0, pieceSetting2.neighborPresences[Direction.DOWN.getId()]);
         }

         if (pieceSetting4.neighbors[Direction.UP.getId()] == null) {
            this.method_14771(structureWorldAccess, boundingBox, 1, 8, 1, 6, 8, 7, PRISMARINE);
         }

         if (pieceSetting3.neighbors[Direction.UP.getId()] == null) {
            this.method_14771(structureWorldAccess, boundingBox, 1, 8, 8, 6, 8, 14, PRISMARINE);
         }

         int j;
         BlockState blockState2;
         for(j = 1; j <= 7; ++j) {
            blockState2 = PRISMARINE_BRICKS;
            if (j == 2 || j == 6) {
               blockState2 = PRISMARINE;
            }

            this.fillWithOutline(structureWorldAccess, boundingBox, 0, j, 0, 0, j, 15, blockState2, blockState2, false);
            this.fillWithOutline(structureWorldAccess, boundingBox, 7, j, 0, 7, j, 15, blockState2, blockState2, false);
            this.fillWithOutline(structureWorldAccess, boundingBox, 1, j, 0, 6, j, 0, blockState2, blockState2, false);
            this.fillWithOutline(structureWorldAccess, boundingBox, 1, j, 15, 6, j, 15, blockState2, blockState2, false);
         }

         for(j = 1; j <= 7; ++j) {
            blockState2 = DARK_PRISMARINE;
            if (j == 2 || j == 6) {
               blockState2 = SEA_LANTERN;
            }

            this.fillWithOutline(structureWorldAccess, boundingBox, 3, j, 7, 4, j, 8, blockState2, blockState2, false);
         }

         if (pieceSetting2.neighborPresences[Direction.SOUTH.getId()]) {
            this.setAirAndWater(structureWorldAccess, boundingBox, 3, 1, 0, 4, 2, 0);
         }

         if (pieceSetting2.neighborPresences[Direction.EAST.getId()]) {
            this.setAirAndWater(structureWorldAccess, boundingBox, 7, 1, 3, 7, 2, 4);
         }

         if (pieceSetting2.neighborPresences[Direction.WEST.getId()]) {
            this.setAirAndWater(structureWorldAccess, boundingBox, 0, 1, 3, 0, 2, 4);
         }

         if (pieceSetting.neighborPresences[Direction.NORTH.getId()]) {
            this.setAirAndWater(structureWorldAccess, boundingBox, 3, 1, 15, 4, 2, 15);
         }

         if (pieceSetting.neighborPresences[Direction.WEST.getId()]) {
            this.setAirAndWater(structureWorldAccess, boundingBox, 0, 1, 11, 0, 2, 12);
         }

         if (pieceSetting.neighborPresences[Direction.EAST.getId()]) {
            this.setAirAndWater(structureWorldAccess, boundingBox, 7, 1, 11, 7, 2, 12);
         }

         if (pieceSetting4.neighborPresences[Direction.SOUTH.getId()]) {
            this.setAirAndWater(structureWorldAccess, boundingBox, 3, 5, 0, 4, 6, 0);
         }

         if (pieceSetting4.neighborPresences[Direction.EAST.getId()]) {
            this.setAirAndWater(structureWorldAccess, boundingBox, 7, 5, 3, 7, 6, 4);
            this.fillWithOutline(structureWorldAccess, boundingBox, 5, 4, 2, 6, 4, 5, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            this.fillWithOutline(structureWorldAccess, boundingBox, 6, 1, 2, 6, 3, 2, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            this.fillWithOutline(structureWorldAccess, boundingBox, 6, 1, 5, 6, 3, 5, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
         }

         if (pieceSetting4.neighborPresences[Direction.WEST.getId()]) {
            this.setAirAndWater(structureWorldAccess, boundingBox, 0, 5, 3, 0, 6, 4);
            this.fillWithOutline(structureWorldAccess, boundingBox, 1, 4, 2, 2, 4, 5, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            this.fillWithOutline(structureWorldAccess, boundingBox, 1, 1, 2, 1, 3, 2, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            this.fillWithOutline(structureWorldAccess, boundingBox, 1, 1, 5, 1, 3, 5, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
         }

         if (pieceSetting3.neighborPresences[Direction.NORTH.getId()]) {
            this.setAirAndWater(structureWorldAccess, boundingBox, 3, 5, 15, 4, 6, 15);
         }

         if (pieceSetting3.neighborPresences[Direction.WEST.getId()]) {
            this.setAirAndWater(structureWorldAccess, boundingBox, 0, 5, 11, 0, 6, 12);
            this.fillWithOutline(structureWorldAccess, boundingBox, 1, 4, 10, 2, 4, 13, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            this.fillWithOutline(structureWorldAccess, boundingBox, 1, 1, 10, 1, 3, 10, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            this.fillWithOutline(structureWorldAccess, boundingBox, 1, 1, 13, 1, 3, 13, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
         }

         if (pieceSetting3.neighborPresences[Direction.EAST.getId()]) {
            this.setAirAndWater(structureWorldAccess, boundingBox, 7, 5, 11, 7, 6, 12);
            this.fillWithOutline(structureWorldAccess, boundingBox, 5, 4, 10, 6, 4, 13, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            this.fillWithOutline(structureWorldAccess, boundingBox, 6, 1, 10, 6, 3, 10, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            this.fillWithOutline(structureWorldAccess, boundingBox, 6, 1, 13, 6, 3, 13, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
         }

         return true;
      }
   }

   public static class DoubleXYRoom extends OceanMonumentGenerator.Piece {
      public DoubleXYRoom(Direction direction, OceanMonumentGenerator.PieceSetting pieceSetting) {
         super(StructurePieceType.OCEAN_MONUMENT_DOUBLE_X_Y_ROOM, 1, direction, pieceSetting, 2, 2, 1);
      }

      public DoubleXYRoom(StructureManager structureManager, CompoundTag compoundTag) {
         super(StructurePieceType.OCEAN_MONUMENT_DOUBLE_X_Y_ROOM, compoundTag);
      }

      public boolean generate(StructureWorldAccess structureWorldAccess, StructureAccessor structureAccessor, ChunkGenerator chunkGenerator, Random random, BlockBox boundingBox, ChunkPos chunkPos, BlockPos blockPos) {
         OceanMonumentGenerator.PieceSetting pieceSetting = this.setting.neighbors[Direction.EAST.getId()];
         OceanMonumentGenerator.PieceSetting pieceSetting2 = this.setting;
         OceanMonumentGenerator.PieceSetting pieceSetting3 = pieceSetting2.neighbors[Direction.UP.getId()];
         OceanMonumentGenerator.PieceSetting pieceSetting4 = pieceSetting.neighbors[Direction.UP.getId()];
         if (this.setting.roomIndex / 25 > 0) {
            this.method_14774(structureWorldAccess, boundingBox, 8, 0, pieceSetting.neighborPresences[Direction.DOWN.getId()]);
            this.method_14774(structureWorldAccess, boundingBox, 0, 0, pieceSetting2.neighborPresences[Direction.DOWN.getId()]);
         }

         if (pieceSetting3.neighbors[Direction.UP.getId()] == null) {
            this.method_14771(structureWorldAccess, boundingBox, 1, 8, 1, 7, 8, 6, PRISMARINE);
         }

         if (pieceSetting4.neighbors[Direction.UP.getId()] == null) {
            this.method_14771(structureWorldAccess, boundingBox, 8, 8, 1, 14, 8, 6, PRISMARINE);
         }

         for(int i = 1; i <= 7; ++i) {
            BlockState blockState = PRISMARINE_BRICKS;
            if (i == 2 || i == 6) {
               blockState = PRISMARINE;
            }

            this.fillWithOutline(structureWorldAccess, boundingBox, 0, i, 0, 0, i, 7, blockState, blockState, false);
            this.fillWithOutline(structureWorldAccess, boundingBox, 15, i, 0, 15, i, 7, blockState, blockState, false);
            this.fillWithOutline(structureWorldAccess, boundingBox, 1, i, 0, 15, i, 0, blockState, blockState, false);
            this.fillWithOutline(structureWorldAccess, boundingBox, 1, i, 7, 14, i, 7, blockState, blockState, false);
         }

         this.fillWithOutline(structureWorldAccess, boundingBox, 2, 1, 3, 2, 7, 4, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
         this.fillWithOutline(structureWorldAccess, boundingBox, 3, 1, 2, 4, 7, 2, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
         this.fillWithOutline(structureWorldAccess, boundingBox, 3, 1, 5, 4, 7, 5, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
         this.fillWithOutline(structureWorldAccess, boundingBox, 13, 1, 3, 13, 7, 4, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
         this.fillWithOutline(structureWorldAccess, boundingBox, 11, 1, 2, 12, 7, 2, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
         this.fillWithOutline(structureWorldAccess, boundingBox, 11, 1, 5, 12, 7, 5, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
         this.fillWithOutline(structureWorldAccess, boundingBox, 5, 1, 3, 5, 3, 4, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
         this.fillWithOutline(structureWorldAccess, boundingBox, 10, 1, 3, 10, 3, 4, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
         this.fillWithOutline(structureWorldAccess, boundingBox, 5, 7, 2, 10, 7, 5, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
         this.fillWithOutline(structureWorldAccess, boundingBox, 5, 5, 2, 5, 7, 2, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
         this.fillWithOutline(structureWorldAccess, boundingBox, 10, 5, 2, 10, 7, 2, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
         this.fillWithOutline(structureWorldAccess, boundingBox, 5, 5, 5, 5, 7, 5, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
         this.fillWithOutline(structureWorldAccess, boundingBox, 10, 5, 5, 10, 7, 5, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
         this.addBlock(structureWorldAccess, PRISMARINE_BRICKS, 6, 6, 2, boundingBox);
         this.addBlock(structureWorldAccess, PRISMARINE_BRICKS, 9, 6, 2, boundingBox);
         this.addBlock(structureWorldAccess, PRISMARINE_BRICKS, 6, 6, 5, boundingBox);
         this.addBlock(structureWorldAccess, PRISMARINE_BRICKS, 9, 6, 5, boundingBox);
         this.fillWithOutline(structureWorldAccess, boundingBox, 5, 4, 3, 6, 4, 4, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
         this.fillWithOutline(structureWorldAccess, boundingBox, 9, 4, 3, 10, 4, 4, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
         this.addBlock(structureWorldAccess, SEA_LANTERN, 5, 4, 2, boundingBox);
         this.addBlock(structureWorldAccess, SEA_LANTERN, 5, 4, 5, boundingBox);
         this.addBlock(structureWorldAccess, SEA_LANTERN, 10, 4, 2, boundingBox);
         this.addBlock(structureWorldAccess, SEA_LANTERN, 10, 4, 5, boundingBox);
         if (pieceSetting2.neighborPresences[Direction.SOUTH.getId()]) {
            this.setAirAndWater(structureWorldAccess, boundingBox, 3, 1, 0, 4, 2, 0);
         }

         if (pieceSetting2.neighborPresences[Direction.NORTH.getId()]) {
            this.setAirAndWater(structureWorldAccess, boundingBox, 3, 1, 7, 4, 2, 7);
         }

         if (pieceSetting2.neighborPresences[Direction.WEST.getId()]) {
            this.setAirAndWater(structureWorldAccess, boundingBox, 0, 1, 3, 0, 2, 4);
         }

         if (pieceSetting.neighborPresences[Direction.SOUTH.getId()]) {
            this.setAirAndWater(structureWorldAccess, boundingBox, 11, 1, 0, 12, 2, 0);
         }

         if (pieceSetting.neighborPresences[Direction.NORTH.getId()]) {
            this.setAirAndWater(structureWorldAccess, boundingBox, 11, 1, 7, 12, 2, 7);
         }

         if (pieceSetting.neighborPresences[Direction.EAST.getId()]) {
            this.setAirAndWater(structureWorldAccess, boundingBox, 15, 1, 3, 15, 2, 4);
         }

         if (pieceSetting3.neighborPresences[Direction.SOUTH.getId()]) {
            this.setAirAndWater(structureWorldAccess, boundingBox, 3, 5, 0, 4, 6, 0);
         }

         if (pieceSetting3.neighborPresences[Direction.NORTH.getId()]) {
            this.setAirAndWater(structureWorldAccess, boundingBox, 3, 5, 7, 4, 6, 7);
         }

         if (pieceSetting3.neighborPresences[Direction.WEST.getId()]) {
            this.setAirAndWater(structureWorldAccess, boundingBox, 0, 5, 3, 0, 6, 4);
         }

         if (pieceSetting4.neighborPresences[Direction.SOUTH.getId()]) {
            this.setAirAndWater(structureWorldAccess, boundingBox, 11, 5, 0, 12, 6, 0);
         }

         if (pieceSetting4.neighborPresences[Direction.NORTH.getId()]) {
            this.setAirAndWater(structureWorldAccess, boundingBox, 11, 5, 7, 12, 6, 7);
         }

         if (pieceSetting4.neighborPresences[Direction.EAST.getId()]) {
            this.setAirAndWater(structureWorldAccess, boundingBox, 15, 5, 3, 15, 6, 4);
         }

         return true;
      }
   }

   public static class DoubleZRoom extends OceanMonumentGenerator.Piece {
      public DoubleZRoom(Direction direction, OceanMonumentGenerator.PieceSetting pieceSetting) {
         super(StructurePieceType.OCEAN_MONUMENT_DOUBLE_Z_ROOM, 1, direction, pieceSetting, 1, 1, 2);
      }

      public DoubleZRoom(StructureManager structureManager, CompoundTag compoundTag) {
         super(StructurePieceType.OCEAN_MONUMENT_DOUBLE_Z_ROOM, compoundTag);
      }

      public boolean generate(StructureWorldAccess structureWorldAccess, StructureAccessor structureAccessor, ChunkGenerator chunkGenerator, Random random, BlockBox boundingBox, ChunkPos chunkPos, BlockPos blockPos) {
         OceanMonumentGenerator.PieceSetting pieceSetting = this.setting.neighbors[Direction.NORTH.getId()];
         OceanMonumentGenerator.PieceSetting pieceSetting2 = this.setting;
         if (this.setting.roomIndex / 25 > 0) {
            this.method_14774(structureWorldAccess, boundingBox, 0, 8, pieceSetting.neighborPresences[Direction.DOWN.getId()]);
            this.method_14774(structureWorldAccess, boundingBox, 0, 0, pieceSetting2.neighborPresences[Direction.DOWN.getId()]);
         }

         if (pieceSetting2.neighbors[Direction.UP.getId()] == null) {
            this.method_14771(structureWorldAccess, boundingBox, 1, 4, 1, 6, 4, 7, PRISMARINE);
         }

         if (pieceSetting.neighbors[Direction.UP.getId()] == null) {
            this.method_14771(structureWorldAccess, boundingBox, 1, 4, 8, 6, 4, 14, PRISMARINE);
         }

         this.fillWithOutline(structureWorldAccess, boundingBox, 0, 3, 0, 0, 3, 15, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
         this.fillWithOutline(structureWorldAccess, boundingBox, 7, 3, 0, 7, 3, 15, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
         this.fillWithOutline(structureWorldAccess, boundingBox, 1, 3, 0, 7, 3, 0, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
         this.fillWithOutline(structureWorldAccess, boundingBox, 1, 3, 15, 6, 3, 15, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
         this.fillWithOutline(structureWorldAccess, boundingBox, 0, 2, 0, 0, 2, 15, PRISMARINE, PRISMARINE, false);
         this.fillWithOutline(structureWorldAccess, boundingBox, 7, 2, 0, 7, 2, 15, PRISMARINE, PRISMARINE, false);
         this.fillWithOutline(structureWorldAccess, boundingBox, 1, 2, 0, 7, 2, 0, PRISMARINE, PRISMARINE, false);
         this.fillWithOutline(structureWorldAccess, boundingBox, 1, 2, 15, 6, 2, 15, PRISMARINE, PRISMARINE, false);
         this.fillWithOutline(structureWorldAccess, boundingBox, 0, 1, 0, 0, 1, 15, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
         this.fillWithOutline(structureWorldAccess, boundingBox, 7, 1, 0, 7, 1, 15, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
         this.fillWithOutline(structureWorldAccess, boundingBox, 1, 1, 0, 7, 1, 0, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
         this.fillWithOutline(structureWorldAccess, boundingBox, 1, 1, 15, 6, 1, 15, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
         this.fillWithOutline(structureWorldAccess, boundingBox, 1, 1, 1, 1, 1, 2, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
         this.fillWithOutline(structureWorldAccess, boundingBox, 6, 1, 1, 6, 1, 2, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
         this.fillWithOutline(structureWorldAccess, boundingBox, 1, 3, 1, 1, 3, 2, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
         this.fillWithOutline(structureWorldAccess, boundingBox, 6, 3, 1, 6, 3, 2, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
         this.fillWithOutline(structureWorldAccess, boundingBox, 1, 1, 13, 1, 1, 14, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
         this.fillWithOutline(structureWorldAccess, boundingBox, 6, 1, 13, 6, 1, 14, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
         this.fillWithOutline(structureWorldAccess, boundingBox, 1, 3, 13, 1, 3, 14, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
         this.fillWithOutline(structureWorldAccess, boundingBox, 6, 3, 13, 6, 3, 14, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
         this.fillWithOutline(structureWorldAccess, boundingBox, 2, 1, 6, 2, 3, 6, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
         this.fillWithOutline(structureWorldAccess, boundingBox, 5, 1, 6, 5, 3, 6, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
         this.fillWithOutline(structureWorldAccess, boundingBox, 2, 1, 9, 2, 3, 9, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
         this.fillWithOutline(structureWorldAccess, boundingBox, 5, 1, 9, 5, 3, 9, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
         this.fillWithOutline(structureWorldAccess, boundingBox, 3, 2, 6, 4, 2, 6, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
         this.fillWithOutline(structureWorldAccess, boundingBox, 3, 2, 9, 4, 2, 9, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
         this.fillWithOutline(structureWorldAccess, boundingBox, 2, 2, 7, 2, 2, 8, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
         this.fillWithOutline(structureWorldAccess, boundingBox, 5, 2, 7, 5, 2, 8, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
         this.addBlock(structureWorldAccess, SEA_LANTERN, 2, 2, 5, boundingBox);
         this.addBlock(structureWorldAccess, SEA_LANTERN, 5, 2, 5, boundingBox);
         this.addBlock(structureWorldAccess, SEA_LANTERN, 2, 2, 10, boundingBox);
         this.addBlock(structureWorldAccess, SEA_LANTERN, 5, 2, 10, boundingBox);
         this.addBlock(structureWorldAccess, PRISMARINE_BRICKS, 2, 3, 5, boundingBox);
         this.addBlock(structureWorldAccess, PRISMARINE_BRICKS, 5, 3, 5, boundingBox);
         this.addBlock(structureWorldAccess, PRISMARINE_BRICKS, 2, 3, 10, boundingBox);
         this.addBlock(structureWorldAccess, PRISMARINE_BRICKS, 5, 3, 10, boundingBox);
         if (pieceSetting2.neighborPresences[Direction.SOUTH.getId()]) {
            this.setAirAndWater(structureWorldAccess, boundingBox, 3, 1, 0, 4, 2, 0);
         }

         if (pieceSetting2.neighborPresences[Direction.EAST.getId()]) {
            this.setAirAndWater(structureWorldAccess, boundingBox, 7, 1, 3, 7, 2, 4);
         }

         if (pieceSetting2.neighborPresences[Direction.WEST.getId()]) {
            this.setAirAndWater(structureWorldAccess, boundingBox, 0, 1, 3, 0, 2, 4);
         }

         if (pieceSetting.neighborPresences[Direction.NORTH.getId()]) {
            this.setAirAndWater(structureWorldAccess, boundingBox, 3, 1, 15, 4, 2, 15);
         }

         if (pieceSetting.neighborPresences[Direction.WEST.getId()]) {
            this.setAirAndWater(structureWorldAccess, boundingBox, 0, 1, 11, 0, 2, 12);
         }

         if (pieceSetting.neighborPresences[Direction.EAST.getId()]) {
            this.setAirAndWater(structureWorldAccess, boundingBox, 7, 1, 11, 7, 2, 12);
         }

         return true;
      }
   }

   public static class DoubleXRoom extends OceanMonumentGenerator.Piece {
      public DoubleXRoom(Direction direction, OceanMonumentGenerator.PieceSetting pieceSetting) {
         super(StructurePieceType.OCEAN_MONUMENT_DOUBLE_X_ROOM, 1, direction, pieceSetting, 2, 1, 1);
      }

      public DoubleXRoom(StructureManager structureManager, CompoundTag compoundTag) {
         super(StructurePieceType.OCEAN_MONUMENT_DOUBLE_X_ROOM, compoundTag);
      }

      public boolean generate(StructureWorldAccess structureWorldAccess, StructureAccessor structureAccessor, ChunkGenerator chunkGenerator, Random random, BlockBox boundingBox, ChunkPos chunkPos, BlockPos blockPos) {
         OceanMonumentGenerator.PieceSetting pieceSetting = this.setting.neighbors[Direction.EAST.getId()];
         OceanMonumentGenerator.PieceSetting pieceSetting2 = this.setting;
         if (this.setting.roomIndex / 25 > 0) {
            this.method_14774(structureWorldAccess, boundingBox, 8, 0, pieceSetting.neighborPresences[Direction.DOWN.getId()]);
            this.method_14774(structureWorldAccess, boundingBox, 0, 0, pieceSetting2.neighborPresences[Direction.DOWN.getId()]);
         }

         if (pieceSetting2.neighbors[Direction.UP.getId()] == null) {
            this.method_14771(structureWorldAccess, boundingBox, 1, 4, 1, 7, 4, 6, PRISMARINE);
         }

         if (pieceSetting.neighbors[Direction.UP.getId()] == null) {
            this.method_14771(structureWorldAccess, boundingBox, 8, 4, 1, 14, 4, 6, PRISMARINE);
         }

         this.fillWithOutline(structureWorldAccess, boundingBox, 0, 3, 0, 0, 3, 7, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
         this.fillWithOutline(structureWorldAccess, boundingBox, 15, 3, 0, 15, 3, 7, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
         this.fillWithOutline(structureWorldAccess, boundingBox, 1, 3, 0, 15, 3, 0, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
         this.fillWithOutline(structureWorldAccess, boundingBox, 1, 3, 7, 14, 3, 7, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
         this.fillWithOutline(structureWorldAccess, boundingBox, 0, 2, 0, 0, 2, 7, PRISMARINE, PRISMARINE, false);
         this.fillWithOutline(structureWorldAccess, boundingBox, 15, 2, 0, 15, 2, 7, PRISMARINE, PRISMARINE, false);
         this.fillWithOutline(structureWorldAccess, boundingBox, 1, 2, 0, 15, 2, 0, PRISMARINE, PRISMARINE, false);
         this.fillWithOutline(structureWorldAccess, boundingBox, 1, 2, 7, 14, 2, 7, PRISMARINE, PRISMARINE, false);
         this.fillWithOutline(structureWorldAccess, boundingBox, 0, 1, 0, 0, 1, 7, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
         this.fillWithOutline(structureWorldAccess, boundingBox, 15, 1, 0, 15, 1, 7, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
         this.fillWithOutline(structureWorldAccess, boundingBox, 1, 1, 0, 15, 1, 0, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
         this.fillWithOutline(structureWorldAccess, boundingBox, 1, 1, 7, 14, 1, 7, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
         this.fillWithOutline(structureWorldAccess, boundingBox, 5, 1, 0, 10, 1, 4, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
         this.fillWithOutline(structureWorldAccess, boundingBox, 6, 2, 0, 9, 2, 3, PRISMARINE, PRISMARINE, false);
         this.fillWithOutline(structureWorldAccess, boundingBox, 5, 3, 0, 10, 3, 4, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
         this.addBlock(structureWorldAccess, SEA_LANTERN, 6, 2, 3, boundingBox);
         this.addBlock(structureWorldAccess, SEA_LANTERN, 9, 2, 3, boundingBox);
         if (pieceSetting2.neighborPresences[Direction.SOUTH.getId()]) {
            this.setAirAndWater(structureWorldAccess, boundingBox, 3, 1, 0, 4, 2, 0);
         }

         if (pieceSetting2.neighborPresences[Direction.NORTH.getId()]) {
            this.setAirAndWater(structureWorldAccess, boundingBox, 3, 1, 7, 4, 2, 7);
         }

         if (pieceSetting2.neighborPresences[Direction.WEST.getId()]) {
            this.setAirAndWater(structureWorldAccess, boundingBox, 0, 1, 3, 0, 2, 4);
         }

         if (pieceSetting.neighborPresences[Direction.SOUTH.getId()]) {
            this.setAirAndWater(structureWorldAccess, boundingBox, 11, 1, 0, 12, 2, 0);
         }

         if (pieceSetting.neighborPresences[Direction.NORTH.getId()]) {
            this.setAirAndWater(structureWorldAccess, boundingBox, 11, 1, 7, 12, 2, 7);
         }

         if (pieceSetting.neighborPresences[Direction.EAST.getId()]) {
            this.setAirAndWater(structureWorldAccess, boundingBox, 15, 1, 3, 15, 2, 4);
         }

         return true;
      }
   }

   public static class DoubleYRoom extends OceanMonumentGenerator.Piece {
      public DoubleYRoom(Direction direction, OceanMonumentGenerator.PieceSetting pieceSetting) {
         super(StructurePieceType.OCEAN_MONUMENT_DOUBLE_Y_ROOM, 1, direction, pieceSetting, 1, 2, 1);
      }

      public DoubleYRoom(StructureManager structureManager, CompoundTag compoundTag) {
         super(StructurePieceType.OCEAN_MONUMENT_DOUBLE_Y_ROOM, compoundTag);
      }

      public boolean generate(StructureWorldAccess structureWorldAccess, StructureAccessor structureAccessor, ChunkGenerator chunkGenerator, Random random, BlockBox boundingBox, ChunkPos chunkPos, BlockPos blockPos) {
         if (this.setting.roomIndex / 25 > 0) {
            this.method_14774(structureWorldAccess, boundingBox, 0, 0, this.setting.neighborPresences[Direction.DOWN.getId()]);
         }

         OceanMonumentGenerator.PieceSetting pieceSetting = this.setting.neighbors[Direction.UP.getId()];
         if (pieceSetting.neighbors[Direction.UP.getId()] == null) {
            this.method_14771(structureWorldAccess, boundingBox, 1, 8, 1, 6, 8, 6, PRISMARINE);
         }

         this.fillWithOutline(structureWorldAccess, boundingBox, 0, 4, 0, 0, 4, 7, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
         this.fillWithOutline(structureWorldAccess, boundingBox, 7, 4, 0, 7, 4, 7, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
         this.fillWithOutline(structureWorldAccess, boundingBox, 1, 4, 0, 6, 4, 0, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
         this.fillWithOutline(structureWorldAccess, boundingBox, 1, 4, 7, 6, 4, 7, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
         this.fillWithOutline(structureWorldAccess, boundingBox, 2, 4, 1, 2, 4, 2, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
         this.fillWithOutline(structureWorldAccess, boundingBox, 1, 4, 2, 1, 4, 2, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
         this.fillWithOutline(structureWorldAccess, boundingBox, 5, 4, 1, 5, 4, 2, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
         this.fillWithOutline(structureWorldAccess, boundingBox, 6, 4, 2, 6, 4, 2, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
         this.fillWithOutline(structureWorldAccess, boundingBox, 2, 4, 5, 2, 4, 6, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
         this.fillWithOutline(structureWorldAccess, boundingBox, 1, 4, 5, 1, 4, 5, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
         this.fillWithOutline(structureWorldAccess, boundingBox, 5, 4, 5, 5, 4, 6, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
         this.fillWithOutline(structureWorldAccess, boundingBox, 6, 4, 5, 6, 4, 5, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
         OceanMonumentGenerator.PieceSetting pieceSetting2 = this.setting;

         for(int i = 1; i <= 5; i += 4) {
            int j = 0;
            if (pieceSetting2.neighborPresences[Direction.SOUTH.getId()]) {
               this.fillWithOutline(structureWorldAccess, boundingBox, 2, i, j, 2, i + 2, j, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
               this.fillWithOutline(structureWorldAccess, boundingBox, 5, i, j, 5, i + 2, j, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
               this.fillWithOutline(structureWorldAccess, boundingBox, 3, i + 2, j, 4, i + 2, j, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            } else {
               this.fillWithOutline(structureWorldAccess, boundingBox, 0, i, j, 7, i + 2, j, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
               this.fillWithOutline(structureWorldAccess, boundingBox, 0, i + 1, j, 7, i + 1, j, PRISMARINE, PRISMARINE, false);
            }

            j = 7;
            if (pieceSetting2.neighborPresences[Direction.NORTH.getId()]) {
               this.fillWithOutline(structureWorldAccess, boundingBox, 2, i, j, 2, i + 2, j, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
               this.fillWithOutline(structureWorldAccess, boundingBox, 5, i, j, 5, i + 2, j, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
               this.fillWithOutline(structureWorldAccess, boundingBox, 3, i + 2, j, 4, i + 2, j, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            } else {
               this.fillWithOutline(structureWorldAccess, boundingBox, 0, i, j, 7, i + 2, j, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
               this.fillWithOutline(structureWorldAccess, boundingBox, 0, i + 1, j, 7, i + 1, j, PRISMARINE, PRISMARINE, false);
            }

            int k = 0;
            if (pieceSetting2.neighborPresences[Direction.WEST.getId()]) {
               this.fillWithOutline(structureWorldAccess, boundingBox, k, i, 2, k, i + 2, 2, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
               this.fillWithOutline(structureWorldAccess, boundingBox, k, i, 5, k, i + 2, 5, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
               this.fillWithOutline(structureWorldAccess, boundingBox, k, i + 2, 3, k, i + 2, 4, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            } else {
               this.fillWithOutline(structureWorldAccess, boundingBox, k, i, 0, k, i + 2, 7, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
               this.fillWithOutline(structureWorldAccess, boundingBox, k, i + 1, 0, k, i + 1, 7, PRISMARINE, PRISMARINE, false);
            }

            k = 7;
            if (pieceSetting2.neighborPresences[Direction.EAST.getId()]) {
               this.fillWithOutline(structureWorldAccess, boundingBox, k, i, 2, k, i + 2, 2, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
               this.fillWithOutline(structureWorldAccess, boundingBox, k, i, 5, k, i + 2, 5, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
               this.fillWithOutline(structureWorldAccess, boundingBox, k, i + 2, 3, k, i + 2, 4, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            } else {
               this.fillWithOutline(structureWorldAccess, boundingBox, k, i, 0, k, i + 2, 7, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
               this.fillWithOutline(structureWorldAccess, boundingBox, k, i + 1, 0, k, i + 1, 7, PRISMARINE, PRISMARINE, false);
            }

            pieceSetting2 = pieceSetting;
         }

         return true;
      }
   }

   public static class SimpleRoomTop extends OceanMonumentGenerator.Piece {
      public SimpleRoomTop(Direction direction, OceanMonumentGenerator.PieceSetting pieceSetting) {
         super(StructurePieceType.OCEAN_MONUMENT_SIMPLE_TOP_ROOM, 1, direction, pieceSetting, 1, 1, 1);
      }

      public SimpleRoomTop(StructureManager structureManager, CompoundTag compoundTag) {
         super(StructurePieceType.OCEAN_MONUMENT_SIMPLE_TOP_ROOM, compoundTag);
      }

      public boolean generate(StructureWorldAccess structureWorldAccess, StructureAccessor structureAccessor, ChunkGenerator chunkGenerator, Random random, BlockBox boundingBox, ChunkPos chunkPos, BlockPos blockPos) {
         if (this.setting.roomIndex / 25 > 0) {
            this.method_14774(structureWorldAccess, boundingBox, 0, 0, this.setting.neighborPresences[Direction.DOWN.getId()]);
         }

         if (this.setting.neighbors[Direction.UP.getId()] == null) {
            this.method_14771(structureWorldAccess, boundingBox, 1, 4, 1, 6, 4, 6, PRISMARINE);
         }

         for(int i = 1; i <= 6; ++i) {
            for(int j = 1; j <= 6; ++j) {
               if (random.nextInt(3) != 0) {
                  int k = 2 + (random.nextInt(4) == 0 ? 0 : 1);
                  BlockState blockState = Blocks.WET_SPONGE.getDefaultState();
                  this.fillWithOutline(structureWorldAccess, boundingBox, i, k, j, i, 3, j, blockState, blockState, false);
               }
            }
         }

         this.fillWithOutline(structureWorldAccess, boundingBox, 0, 1, 0, 0, 1, 7, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
         this.fillWithOutline(structureWorldAccess, boundingBox, 7, 1, 0, 7, 1, 7, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
         this.fillWithOutline(structureWorldAccess, boundingBox, 1, 1, 0, 6, 1, 0, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
         this.fillWithOutline(structureWorldAccess, boundingBox, 1, 1, 7, 6, 1, 7, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
         this.fillWithOutline(structureWorldAccess, boundingBox, 0, 2, 0, 0, 2, 7, DARK_PRISMARINE, DARK_PRISMARINE, false);
         this.fillWithOutline(structureWorldAccess, boundingBox, 7, 2, 0, 7, 2, 7, DARK_PRISMARINE, DARK_PRISMARINE, false);
         this.fillWithOutline(structureWorldAccess, boundingBox, 1, 2, 0, 6, 2, 0, DARK_PRISMARINE, DARK_PRISMARINE, false);
         this.fillWithOutline(structureWorldAccess, boundingBox, 1, 2, 7, 6, 2, 7, DARK_PRISMARINE, DARK_PRISMARINE, false);
         this.fillWithOutline(structureWorldAccess, boundingBox, 0, 3, 0, 0, 3, 7, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
         this.fillWithOutline(structureWorldAccess, boundingBox, 7, 3, 0, 7, 3, 7, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
         this.fillWithOutline(structureWorldAccess, boundingBox, 1, 3, 0, 6, 3, 0, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
         this.fillWithOutline(structureWorldAccess, boundingBox, 1, 3, 7, 6, 3, 7, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
         this.fillWithOutline(structureWorldAccess, boundingBox, 0, 1, 3, 0, 2, 4, DARK_PRISMARINE, DARK_PRISMARINE, false);
         this.fillWithOutline(structureWorldAccess, boundingBox, 7, 1, 3, 7, 2, 4, DARK_PRISMARINE, DARK_PRISMARINE, false);
         this.fillWithOutline(structureWorldAccess, boundingBox, 3, 1, 0, 4, 2, 0, DARK_PRISMARINE, DARK_PRISMARINE, false);
         this.fillWithOutline(structureWorldAccess, boundingBox, 3, 1, 7, 4, 2, 7, DARK_PRISMARINE, DARK_PRISMARINE, false);
         if (this.setting.neighborPresences[Direction.SOUTH.getId()]) {
            this.setAirAndWater(structureWorldAccess, boundingBox, 3, 1, 0, 4, 2, 0);
         }

         return true;
      }
   }

   public static class SimpleRoom extends OceanMonumentGenerator.Piece {
      private int field_14480;

      public SimpleRoom(Direction direction, OceanMonumentGenerator.PieceSetting pieceSetting, Random random) {
         super(StructurePieceType.OCEAN_MONUMENT_SIMPLE_ROOM, 1, direction, pieceSetting, 1, 1, 1);
         this.field_14480 = random.nextInt(3);
      }

      public SimpleRoom(StructureManager structureManager, CompoundTag compoundTag) {
         super(StructurePieceType.OCEAN_MONUMENT_SIMPLE_ROOM, compoundTag);
      }

      public boolean generate(StructureWorldAccess structureWorldAccess, StructureAccessor structureAccessor, ChunkGenerator chunkGenerator, Random random, BlockBox boundingBox, ChunkPos chunkPos, BlockPos blockPos) {
         if (this.setting.roomIndex / 25 > 0) {
            this.method_14774(structureWorldAccess, boundingBox, 0, 0, this.setting.neighborPresences[Direction.DOWN.getId()]);
         }

         if (this.setting.neighbors[Direction.UP.getId()] == null) {
            this.method_14771(structureWorldAccess, boundingBox, 1, 4, 1, 6, 4, 6, PRISMARINE);
         }

         boolean bl = this.field_14480 != 0 && random.nextBoolean() && !this.setting.neighborPresences[Direction.DOWN.getId()] && !this.setting.neighborPresences[Direction.UP.getId()] && this.setting.countNeighbors() > 1;
         if (this.field_14480 == 0) {
            this.fillWithOutline(structureWorldAccess, boundingBox, 0, 1, 0, 2, 1, 2, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            this.fillWithOutline(structureWorldAccess, boundingBox, 0, 3, 0, 2, 3, 2, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            this.fillWithOutline(structureWorldAccess, boundingBox, 0, 2, 0, 0, 2, 2, PRISMARINE, PRISMARINE, false);
            this.fillWithOutline(structureWorldAccess, boundingBox, 1, 2, 0, 2, 2, 0, PRISMARINE, PRISMARINE, false);
            this.addBlock(structureWorldAccess, SEA_LANTERN, 1, 2, 1, boundingBox);
            this.fillWithOutline(structureWorldAccess, boundingBox, 5, 1, 0, 7, 1, 2, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            this.fillWithOutline(structureWorldAccess, boundingBox, 5, 3, 0, 7, 3, 2, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            this.fillWithOutline(structureWorldAccess, boundingBox, 7, 2, 0, 7, 2, 2, PRISMARINE, PRISMARINE, false);
            this.fillWithOutline(structureWorldAccess, boundingBox, 5, 2, 0, 6, 2, 0, PRISMARINE, PRISMARINE, false);
            this.addBlock(structureWorldAccess, SEA_LANTERN, 6, 2, 1, boundingBox);
            this.fillWithOutline(structureWorldAccess, boundingBox, 0, 1, 5, 2, 1, 7, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            this.fillWithOutline(structureWorldAccess, boundingBox, 0, 3, 5, 2, 3, 7, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            this.fillWithOutline(structureWorldAccess, boundingBox, 0, 2, 5, 0, 2, 7, PRISMARINE, PRISMARINE, false);
            this.fillWithOutline(structureWorldAccess, boundingBox, 1, 2, 7, 2, 2, 7, PRISMARINE, PRISMARINE, false);
            this.addBlock(structureWorldAccess, SEA_LANTERN, 1, 2, 6, boundingBox);
            this.fillWithOutline(structureWorldAccess, boundingBox, 5, 1, 5, 7, 1, 7, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            this.fillWithOutline(structureWorldAccess, boundingBox, 5, 3, 5, 7, 3, 7, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            this.fillWithOutline(structureWorldAccess, boundingBox, 7, 2, 5, 7, 2, 7, PRISMARINE, PRISMARINE, false);
            this.fillWithOutline(structureWorldAccess, boundingBox, 5, 2, 7, 6, 2, 7, PRISMARINE, PRISMARINE, false);
            this.addBlock(structureWorldAccess, SEA_LANTERN, 6, 2, 6, boundingBox);
            if (this.setting.neighborPresences[Direction.SOUTH.getId()]) {
               this.fillWithOutline(structureWorldAccess, boundingBox, 3, 3, 0, 4, 3, 0, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            } else {
               this.fillWithOutline(structureWorldAccess, boundingBox, 3, 3, 0, 4, 3, 1, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
               this.fillWithOutline(structureWorldAccess, boundingBox, 3, 2, 0, 4, 2, 0, PRISMARINE, PRISMARINE, false);
               this.fillWithOutline(structureWorldAccess, boundingBox, 3, 1, 0, 4, 1, 1, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            }

            if (this.setting.neighborPresences[Direction.NORTH.getId()]) {
               this.fillWithOutline(structureWorldAccess, boundingBox, 3, 3, 7, 4, 3, 7, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            } else {
               this.fillWithOutline(structureWorldAccess, boundingBox, 3, 3, 6, 4, 3, 7, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
               this.fillWithOutline(structureWorldAccess, boundingBox, 3, 2, 7, 4, 2, 7, PRISMARINE, PRISMARINE, false);
               this.fillWithOutline(structureWorldAccess, boundingBox, 3, 1, 6, 4, 1, 7, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            }

            if (this.setting.neighborPresences[Direction.WEST.getId()]) {
               this.fillWithOutline(structureWorldAccess, boundingBox, 0, 3, 3, 0, 3, 4, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            } else {
               this.fillWithOutline(structureWorldAccess, boundingBox, 0, 3, 3, 1, 3, 4, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
               this.fillWithOutline(structureWorldAccess, boundingBox, 0, 2, 3, 0, 2, 4, PRISMARINE, PRISMARINE, false);
               this.fillWithOutline(structureWorldAccess, boundingBox, 0, 1, 3, 1, 1, 4, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            }

            if (this.setting.neighborPresences[Direction.EAST.getId()]) {
               this.fillWithOutline(structureWorldAccess, boundingBox, 7, 3, 3, 7, 3, 4, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            } else {
               this.fillWithOutline(structureWorldAccess, boundingBox, 6, 3, 3, 7, 3, 4, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
               this.fillWithOutline(structureWorldAccess, boundingBox, 7, 2, 3, 7, 2, 4, PRISMARINE, PRISMARINE, false);
               this.fillWithOutline(structureWorldAccess, boundingBox, 6, 1, 3, 7, 1, 4, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            }
         } else if (this.field_14480 == 1) {
            this.fillWithOutline(structureWorldAccess, boundingBox, 2, 1, 2, 2, 3, 2, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            this.fillWithOutline(structureWorldAccess, boundingBox, 2, 1, 5, 2, 3, 5, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            this.fillWithOutline(structureWorldAccess, boundingBox, 5, 1, 5, 5, 3, 5, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            this.fillWithOutline(structureWorldAccess, boundingBox, 5, 1, 2, 5, 3, 2, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            this.addBlock(structureWorldAccess, SEA_LANTERN, 2, 2, 2, boundingBox);
            this.addBlock(structureWorldAccess, SEA_LANTERN, 2, 2, 5, boundingBox);
            this.addBlock(structureWorldAccess, SEA_LANTERN, 5, 2, 5, boundingBox);
            this.addBlock(structureWorldAccess, SEA_LANTERN, 5, 2, 2, boundingBox);
            this.fillWithOutline(structureWorldAccess, boundingBox, 0, 1, 0, 1, 3, 0, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            this.fillWithOutline(structureWorldAccess, boundingBox, 0, 1, 1, 0, 3, 1, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            this.fillWithOutline(structureWorldAccess, boundingBox, 0, 1, 7, 1, 3, 7, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            this.fillWithOutline(structureWorldAccess, boundingBox, 0, 1, 6, 0, 3, 6, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            this.fillWithOutline(structureWorldAccess, boundingBox, 6, 1, 7, 7, 3, 7, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            this.fillWithOutline(structureWorldAccess, boundingBox, 7, 1, 6, 7, 3, 6, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            this.fillWithOutline(structureWorldAccess, boundingBox, 6, 1, 0, 7, 3, 0, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            this.fillWithOutline(structureWorldAccess, boundingBox, 7, 1, 1, 7, 3, 1, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            this.addBlock(structureWorldAccess, PRISMARINE, 1, 2, 0, boundingBox);
            this.addBlock(structureWorldAccess, PRISMARINE, 0, 2, 1, boundingBox);
            this.addBlock(structureWorldAccess, PRISMARINE, 1, 2, 7, boundingBox);
            this.addBlock(structureWorldAccess, PRISMARINE, 0, 2, 6, boundingBox);
            this.addBlock(structureWorldAccess, PRISMARINE, 6, 2, 7, boundingBox);
            this.addBlock(structureWorldAccess, PRISMARINE, 7, 2, 6, boundingBox);
            this.addBlock(structureWorldAccess, PRISMARINE, 6, 2, 0, boundingBox);
            this.addBlock(structureWorldAccess, PRISMARINE, 7, 2, 1, boundingBox);
            if (!this.setting.neighborPresences[Direction.SOUTH.getId()]) {
               this.fillWithOutline(structureWorldAccess, boundingBox, 1, 3, 0, 6, 3, 0, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
               this.fillWithOutline(structureWorldAccess, boundingBox, 1, 2, 0, 6, 2, 0, PRISMARINE, PRISMARINE, false);
               this.fillWithOutline(structureWorldAccess, boundingBox, 1, 1, 0, 6, 1, 0, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            }

            if (!this.setting.neighborPresences[Direction.NORTH.getId()]) {
               this.fillWithOutline(structureWorldAccess, boundingBox, 1, 3, 7, 6, 3, 7, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
               this.fillWithOutline(structureWorldAccess, boundingBox, 1, 2, 7, 6, 2, 7, PRISMARINE, PRISMARINE, false);
               this.fillWithOutline(structureWorldAccess, boundingBox, 1, 1, 7, 6, 1, 7, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            }

            if (!this.setting.neighborPresences[Direction.WEST.getId()]) {
               this.fillWithOutline(structureWorldAccess, boundingBox, 0, 3, 1, 0, 3, 6, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
               this.fillWithOutline(structureWorldAccess, boundingBox, 0, 2, 1, 0, 2, 6, PRISMARINE, PRISMARINE, false);
               this.fillWithOutline(structureWorldAccess, boundingBox, 0, 1, 1, 0, 1, 6, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            }

            if (!this.setting.neighborPresences[Direction.EAST.getId()]) {
               this.fillWithOutline(structureWorldAccess, boundingBox, 7, 3, 1, 7, 3, 6, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
               this.fillWithOutline(structureWorldAccess, boundingBox, 7, 2, 1, 7, 2, 6, PRISMARINE, PRISMARINE, false);
               this.fillWithOutline(structureWorldAccess, boundingBox, 7, 1, 1, 7, 1, 6, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            }
         } else if (this.field_14480 == 2) {
            this.fillWithOutline(structureWorldAccess, boundingBox, 0, 1, 0, 0, 1, 7, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            this.fillWithOutline(structureWorldAccess, boundingBox, 7, 1, 0, 7, 1, 7, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            this.fillWithOutline(structureWorldAccess, boundingBox, 1, 1, 0, 6, 1, 0, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            this.fillWithOutline(structureWorldAccess, boundingBox, 1, 1, 7, 6, 1, 7, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            this.fillWithOutline(structureWorldAccess, boundingBox, 0, 2, 0, 0, 2, 7, DARK_PRISMARINE, DARK_PRISMARINE, false);
            this.fillWithOutline(structureWorldAccess, boundingBox, 7, 2, 0, 7, 2, 7, DARK_PRISMARINE, DARK_PRISMARINE, false);
            this.fillWithOutline(structureWorldAccess, boundingBox, 1, 2, 0, 6, 2, 0, DARK_PRISMARINE, DARK_PRISMARINE, false);
            this.fillWithOutline(structureWorldAccess, boundingBox, 1, 2, 7, 6, 2, 7, DARK_PRISMARINE, DARK_PRISMARINE, false);
            this.fillWithOutline(structureWorldAccess, boundingBox, 0, 3, 0, 0, 3, 7, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            this.fillWithOutline(structureWorldAccess, boundingBox, 7, 3, 0, 7, 3, 7, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            this.fillWithOutline(structureWorldAccess, boundingBox, 1, 3, 0, 6, 3, 0, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            this.fillWithOutline(structureWorldAccess, boundingBox, 1, 3, 7, 6, 3, 7, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            this.fillWithOutline(structureWorldAccess, boundingBox, 0, 1, 3, 0, 2, 4, DARK_PRISMARINE, DARK_PRISMARINE, false);
            this.fillWithOutline(structureWorldAccess, boundingBox, 7, 1, 3, 7, 2, 4, DARK_PRISMARINE, DARK_PRISMARINE, false);
            this.fillWithOutline(structureWorldAccess, boundingBox, 3, 1, 0, 4, 2, 0, DARK_PRISMARINE, DARK_PRISMARINE, false);
            this.fillWithOutline(structureWorldAccess, boundingBox, 3, 1, 7, 4, 2, 7, DARK_PRISMARINE, DARK_PRISMARINE, false);
            if (this.setting.neighborPresences[Direction.SOUTH.getId()]) {
               this.setAirAndWater(structureWorldAccess, boundingBox, 3, 1, 0, 4, 2, 0);
            }

            if (this.setting.neighborPresences[Direction.NORTH.getId()]) {
               this.setAirAndWater(structureWorldAccess, boundingBox, 3, 1, 7, 4, 2, 7);
            }

            if (this.setting.neighborPresences[Direction.WEST.getId()]) {
               this.setAirAndWater(structureWorldAccess, boundingBox, 0, 1, 3, 0, 2, 4);
            }

            if (this.setting.neighborPresences[Direction.EAST.getId()]) {
               this.setAirAndWater(structureWorldAccess, boundingBox, 7, 1, 3, 7, 2, 4);
            }
         }

         if (bl) {
            this.fillWithOutline(structureWorldAccess, boundingBox, 3, 1, 3, 4, 1, 4, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            this.fillWithOutline(structureWorldAccess, boundingBox, 3, 2, 3, 4, 2, 4, PRISMARINE, PRISMARINE, false);
            this.fillWithOutline(structureWorldAccess, boundingBox, 3, 3, 3, 4, 3, 4, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
         }

         return true;
      }
   }

   public static class Entry extends OceanMonumentGenerator.Piece {
      public Entry(Direction direction, OceanMonumentGenerator.PieceSetting pieceSetting) {
         super(StructurePieceType.OCEAN_MONUMENT_ENTRY_ROOM, 1, direction, pieceSetting, 1, 1, 1);
      }

      public Entry(StructureManager structureManager, CompoundTag compoundTag) {
         super(StructurePieceType.OCEAN_MONUMENT_ENTRY_ROOM, compoundTag);
      }

      public boolean generate(StructureWorldAccess structureWorldAccess, StructureAccessor structureAccessor, ChunkGenerator chunkGenerator, Random random, BlockBox boundingBox, ChunkPos chunkPos, BlockPos blockPos) {
         this.fillWithOutline(structureWorldAccess, boundingBox, 0, 3, 0, 2, 3, 7, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
         this.fillWithOutline(structureWorldAccess, boundingBox, 5, 3, 0, 7, 3, 7, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
         this.fillWithOutline(structureWorldAccess, boundingBox, 0, 2, 0, 1, 2, 7, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
         this.fillWithOutline(structureWorldAccess, boundingBox, 6, 2, 0, 7, 2, 7, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
         this.fillWithOutline(structureWorldAccess, boundingBox, 0, 1, 0, 0, 1, 7, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
         this.fillWithOutline(structureWorldAccess, boundingBox, 7, 1, 0, 7, 1, 7, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
         this.fillWithOutline(structureWorldAccess, boundingBox, 0, 1, 7, 7, 3, 7, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
         this.fillWithOutline(structureWorldAccess, boundingBox, 1, 1, 0, 2, 3, 0, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
         this.fillWithOutline(structureWorldAccess, boundingBox, 5, 1, 0, 6, 3, 0, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
         if (this.setting.neighborPresences[Direction.NORTH.getId()]) {
            this.setAirAndWater(structureWorldAccess, boundingBox, 3, 1, 7, 4, 2, 7);
         }

         if (this.setting.neighborPresences[Direction.WEST.getId()]) {
            this.setAirAndWater(structureWorldAccess, boundingBox, 0, 1, 3, 1, 2, 4);
         }

         if (this.setting.neighborPresences[Direction.EAST.getId()]) {
            this.setAirAndWater(structureWorldAccess, boundingBox, 6, 1, 3, 7, 2, 4);
         }

         return true;
      }
   }

   public static class Base extends OceanMonumentGenerator.Piece {
      private OceanMonumentGenerator.PieceSetting field_14464;
      private OceanMonumentGenerator.PieceSetting field_14466;
      private final List<OceanMonumentGenerator.Piece> field_14465 = Lists.newArrayList();

      public Base(Random random, int i, int j, Direction direction) {
         super(StructurePieceType.OCEAN_MONUMENT_BASE, 0);
         this.setOrientation(direction);
         Direction direction2 = this.getFacing();
         if (direction2.getAxis() == Direction.Axis.Z) {
            this.boundingBox = new BlockBox(i, 39, j, i + 58 - 1, 61, j + 58 - 1);
         } else {
            this.boundingBox = new BlockBox(i, 39, j, i + 58 - 1, 61, j + 58 - 1);
         }

         List<OceanMonumentGenerator.PieceSetting> list = this.method_14760(random);
         this.field_14464.used = true;
         this.field_14465.add(new OceanMonumentGenerator.Entry(direction2, this.field_14464));
         this.field_14465.add(new OceanMonumentGenerator.CoreRoom(direction2, this.field_14466));
         List<OceanMonumentGenerator.PieceFactory> list2 = Lists.newArrayList();
         list2.add(new OceanMonumentGenerator.DoubleXYRoomFactory());
         list2.add(new OceanMonumentGenerator.DoubleYZRoomFactory());
         list2.add(new OceanMonumentGenerator.DoubleZRoomFactory());
         list2.add(new OceanMonumentGenerator.DoubleXRoomFactory());
         list2.add(new OceanMonumentGenerator.DoubleYRoomFactory());
         list2.add(new OceanMonumentGenerator.SimpleRoomTopFactory());
         list2.add(new OceanMonumentGenerator.SimpleRoomFactory());
         Iterator var8 = list.iterator();

         while(true) {
            while(true) {
               OceanMonumentGenerator.PieceSetting pieceSetting;
               do {
                  do {
                     if (!var8.hasNext()) {
                        int k = this.boundingBox.minY;
                        int l = this.applyXTransform(9, 22);
                        int m = this.applyZTransform(9, 22);
                        Iterator var18 = this.field_14465.iterator();

                        while(var18.hasNext()) {
                           OceanMonumentGenerator.Piece piece = (OceanMonumentGenerator.Piece)var18.next();
                           piece.getBoundingBox().move(l, k, m);
                        }

                        BlockBox blockBox = BlockBox.create(this.applyXTransform(1, 1), this.applyYTransform(1), this.applyZTransform(1, 1), this.applyXTransform(23, 21), this.applyYTransform(8), this.applyZTransform(23, 21));
                        BlockBox blockBox2 = BlockBox.create(this.applyXTransform(34, 1), this.applyYTransform(1), this.applyZTransform(34, 1), this.applyXTransform(56, 21), this.applyYTransform(8), this.applyZTransform(56, 21));
                        BlockBox blockBox3 = BlockBox.create(this.applyXTransform(22, 22), this.applyYTransform(13), this.applyZTransform(22, 22), this.applyXTransform(35, 35), this.applyYTransform(17), this.applyZTransform(35, 35));
                        int n = random.nextInt();
                        this.field_14465.add(new OceanMonumentGenerator.WingRoom(direction2, blockBox, n++));
                        this.field_14465.add(new OceanMonumentGenerator.WingRoom(direction2, blockBox2, n++));
                        this.field_14465.add(new OceanMonumentGenerator.Penthouse(direction2, blockBox3));
                        return;
                     }

                     pieceSetting = (OceanMonumentGenerator.PieceSetting)var8.next();
                  } while(pieceSetting.used);
               } while(pieceSetting.isAboveLevelThree());

               Iterator var10 = list2.iterator();

               while(var10.hasNext()) {
                  OceanMonumentGenerator.PieceFactory pieceFactory = (OceanMonumentGenerator.PieceFactory)var10.next();
                  if (pieceFactory.canGenerate(pieceSetting)) {
                     this.field_14465.add(pieceFactory.generate(direction2, pieceSetting, random));
                     break;
                  }
               }
            }
         }
      }

      public Base(StructureManager structureManager, CompoundTag compoundTag) {
         super(StructurePieceType.OCEAN_MONUMENT_BASE, compoundTag);
      }

      private List<OceanMonumentGenerator.PieceSetting> method_14760(Random random) {
         OceanMonumentGenerator.PieceSetting[] pieceSettings = new OceanMonumentGenerator.PieceSetting[75];

         int u;
         int v;
         boolean s;
         int x;
         for(u = 0; u < 5; ++u) {
            for(v = 0; v < 4; ++v) {
               s = false;
               x = getIndex(u, 0, v);
               pieceSettings[x] = new OceanMonumentGenerator.PieceSetting(x);
            }
         }

         for(u = 0; u < 5; ++u) {
            for(v = 0; v < 4; ++v) {
               s = true;
               x = getIndex(u, 1, v);
               pieceSettings[x] = new OceanMonumentGenerator.PieceSetting(x);
            }
         }

         for(u = 1; u < 4; ++u) {
            for(v = 0; v < 2; ++v) {
               s = true;
               x = getIndex(u, 2, v);
               pieceSettings[x] = new OceanMonumentGenerator.PieceSetting(x);
            }
         }

         this.field_14464 = pieceSettings[TWO_ZERO_ZERO_INDEX];

         int var8;
         int var9;
         int y;
         int af;
         int aa;
         for(u = 0; u < 5; ++u) {
            for(v = 0; v < 5; ++v) {
               for(int w = 0; w < 3; ++w) {
                  x = getIndex(u, w, v);
                  if (pieceSettings[x] != null) {
                     Direction[] var7 = Direction.values();
                     var8 = var7.length;

                     for(var9 = 0; var9 < var8; ++var9) {
                        Direction direction = var7[var9];
                        y = u + direction.getOffsetX();
                        af = w + direction.getOffsetY();
                        aa = v + direction.getOffsetZ();
                        if (y >= 0 && y < 5 && aa >= 0 && aa < 5 && af >= 0 && af < 3) {
                           int ab = getIndex(y, af, aa);
                           if (pieceSettings[ab] != null) {
                              if (aa == v) {
                                 pieceSettings[x].setNeighbor(direction, pieceSettings[ab]);
                              } else {
                                 pieceSettings[x].setNeighbor(direction.getOpposite(), pieceSettings[ab]);
                              }
                           }
                        }
                     }
                  }
               }
            }
         }

         OceanMonumentGenerator.PieceSetting pieceSetting = new OceanMonumentGenerator.PieceSetting(1003);
         OceanMonumentGenerator.PieceSetting pieceSetting2 = new OceanMonumentGenerator.PieceSetting(1001);
         OceanMonumentGenerator.PieceSetting pieceSetting3 = new OceanMonumentGenerator.PieceSetting(1002);
         pieceSettings[TWO_TWO_ZERO_INDEX].setNeighbor(Direction.UP, pieceSetting);
         pieceSettings[ZERO_ONE_ZERO_INDEX].setNeighbor(Direction.SOUTH, pieceSetting2);
         pieceSettings[FOUR_ONE_ZERO_INDEX].setNeighbor(Direction.SOUTH, pieceSetting3);
         pieceSetting.used = true;
         pieceSetting2.used = true;
         pieceSetting3.used = true;
         this.field_14464.field_14484 = true;
         this.field_14466 = pieceSettings[getIndex(random.nextInt(4), 0, 2)];
         this.field_14466.used = true;
         this.field_14466.neighbors[Direction.EAST.getId()].used = true;
         this.field_14466.neighbors[Direction.NORTH.getId()].used = true;
         this.field_14466.neighbors[Direction.EAST.getId()].neighbors[Direction.NORTH.getId()].used = true;
         this.field_14466.neighbors[Direction.UP.getId()].used = true;
         this.field_14466.neighbors[Direction.EAST.getId()].neighbors[Direction.UP.getId()].used = true;
         this.field_14466.neighbors[Direction.NORTH.getId()].neighbors[Direction.UP.getId()].used = true;
         this.field_14466.neighbors[Direction.EAST.getId()].neighbors[Direction.NORTH.getId()].neighbors[Direction.UP.getId()].used = true;
         List<OceanMonumentGenerator.PieceSetting> list = Lists.newArrayList();
         OceanMonumentGenerator.PieceSetting[] var20 = pieceSettings;
         var8 = pieceSettings.length;

         for(var9 = 0; var9 < var8; ++var9) {
            OceanMonumentGenerator.PieceSetting pieceSetting4 = var20[var9];
            if (pieceSetting4 != null) {
               pieceSetting4.checkNeighborStates();
               list.add(pieceSetting4);
            }
         }

         pieceSetting.checkNeighborStates();
         Collections.shuffle(list, random);
         int ac = 1;
         Iterator var22 = list.iterator();

         label95:
         while(var22.hasNext()) {
            OceanMonumentGenerator.PieceSetting pieceSetting5 = (OceanMonumentGenerator.PieceSetting)var22.next();
            int ad = 0;
            y = 0;

            while(true) {
               while(true) {
                  do {
                     if (ad >= 2 || y >= 5) {
                        continue label95;
                     }

                     ++y;
                     af = random.nextInt(6);
                  } while(!pieceSetting5.neighborPresences[af]);

                  aa = Direction.byId(af).getOpposite().getId();
                  pieceSetting5.neighborPresences[af] = false;
                  pieceSetting5.neighbors[af].neighborPresences[aa] = false;
                  if (pieceSetting5.method_14783(ac++) && pieceSetting5.neighbors[af].method_14783(ac++)) {
                     ++ad;
                  } else {
                     pieceSetting5.neighborPresences[af] = true;
                     pieceSetting5.neighbors[af].neighborPresences[aa] = true;
                  }
               }
            }
         }

         list.add(pieceSetting);
         list.add(pieceSetting2);
         list.add(pieceSetting3);
         return list;
      }

      public boolean generate(StructureWorldAccess structureWorldAccess, StructureAccessor structureAccessor, ChunkGenerator chunkGenerator, Random random, BlockBox boundingBox, ChunkPos chunkPos, BlockPos blockPos) {
         int i = Math.max(structureWorldAccess.getSeaLevel(), 64) - this.boundingBox.minY;
         this.setAirAndWater(structureWorldAccess, boundingBox, 0, 0, 0, 58, i, 58);
         this.method_14761(false, 0, structureWorldAccess, random, boundingBox);
         this.method_14761(true, 33, structureWorldAccess, random, boundingBox);
         this.method_14763(structureWorldAccess, random, boundingBox);
         this.method_14762(structureWorldAccess, random, boundingBox);
         this.method_14765(structureWorldAccess, random, boundingBox);
         this.method_14764(structureWorldAccess, random, boundingBox);
         this.method_14766(structureWorldAccess, random, boundingBox);
         this.method_14767(structureWorldAccess, random, boundingBox);

         int j;
         label72:
         for(j = 0; j < 7; ++j) {
            int k = 0;

            while(true) {
               while(true) {
                  if (k >= 7) {
                     continue label72;
                  }

                  if (k == 0 && j == 3) {
                     k = 6;
                  }

                  int l = j * 9;
                  int m = k * 9;

                  for(int n = 0; n < 4; ++n) {
                     for(int o = 0; o < 4; ++o) {
                        this.addBlock(structureWorldAccess, PRISMARINE_BRICKS, l + n, 0, m + o, boundingBox);
                        this.fillDownwards(structureWorldAccess, PRISMARINE_BRICKS, l + n, -1, m + o, boundingBox);
                     }
                  }

                  if (j != 0 && j != 6) {
                     k += 6;
                  } else {
                     ++k;
                  }
               }
            }
         }

         for(j = 0; j < 5; ++j) {
            this.setAirAndWater(structureWorldAccess, boundingBox, -1 - j, 0 + j * 2, -1 - j, -1 - j, 23, 58 + j);
            this.setAirAndWater(structureWorldAccess, boundingBox, 58 + j, 0 + j * 2, -1 - j, 58 + j, 23, 58 + j);
            this.setAirAndWater(structureWorldAccess, boundingBox, 0 - j, 0 + j * 2, -1 - j, 57 + j, 23, -1 - j);
            this.setAirAndWater(structureWorldAccess, boundingBox, 0 - j, 0 + j * 2, 58 + j, 57 + j, 23, 58 + j);
         }

         Iterator var15 = this.field_14465.iterator();

         while(var15.hasNext()) {
            OceanMonumentGenerator.Piece piece = (OceanMonumentGenerator.Piece)var15.next();
            if (piece.getBoundingBox().intersects(boundingBox)) {
               piece.generate(structureWorldAccess, structureAccessor, chunkGenerator, random, boundingBox, chunkPos, blockPos);
            }
         }

         return true;
      }

      private void method_14761(boolean bl, int i, StructureWorldAccess structureWorldAccess, Random random, BlockBox blockBox) {
         int j = true;
         if (this.method_14775(blockBox, i, 0, i + 23, 20)) {
            this.fillWithOutline(structureWorldAccess, blockBox, i + 0, 0, 0, i + 24, 0, 20, PRISMARINE, PRISMARINE, false);
            this.setAirAndWater(structureWorldAccess, blockBox, i + 0, 1, 0, i + 24, 10, 20);

            int l;
            for(l = 0; l < 4; ++l) {
               this.fillWithOutline(structureWorldAccess, blockBox, i + l, l + 1, l, i + l, l + 1, 20, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
               this.fillWithOutline(structureWorldAccess, blockBox, i + l + 7, l + 5, l + 7, i + l + 7, l + 5, 20, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
               this.fillWithOutline(structureWorldAccess, blockBox, i + 17 - l, l + 5, l + 7, i + 17 - l, l + 5, 20, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
               this.fillWithOutline(structureWorldAccess, blockBox, i + 24 - l, l + 1, l, i + 24 - l, l + 1, 20, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
               this.fillWithOutline(structureWorldAccess, blockBox, i + l + 1, l + 1, l, i + 23 - l, l + 1, l, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
               this.fillWithOutline(structureWorldAccess, blockBox, i + l + 8, l + 5, l + 7, i + 16 - l, l + 5, l + 7, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            }

            this.fillWithOutline(structureWorldAccess, blockBox, i + 4, 4, 4, i + 6, 4, 20, PRISMARINE, PRISMARINE, false);
            this.fillWithOutline(structureWorldAccess, blockBox, i + 7, 4, 4, i + 17, 4, 6, PRISMARINE, PRISMARINE, false);
            this.fillWithOutline(structureWorldAccess, blockBox, i + 18, 4, 4, i + 20, 4, 20, PRISMARINE, PRISMARINE, false);
            this.fillWithOutline(structureWorldAccess, blockBox, i + 11, 8, 11, i + 13, 8, 20, PRISMARINE, PRISMARINE, false);
            this.addBlock(structureWorldAccess, field_14470, i + 12, 9, 12, blockBox);
            this.addBlock(structureWorldAccess, field_14470, i + 12, 9, 15, blockBox);
            this.addBlock(structureWorldAccess, field_14470, i + 12, 9, 18, blockBox);
            l = i + (bl ? 19 : 5);
            int m = i + (bl ? 5 : 19);

            int p;
            for(p = 20; p >= 5; p -= 3) {
               this.addBlock(structureWorldAccess, field_14470, l, 5, p, blockBox);
            }

            for(p = 19; p >= 7; p -= 3) {
               this.addBlock(structureWorldAccess, field_14470, m, 5, p, blockBox);
            }

            for(p = 0; p < 4; ++p) {
               int q = bl ? i + 24 - (17 - p * 3) : i + 17 - p * 3;
               this.addBlock(structureWorldAccess, field_14470, q, 5, 5, blockBox);
            }

            this.addBlock(structureWorldAccess, field_14470, m, 5, 5, blockBox);
            this.fillWithOutline(structureWorldAccess, blockBox, i + 11, 1, 12, i + 13, 7, 12, PRISMARINE, PRISMARINE, false);
            this.fillWithOutline(structureWorldAccess, blockBox, i + 12, 1, 11, i + 12, 7, 13, PRISMARINE, PRISMARINE, false);
         }

      }

      private void method_14763(StructureWorldAccess structureWorldAccess, Random random, BlockBox blockBox) {
         if (this.method_14775(blockBox, 22, 5, 35, 17)) {
            this.setAirAndWater(structureWorldAccess, blockBox, 25, 0, 0, 32, 8, 20);

            for(int i = 0; i < 4; ++i) {
               this.fillWithOutline(structureWorldAccess, blockBox, 24, 2, 5 + i * 4, 24, 4, 5 + i * 4, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
               this.fillWithOutline(structureWorldAccess, blockBox, 22, 4, 5 + i * 4, 23, 4, 5 + i * 4, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
               this.addBlock(structureWorldAccess, PRISMARINE_BRICKS, 25, 5, 5 + i * 4, blockBox);
               this.addBlock(structureWorldAccess, PRISMARINE_BRICKS, 26, 6, 5 + i * 4, blockBox);
               this.addBlock(structureWorldAccess, SEA_LANTERN, 26, 5, 5 + i * 4, blockBox);
               this.fillWithOutline(structureWorldAccess, blockBox, 33, 2, 5 + i * 4, 33, 4, 5 + i * 4, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
               this.fillWithOutline(structureWorldAccess, blockBox, 34, 4, 5 + i * 4, 35, 4, 5 + i * 4, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
               this.addBlock(structureWorldAccess, PRISMARINE_BRICKS, 32, 5, 5 + i * 4, blockBox);
               this.addBlock(structureWorldAccess, PRISMARINE_BRICKS, 31, 6, 5 + i * 4, blockBox);
               this.addBlock(structureWorldAccess, SEA_LANTERN, 31, 5, 5 + i * 4, blockBox);
               this.fillWithOutline(structureWorldAccess, blockBox, 27, 6, 5 + i * 4, 30, 6, 5 + i * 4, PRISMARINE, PRISMARINE, false);
            }
         }

      }

      private void method_14762(StructureWorldAccess structureWorldAccess, Random random, BlockBox blockBox) {
         if (this.method_14775(blockBox, 15, 20, 42, 21)) {
            this.fillWithOutline(structureWorldAccess, blockBox, 15, 0, 21, 42, 0, 21, PRISMARINE, PRISMARINE, false);
            this.setAirAndWater(structureWorldAccess, blockBox, 26, 1, 21, 31, 3, 21);
            this.fillWithOutline(structureWorldAccess, blockBox, 21, 12, 21, 36, 12, 21, PRISMARINE, PRISMARINE, false);
            this.fillWithOutline(structureWorldAccess, blockBox, 17, 11, 21, 40, 11, 21, PRISMARINE, PRISMARINE, false);
            this.fillWithOutline(structureWorldAccess, blockBox, 16, 10, 21, 41, 10, 21, PRISMARINE, PRISMARINE, false);
            this.fillWithOutline(structureWorldAccess, blockBox, 15, 7, 21, 42, 9, 21, PRISMARINE, PRISMARINE, false);
            this.fillWithOutline(structureWorldAccess, blockBox, 16, 6, 21, 41, 6, 21, PRISMARINE, PRISMARINE, false);
            this.fillWithOutline(structureWorldAccess, blockBox, 17, 5, 21, 40, 5, 21, PRISMARINE, PRISMARINE, false);
            this.fillWithOutline(structureWorldAccess, blockBox, 21, 4, 21, 36, 4, 21, PRISMARINE, PRISMARINE, false);
            this.fillWithOutline(structureWorldAccess, blockBox, 22, 3, 21, 26, 3, 21, PRISMARINE, PRISMARINE, false);
            this.fillWithOutline(structureWorldAccess, blockBox, 31, 3, 21, 35, 3, 21, PRISMARINE, PRISMARINE, false);
            this.fillWithOutline(structureWorldAccess, blockBox, 23, 2, 21, 25, 2, 21, PRISMARINE, PRISMARINE, false);
            this.fillWithOutline(structureWorldAccess, blockBox, 32, 2, 21, 34, 2, 21, PRISMARINE, PRISMARINE, false);
            this.fillWithOutline(structureWorldAccess, blockBox, 28, 4, 20, 29, 4, 21, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            this.addBlock(structureWorldAccess, PRISMARINE_BRICKS, 27, 3, 21, blockBox);
            this.addBlock(structureWorldAccess, PRISMARINE_BRICKS, 30, 3, 21, blockBox);
            this.addBlock(structureWorldAccess, PRISMARINE_BRICKS, 26, 2, 21, blockBox);
            this.addBlock(structureWorldAccess, PRISMARINE_BRICKS, 31, 2, 21, blockBox);
            this.addBlock(structureWorldAccess, PRISMARINE_BRICKS, 25, 1, 21, blockBox);
            this.addBlock(structureWorldAccess, PRISMARINE_BRICKS, 32, 1, 21, blockBox);

            int k;
            for(k = 0; k < 7; ++k) {
               this.addBlock(structureWorldAccess, DARK_PRISMARINE, 28 - k, 6 + k, 21, blockBox);
               this.addBlock(structureWorldAccess, DARK_PRISMARINE, 29 + k, 6 + k, 21, blockBox);
            }

            for(k = 0; k < 4; ++k) {
               this.addBlock(structureWorldAccess, DARK_PRISMARINE, 28 - k, 9 + k, 21, blockBox);
               this.addBlock(structureWorldAccess, DARK_PRISMARINE, 29 + k, 9 + k, 21, blockBox);
            }

            this.addBlock(structureWorldAccess, DARK_PRISMARINE, 28, 12, 21, blockBox);
            this.addBlock(structureWorldAccess, DARK_PRISMARINE, 29, 12, 21, blockBox);

            for(k = 0; k < 3; ++k) {
               this.addBlock(structureWorldAccess, DARK_PRISMARINE, 22 - k * 2, 8, 21, blockBox);
               this.addBlock(structureWorldAccess, DARK_PRISMARINE, 22 - k * 2, 9, 21, blockBox);
               this.addBlock(structureWorldAccess, DARK_PRISMARINE, 35 + k * 2, 8, 21, blockBox);
               this.addBlock(structureWorldAccess, DARK_PRISMARINE, 35 + k * 2, 9, 21, blockBox);
            }

            this.setAirAndWater(structureWorldAccess, blockBox, 15, 13, 21, 42, 15, 21);
            this.setAirAndWater(structureWorldAccess, blockBox, 15, 1, 21, 15, 6, 21);
            this.setAirAndWater(structureWorldAccess, blockBox, 16, 1, 21, 16, 5, 21);
            this.setAirAndWater(structureWorldAccess, blockBox, 17, 1, 21, 20, 4, 21);
            this.setAirAndWater(structureWorldAccess, blockBox, 21, 1, 21, 21, 3, 21);
            this.setAirAndWater(structureWorldAccess, blockBox, 22, 1, 21, 22, 2, 21);
            this.setAirAndWater(structureWorldAccess, blockBox, 23, 1, 21, 24, 1, 21);
            this.setAirAndWater(structureWorldAccess, blockBox, 42, 1, 21, 42, 6, 21);
            this.setAirAndWater(structureWorldAccess, blockBox, 41, 1, 21, 41, 5, 21);
            this.setAirAndWater(structureWorldAccess, blockBox, 37, 1, 21, 40, 4, 21);
            this.setAirAndWater(structureWorldAccess, blockBox, 36, 1, 21, 36, 3, 21);
            this.setAirAndWater(structureWorldAccess, blockBox, 33, 1, 21, 34, 1, 21);
            this.setAirAndWater(structureWorldAccess, blockBox, 35, 1, 21, 35, 2, 21);
         }

      }

      private void method_14765(StructureWorldAccess structureWorldAccess, Random random, BlockBox blockBox) {
         if (this.method_14775(blockBox, 21, 21, 36, 36)) {
            this.fillWithOutline(structureWorldAccess, blockBox, 21, 0, 22, 36, 0, 36, PRISMARINE, PRISMARINE, false);
            this.setAirAndWater(structureWorldAccess, blockBox, 21, 1, 22, 36, 23, 36);

            for(int i = 0; i < 4; ++i) {
               this.fillWithOutline(structureWorldAccess, blockBox, 21 + i, 13 + i, 21 + i, 36 - i, 13 + i, 21 + i, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
               this.fillWithOutline(structureWorldAccess, blockBox, 21 + i, 13 + i, 36 - i, 36 - i, 13 + i, 36 - i, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
               this.fillWithOutline(structureWorldAccess, blockBox, 21 + i, 13 + i, 22 + i, 21 + i, 13 + i, 35 - i, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
               this.fillWithOutline(structureWorldAccess, blockBox, 36 - i, 13 + i, 22 + i, 36 - i, 13 + i, 35 - i, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            }

            this.fillWithOutline(structureWorldAccess, blockBox, 25, 16, 25, 32, 16, 32, PRISMARINE, PRISMARINE, false);
            this.fillWithOutline(structureWorldAccess, blockBox, 25, 17, 25, 25, 19, 25, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            this.fillWithOutline(structureWorldAccess, blockBox, 32, 17, 25, 32, 19, 25, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            this.fillWithOutline(structureWorldAccess, blockBox, 25, 17, 32, 25, 19, 32, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            this.fillWithOutline(structureWorldAccess, blockBox, 32, 17, 32, 32, 19, 32, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            this.addBlock(structureWorldAccess, PRISMARINE_BRICKS, 26, 20, 26, blockBox);
            this.addBlock(structureWorldAccess, PRISMARINE_BRICKS, 27, 21, 27, blockBox);
            this.addBlock(structureWorldAccess, SEA_LANTERN, 27, 20, 27, blockBox);
            this.addBlock(structureWorldAccess, PRISMARINE_BRICKS, 26, 20, 31, blockBox);
            this.addBlock(structureWorldAccess, PRISMARINE_BRICKS, 27, 21, 30, blockBox);
            this.addBlock(structureWorldAccess, SEA_LANTERN, 27, 20, 30, blockBox);
            this.addBlock(structureWorldAccess, PRISMARINE_BRICKS, 31, 20, 31, blockBox);
            this.addBlock(structureWorldAccess, PRISMARINE_BRICKS, 30, 21, 30, blockBox);
            this.addBlock(structureWorldAccess, SEA_LANTERN, 30, 20, 30, blockBox);
            this.addBlock(structureWorldAccess, PRISMARINE_BRICKS, 31, 20, 26, blockBox);
            this.addBlock(structureWorldAccess, PRISMARINE_BRICKS, 30, 21, 27, blockBox);
            this.addBlock(structureWorldAccess, SEA_LANTERN, 30, 20, 27, blockBox);
            this.fillWithOutline(structureWorldAccess, blockBox, 28, 21, 27, 29, 21, 27, PRISMARINE, PRISMARINE, false);
            this.fillWithOutline(structureWorldAccess, blockBox, 27, 21, 28, 27, 21, 29, PRISMARINE, PRISMARINE, false);
            this.fillWithOutline(structureWorldAccess, blockBox, 28, 21, 30, 29, 21, 30, PRISMARINE, PRISMARINE, false);
            this.fillWithOutline(structureWorldAccess, blockBox, 30, 21, 28, 30, 21, 29, PRISMARINE, PRISMARINE, false);
         }

      }

      private void method_14764(StructureWorldAccess structureWorldAccess, Random random, BlockBox blockBox) {
         int k;
         if (this.method_14775(blockBox, 0, 21, 6, 58)) {
            this.fillWithOutline(structureWorldAccess, blockBox, 0, 0, 21, 6, 0, 57, PRISMARINE, PRISMARINE, false);
            this.setAirAndWater(structureWorldAccess, blockBox, 0, 1, 21, 6, 7, 57);
            this.fillWithOutline(structureWorldAccess, blockBox, 4, 4, 21, 6, 4, 53, PRISMARINE, PRISMARINE, false);

            for(k = 0; k < 4; ++k) {
               this.fillWithOutline(structureWorldAccess, blockBox, k, k + 1, 21, k, k + 1, 57 - k, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            }

            for(k = 23; k < 53; k += 3) {
               this.addBlock(structureWorldAccess, field_14470, 5, 5, k, blockBox);
            }

            this.addBlock(structureWorldAccess, field_14470, 5, 5, 52, blockBox);

            for(k = 0; k < 4; ++k) {
               this.fillWithOutline(structureWorldAccess, blockBox, k, k + 1, 21, k, k + 1, 57 - k, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            }

            this.fillWithOutline(structureWorldAccess, blockBox, 4, 1, 52, 6, 3, 52, PRISMARINE, PRISMARINE, false);
            this.fillWithOutline(structureWorldAccess, blockBox, 5, 1, 51, 5, 3, 53, PRISMARINE, PRISMARINE, false);
         }

         if (this.method_14775(blockBox, 51, 21, 58, 58)) {
            this.fillWithOutline(structureWorldAccess, blockBox, 51, 0, 21, 57, 0, 57, PRISMARINE, PRISMARINE, false);
            this.setAirAndWater(structureWorldAccess, blockBox, 51, 1, 21, 57, 7, 57);
            this.fillWithOutline(structureWorldAccess, blockBox, 51, 4, 21, 53, 4, 53, PRISMARINE, PRISMARINE, false);

            for(k = 0; k < 4; ++k) {
               this.fillWithOutline(structureWorldAccess, blockBox, 57 - k, k + 1, 21, 57 - k, k + 1, 57 - k, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            }

            for(k = 23; k < 53; k += 3) {
               this.addBlock(structureWorldAccess, field_14470, 52, 5, k, blockBox);
            }

            this.addBlock(structureWorldAccess, field_14470, 52, 5, 52, blockBox);
            this.fillWithOutline(structureWorldAccess, blockBox, 51, 1, 52, 53, 3, 52, PRISMARINE, PRISMARINE, false);
            this.fillWithOutline(structureWorldAccess, blockBox, 52, 1, 51, 52, 3, 53, PRISMARINE, PRISMARINE, false);
         }

         if (this.method_14775(blockBox, 0, 51, 57, 57)) {
            this.fillWithOutline(structureWorldAccess, blockBox, 7, 0, 51, 50, 0, 57, PRISMARINE, PRISMARINE, false);
            this.setAirAndWater(structureWorldAccess, blockBox, 7, 1, 51, 50, 10, 57);

            for(k = 0; k < 4; ++k) {
               this.fillWithOutline(structureWorldAccess, blockBox, k + 1, k + 1, 57 - k, 56 - k, k + 1, 57 - k, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            }
         }

      }

      private void method_14766(StructureWorldAccess structureWorldAccess, Random random, BlockBox blockBox) {
         int n;
         if (this.method_14775(blockBox, 7, 21, 13, 50)) {
            this.fillWithOutline(structureWorldAccess, blockBox, 7, 0, 21, 13, 0, 50, PRISMARINE, PRISMARINE, false);
            this.setAirAndWater(structureWorldAccess, blockBox, 7, 1, 21, 13, 10, 50);
            this.fillWithOutline(structureWorldAccess, blockBox, 11, 8, 21, 13, 8, 53, PRISMARINE, PRISMARINE, false);

            for(n = 0; n < 4; ++n) {
               this.fillWithOutline(structureWorldAccess, blockBox, n + 7, n + 5, 21, n + 7, n + 5, 54, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            }

            for(n = 21; n <= 45; n += 3) {
               this.addBlock(structureWorldAccess, field_14470, 12, 9, n, blockBox);
            }
         }

         if (this.method_14775(blockBox, 44, 21, 50, 54)) {
            this.fillWithOutline(structureWorldAccess, blockBox, 44, 0, 21, 50, 0, 50, PRISMARINE, PRISMARINE, false);
            this.setAirAndWater(structureWorldAccess, blockBox, 44, 1, 21, 50, 10, 50);
            this.fillWithOutline(structureWorldAccess, blockBox, 44, 8, 21, 46, 8, 53, PRISMARINE, PRISMARINE, false);

            for(n = 0; n < 4; ++n) {
               this.fillWithOutline(structureWorldAccess, blockBox, 50 - n, n + 5, 21, 50 - n, n + 5, 54, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            }

            for(n = 21; n <= 45; n += 3) {
               this.addBlock(structureWorldAccess, field_14470, 45, 9, n, blockBox);
            }
         }

         if (this.method_14775(blockBox, 8, 44, 49, 54)) {
            this.fillWithOutline(structureWorldAccess, blockBox, 14, 0, 44, 43, 0, 50, PRISMARINE, PRISMARINE, false);
            this.setAirAndWater(structureWorldAccess, blockBox, 14, 1, 44, 43, 10, 50);

            for(n = 12; n <= 45; n += 3) {
               this.addBlock(structureWorldAccess, field_14470, n, 9, 45, blockBox);
               this.addBlock(structureWorldAccess, field_14470, n, 9, 52, blockBox);
               if (n == 12 || n == 18 || n == 24 || n == 33 || n == 39 || n == 45) {
                  this.addBlock(structureWorldAccess, field_14470, n, 9, 47, blockBox);
                  this.addBlock(structureWorldAccess, field_14470, n, 9, 50, blockBox);
                  this.addBlock(structureWorldAccess, field_14470, n, 10, 45, blockBox);
                  this.addBlock(structureWorldAccess, field_14470, n, 10, 46, blockBox);
                  this.addBlock(structureWorldAccess, field_14470, n, 10, 51, blockBox);
                  this.addBlock(structureWorldAccess, field_14470, n, 10, 52, blockBox);
                  this.addBlock(structureWorldAccess, field_14470, n, 11, 47, blockBox);
                  this.addBlock(structureWorldAccess, field_14470, n, 11, 50, blockBox);
                  this.addBlock(structureWorldAccess, field_14470, n, 12, 48, blockBox);
                  this.addBlock(structureWorldAccess, field_14470, n, 12, 49, blockBox);
               }
            }

            for(n = 0; n < 3; ++n) {
               this.fillWithOutline(structureWorldAccess, blockBox, 8 + n, 5 + n, 54, 49 - n, 5 + n, 54, PRISMARINE, PRISMARINE, false);
            }

            this.fillWithOutline(structureWorldAccess, blockBox, 11, 8, 54, 46, 8, 54, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            this.fillWithOutline(structureWorldAccess, blockBox, 14, 8, 44, 43, 8, 53, PRISMARINE, PRISMARINE, false);
         }

      }

      private void method_14767(StructureWorldAccess structureWorldAccess, Random random, BlockBox blockBox) {
         int n;
         if (this.method_14775(blockBox, 14, 21, 20, 43)) {
            this.fillWithOutline(structureWorldAccess, blockBox, 14, 0, 21, 20, 0, 43, PRISMARINE, PRISMARINE, false);
            this.setAirAndWater(structureWorldAccess, blockBox, 14, 1, 22, 20, 14, 43);
            this.fillWithOutline(structureWorldAccess, blockBox, 18, 12, 22, 20, 12, 39, PRISMARINE, PRISMARINE, false);
            this.fillWithOutline(structureWorldAccess, blockBox, 18, 12, 21, 20, 12, 21, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);

            for(n = 0; n < 4; ++n) {
               this.fillWithOutline(structureWorldAccess, blockBox, n + 14, n + 9, 21, n + 14, n + 9, 43 - n, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            }

            for(n = 23; n <= 39; n += 3) {
               this.addBlock(structureWorldAccess, field_14470, 19, 13, n, blockBox);
            }
         }

         if (this.method_14775(blockBox, 37, 21, 43, 43)) {
            this.fillWithOutline(structureWorldAccess, blockBox, 37, 0, 21, 43, 0, 43, PRISMARINE, PRISMARINE, false);
            this.setAirAndWater(structureWorldAccess, blockBox, 37, 1, 22, 43, 14, 43);
            this.fillWithOutline(structureWorldAccess, blockBox, 37, 12, 22, 39, 12, 39, PRISMARINE, PRISMARINE, false);
            this.fillWithOutline(structureWorldAccess, blockBox, 37, 12, 21, 39, 12, 21, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);

            for(n = 0; n < 4; ++n) {
               this.fillWithOutline(structureWorldAccess, blockBox, 43 - n, n + 9, 21, 43 - n, n + 9, 43 - n, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            }

            for(n = 23; n <= 39; n += 3) {
               this.addBlock(structureWorldAccess, field_14470, 38, 13, n, blockBox);
            }
         }

         if (this.method_14775(blockBox, 15, 37, 42, 43)) {
            this.fillWithOutline(structureWorldAccess, blockBox, 21, 0, 37, 36, 0, 43, PRISMARINE, PRISMARINE, false);
            this.setAirAndWater(structureWorldAccess, blockBox, 21, 1, 37, 36, 14, 43);
            this.fillWithOutline(structureWorldAccess, blockBox, 21, 12, 37, 36, 12, 39, PRISMARINE, PRISMARINE, false);

            for(n = 0; n < 4; ++n) {
               this.fillWithOutline(structureWorldAccess, blockBox, 15 + n, n + 9, 43 - n, 42 - n, n + 9, 43 - n, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            }

            for(n = 21; n <= 36; n += 3) {
               this.addBlock(structureWorldAccess, field_14470, n, 13, 38, blockBox);
            }
         }

      }
   }

   public abstract static class Piece extends StructurePiece {
      protected static final BlockState PRISMARINE;
      protected static final BlockState PRISMARINE_BRICKS;
      protected static final BlockState DARK_PRISMARINE;
      protected static final BlockState field_14470;
      protected static final BlockState SEA_LANTERN;
      protected static final BlockState WATER;
      protected static final Set<Block> ICE_BLOCKS;
      protected static final int TWO_ZERO_ZERO_INDEX;
      protected static final int TWO_TWO_ZERO_INDEX;
      protected static final int ZERO_ONE_ZERO_INDEX;
      protected static final int FOUR_ONE_ZERO_INDEX;
      protected OceanMonumentGenerator.PieceSetting setting;

      protected static final int getIndex(int x, int y, int z) {
         return y * 25 + z * 5 + x;
      }

      public Piece(StructurePieceType structurePieceType, int i) {
         super(structurePieceType, i);
      }

      public Piece(StructurePieceType structurePieceType, Direction direction, BlockBox blockBox) {
         super(structurePieceType, 1);
         this.setOrientation(direction);
         this.boundingBox = blockBox;
      }

      protected Piece(StructurePieceType structurePieceType, int i, Direction direction, OceanMonumentGenerator.PieceSetting pieceSetting, int j, int k, int l) {
         super(structurePieceType, i);
         this.setOrientation(direction);
         this.setting = pieceSetting;
         int m = pieceSetting.roomIndex;
         int n = m % 5;
         int o = m / 5 % 5;
         int p = m / 25;
         if (direction != Direction.NORTH && direction != Direction.SOUTH) {
            this.boundingBox = new BlockBox(0, 0, 0, l * 8 - 1, k * 4 - 1, j * 8 - 1);
         } else {
            this.boundingBox = new BlockBox(0, 0, 0, j * 8 - 1, k * 4 - 1, l * 8 - 1);
         }

         switch(direction) {
         case NORTH:
            this.boundingBox.move(n * 8, p * 4, -(o + l) * 8 + 1);
            break;
         case SOUTH:
            this.boundingBox.move(n * 8, p * 4, o * 8);
            break;
         case WEST:
            this.boundingBox.move(-(o + l) * 8 + 1, p * 4, n * 8);
            break;
         default:
            this.boundingBox.move(o * 8, p * 4, n * 8);
         }

      }

      public Piece(StructurePieceType structurePieceType, CompoundTag compoundTag) {
         super(structurePieceType, compoundTag);
      }

      protected void toNbt(CompoundTag tag) {
      }

      protected void setAirAndWater(StructureWorldAccess structureWorldAccess, BlockBox blockBox, int x, int y, int z, int width, int height, int depth) {
         for(int i = y; i <= height; ++i) {
            for(int j = x; j <= width; ++j) {
               for(int k = z; k <= depth; ++k) {
                  BlockState blockState = this.getBlockAt(structureWorldAccess, j, i, k, blockBox);
                  if (!ICE_BLOCKS.contains(blockState.getBlock())) {
                     if (this.applyYTransform(i) >= structureWorldAccess.getSeaLevel() && blockState != WATER) {
                        this.addBlock(structureWorldAccess, Blocks.AIR.getDefaultState(), j, i, k, blockBox);
                     } else {
                        this.addBlock(structureWorldAccess, WATER, j, i, k, blockBox);
                     }
                  }
               }
            }
         }

      }

      protected void method_14774(StructureWorldAccess structureWorldAccess, BlockBox blockBox, int i, int j, boolean bl) {
         if (bl) {
            this.fillWithOutline(structureWorldAccess, blockBox, i + 0, 0, j + 0, i + 2, 0, j + 8 - 1, PRISMARINE, PRISMARINE, false);
            this.fillWithOutline(structureWorldAccess, blockBox, i + 5, 0, j + 0, i + 8 - 1, 0, j + 8 - 1, PRISMARINE, PRISMARINE, false);
            this.fillWithOutline(structureWorldAccess, blockBox, i + 3, 0, j + 0, i + 4, 0, j + 2, PRISMARINE, PRISMARINE, false);
            this.fillWithOutline(structureWorldAccess, blockBox, i + 3, 0, j + 5, i + 4, 0, j + 8 - 1, PRISMARINE, PRISMARINE, false);
            this.fillWithOutline(structureWorldAccess, blockBox, i + 3, 0, j + 2, i + 4, 0, j + 2, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            this.fillWithOutline(structureWorldAccess, blockBox, i + 3, 0, j + 5, i + 4, 0, j + 5, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            this.fillWithOutline(structureWorldAccess, blockBox, i + 2, 0, j + 3, i + 2, 0, j + 4, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            this.fillWithOutline(structureWorldAccess, blockBox, i + 5, 0, j + 3, i + 5, 0, j + 4, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
         } else {
            this.fillWithOutline(structureWorldAccess, blockBox, i + 0, 0, j + 0, i + 8 - 1, 0, j + 8 - 1, PRISMARINE, PRISMARINE, false);
         }

      }

      protected void method_14771(StructureWorldAccess structureWorldAccess, BlockBox blockBox, int i, int j, int k, int l, int m, int n, BlockState blockState) {
         for(int o = j; o <= m; ++o) {
            for(int p = i; p <= l; ++p) {
               for(int q = k; q <= n; ++q) {
                  if (this.getBlockAt(structureWorldAccess, p, o, q, blockBox) == WATER) {
                     this.addBlock(structureWorldAccess, blockState, p, o, q, blockBox);
                  }
               }
            }
         }

      }

      protected boolean method_14775(BlockBox blockBox, int i, int j, int k, int l) {
         int m = this.applyXTransform(i, j);
         int n = this.applyZTransform(i, j);
         int o = this.applyXTransform(k, l);
         int p = this.applyZTransform(k, l);
         return blockBox.intersectsXZ(Math.min(m, o), Math.min(n, p), Math.max(m, o), Math.max(n, p));
      }

      protected boolean method_14772(StructureWorldAccess structureWorldAccess, BlockBox blockBox, int i, int j, int k) {
         int l = this.applyXTransform(i, k);
         int m = this.applyYTransform(j);
         int n = this.applyZTransform(i, k);
         if (blockBox.contains(new BlockPos(l, m, n))) {
            ElderGuardianEntity elderGuardianEntity = (ElderGuardianEntity)EntityType.ELDER_GUARDIAN.create(structureWorldAccess.toServerWorld());
            elderGuardianEntity.heal(elderGuardianEntity.getMaxHealth());
            elderGuardianEntity.refreshPositionAndAngles((double)l + 0.5D, (double)m, (double)n + 0.5D, 0.0F, 0.0F);
            elderGuardianEntity.initialize(structureWorldAccess, structureWorldAccess.getLocalDifficulty(elderGuardianEntity.getBlockPos()), SpawnReason.STRUCTURE, (EntityData)null, (CompoundTag)null);
            structureWorldAccess.spawnEntityAndPassengers(elderGuardianEntity);
            return true;
         } else {
            return false;
         }
      }

      static {
         PRISMARINE = Blocks.PRISMARINE.getDefaultState();
         PRISMARINE_BRICKS = Blocks.PRISMARINE_BRICKS.getDefaultState();
         DARK_PRISMARINE = Blocks.DARK_PRISMARINE.getDefaultState();
         field_14470 = PRISMARINE_BRICKS;
         SEA_LANTERN = Blocks.SEA_LANTERN.getDefaultState();
         WATER = Blocks.WATER.getDefaultState();
         ICE_BLOCKS = ImmutableSet.builder().add((Object)Blocks.ICE).add((Object)Blocks.PACKED_ICE).add((Object)Blocks.BLUE_ICE).add((Object)WATER.getBlock()).build();
         TWO_ZERO_ZERO_INDEX = getIndex(2, 0, 0);
         TWO_TWO_ZERO_INDEX = getIndex(2, 2, 0);
         ZERO_ONE_ZERO_INDEX = getIndex(0, 1, 0);
         FOUR_ONE_ZERO_INDEX = getIndex(4, 1, 0);
      }
   }
}
