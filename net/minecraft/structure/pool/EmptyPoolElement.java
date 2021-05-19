package net.minecraft.structure.pool;

import com.mojang.serialization.Codec;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import net.minecraft.structure.Structure;
import net.minecraft.structure.StructureManager;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.chunk.ChunkGenerator;

public class EmptyPoolElement extends StructurePoolElement {
   public static final Codec<EmptyPoolElement> CODEC = Codec.unit(() -> {
      return INSTANCE;
   });
   public static final EmptyPoolElement INSTANCE = new EmptyPoolElement();

   private EmptyPoolElement() {
      super(StructurePool.Projection.TERRAIN_MATCHING);
   }

   public List<Structure.StructureBlockInfo> getStructureBlockInfos(StructureManager structureManager, BlockPos pos, BlockRotation rotation, Random random) {
      return Collections.emptyList();
   }

   public BlockBox getBoundingBox(StructureManager structureManager, BlockPos pos, BlockRotation rotation) {
      return BlockBox.empty();
   }

   public boolean generate(StructureManager structureManager, StructureWorldAccess structureWorldAccess, StructureAccessor structureAccessor, ChunkGenerator chunkGenerator, BlockPos blockPos, BlockPos blockPos2, BlockRotation blockRotation, BlockBox blockBox, Random random, boolean keepJigsaws) {
      return true;
   }

   public StructurePoolElementType<?> getType() {
      return StructurePoolElementType.EMPTY_POOL_ELEMENT;
   }

   public String toString() {
      return "Empty";
   }
}
