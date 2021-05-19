package net.minecraft.block;

import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.Direction;

public abstract class HorizontalFacingBlock extends Block {
   public static final DirectionProperty FACING;

   protected HorizontalFacingBlock(AbstractBlock.Settings settings) {
      super(settings);
   }

   public BlockState rotate(BlockState state, BlockRotation rotation) {
      return (BlockState)state.with(FACING, rotation.rotate((Direction)state.get(FACING)));
   }

   public BlockState mirror(BlockState state, BlockMirror mirror) {
      return state.rotate(mirror.getRotation((Direction)state.get(FACING)));
   }

   static {
      FACING = Properties.HORIZONTAL_FACING;
   }
}
