package net.minecraft.block;

import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.Properties;

public abstract class FacingBlock extends Block {
   public static final DirectionProperty FACING;

   protected FacingBlock(AbstractBlock.Settings settings) {
      super(settings);
   }

   static {
      FACING = Properties.FACING;
   }
}
