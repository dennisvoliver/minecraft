package net.minecraft.structure;

import com.google.common.collect.Lists;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ChestBlock;
import net.minecraft.entity.EntityData;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.mob.IllagerEntity;
import net.minecraft.loot.LootTables;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.structure.processor.BlockIgnoreStructureProcessor;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.ServerWorldAccess;
import org.jetbrains.annotations.Nullable;

public class WoodlandMansionGenerator {
   public static void addPieces(StructureManager manager, BlockPos pos, BlockRotation rotation, List<WoodlandMansionGenerator.Piece> pieces, Random random) {
      WoodlandMansionGenerator.MansionParameters mansionParameters = new WoodlandMansionGenerator.MansionParameters(random);
      WoodlandMansionGenerator.LayoutGenerator layoutGenerator = new WoodlandMansionGenerator.LayoutGenerator(manager, random);
      layoutGenerator.generate(pos, rotation, pieces, mansionParameters);
   }

   static class ThirdFloorRoomPool extends WoodlandMansionGenerator.SecondFloorRoomPool {
      private ThirdFloorRoomPool() {
         super(null);
      }
   }

   static class SecondFloorRoomPool extends WoodlandMansionGenerator.RoomPool {
      private SecondFloorRoomPool() {
         super(null);
      }

      public String getSmallRoom(Random random) {
         return "1x1_b" + (random.nextInt(4) + 1);
      }

      public String getSmallSecretRoom(Random random) {
         return "1x1_as" + (random.nextInt(4) + 1);
      }

      public String getMediumFunctionalRoom(Random random, boolean staircase) {
         return staircase ? "1x2_c_stairs" : "1x2_c" + (random.nextInt(4) + 1);
      }

      public String getMediumGenericRoom(Random random, boolean staircase) {
         return staircase ? "1x2_d_stairs" : "1x2_d" + (random.nextInt(5) + 1);
      }

      public String getMediumSecretRoom(Random random) {
         return "1x2_se" + (random.nextInt(1) + 1);
      }

      public String getBigRoom(Random random) {
         return "2x2_b" + (random.nextInt(5) + 1);
      }

      public String getBigSecretRoom(Random random) {
         return "2x2_s1";
      }
   }

   static class FirstFloorRoomPool extends WoodlandMansionGenerator.RoomPool {
      private FirstFloorRoomPool() {
         super(null);
      }

      public String getSmallRoom(Random random) {
         return "1x1_a" + (random.nextInt(5) + 1);
      }

      public String getSmallSecretRoom(Random random) {
         return "1x1_as" + (random.nextInt(4) + 1);
      }

      public String getMediumFunctionalRoom(Random random, boolean staircase) {
         return "1x2_a" + (random.nextInt(9) + 1);
      }

      public String getMediumGenericRoom(Random random, boolean staircase) {
         return "1x2_b" + (random.nextInt(5) + 1);
      }

      public String getMediumSecretRoom(Random random) {
         return "1x2_s" + (random.nextInt(2) + 1);
      }

      public String getBigRoom(Random random) {
         return "2x2_a" + (random.nextInt(4) + 1);
      }

      public String getBigSecretRoom(Random random) {
         return "2x2_s1";
      }
   }

   abstract static class RoomPool {
      private RoomPool() {
      }

      public abstract String getSmallRoom(Random random);

      public abstract String getSmallSecretRoom(Random random);

      public abstract String getMediumFunctionalRoom(Random random, boolean staircase);

      public abstract String getMediumGenericRoom(Random random, boolean staircase);

      public abstract String getMediumSecretRoom(Random random);

      public abstract String getBigRoom(Random random);

      public abstract String getBigSecretRoom(Random random);
   }

   static class FlagMatrix {
      private final int[][] array;
      private final int n;
      private final int m;
      private final int fallback;

      public FlagMatrix(int n, int m, int fallback) {
         this.n = n;
         this.m = m;
         this.fallback = fallback;
         this.array = new int[n][m];
      }

      public void set(int i, int j, int value) {
         if (i >= 0 && i < this.n && j >= 0 && j < this.m) {
            this.array[i][j] = value;
         }

      }

      public void fill(int i0, int j0, int i1, int j1, int value) {
         for(int i = j0; i <= j1; ++i) {
            for(int j = i0; j <= i1; ++j) {
               this.set(j, i, value);
            }
         }

      }

      public int get(int i, int j) {
         return i >= 0 && i < this.n && j >= 0 && j < this.m ? this.array[i][j] : this.fallback;
      }

      public void update(int i, int j, int expected, int newValue) {
         if (this.get(i, j) == expected) {
            this.set(i, j, newValue);
         }

      }

      public boolean anyMatchAround(int i, int j, int value) {
         return this.get(i - 1, j) == value || this.get(i + 1, j) == value || this.get(i, j + 1) == value || this.get(i, j - 1) == value;
      }
   }

   static class MansionParameters {
      private final Random random;
      private final WoodlandMansionGenerator.FlagMatrix field_15440;
      private final WoodlandMansionGenerator.FlagMatrix field_15439;
      private final WoodlandMansionGenerator.FlagMatrix[] field_15443;
      private final int field_15442;
      private final int field_15441;

      public MansionParameters(Random random) {
         this.random = random;
         int i = true;
         this.field_15442 = 7;
         this.field_15441 = 4;
         this.field_15440 = new WoodlandMansionGenerator.FlagMatrix(11, 11, 5);
         this.field_15440.fill(this.field_15442, this.field_15441, this.field_15442 + 1, this.field_15441 + 1, 3);
         this.field_15440.fill(this.field_15442 - 1, this.field_15441, this.field_15442 - 1, this.field_15441 + 1, 2);
         this.field_15440.fill(this.field_15442 + 2, this.field_15441 - 2, this.field_15442 + 3, this.field_15441 + 3, 5);
         this.field_15440.fill(this.field_15442 + 1, this.field_15441 - 2, this.field_15442 + 1, this.field_15441 - 1, 1);
         this.field_15440.fill(this.field_15442 + 1, this.field_15441 + 2, this.field_15442 + 1, this.field_15441 + 3, 1);
         this.field_15440.set(this.field_15442 - 1, this.field_15441 - 1, 1);
         this.field_15440.set(this.field_15442 - 1, this.field_15441 + 2, 1);
         this.field_15440.fill(0, 0, 11, 1, 5);
         this.field_15440.fill(0, 9, 11, 11, 5);
         this.method_15045(this.field_15440, this.field_15442, this.field_15441 - 2, Direction.WEST, 6);
         this.method_15045(this.field_15440, this.field_15442, this.field_15441 + 3, Direction.WEST, 6);
         this.method_15045(this.field_15440, this.field_15442 - 2, this.field_15441 - 1, Direction.WEST, 3);
         this.method_15045(this.field_15440, this.field_15442 - 2, this.field_15441 + 2, Direction.WEST, 3);

         while(this.method_15046(this.field_15440)) {
         }

         this.field_15443 = new WoodlandMansionGenerator.FlagMatrix[3];
         this.field_15443[0] = new WoodlandMansionGenerator.FlagMatrix(11, 11, 5);
         this.field_15443[1] = new WoodlandMansionGenerator.FlagMatrix(11, 11, 5);
         this.field_15443[2] = new WoodlandMansionGenerator.FlagMatrix(11, 11, 5);
         this.method_15042(this.field_15440, this.field_15443[0]);
         this.method_15042(this.field_15440, this.field_15443[1]);
         this.field_15443[0].fill(this.field_15442 + 1, this.field_15441, this.field_15442 + 1, this.field_15441 + 1, 8388608);
         this.field_15443[1].fill(this.field_15442 + 1, this.field_15441, this.field_15442 + 1, this.field_15441 + 1, 8388608);
         this.field_15439 = new WoodlandMansionGenerator.FlagMatrix(this.field_15440.n, this.field_15440.m, 5);
         this.method_15048();
         this.method_15042(this.field_15439, this.field_15443[2]);
      }

