package net.minecraft.block;

import java.util.Arrays;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.enums.PistonType;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;

public class PistonHeadBlock extends FacingBlock {
   public static final EnumProperty<PistonType> TYPE;
   public static final BooleanProperty SHORT;
   protected static final VoxelShape EAST_HEAD_SHAPE;
   protected static final VoxelShape WEST_HEAD_SHAPE;
   protected static final VoxelShape SOUTH_HEAD_SHAPE;
   protected static final VoxelShape NORTH_HEAD_SHAPE;
   protected static final VoxelShape UP_HEAD_SHAPE;
   protected static final VoxelShape DOWN_HEAD_SHAPE;
   protected static final VoxelShape UP_ARM_SHAPE;
   protected static final VoxelShape DOWN_ARM_SHAPE;
   protected static final VoxelShape SOUTH_ARM_SHAPE;
   protected static final VoxelShape NORTH_ARM_SHAPE;
   protected static final VoxelShape EAST_ARM_SHAPE;
   protected static final VoxelShape WEST_ARM_SHAPE;
   protected static final VoxelShape SHORT_UP_ARM_SHAPE;
   protected static final VoxelShape SHORT_DOWN_ARM_SHAPE;
   protected static final VoxelShape SHORT_SOUTH_ARM_SHAPE;
   protected static final VoxelShape SHORT_NORTH_ARM_SHAPE;
   protected static final VoxelShape SHORT_EAST_ARM_SHAPE;
   protected static final VoxelShape SHORT_WEST_ARM_SHAPE;
   private static final VoxelShape[] field_26660;
   private static final VoxelShape[] field_26661;

   private static VoxelShape[] method_31019(boolean bl) {
      return (VoxelShape[])Arrays.stream(Direction.values()).map((direction) -> {
         return getHeadShape(direction, bl);
      }).toArray((i) -> {
         return new VoxelShape[i];
      });
   }

   private static VoxelShape getHeadShape(Direction direction, boolean bl) {
      switch(direction) {
      case DOWN:
      default:
         return VoxelShapes.union(DOWN_HEAD_SHAPE, bl ? SHORT_DOWN_ARM_SHAPE : DOWN_ARM_SHAPE);
      case UP:
         return VoxelShapes.union(UP_HEAD_SHAPE, bl ? SHORT_UP_ARM_SHAPE : UP_ARM_SHAPE);
      case NORTH:
         return VoxelShapes.union(NORTH_HEAD_SHAPE, bl ? SHORT_NORTH_ARM_SHAPE : NORTH_ARM_SHAPE);
      case SOUTH:
         return VoxelShapes.union(SOUTH_HEAD_SHAPE, bl ? SHORT_SOUTH_ARM_SHAPE : SOUTH_ARM_SHAPE);
      case WEST:
         return VoxelShapes.union(WEST_HEAD_SHAPE, bl ? SHORT_WEST_ARM_SHAPE : WEST_ARM_SHAPE);
      case EAST:
         return VoxelShapes.union(EAST_HEAD_SHAPE, bl ? SHORT_EAST_ARM_SHAPE : EAST_ARM_SHAPE);
      }
   }

   public PistonHeadBlock(AbstractBlock.Settings settings) {
      super(settings);
      this.setDefaultState((BlockState)((BlockState)((BlockState)((BlockState)this.stateManager.getDefaultState()).with(FACING, Direction.NORTH)).with(TYPE, PistonType.DEFAULT)).with(SHORT, false));
   }

   public boolean hasSidedTransparency(BlockState state) {
      return true;
   }

