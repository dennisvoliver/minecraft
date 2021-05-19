package net.minecraft.structure.processor;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.function.BiFunction;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.structure.Structure;
import net.minecraft.structure.StructurePlacementData;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Heightmap;
import net.minecraft.world.WorldView;
import org.jetbrains.annotations.Nullable;

public class GravityStructureProcessor extends StructureProcessor {
   public static final Codec<GravityStructureProcessor> CODEC = RecordCodecBuilder.create((instance) -> {
      return instance.group(Heightmap.Type.CODEC.fieldOf("heightmap").orElse(Heightmap.Type.WORLD_SURFACE_WG).forGetter((gravityStructureProcessor) -> {
         return gravityStructureProcessor.heightmap;
      }), Codec.INT.fieldOf("offset").orElse(0).forGetter((gravityStructureProcessor) -> {
         return gravityStructureProcessor.offset;
      })).apply(instance, (BiFunction)(GravityStructureProcessor::new));
   });
   private final Heightmap.Type heightmap;
   private final int offset;

   public GravityStructureProcessor(Heightmap.Type heightmap, int offset) {
      this.heightmap = heightmap;
      this.offset = offset;
   }

   @Nullable
   public Structure.StructureBlockInfo process(WorldView worldView, BlockPos pos, BlockPos blockPos, Structure.StructureBlockInfo structureBlockInfo, Structure.StructureBlockInfo structureBlockInfo2, StructurePlacementData structurePlacementData) {
      Heightmap.Type type4;
      if (worldView instanceof ServerWorld) {
         if (this.heightmap == Heightmap.Type.WORLD_SURFACE_WG) {
            type4 = Heightmap.Type.WORLD_SURFACE;
         } else if (this.heightmap == Heightmap.Type.OCEAN_FLOOR_WG) {
            type4 = Heightmap.Type.OCEAN_FLOOR;
         } else {
            type4 = this.heightmap;
         }
      } else {
         type4 = this.heightmap;
      }

      int i = worldView.getTopY(type4, structureBlockInfo2.pos.getX(), structureBlockInfo2.pos.getZ()) + this.offset;
      int j = structureBlockInfo.pos.getY();
      return new Structure.StructureBlockInfo(new BlockPos(structureBlockInfo2.pos.getX(), i + j, structureBlockInfo2.pos.getZ()), structureBlockInfo2.state, structureBlockInfo2.tag);
   }

   protected StructureProcessorType<?> getType() {
      return StructureProcessorType.GRAVITY;
   }
}
