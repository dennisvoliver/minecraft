package net.minecraft.structure;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.datafixers.util.Pair;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;
import net.minecraft.SharedConstants;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FluidFillable;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.LootableContainerBlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityData;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.decoration.painting.PaintingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.structure.processor.StructureProcessor;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Clearable;
import net.minecraft.util.collection.IdList;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.BitSetVoxelSet;
import net.minecraft.util.shape.VoxelSet;
import net.minecraft.world.EmptyBlockView;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import org.jetbrains.annotations.Nullable;

public class Structure {
   private final List<Structure.PalettedBlockInfoList> blockInfoLists = Lists.newArrayList();
   private final List<Structure.StructureEntityInfo> entities = Lists.newArrayList();
   private BlockPos size;
   private String author;

   public Structure() {
      this.size = BlockPos.ORIGIN;
      this.author = "?";
   }

   public BlockPos getSize() {
      return this.size;
   }

   public void setAuthor(String name) {
      this.author = name;
   }

   public String getAuthor() {
      return this.author;
   }

   public void saveFromWorld(World world, BlockPos start, BlockPos size, boolean includeEntities, @Nullable Block ignoredBlock) {
      if (size.getX() >= 1 && size.getY() >= 1 && size.getZ() >= 1) {
         BlockPos blockPos = start.add(size).add(-1, -1, -1);
         List<Structure.StructureBlockInfo> list = Lists.newArrayList();
         List<Structure.StructureBlockInfo> list2 = Lists.newArrayList();
         List<Structure.StructureBlockInfo> list3 = Lists.newArrayList();
         BlockPos blockPos2 = new BlockPos(Math.min(start.getX(), blockPos.getX()), Math.min(start.getY(), blockPos.getY()), Math.min(start.getZ(), blockPos.getZ()));
         BlockPos blockPos3 = new BlockPos(Math.max(start.getX(), blockPos.getX()), Math.max(start.getY(), blockPos.getY()), Math.max(start.getZ(), blockPos.getZ()));
         this.size = size;
         Iterator var12 = BlockPos.iterate(blockPos2, blockPos3).iterator();

         while(true) {
            BlockPos blockPos4;
            BlockPos blockPos5;
            BlockState blockState;
            do {
               if (!var12.hasNext()) {
                  List<Structure.StructureBlockInfo> list4 = method_28055(list, list2, list3);
                  this.blockInfoLists.clear();
                  this.blockInfoLists.add(new Structure.PalettedBlockInfoList(list4));
                  if (includeEntities) {
                     this.addEntitiesFromWorld(world, blockPos2, blockPos3.add(1, 1, 1));
                  } else {
                     this.entities.clear();
                  }

                  return;
               }

               blockPos4 = (BlockPos)var12.next();
               blockPos5 = blockPos4.subtract(blockPos2);
               blockState = world.getBlockState(blockPos4);
            } while(ignoredBlock != null && ignoredBlock == blockState.getBlock());

            BlockEntity blockEntity = world.getBlockEntity(blockPos4);
            Structure.StructureBlockInfo structureBlockInfo2;
            if (blockEntity != null) {
               CompoundTag compoundTag = blockEntity.toTag(new CompoundTag());
               compoundTag.remove("x");
               compoundTag.remove("y");
               compoundTag.remove("z");
               structureBlockInfo2 = new Structure.StructureBlockInfo(blockPos5, blockState, compoundTag.copy());
            } else {
               structureBlockInfo2 = new Structure.StructureBlockInfo(blockPos5, blockState, (CompoundTag)null);
            }

            method_28054(structureBlockInfo2, list, list2, list3);
         }
      }
   }

   private static void method_28054(Structure.StructureBlockInfo structureBlockInfo, List<Structure.StructureBlockInfo> list, List<Structure.StructureBlockInfo> list2, List<Structure.StructureBlockInfo> list3) {
      if (structureBlockInfo.tag != null) {
         list2.add(structureBlockInfo);
      } else if (!structureBlockInfo.state.getBlock().hasDynamicBounds() && structureBlockInfo.state.isFullCube(EmptyBlockView.INSTANCE, BlockPos.ORIGIN)) {
         list.add(structureBlockInfo);
      } else {
         list3.add(structureBlockInfo);
      }

   }