      public static boolean method_15047(WoodlandMansionGenerator.FlagMatrix flagMatrix, int i, int j) {
         int k = flagMatrix.get(i, j);
         return k == 1 || k == 2 || k == 3 || k == 4;
      }

      public boolean method_15039(WoodlandMansionGenerator.FlagMatrix flagMatrix, int i, int j, int k, int l) {
         return (this.field_15443[k].get(i, j) & '\uffff') == l;
      }

      @Nullable
      public Direction method_15040(WoodlandMansionGenerator.FlagMatrix flagMatrix, int i, int j, int k, int l) {
         Iterator var6 = Direction.Type.HORIZONTAL.iterator();

         Direction direction;
         do {
            if (!var6.hasNext()) {
               return null;
            }

            direction = (Direction)var6.next();
         } while(!this.method_15039(flagMatrix, i + direction.getOffsetX(), j + direction.getOffsetZ(), k, l));

         return direction;
      }

      private void method_15045(WoodlandMansionGenerator.FlagMatrix flagMatrix, int i, int j, Direction direction, int k) {
         if (k > 0) {
            flagMatrix.set(i, j, 1);
            flagMatrix.update(i + direction.getOffsetX(), j + direction.getOffsetZ(), 0, 1);

            Direction direction2;
            for(int l = 0; l < 8; ++l) {
               direction2 = Direction.fromHorizontal(this.random.nextInt(4));
               if (direction2 != direction.getOpposite() && (direction2 != Direction.EAST || !this.random.nextBoolean())) {
                  int m = i + direction.getOffsetX();
                  int n = j + direction.getOffsetZ();
                  if (flagMatrix.get(m + direction2.getOffsetX(), n + direction2.getOffsetZ()) == 0 && flagMatrix.get(m + direction2.getOffsetX() * 2, n + direction2.getOffsetZ() * 2) == 0) {
                     this.method_15045(flagMatrix, i + direction.getOffsetX() + direction2.getOffsetX(), j + direction.getOffsetZ() + direction2.getOffsetZ(), direction2, k - 1);
                     break;
                  }
               }
            }

            Direction direction3 = direction.rotateYClockwise();
            direction2 = direction.rotateYCounterclockwise();
            flagMatrix.update(i + direction3.getOffsetX(), j + direction3.getOffsetZ(), 0, 2);
            flagMatrix.update(i + direction2.getOffsetX(), j + direction2.getOffsetZ(), 0, 2);
            flagMatrix.update(i + direction.getOffsetX() + direction3.getOffsetX(), j + direction.getOffsetZ() + direction3.getOffsetZ(), 0, 2);
            flagMatrix.update(i + direction.getOffsetX() + direction2.getOffsetX(), j + direction.getOffsetZ() + direction2.getOffsetZ(), 0, 2);
            flagMatrix.update(i + direction.getOffsetX() * 2, j + direction.getOffsetZ() * 2, 0, 2);
            flagMatrix.update(i + direction3.getOffsetX() * 2, j + direction3.getOffsetZ() * 2, 0, 2);
            flagMatrix.update(i + direction2.getOffsetX() * 2, j + direction2.getOffsetZ() * 2, 0, 2);
         }
      }

      private boolean method_15046(WoodlandMansionGenerator.FlagMatrix flagMatrix) {
         boolean bl = false;

         for(int i = 0; i < flagMatrix.m; ++i) {
            for(int j = 0; j < flagMatrix.n; ++j) {
               if (flagMatrix.get(j, i) == 0) {
                  int k = 0;
                  int k = k + (method_15047(flagMatrix, j + 1, i) ? 1 : 0);
                  k += method_15047(flagMatrix, j - 1, i) ? 1 : 0;
                  k += method_15047(flagMatrix, j, i + 1) ? 1 : 0;
                  k += method_15047(flagMatrix, j, i - 1) ? 1 : 0;
                  if (k >= 3) {
                     flagMatrix.set(j, i, 2);
                     bl = true;
                  } else if (k == 2) {
                     int l = 0;
                     int l = l + (method_15047(flagMatrix, j + 1, i + 1) ? 1 : 0);
                     l += method_15047(flagMatrix, j - 1, i + 1) ? 1 : 0;
                     l += method_15047(flagMatrix, j + 1, i - 1) ? 1 : 0;
                     l += method_15047(flagMatrix, j - 1, i - 1) ? 1 : 0;
                     if (l <= 1) {
                        flagMatrix.set(j, i, 2);
                        bl = true;
                     }
                  }
               }
            }
         }

         return bl;
      }

      private void method_15048() {
         List<Pair<Integer, Integer>> list = Lists.newArrayList();
         WoodlandMansionGenerator.FlagMatrix flagMatrix = this.field_15443[1];

         int m;
         int n;
         for(int i = 0; i < this.field_15439.m; ++i) {
            for(m = 0; m < this.field_15439.n; ++m) {
               int k = flagMatrix.get(m, i);
               n = k & 983040;
               if (n == 131072 && (k & 2097152) == 2097152) {
                  list.add(new Pair(m, i));
               }
            }
         }

         if (list.isEmpty()) {
            this.field_15439.fill(0, 0, this.field_15439.n, this.field_15439.m, 5);
         } else {
            Pair<Integer, Integer> pair = (Pair)list.get(this.random.nextInt(list.size()));
            m = flagMatrix.get((Integer)pair.getLeft(), (Integer)pair.getRight());
            flagMatrix.set((Integer)pair.getLeft(), (Integer)pair.getRight(), m | 4194304);
            Direction direction = this.method_15040(this.field_15440, (Integer)pair.getLeft(), (Integer)pair.getRight(), 1, m & '\uffff');
            n = (Integer)pair.getLeft() + direction.getOffsetX();
            int o = (Integer)pair.getRight() + direction.getOffsetZ();

            for(int p = 0; p < this.field_15439.m; ++p) {
               for(int q = 0; q < this.field_15439.n; ++q) {
                  if (!method_15047(this.field_15440, q, p)) {
                     this.field_15439.set(q, p, 5);
                  } else if (q == (Integer)pair.getLeft() && p == (Integer)pair.getRight()) {
                     this.field_15439.set(q, p, 3);
                  } else if (q == n && p == o) {
                     this.field_15439.set(q, p, 3);
                     this.field_15443[2].set(q, p, 8388608);
                  }
               }
            }

            List<Direction> list2 = Lists.newArrayList();
            Iterator var14 = Direction.Type.HORIZONTAL.iterator();

            while(var14.hasNext()) {
               Direction direction2 = (Direction)var14.next();
               if (this.field_15439.get(n + direction2.getOffsetX(), o + direction2.getOffsetZ()) == 0) {
                  list2.add(direction2);
               }
            }

            if (list2.isEmpty()) {
               this.field_15439.fill(0, 0, this.field_15439.n, this.field_15439.m, 5);
               flagMatrix.set((Integer)pair.getLeft(), (Integer)pair.getRight(), m);
            } else {
               Direction direction3 = (Direction)list2.get(this.random.nextInt(list2.size()));
               this.method_15045(this.field_15439, n + direction3.getOffsetX(), o + direction3.getOffsetZ(), direction3, 4);

               while(this.method_15046(this.field_15439)) {
               }

            }
         }
      }

