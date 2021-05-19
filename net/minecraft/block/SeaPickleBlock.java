package net.minecraft.block;

import java.util.Random;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.tag.BlockTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;
import org.jetbrains.annotations.Nullable;

public class SeaPickleBlock extends PlantBlock implements Fertilizable, Waterloggable {
   public static final IntProperty PICKLES;
   public static final BooleanProperty WATERLOGGED;
   protected static final VoxelShape ONE_PICKLE_SHAPE;
   protected static final VoxelShape TWO_PICKLES_SHAPE;
   protected static final VoxelShape THREE_PICKLES_SHAPE;
   protected static final VoxelShape FOUR_PICKLES_SHAPE;

   protected SeaPickleBlock(AbstractBlock.Settings settings) {
      super(settings);
      this.setDefaultState((BlockState)((BlockState)((BlockState)this.stateManager.getDefaultState()).with(PICKLES, 1)).with(WATERLOGGED, true));
   }

   @Nullable
   public BlockState getPlacementState(ItemPlacementContext ctx) {
      BlockState blockState = ctx.getWorld().getBlockState(ctx.getBlockPos());
      if (blockState.isOf(this)) {
         return (BlockState)blockState.with(PICKLES, Math.min(4, (Integer)blockState.get(PICKLES) + 1));
      } else {
         FluidState fluidState = ctx.getWorld().getFluidState(ctx.getBlockPos());
         boolean bl = fluidState.getFluid() == Fluids.WATER;
         return (BlockState)super.getPlacementState(ctx).with(WATERLOGGED, bl);
      }
   }

   public static boolean isDry(BlockState blockState) {
      return !(Boolean)blockState.get(WATERLOGGED);
   }

   protected boolean canPlantOnTop(BlockState floor, BlockView world, BlockPos pos) {
      return !floor.getCollisionShape(world, pos).getFace(Direction.UP).isEmpty() || floor.isSideSolidFullSquare(world, pos, Direction.UP);
   }

   public boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
      BlockPos blockPos = pos.down();
      return this.canPlantOnTop(world.getBlockState(blockPos), world, blockPos);
   }

   public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState newState, WorldAccess world, BlockPos pos, BlockPos posFrom) {
      if (!state.canPlaceAt(world, pos)) {
         return Blocks.AIR.getDefaultState();
      } else {
         if ((Boolean)state.get(WATERLOGGED)) {
            world.getFluidTickScheduler().schedule(pos, Fluids.WATER, Fluids.WATER.getTickRate(world));
         }

         return super.getStateForNeighborUpdate(state, direction, newState, world, pos, posFrom);
      }
   }

   public boolean canReplace(BlockState state, ItemPlacementContext context) {
      return context.getStack().getItem() == this.asItem() && (Integer)state.get(PICKLES) < 4 ? true : super.canReplace(state, context);
   }

   public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
      switch((Integer)state.get(PICKLES)) {
      case 1:
      default:
         return ONE_PICKLE_SHAPE;
      case 2:
         return TWO_PICKLES_SHAPE;
      case 3:
         return THREE_PICKLES_SHAPE;
      case 4:
         return FOUR_PICKLES_SHAPE;
      }
   }

   public FluidState getFluidState(BlockState state) {
      return (Boolean)state.get(WATERLOGGED) ? Fluids.WATER.getStill(false) : super.getFluidState(state);
   }

   protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
      builder.add(PICKLES, WATERLOGGED);
   }

   public boolean isFertilizable(BlockView world, BlockPos pos, BlockState state, boolean isClient) {
      return true;
   }

   public boolean canGrow(World world, Random random, BlockPos pos, BlockState state) {
      return true;
   }

   public void grow(ServerWorld world, Random random, BlockPos pos, BlockState state) {
      if (!isDry(state) && world.getBlockState(pos.down()).isIn(BlockTags.CORAL_BLOCKS)) {
         int i = true;
         int j = 1;
         int k = true;
         int l = 0;
         int m = pos.getX() - 2;
         int n = 0;

         for(int o = 0; o < 5; ++o) {
            for(int p = 0; p < j; ++p) {
               int q = 2 + pos.getY() - 1;

               for(int r = q - 2; r < q; ++r) {
                  BlockPos blockPos = new BlockPos(m + o, r, pos.getZ() - n + p);
                  if (blockPos != pos && random.nextInt(6) == 0 && world.getBlockState(blockPos).isOf(Blocks.WATER)) {
                     BlockState blockState = world.getBlockState(blockPos.down());
                     if (blockState.isIn(BlockTags.CORAL_BLOCKS)) {
                        world.setBlockState(blockPos, (BlockState)Blocks.SEA_PICKLE.getDefaultState().with(PICKLES, random.nextInt(4) + 1), 3);
                     }
                  }
               }
            }

            if (l < 2) {
               j += 2;
               ++n;
            } else {
               j -= 2;
               --n;
            }

            ++l;
         }

         world.setBlockState(pos, (BlockState)state.with(PICKLES, 4), 2);
      }

   }

   public boolean canPathfindThrough(BlockState state, BlockView world, BlockPos pos, NavigationType type) {
      return false;
   }

   static {
      PICKLES = Properties.PICKLES;
      WATERLOGGED = Properties.WATERLOGGED;
      ONE_PICKLE_SHAPE = Block.createCuboidShape(6.0D, 0.0D, 6.0D, 10.0D, 6.0D, 10.0D);
      TWO_PICKLES_SHAPE = Block.createCuboidShape(3.0D, 0.0D, 3.0D, 13.0D, 6.0D, 13.0D);
      THREE_PICKLES_SHAPE = Block.createCuboidShape(2.0D, 0.0D, 2.0D, 14.0D, 6.0D, 14.0D);
      FOUR_PICKLES_SHAPE = Block.createCuboidShape(2.0D, 0.0D, 2.0D, 14.0D, 7.0D, 14.0D);
   }
}
