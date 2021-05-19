package net.minecraft.entity.passive;

import com.google.common.collect.Sets;
import com.google.common.collect.UnmodifiableIterator;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FluidBlock;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.Dismounting;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityData;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.ItemSteerable;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.Saddleable;
import net.minecraft.entity.SaddledComponent;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.goal.AnimalMateGoal;
import net.minecraft.entity.ai.goal.EscapeDangerGoal;
import net.minecraft.entity.ai.goal.FollowParentGoal;
import net.minecraft.entity.ai.goal.LookAroundGoal;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.ai.goal.MoveToTargetPosGoal;
import net.minecraft.entity.ai.goal.TemptGoal;
import net.minecraft.entity.ai.goal.WanderAroundGoal;
import net.minecraft.entity.ai.pathing.EntityNavigation;
import net.minecraft.entity.ai.pathing.LandPathNodeMaker;
import net.minecraft.entity.ai.pathing.MobNavigation;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.entity.ai.pathing.PathNodeNavigator;
import net.minecraft.entity.ai.pathing.PathNodeType;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.recipe.Ingredient;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.tag.BlockTags;
import net.minecraft.tag.FluidTags;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;
import org.jetbrains.annotations.Nullable;

public class StriderEntity extends AnimalEntity implements ItemSteerable, Saddleable {
   private static final Ingredient BREEDING_INGREDIENT;
   private static final Ingredient ATTRACTING_INGREDIENT;
   private static final TrackedData<Integer> BOOST_TIME;
   private static final TrackedData<Boolean> COLD;
   private static final TrackedData<Boolean> SADDLED;
   private final SaddledComponent saddledComponent;
   private TemptGoal temptGoal;
   private EscapeDangerGoal escapeDangerGoal;

   public StriderEntity(EntityType<? extends StriderEntity> entityType, World world) {
      super(entityType, world);
      this.saddledComponent = new SaddledComponent(this.dataTracker, BOOST_TIME, SADDLED);
      this.inanimate = true;
      this.setPathfindingPenalty(PathNodeType.WATER, -1.0F);
      this.setPathfindingPenalty(PathNodeType.LAVA, 0.0F);
      this.setPathfindingPenalty(PathNodeType.DANGER_FIRE, 0.0F);
      this.setPathfindingPenalty(PathNodeType.DAMAGE_FIRE, 0.0F);
   }

   public static boolean canSpawn(EntityType<StriderEntity> type, WorldAccess world, SpawnReason spawnReason, BlockPos pos, Random random) {
      BlockPos.Mutable mutable = pos.mutableCopy();

      do {
         mutable.move(Direction.UP);
      } while(world.getFluidState(mutable).isIn(FluidTags.LAVA));

      return world.getBlockState(mutable).isAir();
   }

   public void onTrackedDataSet(TrackedData<?> data) {
      if (BOOST_TIME.equals(data) && this.world.isClient) {
         this.saddledComponent.boost();
      }

      super.onTrackedDataSet(data);
   }

   protected void initDataTracker() {
      super.initDataTracker();
      this.dataTracker.startTracking(BOOST_TIME, 0);
      this.dataTracker.startTracking(COLD, false);
      this.dataTracker.startTracking(SADDLED, false);
   }

   public void writeCustomDataToTag(CompoundTag tag) {
      super.writeCustomDataToTag(tag);
      this.saddledComponent.toTag(tag);
   }

   public void readCustomDataFromTag(CompoundTag tag) {
      super.readCustomDataFromTag(tag);
      this.saddledComponent.fromTag(tag);
   }

   public boolean isSaddled() {
      return this.saddledComponent.isSaddled();
   }

   public boolean canBeSaddled() {
      return this.isAlive() && !this.isBaby();
   }