   private static List<Structure.StructureBlockInfo> method_28055(List<Structure.StructureBlockInfo> list, List<Structure.StructureBlockInfo> list2, List<Structure.StructureBlockInfo> list3) {
      Comparator<Structure.StructureBlockInfo> comparator = Comparator.comparingInt((structureBlockInfo) -> {
         return structureBlockInfo.pos.getY();
      }).thenComparingInt((structureBlockInfo) -> {
         return structureBlockInfo.pos.getX();
      }).thenComparingInt((structureBlockInfo) -> {
         return structureBlockInfo.pos.getZ();
      });
      list.sort(comparator);
      list3.sort(comparator);
      list2.sort(comparator);
      List<Structure.StructureBlockInfo> list4 = Lists.newArrayList();
      list4.addAll(list);
      list4.addAll(list3);
      list4.addAll(list2);
      return list4;
   }

   private void addEntitiesFromWorld(World world, BlockPos firstCorner, BlockPos secondCorner) {
      List<Entity> list = world.getEntitiesByClass(Entity.class, new Box(firstCorner, secondCorner), (entityx) -> {
         return !(entityx instanceof PlayerEntity);
      });
      this.entities.clear();

      Vec3d vec3d;
      CompoundTag compoundTag;
      BlockPos blockPos2;
      for(Iterator var5 = list.iterator(); var5.hasNext(); this.entities.add(new Structure.StructureEntityInfo(vec3d, blockPos2, compoundTag.copy()))) {
         Entity entity = (Entity)var5.next();
         vec3d = new Vec3d(entity.getX() - (double)firstCorner.getX(), entity.getY() - (double)firstCorner.getY(), entity.getZ() - (double)firstCorner.getZ());
         compoundTag = new CompoundTag();
         entity.saveToTag(compoundTag);
         if (entity instanceof PaintingEntity) {
            blockPos2 = ((PaintingEntity)entity).getDecorationBlockPos().subtract(firstCorner);
         } else {
            blockPos2 = new BlockPos(vec3d);
         }
      }

   }

   public List<Structure.StructureBlockInfo> getInfosForBlock(BlockPos pos, StructurePlacementData placementData, Block block) {
      return this.getInfosForBlock(pos, placementData, block, true);
   }

   public List<Structure.StructureBlockInfo> getInfosForBlock(BlockPos pos, StructurePlacementData placementData, Block block, boolean transformed) {
      List<Structure.StructureBlockInfo> list = Lists.newArrayList();
      BlockBox blockBox = placementData.getBoundingBox();
      if (this.blockInfoLists.isEmpty()) {
         return Collections.emptyList();
      } else {
         Iterator var7 = placementData.getRandomBlockInfos(this.blockInfoLists, pos).getAllOf(block).iterator();

         while(true) {
            Structure.StructureBlockInfo structureBlockInfo;
            BlockPos blockPos;
            do {
               if (!var7.hasNext()) {
                  return list;
               }

               structureBlockInfo = (Structure.StructureBlockInfo)var7.next();
               blockPos = transformed ? transform(placementData, structureBlockInfo.pos).add(pos) : structureBlockInfo.pos;
            } while(blockBox != null && !blockBox.contains(blockPos));

            list.add(new Structure.StructureBlockInfo(blockPos, structureBlockInfo.state.rotate(placementData.getRotation()), structureBlockInfo.tag));
         }
      }
   }

   public BlockPos transformBox(StructurePlacementData placementData1, BlockPos pos1, StructurePlacementData placementData2, BlockPos pos2) {
      BlockPos blockPos = transform(placementData1, pos1);
      BlockPos blockPos2 = transform(placementData2, pos2);
      return blockPos.subtract(blockPos2);
   }

   public static BlockPos transform(StructurePlacementData placementData, BlockPos pos) {
      return transformAround(pos, placementData.getMirror(), placementData.getRotation(), placementData.getPosition());
   }

   public void place(ServerWorldAccess serverWorldAccess, BlockPos pos, StructurePlacementData placementData, Random random) {
      placementData.calculateBoundingBox();
      this.placeAndNotifyListeners(serverWorldAccess, pos, placementData, random);
   }

   public void placeAndNotifyListeners(ServerWorldAccess serverWorldAccess, BlockPos pos, StructurePlacementData data, Random random) {
      this.place(serverWorldAccess, pos, pos, data, random, 2);
   }

