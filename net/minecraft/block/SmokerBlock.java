package net.minecraft.block;

import java.util.Random;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.SmokerBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

public class SmokerBlock extends AbstractFurnaceBlock {
   protected SmokerBlock(AbstractBlock.Settings settings) {
      super(settings);
   }

   public BlockEntity createBlockEntity(BlockView world) {
      return new SmokerBlockEntity();
   }

   protected void openScreen(World world, BlockPos pos, PlayerEntity player) {
      BlockEntity blockEntity = world.getBlockEntity(pos);
      if (blockEntity instanceof SmokerBlockEntity) {
         player.openHandledScreen((NamedScreenHandlerFactory)blockEntity);
         player.incrementStat(Stats.INTERACT_WITH_SMOKER);
      }

   }

   @Environment(EnvType.CLIENT)
   public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
      if ((Boolean)state.get(LIT)) {
         double d = (double)pos.getX() + 0.5D;
         double e = (double)pos.getY();
         double f = (double)pos.getZ() + 0.5D;
         if (random.nextDouble() < 0.1D) {
            world.playSound(d, e, f, SoundEvents.BLOCK_SMOKER_SMOKE, SoundCategory.BLOCKS, 1.0F, 1.0F, false);
         }

         world.addParticle(ParticleTypes.SMOKE, d, e + 1.1D, f, 0.0D, 0.0D, 0.0D);
      }
   }
}
