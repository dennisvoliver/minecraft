package net.minecraft.block;

import net.minecraft.block.enums.WallMountLocation;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;
import org.jetbrains.annotations.Nullable;

public class WallMountedBlock extends HorizontalFacingBlock {
   public static final EnumProperty<WallMountLocation> FACE;

   protected WallMountedBlock(AbstractBlock.Settings settings) {
      super(settings);
   }

   public boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
      return canPlaceAt(world, pos, getDirection(state).getOpposite());
   }

   public static boolean canPlaceAt(WorldView worldView, BlockPos pos, Direction direction) {
      BlockPos blockPos = pos.offset(direction);
      return worldView.getBlockState(blockPos).isSideSolidFullSquare(worldView, blockPos, direction.getOpposite());
   }

   @Nullable
   public BlockState getPlacementState(ItemPlacementContext ctx) {
      Direction[] var2 = ctx.getPlacementDirections();
      int var3 = var2.length;

      for(int var4 = 0; var4 < var3; ++var4) {
         Direction direction = var2[var4];
         BlockState blockState2;
         if (direction.getAxis() == Direction.Axis.Y) {
            blockState2 = (BlockState)((BlockState)this.getDefaultState().with(FACE, direction == Direction.UP ? WallMountLocation.CEILING : WallMountLocation.FLOOR)).with(FACING, ctx.getPlayerFacing());
         } else {
            blockState2 = (BlockState)((BlockState)this.getDefaultState().with(FACE, WallMountLocation.WALL)).with(FACING, direction.getOpposite());
         }

         if (blockState2.canPlaceAt(ctx.getWorld(), ctx.getBlockPos())) {
            return blockState2;
         }
      }

      return null;
   }

   public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState newState, WorldAccess world, BlockPos pos, BlockPos posFrom) {
      return getDirection(state).getOpposite() == direction && !state.canPlaceAt(world, pos) ? Blocks.AIR.getDefaultState() : super.getStateForNeighborUpdate(state, direction, newState, world, pos, posFrom);
   }

   protected static Direction getDirection(BlockState state) {
      switch((WallMountLocation)state.get(FACE)) {
      case CEILING:
         return Direction.DOWN;
      case FLOOR:
         return Direction.UP;
      default:
         return (Direction)state.get(FACING);
      }
   }

   static {
      FACE = Properties.WALL_MOUNT_LOCATION;
   }
}