   public boolean place(ServerWorldAccess serverWorldAccess, BlockPos pos, BlockPos blockPos, StructurePlacementData placementData, Random random, int i) {
      if (this.blockInfoLists.isEmpty()) {
         return false;
      } else {
         List<Structure.StructureBlockInfo> list = placementData.getRandomBlockInfos(this.blockInfoLists, pos).getAll();
         if ((!list.isEmpty() || !placementData.shouldIgnoreEntities() && !this.entities.isEmpty()) && this.size.getX() >= 1 && this.size.getY() >= 1 && this.size.getZ() >= 1) {
            BlockBox blockBox = placementData.getBoundingBox();
            List<BlockPos> list2 = Lists.newArrayListWithCapacity(placementData.shouldPlaceFluids() ? list.size() : 0);
            List<Pair<BlockPos, CompoundTag>> list3 = Lists.newArrayListWithCapacity(list.size());
            int j = Integer.MAX_VALUE;
            int k = Integer.MAX_VALUE;
            int l = Integer.MAX_VALUE;
            int m = Integer.MIN_VALUE;
            int n = Integer.MIN_VALUE;
            int o = Integer.MIN_VALUE;
            List<Structure.StructureBlockInfo> list4 = process(serverWorldAccess, pos, blockPos, placementData, list);
            Iterator var18 = list4.iterator();

            while(true) {
               Structure.StructureBlockInfo structureBlockInfo;
               BlockPos blockPos2;
               BlockEntity blockEntity3;
               do {
                  if (!var18.hasNext()) {
                     boolean bl = true;
                     Direction[] directions = new Direction[]{Direction.UP, Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST};

                     Iterator iterator;
                     BlockPos blockPos7;
                     BlockState blockState4;
                     while(bl && !list2.isEmpty()) {
                        bl = false;
                        iterator = list2.iterator();

                        while(iterator.hasNext()) {
                           BlockPos blockPos3 = (BlockPos)iterator.next();
                           blockPos7 = blockPos3;
                           FluidState fluidState2 = serverWorldAccess.getFluidState(blockPos3);

                           for(int p = 0; p < directions.length && !fluidState2.isStill(); ++p) {
                              BlockPos blockPos5 = blockPos7.offset(directions[p]);
                              FluidState fluidState3 = serverWorldAccess.getFluidState(blockPos5);
                              if (fluidState3.getHeight(serverWorldAccess, blockPos5) > fluidState2.getHeight(serverWorldAccess, blockPos7) || fluidState3.isStill() && !fluidState2.isStill()) {
                                 fluidState2 = fluidState3;
                                 blockPos7 = blockPos5;
                              }
                           }

                           if (fluidState2.isStill()) {
                              blockState4 = serverWorldAccess.getBlockState(blockPos3);
                              Block block = blockState4.getBlock();
                              if (block instanceof FluidFillable) {
                                 ((FluidFillable)block).tryFillWithFluid(serverWorldAccess, blockPos3, blockState4, fluidState2);
                                 bl = true;
                                 iterator.remove();
                              }
                           }
                        }
                     }

                     if (j <= m) {
                        if (!placementData.shouldUpdateNeighbors()) {
                           VoxelSet voxelSet = new BitSetVoxelSet(m - j + 1, n - k + 1, o - l + 1);
                           int q = j;
                           int r = k;
                           int s = l;
                           Iterator var40 = list3.iterator();

                           while(var40.hasNext()) {
                              Pair<BlockPos, CompoundTag> pair = (Pair)var40.next();
                              BlockPos blockPos6 = (BlockPos)pair.getFirst();
                              voxelSet.set(blockPos6.getX() - q, blockPos6.getY() - r, blockPos6.getZ() - s, true, true);
                           }

                           updateCorner(serverWorldAccess, i, voxelSet, q, r, s);
                        }

                        iterator = list3.iterator();

                        while(iterator.hasNext()) {
                           Pair<BlockPos, CompoundTag> pair2 = (Pair)iterator.next();
                           blockPos7 = (BlockPos)pair2.getFirst();
                           if (!placementData.shouldUpdateNeighbors()) {
                              BlockState blockState3 = serverWorldAccess.getBlockState(blockPos7);
                              blockState4 = Block.postProcessState(blockState3, serverWorldAccess, blockPos7);
                              if (blockState3 != blockState4) {
                                 serverWorldAccess.setBlockState(blockPos7, blockState4, i & -2 | 16);
                              }

                              serverWorldAccess.updateNeighbors(blockPos7, blockState4.getBlock());
                           }

                           if (pair2.getSecond() != null) {
                              blockEntity3 = serverWorldAccess.getBlockEntity(blockPos7);
                              if (blockEntity3 != null) {
                                 blockEntity3.markDirty();
                              }
                           }
                        }
                     }

                     if (!placementData.shouldIgnoreEntities()) {
                        this.spawnEntities(serverWorldAccess, pos, placementData.getMirror(), placementData.getRotation(), placementData.getPosition(), blockBox, placementData.method_27265());
                     }

                     return true;
                  }

                  structureBlockInfo = (Structure.StructureBlockInfo)var18.next();
                  blockPos2 = structureBlockInfo.pos;
               } while(blockBox != null && !blockBox.contains(blockPos2));

               FluidState fluidState = placementData.shouldPlaceFluids() ? serverWorldAccess.getFluidState(blockPos2) : null;
               BlockState blockState = structureBlockInfo.state.mirror(placementData.getMirror()).rotate(placementData.getRotation());
               if (structureBlockInfo.tag != null) {
                  blockEntity3 = serverWorldAccess.getBlockEntity(blockPos2);
                  Clearable.clear(blockEntity3);
                  serverWorldAccess.setBlockState(blockPos2, Blocks.BARRIER.getDefaultState(), 20);
               }

               if (serverWorldAccess.setBlockState(blockPos2, blockState, i)) {
                  j = Math.min(j, blockPos2.getX());
                  k = Math.min(k, blockPos2.getY());
                  l = Math.min(l, blockPos2.getZ());
                  m = Math.max(m, blockPos2.getX());
                  n = Math.max(n, blockPos2.getY());
                  o = Math.max(o, blockPos2.getZ());
                  list3.add(Pair.of(blockPos2, structureBlockInfo.tag));
                  if (structureBlockInfo.tag != null) {
                     blockEntity3 = serverWorldAccess.getBlockEntity(blockPos2);
                     if (blockEntity3 != null) {
                        structureBlockInfo.tag.putInt("x", blockPos2.getX());
                        structureBlockInfo.tag.putInt("y", blockPos2.getY());
                        structureBlockInfo.tag.putInt("z", blockPos2.getZ());
                        if (blockEntity3 instanceof LootableContainerBlockEntity) {
                           structureBlockInfo.tag.putLong("LootTableSeed", random.nextLong());
                        }

                        blockEntity3.fromTag(structureBlockInfo.state, structureBlockInfo.tag);
                        blockEntity3.applyMirror(placementData.getMirror());
                        blockEntity3.applyRotation(placementData.getRotation());
                     }
                  }

                  if (fluidState != null && blockState.getBlock() instanceof FluidFillable) {
                     ((FluidFillable)blockState.getBlock()).tryFillWithFluid(serverWorldAccess, blockPos2, blockState, fluidState);
                     if (!fluidState.isStill()) {
                        list2.add(blockPos2);
                     }
                  }
               }
            }
         } else {
            return false;
         }
      }
   }

