package net.minecraft.block;

import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;

public class WeepingVinesPlantBlock extends AbstractPlantBlock {
   public static final VoxelShape SHAPE = Block.createCuboidShape(1.0D, 0.0D, 1.0D, 15.0D, 16.0D, 15.0D);

   public WeepingVinesPlantBlock(AbstractBlock.Settings settings) {
      super(settings, Direction.DOWN, SHAPE, false);
   }

   protected AbstractPlantStemBlock getStem() {
      return (AbstractPlantStemBlock)Blocks.WEEPING_VINES;
   }
}
