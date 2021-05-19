package net.minecraft.block;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.Items;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.state.property.Property;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;

public class TripwireBlock extends Block {
   public static final BooleanProperty POWERED;
   public static final BooleanProperty ATTACHED;
   public static final BooleanProperty DISARMED;
   public static final BooleanProperty NORTH;
   public static final BooleanProperty EAST;
   public static final BooleanProperty SOUTH;
   public static final BooleanProperty WEST;
   private static final Map<Direction, BooleanProperty> FACING_PROPERTIES;
   protected static final VoxelShape ATTACHED_SHAPE;
   protected static final VoxelShape DETACHED_SHAPE;
   private final TripwireHookBlock hookBlock;

   public TripwireBlock(TripwireHookBlock hookBlock, AbstractBlock.Settings settings) {
      super(settings);
      this.setDefaultState((BlockState)((BlockState)((BlockState)((BlockState)((BlockState)((BlockState)((BlockState)((BlockState)this.stateManager.getDefaultState()).with(POWERED, false)).with(ATTACHED, false)).with(DISARMED, false)).with(NORTH, false)).with(EAST, false)).with(SOUTH, false)).with(WEST, false));
      this.hookBlock = hookBlock;
   }

   public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
      return (Boolean)state.get(ATTACHED) ? ATTACHED_SHAPE : DETACHED_SHAPE;
   }

   public BlockState getPlacementState(ItemPlacementContext ctx) {
      BlockView blockView = ctx.getWorld();
      BlockPos blockPos = ctx.getBlockPos();
      return (BlockState)((BlockState)((BlockState)((BlockState)this.getDefaultState().with(NORTH, this.shouldConnectTo(blockView.getBlockState(blockPos.north()), Direction.NORTH))).with(EAST, this.shouldConnectTo(blockView.getBlockState(blockPos.east()), Direction.EAST))).with(SOUTH, this.shouldConnectTo(blockView.getBlockState(blockPos.south()), Direction.SOUTH))).with(WEST, this.shouldConnectTo(blockView.getBlockState(blockPos.west()), Direction.WEST));
   }

   public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState newState, WorldAccess world, BlockPos pos, BlockPos posFrom) {
      return direction.getAxis().isHorizontal() ? (BlockState)state.with((Property)FACING_PROPERTIES.get(direction), this.shouldConnectTo(newState, direction)) : super.getStateForNeighborUpdate(state, direction, newState, world, pos, posFrom);
   }

   public void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState oldState, boolean notify) {
      if (!oldState.isOf(state.getBlock())) {
         this.update(world, pos, state);
      }
   }

   public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
      if (!moved && !state.isOf(newState.getBlock())) {
         this.update(world, pos, (BlockState)state.with(POWERED, true));
      }
   }

   public void onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player) {
      if (!world.isClient && !player.getMainHandStack().isEmpty() && player.getMainHandStack().getItem() == Items.SHEARS) {
         world.setBlockState(pos, (BlockState)state.with(DISARMED, true), 4);
      }

      super.onBreak(world, pos, state, player);
   }

   private void update(World world, BlockPos pos, BlockState state) {
      Direction[] var4 = new Direction[]{Direction.SOUTH, Direction.WEST};
      int var5 = var4.length;

      for(int var6 = 0; var6 < var5; ++var6) {
         Direction direction = var4[var6];

         for(int i = 1; i < 42; ++i) {
            BlockPos blockPos = pos.offset(direction, i);
            BlockState blockState = world.getBlockState(blockPos);
            if (blockState.isOf(this.hookBlock)) {
               if (blockState.get(TripwireHookBlock.FACING) == direction.getOpposite()) {
                  this.hookBlock.update(world, blockPos, blockState, false, true, i, state);
               }
               break;
            }

            if (!blockState.isOf(this)) {
               break;
            }
         }
      }

   }

   public void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity) {
      if (!world.isClient) {
         if (!(Boolean)state.get(POWERED)) {
            this.updatePowered(world, pos);
         }
      }
   }

   public void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
      if ((Boolean)world.getBlockState(pos).get(POWERED)) {
         this.updatePowered(world, pos);
      }
   }

   private void updatePowered(World world, BlockPos pos) {
      BlockState blockState = world.getBlockState(pos);
      boolean bl = (Boolean)blockState.get(POWERED);
      boolean bl2 = false;
      List<? extends Entity> list = world.getOtherEntities((Entity)null, blockState.getOutlineShape(world, pos).getBoundingBox().offset(pos));
      if (!list.isEmpty()) {
         Iterator var7 = list.iterator();

         while(var7.hasNext()) {
            Entity entity = (Entity)var7.next();
            if (!entity.canAvoidTraps()) {
               bl2 = true;
               break;
            }
         }
      }

      if (bl2 != bl) {
         blockState = (BlockState)blockState.with(POWERED, bl2);
         world.setBlockState(pos, blockState, 3);
         this.update(world, pos, blockState);
      }

      if (bl2) {
         world.getBlockTickScheduler().schedule(new BlockPos(pos), this, 10);
      }

   }

   public boolean shouldConnectTo(BlockState state, Direction facing) {
      Block block = state.getBlock();
      if (block == this.hookBlock) {
         return state.get(TripwireHookBlock.FACING) == facing.getOpposite();
      } else {
         return block == this;
      }
   }

   public BlockState rotate(BlockState state, BlockRotation rotation) {
      switch(rotation) {
      case CLOCKWISE_180:
         return (BlockState)((BlockState)((BlockState)((BlockState)state.with(NORTH, state.get(SOUTH))).with(EAST, state.get(WEST))).with(SOUTH, state.get(NORTH))).with(WEST, state.get(EAST));
      case COUNTERCLOCKWISE_90:
         return (BlockState)((BlockState)((BlockState)((BlockState)state.with(NORTH, state.get(EAST))).with(EAST, state.get(SOUTH))).with(SOUTH, state.get(WEST))).with(WEST, state.get(NORTH));
      case CLOCKWISE_90:
         return (BlockState)((BlockState)((BlockState)((BlockState)state.with(NORTH, state.get(WEST))).with(EAST, state.get(NORTH))).with(SOUTH, state.get(EAST))).with(WEST, state.get(SOUTH));
      default:
         return state;
      }
   }

   public BlockState mirror(BlockState state, BlockMirror mirror) {
      switch(mirror) {
      case LEFT_RIGHT:
         return (BlockState)((BlockState)state.with(NORTH, state.get(SOUTH))).with(SOUTH, state.get(NORTH));
      case FRONT_BACK:
         return (BlockState)((BlockState)state.with(EAST, state.get(WEST))).with(WEST, state.get(EAST));
      default:
         return super.mirror(state, mirror);
      }
   }

   protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
      builder.add(POWERED, ATTACHED, DISARMED, NORTH, EAST, WEST, SOUTH);
   }

   static {
      POWERED = Properties.POWERED;
      ATTACHED = Properties.ATTACHED;
      DISARMED = Properties.DISARMED;
      NORTH = ConnectingBlock.NORTH;
      EAST = ConnectingBlock.EAST;
      SOUTH = ConnectingBlock.SOUTH;
      WEST = ConnectingBlock.WEST;
      FACING_PROPERTIES = HorizontalConnectingBlock.FACING_PROPERTIES;
      ATTACHED_SHAPE = Block.createCuboidShape(0.0D, 1.0D, 0.0D, 16.0D, 2.5D, 16.0D);
      DETACHED_SHAPE = Block.createCuboidShape(0.0D, 0.0D, 0.0D, 16.0D, 8.0D, 16.0D);
   }
}
