package net.minecraft.block;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.UnmodifiableIterator;
import com.google.common.collect.ImmutableList.Builder;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Stream;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.Dismounting;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.tag.FluidTags;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.BlockView;
import net.minecraft.world.CollisionView;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion;
import net.minecraft.world.explosion.ExplosionBehavior;

public class RespawnAnchorBlock extends Block {
   public static final IntProperty CHARGES;
   private static final ImmutableList<Vec3i> field_26442;
   private static final ImmutableList<Vec3i> field_26443;

   public RespawnAnchorBlock(AbstractBlock.Settings settings) {
      super(settings);
      this.setDefaultState((BlockState)((BlockState)this.stateManager.getDefaultState()).with(CHARGES, 0));
   }

   public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
      ItemStack itemStack = player.getStackInHand(hand);
      if (hand == Hand.MAIN_HAND && !isChargeItem(itemStack) && isChargeItem(player.getStackInHand(Hand.OFF_HAND))) {
         return ActionResult.PASS;
      } else if (isChargeItem(itemStack) && canCharge(state)) {
         charge(world, pos, state);
         if (!player.abilities.creativeMode) {
            itemStack.decrement(1);
         }

         return ActionResult.success(world.isClient);
      } else if ((Integer)state.get(CHARGES) == 0) {
         return ActionResult.PASS;
      } else if (!isNether(world)) {
         if (!world.isClient) {
            this.explode(state, world, pos);
         }

         return ActionResult.success(world.isClient);
      } else {
         if (!world.isClient) {
            ServerPlayerEntity serverPlayerEntity = (ServerPlayerEntity)player;
            if (serverPlayerEntity.getSpawnPointDimension() != world.getRegistryKey() || !serverPlayerEntity.getSpawnPointPosition().equals(pos)) {
               serverPlayerEntity.setSpawnPoint(world.getRegistryKey(), pos, 0.0F, false, true);
               world.playSound((PlayerEntity)null, (double)pos.getX() + 0.5D, (double)pos.getY() + 0.5D, (double)pos.getZ() + 0.5D, SoundEvents.BLOCK_RESPAWN_ANCHOR_SET_SPAWN, SoundCategory.BLOCKS, 1.0F, 1.0F);
               return ActionResult.SUCCESS;
            }
         }

         return ActionResult.CONSUME;
      }
   }

   private static boolean isChargeItem(ItemStack stack) {
      return stack.getItem() == Items.GLOWSTONE;
   }

   private static boolean canCharge(BlockState state) {
      return (Integer)state.get(CHARGES) < 4;
   }

   private static boolean hasStillWater(BlockPos pos, World world) {
      FluidState fluidState = world.getFluidState(pos);
      if (!fluidState.isIn(FluidTags.WATER)) {
         return false;
      } else if (fluidState.isStill()) {
         return true;
      } else {
         float f = (float)fluidState.getLevel();
         if (f < 2.0F) {
            return false;
         } else {
            FluidState fluidState2 = world.getFluidState(pos.down());
            return !fluidState2.isIn(FluidTags.WATER);
         }
      }
   }

   private void explode(BlockState state, World world, final BlockPos explodedPos) {
      world.removeBlock(explodedPos, false);
      Stream var10000 = Direction.Type.HORIZONTAL.stream();
      explodedPos.getClass();
      boolean bl = var10000.map(explodedPos::offset).anyMatch((blockPos) -> {
         return hasStillWater(blockPos, world);
      });
      final boolean bl2 = bl || world.getFluidState(explodedPos.up()).isIn(FluidTags.WATER);
      ExplosionBehavior explosionBehavior = new ExplosionBehavior() {
         public Optional<Float> getBlastResistance(Explosion explosion, BlockView world, BlockPos pos, BlockState blockState, FluidState fluidState) {
            return pos.equals(explodedPos) && bl2 ? Optional.of(Blocks.WATER.getBlastResistance()) : super.getBlastResistance(explosion, world, pos, blockState, fluidState);
         }
      };
      world.createExplosion((Entity)null, DamageSource.badRespawnPoint(), explosionBehavior, (double)explodedPos.getX() + 0.5D, (double)explodedPos.getY() + 0.5D, (double)explodedPos.getZ() + 0.5D, 5.0F, true, Explosion.DestructionType.DESTROY);
   }

   public static boolean isNether(World world) {
      return world.getDimension().isRespawnAnchorWorking();
   }

   public static void charge(World world, BlockPos pos, BlockState state) {
      world.setBlockState(pos, (BlockState)state.with(CHARGES, (Integer)state.get(CHARGES) + 1), 3);
      world.playSound((PlayerEntity)null, (double)pos.getX() + 0.5D, (double)pos.getY() + 0.5D, (double)pos.getZ() + 0.5D, SoundEvents.BLOCK_RESPAWN_ANCHOR_CHARGE, SoundCategory.BLOCKS, 1.0F, 1.0F);
   }

   @Environment(EnvType.CLIENT)
   public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
      if ((Integer)state.get(CHARGES) != 0) {
         if (random.nextInt(100) == 0) {
            world.playSound((PlayerEntity)null, (double)pos.getX() + 0.5D, (double)pos.getY() + 0.5D, (double)pos.getZ() + 0.5D, SoundEvents.BLOCK_RESPAWN_ANCHOR_AMBIENT, SoundCategory.BLOCKS, 1.0F, 1.0F);
         }

         double d = (double)pos.getX() + 0.5D + (0.5D - random.nextDouble());
         double e = (double)pos.getY() + 1.0D;
         double f = (double)pos.getZ() + 0.5D + (0.5D - random.nextDouble());
         double g = (double)random.nextFloat() * 0.04D;
         world.addParticle(ParticleTypes.REVERSE_PORTAL, d, e, f, 0.0D, g, 0.0D);
      }
   }

   protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
      builder.add(CHARGES);
   }

   public boolean hasComparatorOutput(BlockState state) {
      return true;
   }

   public static int getLightLevel(BlockState state, int maxLevel) {
      return MathHelper.floor((float)((Integer)state.get(CHARGES) - 0) / 4.0F * (float)maxLevel);
   }

   public int getComparatorOutput(BlockState state, World world, BlockPos pos) {
      return getLightLevel(state, 15);
   }

   public static Optional<Vec3d> findRespawnPosition(EntityType<?> entity, CollisionView collisionView, BlockPos pos) {
      Optional<Vec3d> optional = method_30842(entity, collisionView, pos, true);
      return optional.isPresent() ? optional : method_30842(entity, collisionView, pos, false);
   }

   private static Optional<Vec3d> method_30842(EntityType<?> entityType, CollisionView collisionView, BlockPos blockPos, boolean bl) {
      BlockPos.Mutable mutable = new BlockPos.Mutable();
      UnmodifiableIterator var5 = field_26443.iterator();

      Vec3d vec3d;
      do {
         if (!var5.hasNext()) {
            return Optional.empty();
         }

         Vec3i vec3i = (Vec3i)var5.next();
         mutable.set(blockPos).move(vec3i);
         vec3d = Dismounting.method_30769(entityType, collisionView, mutable, bl);
      } while(vec3d == null);

      return Optional.of(vec3d);
   }

   public boolean canPathfindThrough(BlockState state, BlockView world, BlockPos pos, NavigationType type) {
      return false;
   }

   static {
      CHARGES = Properties.CHARGES;
      field_26442 = ImmutableList.of(new Vec3i(0, 0, -1), new Vec3i(-1, 0, 0), new Vec3i(0, 0, 1), new Vec3i(1, 0, 0), new Vec3i(-1, 0, -1), new Vec3i(1, 0, -1), new Vec3i(-1, 0, 1), new Vec3i(1, 0, 1));
      field_26443 = (new Builder()).addAll((Iterable)field_26442).addAll(field_26442.stream().map(Vec3i::down).iterator()).addAll(field_26442.stream().map(Vec3i::up).iterator()).add((Object)(new Vec3i(0, 1, 0))).build();
   }
}
