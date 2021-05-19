package net.minecraft.block;

import java.util.Random;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;

public class TwistingVinesBlock extends AbstractPlantStemBlock {
   public static final VoxelShape SHAPE = Block.createCuboidShape(4.0D, 0.0D, 4.0D, 12.0D, 15.0D, 12.0D);

   public TwistingVinesBlock(AbstractBlock.Settings settings) {
      super(settings, Direction.UP, SHAPE, false, 0.1D);
   }

   protected int method_26376(Random random) {
      return VineLogic.method_26381(random);
   }

   protected Block getPlant() {
      return Blocks.TWISTING_VINES_PLANT;
   }

   protected boolean chooseStemState(BlockState state) {
      return VineLogic.isValidForWeepingStem(state);
   }
}
