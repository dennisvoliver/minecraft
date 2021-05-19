package net.minecraft.block;

import java.util.Random;
import net.minecraft.fluid.Fluids;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;

public abstract class AbstractPlantStemBlock extends AbstractPlantPartBlock implements Fertilizable {
   public static final IntProperty AGE;
   private final double growthChance;

   protected AbstractPlantStemBlock(AbstractBlock.Settings settings, Direction growthDirection, VoxelShape outlineShape, boolean tickWater, double growthChance) {
      super(settings, growthDirection, outlineShape, tickWater);
      this.growthChance = growthChance;
      this.setDefaultState((BlockState)((BlockState)this.stateManager.getDefaultState()).with(AGE, 0));
   }

   public BlockState getRandomGrowthState(WorldAccess worldAccess) {
      return (BlockState)this.getDefaultState().with(AGE, worldAccess.getRandom().nextInt(25));
   }

   public boolean hasRandomTicks(BlockState state) {
      return (Integer)state.get(AGE) < 25;
   }

   public void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
      if ((Integer)state.get(AGE) < 25 && random.nextDouble() < this.growthChance) {
         BlockPos blockPos = pos.offset(this.growthDirection);
         if (this.chooseStemState(world.getBlockState(blockPos))) {
            world.setBlockState(blockPos, (BlockState)state.cycle(AGE));
         }
      }

   }

   public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState newState, WorldAccess world, BlockPos pos, BlockPos posFrom) {
      if (direction == this.growthDirection.getOpposite() && !state.canPlaceAt(world, pos)) {
         world.getBlockTickScheduler().schedule(pos, this, 1);
      }

      if (direction != this.growthDirection || !newState.isOf(this) && !newState.isOf(this.getPlant())) {
         if (this.tickWater) {
            world.getFluidTickScheduler().schedule(pos, Fluids.WATER, Fluids.WATER.getTickRate(world));
         }

         return super.getStateForNeighborUpdate(state, direction, newState, world, pos, posFrom);
      } else {
         return this.getPlant().getDefaultState();
      }
   }

   protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
      builder.add(AGE);
   }

   public boolean isFertilizable(BlockView world, BlockPos pos, BlockState state, boolean isClient) {
      return this.chooseStemState(world.getBlockState(pos.offset(this.growthDirection)));
   }

   public boolean canGrow(World world, Random random, BlockPos pos, BlockState state) {
      return true;
   }

   public void grow(ServerWorld world, Random random, BlockPos pos, BlockState state) {
      BlockPos blockPos = pos.offset(this.growthDirection);
      int i = Math.min((Integer)state.get(AGE) + 1, 25);
      int j = this.method_26376(random);

      for(int k = 0; k < j && this.chooseStemState(world.getBlockState(blockPos)); ++k) {
         world.setBlockState(blockPos, (BlockState)state.with(AGE, i));
         blockPos = blockPos.offset(this.growthDirection);
         i = Math.min(i + 1, 25);
      }

   }

   protected abstract int method_26376(Random random);

   protected abstract boolean chooseStemState(BlockState state);

   protected AbstractPlantStemBlock getStem() {
      return this;
   }

   static {
      AGE = Properties.AGE_25;
   }
}