   public void saddle(@Nullable SoundCategory sound) {
      this.saddledComponent.setSaddled(true);
      if (sound != null) {
         this.world.playSoundFromEntity((PlayerEntity)null, this, SoundEvents.ENTITY_STRIDER_SADDLE, sound, 0.5F, 1.0F);
      }

   }

   protected void initGoals() {
      this.escapeDangerGoal = new EscapeDangerGoal(this, 1.65D);
      this.goalSelector.add(1, this.escapeDangerGoal);
      this.goalSelector.add(2, new AnimalMateGoal(this, 1.0D));
      this.temptGoal = new TemptGoal(this, 1.4D, false, ATTRACTING_INGREDIENT);
      this.goalSelector.add(3, this.temptGoal);
      this.goalSelector.add(4, new StriderEntity.GoBackToLavaGoal(this, 1.5D));
      this.goalSelector.add(5, new FollowParentGoal(this, 1.1D));
      this.goalSelector.add(7, new WanderAroundGoal(this, 1.0D, 60));
      this.goalSelector.add(8, new LookAtEntityGoal(this, PlayerEntity.class, 8.0F));
      this.goalSelector.add(8, new LookAroundGoal(this));
      this.goalSelector.add(9, new LookAtEntityGoal(this, StriderEntity.class, 8.0F));
   }

   public void setCold(boolean cold) {
      this.dataTracker.set(COLD, cold);
   }

   public boolean isCold() {
      return this.getVehicle() instanceof StriderEntity ? ((StriderEntity)this.getVehicle()).isCold() : (Boolean)this.dataTracker.get(COLD);
   }

   public boolean canWalkOnFluid(Fluid fluid) {
      return fluid.isIn(FluidTags.LAVA);
   }

   public double getMountedHeightOffset() {
      float f = Math.min(0.25F, this.limbDistance);
      float g = this.limbAngle;
      return (double)this.getHeight() - 0.19D + (double)(0.12F * MathHelper.cos(g * 1.5F) * 2.0F * f);
   }

   public boolean canBeControlledByRider() {
      Entity entity = this.getPrimaryPassenger();
      if (!(entity instanceof PlayerEntity)) {
         return false;
      } else {
         PlayerEntity playerEntity = (PlayerEntity)entity;
         return playerEntity.getMainHandStack().getItem() == Items.WARPED_FUNGUS_ON_A_STICK || playerEntity.getOffHandStack().getItem() == Items.WARPED_FUNGUS_ON_A_STICK;
      }
   }

   public boolean canSpawn(WorldView world) {
      return world.intersectsEntities(this);
   }

   @Nullable
   public Entity getPrimaryPassenger() {
      return this.getPassengerList().isEmpty() ? null : (Entity)this.getPassengerList().get(0);
   }

   public Vec3d updatePassengerForDismount(LivingEntity passenger) {
      Vec3d[] vec3ds = new Vec3d[]{getPassengerDismountOffset((double)this.getWidth(), (double)passenger.getWidth(), passenger.yaw), getPassengerDismountOffset((double)this.getWidth(), (double)passenger.getWidth(), passenger.yaw - 22.5F), getPassengerDismountOffset((double)this.getWidth(), (double)passenger.getWidth(), passenger.yaw + 22.5F), getPassengerDismountOffset((double)this.getWidth(), (double)passenger.getWidth(), passenger.yaw - 45.0F), getPassengerDismountOffset((double)this.getWidth(), (double)passenger.getWidth(), passenger.yaw + 45.0F)};
      Set<BlockPos> set = Sets.newLinkedHashSet();
      double d = this.getBoundingBox().maxY;
      double e = this.getBoundingBox().minY - 0.5D;
      BlockPos.Mutable mutable = new BlockPos.Mutable();
      Vec3d[] var9 = vec3ds;
      int var10 = vec3ds.length;

      for(int var11 = 0; var11 < var10; ++var11) {
         Vec3d vec3d = var9[var11];
         mutable.set(this.getX() + vec3d.x, d, this.getZ() + vec3d.z);

         for(double f = d; f > e; --f) {
            set.add(mutable.toImmutable());
            mutable.move(Direction.DOWN);
         }
      }

      Iterator var17 = set.iterator();

      while(true) {
         BlockPos blockPos;
         double g;
         do {
            do {
               if (!var17.hasNext()) {
                  return new Vec3d(this.getX(), this.getBoundingBox().maxY, this.getZ());
               }

               blockPos = (BlockPos)var17.next();
            } while(this.world.getFluidState(blockPos).isIn(FluidTags.LAVA));

            g = this.world.getDismountHeight(blockPos);
         } while(!Dismounting.canDismountInBlock(g));

         Vec3d vec3d2 = Vec3d.ofCenter(blockPos, g);
         UnmodifiableIterator var14 = passenger.getPoses().iterator();

         while(var14.hasNext()) {
            EntityPose entityPose = (EntityPose)var14.next();
            Box box = passenger.getBoundingBox(entityPose);
            if (Dismounting.canPlaceEntityAt(this.world, passenger, box.offset(vec3d2))) {
               passenger.setPose(entityPose);
               return vec3d2;
            }
         }
      }
   }

