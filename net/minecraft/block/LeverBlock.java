package net.minecraft.block;

import java.util.Random;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.enums.WallMountLocation;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
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

public class LeverBlock extends WallMountedBlock {
   public static final BooleanProperty POWERED;
   protected static final VoxelShape NORTH_WALL_SHAPE;
   protected static final VoxelShape SOUTH_WALL_SHAPE;
   protected static final VoxelShape WEST_WALL_SHAPE;
   protected static final VoxelShape EAST_WALL_SHAPE;
   protected static final VoxelShape FLOOR_Z_AXIS_SHAPE;
   protected static final VoxelShape FLOOR_X_AXIS_SHAPE;
   protected static final VoxelShape CEILING_Z_AXIS_SHAPE;
   protected static final VoxelShape CEILING_X_AXIS_SHAPE;

   protected LeverBlock(AbstractBlock.Settings settings) {
      super(settings);
      this.setDefaultState((BlockState)((BlockState)((BlockState)((BlockState)this.stateManager.getDefaultState()).with(FACING, Direction.NORTH)).with(POWERED, false)).with(FACE, WallMountLocation.WALL));
   }

   public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
      switch((WallMountLocation)state.get(FACE)) {
      case FLOOR:
         switch(((Direction)state.get(FACING)).getAxis()) {
         case X:
            return FLOOR_X_AXIS_SHAPE;
         case Z:
         default:
            return FLOOR_Z_AXIS_SHAPE;
         }
      case WALL:
         switch((Direction)state.get(FACING)) {
         case EAST:
            return EAST_WALL_SHAPE;
         case WEST:
            return WEST_WALL_SHAPE;
         case SOUTH:
            return SOUTH_WALL_SHAPE;
         case NORTH:
         default:
            return NORTH_WALL_SHAPE;
         }
      case CEILING:
      default:
         switch(((Direction)state.get(FACING)).getAxis()) {
         case X:
            return CEILING_X_AXIS_SHAPE;
         case Z:
         default:
            return CEILING_Z_AXIS_SHAPE;
         }
      }
   }

   public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
      BlockState blockState;
      if (world.isClient) {
         blockState = (BlockState)state.cycle(POWERED);
         if ((Boolean)blockState.get(POWERED)) {
            spawnParticles(blockState, world, pos, 1.0F);
         }

         return ActionResult.SUCCESS;
      } else {
         blockState = this.method_21846(state, world, pos);
         float f = (Boolean)blockState.get(POWERED) ? 0.6F : 0.5F;
         world.playSound((PlayerEntity)null, pos, SoundEvents.BLOCK_LEVER_CLICK, SoundCategory.BLOCKS, 0.3F, f);
         return ActionResult.CONSUME;
      }
   }

   public BlockState method_21846(BlockState blockState, World world, BlockPos blockPos) {
      blockState = (BlockState)blockState.cycle(POWERED);
      world.setBlockState(blockPos, blockState, 3);
      this.updateNeighbors(blockState, world, blockPos);
      return blockState;
   }

   private static void spawnParticles(BlockState state, WorldAccess world, BlockPos pos, float alpha) {
      Direction direction = ((Direction)state.get(FACING)).getOpposite();
      Direction direction2 = getDirection(state).getOpposite();
      double d = (double)pos.getX() + 0.5D + 0.1D * (double)direction.getOffsetX() + 0.2D * (double)direction2.getOffsetX();
      double e = (double)pos.getY() + 0.5D + 0.1D * (double)direction.getOffsetY() + 0.2D * (double)direction2.getOffsetY();
      double f = (double)pos.getZ() + 0.5D + 0.1D * (double)direction.getOffsetZ() + 0.2D * (double)direction2.getOffsetZ();
      world.addParticle(new DustParticleEffect(1.0F, 0.0F, 0.0F, alpha), d, e, f, 0.0D, 0.0D, 0.0D);
   }

   @Environment(EnvType.CLIENT)
   public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
      if ((Boolean)state.get(POWERED) && random.nextFloat() < 0.25F) {
         spawnParticles(state, world, pos, 0.5F);
      }

   }

   public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
      if (!moved && !state.isOf(newState.getBlock())) {
         if ((Boolean)state.get(POWERED)) {
            this.updateNeighbors(state, world, pos);
         }

         super.onStateReplaced(state, world, pos, newState, moved);
      }
   }

   public int getWeakRedstonePower(BlockState state, BlockView world, BlockPos pos, Direction direction) {
      return (Boolean)state.get(POWERED) ? 15 : 0;
   }

   public int getStrongRedstonePower(BlockState state, BlockView world, BlockPos pos, Direction direction) {
      return (Boolean)state.get(POWERED) && getDirection(state) == direction ? 15 : 0;
   }

   public boolean emitsRedstonePower(BlockState state) {
      return true;
   }

   private void updateNeighbors(BlockState state, World world, BlockPos pos) {
      world.updateNeighborsAlways(pos, this);
      world.updateNeighborsAlways(pos.offset(getDirection(state).getOpposite()), this);
   }

   protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
      builder.add(FACE, FACING, POWERED);
   }

   static {
      POWERED = Properties.POWERED;
      NORTH_WALL_SHAPE = Block.createCuboidShape(5.0D, 4.0D, 10.0D, 11.0D, 12.0D, 16.0D);
      SOUTH_WALL_SHAPE = Block.createCuboidShape(5.0D, 4.0D, 0.0D, 11.0D, 12.0D, 6.0D);
      WEST_WALL_SHAPE = Block.createCuboidShape(10.0D, 4.0D, 5.0D, 16.0D, 12.0D, 11.0D);
      EAST_WALL_SHAPE = Block.createCuboidShape(0.0D, 4.0D, 5.0D, 6.0D, 12.0D, 11.0D);
      FLOOR_Z_AXIS_SHAPE = Block.createCuboidShape(5.0D, 0.0D, 4.0D, 11.0D, 6.0D, 12.0D);
      FLOOR_X_AXIS_SHAPE = Block.createCuboidShape(4.0D, 0.0D, 5.0D, 12.0D, 6.0D, 11.0D);
      CEILING_Z_AXIS_SHAPE = Block.createCuboidShape(5.0D, 10.0D, 4.0D, 11.0D, 16.0D, 12.0D);
      CEILING_X_AXIS_SHAPE = Block.createCuboidShape(4.0D, 10.0D, 5.0D, 12.0D, 16.0D, 11.0D);
   }
}