   public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
      return ((Boolean)state.get(SHORT) ? field_26660 : field_26661)[((Direction)state.get(FACING)).ordinal()];
   }

   private boolean method_26980(BlockState blockState, BlockState blockState2) {
      Block block = blockState.get(TYPE) == PistonType.DEFAULT ? Blocks.PISTON : Blocks.STICKY_PISTON;
      return blockState2.isOf(block) && (Boolean)blockState2.get(PistonBlock.EXTENDED) && blockState2.get(FACING) == blockState.get(FACING);
   }

   public void onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player) {
      if (!world.isClient && player.abilities.creativeMode) {
         BlockPos blockPos = pos.offset(((Direction)state.get(FACING)).getOpposite());
         if (this.method_26980(state, world.getBlockState(blockPos))) {
            world.breakBlock(blockPos, false);
         }
      }

      super.onBreak(world, pos, state, player);
   }

   public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
      if (!state.isOf(newState.getBlock())) {
         super.onStateReplaced(state, world, pos, newState, moved);
         BlockPos blockPos = pos.offset(((Direction)state.get(FACING)).getOpposite());
         if (this.method_26980(state, world.getBlockState(blockPos))) {
            world.breakBlock(blockPos, true);
         }

      }
   }

   public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState newState, WorldAccess world, BlockPos pos, BlockPos posFrom) {
      return direction.getOpposite() == state.get(FACING) && !state.canPlaceAt(world, pos) ? Blocks.AIR.getDefaultState() : super.getStateForNeighborUpdate(state, direction, newState, world, pos, posFrom);
   }

   public boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
      BlockState blockState = world.getBlockState(pos.offset(((Direction)state.get(FACING)).getOpposite()));
      return this.method_26980(state, blockState) || blockState.isOf(Blocks.MOVING_PISTON) && blockState.get(FACING) == state.get(FACING);
   }

   public void neighborUpdate(BlockState state, World world, BlockPos pos, Block block, BlockPos fromPos, boolean notify) {
      if (state.canPlaceAt(world, pos)) {
         BlockPos blockPos = pos.offset(((Direction)state.get(FACING)).getOpposite());
         world.getBlockState(blockPos).neighborUpdate(world, blockPos, block, fromPos, false);
      }

   }

   @Environment(EnvType.CLIENT)
   public ItemStack getPickStack(BlockView world, BlockPos pos, BlockState state) {
      return new ItemStack(state.get(TYPE) == PistonType.STICKY ? Blocks.STICKY_PISTON : Blocks.PISTON);
   }

   public BlockState rotate(BlockState state, BlockRotation rotation) {
      return (BlockState)state.with(FACING, rotation.rotate((Direction)state.get(FACING)));
   }

   public BlockState mirror(BlockState state, BlockMirror mirror) {
      return state.rotate(mirror.getRotation((Direction)state.get(FACING)));
   }

   protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
      builder.add(FACING, TYPE, SHORT);
   }

   public boolean canPathfindThrough(BlockState state, BlockView world, BlockPos pos, NavigationType type) {
      return false;
   }

   static {
      TYPE = Properties.PISTON_TYPE;
      SHORT = Properties.SHORT;
      EAST_HEAD_SHAPE = Block.createCuboidShape(12.0D, 0.0D, 0.0D, 16.0D, 16.0D, 16.0D);
      WEST_HEAD_SHAPE = Block.createCuboidShape(0.0D, 0.0D, 0.0D, 4.0D, 16.0D, 16.0D);
      SOUTH_HEAD_SHAPE = Block.createCuboidShape(0.0D, 0.0D, 12.0D, 16.0D, 16.0D, 16.0D);
      NORTH_HEAD_SHAPE = Block.createCuboidShape(0.0D, 0.0D, 0.0D, 16.0D, 16.0D, 4.0D);
      UP_HEAD_SHAPE = Block.createCuboidShape(0.0D, 12.0D, 0.0D, 16.0D, 16.0D, 16.0D);
      DOWN_HEAD_SHAPE = Block.createCuboidShape(0.0D, 0.0D, 0.0D, 16.0D, 4.0D, 16.0D);
      UP_ARM_SHAPE = Block.createCuboidShape(6.0D, -4.0D, 6.0D, 10.0D, 12.0D, 10.0D);
      DOWN_ARM_SHAPE = Block.createCuboidShape(6.0D, 4.0D, 6.0D, 10.0D, 20.0D, 10.0D);
      SOUTH_ARM_SHAPE = Block.createCuboidShape(6.0D, 6.0D, -4.0D, 10.0D, 10.0D, 12.0D);
      NORTH_ARM_SHAPE = Block.createCuboidShape(6.0D, 6.0D, 4.0D, 10.0D, 10.0D, 20.0D);
      EAST_ARM_SHAPE = Block.createCuboidShape(-4.0D, 6.0D, 6.0D, 12.0D, 10.0D, 10.0D);
      WEST_ARM_SHAPE = Block.createCuboidShape(4.0D, 6.0D, 6.0D, 20.0D, 10.0D, 10.0D);
      SHORT_UP_ARM_SHAPE = Block.createCuboidShape(6.0D, 0.0D, 6.0D, 10.0D, 12.0D, 10.0D);
      SHORT_DOWN_ARM_SHAPE = Block.createCuboidShape(6.0D, 4.0D, 6.0D, 10.0D, 16.0D, 10.0D);
      SHORT_SOUTH_ARM_SHAPE = Block.createCuboidShape(6.0D, 6.0D, 0.0D, 10.0D, 10.0D, 12.0D);
      SHORT_NORTH_ARM_SHAPE = Block.createCuboidShape(6.0D, 6.0D, 4.0D, 10.0D, 10.0D, 16.0D);
      SHORT_EAST_ARM_SHAPE = Block.createCuboidShape(0.0D, 6.0D, 6.0D, 12.0D, 10.0D, 10.0D);
      SHORT_WEST_ARM_SHAPE = Block.createCuboidShape(4.0D, 6.0D, 6.0D, 16.0D, 10.0D, 10.0D);
      field_26660 = method_31019(true);
      field_26661 = method_31019(false);
   }
}
