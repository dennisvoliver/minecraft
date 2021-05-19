package net.minecraft.block;

import java.util.Random;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.entity.passive.BatEntity;
import net.minecraft.entity.passive.TurtleEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.tag.BlockTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class TurtleEggBlock extends Block {
   private static final VoxelShape SMALL_SHAPE = Block.createCuboidShape(3.0D, 0.0D, 3.0D, 12.0D, 7.0D, 12.0D);
   private static final VoxelShape LARGE_SHAPE = Block.createCuboidShape(1.0D, 0.0D, 1.0D, 15.0D, 7.0D, 15.0D);
   public static final IntProperty HATCH;
   public static final IntProperty EGGS;

   public TurtleEggBlock(AbstractBlock.Settings settings) {
      super(settings);
      this.setDefaultState((BlockState)((BlockState)((BlockState)this.stateManager.getDefaultState()).with(HATCH, 0)).with(EGGS, 1));
   }

   public void onSteppedOn(World world, BlockPos pos, Entity entity) {
      this.tryBreakEgg(world, pos, entity, 100);
      super.onSteppedOn(world, pos, entity);
   }

   public void onLandedUpon(World world, BlockPos pos, Entity entity, float distance) {
      if (!(entity instanceof ZombieEntity)) {
         this.tryBreakEgg(world, pos, entity, 3);
      }

      super.onLandedUpon(world, pos, entity, distance);
   }

   private void tryBreakEgg(World world, BlockPos blockPos, Entity entity, int inverseChance) {
      if (this.breaksEgg(world, entity)) {
         if (!world.isClient && world.random.nextInt(inverseChance) == 0) {
            BlockState blockState = world.getBlockState(blockPos);
            if (blockState.isOf(Blocks.TURTLE_EGG)) {
               this.breakEgg(world, blockPos, blockState);
            }
         }

      }
   }

   private void breakEgg(World world, BlockPos pos, BlockState state) {
      world.playSound((PlayerEntity)null, pos, SoundEvents.ENTITY_TURTLE_EGG_BREAK, SoundCategory.BLOCKS, 0.7F, 0.9F + world.random.nextFloat() * 0.2F);
      int i = (Integer)state.get(EGGS);
      if (i <= 1) {
         world.breakBlock(pos, false);
      } else {
         world.setBlockState(pos, (BlockState)state.with(EGGS, i - 1), 2);
         world.syncWorldEvent(2001, pos, Block.getRawIdFromState(state));
      }

   }

   public void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
      if (this.shouldHatchProgress(world) && isSand(world, pos)) {
         int i = (Integer)state.get(HATCH);
         if (i < 2) {
            world.playSound((PlayerEntity)null, pos, SoundEvents.ENTITY_TURTLE_EGG_CRACK, SoundCategory.BLOCKS, 0.7F, 0.9F + random.nextFloat() * 0.2F);
            world.setBlockState(pos, (BlockState)state.with(HATCH, i + 1), 2);
         } else {
            world.playSound((PlayerEntity)null, pos, SoundEvents.ENTITY_TURTLE_EGG_HATCH, SoundCategory.BLOCKS, 0.7F, 0.9F + random.nextFloat() * 0.2F);
            world.removeBlock(pos, false);

            for(int j = 0; j < (Integer)state.get(EGGS); ++j) {
               world.syncWorldEvent(2001, pos, Block.getRawIdFromState(state));
               TurtleEntity turtleEntity = (TurtleEntity)EntityType.TURTLE.create(world);
               turtleEntity.setBreedingAge(-24000);
               turtleEntity.setHomePos(pos);
               turtleEntity.refreshPositionAndAngles((double)pos.getX() + 0.3D + (double)j * 0.2D, (double)pos.getY(), (double)pos.getZ() + 0.3D, 0.0F, 0.0F);
               world.spawnEntity(turtleEntity);
            }
         }
      }

   }

   public static boolean isSand(BlockView blockView, BlockPos blockPos) {
      return method_29952(blockView, blockPos.down());
   }

   public static boolean method_29952(BlockView blockView, BlockPos blockPos) {
      return blockView.getBlockState(blockPos).isIn(BlockTags.SAND);
   }

   public void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState oldState, boolean notify) {
      if (isSand(world, pos) && !world.isClient) {
         world.syncWorldEvent(2005, pos, 0);
      }

   }

   private boolean shouldHatchProgress(World world) {
      float f = world.getSkyAngle(1.0F);
      if ((double)f < 0.69D && (double)f > 0.65D) {
         return true;
      } else {
         return world.random.nextInt(500) == 0;
      }
   }

   public void afterBreak(World world, PlayerEntity player, BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity, ItemStack stack) {
      super.afterBreak(world, player, pos, state, blockEntity, stack);
      this.breakEgg(world, pos, state);
   }

   public boolean canReplace(BlockState state, ItemPlacementContext context) {
      return context.getStack().getItem() == this.asItem() && (Integer)state.get(EGGS) < 4 ? true : super.canReplace(state, context);
   }

   @Nullable
   public BlockState getPlacementState(ItemPlacementContext ctx) {
      BlockState blockState = ctx.getWorld().getBlockState(ctx.getBlockPos());
      return blockState.isOf(this) ? (BlockState)blockState.with(EGGS, Math.min(4, (Integer)blockState.get(EGGS) + 1)) : super.getPlacementState(ctx);
   }

   public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
      return (Integer)state.get(EGGS) > 1 ? LARGE_SHAPE : SMALL_SHAPE;
   }

   protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
      builder.add(HATCH, EGGS);
   }

   private boolean breaksEgg(World world, Entity entity) {
      if (!(entity instanceof TurtleEntity) && !(entity instanceof BatEntity)) {
         if (!(entity instanceof LivingEntity)) {
            return false;
         } else {
            return entity instanceof PlayerEntity || world.getGameRules().getBoolean(GameRules.DO_MOB_GRIEFING);
         }
      } else {
         return false;
      }
   }

   static {
      HATCH = Properties.HATCH;
      EGGS = Properties.EGGS;
   }
}
