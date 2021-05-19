package net.minecraft.block;

import java.util.Optional;
import java.util.Random;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.dimension.AreaHelper;

public abstract class AbstractFireBlock extends Block {
   private final float damage;
   protected static final VoxelShape BASE_SHAPE = Block.createCuboidShape(0.0D, 0.0D, 0.0D, 16.0D, 1.0D, 16.0D);

   public AbstractFireBlock(AbstractBlock.Settings settings, float damage) {
      super(settings);
      this.damage = damage;
   }

   public BlockState getPlacementState(ItemPlacementContext ctx) {
      return getState(ctx.getWorld(), ctx.getBlockPos());
   }

   public static BlockState getState(BlockView world, BlockPos pos) {
      BlockPos blockPos = pos.down();
      BlockState blockState = world.getBlockState(blockPos);
      return SoulFireBlock.isSoulBase(blockState.getBlock()) ? Blocks.SOUL_FIRE.getDefaultState() : ((FireBlock)Blocks.FIRE).getStateForPosition(world, pos);
   }

   public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
      return BASE_SHAPE;
   }

   @Environment(EnvType.CLIENT)
   public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
      if (random.nextInt(24) == 0) {
         world.playSound((double)pos.getX() + 0.5D, (double)pos.getY() + 0.5D, (double)pos.getZ() + 0.5D, SoundEvents.BLOCK_FIRE_AMBIENT, SoundCategory.BLOCKS, 1.0F + random.nextFloat(), random.nextFloat() * 0.7F + 0.3F, false);
      }

      BlockPos blockPos = pos.down();
      BlockState blockState = world.getBlockState(blockPos);
      int x;
      double y;
      double z;
      double aa;
      if (!this.isFlammable(blockState) && !blockState.isSideSolidFullSquare(world, blockPos, Direction.UP)) {
         if (this.isFlammable(world.getBlockState(pos.west()))) {
            for(x = 0; x < 2; ++x) {
               y = (double)pos.getX() + random.nextDouble() * 0.10000000149011612D;
               z = (double)pos.getY() + random.nextDouble();
               aa = (double)pos.getZ() + random.nextDouble();
               world.addParticle(ParticleTypes.LARGE_SMOKE, y, z, aa, 0.0D, 0.0D, 0.0D);
            }
         }

         if (this.isFlammable(world.getBlockState(pos.east()))) {
            for(x = 0; x < 2; ++x) {
               y = (double)(pos.getX() + 1) - random.nextDouble() * 0.10000000149011612D;
               z = (double)pos.getY() + random.nextDouble();
               aa = (double)pos.getZ() + random.nextDouble();
               world.addParticle(ParticleTypes.LARGE_SMOKE, y, z, aa, 0.0D, 0.0D, 0.0D);
            }
         }

         if (this.isFlammable(world.getBlockState(pos.north()))) {
            for(x = 0; x < 2; ++x) {
               y = (double)pos.getX() + random.nextDouble();
               z = (double)pos.getY() + random.nextDouble();
               aa = (double)pos.getZ() + random.nextDouble() * 0.10000000149011612D;
               world.addParticle(ParticleTypes.LARGE_SMOKE, y, z, aa, 0.0D, 0.0D, 0.0D);
            }
         }

         if (this.isFlammable(world.getBlockState(pos.south()))) {
            for(x = 0; x < 2; ++x) {
               y = (double)pos.getX() + random.nextDouble();
               z = (double)pos.getY() + random.nextDouble();
               aa = (double)(pos.getZ() + 1) - random.nextDouble() * 0.10000000149011612D;
               world.addParticle(ParticleTypes.LARGE_SMOKE, y, z, aa, 0.0D, 0.0D, 0.0D);
            }
         }

         if (this.isFlammable(world.getBlockState(pos.up()))) {
            for(x = 0; x < 2; ++x) {
               y = (double)pos.getX() + random.nextDouble();
               z = (double)(pos.getY() + 1) - random.nextDouble() * 0.10000000149011612D;
               aa = (double)pos.getZ() + random.nextDouble();
               world.addParticle(ParticleTypes.LARGE_SMOKE, y, z, aa, 0.0D, 0.0D, 0.0D);
            }
         }
      } else {
         for(x = 0; x < 3; ++x) {
            y = (double)pos.getX() + random.nextDouble();
            z = (double)pos.getY() + random.nextDouble() * 0.5D + 0.5D;
            aa = (double)pos.getZ() + random.nextDouble();
            world.addParticle(ParticleTypes.LARGE_SMOKE, y, z, aa, 0.0D, 0.0D, 0.0D);
         }
      }

   }

   protected abstract boolean isFlammable(BlockState state);

   public void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity) {
      if (!entity.isFireImmune()) {
         entity.setFireTicks(entity.getFireTicks() + 1);
         if (entity.getFireTicks() == 0) {
            entity.setOnFireFor(8);
         }

         entity.damage(DamageSource.IN_FIRE, this.damage);
      }

      super.onEntityCollision(state, world, pos, entity);
   }

   public void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState oldState, boolean notify) {
      if (!oldState.isOf(state.getBlock())) {
         if (method_30366(world)) {
            Optional<AreaHelper> optional = AreaHelper.method_30485(world, pos, Direction.Axis.X);
            if (optional.isPresent()) {
               ((AreaHelper)optional.get()).createPortal();
               return;
            }
         }

         if (!state.canPlaceAt(world, pos)) {
            world.removeBlock(pos, false);
         }

      }
   }

   private static boolean method_30366(World world) {
      return world.getRegistryKey() == World.OVERWORLD || world.getRegistryKey() == World.NETHER;
   }

   public void onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player) {
      if (!world.isClient()) {
         world.syncWorldEvent((PlayerEntity)null, 1009, pos, 0);
      }

   }

   public static boolean method_30032(World world, BlockPos blockPos, Direction direction) {
      BlockState blockState = world.getBlockState(blockPos);
      if (!blockState.isAir()) {
         return false;
      } else {
         return getState(world, blockPos).canPlaceAt(world, blockPos) || method_30033(world, blockPos, direction);
      }
   }

   private static boolean method_30033(World world, BlockPos blockPos, Direction direction) {
      if (!method_30366(world)) {
         return false;
      } else {
         BlockPos.Mutable mutable = blockPos.mutableCopy();
         boolean bl = false;
         Direction[] var5 = Direction.values();
         int var6 = var5.length;

         for(int var7 = 0; var7 < var6; ++var7) {
            Direction direction2 = var5[var7];
            if (world.getBlockState(mutable.set(blockPos).move(direction2)).isOf(Blocks.OBSIDIAN)) {
               bl = true;
               break;
            }
         }

         if (!bl) {
            return false;
         } else {
            Direction.Axis axis = direction.getAxis().isHorizontal() ? direction.rotateYCounterclockwise().getAxis() : Direction.Type.HORIZONTAL.randomAxis(world.random);
            return AreaHelper.method_30485(world, blockPos, axis).isPresent();
         }
      }
   }
}
