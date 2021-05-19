package net.minecraft.block;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.Hopper;
import net.minecraft.block.entity.HopperBlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.stat.Stats;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.function.BooleanBiFunction;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

public class HopperBlock extends BlockWithEntity {
   public static final DirectionProperty FACING;
   public static final BooleanProperty ENABLED;
   private static final VoxelShape TOP_SHAPE;
   private static final VoxelShape MIDDLE_SHAPE;
   private static final VoxelShape OUTSIDE_SHAPE;
   private static final VoxelShape DEFAULT_SHAPE;
   private static final VoxelShape DOWN_SHAPE;
   private static final VoxelShape EAST_SHAPE;
   private static final VoxelShape NORTH_SHAPE;
   private static final VoxelShape SOUTH_SHAPE;
   private static final VoxelShape WEST_SHAPE;
   private static final VoxelShape DOWN_RAY_TRACE_SHAPE;
   private static final VoxelShape EAST_RAY_TRACE_SHAPE;
   private static final VoxelShape NORTH_RAY_TRACE_SHAPE;
   private static final VoxelShape SOUTH_RAY_TRACE_SHAPE;
   private static final VoxelShape WEST_RAY_TRACE_SHAPE;

   public HopperBlock(AbstractBlock.Settings settings) {
      super(settings);
      this.setDefaultState((BlockState)((BlockState)((BlockState)this.stateManager.getDefaultState()).with(FACING, Direction.DOWN)).with(ENABLED, true));
   }

