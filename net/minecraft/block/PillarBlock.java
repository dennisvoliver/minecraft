package net.minecraft.block;

import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.Direction;

public class PillarBlock extends Block {
   public static final EnumProperty<Direction.Axis> AXIS;

   public PillarBlock(AbstractBlock.Settings settings) {
      super(settings);
      this.setDefaultState((BlockState)this.getDefaultState().with(AXIS, Direction.Axis.Y));
   }

   public BlockState rotate(BlockState state, BlockRotation rotation) {
      switch(rotation) {
      case COUNTERCLOCKWISE_90:
      case CLOCKWISE_90:
         switch((Direction.Axis)state.get(AXIS)) {
         case X:
            return (BlockState)state.with(AXIS, Direction.Axis.Z);
         case Z:
            return (BlockState)state.with(AXIS, Direction.Axis.X);
         default:
            return state;
         }
      default:
         return state;
      }
   }

   protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
      builder.add(AXIS);
   }

   public BlockState getPlacementState(ItemPlacementContext ctx) {
      return (BlockState)this.getDefaultState().with(AXIS, ctx.getSide().getAxis());
   }

   static {
      AXIS = Properties.AXIS;
   }
}