      private void method_15042(WoodlandMansionGenerator.FlagMatrix flagMatrix, WoodlandMansionGenerator.FlagMatrix flagMatrix2) {
         List<Pair<Integer, Integer>> list = Lists.newArrayList();

         int k;
         for(k = 0; k < flagMatrix.m; ++k) {
            for(int j = 0; j < flagMatrix.n; ++j) {
               if (flagMatrix.get(j, k) == 2) {
                  list.add(new Pair(j, k));
               }
            }
         }

         Collections.shuffle(list, this.random);
         k = 10;
         Iterator var19 = list.iterator();

         while(true) {
            int l;
            int m;
            do {
               if (!var19.hasNext()) {
                  return;
               }

               Pair<Integer, Integer> pair = (Pair)var19.next();
               l = (Integer)pair.getLeft();
               m = (Integer)pair.getRight();
            } while(flagMatrix2.get(l, m) != 0);

            int n = l;
            int o = l;
            int p = m;
            int q = m;
            int r = 65536;
            if (flagMatrix2.get(l + 1, m) == 0 && flagMatrix2.get(l, m + 1) == 0 && flagMatrix2.get(l + 1, m + 1) == 0 && flagMatrix.get(l + 1, m) == 2 && flagMatrix.get(l, m + 1) == 2 && flagMatrix.get(l + 1, m + 1) == 2) {
               o = l + 1;
               q = m + 1;
               r = 262144;
            } else if (flagMatrix2.get(l - 1, m) == 0 && flagMatrix2.get(l, m + 1) == 0 && flagMatrix2.get(l - 1, m + 1) == 0 && flagMatrix.get(l - 1, m) == 2 && flagMatrix.get(l, m + 1) == 2 && flagMatrix.get(l - 1, m + 1) == 2) {
               n = l - 1;
               q = m + 1;
               r = 262144;
            } else if (flagMatrix2.get(l - 1, m) == 0 && flagMatrix2.get(l, m - 1) == 0 && flagMatrix2.get(l - 1, m - 1) == 0 && flagMatrix.get(l - 1, m) == 2 && flagMatrix.get(l, m - 1) == 2 && flagMatrix.get(l - 1, m - 1) == 2) {
               n = l - 1;
               p = m - 1;
               r = 262144;
            } else if (flagMatrix2.get(l + 1, m) == 0 && flagMatrix.get(l + 1, m) == 2) {
               o = l + 1;
               r = 131072;
            } else if (flagMatrix2.get(l, m + 1) == 0 && flagMatrix.get(l, m + 1) == 2) {
               q = m + 1;
               r = 131072;
            } else if (flagMatrix2.get(l - 1, m) == 0 && flagMatrix.get(l - 1, m) == 2) {
               n = l - 1;
               r = 131072;
            } else if (flagMatrix2.get(l, m - 1) == 0 && flagMatrix.get(l, m - 1) == 2) {
               p = m - 1;
               r = 131072;
            }

            int s = this.random.nextBoolean() ? n : o;
            int t = this.random.nextBoolean() ? p : q;
            int u = 2097152;
            if (!flagMatrix.anyMatchAround(s, t, 1)) {
               s = s == n ? o : n;
               t = t == p ? q : p;
               if (!flagMatrix.anyMatchAround(s, t, 1)) {
                  t = t == p ? q : p;
                  if (!flagMatrix.anyMatchAround(s, t, 1)) {
                     s = s == n ? o : n;
                     t = t == p ? q : p;
                     if (!flagMatrix.anyMatchAround(s, t, 1)) {
                        u = 0;
                        s = n;
                        t = p;
                     }
                  }
               }
            }

            for(int v = p; v <= q; ++v) {
               for(int w = n; w <= o; ++w) {
                  if (w == s && v == t) {
                     flagMatrix2.set(w, v, 1048576 | u | r | k);
                  } else {
                     flagMatrix2.set(w, v, r | k);
                  }
               }
            }

            ++k;
         }
      }
   }

   static class LayoutGenerator {
      private final StructureManager manager;
      private final Random random;
      private int field_15446;
      private int field_15445;

      public LayoutGenerator(StructureManager manager, Random random) {
         this.manager = manager;
         this.random = random;
      }