   public static void updateCorner(WorldAccess world, int flags, VoxelSet voxelSet, int startX, int startY, int startZ) {
      voxelSet.forEachDirection((direction, m, n, o) -> {
         BlockPos blockPos = new BlockPos(startX + m, startY + n, startZ + o);
         BlockPos blockPos2 = blockPos.offset(direction);
         BlockState blockState = world.getBlockState(blockPos);
         BlockState blockState2 = world.getBlockState(blockPos2);
         BlockState blockState3 = blockState.getStateForNeighborUpdate(direction, blockState2, world, blockPos, blockPos2);
         if (blockState != blockState3) {
            world.setBlockState(blockPos, blockState3, flags & -2);
         }

         BlockState blockState4 = blockState2.getStateForNeighborUpdate(direction.getOpposite(), blockState3, world, blockPos2, blockPos);
         if (blockState2 != blockState4) {
            world.setBlockState(blockPos2, blockState4, flags & -2);
         }

      });
   }

   public static List<Structure.StructureBlockInfo> process(WorldAccess world, BlockPos pos, BlockPos blockPos, StructurePlacementData structurePlacementData, List<Structure.StructureBlockInfo> list) {
      List<Structure.StructureBlockInfo> list2 = Lists.newArrayList();
      Iterator var6 = list.iterator();

      while(var6.hasNext()) {
         Structure.StructureBlockInfo structureBlockInfo = (Structure.StructureBlockInfo)var6.next();
         BlockPos blockPos2 = transform(structurePlacementData, structureBlockInfo.pos).add(pos);
         Structure.StructureBlockInfo structureBlockInfo2 = new Structure.StructureBlockInfo(blockPos2, structureBlockInfo.state, structureBlockInfo.tag != null ? structureBlockInfo.tag.copy() : null);

         for(Iterator iterator = structurePlacementData.getProcessors().iterator(); structureBlockInfo2 != null && iterator.hasNext(); structureBlockInfo2 = ((StructureProcessor)iterator.next()).process(world, pos, blockPos, structureBlockInfo, structureBlockInfo2, structurePlacementData)) {
         }

         if (structureBlockInfo2 != null) {
            list2.add(structureBlockInfo2);
         }
      }

      return list2;
   }

