package net.minecraft.block;

import java.util.Optional;
import java.util.Random;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.CampfireBlockEntity;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.DefaultParticleType;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.recipe.CampfireCookingRecipe;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.tag.BlockTags;
import net.minecraft.util.ActionResult;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.function.BooleanBiFunction;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import org.jetbrains.annotations.Nullable;

public class CampfireBlock extends BlockWithEntity implements Waterloggable {
   protected static final VoxelShape SHAPE = Block.createCuboidShape(0.0D, 0.0D, 0.0D, 16.0D, 7.0D, 16.0D);
   public static final BooleanProperty LIT;
   public static final BooleanProperty SIGNAL_FIRE;
   public static final BooleanProperty WATERLOGGED;
   public static final DirectionProperty FACING;
   /**
    * The shape used to test whether a given block is considered 'smokey'.
    */
   private static final VoxelShape SMOKEY_SHAPE;
   private final boolean emitsParticles;
   private final int fireDamage;

   public CampfireBlock(boolean emitsParticles, int fireDamage, AbstractBlock.Settings settings) {
      super(settings);
      this.emitsParticles = emitsParticles;
      this.fireDamage = fireDamage;
      this.setDefaultState((BlockState)((BlockState)((BlockState)((BlockState)((BlockState)this.stateManager.getDefaultState()).with(LIT, true)).with(SIGNAL_FIRE, false)).with(WATERLOGGED, false)).with(FACING, Direction.NORTH));
   }

   public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
      BlockEntity blockEntity = world.getBlockEntity(pos);
      if (blockEntity instanceof CampfireBlockEntity) {
         CampfireBlockEntity campfireBlockEntity = (CampfireBlockEntity)blockEntity;
         ItemStack itemStack = player.getStackInHand(hand);
         Optional<CampfireCookingRecipe> optional = campfireBlockEntity.getRecipeFor(itemStack);
         if (optional.isPresent()) {
            if (!world.isClient && campfireBlockEntity.addItem(player.abilities.creativeMode ? itemStack.copy() : itemStack, ((CampfireCookingRecipe)optional.get()).getCookTime())) {
               player.incrementStat(Stats.INTERACT_WITH_CAMPFIRE);
               return ActionResult.SUCCESS;
            }

            return ActionResult.CONSUME;
         }
      }