      public void generate(BlockPos pos, BlockRotation rotation, List<WoodlandMansionGenerator.Piece> pieces, WoodlandMansionGenerator.MansionParameters mansionParameters) {
         WoodlandMansionGenerator.GenerationPiece generationPiece = new WoodlandMansionGenerator.GenerationPiece();
         generationPiece.position = pos;
         generationPiece.rotation = rotation;
         generationPiece.template = "wall_flat";
         WoodlandMansionGenerator.GenerationPiece generationPiece2 = new WoodlandMansionGenerator.GenerationPiece();
         this.addEntrance(pieces, generationPiece);
         generationPiece2.position = generationPiece.position.up(8);
         generationPiece2.rotation = generationPiece.rotation;
         generationPiece2.template = "wall_window";
         if (!pieces.isEmpty()) {
         }

         WoodlandMansionGenerator.FlagMatrix flagMatrix = mansionParameters.field_15440;
         WoodlandMansionGenerator.FlagMatrix flagMatrix2 = mansionParameters.field_15439;
         this.field_15446 = mansionParameters.field_15442 + 1;
         this.field_15445 = mansionParameters.field_15441 + 1;
         int i = mansionParameters.field_15442 + 1;
         int j = mansionParameters.field_15441;
         this.addRoof(pieces, generationPiece, flagMatrix, Direction.SOUTH, this.field_15446, this.field_15445, i, j);
         this.addRoof(pieces, generationPiece2, flagMatrix, Direction.SOUTH, this.field_15446, this.field_15445, i, j);
         WoodlandMansionGenerator.GenerationPiece generationPiece3 = new WoodlandMansionGenerator.GenerationPiece();
         generationPiece3.position = generationPiece.position.up(19);
         generationPiece3.rotation = generationPiece.rotation;
         generationPiece3.template = "wall_window";
         boolean bl = false;

         int m;
         for(int k = 0; k < flagMatrix2.m && !bl; ++k) {
            for(m = flagMatrix2.n - 1; m >= 0 && !bl; --m) {
               if (WoodlandMansionGenerator.MansionParameters.method_15047(flagMatrix2, m, k)) {
                  generationPiece3.position = generationPiece3.position.offset(rotation.rotate(Direction.SOUTH), 8 + (k - this.field_15445) * 8);
                  generationPiece3.position = generationPiece3.position.offset(rotation.rotate(Direction.EAST), (m - this.field_15446) * 8);
                  this.method_15052(pieces, generationPiece3);
                  this.addRoof(pieces, generationPiece3, flagMatrix2, Direction.SOUTH, m, k, m, k);
                  bl = true;
               }
            }
         }

         this.method_15055(pieces, pos.up(16), rotation, flagMatrix, flagMatrix2);
         this.method_15055(pieces, pos.up(27), rotation, flagMatrix2, (WoodlandMansionGenerator.FlagMatrix)null);
         if (!pieces.isEmpty()) {
         }

         WoodlandMansionGenerator.RoomPool[] roomPools = new WoodlandMansionGenerator.RoomPool[]{new WoodlandMansionGenerator.FirstFloorRoomPool(), new WoodlandMansionGenerator.SecondFloorRoomPool(), new WoodlandMansionGenerator.ThirdFloorRoomPool()};

         for(m = 0; m < 3; ++m) {
            BlockPos blockPos = pos.up(8 * m + (m == 2 ? 3 : 0));
            WoodlandMansionGenerator.FlagMatrix flagMatrix3 = mansionParameters.field_15443[m];
            WoodlandMansionGenerator.FlagMatrix flagMatrix4 = m == 2 ? flagMatrix2 : flagMatrix;
            String string = m == 0 ? "carpet_south_1" : "carpet_south_2";
            String string2 = m == 0 ? "carpet_west_1" : "carpet_west_2";

            for(int n = 0; n < flagMatrix4.m; ++n) {
               for(int o = 0; o < flagMatrix4.n; ++o) {
                  if (flagMatrix4.get(o, n) == 1) {
                     BlockPos blockPos2 = blockPos.offset(rotation.rotate(Direction.SOUTH), 8 + (n - this.field_15445) * 8);
                     blockPos2 = blockPos2.offset(rotation.rotate(Direction.EAST), (o - this.field_15446) * 8);
                     pieces.add(new WoodlandMansionGenerator.Piece(this.manager, "corridor_floor", blockPos2, rotation));
                     if (flagMatrix4.get(o, n - 1) == 1 || (flagMatrix3.get(o, n - 1) & 8388608) == 8388608) {
                        pieces.add(new WoodlandMansionGenerator.Piece(this.manager, "carpet_north", blockPos2.offset((Direction)rotation.rotate(Direction.EAST), 1).up(), rotation));
                     }

                     if (flagMatrix4.get(o + 1, n) == 1 || (flagMatrix3.get(o + 1, n) & 8388608) == 8388608) {
                        pieces.add(new WoodlandMansionGenerator.Piece(this.manager, "carpet_east", blockPos2.offset((Direction)rotation.rotate(Direction.SOUTH), 1).offset((Direction)rotation.rotate(Direction.EAST), 5).up(), rotation));
                     }

                     if (flagMatrix4.get(o, n + 1) == 1 || (flagMatrix3.get(o, n + 1) & 8388608) == 8388608) {
                        pieces.add(new WoodlandMansionGenerator.Piece(this.manager, string, blockPos2.offset((Direction)rotation.rotate(Direction.SOUTH), 5).offset((Direction)rotation.rotate(Direction.WEST), 1), rotation));
                     }

                     if (flagMatrix4.get(o - 1, n) == 1 || (flagMatrix3.get(o - 1, n) & 8388608) == 8388608) {
                        pieces.add(new WoodlandMansionGenerator.Piece(this.manager, string2, blockPos2.offset((Direction)rotation.rotate(Direction.WEST), 1).offset((Direction)rotation.rotate(Direction.NORTH), 1), rotation));
                     }
                  }
               }
            }

            String string3 = m == 0 ? "indoors_wall_1" : "indoors_wall_2";
            String string4 = m == 0 ? "indoors_door_1" : "indoors_door_2";
            List<Direction> list = Lists.newArrayList();

            for(int p = 0; p < flagMatrix4.m; ++p) {
               for(int q = 0; q < flagMatrix4.n; ++q) {
                  boolean bl2 = m == 2 && flagMatrix4.get(q, p) == 3;
                  if (flagMatrix4.get(q, p) == 2 || bl2) {
                     int r = flagMatrix3.get(q, p);
                     int s = r & 983040;
                     int t = r & '\uffff';
                     bl2 = bl2 && (r & 8388608) == 8388608;
                     list.clear();
                     if ((r & 2097152) == 2097152) {
                        Iterator var29 = Direction.Type.HORIZONTAL.iterator();

                        while(var29.hasNext()) {
                           Direction direction = (Direction)var29.next();
                           if (flagMatrix4.get(q + direction.getOffsetX(), p + direction.getOffsetZ()) == 1) {
                              list.add(direction);
                           }
                        }
                     }

                     Direction direction2 = null;
                     if (!list.isEmpty()) {
                        direction2 = (Direction)list.get(this.random.nextInt(list.size()));
                     } else if ((r & 1048576) == 1048576) {
                        direction2 = Direction.UP;
                     }

                     BlockPos blockPos3 = blockPos.offset(rotation.rotate(Direction.SOUTH), 8 + (p - this.field_15445) * 8);
                     blockPos3 = blockPos3.offset(rotation.rotate(Direction.EAST), -1 + (q - this.field_15446) * 8);
                     if (WoodlandMansionGenerator.MansionParameters.method_15047(flagMatrix4, q - 1, p) && !mansionParameters.method_15039(flagMatrix4, q - 1, p, m, t)) {
                        pieces.add(new WoodlandMansionGenerator.Piece(this.manager, direction2 == Direction.WEST ? string4 : string3, blockPos3, rotation));
                     }

                     BlockPos blockPos6;
                     if (flagMatrix4.get(q + 1, p) == 1 && !bl2) {
                        blockPos6 = blockPos3.offset((Direction)rotation.rotate(Direction.EAST), 8);
                        pieces.add(new WoodlandMansionGenerator.Piece(this.manager, direction2 == Direction.EAST ? string4 : string3, blockPos6, rotation));
                     }

                     if (WoodlandMansionGenerator.MansionParameters.method_15047(flagMatrix4, q, p + 1) && !mansionParameters.method_15039(flagMatrix4, q, p + 1, m, t)) {
                        blockPos6 = blockPos3.offset((Direction)rotation.rotate(Direction.SOUTH), 7);
                        blockPos6 = blockPos6.offset((Direction)rotation.rotate(Direction.EAST), 7);
                        pieces.add(new WoodlandMansionGenerator.Piece(this.manager, direction2 == Direction.SOUTH ? string4 : string3, blockPos6, rotation.rotate(BlockRotation.CLOCKWISE_90)));
                     }

                     if (flagMatrix4.get(q, p - 1) == 1 && !bl2) {
                        blockPos6 = blockPos3.offset((Direction)rotation.rotate(Direction.NORTH), 1);
                        blockPos6 = blockPos6.offset((Direction)rotation.rotate(Direction.EAST), 7);
                        pieces.add(new WoodlandMansionGenerator.Piece(this.manager, direction2 == Direction.NORTH ? string4 : string3, blockPos6, rotation.rotate(BlockRotation.CLOCKWISE_90)));
                     }

                     if (s == 65536) {
                        this.addSmallRoom(pieces, blockPos3, rotation, direction2, roomPools[m]);
                     } else {
                        Direction direction4;
                        if (s == 131072 && direction2 != null) {
                           direction4 = mansionParameters.method_15040(flagMatrix4, q, p, m, t);
                           boolean bl3 = (r & 4194304) == 4194304;
                           this.addMediumRoom(pieces, blockPos3, rotation, direction4, direction2, roomPools[m], bl3);
                        } else if (s == 262144 && direction2 != null && direction2 != Direction.UP) {
                           direction4 = direction2.rotateYClockwise();
                           if (!mansionParameters.method_15039(flagMatrix4, q + direction4.getOffsetX(), p + direction4.getOffsetZ(), m, t)) {
                              direction4 = direction4.getOpposite();
                           }

                           this.addBigRoom(pieces, blockPos3, rotation, direction4, direction2, roomPools[m]);
                        } else if (s == 262144 && direction2 == Direction.UP) {
                           this.addBigSecretRoom(pieces, blockPos3, rotation, roomPools[m]);
                        }
                     }
                  }
               }
            }
         }

      }

      private void addRoof(List<WoodlandMansionGenerator.Piece> list, WoodlandMansionGenerator.GenerationPiece generationPiece, WoodlandMansionGenerator.FlagMatrix flagMatrix, Direction direction, int i, int j, int k, int l) {
         int m = i;
         int n = j;
         Direction direction2 = direction;

         do {
            if (!WoodlandMansionGenerator.MansionParameters.method_15047(flagMatrix, m + direction.getOffsetX(), n + direction.getOffsetZ())) {
               this.method_15058(list, generationPiece);
               direction = direction.rotateYClockwise();
               if (m != k || n != l || direction2 != direction) {
                  this.method_15052(list, generationPiece);
               }
            } else if (WoodlandMansionGenerator.MansionParameters.method_15047(flagMatrix, m + direction.getOffsetX(), n + direction.getOffsetZ()) && WoodlandMansionGenerator.MansionParameters.method_15047(flagMatrix, m + direction.getOffsetX() + direction.rotateYCounterclockwise().getOffsetX(), n + direction.getOffsetZ() + direction.rotateYCounterclockwise().getOffsetZ())) {
               this.method_15060(list, generationPiece);
               m += direction.getOffsetX();
               n += direction.getOffsetZ();
               direction = direction.rotateYCounterclockwise();
            } else {
               m += direction.getOffsetX();
               n += direction.getOffsetZ();
               if (m != k || n != l || direction2 != direction) {
                  this.method_15052(list, generationPiece);
               }
            }
         } while(m != k || n != l || direction2 != direction);

      }

