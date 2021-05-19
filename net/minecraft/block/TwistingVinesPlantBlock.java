package net.minecraft.block;

import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;

public class TwistingVinesPlantBlock extends AbstractPlantBlock {
   public static final VoxelShape SHAPE = Block.createCuboidShape(4.0D, 0.0D, 4.0D, 12.0D, 16.0D, 12.0D);

   public TwistingVinesPlantBlock(AbstractBlock.Settings settings) {
      super(settings, Direction.UP, SHAPE, false);
   }

   protected AbstractPlantStemBlock getStem() {
      return (AbstractPlantStemBlock)Blocks.TWISTING_VINES;
   }
}
