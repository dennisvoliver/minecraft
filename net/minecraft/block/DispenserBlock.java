package net.minecraft.block;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.Map;
import java.util.Random;
import net.minecraft.block.dispenser.DispenserBehavior;
import net.minecraft.block.dispenser.ItemDispenserBehavior;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.DispenserBlockEntity;
import net.minecraft.block.entity.DropperBlockEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.world.ServerWorld;
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
import net.minecraft.util.Util;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPointer;
import net.minecraft.util.math.BlockPointerImpl;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Position;
import net.minecraft.util.math.PositionImpl;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

public class DispenserBlock extends BlockWithEntity {
   public static final DirectionProperty FACING;
   public static final BooleanProperty TRIGGERED;
   private static final Map<Item, DispenserBehavior> BEHAVIORS;

   public static void registerBehavior(ItemConvertible provider, DispenserBehavior behavior) {
      BEHAVIORS.put(provider.asItem(), behavior);
   }

   protected DispenserBlock(AbstractBlock.Settings settings) {
      super(settings);
      this.setDefaultState((BlockState)((BlockState)((BlockState)this.stateManager.getDefaultState()).with(FACING, Direction.NORTH)).with(TRIGGERED, false));
   }

   public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
      if (world.isClient) {
         return ActionResult.SUCCESS;
      } else {
         BlockEntity blockEntity = world.getBlockEntity(pos);
         if (blockEntity instanceof DispenserBlockEntity) {
            player.openHandledScreen((DispenserBlockEntity)blockEntity);
            if (blockEntity instanceof DropperBlockEntity) {
               player.incrementStat(Stats.INSPECT_DROPPER);
            } else {
               player.incrementStat(Stats.INSPECT_DISPENSER);
            }
         }

         return ActionResult.CONSUME;
      }
   }

   protected void dispense(ServerWorld serverWorld, BlockPos pos) {
      BlockPointerImpl blockPointerImpl = new BlockPointerImpl(serverWorld, pos);
      DispenserBlockEntity dispenserBlockEntity = (DispenserBlockEntity)blockPointerImpl.getBlockEntity();
      int i = dispenserBlockEntity.chooseNonEmptySlot();
      if (i < 0) {
         serverWorld.syncWorldEvent(1001, pos, 0);
      } else {
         ItemStack itemStack = dispenserBlockEntity.getStack(i);
         DispenserBehavior dispenserBehavior = this.getBehaviorForItem(itemStack);
         if (dispenserBehavior != DispenserBehavior.NOOP) {
            dispenserBlockEntity.setStack(i, dispenserBehavior.dispense(blockPointerImpl, itemStack));
         }

      }
   }

   protected DispenserBehavior getBehaviorForItem(ItemStack stack) {
      return (DispenserBehavior)BEHAVIORS.get(stack.getItem());
   }

   public void neighborUpdate(BlockState state, World world, BlockPos pos, Block block, BlockPos fromPos, boolean notify) {
      boolean bl = world.isReceivingRedstonePower(pos) || world.isReceivingRedstonePower(pos.up());
      boolean bl2 = (Boolean)state.get(TRIGGERED);
      if (bl && !bl2) {
         world.getBlockTickScheduler().schedule(pos, this, 4);
         world.setBlockState(pos, (BlockState)state.with(TRIGGERED, true), 4);
      } else if (!bl && bl2) {
         world.setBlockState(pos, (BlockState)state.with(TRIGGERED, false), 4);
      }

   }

   public void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
      this.dispense(world, pos);
   }

   public BlockEntity createBlockEntity(BlockView world) {
      return new DispenserBlockEntity();
   }

   public BlockState getPlacementState(ItemPlacementContext ctx) {
      return (BlockState)this.getDefaultState().with(FACING, ctx.getPlayerLookDirection().getOpposite());
   }

   public void onPlaced(World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack itemStack) {
      if (itemStack.hasCustomName()) {
         BlockEntity blockEntity = world.getBlockEntity(pos);
         if (blockEntity instanceof DispenserBlockEntity) {
            ((DispenserBlockEntity)blockEntity).setCustomName(itemStack.getName());
         }
      }

   }

   public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
      if (!state.isOf(newState.getBlock())) {
         BlockEntity blockEntity = world.getBlockEntity(pos);
         if (blockEntity instanceof DispenserBlockEntity) {
            ItemScatterer.spawn(world, (BlockPos)pos, (Inventory)((DispenserBlockEntity)blockEntity));
            world.updateComparators(pos, this);
         }

         super.onStateReplaced(state, world, pos, newState, moved);
      }
   }

   public static Position getOutputLocation(BlockPointer pointer) {
      Direction direction = (Direction)pointer.getBlockState().get(FACING);
      double d = pointer.getX() + 0.7D * (double)direction.getOffsetX();
      double e = pointer.getY() + 0.7D * (double)direction.getOffsetY();
      double f = pointer.getZ() + 0.7D * (double)direction.getOffsetZ();
      return new PositionImpl(d, e, f);
   }

   public boolean hasComparatorOutput(BlockState state) {
      return true;
   }

   public int getComparatorOutput(BlockState state, World world, BlockPos pos) {
      return ScreenHandler.calculateComparatorOutput(world.getBlockEntity(pos));
   }

   public BlockRenderType getRenderType(BlockState state) {
      return BlockRenderType.MODEL;
   }

   public BlockState rotate(BlockState state, BlockRotation rotation) {
      return (BlockState)state.with(FACING, rotation.rotate((Direction)state.get(FACING)));
   }

   public BlockState mirror(BlockState state, BlockMirror mirror) {
      return state.rotate(mirror.getRotation((Direction)state.get(FACING)));
   }

   protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
      builder.add(FACING, TRIGGERED);
   }

   static {
      FACING = FacingBlock.FACING;
      TRIGGERED = Properties.TRIGGERED;
      BEHAVIORS = (Map)Util.make(new Object2ObjectOpenHashMap(), (object2ObjectOpenHashMap) -> {
         object2ObjectOpenHashMap.defaultReturnValue(new ItemDispenserBehavior());
      });
   }
}
