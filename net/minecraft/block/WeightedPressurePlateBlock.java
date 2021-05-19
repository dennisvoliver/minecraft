package net.minecraft.block;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;

public class WeightedPressurePlateBlock extends AbstractPressurePlateBlock {
   public static final IntProperty POWER;
   private final int weight;

   protected WeightedPressurePlateBlock(int weight, AbstractBlock.Settings settings) {
      super(settings);
      this.setDefaultState((BlockState)((BlockState)this.stateManager.getDefaultState()).with(POWER, 0));
      this.weight = weight;
   }

   protected int getRedstoneOutput(World world, BlockPos pos) {
      int i = Math.min(world.getNonSpectatingEntities(Entity.class, BOX.offset(pos)).size(), this.weight);
      if (i > 0) {
         float f = (float)Math.min(this.weight, i) / (float)this.weight;
         return MathHelper.ceil(f * 15.0F);
      } else {
         return 0;
      }
   }

   protected void playPressSound(WorldAccess world, BlockPos pos) {
      world.playSound((PlayerEntity)null, pos, SoundEvents.BLOCK_METAL_PRESSURE_PLATE_CLICK_ON, SoundCategory.BLOCKS, 0.3F, 0.90000004F);
   }

   protected void playDepressSound(WorldAccess world, BlockPos pos) {
      world.playSound((PlayerEntity)null, pos, SoundEvents.BLOCK_METAL_PRESSURE_PLATE_CLICK_OFF, SoundCategory.BLOCKS, 0.3F, 0.75F);
   }

   protected int getRedstoneOutput(BlockState state) {
      return (Integer)state.get(POWER);
   }

   protected BlockState setRedstoneOutput(BlockState state, int rsOut) {
      return (BlockState)state.with(POWER, rsOut);
   }

   protected int getTickRate() {
      return 10;
   }

   protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
      builder.add(POWER);
   }

   static {
      POWER = Properties.POWER;
   }
}