   private void spawnEntities(ServerWorldAccess serverWorldAccess, BlockPos pos, BlockMirror blockMirror, BlockRotation blockRotation, BlockPos pivot, @Nullable BlockBox area, boolean bl) {
      Iterator var8 = this.entities.iterator();

      while(true) {
         Structure.StructureEntityInfo structureEntityInfo;
         BlockPos blockPos;
         do {
            if (!var8.hasNext()) {
               return;
            }

            structureEntityInfo = (Structure.StructureEntityInfo)var8.next();
            blockPos = transformAround(structureEntityInfo.blockPos, blockMirror, blockRotation, pivot).add(pos);
         } while(area != null && !area.contains(blockPos));

         CompoundTag compoundTag = structureEntityInfo.tag.copy();
         Vec3d vec3d = transformAround(structureEntityInfo.pos, blockMirror, blockRotation, pivot);
         Vec3d vec3d2 = vec3d.add((double)pos.getX(), (double)pos.getY(), (double)pos.getZ());
         ListTag listTag = new ListTag();
         listTag.add(DoubleTag.of(vec3d2.x));
         listTag.add(DoubleTag.of(vec3d2.y));
         listTag.add(DoubleTag.of(vec3d2.z));
         compoundTag.put("Pos", listTag);
         compoundTag.remove("UUID");
         getEntity(serverWorldAccess, compoundTag).ifPresent((entity) -> {
            float f = entity.applyMirror(blockMirror);
            f += entity.yaw - entity.applyRotation(blockRotation);
            entity.refreshPositionAndAngles(vec3d2.x, vec3d2.y, vec3d2.z, f, entity.pitch);
            if (bl && entity instanceof MobEntity) {
               ((MobEntity)entity).initialize(serverWorldAccess, serverWorldAccess.getLocalDifficulty(new BlockPos(vec3d2)), SpawnReason.STRUCTURE, (EntityData)null, compoundTag);
            }

            serverWorldAccess.spawnEntityAndPassengers(entity);
         });
      }
   }

   private static Optional<Entity> getEntity(ServerWorldAccess serverWorldAccess, CompoundTag compoundTag) {
      try {
         return EntityType.getEntityFromTag(compoundTag, serverWorldAccess.toServerWorld());
      } catch (Exception var3) {
         return Optional.empty();
      }
   }

   public BlockPos getRotatedSize(BlockRotation blockRotation) {
      switch(blockRotation) {
      case COUNTERCLOCKWISE_90:
      case CLOCKWISE_90:
         return new BlockPos(this.size.getZ(), this.size.getY(), this.size.getX());
      default:
         return this.size;
      }
   }

   public static BlockPos transformAround(BlockPos pos, BlockMirror blockMirror, BlockRotation blockRotation, BlockPos pivot) {
      int i = pos.getX();
      int j = pos.getY();
      int k = pos.getZ();
      boolean bl = true;
      switch(blockMirror) {
      case LEFT_RIGHT:
         k = -k;
         break;
      case FRONT_BACK:
         i = -i;
         break;
      default:
         bl = false;
      }

      int l = pivot.getX();
      int m = pivot.getZ();
      switch(blockRotation) {
      case COUNTERCLOCKWISE_90:
         return new BlockPos(l - m + k, j, l + m - i);
      case CLOCKWISE_90:
         return new BlockPos(l + m - k, j, m - l + i);
      case CLOCKWISE_180:
         return new BlockPos(l + l - i, j, m + m - k);
      default:
         return bl ? new BlockPos(i, j, k) : pos;
      }
   }

   public static Vec3d transformAround(Vec3d point, BlockMirror blockMirror, BlockRotation blockRotation, BlockPos pivot) {
      double d = point.x;
      double e = point.y;
      double f = point.z;
      boolean bl = true;
      switch(blockMirror) {
      case LEFT_RIGHT:
         f = 1.0D - f;
         break;
      case FRONT_BACK:
         d = 1.0D - d;
         break;
      default:
         bl = false;
      }

      int i = pivot.getX();
      int j = pivot.getZ();
      switch(blockRotation) {
      case COUNTERCLOCKWISE_90:
         return new Vec3d((double)(i - j) + f, e, (double)(i + j + 1) - d);
      case CLOCKWISE_90:
         return new Vec3d((double)(i + j + 1) - f, e, (double)(j - i) + d);
      case CLOCKWISE_180:
         return new Vec3d((double)(i + i + 1) - d, e, (double)(j + j + 1) - f);
      default:
         return bl ? new Vec3d(d, e, f) : point;
      }
   }

