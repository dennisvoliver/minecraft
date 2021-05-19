package net.minecraft.structure.processor;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import java.util.Collection;
import java.util.List;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.structure.Structure;
import net.minecraft.structure.StructurePlacementData;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldView;
import org.jetbrains.annotations.Nullable;

public class BlockIgnoreStructureProcessor extends StructureProcessor {
   public static final Codec<BlockIgnoreStructureProcessor> CODEC;
   public static final BlockIgnoreStructureProcessor IGNORE_STRUCTURE_BLOCKS;
   public static final BlockIgnoreStructureProcessor IGNORE_AIR;
   public static final BlockIgnoreStructureProcessor IGNORE_AIR_AND_STRUCTURE_BLOCKS;
   private final ImmutableList<Block> blocks;

   public BlockIgnoreStructureProcessor(List<Block> blocks) {
      this.blocks = ImmutableList.copyOf((Collection)blocks);
   }

   @Nullable
   public Structure.StructureBlockInfo process(WorldView worldView, BlockPos pos, BlockPos blockPos, Structure.StructureBlockInfo structureBlockInfo, Structure.StructureBlockInfo structureBlockInfo2, StructurePlacementData structurePlacementData) {
      return this.blocks.contains(structureBlockInfo2.state.getBlock()) ? null : structureBlockInfo2;
   }

   protected StructureProcessorType<?> getType() {
      return StructureProcessorType.BLOCK_IGNORE;
   }

   static {
      CODEC = BlockState.CODEC.xmap(AbstractBlock.AbstractBlockState::getBlock, Block::getDefaultState).listOf().fieldOf("blocks").xmap(BlockIgnoreStructureProcessor::new, (blockIgnoreStructureProcessor) -> {
         return blockIgnoreStructureProcessor.blocks;
      }).codec();
      IGNORE_STRUCTURE_BLOCKS = new BlockIgnoreStructureProcessor(ImmutableList.of(Blocks.STRUCTURE_BLOCK));
      IGNORE_AIR = new BlockIgnoreStructureProcessor(ImmutableList.of(Blocks.AIR));
      IGNORE_AIR_AND_STRUCTURE_BLOCKS = new BlockIgnoreStructureProcessor(ImmutableList.of(Blocks.AIR, Blocks.STRUCTURE_BLOCK));
   }
}
