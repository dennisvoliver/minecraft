package net.minecraft.block;

import java.util.Random;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;

public class WeepingVinesBlock extends AbstractPlantStemBlock {
   protected static final VoxelShape SHAPE = Block.createCuboidShape(4.0D, 9.0D, 4.0D, 12.0D, 16.0D, 12.0D);

   public WeepingVinesBlock(AbstractBlock.Settings settings) {
      super(settings, Direction.DOWN, SHAPE, false, 0.1D);
   }

   protected int method_26376(Random random) {
      return VineLogic.method_26381(random);
   }

   protected Block getPlant() {
      return Blocks.WEEPING_VINES_PLANT;
   }

   protected boolean chooseStemState(BlockState state) {
      return VineLogic.isValidForWeepingStem(state);
   }
}