   public BlockPos offsetByTransformedSize(BlockPos blockPos, BlockMirror blockMirror, BlockRotation blockRotation) {
      return applyTransformedOffset(blockPos, blockMirror, blockRotation, this.getSize().getX(), this.getSize().getZ());
   }

   public static BlockPos applyTransformedOffset(BlockPos blockPos, BlockMirror blockMirror, BlockRotation blockRotation, int offsetX, int offsetZ) {
      --offsetX;
      --offsetZ;
      int i = blockMirror == BlockMirror.FRONT_BACK ? offsetX : 0;
      int j = blockMirror == BlockMirror.LEFT_RIGHT ? offsetZ : 0;
      BlockPos blockPos2 = blockPos;
      switch(blockRotation) {
      case COUNTERCLOCKWISE_90:
         blockPos2 = blockPos.add(j, 0, offsetX - i);
         break;
      case CLOCKWISE_90:
         blockPos2 = blockPos.add(offsetZ - j, 0, i);
         break;
      case CLOCKWISE_180:
         blockPos2 = blockPos.add(offsetX - i, 0, offsetZ - j);
         break;
      case NONE:
         blockPos2 = blockPos.add(i, 0, j);
      }

      return blockPos2;
   }

   public BlockBox calculateBoundingBox(StructurePlacementData structurePlacementData, BlockPos pos) {
      return this.method_27267(pos, structurePlacementData.getRotation(), structurePlacementData.getPosition(), structurePlacementData.getMirror());
   }

   public BlockBox method_27267(BlockPos blockPos, BlockRotation blockRotation, BlockPos blockPos2, BlockMirror blockMirror) {
      BlockPos blockPos3 = this.getRotatedSize(blockRotation);
      int i = blockPos2.getX();
      int j = blockPos2.getZ();
      int k = blockPos3.getX() - 1;
      int l = blockPos3.getY() - 1;
      int m = blockPos3.getZ() - 1;
      BlockBox blockBox = new BlockBox(0, 0, 0, 0, 0, 0);
      switch(blockRotation) {
      case COUNTERCLOCKWISE_90:
         blockBox = new BlockBox(i - j, 0, i + j - m, i - j + k, l, i + j);
         break;
      case CLOCKWISE_90:
         blockBox = new BlockBox(i + j - k, 0, j - i, i + j, l, j - i + m);
         break;
      case CLOCKWISE_180:
         blockBox = new BlockBox(i + i - k, 0, j + j - m, i + i, l, j + j);
         break;
      case NONE:
         blockBox = new BlockBox(0, 0, 0, k, l, m);
      }

      switch(blockMirror) {
      case LEFT_RIGHT:
         this.mirrorBoundingBox(blockRotation, m, k, blockBox, Direction.NORTH, Direction.SOUTH);
         break;
      case FRONT_BACK:
         this.mirrorBoundingBox(blockRotation, k, m, blockBox, Direction.WEST, Direction.EAST);
      case NONE:
      }

      blockBox.move(blockPos.getX(), blockPos.getY(), blockPos.getZ());
      return blockBox;
   }

   private void mirrorBoundingBox(BlockRotation rotation, int offsetX, int offsetZ, BlockBox boundingBox, Direction direction, Direction direction2) {
      BlockPos blockPos = BlockPos.ORIGIN;
      if (rotation != BlockRotation.CLOCKWISE_90 && rotation != BlockRotation.COUNTERCLOCKWISE_90) {
         if (rotation == BlockRotation.CLOCKWISE_180) {
            blockPos = blockPos.offset(direction2, offsetX);
         } else {
            blockPos = blockPos.offset(direction, offsetX);
         }
      } else {
         blockPos = blockPos.offset(rotation.rotate(direction), offsetZ);
      }

      boundingBox.move(blockPos.getX(), 0, blockPos.getZ());
   }