      private void method_15055(List<WoodlandMansionGenerator.Piece> list, BlockPos blockPos, BlockRotation blockRotation, WoodlandMansionGenerator.FlagMatrix flagMatrix, @Nullable WoodlandMansionGenerator.FlagMatrix flagMatrix2) {
         int k;
         int l;
         BlockPos blockPos7;
         boolean bl3;
         BlockPos blockPos15;
         for(k = 0; k < flagMatrix.m; ++k) {
            for(l = 0; l < flagMatrix.n; ++l) {
               blockPos7 = blockPos.offset(blockRotation.rotate(Direction.SOUTH), 8 + (k - this.field_15445) * 8);
               blockPos7 = blockPos7.offset(blockRotation.rotate(Direction.EAST), (l - this.field_15446) * 8);
               bl3 = flagMatrix2 != null && WoodlandMansionGenerator.MansionParameters.method_15047(flagMatrix2, l, k);
               if (WoodlandMansionGenerator.MansionParameters.method_15047(flagMatrix, l, k) && !bl3) {
                  list.add(new WoodlandMansionGenerator.Piece(this.manager, "roof", blockPos7.up(3), blockRotation));
                  if (!WoodlandMansionGenerator.MansionParameters.method_15047(flagMatrix, l + 1, k)) {
                     blockPos15 = blockPos7.offset((Direction)blockRotation.rotate(Direction.EAST), 6);
                     list.add(new WoodlandMansionGenerator.Piece(this.manager, "roof_front", blockPos15, blockRotation));
                  }

                  if (!WoodlandMansionGenerator.MansionParameters.method_15047(flagMatrix, l - 1, k)) {
                     blockPos15 = blockPos7.offset((Direction)blockRotation.rotate(Direction.EAST), 0);
                     blockPos15 = blockPos15.offset((Direction)blockRotation.rotate(Direction.SOUTH), 7);
                     list.add(new WoodlandMansionGenerator.Piece(this.manager, "roof_front", blockPos15, blockRotation.rotate(BlockRotation.CLOCKWISE_180)));
                  }

                  if (!WoodlandMansionGenerator.MansionParameters.method_15047(flagMatrix, l, k - 1)) {
                     blockPos15 = blockPos7.offset((Direction)blockRotation.rotate(Direction.WEST), 1);
                     list.add(new WoodlandMansionGenerator.Piece(this.manager, "roof_front", blockPos15, blockRotation.rotate(BlockRotation.COUNTERCLOCKWISE_90)));
                  }

                  if (!WoodlandMansionGenerator.MansionParameters.method_15047(flagMatrix, l, k + 1)) {
                     blockPos15 = blockPos7.offset((Direction)blockRotation.rotate(Direction.EAST), 6);
                     blockPos15 = blockPos15.offset((Direction)blockRotation.rotate(Direction.SOUTH), 6);
                     list.add(new WoodlandMansionGenerator.Piece(this.manager, "roof_front", blockPos15, blockRotation.rotate(BlockRotation.CLOCKWISE_90)));
                  }
               }
            }
         }

         if (flagMatrix2 != null) {
            for(k = 0; k < flagMatrix.m; ++k) {
               for(l = 0; l < flagMatrix.n; ++l) {
                  blockPos7 = blockPos.offset(blockRotation.rotate(Direction.SOUTH), 8 + (k - this.field_15445) * 8);
                  blockPos7 = blockPos7.offset(blockRotation.rotate(Direction.EAST), (l - this.field_15446) * 8);
                  bl3 = WoodlandMansionGenerator.MansionParameters.method_15047(flagMatrix2, l, k);
                  if (WoodlandMansionGenerator.MansionParameters.method_15047(flagMatrix, l, k) && bl3) {
                     if (!WoodlandMansionGenerator.MansionParameters.method_15047(flagMatrix, l + 1, k)) {
                        blockPos15 = blockPos7.offset((Direction)blockRotation.rotate(Direction.EAST), 7);
                        list.add(new WoodlandMansionGenerator.Piece(this.manager, "small_wall", blockPos15, blockRotation));
                     }

                     if (!WoodlandMansionGenerator.MansionParameters.method_15047(flagMatrix, l - 1, k)) {
                        blockPos15 = blockPos7.offset((Direction)blockRotation.rotate(Direction.WEST), 1);
                        blockPos15 = blockPos15.offset((Direction)blockRotation.rotate(Direction.SOUTH), 6);
                        list.add(new WoodlandMansionGenerator.Piece(this.manager, "small_wall", blockPos15, blockRotation.rotate(BlockRotation.CLOCKWISE_180)));
                     }

                     if (!WoodlandMansionGenerator.MansionParameters.method_15047(flagMatrix, l, k - 1)) {
                        blockPos15 = blockPos7.offset((Direction)blockRotation.rotate(Direction.WEST), 0);
                        blockPos15 = blockPos15.offset((Direction)blockRotation.rotate(Direction.NORTH), 1);
                        list.add(new WoodlandMansionGenerator.Piece(this.manager, "small_wall", blockPos15, blockRotation.rotate(BlockRotation.COUNTERCLOCKWISE_90)));
                     }

                     if (!WoodlandMansionGenerator.MansionParameters.method_15047(flagMatrix, l, k + 1)) {
                        blockPos15 = blockPos7.offset((Direction)blockRotation.rotate(Direction.EAST), 6);
                        blockPos15 = blockPos15.offset((Direction)blockRotation.rotate(Direction.SOUTH), 7);
                        list.add(new WoodlandMansionGenerator.Piece(this.manager, "small_wall", blockPos15, blockRotation.rotate(BlockRotation.CLOCKWISE_90)));
                     }

                     if (!WoodlandMansionGenerator.MansionParameters.method_15047(flagMatrix, l + 1, k)) {
                        if (!WoodlandMansionGenerator.MansionParameters.method_15047(flagMatrix, l, k - 1)) {
                           blockPos15 = blockPos7.offset((Direction)blockRotation.rotate(Direction.EAST), 7);
                           blockPos15 = blockPos15.offset((Direction)blockRotation.rotate(Direction.NORTH), 2);
                           list.add(new WoodlandMansionGenerator.Piece(this.manager, "small_wall_corner", blockPos15, blockRotation));
                        }

                        if (!WoodlandMansionGenerator.MansionParameters.method_15047(flagMatrix, l, k + 1)) {
                           blockPos15 = blockPos7.offset((Direction)blockRotation.rotate(Direction.EAST), 8);
                           blockPos15 = blockPos15.offset((Direction)blockRotation.rotate(Direction.SOUTH), 7);
                           list.add(new WoodlandMansionGenerator.Piece(this.manager, "small_wall_corner", blockPos15, blockRotation.rotate(BlockRotation.CLOCKWISE_90)));
                        }
                     }

                     if (!WoodlandMansionGenerator.MansionParameters.method_15047(flagMatrix, l - 1, k)) {
                        if (!WoodlandMansionGenerator.MansionParameters.method_15047(flagMatrix, l, k - 1)) {
                           blockPos15 = blockPos7.offset((Direction)blockRotation.rotate(Direction.WEST), 2);
                           blockPos15 = blockPos15.offset((Direction)blockRotation.rotate(Direction.NORTH), 1);
                           list.add(new WoodlandMansionGenerator.Piece(this.manager, "small_wall_corner", blockPos15, blockRotation.rotate(BlockRotation.COUNTERCLOCKWISE_90)));
                        }

                        if (!WoodlandMansionGenerator.MansionParameters.method_15047(flagMatrix, l, k + 1)) {
                           blockPos15 = blockPos7.offset((Direction)blockRotation.rotate(Direction.WEST), 1);
                           blockPos15 = blockPos15.offset((Direction)blockRotation.rotate(Direction.SOUTH), 8);
                           list.add(new WoodlandMansionGenerator.Piece(this.manager, "small_wall_corner", blockPos15, blockRotation.rotate(BlockRotation.CLOCKWISE_180)));
                        }
                     }
                  }
               }
            }
         }

         for(k = 0; k < flagMatrix.m; ++k) {
            for(l = 0; l < flagMatrix.n; ++l) {
               blockPos7 = blockPos.offset(blockRotation.rotate(Direction.SOUTH), 8 + (k - this.field_15445) * 8);
               blockPos7 = blockPos7.offset(blockRotation.rotate(Direction.EAST), (l - this.field_15446) * 8);
               bl3 = flagMatrix2 != null && WoodlandMansionGenerator.MansionParameters.method_15047(flagMatrix2, l, k);
               if (WoodlandMansionGenerator.MansionParameters.method_15047(flagMatrix, l, k) && !bl3) {
                  BlockPos blockPos24;
                  if (!WoodlandMansionGenerator.MansionParameters.method_15047(flagMatrix, l + 1, k)) {
                     blockPos15 = blockPos7.offset((Direction)blockRotation.rotate(Direction.EAST), 6);
                     if (!WoodlandMansionGenerator.MansionParameters.method_15047(flagMatrix, l, k + 1)) {
                        blockPos24 = blockPos15.offset((Direction)blockRotation.rotate(Direction.SOUTH), 6);
                        list.add(new WoodlandMansionGenerator.Piece(this.manager, "roof_corner", blockPos24, blockRotation));
                     } else if (WoodlandMansionGenerator.MansionParameters.method_15047(flagMatrix, l + 1, k + 1)) {
                        blockPos24 = blockPos15.offset((Direction)blockRotation.rotate(Direction.SOUTH), 5);
                        list.add(new WoodlandMansionGenerator.Piece(this.manager, "roof_inner_corner", blockPos24, blockRotation));
                     }

                     if (!WoodlandMansionGenerator.MansionParameters.method_15047(flagMatrix, l, k - 1)) {
                        list.add(new WoodlandMansionGenerator.Piece(this.manager, "roof_corner", blockPos15, blockRotation.rotate(BlockRotation.COUNTERCLOCKWISE_90)));
                     } else if (WoodlandMansionGenerator.MansionParameters.method_15047(flagMatrix, l + 1, k - 1)) {
                        blockPos24 = blockPos7.offset((Direction)blockRotation.rotate(Direction.EAST), 9);
                        blockPos24 = blockPos24.offset((Direction)blockRotation.rotate(Direction.NORTH), 2);
                        list.add(new WoodlandMansionGenerator.Piece(this.manager, "roof_inner_corner", blockPos24, blockRotation.rotate(BlockRotation.CLOCKWISE_90)));
                     }
                  }

                  if (!WoodlandMansionGenerator.MansionParameters.method_15047(flagMatrix, l - 1, k)) {
                     blockPos15 = blockPos7.offset((Direction)blockRotation.rotate(Direction.EAST), 0);
                     blockPos15 = blockPos15.offset((Direction)blockRotation.rotate(Direction.SOUTH), 0);
                     if (!WoodlandMansionGenerator.MansionParameters.method_15047(flagMatrix, l, k + 1)) {
                        blockPos24 = blockPos15.offset((Direction)blockRotation.rotate(Direction.SOUTH), 6);
                        list.add(new WoodlandMansionGenerator.Piece(this.manager, "roof_corner", blockPos24, blockRotation.rotate(BlockRotation.CLOCKWISE_90)));
                     } else if (WoodlandMansionGenerator.MansionParameters.method_15047(flagMatrix, l - 1, k + 1)) {
                        blockPos24 = blockPos15.offset((Direction)blockRotation.rotate(Direction.SOUTH), 8);
                        blockPos24 = blockPos24.offset((Direction)blockRotation.rotate(Direction.WEST), 3);
                        list.add(new WoodlandMansionGenerator.Piece(this.manager, "roof_inner_corner", blockPos24, blockRotation.rotate(BlockRotation.COUNTERCLOCKWISE_90)));
                     }

                     if (!WoodlandMansionGenerator.MansionParameters.method_15047(flagMatrix, l, k - 1)) {
                        list.add(new WoodlandMansionGenerator.Piece(this.manager, "roof_corner", blockPos15, blockRotation.rotate(BlockRotation.CLOCKWISE_180)));
                     } else if (WoodlandMansionGenerator.MansionParameters.method_15047(flagMatrix, l - 1, k - 1)) {
                        blockPos24 = blockPos15.offset((Direction)blockRotation.rotate(Direction.SOUTH), 1);
                        list.add(new WoodlandMansionGenerator.Piece(this.manager, "roof_inner_corner", blockPos24, blockRotation.rotate(BlockRotation.CLOCKWISE_180)));
                     }
                  }
               }
            }
         }

      }

