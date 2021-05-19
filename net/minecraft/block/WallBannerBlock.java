package net.minecraft.block;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import java.util.Map;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.DyeColor;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;

public class WallBannerBlock extends AbstractBannerBlock {
   public static final DirectionProperty FACING;
   private static final Map<Direction, VoxelShape> FACING_TO_SHAPE;

   public WallBannerBlock(DyeColor dyeColor, AbstractBlock.Settings settings) {
      super(dyeColor, settings);
      this.setDefaultState((BlockState)((BlockState)this.stateManager.getDefaultState()).with(FACING, Direction.NORTH));
   }

   public String getTranslationKey() {
      return this.asItem().getTranslationKey();
   }

   public boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
      return world.getBlockState(pos.offset(((Direction)state.get(FACING)).getOpposite())).getMaterial().isSolid();
   }

   public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState newState, WorldAccess world, BlockPos pos, BlockPos posFrom) {
      return direction == ((Direction)state.get(FACING)).getOpposite() && !state.canPlaceAt(world, pos) ? Blocks.AIR.getDefaultState() : super.getStateForNeighborUpdate(state, direction, newState, world, pos, posFrom);
   }

   public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
      return (VoxelShape)FACING_TO_SHAPE.get(state.get(FACING));
   }

   public BlockState getPlacementState(ItemPlacementContext ctx) {
      BlockState blockState = this.getDefaultState();
      WorldView worldView = ctx.getWorld();
      BlockPos blockPos = ctx.getBlockPos();
      Direction[] directions = ctx.getPlacementDirections();
      Direction[] var6 = directions;
      int var7 = directions.length;

      for(int var8 = 0; var8 < var7; ++var8) {
         Direction direction = var6[var8];
         if (direction.getAxis().isHorizontal()) {
            Direction direction2 = direction.getOpposite();
            blockState = (BlockState)blockState.with(FACING, direction2);
            if (blockState.canPlaceAt(worldView, blockPos)) {
               return blockState;
            }
         }
      }

      return null;
   }

   public BlockState rotate(BlockState state, BlockRotation rotation) {
      return (BlockState)state.with(FACING, rotation.rotate((Direction)state.get(FACING)));
   }

   public BlockState mirror(BlockState state, BlockMirror mirror) {
      return state.rotate(mirror.getRotation((Direction)state.get(FACING)));
   }

   protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
      builder.add(FACING);
   }

   static {
      FACING = HorizontalFacingBlock.FACING;
      FACING_TO_SHAPE = Maps.newEnumMap((Map)ImmutableMap.of(Direction.NORTH, Block.createCuboidShape(0.0D, 0.0D, 14.0D, 16.0D, 12.5D, 16.0D), Direction.SOUTH, Block.createCuboidShape(0.0D, 0.0D, 0.0D, 16.0D, 12.5D, 2.0D), Direction.WEST, Block.createCuboidShape(14.0D, 0.0D, 0.0D, 16.0D, 12.5D, 16.0D), Direction.EAST, Block.createCuboidShape(0.0D, 0.0D, 0.0D, 2.0D, 12.5D, 16.0D)));
   }
}