   public CompoundTag toTag(CompoundTag tag) {
      if (this.blockInfoLists.isEmpty()) {
         tag.put("blocks", new ListTag());
         tag.put("palette", new ListTag());
      } else {
         List<Structure.Palette> list = Lists.newArrayList();
         Structure.Palette palette = new Structure.Palette();
         list.add(palette);

         for(int i = 1; i < this.blockInfoLists.size(); ++i) {
            list.add(new Structure.Palette());
         }

         ListTag listTag = new ListTag();
         List<Structure.StructureBlockInfo> list2 = ((Structure.PalettedBlockInfoList)this.blockInfoLists.get(0)).getAll();

         for(int j = 0; j < list2.size(); ++j) {
            Structure.StructureBlockInfo structureBlockInfo = (Structure.StructureBlockInfo)list2.get(j);
            CompoundTag compoundTag = new CompoundTag();
            compoundTag.put("pos", this.createIntListTag(structureBlockInfo.pos.getX(), structureBlockInfo.pos.getY(), structureBlockInfo.pos.getZ()));
            int k = palette.getId(structureBlockInfo.state);
            compoundTag.putInt("state", k);
            if (structureBlockInfo.tag != null) {
               compoundTag.put("nbt", structureBlockInfo.tag);
            }

            listTag.add(compoundTag);

            for(int l = 1; l < this.blockInfoLists.size(); ++l) {
               Structure.Palette palette2 = (Structure.Palette)list.get(l);
               palette2.set(((Structure.StructureBlockInfo)((Structure.PalettedBlockInfoList)this.blockInfoLists.get(l)).getAll().get(j)).state, k);
            }
         }

         tag.put("blocks", listTag);
         ListTag listTag3;
         Iterator var18;
         if (list.size() == 1) {
            listTag3 = new ListTag();
            var18 = palette.iterator();

            while(var18.hasNext()) {
               BlockState blockState = (BlockState)var18.next();
               listTag3.add(NbtHelper.fromBlockState(blockState));
            }

            tag.put("palette", listTag3);
         } else {
            listTag3 = new ListTag();
            var18 = list.iterator();

            while(var18.hasNext()) {
               Structure.Palette palette3 = (Structure.Palette)var18.next();
               ListTag listTag4 = new ListTag();
               Iterator var22 = palette3.iterator();

               while(var22.hasNext()) {
                  BlockState blockState2 = (BlockState)var22.next();
                  listTag4.add(NbtHelper.fromBlockState(blockState2));
               }

               listTag3.add(listTag4);
            }

            tag.put("palettes", listTag3);
         }
      }

      ListTag listTag5 = new ListTag();

      CompoundTag compoundTag2;
      for(Iterator var13 = this.entities.iterator(); var13.hasNext(); listTag5.add(compoundTag2)) {
         Structure.StructureEntityInfo structureEntityInfo = (Structure.StructureEntityInfo)var13.next();
         compoundTag2 = new CompoundTag();
         compoundTag2.put("pos", this.createDoubleListTag(structureEntityInfo.pos.x, structureEntityInfo.pos.y, structureEntityInfo.pos.z));
         compoundTag2.put("blockPos", this.createIntListTag(structureEntityInfo.blockPos.getX(), structureEntityInfo.blockPos.getY(), structureEntityInfo.blockPos.getZ()));
         if (structureEntityInfo.tag != null) {
            compoundTag2.put("nbt", structureEntityInfo.tag);
         }
      }

      tag.put("entities", listTag5);
      tag.put("size", this.createIntListTag(this.size.getX(), this.size.getY(), this.size.getZ()));
      tag.putInt("DataVersion", SharedConstants.getGameVersion().getWorldVersion());
      return tag;
   }

   public void fromTag(CompoundTag tag) {
      this.blockInfoLists.clear();
      this.entities.clear();
      ListTag listTag = tag.getList("size", 3);
      this.size = new BlockPos(listTag.getInt(0), listTag.getInt(1), listTag.getInt(2));
      ListTag listTag2 = tag.getList("blocks", 10);
      ListTag listTag4;
      int j;
      if (tag.contains("palettes", 9)) {
         listTag4 = tag.getList("palettes", 9);

         for(j = 0; j < listTag4.size(); ++j) {
            this.loadPalettedBlockInfo(listTag4.getList(j), listTag2);
         }
      } else {
         this.loadPalettedBlockInfo(tag.getList("palette", 10), listTag2);
      }

      listTag4 = tag.getList("entities", 10);

      for(j = 0; j < listTag4.size(); ++j) {
         CompoundTag compoundTag = listTag4.getCompound(j);
         ListTag listTag5 = compoundTag.getList("pos", 6);
         Vec3d vec3d = new Vec3d(listTag5.getDouble(0), listTag5.getDouble(1), listTag5.getDouble(2));
         ListTag listTag6 = compoundTag.getList("blockPos", 3);
         BlockPos blockPos = new BlockPos(listTag6.getInt(0), listTag6.getInt(1), listTag6.getInt(2));
         if (compoundTag.contains("nbt")) {
            CompoundTag compoundTag2 = compoundTag.getCompound("nbt");
            this.entities.add(new Structure.StructureEntityInfo(vec3d, blockPos, compoundTag2));
         }
      }

   }