      private void addEntrance(List<WoodlandMansionGenerator.Piece> list, WoodlandMansionGenerator.GenerationPiece generationPiece) {
         Direction direction = generationPiece.rotation.rotate(Direction.WEST);
         list.add(new WoodlandMansionGenerator.Piece(this.manager, "entrance", generationPiece.position.offset((Direction)direction, 9), generationPiece.rotation));
         generationPiece.position = generationPiece.position.offset((Direction)generationPiece.rotation.rotate(Direction.SOUTH), 16);
      }

      private void method_15052(List<WoodlandMansionGenerator.Piece> list, WoodlandMansionGenerator.GenerationPiece generationPiece) {
         list.add(new WoodlandMansionGenerator.Piece(this.manager, generationPiece.template, generationPiece.position.offset((Direction)generationPiece.rotation.rotate(Direction.EAST), 7), generationPiece.rotation));
         generationPiece.position = generationPiece.position.offset((Direction)generationPiece.rotation.rotate(Direction.SOUTH), 8);
      }

      private void method_15058(List<WoodlandMansionGenerator.Piece> list, WoodlandMansionGenerator.GenerationPiece generationPiece) {
         generationPiece.position = generationPiece.position.offset((Direction)generationPiece.rotation.rotate(Direction.SOUTH), -1);
         list.add(new WoodlandMansionGenerator.Piece(this.manager, "wall_corner", generationPiece.position, generationPiece.rotation));
         generationPiece.position = generationPiece.position.offset((Direction)generationPiece.rotation.rotate(Direction.SOUTH), -7);
         generationPiece.position = generationPiece.position.offset((Direction)generationPiece.rotation.rotate(Direction.WEST), -6);
         generationPiece.rotation = generationPiece.rotation.rotate(BlockRotation.CLOCKWISE_90);
      }

      private void method_15060(List<WoodlandMansionGenerator.Piece> list, WoodlandMansionGenerator.GenerationPiece generationPiece) {
         generationPiece.position = generationPiece.position.offset((Direction)generationPiece.rotation.rotate(Direction.SOUTH), 6);
         generationPiece.position = generationPiece.position.offset((Direction)generationPiece.rotation.rotate(Direction.EAST), 8);
         generationPiece.rotation = generationPiece.rotation.rotate(BlockRotation.COUNTERCLOCKWISE_90);
      }

