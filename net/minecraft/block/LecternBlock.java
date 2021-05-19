package net.minecraft.block;

import java.util.Random;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.LecternBlockEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.tag.ItemTags;
import net.minecraft.tag.Tag;
import net.minecraft.util.ActionResult;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class LecternBlock extends BlockWithEntity {
   public static final DirectionProperty FACING;
   public static final BooleanProperty POWERED;
   public static final BooleanProperty HAS_BOOK;
   public static final VoxelShape BOTTOM_SHAPE;
   public static final VoxelShape MIDDLE_SHAPE;
   public static final VoxelShape BASE_SHAPE;
   public static final VoxelShape COLLISION_SHAPE_TOP;
   public static final VoxelShape COLLISION_SHAPE;
   public static final VoxelShape WEST_SHAPE;
   public static final VoxelShape NORTH_SHAPE;
   public static final VoxelShape EAST_SHAPE;
   public static final VoxelShape SOUTH_SHAPE;

   protected LecternBlock(AbstractBlock.Settings settings) {
      super(settings);
      this.setDefaultState((BlockState)((BlockState)((BlockState)((BlockState)this.stateManager.getDefaultState()).with(FACING, Direction.NORTH)).with(POWERED, false)).with(HAS_BOOK, false));
   }

   public BlockRenderType getRenderType(BlockState state) {
      return BlockRenderType.MODEL;
   }

   public VoxelShape getCullingShape(BlockState state, BlockView world, BlockPos pos) {
      return BASE_SHAPE;
   }

   public boolean hasSidedTransparency(BlockState state) {
      return true;
   }

   public BlockState getPlacementState(ItemPlacementContext ctx) {
      World world = ctx.getWorld();
      ItemStack itemStack = ctx.getStack();
      CompoundTag compoundTag = itemStack.getTag();
      PlayerEntity playerEntity = ctx.getPlayer();
      boolean bl = false;
      if (!world.isClient && playerEntity != null && compoundTag != null && playerEntity.isCreativeLevelTwoOp() && compoundTag.contains("BlockEntityTag")) {
         CompoundTag compoundTag2 = compoundTag.getCompound("BlockEntityTag");
         if (compoundTag2.contains("Book")) {
            bl = true;
         }
      }

      return (BlockState)((BlockState)this.getDefaultState().with(FACING, ctx.getPlayerFacing().getOpposite())).with(HAS_BOOK, bl);
   }

   public VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
      return COLLISION_SHAPE;
   }

   public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
      switch((Direction)state.get(FACING)) {
      case NORTH:
         return NORTH_SHAPE;
      case SOUTH:
         return SOUTH_SHAPE;
      case EAST:
         return EAST_SHAPE;
      case WEST:
         return WEST_SHAPE;
      default:
         return BASE_SHAPE;
      }
   }

   public BlockState rotate(BlockState state, BlockRotation rotation) {
      return (BlockState)state.with(FACING, rotation.rotate((Direction)state.get(FACING)));
   }

   public BlockState mirror(BlockState state, BlockMirror mirror) {
      return state.rotate(mirror.getRotation((Direction)state.get(FACING)));
   }

   protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
      builder.add(FACING, POWERED, HAS_BOOK);
   }

   @Nullable
   public BlockEntity createBlockEntity(BlockView world) {
      return new LecternBlockEntity();
   }

   public static boolean putBookIfAbsent(World world, BlockPos pos, BlockState state, ItemStack book) {
      if (!(Boolean)state.get(HAS_BOOK)) {
         if (!world.isClient) {
            putBook(world, pos, state, book);
         }

         return true;
      } else {
         return false;
      }
   }

   private static void putBook(World world, BlockPos pos, BlockState state, ItemStack book) {
      BlockEntity blockEntity = world.getBlockEntity(pos);
      if (blockEntity instanceof LecternBlockEntity) {
         LecternBlockEntity lecternBlockEntity = (LecternBlockEntity)blockEntity;
         lecternBlockEntity.setBook(book.split(1));
         setHasBook(world, pos, state, true);
         world.playSound((PlayerEntity)null, pos, SoundEvents.ITEM_BOOK_PUT, SoundCategory.BLOCKS, 1.0F, 1.0F);
      }

   }

   public static void setHasBook(World world, BlockPos pos, BlockState state, boolean hasBook) {
      world.setBlockState(pos, (BlockState)((BlockState)state.with(POWERED, false)).with(HAS_BOOK, hasBook), 3);
      updateNeighborAlways(world, pos, state);
   }

   public static void setPowered(World world, BlockPos pos, BlockState state) {
      setPowered(world, pos, state, true);
      world.getBlockTickScheduler().schedule(pos, state.getBlock(), 2);
      world.syncWorldEvent(1043, pos, 0);
   }

   private static void setPowered(World world, BlockPos pos, BlockState state, boolean powered) {
      world.setBlockState(pos, (BlockState)state.with(POWERED, powered), 3);
      updateNeighborAlways(world, pos, state);
   }

   private static void updateNeighborAlways(World world, BlockPos pos, BlockState state) {
      world.updateNeighborsAlways(pos.down(), state.getBlock());
   }

   public void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
      setPowered(world, pos, state, false);
   }

   public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
      if (!state.isOf(newState.getBlock())) {
         if ((Boolean)state.get(HAS_BOOK)) {
            this.dropBook(state, world, pos);
         }

         if ((Boolean)state.get(POWERED)) {
            world.updateNeighborsAlways(pos.down(), this);
         }

         super.onStateReplaced(state, world, pos, newState, moved);
      }
   }

   private void dropBook(BlockState state, World world, BlockPos pos) {
      BlockEntity blockEntity = world.getBlockEntity(pos);
      if (blockEntity instanceof LecternBlockEntity) {
         LecternBlockEntity lecternBlockEntity = (LecternBlockEntity)blockEntity;
         Direction direction = (Direction)state.get(FACING);
         ItemStack itemStack = lecternBlockEntity.getBook().copy();
         float f = 0.25F * (float)direction.getOffsetX();
         float g = 0.25F * (float)direction.getOffsetZ();
         ItemEntity itemEntity = new ItemEntity(world, (double)pos.getX() + 0.5D + (double)f, (double)(pos.getY() + 1), (double)pos.getZ() + 0.5D + (double)g, itemStack);
         itemEntity.setToDefaultPickupDelay();
         world.spawnEntity(itemEntity);
         lecternBlockEntity.clear();
      }

   }

   public boolean emitsRedstonePower(BlockState state) {
      return true;
   }

   public int getWeakRedstonePower(BlockState state, BlockView world, BlockPos pos, Direction direction) {
      return (Boolean)state.get(POWERED) ? 15 : 0;
   }

   public int getStrongRedstonePower(BlockState state, BlockView world, BlockPos pos, Direction direction) {
      return direction == Direction.UP && (Boolean)state.get(POWERED) ? 15 : 0;
   }

   public boolean hasComparatorOutput(BlockState state) {
      return true;
   }

   public int getComparatorOutput(BlockState state, World world, BlockPos pos) {
      if ((Boolean)state.get(HAS_BOOK)) {
         BlockEntity blockEntity = world.getBlockEntity(pos);
         if (blockEntity instanceof LecternBlockEntity) {
            return ((LecternBlockEntity)blockEntity).getComparatorOutput();
         }
      }

      return 0;
   }

   public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
      if ((Boolean)state.get(HAS_BOOK)) {
         if (!world.isClient) {
            this.openScreen(world, pos, player);
         }

         return ActionResult.success(world.isClient);
      } else {
         ItemStack itemStack = player.getStackInHand(hand);
         return !itemStack.isEmpty() && !itemStack.getItem().isIn((Tag)ItemTags.LECTERN_BOOKS) ? ActionResult.CONSUME : ActionResult.PASS;
      }
   }

   @Nullable
   public NamedScreenHandlerFactory createScreenHandlerFactory(BlockState state, World world, BlockPos pos) {
      return !(Boolean)state.get(HAS_BOOK) ? null : super.createScreenHandlerFactory(state, world, pos);
   }

   private void openScreen(World world, BlockPos pos, PlayerEntity player) {
      BlockEntity blockEntity = world.getBlockEntity(pos);
      if (blockEntity instanceof LecternBlockEntity) {
         player.openHandledScreen((LecternBlockEntity)blockEntity);
         player.incrementStat(Stats.INTERACT_WITH_LECTERN);
      }

   }

   public boolean canPathfindThrough(BlockState state, BlockView world, BlockPos pos, NavigationType type) {
      return false;
   }

   static {
      FACING = HorizontalFacingBlock.FACING;
      POWERED = Properties.POWERED;
      HAS_BOOK = Properties.HAS_BOOK;
      BOTTOM_SHAPE = Block.createCuboidShape(0.0D, 0.0D, 0.0D, 16.0D, 2.0D, 16.0D);
      MIDDLE_SHAPE = Block.createCuboidShape(4.0D, 2.0D, 4.0D, 12.0D, 14.0D, 12.0D);
      BASE_SHAPE = VoxelShapes.union(BOTTOM_SHAPE, MIDDLE_SHAPE);
      COLLISION_SHAPE_TOP = Block.createCuboidShape(0.0D, 15.0D, 0.0D, 16.0D, 15.0D, 16.0D);
      COLLISION_SHAPE = VoxelShapes.union(BASE_SHAPE, COLLISION_SHAPE_TOP);
      WEST_SHAPE = VoxelShapes.union(Block.createCuboidShape(1.0D, 10.0D, 0.0D, 5.333333D, 14.0D, 16.0D), Block.createCuboidShape(5.333333D, 12.0D, 0.0D, 9.666667D, 16.0D, 16.0D), Block.createCuboidShape(9.666667D, 14.0D, 0.0D, 14.0D, 18.0D, 16.0D), BASE_SHAPE);
      NORTH_SHAPE = VoxelShapes.union(Block.createCuboidShape(0.0D, 10.0D, 1.0D, 16.0D, 14.0D, 5.333333D), Block.createCuboidShape(0.0D, 12.0D, 5.333333D, 16.0D, 16.0D, 9.666667D), Block.createCuboidShape(0.0D, 14.0D, 9.666667D, 16.0D, 18.0D, 14.0D), BASE_SHAPE);
      EAST_SHAPE = VoxelShapes.union(Block.createCuboidShape(15.0D, 10.0D, 0.0D, 10.666667D, 14.0D, 16.0D), Block.createCuboidShape(10.666667D, 12.0D, 0.0D, 6.333333D, 16.0D, 16.0D), Block.createCuboidShape(6.333333D, 14.0D, 0.0D, 2.0D, 18.0D, 16.0D), BASE_SHAPE);
      SOUTH_SHAPE = VoxelShapes.union(Block.createCuboidShape(0.0D, 10.0D, 15.0D, 16.0D, 14.0D, 10.666667D), Block.createCuboidShape(0.0D, 12.0D, 10.666667D, 16.0D, 16.0D, 6.333333D), Block.createCuboidShape(0.0D, 14.0D, 6.333333D, 16.0D, 18.0D, 2.0D), BASE_SHAPE);
   }
}
