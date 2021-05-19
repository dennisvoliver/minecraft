package net.minecraft.world.gen.feature;

import com.mojang.serialization.Codec;
import java.util.Iterator;
import java.util.Random;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.Material;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.LootableContainerBlockEntity;
import net.minecraft.block.entity.MobSpawnerBlockEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.loot.LootTables;
import net.minecraft.structure.StructurePiece;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class DungeonFeature extends Feature<DefaultFeatureConfig> {
   private static final Logger LOGGER = LogManager.getLogger();
   private static final EntityType<?>[] MOB_SPAWNER_ENTITIES;
   private static final BlockState AIR;

   public DungeonFeature(Codec<DefaultFeatureConfig> codec) {
      super(codec);
   }

   public boolean generate(StructureWorldAccess structureWorldAccess, ChunkGenerator chunkGenerator, Random random, BlockPos blockPos, DefaultFeatureConfig defaultFeatureConfig) {
      int i = true;
      int j = random.nextInt(2) + 2;
      int k = -j - 1;
      int l = j + 1;
      int m = true;
      int n = true;
      int o = random.nextInt(2) + 2;
      int p = -o - 1;
      int q = o + 1;
      int r = 0;

      int v;
      int w;
      int x;
      BlockPos blockPos3;
      for(v = k; v <= l; ++v) {
         for(w = -1; w <= 4; ++w) {
            for(x = p; x <= q; ++x) {
               blockPos3 = blockPos.add(v, w, x);
               Material material = structureWorldAccess.getBlockState(blockPos3).getMaterial();
               boolean bl = material.isSolid();
               if (w == -1 && !bl) {
                  return false;
               }

               if (w == 4 && !bl) {
                  return false;
               }

               if ((v == k || v == l || x == p || x == q) && w == 0 && structureWorldAccess.isAir(blockPos3) && structureWorldAccess.isAir(blockPos3.up())) {
                  ++r;
               }
            }
         }
      }

      if (r >= 1 && r <= 5) {
         for(v = k; v <= l; ++v) {
            for(w = 3; w >= -1; --w) {
               for(x = p; x <= q; ++x) {
                  blockPos3 = blockPos.add(v, w, x);
                  BlockState blockState = structureWorldAccess.getBlockState(blockPos3);
                  if (v != k && w != -1 && x != p && v != l && w != 4 && x != q) {
                     if (!blockState.isOf(Blocks.CHEST) && !blockState.isOf(Blocks.SPAWNER)) {
                        structureWorldAccess.setBlockState(blockPos3, AIR, 2);
                     }
                  } else if (blockPos3.getY() >= 0 && !structureWorldAccess.getBlockState(blockPos3.down()).getMaterial().isSolid()) {
                     structureWorldAccess.setBlockState(blockPos3, AIR, 2);
                  } else if (blockState.getMaterial().isSolid() && !blockState.isOf(Blocks.CHEST)) {
                     if (w == -1 && random.nextInt(4) != 0) {
                        structureWorldAccess.setBlockState(blockPos3, Blocks.MOSSY_COBBLESTONE.getDefaultState(), 2);
                     } else {
                        structureWorldAccess.setBlockState(blockPos3, Blocks.COBBLESTONE.getDefaultState(), 2);
                     }
                  }
               }
            }
         }

         for(v = 0; v < 2; ++v) {
            for(w = 0; w < 3; ++w) {
               x = blockPos.getX() + random.nextInt(j * 2 + 1) - j;
               int ab = blockPos.getY();
               int ac = blockPos.getZ() + random.nextInt(o * 2 + 1) - o;
               BlockPos blockPos4 = new BlockPos(x, ab, ac);
               if (structureWorldAccess.isAir(blockPos4)) {
                  int ad = 0;
                  Iterator var23 = Direction.Type.HORIZONTAL.iterator();

                  while(var23.hasNext()) {
                     Direction direction = (Direction)var23.next();
                     if (structureWorldAccess.getBlockState(blockPos4.offset(direction)).getMaterial().isSolid()) {
                        ++ad;
                     }
                  }

                  if (ad == 1) {
                     structureWorldAccess.setBlockState(blockPos4, StructurePiece.orientateChest(structureWorldAccess, blockPos4, Blocks.CHEST.getDefaultState()), 2);
                     LootableContainerBlockEntity.setLootTable(structureWorldAccess, random, blockPos4, LootTables.SIMPLE_DUNGEON_CHEST);
                     break;
                  }
               }
            }
         }

         structureWorldAccess.setBlockState(blockPos, Blocks.SPAWNER.getDefaultState(), 2);
         BlockEntity blockEntity = structureWorldAccess.getBlockEntity(blockPos);
         if (blockEntity instanceof MobSpawnerBlockEntity) {
            ((MobSpawnerBlockEntity)blockEntity).getLogic().setEntityId(this.getMobSpawnerEntity(random));
         } else {
            LOGGER.error((String)"Failed to fetch mob spawner entity at ({}, {}, {})", (Object)blockPos.getX(), blockPos.getY(), blockPos.getZ());
         }

         return true;
      } else {
         return false;
      }
   }

   private EntityType<?> getMobSpawnerEntity(Random random) {
      return (EntityType)Util.getRandom((Object[])MOB_SPAWNER_ENTITIES, random);
   }

   static {
      MOB_SPAWNER_ENTITIES = new EntityType[]{EntityType.SKELETON, EntityType.ZOMBIE, EntityType.ZOMBIE, EntityType.SPIDER};
      AIR = Blocks.CAVE_AIR.getDefaultState();
   }
}