      private void addSmallRoom(List<WoodlandMansionGenerator.Piece> list, BlockPos blockPos, BlockRotation blockRotation, Direction direction, WoodlandMansionGenerator.RoomPool roomPool) {
         BlockRotation blockRotation2 = BlockRotation.NONE;
         String string = roomPool.getSmallRoom(this.random);
         if (direction != Direction.EAST) {
            if (direction == Direction.NORTH) {
               blockRotation2 = blockRotation2.rotate(BlockRotation.COUNTERCLOCKWISE_90);
            } else if (direction == Direction.WEST) {
               blockRotation2 = blockRotation2.rotate(BlockRotation.CLOCKWISE_180);
            } else if (direction == Direction.SOUTH) {
               blockRotation2 = blockRotation2.rotate(BlockRotation.CLOCKWISE_90);
            } else {
               string = roomPool.getSmallSecretRoom(this.random);
            }
         }

         BlockPos blockPos2 = Structure.applyTransformedOffset(new BlockPos(1, 0, 0), BlockMirror.NONE, blockRotation2, 7, 7);
         blockRotation2 = blockRotation2.rotate(blockRotation);
         blockPos2 = blockPos2.rotate(blockRotation);
         BlockPos blockPos3 = blockPos.add(blockPos2.getX(), 0, blockPos2.getZ());
         list.add(new WoodlandMansionGenerator.Piece(this.manager, string, blockPos3, blockRotation2));
      }

      private void addMediumRoom(List<WoodlandMansionGenerator.Piece> list, BlockPos blockPos, BlockRotation blockRotation, Direction direction, Direction direction2, WoodlandMansionGenerator.RoomPool roomPool, boolean staircase) {
         BlockPos blockPos15;
         if (direction2 == Direction.EAST && direction == Direction.SOUTH) {
            blockPos15 = blockPos.offset((Direction)blockRotation.rotate(Direction.EAST), 1);
            list.add(new WoodlandMansionGenerator.Piece(this.manager, roomPool.getMediumFunctionalRoom(this.random, staircase), blockPos15, blockRotation));
         } else if (direction2 == Direction.EAST && direction == Direction.NORTH) {
            blockPos15 = blockPos.offset((Direction)blockRotation.rotate(Direction.EAST), 1);
            blockPos15 = blockPos15.offset((Direction)blockRotation.rotate(Direction.SOUTH), 6);
            list.add(new WoodlandMansionGenerator.Piece(this.manager, roomPool.getMediumFunctionalRoom(this.random, staircase), blockPos15, blockRotation, BlockMirror.LEFT_RIGHT));
         } else if (direction2 == Direction.WEST && direction == Direction.NORTH) {
            blockPos15 = blockPos.offset((Direction)blockRotation.rotate(Direction.EAST), 7);
            blockPos15 = blockPos15.offset((Direction)blockRotation.rotate(Direction.SOUTH), 6);
            list.add(new WoodlandMansionGenerator.Piece(this.manager, roomPool.getMediumFunctionalRoom(this.random, staircase), blockPos15, blockRotation.rotate(BlockRotation.CLOCKWISE_180)));
         } else if (direction2 == Direction.WEST && direction == Direction.SOUTH) {
            blockPos15 = blockPos.offset((Direction)blockRotation.rotate(Direction.EAST), 7);
            list.add(new WoodlandMansionGenerator.Piece(this.manager, roomPool.getMediumFunctionalRoom(this.random, staircase), blockPos15, blockRotation, BlockMirror.FRONT_BACK));
         } else if (direction2 == Direction.SOUTH && direction == Direction.EAST) {
            blockPos15 = blockPos.offset((Direction)blockRotation.rotate(Direction.EAST), 1);
            list.add(new WoodlandMansionGenerator.Piece(this.manager, roomPool.getMediumFunctionalRoom(this.random, staircase), blockPos15, blockRotation.rotate(BlockRotation.CLOCKWISE_90), BlockMirror.LEFT_RIGHT));
         } else if (direction2 == Direction.SOUTH && direction == Direction.WEST) {
            blockPos15 = blockPos.offset((Direction)blockRotation.rotate(Direction.EAST), 7);
            list.add(new WoodlandMansionGenerator.Piece(this.manager, roomPool.getMediumFunctionalRoom(this.random, staircase), blockPos15, blockRotation.rotate(BlockRotation.CLOCKWISE_90)));
         } else if (direction2 == Direction.NORTH && direction == Direction.WEST) {
            blockPos15 = blockPos.offset((Direction)blockRotation.rotate(Direction.EAST), 7);
            blockPos15 = blockPos15.offset((Direction)blockRotation.rotate(Direction.SOUTH), 6);
            list.add(new WoodlandMansionGenerator.Piece(this.manager, roomPool.getMediumFunctionalRoom(this.random, staircase), blockPos15, blockRotation.rotate(BlockRotation.CLOCKWISE_90), BlockMirror.FRONT_BACK));
         } else if (direction2 == Direction.NORTH && direction == Direction.EAST) {
            blockPos15 = blockPos.offset((Direction)blockRotation.rotate(Direction.EAST), 1);
            blockPos15 = blockPos15.offset((Direction)blockRotation.rotate(Direction.SOUTH), 6);
            list.add(new WoodlandMansionGenerator.Piece(this.manager, roomPool.getMediumFunctionalRoom(this.random, staircase), blockPos15, blockRotation.rotate(BlockRotation.COUNTERCLOCKWISE_90)));
         } else if (direction2 == Direction.SOUTH && direction == Direction.NORTH) {
            blockPos15 = blockPos.offset((Direction)blockRotation.rotate(Direction.EAST), 1);
            blockPos15 = blockPos15.offset((Direction)blockRotation.rotate(Direction.NORTH), 8);
            list.add(new WoodlandMansionGenerator.Piece(this.manager, roomPool.getMediumGenericRoom(this.random, staircase), blockPos15, blockRotation));
         } else if (direction2 == Direction.NORTH && direction == Direction.SOUTH) {
            blockPos15 = blockPos.offset((Direction)blockRotation.rotate(Direction.EAST), 7);
            blockPos15 = blockPos15.offset((Direction)blockRotation.rotate(Direction.SOUTH), 14);
            list.add(new WoodlandMansionGenerator.Piece(this.manager, roomPool.getMediumGenericRoom(this.random, staircase), blockPos15, blockRotation.rotate(BlockRotation.CLOCKWISE_180)));
         } else if (direction2 == Direction.WEST && direction == Direction.EAST) {
            blockPos15 = blockPos.offset((Direction)blockRotation.rotate(Direction.EAST), 15);
            list.add(new WoodlandMansionGenerator.Piece(this.manager, roomPool.getMediumGenericRoom(this.random, staircase), blockPos15, blockRotation.rotate(BlockRotation.CLOCKWISE_90)));
         } else if (direction2 == Direction.EAST && direction == Direction.WEST) {
            blockPos15 = blockPos.offset((Direction)blockRotation.rotate(Direction.WEST), 7);
            blockPos15 = blockPos15.offset((Direction)blockRotation.rotate(Direction.SOUTH), 6);
            list.add(new WoodlandMansionGenerator.Piece(this.manager, roomPool.getMediumGenericRoom(this.random, staircase), blockPos15, blockRotation.rotate(BlockRotation.COUNTERCLOCKWISE_90)));
         } else if (direction2 == Direction.UP && direction == Direction.EAST) {
            blockPos15 = blockPos.offset((Direction)blockRotation.rotate(Direction.EAST), 15);
            list.add(new WoodlandMansionGenerator.Piece(this.manager, roomPool.getMediumSecretRoom(this.random), blockPos15, blockRotation.rotate(BlockRotation.CLOCKWISE_90)));
         } else if (direction2 == Direction.UP && direction == Direction.SOUTH) {
            blockPos15 = blockPos.offset((Direction)blockRotation.rotate(Direction.EAST), 1);
            blockPos15 = blockPos15.offset((Direction)blockRotation.rotate(Direction.NORTH), 0);
            list.add(new WoodlandMansionGenerator.Piece(this.manager, roomPool.getMediumSecretRoom(this.random), blockPos15, blockRotation));
         }

      }