   public void travel(Vec3d movementInput) {
      this.setMovementSpeed(this.getSpeed());
      this.travel(this, this.saddledComponent, movementInput);
   }

   public float getSpeed() {
      return (float)this.getAttributeValue(EntityAttributes.GENERIC_MOVEMENT_SPEED) * (this.isCold() ? 0.66F : 1.0F);
   }

   public float getSaddledSpeed() {
      return (float)this.getAttributeValue(EntityAttributes.GENERIC_MOVEMENT_SPEED) * (this.isCold() ? 0.23F : 0.55F);
   }

   public void setMovementInput(Vec3d movementInput) {
      super.travel(movementInput);
   }

   protected float calculateNextStepSoundDistance() {
      return this.distanceTraveled + 0.6F;
   }

   protected void playStepSound(BlockPos pos, BlockState state) {
      this.playSound(this.isInLava() ? SoundEvents.ENTITY_STRIDER_STEP_LAVA : SoundEvents.ENTITY_STRIDER_STEP, 1.0F, 1.0F);
   }

   public boolean consumeOnAStickItem() {
      return this.saddledComponent.boost(this.getRandom());
   }

   protected void fall(double heightDifference, boolean onGround, BlockState landedState, BlockPos landedPosition) {
      this.checkBlockCollision();
      if (this.isInLava()) {
         this.fallDistance = 0.0F;
      } else {
         super.fall(heightDifference, onGround, landedState, landedPosition);
      }
   }

   public void tick() {
      if (this.method_30079() && this.random.nextInt(140) == 0) {
         this.playSound(SoundEvents.ENTITY_STRIDER_HAPPY, 1.0F, this.getSoundPitch());
      } else if (this.method_30078() && this.random.nextInt(60) == 0) {
         this.playSound(SoundEvents.ENTITY_STRIDER_RETREAT, 1.0F, this.getSoundPitch());
      }

      BlockState blockState = this.world.getBlockState(this.getBlockPos());
      BlockState blockState2 = this.getLandingBlockState();
      boolean bl = blockState.isIn(BlockTags.STRIDER_WARM_BLOCKS) || blockState2.isIn(BlockTags.STRIDER_WARM_BLOCKS) || this.getFluidHeight(FluidTags.LAVA) > 0.0D;
      this.setCold(!bl);
      super.tick();
      this.updateFloating();
      this.checkBlockCollision();
   }

   private boolean method_30078() {
      return this.escapeDangerGoal != null && this.escapeDangerGoal.isActive();
   }

   private boolean method_30079() {
      return this.temptGoal != null && this.temptGoal.isActive();
   }

   protected boolean movesIndependently() {
      return true;
   }

