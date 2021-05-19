package net.minecraft.block;

import java.util.Random;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.state.StateManager;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

public class EndRodBlock extends FacingBlock {
   protected static final VoxelShape Y_SHAPE = Block.createCuboidShape(6.0D, 0.0D, 6.0D, 10.0D, 16.0D, 10.0D);
   protected static final VoxelShape Z_SHAPE = Block.createCuboidShape(6.0D, 6.0D, 0.0D, 10.0D, 10.0D, 16.0D);
   protected static final VoxelShape X_SHAPE = Block.createCuboidShape(0.0D, 6.0D, 6.0D, 16.0D, 10.0D, 10.0D);

   protected EndRodBlock(AbstractBlock.Settings settings) {
      super(settings);
      this.setDefaultState((BlockState)((BlockState)this.stateManager.getDefaultState()).with(FACING, Direction.UP));
   }

   public BlockState rotate(BlockState state, BlockRotation rotation) {
      return (BlockState)state.with(FACING, rotation.rotate((Direction)state.get(FACING)));
   }

   public BlockState mirror(BlockState state, BlockMirror mirror) {
      return (BlockState)state.with(FACING, mirror.apply((Direction)state.get(FACING)));
   }

   public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
      switch(((Direction)state.get(FACING)).getAxis()) {
      case X:
      default:
         return X_SHAPE;
      case Z:
         return Z_SHAPE;
      case Y:
         return Y_SHAPE;
      }
   }

   public BlockState getPlacementState(ItemPlacementContext ctx) {
      Direction direction = ctx.getSide();
      BlockState blockState = ctx.getWorld().getBlockState(ctx.getBlockPos().offset(direction.getOpposite()));
      return blockState.isOf(this) && blockState.get(FACING) == direction ? (BlockState)this.getDefaultState().with(FACING, direction.getOpposite()) : (BlockState)this.getDefaultState().with(FACING, direction);
   }

   @Environment(EnvType.CLIENT)
   public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
      Direction direction = (Direction)state.get(FACING);
      double d = (double)pos.getX() + 0.55D - (double)(random.nextFloat() * 0.1F);
      double e = (double)pos.getY() + 0.55D - (double)(random.nextFloat() * 0.1F);
      double f = (double)pos.getZ() + 0.55D - (double)(random.nextFloat() * 0.1F);
      double g = (double)(0.4F - (random.nextFloat() + random.nextFloat()) * 0.4F);
      if (random.nextInt(5) == 0) {
         world.addParticle(ParticleTypes.END_ROD, d + (double)direction.getOffsetX() * g, e + (double)direction.getOffsetY() * g, f + (double)direction.getOffsetZ() * g, random.nextGaussian() * 0.005D, random.nextGaussian() * 0.005D, random.nextGaussian() * 0.005D);
      }

   }

   protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
      builder.add(FACING);
   }

   public PistonBehavior getPistonBehavior(BlockState state) {
      return PistonBehavior.NORMAL;
   }

   public boolean canPathfindThrough(BlockState state, BlockView world, BlockPos pos, NavigationType type) {
      return false;
   }
}
