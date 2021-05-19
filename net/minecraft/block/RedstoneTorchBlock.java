package net.minecraft.block;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.WeakHashMap;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

public class RedstoneTorchBlock extends TorchBlock {
   public static final BooleanProperty LIT;
   private static final Map<BlockView, List<RedstoneTorchBlock.BurnoutEntry>> BURNOUT_MAP;

   protected RedstoneTorchBlock(AbstractBlock.Settings settings) {
      super(settings, DustParticleEffect.RED);
      this.setDefaultState((BlockState)((BlockState)this.stateManager.getDefaultState()).with(LIT, true));
   }

   public void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState oldState, boolean notify) {
      Direction[] var6 = Direction.values();
      int var7 = var6.length;

      for(int var8 = 0; var8 < var7; ++var8) {
         Direction direction = var6[var8];
         world.updateNeighborsAlways(pos.offset(direction), this);
      }

   }

   public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
      if (!moved) {
         Direction[] var6 = Direction.values();
         int var7 = var6.length;

         for(int var8 = 0; var8 < var7; ++var8) {
            Direction direction = var6[var8];
            world.updateNeighborsAlways(pos.offset(direction), this);
         }

      }
   }

   public int getWeakRedstonePower(BlockState state, BlockView world, BlockPos pos, Direction direction) {
      return (Boolean)state.get(LIT) && Direction.UP != direction ? 15 : 0;
   }

   protected boolean shouldUnpower(World world, BlockPos pos, BlockState state) {
      return world.isEmittingRedstonePower(pos.down(), Direction.DOWN);
   }

   public void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
      boolean bl = this.shouldUnpower(world, pos, state);
      List list = (List)BURNOUT_MAP.get(world);

      while(list != null && !list.isEmpty() && world.getTime() - ((RedstoneTorchBlock.BurnoutEntry)list.get(0)).time > 60L) {
         list.remove(0);
      }

      if ((Boolean)state.get(LIT)) {
         if (bl) {
            world.setBlockState(pos, (BlockState)state.with(LIT, false), 3);
            if (isBurnedOut(world, pos, true)) {
               world.syncWorldEvent(1502, pos, 0);
               world.getBlockTickScheduler().schedule(pos, world.getBlockState(pos).getBlock(), 160);
            }
         }
      } else if (!bl && !isBurnedOut(world, pos, false)) {
         world.setBlockState(pos, (BlockState)state.with(LIT, true), 3);
      }

   }

   public void neighborUpdate(BlockState state, World world, BlockPos pos, Block block, BlockPos fromPos, boolean notify) {
      if ((Boolean)state.get(LIT) == this.shouldUnpower(world, pos, state) && !world.getBlockTickScheduler().isTicking(pos, this)) {
         world.getBlockTickScheduler().schedule(pos, this, 2);
      }

   }

   public int getStrongRedstonePower(BlockState state, BlockView world, BlockPos pos, Direction direction) {
      return direction == Direction.DOWN ? state.getWeakRedstonePower(world, pos, direction) : 0;
   }

   public boolean emitsRedstonePower(BlockState state) {
      return true;
   }

   @Environment(EnvType.CLIENT)
   public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
      if ((Boolean)state.get(LIT)) {
         double d = (double)pos.getX() + 0.5D + (random.nextDouble() - 0.5D) * 0.2D;
         double e = (double)pos.getY() + 0.7D + (random.nextDouble() - 0.5D) * 0.2D;
         double f = (double)pos.getZ() + 0.5D + (random.nextDouble() - 0.5D) * 0.2D;
         world.addParticle(this.particle, d, e, f, 0.0D, 0.0D, 0.0D);
      }
   }

   protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
      builder.add(LIT);
   }

   private static boolean isBurnedOut(World world, BlockPos pos, boolean addNew) {
      List<RedstoneTorchBlock.BurnoutEntry> list = (List)BURNOUT_MAP.computeIfAbsent(world, (blockView) -> {
         return Lists.newArrayList();
      });
      if (addNew) {
         list.add(new RedstoneTorchBlock.BurnoutEntry(pos.toImmutable(), world.getTime()));
      }

      int i = 0;

      for(int j = 0; j < list.size(); ++j) {
         RedstoneTorchBlock.BurnoutEntry burnoutEntry = (RedstoneTorchBlock.BurnoutEntry)list.get(j);
         if (burnoutEntry.pos.equals(pos)) {
            ++i;
            if (i >= 8) {
               return true;
            }
         }
      }

      return false;
   }

   static {
      LIT = Properties.LIT;
      BURNOUT_MAP = new WeakHashMap();
   }

   public static class BurnoutEntry {
      private final BlockPos pos;
      private final long time;

      public BurnoutEntry(BlockPos pos, long time) {
         this.pos = pos;
         this.time = time;
      }
   }
}