      return ActionResult.PASS;
   }

   public void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity) {
      if (!entity.isFireImmune() && (Boolean)state.get(LIT) && entity instanceof LivingEntity && !EnchantmentHelper.hasFrostWalker((LivingEntity)entity)) {
         entity.damage(DamageSource.IN_FIRE, (float)this.fireDamage);
      }

      super.onEntityCollision(state, world, pos, entity);
   }

   public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
      if (!state.isOf(newState.getBlock())) {
         BlockEntity blockEntity = world.getBlockEntity(pos);
         if (blockEntity instanceof CampfireBlockEntity) {
            ItemScatterer.spawn(world, pos, ((CampfireBlockEntity)blockEntity).getItemsBeingCooked());
         }

         super.onStateReplaced(state, world, pos, newState, moved);
      }
   }

   @Nullable
   public BlockState getPlacementState(ItemPlacementContext ctx) {
      WorldAccess worldAccess = ctx.getWorld();
      BlockPos blockPos = ctx.getBlockPos();
      boolean bl = worldAccess.getFluidState(blockPos).getFluid() == Fluids.WATER;
      return (BlockState)((BlockState)((BlockState)((BlockState)this.getDefaultState().with(WATERLOGGED, bl)).with(SIGNAL_FIRE, this.doesBlockCauseSignalFire(worldAccess.getBlockState(blockPos.down())))).with(LIT, !bl)).with(FACING, ctx.getPlayerFacing());
   }

   public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState newState, WorldAccess world, BlockPos pos, BlockPos posFrom) {
      if ((Boolean)state.get(WATERLOGGED)) {
         world.getFluidTickScheduler().schedule(pos, Fluids.WATER, Fluids.WATER.getTickRate(world));
      }

      return direction == Direction.DOWN ? (BlockState)state.with(SIGNAL_FIRE, this.doesBlockCauseSignalFire(newState)) : super.getStateForNeighborUpdate(state, direction, newState, world, pos, posFrom);
   }

   private boolean doesBlockCauseSignalFire(BlockState state) {
      return state.isOf(Blocks.HAY_BLOCK);
   }

   public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
      return SHAPE;
   }

   public BlockRenderType getRenderType(BlockState state) {
      return BlockRenderType.MODEL;
   }

   @Environment(EnvType.CLIENT)
   public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
      if ((Boolean)state.get(LIT)) {
         if (random.nextInt(10) == 0) {
            world.playSound((double)pos.getX() + 0.5D, (double)pos.getY() + 0.5D, (double)pos.getZ() + 0.5D, SoundEvents.BLOCK_CAMPFIRE_CRACKLE, SoundCategory.BLOCKS, 0.5F + random.nextFloat(), random.nextFloat() * 0.7F + 0.6F, false);
         }

         if (this.emitsParticles && random.nextInt(5) == 0) {
            for(int i = 0; i < random.nextInt(1) + 1; ++i) {
               world.addParticle(ParticleTypes.LAVA, (double)pos.getX() + 0.5D, (double)pos.getY() + 0.5D, (double)pos.getZ() + 0.5D, (double)(random.nextFloat() / 2.0F), 5.0E-5D, (double)(random.nextFloat() / 2.0F));
            }
         }

      }
   }

   public static void extinguish(WorldAccess world, BlockPos pos, BlockState state) {
      if (world.isClient()) {
         for(int i = 0; i < 20; ++i) {
            spawnSmokeParticle((World)world, pos, (Boolean)state.get(SIGNAL_FIRE), true);
         }
      }

      BlockEntity blockEntity = world.getBlockEntity(pos);
      if (blockEntity instanceof CampfireBlockEntity) {
         ((CampfireBlockEntity)blockEntity).spawnItemsBeingCooked();
      }

   }

   public boolean tryFillWithFluid(WorldAccess world, BlockPos pos, BlockState state, FluidState fluidState) {
      if (!(Boolean)state.get(Properties.WATERLOGGED) && fluidState.getFluid() == Fluids.WATER) {
         boolean bl = (Boolean)state.get(LIT);
         if (bl) {
            if (!world.isClient()) {
               world.playSound((PlayerEntity)null, pos, SoundEvents.ENTITY_GENERIC_EXTINGUISH_FIRE, SoundCategory.BLOCKS, 1.0F, 1.0F);
            }

            extinguish(world, pos, state);
         }

         world.setBlockState(pos, (BlockState)((BlockState)state.with(WATERLOGGED, true)).with(LIT, false), 3);
         world.getFluidTickScheduler().schedule(pos, fluidState.getFluid(), fluidState.getFluid().getTickRate(world));
         return true;
      } else {
         return false;
      }
   }

   public void onProjectileHit(World world, BlockState state, BlockHitResult hit, ProjectileEntity projectile) {
      if (!world.isClient && projectile.isOnFire()) {
         Entity entity = projectile.getOwner();
         boolean bl = entity == null || entity instanceof PlayerEntity || world.getGameRules().getBoolean(GameRules.DO_MOB_GRIEFING);
         if (bl && !(Boolean)state.get(LIT) && !(Boolean)state.get(WATERLOGGED)) {
            BlockPos blockPos = hit.getBlockPos();
            world.setBlockState(blockPos, (BlockState)state.with(Properties.LIT, true), 11);
         }
      }

   }

   public static void spawnSmokeParticle(World world, BlockPos pos, boolean isSignal, boolean lotsOfSmoke) {
      Random random = world.getRandom();
      DefaultParticleType defaultParticleType = isSignal ? ParticleTypes.CAMPFIRE_SIGNAL_SMOKE : ParticleTypes.CAMPFIRE_COSY_SMOKE;
      world.addImportantParticle(defaultParticleType, true, (double)pos.getX() + 0.5D + random.nextDouble() / 3.0D * (double)(random.nextBoolean() ? 1 : -1), (double)pos.getY() + random.nextDouble() + random.nextDouble(), (double)pos.getZ() + 0.5D + random.nextDouble() / 3.0D * (double)(random.nextBoolean() ? 1 : -1), 0.0D, 0.07D, 0.0D);
      if (lotsOfSmoke) {
         world.addParticle(ParticleTypes.SMOKE, (double)pos.getX() + 0.25D + random.nextDouble() / 2.0D * (double)(random.nextBoolean() ? 1 : -1), (double)pos.getY() + 0.4D, (double)pos.getZ() + 0.25D + random.nextDouble() / 2.0D * (double)(random.nextBoolean() ? 1 : -1), 0.0D, 0.005D, 0.0D);
      }

   }

   public static boolean isLitCampfireInRange(World world, BlockPos pos) {
      for(int i = 1; i <= 5; ++i) {
         BlockPos blockPos = pos.down(i);
         BlockState blockState = world.getBlockState(blockPos);
         if (isLitCampfire(blockState)) {
            return true;
         }

         boolean bl = VoxelShapes.matchesAnywhere(SMOKEY_SHAPE, blockState.getCollisionShape(world, pos, ShapeContext.absent()), BooleanBiFunction.AND);
         if (bl) {
            BlockState blockState2 = world.getBlockState(blockPos.down());
            return isLitCampfire(blockState2);
         }
      }

      return false;
   }

   public static boolean isLitCampfire(BlockState state) {
      return state.contains(LIT) && state.isIn(BlockTags.CAMPFIRES) && (Boolean)state.get(LIT);
   }

   public FluidState getFluidState(BlockState state) {
      return (Boolean)state.get(WATERLOGGED) ? Fluids.WATER.getStill(false) : super.getFluidState(state);
   }

   public BlockState rotate(BlockState state, BlockRotation rotation) {
      return (BlockState)state.with(FACING, rotation.rotate((Direction)state.get(FACING)));
   }

   public BlockState mirror(BlockState state, BlockMirror mirror) {
      return state.rotate(mirror.getRotation((Direction)state.get(FACING)));
   }

   protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
      builder.add(LIT, SIGNAL_FIRE, WATERLOGGED, FACING);
   }

   public BlockEntity createBlockEntity(BlockView world) {
      return new CampfireBlockEntity();
   }

   public boolean canPathfindThrough(BlockState state, BlockView world, BlockPos pos, NavigationType type) {
      return false;
   }

   public static boolean method_30035(BlockState blockState) {
      return blockState.method_27851(BlockTags.CAMPFIRES, (abstractBlockState) -> {
         return abstractBlockState.contains(Properties.WATERLOGGED) && abstractBlockState.contains(Properties.LIT);
      }) && !(Boolean)blockState.get(Properties.WATERLOGGED) && !(Boolean)blockState.get(Properties.LIT);
   }

   static {
      LIT = Properties.LIT;
      SIGNAL_FIRE = Properties.SIGNAL_FIRE;
      WATERLOGGED = Properties.WATERLOGGED;
      FACING = Properties.HORIZONTAL_FACING;
      SMOKEY_SHAPE = Block.createCuboidShape(6.0D, 0.0D, 6.0D, 10.0D, 16.0D, 10.0D);
   }
}
