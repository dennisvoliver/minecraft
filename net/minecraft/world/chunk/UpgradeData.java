package net.minecraft.world.chunk;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import java.util.EnumSet;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.GourdBlock;
import net.minecraft.block.HorizontalFacingBlock;
import net.minecraft.block.StemBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.block.enums.ChestType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.state.property.Properties;
import net.minecraft.util.EightWayDirection;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class UpgradeData {
   private static final Logger LOGGER = LogManager.getLogger();
   public static final UpgradeData NO_UPGRADE_DATA = new UpgradeData();
   private static final EightWayDirection[] EIGHT_WAYS = EightWayDirection.values();
   private final EnumSet<EightWayDirection> sidesToUpgrade;
   private final int[][] centerIndicesToUpgrade;
   private static final Map<Block, UpgradeData.Logic> BLOCK_TO_LOGIC = new IdentityHashMap();
   private static final Set<UpgradeData.Logic> CALLBACK_LOGICS = Sets.newHashSet();

   private UpgradeData() {
      this.sidesToUpgrade = EnumSet.noneOf(EightWayDirection.class);
      this.centerIndicesToUpgrade = new int[16][];
   }

   public UpgradeData(CompoundTag tag) {
      this();
      if (tag.contains("Indices", 10)) {
         CompoundTag compoundTag = tag.getCompound("Indices");

         for(int i = 0; i < this.centerIndicesToUpgrade.length; ++i) {
            String string = String.valueOf(i);
            if (compoundTag.contains(string, 11)) {
               this.centerIndicesToUpgrade[i] = compoundTag.getIntArray(string);
            }
         }
      }

      int j = tag.getInt("Sides");
      EightWayDirection[] var8 = EightWayDirection.values();
      int var9 = var8.length;

      for(int var5 = 0; var5 < var9; ++var5) {
         EightWayDirection eightWayDirection = var8[var5];
         if ((j & 1 << eightWayDirection.ordinal()) != 0) {
            this.sidesToUpgrade.add(eightWayDirection);
         }
      }

   }

   public void upgrade(WorldChunk chunk) {
      this.upgradeCenter(chunk);
      EightWayDirection[] var2 = EIGHT_WAYS;
      int var3 = var2.length;

      for(int var4 = 0; var4 < var3; ++var4) {
         EightWayDirection eightWayDirection = var2[var4];
         upgradeSide(chunk, eightWayDirection);
      }

      World world = chunk.getWorld();
      CALLBACK_LOGICS.forEach((logic) -> {
         logic.postUpdate(world);
      });
   }

   private static void upgradeSide(WorldChunk chunk, EightWayDirection side) {
      World world = chunk.getWorld();
      if (chunk.getUpgradeData().sidesToUpgrade.remove(side)) {
         Set<Direction> set = side.getDirections();
         int i = false;
         int j = true;
         boolean bl = set.contains(Direction.EAST);
         boolean bl2 = set.contains(Direction.WEST);
         boolean bl3 = set.contains(Direction.SOUTH);
         boolean bl4 = set.contains(Direction.NORTH);
         boolean bl5 = set.size() == 1;
         ChunkPos chunkPos = chunk.getPos();
         int k = chunkPos.getStartX() + (bl5 && (bl4 || bl3) ? 1 : (bl2 ? 0 : 15));
         int l = chunkPos.getStartX() + (bl5 && (bl4 || bl3) ? 14 : (bl2 ? 0 : 15));
         int m = chunkPos.getStartZ() + (!bl5 || !bl && !bl2 ? (bl4 ? 0 : 15) : 1);
         int n = chunkPos.getStartZ() + (bl5 && (bl || bl2) ? 14 : (bl4 ? 0 : 15));
         Direction[] directions = Direction.values();
         BlockPos.Mutable mutable = new BlockPos.Mutable();
         Iterator var18 = BlockPos.iterate(k, 0, m, l, world.getHeight() - 1, n).iterator();

         while(var18.hasNext()) {
            BlockPos blockPos = (BlockPos)var18.next();
            BlockState blockState = world.getBlockState(blockPos);
            BlockState blockState2 = blockState;
            Direction[] var22 = directions;
            int var23 = directions.length;

            for(int var24 = 0; var24 < var23; ++var24) {
               Direction direction = var22[var24];
               mutable.set(blockPos, direction);
               blockState2 = applyAdjacentBlock(blockState2, direction, world, blockPos, mutable);
            }

            Block.replace(blockState, blockState2, world, blockPos, 18);
         }

      }
   }

   private static BlockState applyAdjacentBlock(BlockState oldState, Direction dir, WorldAccess world, BlockPos currentPos, BlockPos otherPos) {
      return ((UpgradeData.Logic)BLOCK_TO_LOGIC.getOrDefault(oldState.getBlock(), UpgradeData.BuiltinLogic.DEFAULT)).getUpdatedState(oldState, dir, world.getBlockState(otherPos), world, currentPos, otherPos);
   }

   private void upgradeCenter(WorldChunk chunk) {
      BlockPos.Mutable mutable = new BlockPos.Mutable();
      BlockPos.Mutable mutable2 = new BlockPos.Mutable();
      ChunkPos chunkPos = chunk.getPos();
      WorldAccess worldAccess = chunk.getWorld();

      int i;
      for(i = 0; i < 16; ++i) {
         ChunkSection chunkSection = chunk.getSectionArray()[i];
         int[] is = this.centerIndicesToUpgrade[i];
         this.centerIndicesToUpgrade[i] = null;
         if (chunkSection != null && is != null && is.length > 0) {
            Direction[] directions = Direction.values();
            PalettedContainer<BlockState> palettedContainer = chunkSection.getContainer();
            int[] var11 = is;
            int var12 = is.length;

            for(int var13 = 0; var13 < var12; ++var13) {
               int j = var11[var13];
               int k = j & 15;
               int l = j >> 8 & 15;
               int m = j >> 4 & 15;
               mutable.set(chunkPos.getStartX() + k, (i << 4) + l, chunkPos.getStartZ() + m);
               BlockState blockState = (BlockState)palettedContainer.get(j);
               BlockState blockState2 = blockState;
               Direction[] var20 = directions;
               int var21 = directions.length;

               for(int var22 = 0; var22 < var21; ++var22) {
                  Direction direction = var20[var22];
                  mutable2.set(mutable, direction);
                  if (mutable.getX() >> 4 == chunkPos.x && mutable.getZ() >> 4 == chunkPos.z) {
                     blockState2 = applyAdjacentBlock(blockState2, direction, worldAccess, mutable, mutable2);
                  }
               }

               Block.replace(blockState, blockState2, worldAccess, mutable, 18);
            }
         }
      }

      for(i = 0; i < this.centerIndicesToUpgrade.length; ++i) {
         if (this.centerIndicesToUpgrade[i] != null) {
            LOGGER.warn((String)"Discarding update data for section {} for chunk ({} {})", (Object)i, chunkPos.x, chunkPos.z);
         }

         this.centerIndicesToUpgrade[i] = null;
      }

   }

   public boolean isDone() {
      int[][] var1 = this.centerIndicesToUpgrade;
      int var2 = var1.length;

      for(int var3 = 0; var3 < var2; ++var3) {
         int[] is = var1[var3];
         if (is != null) {
            return false;
         }
      }

      return this.sidesToUpgrade.isEmpty();
   }

   public CompoundTag toTag() {
      CompoundTag compoundTag = new CompoundTag();
      CompoundTag compoundTag2 = new CompoundTag();

      int j;
      for(j = 0; j < this.centerIndicesToUpgrade.length; ++j) {
         String string = String.valueOf(j);
         if (this.centerIndicesToUpgrade[j] != null && this.centerIndicesToUpgrade[j].length != 0) {
            compoundTag2.putIntArray(string, this.centerIndicesToUpgrade[j]);
         }
      }

      if (!compoundTag2.isEmpty()) {
         compoundTag.put("Indices", compoundTag2);
      }

      j = 0;

      EightWayDirection eightWayDirection;
      for(Iterator var6 = this.sidesToUpgrade.iterator(); var6.hasNext(); j |= 1 << eightWayDirection.ordinal()) {
         eightWayDirection = (EightWayDirection)var6.next();
      }

      compoundTag.putByte("Sides", (byte)j);
      return compoundTag;
   }

   static enum BuiltinLogic implements UpgradeData.Logic {
      BLACKLIST(new Block[]{Blocks.OBSERVER, Blocks.NETHER_PORTAL, Blocks.WHITE_CONCRETE_POWDER, Blocks.ORANGE_CONCRETE_POWDER, Blocks.MAGENTA_CONCRETE_POWDER, Blocks.LIGHT_BLUE_CONCRETE_POWDER, Blocks.YELLOW_CONCRETE_POWDER, Blocks.LIME_CONCRETE_POWDER, Blocks.PINK_CONCRETE_POWDER, Blocks.GRAY_CONCRETE_POWDER, Blocks.LIGHT_GRAY_CONCRETE_POWDER, Blocks.CYAN_CONCRETE_POWDER, Blocks.PURPLE_CONCRETE_POWDER, Blocks.BLUE_CONCRETE_POWDER, Blocks.BROWN_CONCRETE_POWDER, Blocks.GREEN_CONCRETE_POWDER, Blocks.RED_CONCRETE_POWDER, Blocks.BLACK_CONCRETE_POWDER, Blocks.ANVIL, Blocks.CHIPPED_ANVIL, Blocks.DAMAGED_ANVIL, Blocks.DRAGON_EGG, Blocks.GRAVEL, Blocks.SAND, Blocks.RED_SAND, Blocks.OAK_SIGN, Blocks.SPRUCE_SIGN, Blocks.BIRCH_SIGN, Blocks.ACACIA_SIGN, Blocks.JUNGLE_SIGN, Blocks.DARK_OAK_SIGN, Blocks.OAK_WALL_SIGN, Blocks.SPRUCE_WALL_SIGN, Blocks.BIRCH_WALL_SIGN, Blocks.ACACIA_WALL_SIGN, Blocks.JUNGLE_WALL_SIGN, Blocks.DARK_OAK_WALL_SIGN}) {
         public BlockState getUpdatedState(BlockState blockState, Direction direction, BlockState blockState2, WorldAccess worldAccess, BlockPos blockPos, BlockPos blockPos2) {
            return blockState;
         }
      },
      DEFAULT(new Block[0]) {
         public BlockState getUpdatedState(BlockState blockState, Direction direction, BlockState blockState2, WorldAccess worldAccess, BlockPos blockPos, BlockPos blockPos2) {
            return blockState.getStateForNeighborUpdate(direction, worldAccess.getBlockState(blockPos2), worldAccess, blockPos, blockPos2);
         }
      },
      CHEST(new Block[]{Blocks.CHEST, Blocks.TRAPPED_CHEST}) {
         public BlockState getUpdatedState(BlockState blockState, Direction direction, BlockState blockState2, WorldAccess worldAccess, BlockPos blockPos, BlockPos blockPos2) {
            if (blockState2.isOf(blockState.getBlock()) && direction.getAxis().isHorizontal() && blockState.get(ChestBlock.CHEST_TYPE) == ChestType.SINGLE && blockState2.get(ChestBlock.CHEST_TYPE) == ChestType.SINGLE) {
               Direction direction2 = (Direction)blockState.get(ChestBlock.FACING);
               if (direction.getAxis() != direction2.getAxis() && direction2 == blockState2.get(ChestBlock.FACING)) {
                  ChestType chestType = direction == direction2.rotateYClockwise() ? ChestType.LEFT : ChestType.RIGHT;
                  worldAccess.setBlockState(blockPos2, (BlockState)blockState2.with(ChestBlock.CHEST_TYPE, chestType.getOpposite()), 18);
                  if (direction2 == Direction.NORTH || direction2 == Direction.EAST) {
                     BlockEntity blockEntity = worldAccess.getBlockEntity(blockPos);
                     BlockEntity blockEntity2 = worldAccess.getBlockEntity(blockPos2);
                     if (blockEntity instanceof ChestBlockEntity && blockEntity2 instanceof ChestBlockEntity) {
                        ChestBlockEntity.copyInventory((ChestBlockEntity)blockEntity, (ChestBlockEntity)blockEntity2);
                     }
                  }

                  return (BlockState)blockState.with(ChestBlock.CHEST_TYPE, chestType);
               }
            }

            return blockState;
         }
      },
      LEAVES(true, new Block[]{Blocks.ACACIA_LEAVES, Blocks.BIRCH_LEAVES, Blocks.DARK_OAK_LEAVES, Blocks.JUNGLE_LEAVES, Blocks.OAK_LEAVES, Blocks.SPRUCE_LEAVES}) {
         private final ThreadLocal<List<ObjectSet<BlockPos>>> distanceToPositions = ThreadLocal.withInitial(() -> {
            return Lists.newArrayListWithCapacity(7);
         });

         public BlockState getUpdatedState(BlockState blockState, Direction direction, BlockState blockState2, WorldAccess worldAccess, BlockPos blockPos, BlockPos blockPos2) {
            BlockState blockState3 = blockState.getStateForNeighborUpdate(direction, worldAccess.getBlockState(blockPos2), worldAccess, blockPos, blockPos2);
            if (blockState != blockState3) {
               int i = (Integer)blockState3.get(Properties.DISTANCE_1_7);
               List<ObjectSet<BlockPos>> list = (List)this.distanceToPositions.get();
               if (list.isEmpty()) {
                  for(int j = 0; j < 7; ++j) {
                     list.add(new ObjectOpenHashSet());
                  }
               }

               ((ObjectSet)list.get(i)).add(blockPos.toImmutable());
            }

            return blockState;
         }

         public void postUpdate(WorldAccess world) {
            BlockPos.Mutable mutable = new BlockPos.Mutable();
            List<ObjectSet<BlockPos>> list = (List)this.distanceToPositions.get();

            label44:
            for(int i = 2; i < list.size(); ++i) {
               int j = i - 1;
               ObjectSet<BlockPos> objectSet = (ObjectSet)list.get(j);
               ObjectSet<BlockPos> objectSet2 = (ObjectSet)list.get(i);
               ObjectIterator var8 = objectSet.iterator();

               while(true) {
                  BlockPos blockPos;
                  BlockState blockState;
                  do {
                     do {
                        if (!var8.hasNext()) {
                           continue label44;
                        }

                        blockPos = (BlockPos)var8.next();
                        blockState = world.getBlockState(blockPos);
                     } while((Integer)blockState.get(Properties.DISTANCE_1_7) < j);

                     world.setBlockState(blockPos, (BlockState)blockState.with(Properties.DISTANCE_1_7, j), 18);
                  } while(i == 7);

                  Direction[] var11 = DIRECTIONS;
                  int var12 = var11.length;

                  for(int var13 = 0; var13 < var12; ++var13) {
                     Direction direction = var11[var13];
                     mutable.set(blockPos, direction);
                     BlockState blockState2 = world.getBlockState(mutable);
                     if (blockState2.contains(Properties.DISTANCE_1_7) && (Integer)blockState.get(Properties.DISTANCE_1_7) > i) {
                        objectSet2.add(mutable.toImmutable());
                     }
                  }
               }
            }

            list.clear();
         }
      },
      STEM_BLOCK(new Block[]{Blocks.MELON_STEM, Blocks.PUMPKIN_STEM}) {
         public BlockState getUpdatedState(BlockState blockState, Direction direction, BlockState blockState2, WorldAccess worldAccess, BlockPos blockPos, BlockPos blockPos2) {
            if ((Integer)blockState.get(StemBlock.AGE) == 7) {
               GourdBlock gourdBlock = ((StemBlock)blockState.getBlock()).getGourdBlock();
               if (blockState2.isOf(gourdBlock)) {
                  return (BlockState)gourdBlock.getAttachedStem().getDefaultState().with(HorizontalFacingBlock.FACING, direction);
               }
            }

            return blockState;
         }
      };

      public static final Direction[] DIRECTIONS = Direction.values();

      private BuiltinLogic(Block... blocks) {
         this(false, blocks);
      }

      private BuiltinLogic(boolean bl, Block... blocks) {
         Block[] var5 = blocks;
         int var6 = blocks.length;

         for(int var7 = 0; var7 < var6; ++var7) {
            Block block = var5[var7];
            UpgradeData.BLOCK_TO_LOGIC.put(block, this);
         }

         if (bl) {
            UpgradeData.CALLBACK_LOGICS.add(this);
         }

      }
   }

   public interface Logic {
      BlockState getUpdatedState(BlockState blockState, Direction direction, BlockState blockState2, WorldAccess worldAccess, BlockPos blockPos, BlockPos blockPos2);

      default void postUpdate(WorldAccess world) {
      }
   }
}
