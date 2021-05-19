package net.minecraft.block;

import net.minecraft.block.enums.Instrument;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundCategory;
import net.minecraft.stat.Stats;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;

public class NoteBlock extends Block {
   public static final EnumProperty<Instrument> INSTRUMENT;
   public static final BooleanProperty POWERED;
   public static final IntProperty NOTE;

   public NoteBlock(AbstractBlock.Settings settings) {
      super(settings);
      this.setDefaultState((BlockState)((BlockState)((BlockState)((BlockState)this.stateManager.getDefaultState()).with(INSTRUMENT, Instrument.HARP)).with(NOTE, 0)).with(POWERED, false));
   }

   public BlockState getPlacementState(ItemPlacementContext ctx) {
      return (BlockState)this.getDefaultState().with(INSTRUMENT, Instrument.fromBlockState(ctx.getWorld().getBlockState(ctx.getBlockPos().down())));
   }

   public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState newState, WorldAccess world, BlockPos pos, BlockPos posFrom) {
      return direction == Direction.DOWN ? (BlockState)state.with(INSTRUMENT, Instrument.fromBlockState(newState)) : super.getStateForNeighborUpdate(state, direction, newState, world, pos, posFrom);
   }

   public void neighborUpdate(BlockState state, World world, BlockPos pos, Block block, BlockPos fromPos, boolean notify) {
      boolean bl = world.isReceivingRedstonePower(pos);
      if (bl != (Boolean)state.get(POWERED)) {
         if (bl) {
            this.playNote(world, pos);
         }

         world.setBlockState(pos, (BlockState)state.with(POWERED, bl), 3);
      }

   }

   private void playNote(World world, BlockPos pos) {
      if (world.getBlockState(pos.up()).isAir()) {
         world.addSyncedBlockEvent(pos, this, 0, 0);
      }

   }

   public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
      if (world.isClient) {
         return ActionResult.SUCCESS;
      } else {
         state = (BlockState)state.cycle(NOTE);
         world.setBlockState(pos, state, 3);
         this.playNote(world, pos);
         player.incrementStat(Stats.TUNE_NOTEBLOCK);
         return ActionResult.CONSUME;
      }
   }

   public void onBlockBreakStart(BlockState state, World world, BlockPos pos, PlayerEntity player) {
      if (!world.isClient) {
         this.playNote(world, pos);
         player.incrementStat(Stats.PLAY_NOTEBLOCK);
      }
   }

   public boolean onSyncedBlockEvent(BlockState state, World world, BlockPos pos, int type, int data) {
      int i = (Integer)state.get(NOTE);
      float f = (float)Math.pow(2.0D, (double)(i - 12) / 12.0D);
      world.playSound((PlayerEntity)null, pos, ((Instrument)state.get(INSTRUMENT)).getSound(), SoundCategory.RECORDS, 3.0F, f);
      world.addParticle(ParticleTypes.NOTE, (double)pos.getX() + 0.5D, (double)pos.getY() + 1.2D, (double)pos.getZ() + 0.5D, (double)i / 24.0D, 0.0D, 0.0D);
      return true;
   }

   protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
      builder.add(INSTRUMENT, POWERED, NOTE);
   }

   static {
      INSTRUMENT = Properties.INSTRUMENT;
      POWERED = Properties.POWERED;
      NOTE = Properties.NOTE;
   }
}
