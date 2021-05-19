package net.minecraft.structure.processor;

import com.mojang.serialization.Codec;
import net.minecraft.structure.Structure;
import net.minecraft.structure.StructurePlacementData;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldView;
import org.jetbrains.annotations.Nullable;

public class NopStructureProcessor extends StructureProcessor {
   public static final Codec<NopStructureProcessor> CODEC = Codec.unit(() -> {
      return INSTANCE;
   });
   public static final NopStructureProcessor INSTANCE = new NopStructureProcessor();

   private NopStructureProcessor() {
   }

   @Nullable
   public Structure.StructureBlockInfo process(WorldView worldView, BlockPos pos, BlockPos blockPos, Structure.StructureBlockInfo structureBlockInfo, Structure.StructureBlockInfo structureBlockInfo2, StructurePlacementData structurePlacementData) {
      return structureBlockInfo2;
   }

   protected StructureProcessorType<?> getType() {
      return StructureProcessorType.NOP;
   }
}