   public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
      switch((Direction)state.get(FACING)) {
      case DOWN:
         return DOWN_SHAPE;
      case NORTH:
         return NORTH_SHAPE;
      case SOUTH:
         return SOUTH_SHAPE;
      case WEST:
         return WEST_SHAPE;
      case EAST:
         return EAST_SHAPE;
      default:
         return DEFAULT_SHAPE;
      }
   }

   public VoxelShape getRaycastShape(BlockState state, BlockView world, BlockPos pos) {
      switch((Direction)state.get(FACING)) {
      case DOWN:
         return DOWN_RAY_TRACE_SHAPE;
      case NORTH:
         return NORTH_RAY_TRACE_SHAPE;
      case SOUTH:
         return SOUTH_RAY_TRACE_SHAPE;
      case WEST:
         return WEST_RAY_TRACE_SHAPE;
      case EAST:
         return EAST_RAY_TRACE_SHAPE;
      default:
         return Hopper.INSIDE_SHAPE;
      }
   }

   public BlockState getPlacementState(ItemPlacementContext ctx) {
      Direction direction = ctx.getSide().getOpposite();
      return (BlockState)((BlockState)this.getDefaultState().with(FACING, direction.getAxis() == Direction.Axis.Y ? Direction.DOWN : direction)).with(ENABLED, true);
   }

   public BlockEntity createBlockEntity(BlockView world) {
      return new HopperBlockEntity();
   }

   public void onPlaced(World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack itemStack) {
      if (itemStack.hasCustomName()) {
         BlockEntity blockEntity = world.getBlockEntity(pos);
         if (blockEntity instanceof HopperBlockEntity) {
            ((HopperBlockEntity)blockEntity).setCustomName(itemStack.getName());
         }
      }

   }

   public void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState oldState, boolean notify) {
      if (!oldState.isOf(state.getBlock())) {
         this.updateEnabled(world, pos, state);
      }
   }

   public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
      if (world.isClient) {
         return ActionResult.SUCCESS;
      } else {
         BlockEntity blockEntity = world.getBlockEntity(pos);
         if (blockEntity instanceof HopperBlockEntity) {
            player.openHandledScreen((HopperBlockEntity)blockEntity);
            player.incrementStat(Stats.INSPECT_HOPPER);
         }

         return ActionResult.CONSUME;
      }
   }

   public void neighborUpdate(BlockState state, World world, BlockPos pos, Block block, BlockPos fromPos, boolean notify) {
      this.updateEnabled(world, pos, state);
   }

   private void updateEnabled(World world, BlockPos pos, BlockState state) {
      boolean bl = !world.isReceivingRedstonePower(pos);
      if (bl != (Boolean)state.get(ENABLED)) {
         world.setBlockState(pos, (BlockState)state.with(ENABLED, bl), 4);
      }

   }

   public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
      if (!state.isOf(newState.getBlock())) {
         BlockEntity blockEntity = world.getBlockEntity(pos);
         if (blockEntity instanceof HopperBlockEntity) {
            ItemScatterer.spawn(world, (BlockPos)pos, (Inventory)((HopperBlockEntity)blockEntity));
            world.updateComparators(pos, this);
         }

         super.onStateReplaced(state, world, pos, newState, moved);
      }
   }

   public BlockRenderType getRenderType(BlockState state) {
      return BlockRenderType.MODEL;
   }

   public boolean hasComparatorOutput(BlockState state) {
      return true;
   }

   public int getComparatorOutput(BlockState state, World world, BlockPos pos) {
      return ScreenHandler.calculateComparatorOutput(world.getBlockEntity(pos));
   }

   public BlockState rotate(BlockState state, BlockRotation rotation) {
      return (BlockState)state.with(FACING, rotation.rotate((Direction)state.get(FACING)));
   }

   public BlockState mirror(BlockState state, BlockMirror mirror) {
      return state.rotate(mirror.getRotation((Direction)state.get(FACING)));
   }

   protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
      builder.add(FACING, ENABLED);
   }

   public void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity) {
      BlockEntity blockEntity = world.getBlockEntity(pos);
      if (blockEntity instanceof HopperBlockEntity) {
         ((HopperBlockEntity)blockEntity).onEntityCollided(entity);
      }

   }

   public boolean canPathfindThrough(BlockState state, BlockView world, BlockPos pos, NavigationType type) {
      return false;
   }

   static {
      FACING = Properties.HOPPER_FACING;
      ENABLED = Properties.ENABLED;
      TOP_SHAPE = Block.createCuboidShape(0.0D, 10.0D, 0.0D, 16.0D, 16.0D, 16.0D);
      MIDDLE_SHAPE = Block.createCuboidShape(4.0D, 4.0D, 4.0D, 12.0D, 10.0D, 12.0D);
      OUTSIDE_SHAPE = VoxelShapes.union(MIDDLE_SHAPE, TOP_SHAPE);
      DEFAULT_SHAPE = VoxelShapes.combineAndSimplify(OUTSIDE_SHAPE, Hopper.INSIDE_SHAPE, BooleanBiFunction.ONLY_FIRST);
      DOWN_SHAPE = VoxelShapes.union(DEFAULT_SHAPE, Block.createCuboidShape(6.0D, 0.0D, 6.0D, 10.0D, 4.0D, 10.0D));
      EAST_SHAPE = VoxelShapes.union(DEFAULT_SHAPE, Block.createCuboidShape(12.0D, 4.0D, 6.0D, 16.0D, 8.0D, 10.0D));
      NORTH_SHAPE = VoxelShapes.union(DEFAULT_SHAPE, Block.createCuboidShape(6.0D, 4.0D, 0.0D, 10.0D, 8.0D, 4.0D));
      SOUTH_SHAPE = VoxelShapes.union(DEFAULT_SHAPE, Block.createCuboidShape(6.0D, 4.0D, 12.0D, 10.0D, 8.0D, 16.0D));
      WEST_SHAPE = VoxelShapes.union(DEFAULT_SHAPE, Block.createCuboidShape(0.0D, 4.0D, 6.0D, 4.0D, 8.0D, 10.0D));
      DOWN_RAY_TRACE_SHAPE = Hopper.INSIDE_SHAPE;
      EAST_RAY_TRACE_SHAPE = VoxelShapes.union(Hopper.INSIDE_SHAPE, Block.createCuboidShape(12.0D, 8.0D, 6.0D, 16.0D, 10.0D, 10.0D));
      NORTH_RAY_TRACE_SHAPE = VoxelShapes.union(Hopper.INSIDE_SHAPE, Block.createCuboidShape(6.0D, 8.0D, 0.0D, 10.0D, 10.0D, 4.0D));
      SOUTH_RAY_TRACE_SHAPE = VoxelShapes.union(Hopper.INSIDE_SHAPE, Block.createCuboidShape(6.0D, 8.0D, 12.0D, 10.0D, 10.0D, 16.0D));
      WEST_RAY_TRACE_SHAPE = VoxelShapes.union(Hopper.INSIDE_SHAPE, Block.createCuboidShape(0.0D, 8.0D, 6.0D, 4.0D, 10.0D, 10.0D));
   }
}
