package net.minecraft.block;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;

public abstract class AbstractGlassBlock extends TransparentBlock {
   protected AbstractGlassBlock(AbstractBlock.Settings settings) {
      super(settings);
   }

   public VoxelShape getVisualShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
      return VoxelShapes.empty();
   }

   @Environment(EnvType.CLIENT)
   public float getAmbientOcclusionLightLevel(BlockState state, BlockView world, BlockPos pos) {
      return 1.0F;
   }

   public boolean isTranslucent(BlockState state, BlockView world, BlockPos pos) {
      return true;
   }
}
