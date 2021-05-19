package net.minecraft.structure.processor;

import com.mojang.serialization.Codec;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.structure.Structure;
import net.minecraft.structure.StructurePlacementData;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldView;
import org.jetbrains.annotations.Nullable;

public class LavaSubmergedBlockStructureProcessor extends StructureProcessor {
   public static final Codec<LavaSubmergedBlockStructureProcessor> CODEC = Codec.unit(() -> {
      return INSTANCE;
   });
   public static final LavaSubmergedBlockStructureProcessor INSTANCE = new LavaSubmergedBlockStructureProcessor();

   @Nullable
   public Structure.StructureBlockInfo process(WorldView worldView, BlockPos pos, BlockPos blockPos, Structure.StructureBlockInfo structureBlockInfo, Structure.StructureBlockInfo structureBlockInfo2, StructurePlacementData structurePlacementData) {
      BlockPos blockPos2 = structureBlockInfo2.pos;
      boolean bl = worldView.getBlockState(blockPos2).isOf(Blocks.LAVA);
      return bl && !Block.isShapeFullCube(structureBlockInfo2.state.getOutlineShape(worldView, blockPos2)) ? new Structure.StructureBlockInfo(blockPos2, Blocks.LAVA.getDefaultState(), structureBlockInfo2.tag) : structureBlockInfo2;
   }

   protected StructureProcessorType<?> getType() {
      return StructureProcessorType.LAVA_SUBMERGED_BLOCK;
   }
}