   private void loadPalettedBlockInfo(ListTag paletteTag, ListTag blocksTag) {
      Structure.Palette palette = new Structure.Palette();

      for(int i = 0; i < paletteTag.size(); ++i) {
         palette.set(NbtHelper.toBlockState(paletteTag.getCompound(i)), i);
      }

      List<Structure.StructureBlockInfo> list = Lists.newArrayList();
      List<Structure.StructureBlockInfo> list2 = Lists.newArrayList();
      List<Structure.StructureBlockInfo> list3 = Lists.newArrayList();

      for(int j = 0; j < blocksTag.size(); ++j) {
         CompoundTag compoundTag = blocksTag.getCompound(j);
         ListTag listTag = compoundTag.getList("pos", 3);
         BlockPos blockPos = new BlockPos(listTag.getInt(0), listTag.getInt(1), listTag.getInt(2));
         BlockState blockState = palette.getState(compoundTag.getInt("state"));
         CompoundTag compoundTag3;
         if (compoundTag.contains("nbt")) {
            compoundTag3 = compoundTag.getCompound("nbt");
         } else {
            compoundTag3 = null;
         }

         Structure.StructureBlockInfo structureBlockInfo = new Structure.StructureBlockInfo(blockPos, blockState, compoundTag3);
         method_28054(structureBlockInfo, list, list2, list3);
      }

      List<Structure.StructureBlockInfo> list4 = method_28055(list, list2, list3);
      this.blockInfoLists.add(new Structure.PalettedBlockInfoList(list4));
   }

   private ListTag createIntListTag(int... is) {
      ListTag listTag = new ListTag();
      int[] var3 = is;
      int var4 = is.length;

      for(int var5 = 0; var5 < var4; ++var5) {
         int i = var3[var5];
         listTag.add(IntTag.of(i));
      }

      return listTag;
   }

   private ListTag createDoubleListTag(double... ds) {
      ListTag listTag = new ListTag();
      double[] var3 = ds;
      int var4 = ds.length;

      for(int var5 = 0; var5 < var4; ++var5) {
         double d = var3[var5];
         listTag.add(DoubleTag.of(d));
      }

      return listTag;
   }

   public static final class PalettedBlockInfoList {
      private final List<Structure.StructureBlockInfo> infos;
      private final Map<Block, List<Structure.StructureBlockInfo>> blockToInfos;

      private PalettedBlockInfoList(List<Structure.StructureBlockInfo> infos) {
         this.blockToInfos = Maps.newHashMap();
         this.infos = infos;
      }

      public List<Structure.StructureBlockInfo> getAll() {
         return this.infos;
      }

      public List<Structure.StructureBlockInfo> getAllOf(Block block) {
         return (List)this.blockToInfos.computeIfAbsent(block, (blockx) -> {
            return (List)this.infos.stream().filter((structureBlockInfo) -> {
               return structureBlockInfo.state.isOf(blockx);
            }).collect(Collectors.toList());
         });
      }
   }

   public static class StructureEntityInfo {
      public final Vec3d pos;
      public final BlockPos blockPos;
      public final CompoundTag tag;

      public StructureEntityInfo(Vec3d pos, BlockPos blockPos, CompoundTag tag) {
         this.pos = pos;
         this.blockPos = blockPos;
         this.tag = tag;
      }
   }

   public static class StructureBlockInfo {
      public final BlockPos pos;
      public final BlockState state;
      public final CompoundTag tag;

      public StructureBlockInfo(BlockPos pos, BlockState state, @Nullable CompoundTag tag) {
         this.pos = pos;
         this.state = state;
         this.tag = tag;
      }

      public String toString() {
         return String.format("<StructureBlockInfo | %s | %s | %s>", this.pos, this.state, this.tag);
      }
   }

   static class Palette implements Iterable<BlockState> {
      public static final BlockState AIR;
      private final IdList<BlockState> ids;
      private int currentIndex;

      private Palette() {
         this.ids = new IdList(16);
      }

      public int getId(BlockState state) {
         int i = this.ids.getRawId(state);
         if (i == -1) {
            i = this.currentIndex++;
            this.ids.set(state, i);
         }

         return i;
      }

      @Nullable
      public BlockState getState(int id) {
         BlockState blockState = (BlockState)this.ids.get(id);
         return blockState == null ? AIR : blockState;
      }

      public Iterator<BlockState> iterator() {
         return this.ids.iterator();
      }

      public void set(BlockState state, int id) {
         this.ids.set(state, id);
      }

      static {
         AIR = Blocks.AIR.getDefaultState();
      }
   }
}