   private void updateFloating() {
      if (this.isInLava()) {
         ShapeContext shapeContext = ShapeContext.of(this);
         if (shapeContext.isAbove(FluidBlock.COLLISION_SHAPE, this.getBlockPos(), true) && !this.world.getFluidState(this.getBlockPos().up()).isIn(FluidTags.LAVA)) {
            this.onGround = true;
         } else {
            this.setVelocity(this.getVelocity().multiply(0.5D).add(0.0D, 0.05D, 0.0D));
         }
      }

   }

   public static DefaultAttributeContainer.Builder createStriderAttributes() {
      return MobEntity.createMobAttributes().add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.17499999701976776D).add(EntityAttributes.GENERIC_FOLLOW_RANGE, 16.0D);
   }

   protected SoundEvent getAmbientSound() {
      return !this.method_30078() && !this.method_30079() ? SoundEvents.ENTITY_STRIDER_AMBIENT : null;
   }

   protected SoundEvent getHurtSound(DamageSource source) {
      return SoundEvents.ENTITY_STRIDER_HURT;
   }

   protected SoundEvent getDeathSound() {
      return SoundEvents.ENTITY_STRIDER_DEATH;
   }

   protected boolean canAddPassenger(Entity passenger) {
      return this.getPassengerList().isEmpty() && !this.isSubmergedIn(FluidTags.LAVA);
   }

   public boolean hurtByWater() {
      return true;
   }

   public boolean isOnFire() {
      return false;
   }

   protected EntityNavigation createNavigation(World world) {
      return new StriderEntity.Navigation(this, world);
   }

   public float getPathfindingFavor(BlockPos pos, WorldView world) {
      if (world.getBlockState(pos).getFluidState().isIn(FluidTags.LAVA)) {
         return 10.0F;
      } else {
         return this.isInLava() ? Float.NEGATIVE_INFINITY : 0.0F;
      }
   }

   public StriderEntity createChild(ServerWorld serverWorld, PassiveEntity passiveEntity) {
      return (StriderEntity)EntityType.STRIDER.create(serverWorld);
   }

   public boolean isBreedingItem(ItemStack stack) {
      return BREEDING_INGREDIENT.test(stack);
   }

   protected void dropInventory() {
      super.dropInventory();
      if (this.isSaddled()) {
         this.dropItem(Items.SADDLE);
      }

   }

   public ActionResult interactMob(PlayerEntity player, Hand hand) {
      boolean bl = this.isBreedingItem(player.getStackInHand(hand));
      if (!bl && this.isSaddled() && !this.hasPassengers() && !player.shouldCancelInteraction()) {
         if (!this.world.isClient) {
            player.startRiding(this);
         }

         return ActionResult.success(this.world.isClient);
      } else {
         ActionResult actionResult = super.interactMob(player, hand);
         if (!actionResult.isAccepted()) {
            ItemStack itemStack = player.getStackInHand(hand);
            return itemStack.getItem() == Items.SADDLE ? itemStack.useOnEntity(player, this, hand) : ActionResult.PASS;
         } else {
            if (bl && !this.isSilent()) {
               this.world.playSound((PlayerEntity)null, this.getX(), this.getY(), this.getZ(), SoundEvents.ENTITY_STRIDER_EAT, this.getSoundCategory(), 1.0F, 1.0F + (this.random.nextFloat() - this.random.nextFloat()) * 0.2F);
            }

            return actionResult;
         }
      }
   }

   @Environment(EnvType.CLIENT)
   public Vec3d method_29919() {
      return new Vec3d(0.0D, (double)(0.6F * this.getStandingEyeHeight()), (double)(this.getWidth() * 0.4F));
   }

   @Nullable
   public EntityData initialize(ServerWorldAccess world, LocalDifficulty difficulty, SpawnReason spawnReason, @Nullable EntityData entityData, @Nullable CompoundTag entityTag) {
      if (this.isBaby()) {
         return super.initialize(world, difficulty, spawnReason, entityData, entityTag);
      } else {
         Object entityData;
         if (this.random.nextInt(30) == 0) {
            MobEntity mobEntity = (MobEntity)EntityType.ZOMBIFIED_PIGLIN.create(world.toServerWorld());
            entityData = this.method_30336(world, difficulty, mobEntity, new ZombieEntity.ZombieData(ZombieEntity.method_29936(this.random), false));
            mobEntity.equipStack(EquipmentSlot.MAINHAND, new ItemStack(Items.WARPED_FUNGUS_ON_A_STICK));
            this.saddle((SoundCategory)null);
         } else if (this.random.nextInt(10) == 0) {
            PassiveEntity passiveEntity = (PassiveEntity)EntityType.STRIDER.create(world.toServerWorld());
            passiveEntity.setBreedingAge(-24000);
            entityData = this.method_30336(world, difficulty, passiveEntity, (EntityData)null);
         } else {
            entityData = new PassiveEntity.PassiveData(0.5F);
         }

         return super.initialize(world, difficulty, spawnReason, (EntityData)entityData, entityTag);
      }
   }

   private EntityData method_30336(ServerWorldAccess serverWorldAccess, LocalDifficulty localDifficulty, MobEntity mobEntity, @Nullable EntityData entityData) {
      mobEntity.refreshPositionAndAngles(this.getX(), this.getY(), this.getZ(), this.yaw, 0.0F);
      mobEntity.initialize(serverWorldAccess, localDifficulty, SpawnReason.JOCKEY, entityData, (CompoundTag)null);
      mobEntity.startRiding(this, true);
      return new PassiveEntity.PassiveData(0.0F);
   }

   static {
      BREEDING_INGREDIENT = Ingredient.ofItems(Items.WARPED_FUNGUS);
      ATTRACTING_INGREDIENT = Ingredient.ofItems(Items.WARPED_FUNGUS, Items.WARPED_FUNGUS_ON_A_STICK);
      BOOST_TIME = DataTracker.registerData(StriderEntity.class, TrackedDataHandlerRegistry.INTEGER);
      COLD = DataTracker.registerData(StriderEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
      SADDLED = DataTracker.registerData(StriderEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
   }

   static class GoBackToLavaGoal extends MoveToTargetPosGoal {
      private final StriderEntity strider;

      private GoBackToLavaGoal(StriderEntity strider, double speed) {
         super(strider, speed, 8, 2);
         this.strider = strider;
      }

      public BlockPos getTargetPos() {
         return this.targetPos;
      }

      public boolean shouldContinue() {
         return !this.strider.isInLava() && this.isTargetPos(this.strider.world, this.targetPos);
      }

      public boolean canStart() {
         return !this.strider.isInLava() && super.canStart();
      }

      public boolean shouldResetPath() {
         return this.tryingTime % 20 == 0;
      }

      protected boolean isTargetPos(WorldView world, BlockPos pos) {
         return world.getBlockState(pos).isOf(Blocks.LAVA) && world.getBlockState(pos.up()).canPathfindThrough(world, pos, NavigationType.LAND);
      }
   }

   static class Navigation extends MobNavigation {
      Navigation(StriderEntity entity, World world) {
         super(entity, world);
      }

      protected PathNodeNavigator createPathNodeNavigator(int range) {
         this.nodeMaker = new LandPathNodeMaker();
         return new PathNodeNavigator(this.nodeMaker, range);
      }

      protected boolean canWalkOnPath(PathNodeType pathType) {
         return pathType != PathNodeType.LAVA && pathType != PathNodeType.DAMAGE_FIRE && pathType != PathNodeType.DANGER_FIRE ? super.canWalkOnPath(pathType) : true;
      }

      public boolean isValidPosition(BlockPos pos) {
         return this.world.getBlockState(pos).isOf(Blocks.LAVA) || super.isValidPosition(pos);
      }
   }
}
