package net.minecraft.block;

import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.function.Predicate;
import net.minecraft.block.enums.RailShape;
import net.minecraft.entity.Entity;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.entity.vehicle.CommandBlockMinecartEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.state.property.Property;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class DetectorRailBlock extends AbstractRailBlock {
   public static final EnumProperty<RailShape> SHAPE;
   public static final BooleanProperty POWERED;

   public DetectorRailBlock(AbstractBlock.Settings settings) {
      super(true, settings);
      this.setDefaultState((BlockState)((BlockState)((BlockState)this.stateManager.getDefaultState()).with(POWERED, false)).with(SHAPE, RailShape.NORTH_SOUTH));
   }

   public boolean emitsRedstonePower(BlockState state) {
      return true;
   }

   public void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity) {
      if (!world.isClient) {
         if (!(Boolean)state.get(POWERED)) {
            this.updatePoweredStatus(world, pos, state);
         }
      }
   }

   public void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
      if ((Boolean)state.get(POWERED)) {
         this.updatePoweredStatus(world, pos, state);
      }
   }

   public int getWeakRedstonePower(BlockState state, BlockView world, BlockPos pos, Direction direction) {
      return (Boolean)state.get(POWERED) ? 15 : 0;
   }

   public int getStrongRedstonePower(BlockState state, BlockView world, BlockPos pos, Direction direction) {
      if (!(Boolean)state.get(POWERED)) {
         return 0;
      } else {
         return direction == Direction.UP ? 15 : 0;
      }
   }

   private void updatePoweredStatus(World world, BlockPos pos, BlockState state) {
      if (this.canPlaceAt(state, world, pos)) {
         boolean bl = (Boolean)state.get(POWERED);
         boolean bl2 = false;
         List<AbstractMinecartEntity> list = this.getCarts(world, pos, AbstractMinecartEntity.class, (Predicate)null);
         if (!list.isEmpty()) {
            bl2 = true;
         }

         BlockState blockState2;
         if (bl2 && !bl) {
            blockState2 = (BlockState)state.with(POWERED, true);
            world.setBlockState(pos, blockState2, 3);
            this.updateNearbyRails(world, pos, blockState2, true);
            world.updateNeighborsAlways(pos, this);
            world.updateNeighborsAlways(pos.down(), this);
            world.scheduleBlockRerenderIfNeeded(pos, state, blockState2);
         }

         if (!bl2 && bl) {
            blockState2 = (BlockState)state.with(POWERED, false);
            world.setBlockState(pos, blockState2, 3);
            this.updateNearbyRails(world, pos, blockState2, false);
            world.updateNeighborsAlways(pos, this);
            world.updateNeighborsAlways(pos.down(), this);
            world.scheduleBlockRerenderIfNeeded(pos, state, blockState2);
         }

         if (bl2) {
            world.getBlockTickScheduler().schedule(pos, this, 20);
         }

         world.updateComparators(pos, this);
      }
   }

   protected void updateNearbyRails(World world, BlockPos pos, BlockState state, boolean unpowering) {
      RailPlacementHelper railPlacementHelper = new RailPlacementHelper(world, pos, state);
      List<BlockPos> list = railPlacementHelper.getNeighbors();
      Iterator var7 = list.iterator();

      while(var7.hasNext()) {
         BlockPos blockPos = (BlockPos)var7.next();
         BlockState blockState = world.getBlockState(blockPos);
         blockState.neighborUpdate(world, blockPos, blockState.getBlock(), pos, false);
      }

   }

   public void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState oldState, boolean notify) {
      if (!oldState.isOf(state.getBlock())) {
         this.updatePoweredStatus(world, pos, this.updateCurves(state, world, pos, notify));
      }
   }

   public Property<RailShape> getShapeProperty() {
      return SHAPE;
   }

   public boolean hasComparatorOutput(BlockState state) {
      return true;
   }

   public int getComparatorOutput(BlockState state, World world, BlockPos pos) {
      if ((Boolean)state.get(POWERED)) {
         List<CommandBlockMinecartEntity> list = this.getCarts(world, pos, CommandBlockMinecartEntity.class, (Predicate)null);
         if (!list.isEmpty()) {
            return ((CommandBlockMinecartEntity)list.get(0)).getCommandExecutor().getSuccessCount();
         }

         List<AbstractMinecartEntity> list2 = this.getCarts(world, pos, AbstractMinecartEntity.class, EntityPredicates.VALID_INVENTORIES);
         if (!list2.isEmpty()) {
            return ScreenHandler.calculateComparatorOutput((Inventory)list2.get(0));
         }
      }

      return 0;
   }

   protected <T extends AbstractMinecartEntity> List<T> getCarts(World world, BlockPos pos, Class<T> entityClass, @Nullable Predicate<Entity> entityPredicate) {
      return world.getEntitiesByClass(entityClass, this.getCartDetectionBox(pos), entityPredicate);
   }

   private Box getCartDetectionBox(BlockPos pos) {
      double d = 0.2D;
      return new Box((double)pos.getX() + 0.2D, (double)pos.getY(), (double)pos.getZ() + 0.2D, (double)(pos.getX() + 1) - 0.2D, (double)(pos.getY() + 1) - 0.2D, (double)(pos.getZ() + 1) - 0.2D);
   }

   public BlockState rotate(BlockState state, BlockRotation rotation) {
      switch(rotation) {
      case CLOCKWISE_180:
         switch((RailShape)state.get(SHAPE)) {
         case ASCENDING_EAST:
            return (BlockState)state.with(SHAPE, RailShape.ASCENDING_WEST);
         case ASCENDING_WEST:
            return (BlockState)state.with(SHAPE, RailShape.ASCENDING_EAST);
         case ASCENDING_NORTH:
            return (BlockState)state.with(SHAPE, RailShape.ASCENDING_SOUTH);
         case ASCENDING_SOUTH:
            return (BlockState)state.with(SHAPE, RailShape.ASCENDING_NORTH);
         case SOUTH_EAST:
            return (BlockState)state.with(SHAPE, RailShape.NORTH_WEST);
         case SOUTH_WEST:
            return (BlockState)state.with(SHAPE, RailShape.NORTH_EAST);
         case NORTH_WEST:
            return (BlockState)state.with(SHAPE, RailShape.SOUTH_EAST);
         case NORTH_EAST:
            return (BlockState)state.with(SHAPE, RailShape.SOUTH_WEST);
         }
      case COUNTERCLOCKWISE_90:
         switch((RailShape)state.get(SHAPE)) {
         case ASCENDING_EAST:
            return (BlockState)state.with(SHAPE, RailShape.ASCENDING_NORTH);
         case ASCENDING_WEST:
            return (BlockState)state.with(SHAPE, RailShape.ASCENDING_SOUTH);
         case ASCENDING_NORTH:
            return (BlockState)state.with(SHAPE, RailShape.ASCENDING_WEST);
         case ASCENDING_SOUTH:
            return (BlockState)state.with(SHAPE, RailShape.ASCENDING_EAST);
         case SOUTH_EAST:
            return (BlockState)state.with(SHAPE, RailShape.NORTH_EAST);
         case SOUTH_WEST:
            return (BlockState)state.with(SHAPE, RailShape.SOUTH_EAST);
         case NORTH_WEST:
            return (BlockState)state.with(SHAPE, RailShape.SOUTH_WEST);
         case NORTH_EAST:
            return (BlockState)state.with(SHAPE, RailShape.NORTH_WEST);
         case NORTH_SOUTH:
            return (BlockState)state.with(SHAPE, RailShape.EAST_WEST);
         case EAST_WEST:
            return (BlockState)state.with(SHAPE, RailShape.NORTH_SOUTH);
         }
      case CLOCKWISE_90:
         switch((RailShape)state.get(SHAPE)) {
         case ASCENDING_EAST:
            return (BlockState)state.with(SHAPE, RailShape.ASCENDING_SOUTH);
         case ASCENDING_WEST:
            return (BlockState)state.with(SHAPE, RailShape.ASCENDING_NORTH);
         case ASCENDING_NORTH:
            return (BlockState)state.with(SHAPE, RailShape.ASCENDING_EAST);
         case ASCENDING_SOUTH:
            return (BlockState)state.with(SHAPE, RailShape.ASCENDING_WEST);
         case SOUTH_EAST:
            return (BlockState)state.with(SHAPE, RailShape.SOUTH_WEST);
         case SOUTH_WEST:
            return (BlockState)state.with(SHAPE, RailShape.NORTH_WEST);
         case NORTH_WEST:
            return (BlockState)state.with(SHAPE, RailShape.NORTH_EAST);
         case NORTH_EAST:
            return (BlockState)state.with(SHAPE, RailShape.SOUTH_EAST);
         case NORTH_SOUTH:
            return (BlockState)state.with(SHAPE, RailShape.EAST_WEST);
         case EAST_WEST:
            return (BlockState)state.with(SHAPE, RailShape.NORTH_SOUTH);
         }
      default:
         return state;
      }
   }

   public BlockState mirror(BlockState state, BlockMirror mirror) {
      RailShape railShape = (RailShape)state.get(SHAPE);
      switch(mirror) {
      case LEFT_RIGHT:
         switch(railShape) {
         case ASCENDING_NORTH:
            return (BlockState)state.with(SHAPE, RailShape.ASCENDING_SOUTH);
         case ASCENDING_SOUTH:
            return (BlockState)state.with(SHAPE, RailShape.ASCENDING_NORTH);
         case SOUTH_EAST:
            return (BlockState)state.with(SHAPE, RailShape.NORTH_EAST);
         case SOUTH_WEST:
            return (BlockState)state.with(SHAPE, RailShape.NORTH_WEST);
         case NORTH_WEST:
            return (BlockState)state.with(SHAPE, RailShape.SOUTH_WEST);
         case NORTH_EAST:
            return (BlockState)state.with(SHAPE, RailShape.SOUTH_EAST);
         default:
            return super.mirror(state, mirror);
         }
      case FRONT_BACK:
         switch(railShape) {
         case ASCENDING_EAST:
            return (BlockState)state.with(SHAPE, RailShape.ASCENDING_WEST);
         case ASCENDING_WEST:
            return (BlockState)state.with(SHAPE, RailShape.ASCENDING_EAST);
         case ASCENDING_NORTH:
         case ASCENDING_SOUTH:
         default:
            break;
         case SOUTH_EAST:
            return (BlockState)state.with(SHAPE, RailShape.SOUTH_WEST);
         case SOUTH_WEST:
            return (BlockState)state.with(SHAPE, RailShape.SOUTH_EAST);
         case NORTH_WEST:
            return (BlockState)state.with(SHAPE, RailShape.NORTH_EAST);
         case NORTH_EAST:
            return (BlockState)state.with(SHAPE, RailShape.NORTH_WEST);
         }
      }

      return super.mirror(state, mirror);
   }

   protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
      builder.add(SHAPE, POWERED);
   }

   static {
      SHAPE = Properties.STRAIGHT_RAIL_SHAPE;
      POWERED = Properties.POWERED;
   }
}
