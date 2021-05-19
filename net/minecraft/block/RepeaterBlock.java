package net.minecraft.block;

import java.util.Random;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;

public class RepeaterBlock extends AbstractRedstoneGateBlock {
   public static final BooleanProperty LOCKED;
   public static final IntProperty DELAY;

   protected RepeaterBlock(AbstractBlock.Settings settings) {
      super(settings);
      this.setDefaultState((BlockState)((BlockState)((BlockState)((BlockState)((BlockState)this.stateManager.getDefaultState()).with(FACING, Direction.NORTH)).with(DELAY, 1)).with(LOCKED, false)).with(POWERED, false));
   }

   public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
      if (!player.abilities.allowModifyWorld) {
         return ActionResult.PASS;
      } else {
         world.setBlockState(pos, (BlockState)state.cycle(DELAY), 3);
         return ActionResult.success(world.isClient);
      }
   }

   protected int getUpdateDelayInternal(BlockState state) {
      return (Integer)state.get(DELAY) * 2;
   }

   public BlockState getPlacementState(ItemPlacementContext ctx) {
      BlockState blockState = super.getPlacementState(ctx);
      return (BlockState)blockState.with(LOCKED, this.isLocked(ctx.getWorld(), ctx.getBlockPos(), blockState));
   }

   public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState newState, WorldAccess world, BlockPos pos, BlockPos posFrom) {
      return !world.isClient() && direction.getAxis() != ((Direction)state.get(FACING)).getAxis() ? (BlockState)state.with(LOCKED, this.isLocked(world, pos, state)) : super.getStateForNeighborUpdate(state, direction, newState, world, pos, posFrom);
   }

   public boolean isLocked(WorldView worldView, BlockPos pos, BlockState state) {
      return this.getMaxInputLevelSides(worldView, pos, state) > 0;
   }

   protected boolean isValidInput(BlockState state) {
      return isRedstoneGate(state);
   }

   @Environment(EnvType.CLIENT)
   public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
      if ((Boolean)state.get(POWERED)) {
         Direction direction = (Direction)state.get(FACING);
         double d = (double)pos.getX() + 0.5D + (random.nextDouble() - 0.5D) * 0.2D;
         double e = (double)pos.getY() + 0.4D + (random.nextDouble() - 0.5D) * 0.2D;
         double f = (double)pos.getZ() + 0.5D + (random.nextDouble() - 0.5D) * 0.2D;
         float g = -5.0F;
         if (random.nextBoolean()) {
            g = (float)((Integer)state.get(DELAY) * 2 - 1);
         }

         g /= 16.0F;
         double h = (double)(g * (float)direction.getOffsetX());
         double i = (double)(g * (float)direction.getOffsetZ());
         world.addParticle(DustParticleEffect.RED, d + h, e, f + i, 0.0D, 0.0D, 0.0D);
      }
   }

   protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
      builder.add(FACING, DELAY, LOCKED, POWERED);
   }

   static {
      LOCKED = Properties.LOCKED;
      DELAY = Properties.DELAY;
   }
}
