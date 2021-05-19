package net.minecraft.structure.pool;

import com.google.common.collect.Lists;
import com.google.common.collect.Queues;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import net.minecraft.block.JigsawBlock;
import net.minecraft.structure.JigsawJunction;
import net.minecraft.structure.PoolStructurePiece;
import net.minecraft.structure.Structure;
import net.minecraft.structure.StructureManager;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Identifier;
import net.minecraft.util.function.BooleanBiFunction;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.registry.DynamicRegistryManager;
import net.minecraft.util.registry.MutableRegistry;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.Heightmap;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.feature.StructureFeature;
import net.minecraft.world.gen.feature.StructurePoolFeatureConfig;
import org.apache.commons.lang3.mutable.MutableObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class StructurePoolBasedGenerator {
   private static final Logger LOGGER = LogManager.getLogger();

   public static void method_30419(DynamicRegistryManager dynamicRegistryManager, StructurePoolFeatureConfig structurePoolFeatureConfig, StructurePoolBasedGenerator.PieceFactory pieceFactory, ChunkGenerator chunkGenerator, StructureManager structureManager, BlockPos blockPos, List<? super PoolStructurePiece> list, Random random, boolean bl, boolean bl2) {
      StructureFeature.method_28664();
      MutableRegistry<StructurePool> mutableRegistry = dynamicRegistryManager.get(Registry.TEMPLATE_POOL_WORLDGEN);
      BlockRotation blockRotation = BlockRotation.random(random);
      StructurePool structurePool = (StructurePool)structurePoolFeatureConfig.getStartPool().get();
      StructurePoolElement structurePoolElement = structurePool.getRandomElement(random);
      PoolStructurePiece poolStructurePiece = pieceFactory.create(structureManager, structurePoolElement, blockPos, structurePoolElement.getGroundLevelDelta(), blockRotation, structurePoolElement.getBoundingBox(structureManager, blockPos, blockRotation));
      BlockBox blockBox = poolStructurePiece.getBoundingBox();
      int i = (blockBox.maxX + blockBox.minX) / 2;
      int j = (blockBox.maxZ + blockBox.minZ) / 2;
      int l;
      if (bl2) {
         l = blockPos.getY() + chunkGenerator.getHeightOnGround(i, j, Heightmap.Type.WORLD_SURFACE_WG);
      } else {
         l = blockPos.getY();
      }

      int m = blockBox.minY + poolStructurePiece.getGroundLevelDelta();
      poolStructurePiece.translate(0, l - m, 0);
      list.add(poolStructurePiece);
      if (structurePoolFeatureConfig.getSize() > 0) {
         int n = true;
         Box box = new Box((double)(i - 80), (double)(l - 80), (double)(j - 80), (double)(i + 80 + 1), (double)(l + 80 + 1), (double)(j + 80 + 1));
         StructurePoolBasedGenerator.StructurePoolGenerator structurePoolGenerator = new StructurePoolBasedGenerator.StructurePoolGenerator(mutableRegistry, structurePoolFeatureConfig.getSize(), pieceFactory, chunkGenerator, structureManager, list, random);
         structurePoolGenerator.structurePieces.addLast(new StructurePoolBasedGenerator.ShapedPoolStructurePiece(poolStructurePiece, new MutableObject(VoxelShapes.combineAndSimplify(VoxelShapes.cuboid(box), VoxelShapes.cuboid(Box.from(blockBox)), BooleanBiFunction.ONLY_FIRST)), l + 80, 0));

         while(!structurePoolGenerator.structurePieces.isEmpty()) {
            StructurePoolBasedGenerator.ShapedPoolStructurePiece shapedPoolStructurePiece = (StructurePoolBasedGenerator.ShapedPoolStructurePiece)structurePoolGenerator.structurePieces.removeFirst();
            structurePoolGenerator.generatePiece(shapedPoolStructurePiece.piece, shapedPoolStructurePiece.pieceShape, shapedPoolStructurePiece.minY, shapedPoolStructurePiece.currentSize, bl);
         }

      }
   }

   public static void method_27230(DynamicRegistryManager dynamicRegistryManager, PoolStructurePiece poolStructurePiece, int i, StructurePoolBasedGenerator.PieceFactory pieceFactory, ChunkGenerator chunkGenerator, StructureManager structureManager, List<? super PoolStructurePiece> list, Random random) {
      MutableRegistry<StructurePool> mutableRegistry = dynamicRegistryManager.get(Registry.TEMPLATE_POOL_WORLDGEN);
      StructurePoolBasedGenerator.StructurePoolGenerator structurePoolGenerator = new StructurePoolBasedGenerator.StructurePoolGenerator(mutableRegistry, i, pieceFactory, chunkGenerator, structureManager, list, random);
      structurePoolGenerator.structurePieces.addLast(new StructurePoolBasedGenerator.ShapedPoolStructurePiece(poolStructurePiece, new MutableObject(VoxelShapes.UNBOUNDED), 0, 0));

      while(!structurePoolGenerator.structurePieces.isEmpty()) {
         StructurePoolBasedGenerator.ShapedPoolStructurePiece shapedPoolStructurePiece = (StructurePoolBasedGenerator.ShapedPoolStructurePiece)structurePoolGenerator.structurePieces.removeFirst();
         structurePoolGenerator.generatePiece(shapedPoolStructurePiece.piece, shapedPoolStructurePiece.pieceShape, shapedPoolStructurePiece.minY, shapedPoolStructurePiece.currentSize, false);
      }

   }

   public interface PieceFactory {
      PoolStructurePiece create(StructureManager structureManager, StructurePoolElement poolElement, BlockPos pos, int i, BlockRotation rotation, BlockBox elementBounds);
   }

   static final class StructurePoolGenerator {
      private final Registry<StructurePool> field_25852;
      private final int maxSize;
      private final StructurePoolBasedGenerator.PieceFactory pieceFactory;
      private final ChunkGenerator chunkGenerator;
      private final StructureManager structureManager;
      private final List<? super PoolStructurePiece> children;
      private final Random random;
      private final Deque<StructurePoolBasedGenerator.ShapedPoolStructurePiece> structurePieces;

      private StructurePoolGenerator(Registry<StructurePool> registry, int i, StructurePoolBasedGenerator.PieceFactory pieceFactory, ChunkGenerator chunkGenerator, StructureManager structureManager, List<? super PoolStructurePiece> list, Random random) {
         this.structurePieces = Queues.newArrayDeque();
         this.field_25852 = registry;
         this.maxSize = i;
         this.pieceFactory = pieceFactory;
         this.chunkGenerator = chunkGenerator;
         this.structureManager = structureManager;
         this.children = list;
         this.random = random;
      }

      private void generatePiece(PoolStructurePiece piece, MutableObject<VoxelShape> mutableObject, int minY, int currentSize, boolean bl) {
         StructurePoolElement structurePoolElement = piece.getPoolElement();
         BlockPos blockPos = piece.getPos();
         BlockRotation blockRotation = piece.getRotation();
         StructurePool.Projection projection = structurePoolElement.getProjection();
         boolean bl2 = projection == StructurePool.Projection.RIGID;
         MutableObject<VoxelShape> mutableObject2 = new MutableObject();
         BlockBox blockBox = piece.getBoundingBox();
         int i = blockBox.minY;
         Iterator var14 = structurePoolElement.getStructureBlockInfos(this.structureManager, blockPos, blockRotation, this.random).iterator();

         while(true) {
            while(true) {
               while(true) {
                  label93:
                  while(var14.hasNext()) {
                     Structure.StructureBlockInfo structureBlockInfo = (Structure.StructureBlockInfo)var14.next();
                     Direction direction = JigsawBlock.getFacing(structureBlockInfo.state);
                     BlockPos blockPos2 = structureBlockInfo.pos;
                     BlockPos blockPos3 = blockPos2.offset(direction);
                     int j = blockPos2.getY() - i;
                     int k = -1;
                     Identifier identifier = new Identifier(structureBlockInfo.tag.getString("pool"));
                     Optional<StructurePool> optional = this.field_25852.getOrEmpty(identifier);
                     if (optional.isPresent() && (((StructurePool)optional.get()).getElementCount() != 0 || Objects.equals(identifier, StructurePools.EMPTY.getValue()))) {
                        Identifier identifier2 = ((StructurePool)optional.get()).getTerminatorsId();
                        Optional<StructurePool> optional2 = this.field_25852.getOrEmpty(identifier2);
                        if (optional2.isPresent() && (((StructurePool)optional2.get()).getElementCount() != 0 || Objects.equals(identifier2, StructurePools.EMPTY.getValue()))) {
                           boolean bl3 = blockBox.contains(blockPos3);
                           MutableObject mutableObject4;
                           int m;
                           if (bl3) {
                              mutableObject4 = mutableObject2;
                              m = i;
                              if (mutableObject2.getValue() == null) {
                                 mutableObject2.setValue(VoxelShapes.cuboid(Box.from(blockBox)));
                              }
                           } else {
                              mutableObject4 = mutableObject;
                              m = minY;
                           }

                           List<StructurePoolElement> list = Lists.newArrayList();
                           if (currentSize != this.maxSize) {
                              list.addAll(((StructurePool)optional.get()).getElementIndicesInRandomOrder(this.random));
                           }

                           list.addAll(((StructurePool)optional2.get()).getElementIndicesInRandomOrder(this.random));
                           Iterator var29 = list.iterator();

                           while(var29.hasNext()) {
                              StructurePoolElement structurePoolElement2 = (StructurePoolElement)var29.next();
                              if (structurePoolElement2 == EmptyPoolElement.INSTANCE) {
                                 break;
                              }

                              Iterator var31 = BlockRotation.randomRotationOrder(this.random).iterator();

                              label133:
                              while(var31.hasNext()) {
                                 BlockRotation blockRotation2 = (BlockRotation)var31.next();
                                 List<Structure.StructureBlockInfo> list2 = structurePoolElement2.getStructureBlockInfos(this.structureManager, BlockPos.ORIGIN, blockRotation2, this.random);
                                 BlockBox blockBox2 = structurePoolElement2.getBoundingBox(this.structureManager, BlockPos.ORIGIN, blockRotation2);
                                 int o;
                                 if (bl && blockBox2.getBlockCountY() <= 16) {
                                    o = list2.stream().mapToInt((structureBlockInfox) -> {
                                       if (!blockBox2.contains(structureBlockInfox.pos.offset(JigsawBlock.getFacing(structureBlockInfox.state)))) {
                                          return 0;
                                       } else {
                                          Identifier identifier = new Identifier(structureBlockInfox.tag.getString("pool"));
                                          Optional<StructurePool> optional = this.field_25852.getOrEmpty(identifier);
                                          Optional<StructurePool> optional2 = optional.flatMap((structurePool) -> {
                                             return this.field_25852.getOrEmpty(structurePool.getTerminatorsId());
                                          });
                                          int i = (Integer)optional.map((structurePool) -> {
                                             return structurePool.getHighestY(this.structureManager);
                                          }).orElse(0);
                                          int j = (Integer)optional2.map((structurePool) -> {
                                             return structurePool.getHighestY(this.structureManager);
                                          }).orElse(0);
                                          return Math.max(i, j);
                                       }
                                    }).max().orElse(0);
                                 } else {
                                    o = 0;
                                 }

                                 Iterator var36 = list2.iterator();

                                 StructurePool.Projection projection2;
                                 boolean bl4;
                                 int q;
                                 int r;
                                 int t;
                                 BlockBox blockBox4;
                                 BlockPos blockPos6;
                                 int w;
                                 do {
                                    Structure.StructureBlockInfo structureBlockInfo2;
                                    do {
                                       if (!var36.hasNext()) {
                                          continue label133;
                                       }

                                       structureBlockInfo2 = (Structure.StructureBlockInfo)var36.next();
                                    } while(!JigsawBlock.attachmentMatches(structureBlockInfo, structureBlockInfo2));

                                    BlockPos blockPos4 = structureBlockInfo2.pos;
                                    BlockPos blockPos5 = new BlockPos(blockPos3.getX() - blockPos4.getX(), blockPos3.getY() - blockPos4.getY(), blockPos3.getZ() - blockPos4.getZ());
                                    BlockBox blockBox3 = structurePoolElement2.getBoundingBox(this.structureManager, blockPos5, blockRotation2);
                                    int p = blockBox3.minY;
                                    projection2 = structurePoolElement2.getProjection();
                                    bl4 = projection2 == StructurePool.Projection.RIGID;
                                    q = blockPos4.getY();
                                    r = j - q + JigsawBlock.getFacing(structureBlockInfo.state).getOffsetY();
                                    if (bl2 && bl4) {
                                       t = i + r;
                                    } else {
                                       if (k == -1) {
                                          k = this.chunkGenerator.getHeightOnGround(blockPos2.getX(), blockPos2.getZ(), Heightmap.Type.WORLD_SURFACE_WG);
                                       }

                                       t = k - q;
                                    }

                                    int u = t - p;
                                    blockBox4 = blockBox3.offset(0, u, 0);
                                    blockPos6 = blockPos5.add(0, u, 0);
                                    if (o > 0) {
                                       w = Math.max(o + 1, blockBox4.maxY - blockBox4.minY);
                                       blockBox4.maxY = blockBox4.minY + w;
                                    }
                                 } while(VoxelShapes.matchesAnywhere((VoxelShape)mutableObject4.getValue(), VoxelShapes.cuboid(Box.from(blockBox4).contract(0.25D)), BooleanBiFunction.ONLY_SECOND));

                                 mutableObject4.setValue(VoxelShapes.combine((VoxelShape)mutableObject4.getValue(), VoxelShapes.cuboid(Box.from(blockBox4)), BooleanBiFunction.ONLY_FIRST));
                                 w = piece.getGroundLevelDelta();
                                 int y;
                                 if (bl4) {
                                    y = w - r;
                                 } else {
                                    y = structurePoolElement2.getGroundLevelDelta();
                                 }

                                 PoolStructurePiece poolStructurePiece = this.pieceFactory.create(this.structureManager, structurePoolElement2, blockPos6, y, blockRotation2, blockBox4);
                                 int ab;
                                 if (bl2) {
                                    ab = i + j;
                                 } else if (bl4) {
                                    ab = t + q;
                                 } else {
                                    if (k == -1) {
                                       k = this.chunkGenerator.getHeightOnGround(blockPos2.getX(), blockPos2.getZ(), Heightmap.Type.WORLD_SURFACE_WG);
                                    }

                                    ab = k + r / 2;
                                 }

                                 piece.addJunction(new JigsawJunction(blockPos3.getX(), ab - j + w, blockPos3.getZ(), r, projection2));
                                 poolStructurePiece.addJunction(new JigsawJunction(blockPos2.getX(), ab - q + y, blockPos2.getZ(), -r, projection));
                                 this.children.add(poolStructurePiece);
                                 if (currentSize + 1 <= this.maxSize) {
                                    this.structurePieces.addLast(new StructurePoolBasedGenerator.ShapedPoolStructurePiece(poolStructurePiece, mutableObject4, m, currentSize + 1));
                                 }
                                 continue label93;
                              }
                           }
                        } else {
                           StructurePoolBasedGenerator.LOGGER.warn((String)"Empty or none existent fallback pool: {}", (Object)identifier2);
                        }
                     } else {
                        StructurePoolBasedGenerator.LOGGER.warn((String)"Empty or none existent pool: {}", (Object)identifier);
                     }
                  }

                  return;
               }
            }
         }
      }
   }

   static final class ShapedPoolStructurePiece {
      private final PoolStructurePiece piece;
      private final MutableObject<VoxelShape> pieceShape;
      private final int minY;
      private final int currentSize;

      private ShapedPoolStructurePiece(PoolStructurePiece piece, MutableObject<VoxelShape> mutableObject, int minY, int currentSize) {
         this.piece = piece;
         this.pieceShape = mutableObject;
         this.minY = minY;
         this.currentSize = currentSize;
      }
   }
}