      private void addBigRoom(List<WoodlandMansionGenerator.Piece> list, BlockPos blockPos, BlockRotation blockRotation, Direction direction, Direction direction2, WoodlandMansionGenerator.RoomPool roomPool) {
         int i = 0;
         int j = 0;
         BlockRotation blockRotation2 = blockRotation;
         BlockMirror blockMirror = BlockMirror.NONE;
         if (direction2 == Direction.EAST && direction == Direction.SOUTH) {
            i = -7;
         } else if (direction2 == Direction.EAST && direction == Direction.NORTH) {
            i = -7;
            j = 6;
            blockMirror = BlockMirror.LEFT_RIGHT;
         } else if (direction2 == Direction.NORTH && direction == Direction.EAST) {
            i = 1;
            j = 14;
            blockRotation2 = blockRotation.rotate(BlockRotation.COUNTERCLOCKWISE_90);
         } else if (direction2 == Direction.NORTH && direction == Direction.WEST) {
            i = 7;
            j = 14;
            blockRotation2 = blockRotation.rotate(BlockRotation.COUNTERCLOCKWISE_90);
            blockMirror = BlockMirror.LEFT_RIGHT;
         } else if (direction2 == Direction.SOUTH && direction == Direction.WEST) {
            i = 7;
            j = -8;
            blockRotation2 = blockRotation.rotate(BlockRotation.CLOCKWISE_90);
         } else if (direction2 == Direction.SOUTH && direction == Direction.EAST) {
            i = 1;
            j = -8;
            blockRotation2 = blockRotation.rotate(BlockRotation.CLOCKWISE_90);
            blockMirror = BlockMirror.LEFT_RIGHT;
         } else if (direction2 == Direction.WEST && direction == Direction.NORTH) {
            i = 15;
            j = 6;
            blockRotation2 = blockRotation.rotate(BlockRotation.CLOCKWISE_180);
         } else if (direction2 == Direction.WEST && direction == Direction.SOUTH) {
            i = 15;
            blockMirror = BlockMirror.FRONT_BACK;
         }

         BlockPos blockPos2 = blockPos.offset((Direction)blockRotation.rotate(Direction.EAST), i);
         blockPos2 = blockPos2.offset((Direction)blockRotation.rotate(Direction.SOUTH), j);
         list.add(new WoodlandMansionGenerator.Piece(this.manager, roomPool.getBigRoom(this.random), blockPos2, blockRotation2, blockMirror));
      }

      private void addBigSecretRoom(List<WoodlandMansionGenerator.Piece> list, BlockPos blockPos, BlockRotation blockRotation, WoodlandMansionGenerator.RoomPool roomPool) {
         BlockPos blockPos2 = blockPos.offset((Direction)blockRotation.rotate(Direction.EAST), 1);
         list.add(new WoodlandMansionGenerator.Piece(this.manager, roomPool.getBigSecretRoom(this.random), blockPos2, blockRotation, BlockMirror.NONE));
      }
   }

   static class GenerationPiece {
      public BlockRotation rotation;
      public BlockPos position;
      public String template;

      private GenerationPiece() {
      }
   }

   public static class Piece extends SimpleStructurePiece {
      private final String template;
      private final BlockRotation rotation;
      private final BlockMirror mirror;

      public Piece(StructureManager structureManager, String string, BlockPos blockPos, BlockRotation blockRotation) {
         this(structureManager, string, blockPos, blockRotation, BlockMirror.NONE);
      }

      public Piece(StructureManager structureManager, String string, BlockPos blockPos, BlockRotation blockRotation, BlockMirror blockMirror) {
         super(StructurePieceType.WOODLAND_MANSION, 0);
         this.template = string;
         this.pos = blockPos;
         this.rotation = blockRotation;
         this.mirror = blockMirror;
         this.setupPlacement(structureManager);
      }

      public Piece(StructureManager structureManager, CompoundTag compoundTag) {
         super(StructurePieceType.WOODLAND_MANSION, compoundTag);
         this.template = compoundTag.getString("Template");
         this.rotation = BlockRotation.valueOf(compoundTag.getString("Rot"));
         this.mirror = BlockMirror.valueOf(compoundTag.getString("Mi"));
         this.setupPlacement(structureManager);
      }

      private void setupPlacement(StructureManager structureManager) {
         Structure structure = structureManager.getStructureOrBlank(new Identifier("woodland_mansion/" + this.template));
         StructurePlacementData structurePlacementData = (new StructurePlacementData()).setIgnoreEntities(true).setRotation(this.rotation).setMirror(this.mirror).addProcessor(BlockIgnoreStructureProcessor.IGNORE_STRUCTURE_BLOCKS);
         this.setStructureData(structure, this.pos, structurePlacementData);
      }

      protected void toNbt(CompoundTag tag) {
         super.toNbt(tag);
         tag.putString("Template", this.template);
         tag.putString("Rot", this.placementData.getRotation().name());
         tag.putString("Mi", this.placementData.getMirror().name());
      }

      protected void handleMetadata(String metadata, BlockPos pos, ServerWorldAccess serverWorldAccess, Random random, BlockBox boundingBox) {
         if (metadata.startsWith("Chest")) {
            BlockRotation blockRotation = this.placementData.getRotation();
            BlockState blockState = Blocks.CHEST.getDefaultState();
            if ("ChestWest".equals(metadata)) {
               blockState = (BlockState)blockState.with(ChestBlock.FACING, blockRotation.rotate(Direction.WEST));
            } else if ("ChestEast".equals(metadata)) {
               blockState = (BlockState)blockState.with(ChestBlock.FACING, blockRotation.rotate(Direction.EAST));
            } else if ("ChestSouth".equals(metadata)) {
               blockState = (BlockState)blockState.with(ChestBlock.FACING, blockRotation.rotate(Direction.SOUTH));
            } else if ("ChestNorth".equals(metadata)) {
               blockState = (BlockState)blockState.with(ChestBlock.FACING, blockRotation.rotate(Direction.NORTH));
            }

            this.addChest(serverWorldAccess, boundingBox, random, pos, LootTables.WOODLAND_MANSION_CHEST, blockState);
         } else {
            byte var8 = -1;
            switch(metadata.hashCode()) {
            case -1505748702:
               if (metadata.equals("Warrior")) {
                  var8 = 1;
               }
               break;
            case 2390418:
               if (metadata.equals("Mage")) {
                  var8 = 0;
               }
            }

            IllagerEntity illagerEntity3;
            switch(var8) {
            case 0:
               illagerEntity3 = (IllagerEntity)EntityType.EVOKER.create(serverWorldAccess.toServerWorld());
               break;
            case 1:
               illagerEntity3 = (IllagerEntity)EntityType.VINDICATOR.create(serverWorldAccess.toServerWorld());
               break;
            default:
               return;
            }

            illagerEntity3.setPersistent();
            illagerEntity3.refreshPositionAndAngles(pos, 0.0F, 0.0F);
            illagerEntity3.initialize(serverWorldAccess, serverWorldAccess.getLocalDifficulty(illagerEntity3.getBlockPos()), SpawnReason.STRUCTURE, (EntityData)null, (CompoundTag)null);
            serverWorldAccess.spawnEntityAndPassengers(illagerEntity3);
            serverWorldAccess.setBlockState(pos, Blocks.AIR.getDefaultState(), 2);
         }

      }
   }
}
