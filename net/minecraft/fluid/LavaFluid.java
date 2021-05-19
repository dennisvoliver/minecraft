package net.minecraft.fluid;

import java.util.Random;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.AbstractFireBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FluidBlock;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.tag.FluidTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;
import org.jetbrains.annotations.Nullable;

public abstract class LavaFluid extends FlowableFluid {
   public Fluid getFlowing() {
      return Fluids.FLOWING_LAVA;
   }

   public Fluid getStill() {
      return Fluids.LAVA;
   }

   public Item getBucketItem() {
      return Items.LAVA_BUCKET;
   }

   @Environment(EnvType.CLIENT)
   public void randomDisplayTick(World world, BlockPos pos, FluidState state, Random random) {
      BlockPos blockPos = pos.up();
      if (world.getBlockState(blockPos).isAir() && !world.getBlockState(blockPos).isOpaqueFullCube(world, blockPos)) {
         if (random.nextInt(100) == 0) {
            double d = (double)pos.getX() + random.nextDouble();
            double e = (double)pos.getY() + 1.0D;
            double f = (double)pos.getZ() + random.nextDouble();
            world.addParticle(ParticleTypes.LAVA, d, e, f, 0.0D, 0.0D, 0.0D);
            world.playSound(d, e, f, SoundEvents.BLOCK_LAVA_POP, SoundCategory.BLOCKS, 0.2F + random.nextFloat() * 0.2F, 0.9F + random.nextFloat() * 0.15F, false);
         }

         if (random.nextInt(200) == 0) {
            world.playSound((double)pos.getX(), (double)pos.getY(), (double)pos.getZ(), SoundEvents.BLOCK_LAVA_AMBIENT, SoundCategory.BLOCKS, 0.2F + random.nextFloat() * 0.2F, 0.9F + random.nextFloat() * 0.15F, false);
         }
      }

   }

   public void onRandomTick(World world, BlockPos pos, FluidState state, Random random) {
      if (world.getGameRules().getBoolean(GameRules.DO_FIRE_TICK)) {
         int i = random.nextInt(3);
         if (i > 0) {
            BlockPos blockPos = pos;

            for(int j = 0; j < i; ++j) {
               blockPos = blockPos.add(random.nextInt(3) - 1, 1, random.nextInt(3) - 1);
               if (!world.canSetBlock(blockPos)) {
                  return;
               }

               BlockState blockState = world.getBlockState(blockPos);
               if (blockState.isAir()) {
                  if (this.canLightFire(world, blockPos)) {
                     world.setBlockState(blockPos, AbstractFireBlock.getState(world, blockPos));
                     return;
                  }
               } else if (blockState.getMaterial().blocksMovement()) {
                  return;
               }
            }
         } else {
            for(int k = 0; k < 3; ++k) {
               BlockPos blockPos2 = pos.add(random.nextInt(3) - 1, 0, random.nextInt(3) - 1);
               if (!world.canSetBlock(blockPos2)) {
                  return;
               }

               if (world.isAir(blockPos2.up()) && this.hasBurnableBlock(world, blockPos2)) {
                  world.setBlockState(blockPos2.up(), AbstractFireBlock.getState(world, blockPos2));
               }
            }
         }

      }
   }

   private boolean canLightFire(WorldView world, BlockPos pos) {
      Direction[] var3 = Direction.values();
      int var4 = var3.length;

      for(int var5 = 0; var5 < var4; ++var5) {
         Direction direction = var3[var5];
         if (this.hasBurnableBlock(world, pos.offset(direction))) {
            return true;
         }
      }

      return false;
   }

   private boolean hasBurnableBlock(WorldView world, BlockPos pos) {
      return pos.getY() >= 0 && pos.getY() < 256 && !world.isChunkLoaded(pos) ? false : world.getBlockState(pos).getMaterial().isBurnable();
   }

   @Nullable
   @Environment(EnvType.CLIENT)
   public ParticleEffect getParticle() {
      return ParticleTypes.DRIPPING_LAVA;
   }

   protected void beforeBreakingBlock(WorldAccess world, BlockPos pos, BlockState state) {
      this.playExtinguishEvent(world, pos);
   }

   public int getFlowSpeed(WorldView world) {
      return world.getDimension().isUltrawarm() ? 4 : 2;
   }

   public BlockState toBlockState(FluidState state) {
      return (BlockState)Blocks.LAVA.getDefaultState().with(FluidBlock.LEVEL, method_15741(state));
   }

   public boolean matchesType(Fluid fluid) {
      return fluid == Fluids.LAVA || fluid == Fluids.FLOWING_LAVA;
   }

   public int getLevelDecreasePerBlock(WorldView world) {
      return world.getDimension().isUltrawarm() ? 1 : 2;
   }

   public boolean canBeReplacedWith(FluidState state, BlockView world, BlockPos pos, Fluid fluid, Direction direction) {
      return state.getHeight(world, pos) >= 0.44444445F && fluid.isIn(FluidTags.WATER);
   }

   public int getTickRate(WorldView world) {
      return world.getDimension().isUltrawarm() ? 10 : 30;
   }

   public int getNextTickDelay(World world, BlockPos pos, FluidState oldState, FluidState newState) {
      int i = this.getTickRate(world);
      if (!oldState.isEmpty() && !newState.isEmpty() && !(Boolean)oldState.get(FALLING) && !(Boolean)newState.get(FALLING) && newState.getHeight(world, pos) > oldState.getHeight(world, pos) && world.getRandom().nextInt(4) != 0) {
         i *= 4;
      }

      return i;
   }

   private void playExtinguishEvent(WorldAccess world, BlockPos pos) {
      world.syncWorldEvent(1501, pos, 0);
   }

   protected boolean isInfinite() {
      return false;
   }

   protected void flow(WorldAccess world, BlockPos pos, BlockState state, Direction direction, FluidState fluidState) {
      if (direction == Direction.DOWN) {
         FluidState fluidState2 = world.getFluidState(pos);
         if (this.isIn(FluidTags.LAVA) && fluidState2.isIn(FluidTags.WATER)) {
            if (state.getBlock() instanceof FluidBlock) {
               world.setBlockState(pos, Blocks.STONE.getDefaultState(), 3);
            }

            this.playExtinguishEvent(world, pos);
            return;
         }
      }

      super.flow(world, pos, state, direction, fluidState);
   }

   protected boolean hasRandomTicks() {
      return true;
   }

   protected float getBlastResistance() {
      return 100.0F;
   }

   public static class Flowing extends LavaFluid {
      protected void appendProperties(StateManager.Builder<Fluid, FluidState> builder) {
         super.appendProperties(builder);
         builder.add(LEVEL);
      }

      public int getLevel(FluidState state) {
         return (Integer)state.get(LEVEL);
      }

      public boolean isStill(FluidState state) {
         return false;
      }
   }

   public static class Still extends LavaFluid {
      public int getLevel(FluidState state) {
         return 8;
      }

      public boolean isStill(FluidState state) {
         return true;
      }
   }
}
