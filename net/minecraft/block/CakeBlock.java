package net.minecraft.block;

import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.stat.Stats;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;

public class CakeBlock extends Block {
   public static final IntProperty BITES;
   protected static final VoxelShape[] BITES_TO_SHAPE;

   protected CakeBlock(AbstractBlock.Settings settings) {
      super(settings);
      this.setDefaultState((BlockState)((BlockState)this.stateManager.getDefaultState()).with(BITES, 0));
   }

   public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
      return BITES_TO_SHAPE[(Integer)state.get(BITES)];
   }

   public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
      if (world.isClient) {
         ItemStack itemStack = player.getStackInHand(hand);
         if (this.tryEat(world, pos, state, player).isAccepted()) {
            return ActionResult.SUCCESS;
         }

         if (itemStack.isEmpty()) {
            return ActionResult.CONSUME;
         }
      }

      return this.tryEat(world, pos, state, player);
   }

   private ActionResult tryEat(WorldAccess world, BlockPos pos, BlockState state, PlayerEntity player) {
      if (!player.canConsume(false)) {
         return ActionResult.PASS;
      } else {
         player.incrementStat(Stats.EAT_CAKE_SLICE);
         player.getHungerManager().add(2, 0.1F);
         int i = (Integer)state.get(BITES);
         if (i < 6) {
            world.setBlockState(pos, (BlockState)state.with(BITES, i + 1), 3);
         } else {
            world.removeBlock(pos, false);
         }

         return ActionResult.SUCCESS;
      }
   }

   public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState newState, WorldAccess world, BlockPos pos, BlockPos posFrom) {
      return direction == Direction.DOWN && !state.canPlaceAt(world, pos) ? Blocks.AIR.getDefaultState() : super.getStateForNeighborUpdate(state, direction, newState, world, pos, posFrom);
   }

   public boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
      return world.getBlockState(pos.down()).getMaterial().isSolid();
   }

   protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
      builder.add(BITES);
   }

   public int getComparatorOutput(BlockState state, World world, BlockPos pos) {
      return (7 - (Integer)state.get(BITES)) * 2;
   }

   public boolean hasComparatorOutput(BlockState state) {
      return true;
   }

   public boolean canPathfindThrough(BlockState state, BlockView world, BlockPos pos, NavigationType type) {
      return false;
   }

   static {
      BITES = Properties.BITES;
      BITES_TO_SHAPE = new VoxelShape[]{Block.createCuboidShape(1.0D, 0.0D, 1.0D, 15.0D, 8.0D, 15.0D), Block.createCuboidShape(3.0D, 0.0D, 1.0D, 15.0D, 8.0D, 15.0D), Block.createCuboidShape(5.0D, 0.0D, 1.0D, 15.0D, 8.0D, 15.0D), Block.createCuboidShape(7.0D, 0.0D, 1.0D, 15.0D, 8.0D, 15.0D), Block.createCuboidShape(9.0D, 0.0D, 1.0D, 15.0D, 8.0D, 15.0D), Block.createCuboidShape(11.0D, 0.0D, 1.0D, 15.0D, 8.0D, 15.0D), Block.createCuboidShape(13.0D, 0.0D, 1.0D, 15.0D, 8.0D, 15.0D)};
   }
}
