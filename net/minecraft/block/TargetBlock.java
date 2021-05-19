package net.minecraft.block;

import java.util.Random;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.stat.Stats;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;

public class TargetBlock extends Block {
   private static final IntProperty POWER;

   public TargetBlock(AbstractBlock.Settings settings) {
      super(settings);
      this.setDefaultState((BlockState)((BlockState)this.stateManager.getDefaultState()).with(POWER, 0));
   }

   public void onProjectileHit(World world, BlockState state, BlockHitResult hit, ProjectileEntity projectile) {
      int i = trigger(world, state, hit, projectile);
      Entity entity = projectile.getOwner();
      if (entity instanceof ServerPlayerEntity) {
         ServerPlayerEntity serverPlayerEntity = (ServerPlayerEntity)entity;
         serverPlayerEntity.incrementStat(Stats.TARGET_HIT);
         Criteria.TARGET_HIT.trigger(serverPlayerEntity, projectile, hit.getPos(), i);
      }

   }

   private static int trigger(WorldAccess world, BlockState state, BlockHitResult blockHitResult, Entity entity) {
      int i = calculatePower(blockHitResult, blockHitResult.getPos());
      int j = entity instanceof PersistentProjectileEntity ? 20 : 8;
      if (!world.getBlockTickScheduler().isScheduled(blockHitResult.getBlockPos(), state.getBlock())) {
         setPower(world, state, i, blockHitResult.getBlockPos(), j);
      }

      return i;
   }

   private static int calculatePower(BlockHitResult blockHitResult, Vec3d pos) {
      Direction direction = blockHitResult.getSide();
      double d = Math.abs(MathHelper.fractionalPart(pos.x) - 0.5D);
      double e = Math.abs(MathHelper.fractionalPart(pos.y) - 0.5D);
      double f = Math.abs(MathHelper.fractionalPart(pos.z) - 0.5D);
      Direction.Axis axis = direction.getAxis();
      double i;
      if (axis == Direction.Axis.Y) {
         i = Math.max(d, f);
      } else if (axis == Direction.Axis.Z) {
         i = Math.max(d, e);
      } else {
         i = Math.max(e, f);
      }

      return Math.max(1, MathHelper.ceil(15.0D * MathHelper.clamp((0.5D - i) / 0.5D, 0.0D, 1.0D)));
   }

   private static void setPower(WorldAccess world, BlockState state, int power, BlockPos pos, int delay) {
      world.setBlockState(pos, (BlockState)state.with(POWER, power), 3);
      world.getBlockTickScheduler().schedule(pos, state.getBlock(), delay);
   }

   public void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
      if ((Integer)state.get(POWER) != 0) {
         world.setBlockState(pos, (BlockState)state.with(POWER, 0), 3);
      }

   }

   public int getWeakRedstonePower(BlockState state, BlockView world, BlockPos pos, Direction direction) {
      return (Integer)state.get(POWER);
   }

   public boolean emitsRedstonePower(BlockState state) {
      return true;
   }

   protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
      builder.add(POWER);
   }

   public void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState oldState, boolean notify) {
      if (!world.isClient() && !state.isOf(oldState.getBlock())) {
         if ((Integer)state.get(POWER) > 0 && !world.getBlockTickScheduler().isScheduled(pos, this)) {
            world.setBlockState(pos, (BlockState)state.with(POWER, 0), 18);
         }

      }
   }

   static {
      POWER = Properties.POWER;
   }
}
