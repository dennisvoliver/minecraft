package net.minecraft.block;

import java.util.Iterator;
import java.util.Random;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.tag.FluidTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;

public class CactusBlock extends Block {
   public static final IntProperty AGE;
   protected static final VoxelShape COLLISION_SHAPE;
   protected static final VoxelShape OUTLINE_SHAPE;

   protected CactusBlock(AbstractBlock.Settings settings) {
      super(settings);
      this.setDefaultState((BlockState)((BlockState)this.stateManager.getDefaultState()).with(AGE, 0));
   }

   public void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
      if (!state.canPlaceAt(world, pos)) {
         world.breakBlock(pos, true);
      }

   }

   public void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
      BlockPos blockPos = pos.up();
      if (world.isAir(blockPos)) {
         int i;
         for(i = 1; world.getBlockState(pos.down(i)).isOf(this); ++i) {
         }

         if (i < 3) {
            int j = (Integer)state.get(AGE);
            if (j == 15) {
               world.setBlockState(blockPos, this.getDefaultState());
               BlockState blockState = (BlockState)state.with(AGE, 0);
               world.setBlockState(pos, blockState, 4);
               blockState.neighborUpdate(world, blockPos, this, pos, false);
            } else {
               world.setBlockState(pos, (BlockState)state.with(AGE, j + 1), 4);
            }

         }
      }
   }

   public VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
      return COLLISION_SHAPE;
   }

   public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
      return OUTLINE_SHAPE;
   }

   public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState newState, WorldAccess world, BlockPos pos, BlockPos posFrom) {
      if (!state.canPlaceAt(world, pos)) {
         world.getBlockTickScheduler().schedule(pos, this, 1);
      }

      return super.getStateForNeighborUpdate(state, direction, newState, world, pos, posFrom);
   }

   public boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
      Iterator var4 = Direction.Type.HORIZONTAL.iterator();

      Direction direction;
      Material material;
      do {
         if (!var4.hasNext()) {
            BlockState blockState2 = world.getBlockState(pos.down());
            return (blockState2.isOf(Blocks.CACTUS) || blockState2.isOf(Blocks.SAND) || blockState2.isOf(Blocks.RED_SAND)) && !world.getBlockState(pos.up()).getMaterial().isLiquid();
         }

         direction = (Direction)var4.next();
         BlockState blockState = world.getBlockState(pos.offset(direction));
         material = blockState.getMaterial();
      } while(!material.isSolid() && !world.getFluidState(pos.offset(direction)).isIn(FluidTags.LAVA));

      return false;
   }

   public void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity) {
      entity.damage(DamageSource.CACTUS, 1.0F);
   }

   protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
      builder.add(AGE);
   }

   public boolean canPathfindThrough(BlockState state, BlockView world, BlockPos pos, NavigationType type) {
      return false;
   }

   static {
      AGE = Properties.AGE_15;
      COLLISION_SHAPE = Block.createCuboidShape(1.0D, 0.0D, 1.0D, 15.0D, 15.0D, 15.0D);
      OUTLINE_SHAPE = Block.createCuboidShape(1.0D, 0.0D, 1.0D, 15.0D, 16.0D, 15.0D);
   }
}
