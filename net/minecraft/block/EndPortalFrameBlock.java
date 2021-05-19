package net.minecraft.block;

import com.google.common.base.Predicates;
import net.minecraft.block.pattern.BlockPattern;
import net.minecraft.block.pattern.BlockPatternBuilder;
import net.minecraft.block.pattern.CachedBlockPosition;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.predicate.block.BlockStatePredicate;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

public class EndPortalFrameBlock extends Block {
   public static final DirectionProperty FACING;
   public static final BooleanProperty EYE;
   protected static final VoxelShape FRAME_SHAPE;
   protected static final VoxelShape EYE_SHAPE;
   protected static final VoxelShape FRAME_WITH_EYE_SHAPE;
   private static BlockPattern COMPLETED_FRAME;

   public EndPortalFrameBlock(AbstractBlock.Settings settings) {
      super(settings);
      this.setDefaultState((BlockState)((BlockState)((BlockState)this.stateManager.getDefaultState()).with(FACING, Direction.NORTH)).with(EYE, false));
   }

   public boolean hasSidedTransparency(BlockState state) {
      return true;
   }

   public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
      return (Boolean)state.get(EYE) ? FRAME_WITH_EYE_SHAPE : FRAME_SHAPE;
   }

   public BlockState getPlacementState(ItemPlacementContext ctx) {
      return (BlockState)((BlockState)this.getDefaultState().with(FACING, ctx.getPlayerFacing().getOpposite())).with(EYE, false);
   }

   public boolean hasComparatorOutput(BlockState state) {
      return true;
   }

   public int getComparatorOutput(BlockState state, World world, BlockPos pos) {
      return (Boolean)state.get(EYE) ? 15 : 0;
   }

   public BlockState rotate(BlockState state, BlockRotation rotation) {
      return (BlockState)state.with(FACING, rotation.rotate((Direction)state.get(FACING)));
   }

   public BlockState mirror(BlockState state, BlockMirror mirror) {
      return state.rotate(mirror.getRotation((Direction)state.get(FACING)));
   }

   protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
      builder.add(FACING, EYE);
   }

   public static BlockPattern getCompletedFramePattern() {
      if (COMPLETED_FRAME == null) {
         COMPLETED_FRAME = BlockPatternBuilder.start().aisle("?vvv?", ">???<", ">???<", ">???<", "?^^^?").where('?', CachedBlockPosition.matchesBlockState(BlockStatePredicate.ANY)).where('^', CachedBlockPosition.matchesBlockState(BlockStatePredicate.forBlock(Blocks.END_PORTAL_FRAME).with(EYE, Predicates.equalTo(true)).with(FACING, Predicates.equalTo(Direction.SOUTH)))).where('>', CachedBlockPosition.matchesBlockState(BlockStatePredicate.forBlock(Blocks.END_PORTAL_FRAME).with(EYE, Predicates.equalTo(true)).with(FACING, Predicates.equalTo(Direction.WEST)))).where('v', CachedBlockPosition.matchesBlockState(BlockStatePredicate.forBlock(Blocks.END_PORTAL_FRAME).with(EYE, Predicates.equalTo(true)).with(FACING, Predicates.equalTo(Direction.NORTH)))).where('<', CachedBlockPosition.matchesBlockState(BlockStatePredicate.forBlock(Blocks.END_PORTAL_FRAME).with(EYE, Predicates.equalTo(true)).with(FACING, Predicates.equalTo(Direction.EAST)))).build();
      }

      return COMPLETED_FRAME;
   }

   public boolean canPathfindThrough(BlockState state, BlockView world, BlockPos pos, NavigationType type) {
      return false;
   }

   static {
      FACING = HorizontalFacingBlock.FACING;
      EYE = Properties.EYE;
      FRAME_SHAPE = Block.createCuboidShape(0.0D, 0.0D, 0.0D, 16.0D, 13.0D, 16.0D);
      EYE_SHAPE = Block.createCuboidShape(4.0D, 13.0D, 4.0D, 12.0D, 16.0D, 12.0D);
      FRAME_WITH_EYE_SHAPE = VoxelShapes.union(FRAME_SHAPE, EYE_SHAPE);
   }
}
