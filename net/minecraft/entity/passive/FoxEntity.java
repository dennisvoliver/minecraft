package net.minecraft.entity.passive;

import com.google.common.collect.Lists;
import java.util.Arrays;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.SweetBerryBushBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityData;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.TargetPredicate;
import net.minecraft.entity.ai.control.LookControl;
import net.minecraft.entity.ai.control.MoveControl;
import net.minecraft.entity.ai.goal.AnimalMateGoal;
import net.minecraft.entity.ai.goal.DiveJumpingGoal;
import net.minecraft.entity.ai.goal.EscapeDangerGoal;
import net.minecraft.entity.ai.goal.EscapeSunlightGoal;
import net.minecraft.entity.ai.goal.FleeEntityGoal;
import net.minecraft.entity.ai.goal.FollowTargetGoal;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.entity.ai.goal.MoveToTargetPosGoal;
import net.minecraft.entity.ai.goal.PounceAtTargetGoal;
import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.entity.ai.goal.WanderAroundFarGoal;
import net.minecraft.entity.ai.pathing.PathNodeType;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.particle.ItemStackParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.tag.FluidTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.GameRules;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeKeys;
import org.jetbrains.annotations.Nullable;

public class FoxEntity extends AnimalEntity {
   private static final TrackedData<Integer> TYPE;
   private static final TrackedData<Byte> FOX_FLAGS;
   private static final TrackedData<Optional<UUID>> OWNER;
   private static final TrackedData<Optional<UUID>> OTHER_TRUSTED;
   private static final Predicate<ItemEntity> PICKABLE_DROP_FILTER;
   private static final Predicate<Entity> JUST_ATTACKED_SOMETHING_FILTER;
   private static final Predicate<Entity> CHICKEN_AND_RABBIT_FILTER;
   private static final Predicate<Entity> NOTICEABLE_PLAYER_FILTER;
   private Goal followChickenAndRabbitGoal;
   private Goal followBabyTurtleGoal;
   private Goal followFishGoal;
   private float headRollProgress;
   private float lastHeadRollProgress;
   private float extraRollingHeight;
   private float lastExtraRollingHeight;
   private int eatingTime;

   public FoxEntity(EntityType<? extends FoxEntity> entityType, World world) {
      super(entityType, world);
      this.lookControl = new FoxEntity.FoxLookControl();
      this.moveControl = new FoxEntity.FoxMoveControl();
      this.setPathfindingPenalty(PathNodeType.DANGER_OTHER, 0.0F);
      this.setPathfindingPenalty(PathNodeType.DAMAGE_OTHER, 0.0F);
      this.setCanPickUpLoot(true);
   }

   protected void initDataTracker() {
      super.initDataTracker();
      this.dataTracker.startTracking(OWNER, Optional.empty());
      this.dataTracker.startTracking(OTHER_TRUSTED, Optional.empty());
      this.dataTracker.startTracking(TYPE, 0);
      this.dataTracker.startTracking(FOX_FLAGS, (byte)0);
   }

   protected void initGoals() {
      this.followChickenAndRabbitGoal = new FollowTargetGoal(this, AnimalEntity.class, 10, false, false, (livingEntity) -> {
         return livingEntity instanceof ChickenEntity || livingEntity instanceof RabbitEntity;
      });
      this.followBabyTurtleGoal = new FollowTargetGoal(this, TurtleEntity.class, 10, false, false, TurtleEntity.BABY_TURTLE_ON_LAND_FILTER);
      this.followFishGoal = new FollowTargetGoal(this, FishEntity.class, 20, false, false, (livingEntity) -> {
         return livingEntity instanceof SchoolingFishEntity;
      });
      this.goalSelector.add(0, new FoxEntity.FoxSwimGoal());
      this.goalSelector.add(1, new FoxEntity.StopWanderingGoal());
      this.goalSelector.add(2, new FoxEntity.EscapeWhenNotAggressiveGoal(2.2D));
      this.goalSelector.add(3, new FoxEntity.MateGoal(1.0D));
      this.goalSelector.add(4, new FleeEntityGoal(this, PlayerEntity.class, 16.0F, 1.6D, 1.4D, (livingEntity) -> {
         return NOTICEABLE_PLAYER_FILTER.test(livingEntity) && !this.canTrust(livingEntity.getUuid()) && !this.isAggressive();
      }));
      this.goalSelector.add(4, new FleeEntityGoal(this, WolfEntity.class, 8.0F, 1.6D, 1.4D, (livingEntity) -> {
         return !((WolfEntity)livingEntity).isTamed() && !this.isAggressive();
      }));
      this.goalSelector.add(4, new FleeEntityGoal(this, PolarBearEntity.class, 8.0F, 1.6D, 1.4D, (livingEntity) -> {
         return !this.isAggressive();
      }));
      this.goalSelector.add(5, new FoxEntity.MoveToHuntGoal());
      this.goalSelector.add(6, new FoxEntity.JumpChasingGoal());
      this.goalSelector.add(6, new FoxEntity.AvoidDaylightGoal(1.25D));
      this.goalSelector.add(7, new FoxEntity.AttackGoal(1.2000000476837158D, true));
      this.goalSelector.add(7, new FoxEntity.DelayedCalmDownGoal());
      this.goalSelector.add(8, new FoxEntity.FollowParentGoal(this, 1.25D));
      this.goalSelector.add(9, new FoxEntity.GoToVillageGoal(32, 200));
      this.goalSelector.add(10, new FoxEntity.EatSweetBerriesGoal(1.2000000476837158D, 12, 2));
      this.goalSelector.add(10, new PounceAtTargetGoal(this, 0.4F));
      this.goalSelector.add(11, new WanderAroundFarGoal(this, 1.0D));
      this.goalSelector.add(11, new FoxEntity.PickupItemGoal());
      this.goalSelector.add(12, new FoxEntity.LookAtEntityGoal(this, PlayerEntity.class, 24.0F));
      this.goalSelector.add(13, new FoxEntity.SitDownAndLookAroundGoal());
      this.targetSelector.add(3, new FoxEntity.DefendFriendGoal(LivingEntity.class, false, false, (livingEntity) -> {
         return JUST_ATTACKED_SOMETHING_FILTER.test(livingEntity) && !this.canTrust(livingEntity.getUuid());
      }));
   }

   public SoundEvent getEatSound(ItemStack stack) {
      return SoundEvents.ENTITY_FOX_EAT;
   }

   public void tickMovement() {
      if (!this.world.isClient && this.isAlive() && this.canMoveVoluntarily()) {
         ++this.eatingTime;
         ItemStack itemStack = this.getEquippedStack(EquipmentSlot.MAINHAND);
         if (this.canEat(itemStack)) {
            if (this.eatingTime > 600) {
               ItemStack itemStack2 = itemStack.finishUsing(this.world, this);
               if (!itemStack2.isEmpty()) {
                  this.equipStack(EquipmentSlot.MAINHAND, itemStack2);
               }

               this.eatingTime = 0;
            } else if (this.eatingTime > 560 && this.random.nextFloat() < 0.1F) {
               this.playSound(this.getEatSound(itemStack), 1.0F, 1.0F);
               this.world.sendEntityStatus(this, (byte)45);
            }
         }

         LivingEntity livingEntity = this.getTarget();
         if (livingEntity == null || !livingEntity.isAlive()) {
            this.setCrouching(false);
            this.setRollingHead(false);
         }
      }

      if (this.isSleeping() || this.isImmobile()) {
         this.jumping = false;
         this.sidewaysSpeed = 0.0F;
         this.forwardSpeed = 0.0F;
      }

      super.tickMovement();
      if (this.isAggressive() && this.random.nextFloat() < 0.05F) {
         this.playSound(SoundEvents.ENTITY_FOX_AGGRO, 1.0F, 1.0F);
      }

   }

   protected boolean isImmobile() {
      return this.isDead();
   }

   private boolean canEat(ItemStack stack) {
      return stack.getItem().isFood() && this.getTarget() == null && this.onGround && !this.isSleeping();
   }

   protected void initEquipment(LocalDifficulty difficulty) {
      if (this.random.nextFloat() < 0.2F) {
         float f = this.random.nextFloat();
         ItemStack itemStack6;
         if (f < 0.05F) {
            itemStack6 = new ItemStack(Items.EMERALD);
         } else if (f < 0.2F) {
            itemStack6 = new ItemStack(Items.EGG);
         } else if (f < 0.4F) {
            itemStack6 = this.random.nextBoolean() ? new ItemStack(Items.RABBIT_FOOT) : new ItemStack(Items.RABBIT_HIDE);
         } else if (f < 0.6F) {
            itemStack6 = new ItemStack(Items.WHEAT);
         } else if (f < 0.8F) {
            itemStack6 = new ItemStack(Items.LEATHER);
         } else {
            itemStack6 = new ItemStack(Items.FEATHER);
         }

         this.equipStack(EquipmentSlot.MAINHAND, itemStack6);
      }

   }

   @Environment(EnvType.CLIENT)
   public void handleStatus(byte status) {
      if (status == 45) {
         ItemStack itemStack = this.getEquippedStack(EquipmentSlot.MAINHAND);
         if (!itemStack.isEmpty()) {
            for(int i = 0; i < 8; ++i) {
               Vec3d vec3d = (new Vec3d(((double)this.random.nextFloat() - 0.5D) * 0.1D, Math.random() * 0.1D + 0.1D, 0.0D)).rotateX(-this.pitch * 0.017453292F).rotateY(-this.yaw * 0.017453292F);
               this.world.addParticle(new ItemStackParticleEffect(ParticleTypes.ITEM, itemStack), this.getX() + this.getRotationVector().x / 2.0D, this.getY(), this.getZ() + this.getRotationVector().z / 2.0D, vec3d.x, vec3d.y + 0.05D, vec3d.z);
            }
         }
      } else {
         super.handleStatus(status);
      }

   }

   public static DefaultAttributeContainer.Builder createFoxAttributes() {
      return MobEntity.createMobAttributes().add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.30000001192092896D).add(EntityAttributes.GENERIC_MAX_HEALTH, 10.0D).add(EntityAttributes.GENERIC_FOLLOW_RANGE, 32.0D).add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 2.0D);
   }

   public FoxEntity createChild(ServerWorld serverWorld, PassiveEntity passiveEntity) {
      FoxEntity foxEntity = (FoxEntity)EntityType.FOX.create(serverWorld);
      foxEntity.setType(this.random.nextBoolean() ? this.getFoxType() : ((FoxEntity)passiveEntity).getFoxType());
      return foxEntity;
   }

   @Nullable
   public EntityData initialize(ServerWorldAccess world, LocalDifficulty difficulty, SpawnReason spawnReason, @Nullable EntityData entityData, @Nullable CompoundTag entityTag) {
      Optional<RegistryKey<Biome>> optional = world.getBiomeKey(this.getBlockPos());
      FoxEntity.Type type = FoxEntity.Type.fromBiome(optional);
      boolean bl = false;
      if (entityData instanceof FoxEntity.FoxData) {
         type = ((FoxEntity.FoxData)entityData).type;
         if (((FoxEntity.FoxData)entityData).getSpawnedCount() >= 2) {
            bl = true;
         }
      } else {
         entityData = new FoxEntity.FoxData(type);
      }

      this.setType(type);
      if (bl) {
         this.setBreedingAge(-24000);
      }

      if (world instanceof ServerWorld) {
         this.addTypeSpecificGoals();
      }

      this.initEquipment(difficulty);
      return super.initialize(world, difficulty, spawnReason, (EntityData)entityData, entityTag);
   }

   private void addTypeSpecificGoals() {
      if (this.getFoxType() == FoxEntity.Type.RED) {
         this.targetSelector.add(4, this.followChickenAndRabbitGoal);
         this.targetSelector.add(4, this.followBabyTurtleGoal);
         this.targetSelector.add(6, this.followFishGoal);
      } else {
         this.targetSelector.add(4, this.followFishGoal);
         this.targetSelector.add(6, this.followChickenAndRabbitGoal);
         this.targetSelector.add(6, this.followBabyTurtleGoal);
      }

   }

   protected void eat(PlayerEntity player, ItemStack stack) {
      if (this.isBreedingItem(stack)) {
         this.playSound(this.getEatSound(stack), 1.0F, 1.0F);
      }

      super.eat(player, stack);
   }

   protected float getActiveEyeHeight(EntityPose pose, EntityDimensions dimensions) {
      return this.isBaby() ? dimensions.height * 0.85F : 0.4F;
   }

   public FoxEntity.Type getFoxType() {
      return FoxEntity.Type.fromId((Integer)this.dataTracker.get(TYPE));
   }

   private void setType(FoxEntity.Type type) {
      this.dataTracker.set(TYPE, type.getId());
   }

   private List<UUID> getTrustedUuids() {
      List<UUID> list = Lists.newArrayList();
      list.add(((Optional)this.dataTracker.get(OWNER)).orElse((Object)null));
      list.add(((Optional)this.dataTracker.get(OTHER_TRUSTED)).orElse((Object)null));
      return list;
   }

   private void addTrustedUuid(@Nullable UUID uuid) {
      if (((Optional)this.dataTracker.get(OWNER)).isPresent()) {
         this.dataTracker.set(OTHER_TRUSTED, Optional.ofNullable(uuid));
      } else {
         this.dataTracker.set(OWNER, Optional.ofNullable(uuid));
      }

   }

   public void writeCustomDataToTag(CompoundTag tag) {
      super.writeCustomDataToTag(tag);
      List<UUID> list = this.getTrustedUuids();
      ListTag listTag = new ListTag();
      Iterator var4 = list.iterator();

      while(var4.hasNext()) {
         UUID uUID = (UUID)var4.next();
         if (uUID != null) {
            listTag.add(NbtHelper.fromUuid(uUID));
         }
      }

      tag.put("Trusted", listTag);
      tag.putBoolean("Sleeping", this.isSleeping());
      tag.putString("Type", this.getFoxType().getKey());
      tag.putBoolean("Sitting", this.isSitting());
      tag.putBoolean("Crouching", this.isInSneakingPose());
   }

   public void readCustomDataFromTag(CompoundTag tag) {
      super.readCustomDataFromTag(tag);
      ListTag listTag = tag.getList("Trusted", 11);

      for(int i = 0; i < listTag.size(); ++i) {
         this.addTrustedUuid(NbtHelper.toUuid(listTag.get(i)));
      }

      this.setSleeping(tag.getBoolean("Sleeping"));
      this.setType(FoxEntity.Type.byName(tag.getString("Type")));
      this.setSitting(tag.getBoolean("Sitting"));
      this.setCrouching(tag.getBoolean("Crouching"));
      if (this.world instanceof ServerWorld) {
         this.addTypeSpecificGoals();
      }

   }

   public boolean isSitting() {
      return this.getFoxFlag(1);
   }

   public void setSitting(boolean sitting) {
      this.setFoxFlag(1, sitting);
   }

   public boolean isWalking() {
      return this.getFoxFlag(64);
   }

   private void setWalking(boolean walking) {
      this.setFoxFlag(64, walking);
   }

   private boolean isAggressive() {
      return this.getFoxFlag(128);
   }

   private void setAggressive(boolean aggressive) {
      this.setFoxFlag(128, aggressive);
   }

   public boolean isSleeping() {
      return this.getFoxFlag(32);
   }

   private void setSleeping(boolean sleeping) {
      this.setFoxFlag(32, sleeping);
   }

   private void setFoxFlag(int mask, boolean value) {
      if (value) {
         this.dataTracker.set(FOX_FLAGS, (byte)((Byte)this.dataTracker.get(FOX_FLAGS) | mask));
      } else {
         this.dataTracker.set(FOX_FLAGS, (byte)((Byte)this.dataTracker.get(FOX_FLAGS) & ~mask));
      }

   }

   private boolean getFoxFlag(int bitmask) {
      return ((Byte)this.dataTracker.get(FOX_FLAGS) & bitmask) != 0;
   }

   public boolean canEquip(ItemStack stack) {
      EquipmentSlot equipmentSlot = MobEntity.getPreferredEquipmentSlot(stack);
      if (!this.getEquippedStack(equipmentSlot).isEmpty()) {
         return false;
      } else {
         return equipmentSlot == EquipmentSlot.MAINHAND && super.canEquip(stack);
      }
   }

   public boolean canPickupItem(ItemStack stack) {
      Item item = stack.getItem();
      ItemStack itemStack = this.getEquippedStack(EquipmentSlot.MAINHAND);
      return itemStack.isEmpty() || this.eatingTime > 0 && item.isFood() && !itemStack.getItem().isFood();
   }

   private void spit(ItemStack stack) {
      if (!stack.isEmpty() && !this.world.isClient) {
         ItemEntity itemEntity = new ItemEntity(this.world, this.getX() + this.getRotationVector().x, this.getY() + 1.0D, this.getZ() + this.getRotationVector().z, stack);
         itemEntity.setPickupDelay(40);
         itemEntity.setThrower(this.getUuid());
         this.playSound(SoundEvents.ENTITY_FOX_SPIT, 1.0F, 1.0F);
         this.world.spawnEntity(itemEntity);
      }
   }

   private void dropItem(ItemStack stack) {
      ItemEntity itemEntity = new ItemEntity(this.world, this.getX(), this.getY(), this.getZ(), stack);
      this.world.spawnEntity(itemEntity);
   }

   protected void loot(ItemEntity item) {
      ItemStack itemStack = item.getStack();
      if (this.canPickupItem(itemStack)) {
         int i = itemStack.getCount();
         if (i > 1) {
            this.dropItem(itemStack.split(i - 1));
         }

         this.spit(this.getEquippedStack(EquipmentSlot.MAINHAND));
         this.method_29499(item);
         this.equipStack(EquipmentSlot.MAINHAND, itemStack.split(1));
         this.handDropChances[EquipmentSlot.MAINHAND.getEntitySlotId()] = 2.0F;
         this.sendPickup(item, itemStack.getCount());
         item.remove();
         this.eatingTime = 0;
      }

   }

   public void tick() {
      super.tick();
      if (this.canMoveVoluntarily()) {
         boolean bl = this.isTouchingWater();
         if (bl || this.getTarget() != null || this.world.isThundering()) {
            this.stopSleeping();
         }

         if (bl || this.isSleeping()) {
            this.setSitting(false);
         }

         if (this.isWalking() && this.world.random.nextFloat() < 0.2F) {
            BlockPos blockPos = this.getBlockPos();
            BlockState blockState = this.world.getBlockState(blockPos);
            this.world.syncWorldEvent(2001, blockPos, Block.getRawIdFromState(blockState));
         }
      }

      this.lastHeadRollProgress = this.headRollProgress;
      if (this.isRollingHead()) {
         this.headRollProgress += (1.0F - this.headRollProgress) * 0.4F;
      } else {
         this.headRollProgress += (0.0F - this.headRollProgress) * 0.4F;
      }

      this.lastExtraRollingHeight = this.extraRollingHeight;
      if (this.isInSneakingPose()) {
         this.extraRollingHeight += 0.2F;
         if (this.extraRollingHeight > 3.0F) {
            this.extraRollingHeight = 3.0F;
         }
      } else {
         this.extraRollingHeight = 0.0F;
      }

   }

   public boolean isBreedingItem(ItemStack stack) {
      return stack.getItem() == Items.SWEET_BERRIES;
   }

   protected void onPlayerSpawnedChild(PlayerEntity player, MobEntity child) {
      ((FoxEntity)child).addTrustedUuid(player.getUuid());
   }

   public boolean isChasing() {
      return this.getFoxFlag(16);
   }

   public void setChasing(boolean chasing) {
      this.setFoxFlag(16, chasing);
   }

   public boolean isFullyCrouched() {
      return this.extraRollingHeight == 3.0F;
   }

   public void setCrouching(boolean crouching) {
      this.setFoxFlag(4, crouching);
   }

   public boolean isInSneakingPose() {
      return this.getFoxFlag(4);
   }

   public void setRollingHead(boolean rollingHead) {
      this.setFoxFlag(8, rollingHead);
   }

   public boolean isRollingHead() {
      return this.getFoxFlag(8);
   }

   @Environment(EnvType.CLIENT)
   public float getHeadRoll(float tickDelta) {
      return MathHelper.lerp(tickDelta, this.lastHeadRollProgress, this.headRollProgress) * 0.11F * 3.1415927F;
   }

   @Environment(EnvType.CLIENT)
   public float getBodyRotationHeightOffset(float tickDelta) {
      return MathHelper.lerp(tickDelta, this.lastExtraRollingHeight, this.extraRollingHeight);
   }

   public void setTarget(@Nullable LivingEntity target) {
      if (this.isAggressive() && target == null) {
         this.setAggressive(false);
      }

      super.setTarget(target);
   }

   protected int computeFallDamage(float fallDistance, float damageMultiplier) {
      return MathHelper.ceil((fallDistance - 5.0F) * damageMultiplier);
   }

   private void stopSleeping() {
      this.setSleeping(false);
   }

   private void stopActions() {
      this.setRollingHead(false);
      this.setCrouching(false);
      this.setSitting(false);
      this.setSleeping(false);
      this.setAggressive(false);
      this.setWalking(false);
   }

   private boolean wantsToPickupItem() {
      return !this.isSleeping() && !this.isSitting() && !this.isWalking();
   }

   public void playAmbientSound() {
      SoundEvent soundEvent = this.getAmbientSound();
      if (soundEvent == SoundEvents.ENTITY_FOX_SCREECH) {
         this.playSound(soundEvent, 2.0F, this.getSoundPitch());
      } else {
         super.playAmbientSound();
      }

   }

   @Nullable
   protected SoundEvent getAmbientSound() {
      if (this.isSleeping()) {
         return SoundEvents.ENTITY_FOX_SLEEP;
      } else {
         if (!this.world.isDay() && this.random.nextFloat() < 0.1F) {
            List<PlayerEntity> list = this.world.getEntitiesByClass(PlayerEntity.class, this.getBoundingBox().expand(16.0D, 16.0D, 16.0D), EntityPredicates.EXCEPT_SPECTATOR);
            if (list.isEmpty()) {
               return SoundEvents.ENTITY_FOX_SCREECH;
            }
         }

         return SoundEvents.ENTITY_FOX_AMBIENT;
      }
   }

   @Nullable
   protected SoundEvent getHurtSound(DamageSource source) {
      return SoundEvents.ENTITY_FOX_HURT;
   }

   @Nullable
   protected SoundEvent getDeathSound() {
      return SoundEvents.ENTITY_FOX_DEATH;
   }

   private boolean canTrust(UUID uuid) {
      return this.getTrustedUuids().contains(uuid);
   }

   protected void drop(DamageSource source) {
      ItemStack itemStack = this.getEquippedStack(EquipmentSlot.MAINHAND);
      if (!itemStack.isEmpty()) {
         this.dropStack(itemStack);
         this.equipStack(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
      }

      super.drop(source);
   }

   public static boolean canJumpChase(FoxEntity fox, LivingEntity chasedEntity) {
      double d = chasedEntity.getZ() - fox.getZ();
      double e = chasedEntity.getX() - fox.getX();
      double f = d / e;
      int i = true;

      for(int j = 0; j < 6; ++j) {
         double g = f == 0.0D ? 0.0D : d * (double)((float)j / 6.0F);
         double h = f == 0.0D ? e * (double)((float)j / 6.0F) : g / f;

         for(int k = 1; k < 4; ++k) {
            if (!fox.world.getBlockState(new BlockPos(fox.getX() + h, fox.getY() + (double)k, fox.getZ() + g)).getMaterial().isReplaceable()) {
               return false;
            }
         }
      }

      return true;
   }

   @Environment(EnvType.CLIENT)
   public Vec3d method_29919() {
      return new Vec3d(0.0D, (double)(0.55F * this.getStandingEyeHeight()), (double)(this.getWidth() * 0.4F));
   }

   static {
      TYPE = DataTracker.registerData(FoxEntity.class, TrackedDataHandlerRegistry.INTEGER);
      FOX_FLAGS = DataTracker.registerData(FoxEntity.class, TrackedDataHandlerRegistry.BYTE);
      OWNER = DataTracker.registerData(FoxEntity.class, TrackedDataHandlerRegistry.OPTIONAL_UUID);
      OTHER_TRUSTED = DataTracker.registerData(FoxEntity.class, TrackedDataHandlerRegistry.OPTIONAL_UUID);
      PICKABLE_DROP_FILTER = (itemEntity) -> {
         return !itemEntity.cannotPickup() && itemEntity.isAlive();
      };
      JUST_ATTACKED_SOMETHING_FILTER = (entity) -> {
         if (!(entity instanceof LivingEntity)) {
            return false;
         } else {
            LivingEntity livingEntity = (LivingEntity)entity;
            return livingEntity.getAttacking() != null && livingEntity.getLastAttackTime() < livingEntity.age + 600;
         }
      };
      CHICKEN_AND_RABBIT_FILTER = (entity) -> {
         return entity instanceof ChickenEntity || entity instanceof RabbitEntity;
      };
      NOTICEABLE_PLAYER_FILTER = (entity) -> {
         return !entity.isSneaky() && EntityPredicates.EXCEPT_CREATIVE_OR_SPECTATOR.test(entity);
      };
   }

   class LookAtEntityGoal extends net.minecraft.entity.ai.goal.LookAtEntityGoal {
      public LookAtEntityGoal(MobEntity fox, Class<? extends LivingEntity> targetType, float range) {
         super(fox, targetType, range);
      }

      public boolean canStart() {
         return super.canStart() && !FoxEntity.this.isWalking() && !FoxEntity.this.isRollingHead();
      }

      public boolean shouldContinue() {
         return super.shouldContinue() && !FoxEntity.this.isWalking() && !FoxEntity.this.isRollingHead();
      }
   }

   class FollowParentGoal extends net.minecraft.entity.ai.goal.FollowParentGoal {
      private final FoxEntity fox;

      public FollowParentGoal(FoxEntity fox, double speed) {
         super(fox, speed);
         this.fox = fox;
      }

      public boolean canStart() {
         return !this.fox.isAggressive() && super.canStart();
      }

      public boolean shouldContinue() {
         return !this.fox.isAggressive() && super.shouldContinue();
      }

      public void start() {
         this.fox.stopActions();
         super.start();
      }
   }

   public class FoxLookControl extends LookControl {
      public FoxLookControl() {
         super(FoxEntity.this);
      }

      public void tick() {
         if (!FoxEntity.this.isSleeping()) {
            super.tick();
         }

      }

      protected boolean shouldStayHorizontal() {
         return !FoxEntity.this.isChasing() && !FoxEntity.this.isInSneakingPose() && !FoxEntity.this.isRollingHead() & !FoxEntity.this.isWalking();
      }
   }

   public class JumpChasingGoal extends DiveJumpingGoal {
      public boolean canStart() {
         if (!FoxEntity.this.isFullyCrouched()) {
            return false;
         } else {
            LivingEntity livingEntity = FoxEntity.this.getTarget();
            if (livingEntity != null && livingEntity.isAlive()) {
               if (livingEntity.getMovementDirection() != livingEntity.getHorizontalFacing()) {
                  return false;
               } else {
                  boolean bl = FoxEntity.canJumpChase(FoxEntity.this, livingEntity);
                  if (!bl) {
                     FoxEntity.this.getNavigation().findPathTo((Entity)livingEntity, 0);
                     FoxEntity.this.setCrouching(false);
                     FoxEntity.this.setRollingHead(false);
                  }

                  return bl;
               }
            } else {
               return false;
            }
         }
      }

      public boolean shouldContinue() {
         LivingEntity livingEntity = FoxEntity.this.getTarget();
         if (livingEntity != null && livingEntity.isAlive()) {
            double d = FoxEntity.this.getVelocity().y;
            return (!(d * d < 0.05000000074505806D) || !(Math.abs(FoxEntity.this.pitch) < 15.0F) || !FoxEntity.this.onGround) && !FoxEntity.this.isWalking();
         } else {
            return false;
         }
      }

      public boolean canStop() {
         return false;
      }

      public void start() {
         FoxEntity.this.setJumping(true);
         FoxEntity.this.setChasing(true);
         FoxEntity.this.setRollingHead(false);
         LivingEntity livingEntity = FoxEntity.this.getTarget();
         FoxEntity.this.getLookControl().lookAt(livingEntity, 60.0F, 30.0F);
         Vec3d vec3d = (new Vec3d(livingEntity.getX() - FoxEntity.this.getX(), livingEntity.getY() - FoxEntity.this.getY(), livingEntity.getZ() - FoxEntity.this.getZ())).normalize();
         FoxEntity.this.setVelocity(FoxEntity.this.getVelocity().add(vec3d.x * 0.8D, 0.9D, vec3d.z * 0.8D));
         FoxEntity.this.getNavigation().stop();
      }

      public void stop() {
         FoxEntity.this.setCrouching(false);
         FoxEntity.this.extraRollingHeight = 0.0F;
         FoxEntity.this.lastExtraRollingHeight = 0.0F;
         FoxEntity.this.setRollingHead(false);
         FoxEntity.this.setChasing(false);
      }

      public void tick() {
         LivingEntity livingEntity = FoxEntity.this.getTarget();
         if (livingEntity != null) {
            FoxEntity.this.getLookControl().lookAt(livingEntity, 60.0F, 30.0F);
         }

         if (!FoxEntity.this.isWalking()) {
            Vec3d vec3d = FoxEntity.this.getVelocity();
            if (vec3d.y * vec3d.y < 0.029999999329447746D && FoxEntity.this.pitch != 0.0F) {
               FoxEntity.this.pitch = MathHelper.lerpAngle(FoxEntity.this.pitch, 0.0F, 0.2F);
            } else {
               double d = Math.sqrt(Entity.squaredHorizontalLength(vec3d));
               double e = Math.signum(-vec3d.y) * Math.acos(d / vec3d.length()) * 57.2957763671875D;
               FoxEntity.this.pitch = (float)e;
            }
         }

         if (livingEntity != null && FoxEntity.this.distanceTo(livingEntity) <= 2.0F) {
            FoxEntity.this.tryAttack(livingEntity);
         } else if (FoxEntity.this.pitch > 0.0F && FoxEntity.this.onGround && (float)FoxEntity.this.getVelocity().y != 0.0F && FoxEntity.this.world.getBlockState(FoxEntity.this.getBlockPos()).isOf(Blocks.SNOW)) {
            FoxEntity.this.pitch = 60.0F;
            FoxEntity.this.setTarget((LivingEntity)null);
            FoxEntity.this.setWalking(true);
         }

      }
   }

   class FoxSwimGoal extends SwimGoal {
      public FoxSwimGoal() {
         super(FoxEntity.this);
      }

      public void start() {
         super.start();
         FoxEntity.this.stopActions();
      }

      public boolean canStart() {
         return FoxEntity.this.isTouchingWater() && FoxEntity.this.getFluidHeight(FluidTags.WATER) > 0.25D || FoxEntity.this.isInLava();
      }
   }

   class GoToVillageGoal extends net.minecraft.entity.ai.goal.GoToVillageGoal {
      public GoToVillageGoal(int unused, int searchRange) {
         super(FoxEntity.this, searchRange);
      }

      public void start() {
         FoxEntity.this.stopActions();
         super.start();
      }

      public boolean canStart() {
         return super.canStart() && this.canGoToVillage();
      }

      public boolean shouldContinue() {
         return super.shouldContinue() && this.canGoToVillage();
      }

      private boolean canGoToVillage() {
         return !FoxEntity.this.isSleeping() && !FoxEntity.this.isSitting() && !FoxEntity.this.isAggressive() && FoxEntity.this.getTarget() == null;
      }
   }

   class EscapeWhenNotAggressiveGoal extends EscapeDangerGoal {
      public EscapeWhenNotAggressiveGoal(double speed) {
         super(FoxEntity.this, speed);
      }

      public boolean canStart() {
         return !FoxEntity.this.isAggressive() && super.canStart();
      }
   }

   class StopWanderingGoal extends Goal {
      int timer;

      public StopWanderingGoal() {
         this.setControls(EnumSet.of(Goal.Control.LOOK, Goal.Control.JUMP, Goal.Control.MOVE));
      }

      public boolean canStart() {
         return FoxEntity.this.isWalking();
      }

      public boolean shouldContinue() {
         return this.canStart() && this.timer > 0;
      }

      public void start() {
         this.timer = 40;
      }

      public void stop() {
         FoxEntity.this.setWalking(false);
      }

      public void tick() {
         --this.timer;
      }
   }

   public static class FoxData extends PassiveEntity.PassiveData {
      public final FoxEntity.Type type;

      public FoxData(FoxEntity.Type type) {
         super(false);
         this.type = type;
      }
   }

   public class EatSweetBerriesGoal extends MoveToTargetPosGoal {
      protected int timer;

      public EatSweetBerriesGoal(double speed, int range, int maxYDifference) {
         super(FoxEntity.this, speed, range, maxYDifference);
      }

      public double getDesiredSquaredDistanceToTarget() {
         return 2.0D;
      }

      public boolean shouldResetPath() {
         return this.tryingTime % 100 == 0;
      }

      protected boolean isTargetPos(WorldView world, BlockPos pos) {
         BlockState blockState = world.getBlockState(pos);
         return blockState.isOf(Blocks.SWEET_BERRY_BUSH) && (Integer)blockState.get(SweetBerryBushBlock.AGE) >= 2;
      }

      public void tick() {
         if (this.hasReached()) {
            if (this.timer >= 40) {
               this.eatSweetBerry();
            } else {
               ++this.timer;
            }
         } else if (!this.hasReached() && FoxEntity.this.random.nextFloat() < 0.05F) {
            FoxEntity.this.playSound(SoundEvents.ENTITY_FOX_SNIFF, 1.0F, 1.0F);
         }

         super.tick();
      }

      protected void eatSweetBerry() {
         if (FoxEntity.this.world.getGameRules().getBoolean(GameRules.DO_MOB_GRIEFING)) {
            BlockState blockState = FoxEntity.this.world.getBlockState(this.targetPos);
            if (blockState.isOf(Blocks.SWEET_BERRY_BUSH)) {
               int i = (Integer)blockState.get(SweetBerryBushBlock.AGE);
               blockState.with(SweetBerryBushBlock.AGE, 1);
               int j = 1 + FoxEntity.this.world.random.nextInt(2) + (i == 3 ? 1 : 0);
               ItemStack itemStack = FoxEntity.this.getEquippedStack(EquipmentSlot.MAINHAND);
               if (itemStack.isEmpty()) {
                  FoxEntity.this.equipStack(EquipmentSlot.MAINHAND, new ItemStack(Items.SWEET_BERRIES));
                  --j;
               }

               if (j > 0) {
                  Block.dropStack(FoxEntity.this.world, this.targetPos, new ItemStack(Items.SWEET_BERRIES, j));
               }

               FoxEntity.this.playSound(SoundEvents.ITEM_SWEET_BERRIES_PICK_FROM_BUSH, 1.0F, 1.0F);
               FoxEntity.this.world.setBlockState(this.targetPos, (BlockState)blockState.with(SweetBerryBushBlock.AGE, 1), 2);
            }
         }
      }

      public boolean canStart() {
         return !FoxEntity.this.isSleeping() && super.canStart();
      }

      public void start() {
         this.timer = 0;
         FoxEntity.this.setSitting(false);
         super.start();
      }
   }

   class SitDownAndLookAroundGoal extends FoxEntity.CalmDownGoal {
      private double lookX;
      private double lookZ;
      private int timer;
      private int counter;

      public SitDownAndLookAroundGoal() {
         super(null);
         this.setControls(EnumSet.of(Goal.Control.MOVE, Goal.Control.LOOK));
      }

      public boolean canStart() {
         return FoxEntity.this.getAttacker() == null && FoxEntity.this.getRandom().nextFloat() < 0.02F && !FoxEntity.this.isSleeping() && FoxEntity.this.getTarget() == null && FoxEntity.this.getNavigation().isIdle() && !this.canCalmDown() && !FoxEntity.this.isChasing() && !FoxEntity.this.isInSneakingPose();
      }

      public boolean shouldContinue() {
         return this.counter > 0;
      }

      public void start() {
         this.chooseNewAngle();
         this.counter = 2 + FoxEntity.this.getRandom().nextInt(3);
         FoxEntity.this.setSitting(true);
         FoxEntity.this.getNavigation().stop();
      }

      public void stop() {
         FoxEntity.this.setSitting(false);
      }

      public void tick() {
         --this.timer;
         if (this.timer <= 0) {
            --this.counter;
            this.chooseNewAngle();
         }

         FoxEntity.this.getLookControl().lookAt(FoxEntity.this.getX() + this.lookX, FoxEntity.this.getEyeY(), FoxEntity.this.getZ() + this.lookZ, (float)FoxEntity.this.getBodyYawSpeed(), (float)FoxEntity.this.getLookPitchSpeed());
      }

      private void chooseNewAngle() {
         double d = 6.283185307179586D * FoxEntity.this.getRandom().nextDouble();
         this.lookX = Math.cos(d);
         this.lookZ = Math.sin(d);
         this.timer = 80 + FoxEntity.this.getRandom().nextInt(20);
      }
   }

   class DelayedCalmDownGoal extends FoxEntity.CalmDownGoal {
      private int timer;

      public DelayedCalmDownGoal() {
         super(null);
         this.timer = FoxEntity.this.random.nextInt(140);
         this.setControls(EnumSet.of(Goal.Control.MOVE, Goal.Control.LOOK, Goal.Control.JUMP));
      }

      public boolean canStart() {
         if (FoxEntity.this.sidewaysSpeed == 0.0F && FoxEntity.this.upwardSpeed == 0.0F && FoxEntity.this.forwardSpeed == 0.0F) {
            return this.canNotCalmDown() || FoxEntity.this.isSleeping();
         } else {
            return false;
         }
      }

      public boolean shouldContinue() {
         return this.canNotCalmDown();
      }

      private boolean canNotCalmDown() {
         if (this.timer > 0) {
            --this.timer;
            return false;
         } else {
            return FoxEntity.this.world.isDay() && this.isAtFavoredLocation() && !this.canCalmDown();
         }
      }

      public void stop() {
         this.timer = FoxEntity.this.random.nextInt(140);
         FoxEntity.this.stopActions();
      }

      public void start() {
         FoxEntity.this.setSitting(false);
         FoxEntity.this.setCrouching(false);
         FoxEntity.this.setRollingHead(false);
         FoxEntity.this.setJumping(false);
         FoxEntity.this.setSleeping(true);
         FoxEntity.this.getNavigation().stop();
         FoxEntity.this.getMoveControl().moveTo(FoxEntity.this.getX(), FoxEntity.this.getY(), FoxEntity.this.getZ(), 0.0D);
      }
   }

   abstract class CalmDownGoal extends Goal {
      private final TargetPredicate WORRIABLE_ENTITY_PREDICATE;

      private CalmDownGoal() {
         this.WORRIABLE_ENTITY_PREDICATE = (new TargetPredicate()).setBaseMaxDistance(12.0D).includeHidden().setPredicate(FoxEntity.this.new WorriableEntityFilter());
      }

      protected boolean isAtFavoredLocation() {
         BlockPos blockPos = new BlockPos(FoxEntity.this.getX(), FoxEntity.this.getBoundingBox().maxY, FoxEntity.this.getZ());
         return !FoxEntity.this.world.isSkyVisible(blockPos) && FoxEntity.this.getPathfindingFavor(blockPos) >= 0.0F;
      }

      protected boolean canCalmDown() {
         return !FoxEntity.this.world.getTargets(LivingEntity.class, this.WORRIABLE_ENTITY_PREDICATE, FoxEntity.this, FoxEntity.this.getBoundingBox().expand(12.0D, 6.0D, 12.0D)).isEmpty();
      }
   }

   public class WorriableEntityFilter implements Predicate<LivingEntity> {
      public boolean test(LivingEntity livingEntity) {
         if (livingEntity instanceof FoxEntity) {
            return false;
         } else if (!(livingEntity instanceof ChickenEntity) && !(livingEntity instanceof RabbitEntity) && !(livingEntity instanceof HostileEntity)) {
            if (livingEntity instanceof TameableEntity) {
               return !((TameableEntity)livingEntity).isTamed();
            } else if (livingEntity instanceof PlayerEntity && (livingEntity.isSpectator() || ((PlayerEntity)livingEntity).isCreative())) {
               return false;
            } else if (FoxEntity.this.canTrust(livingEntity.getUuid())) {
               return false;
            } else {
               return !livingEntity.isSleeping() && !livingEntity.isSneaky();
            }
         } else {
            return true;
         }
      }
   }

   class AvoidDaylightGoal extends EscapeSunlightGoal {
      private int timer = 100;

      public AvoidDaylightGoal(double speed) {
         super(FoxEntity.this, speed);
      }

      public boolean canStart() {
         if (!FoxEntity.this.isSleeping() && this.mob.getTarget() == null) {
            if (FoxEntity.this.world.isThundering()) {
               return true;
            } else if (this.timer > 0) {
               --this.timer;
               return false;
            } else {
               this.timer = 100;
               BlockPos blockPos = this.mob.getBlockPos();
               return FoxEntity.this.world.isDay() && FoxEntity.this.world.isSkyVisible(blockPos) && !((ServerWorld)FoxEntity.this.world).isNearOccupiedPointOfInterest(blockPos) && this.targetShadedPos();
            }
         } else {
            return false;
         }
      }

      public void start() {
         FoxEntity.this.stopActions();
         super.start();
      }
   }

   class DefendFriendGoal extends FollowTargetGoal<LivingEntity> {
      @Nullable
      private LivingEntity offender;
      private LivingEntity friend;
      private int lastAttackedTime;

      public DefendFriendGoal(Class<LivingEntity> targetEntityClass, boolean checkVisibility, boolean checkCanNavigate, @Nullable Predicate<LivingEntity> targetPredicate) {
         super(FoxEntity.this, targetEntityClass, 10, checkVisibility, checkCanNavigate, targetPredicate);
      }

      public boolean canStart() {
         if (this.reciprocalChance > 0 && this.mob.getRandom().nextInt(this.reciprocalChance) != 0) {
            return false;
         } else {
            Iterator var1 = FoxEntity.this.getTrustedUuids().iterator();

            while(var1.hasNext()) {
               UUID uUID = (UUID)var1.next();
               if (uUID != null && FoxEntity.this.world instanceof ServerWorld) {
                  Entity entity = ((ServerWorld)FoxEntity.this.world).getEntity(uUID);
                  if (entity instanceof LivingEntity) {
                     LivingEntity livingEntity = (LivingEntity)entity;
                     this.friend = livingEntity;
                     this.offender = livingEntity.getAttacker();
                     int i = livingEntity.getLastAttackedTime();
                     return i != this.lastAttackedTime && this.canTrack(this.offender, this.targetPredicate);
                  }
               }
            }

            return false;
         }
      }

      public void start() {
         this.setTargetEntity(this.offender);
         this.targetEntity = this.offender;
         if (this.friend != null) {
            this.lastAttackedTime = this.friend.getLastAttackedTime();
         }

         FoxEntity.this.playSound(SoundEvents.ENTITY_FOX_AGGRO, 1.0F, 1.0F);
         FoxEntity.this.setAggressive(true);
         FoxEntity.this.stopSleeping();
         super.start();
      }
   }

   class MateGoal extends AnimalMateGoal {
      public MateGoal(double chance) {
         super(FoxEntity.this, chance);
      }

      public void start() {
         ((FoxEntity)this.animal).stopActions();
         ((FoxEntity)this.mate).stopActions();
         super.start();
      }

      protected void breed() {
         ServerWorld serverWorld = (ServerWorld)this.world;
         FoxEntity foxEntity = (FoxEntity)this.animal.createChild(serverWorld, this.mate);
         if (foxEntity != null) {
            ServerPlayerEntity serverPlayerEntity = this.animal.getLovingPlayer();
            ServerPlayerEntity serverPlayerEntity2 = this.mate.getLovingPlayer();
            ServerPlayerEntity serverPlayerEntity3 = serverPlayerEntity;
            if (serverPlayerEntity != null) {
               foxEntity.addTrustedUuid(serverPlayerEntity.getUuid());
            } else {
               serverPlayerEntity3 = serverPlayerEntity2;
            }

            if (serverPlayerEntity2 != null && serverPlayerEntity != serverPlayerEntity2) {
               foxEntity.addTrustedUuid(serverPlayerEntity2.getUuid());
            }

            if (serverPlayerEntity3 != null) {
               serverPlayerEntity3.incrementStat(Stats.ANIMALS_BRED);
               Criteria.BRED_ANIMALS.trigger(serverPlayerEntity3, this.animal, this.mate, foxEntity);
            }

            this.animal.setBreedingAge(6000);
            this.mate.setBreedingAge(6000);
            this.animal.resetLoveTicks();
            this.mate.resetLoveTicks();
            foxEntity.setBreedingAge(-24000);
            foxEntity.refreshPositionAndAngles(this.animal.getX(), this.animal.getY(), this.animal.getZ(), 0.0F, 0.0F);
            serverWorld.spawnEntityAndPassengers(foxEntity);
            this.world.sendEntityStatus(this.animal, (byte)18);
            if (this.world.getGameRules().getBoolean(GameRules.DO_MOB_LOOT)) {
               this.world.spawnEntity(new ExperienceOrbEntity(this.world, this.animal.getX(), this.animal.getY(), this.animal.getZ(), this.animal.getRandom().nextInt(7) + 1));
            }

         }
      }
   }

   class AttackGoal extends MeleeAttackGoal {
      public AttackGoal(double speed, boolean pauseWhenIdle) {
         super(FoxEntity.this, speed, pauseWhenIdle);
      }

      protected void attack(LivingEntity target, double squaredDistance) {
         double d = this.getSquaredMaxAttackDistance(target);
         if (squaredDistance <= d && this.method_28347()) {
            this.method_28346();
            this.mob.tryAttack(target);
            FoxEntity.this.playSound(SoundEvents.ENTITY_FOX_BITE, 1.0F, 1.0F);
         }

      }

      public void start() {
         FoxEntity.this.setRollingHead(false);
         super.start();
      }

      public boolean canStart() {
         return !FoxEntity.this.isSitting() && !FoxEntity.this.isSleeping() && !FoxEntity.this.isInSneakingPose() && !FoxEntity.this.isWalking() && super.canStart();
      }
   }

   class MoveToHuntGoal extends Goal {
      public MoveToHuntGoal() {
         this.setControls(EnumSet.of(Goal.Control.MOVE, Goal.Control.LOOK));
      }

      public boolean canStart() {
         if (FoxEntity.this.isSleeping()) {
            return false;
         } else {
            LivingEntity livingEntity = FoxEntity.this.getTarget();
            return livingEntity != null && livingEntity.isAlive() && FoxEntity.CHICKEN_AND_RABBIT_FILTER.test(livingEntity) && FoxEntity.this.squaredDistanceTo(livingEntity) > 36.0D && !FoxEntity.this.isInSneakingPose() && !FoxEntity.this.isRollingHead() && !FoxEntity.this.jumping;
         }
      }

      public void start() {
         FoxEntity.this.setSitting(false);
         FoxEntity.this.setWalking(false);
      }

      public void stop() {
         LivingEntity livingEntity = FoxEntity.this.getTarget();
         if (livingEntity != null && FoxEntity.canJumpChase(FoxEntity.this, livingEntity)) {
            FoxEntity.this.setRollingHead(true);
            FoxEntity.this.setCrouching(true);
            FoxEntity.this.getNavigation().stop();
            FoxEntity.this.getLookControl().lookAt(livingEntity, (float)FoxEntity.this.getBodyYawSpeed(), (float)FoxEntity.this.getLookPitchSpeed());
         } else {
            FoxEntity.this.setRollingHead(false);
            FoxEntity.this.setCrouching(false);
         }

      }

      public void tick() {
         LivingEntity livingEntity = FoxEntity.this.getTarget();
         FoxEntity.this.getLookControl().lookAt(livingEntity, (float)FoxEntity.this.getBodyYawSpeed(), (float)FoxEntity.this.getLookPitchSpeed());
         if (FoxEntity.this.squaredDistanceTo(livingEntity) <= 36.0D) {
            FoxEntity.this.setRollingHead(true);
            FoxEntity.this.setCrouching(true);
            FoxEntity.this.getNavigation().stop();
         } else {
            FoxEntity.this.getNavigation().startMovingTo(livingEntity, 1.5D);
         }

      }
   }

   class FoxMoveControl extends MoveControl {
      public FoxMoveControl() {
         super(FoxEntity.this);
      }

      public void tick() {
         if (FoxEntity.this.wantsToPickupItem()) {
            super.tick();
         }

      }
   }

   class PickupItemGoal extends Goal {
      public PickupItemGoal() {
         this.setControls(EnumSet.of(Goal.Control.MOVE));
      }

      public boolean canStart() {
         if (!FoxEntity.this.getEquippedStack(EquipmentSlot.MAINHAND).isEmpty()) {
            return false;
         } else if (FoxEntity.this.getTarget() == null && FoxEntity.this.getAttacker() == null) {
            if (!FoxEntity.this.wantsToPickupItem()) {
               return false;
            } else if (FoxEntity.this.getRandom().nextInt(10) != 0) {
               return false;
            } else {
               List<ItemEntity> list = FoxEntity.this.world.getEntitiesByClass(ItemEntity.class, FoxEntity.this.getBoundingBox().expand(8.0D, 8.0D, 8.0D), FoxEntity.PICKABLE_DROP_FILTER);
               return !list.isEmpty() && FoxEntity.this.getEquippedStack(EquipmentSlot.MAINHAND).isEmpty();
            }
         } else {
            return false;
         }
      }

      public void tick() {
         List<ItemEntity> list = FoxEntity.this.world.getEntitiesByClass(ItemEntity.class, FoxEntity.this.getBoundingBox().expand(8.0D, 8.0D, 8.0D), FoxEntity.PICKABLE_DROP_FILTER);
         ItemStack itemStack = FoxEntity.this.getEquippedStack(EquipmentSlot.MAINHAND);
         if (itemStack.isEmpty() && !list.isEmpty()) {
            FoxEntity.this.getNavigation().startMovingTo((Entity)list.get(0), 1.2000000476837158D);
         }

      }

      public void start() {
         List<ItemEntity> list = FoxEntity.this.world.getEntitiesByClass(ItemEntity.class, FoxEntity.this.getBoundingBox().expand(8.0D, 8.0D, 8.0D), FoxEntity.PICKABLE_DROP_FILTER);
         if (!list.isEmpty()) {
            FoxEntity.this.getNavigation().startMovingTo((Entity)list.get(0), 1.2000000476837158D);
         }

      }
   }

   public static enum Type {
      RED(0, "red", new RegistryKey[]{BiomeKeys.TAIGA, BiomeKeys.TAIGA_HILLS, BiomeKeys.TAIGA_MOUNTAINS, BiomeKeys.GIANT_TREE_TAIGA, BiomeKeys.GIANT_SPRUCE_TAIGA, BiomeKeys.GIANT_TREE_TAIGA_HILLS, BiomeKeys.GIANT_SPRUCE_TAIGA_HILLS}),
      SNOW(1, "snow", new RegistryKey[]{BiomeKeys.SNOWY_TAIGA, BiomeKeys.SNOWY_TAIGA_HILLS, BiomeKeys.SNOWY_TAIGA_MOUNTAINS});

      private static final FoxEntity.Type[] TYPES = (FoxEntity.Type[])Arrays.stream(values()).sorted(Comparator.comparingInt(FoxEntity.Type::getId)).toArray((i) -> {
         return new FoxEntity.Type[i];
      });
      private static final Map<String, FoxEntity.Type> NAME_TYPE_MAP = (Map)Arrays.stream(values()).collect(Collectors.toMap(FoxEntity.Type::getKey, (type) -> {
         return type;
      }));
      private final int id;
      private final String key;
      private final List<RegistryKey<Biome>> biomes;

      private Type(int id, String key, RegistryKey<Biome>... registryKeys) {
         this.id = id;
         this.key = key;
         this.biomes = Arrays.asList(registryKeys);
      }

      public String getKey() {
         return this.key;
      }

      public int getId() {
         return this.id;
      }

      public static FoxEntity.Type byName(String name) {
         return (FoxEntity.Type)NAME_TYPE_MAP.getOrDefault(name, RED);
      }

      public static FoxEntity.Type fromId(int id) {
         if (id < 0 || id > TYPES.length) {
            id = 0;
         }

         return TYPES[id];
      }

      public static FoxEntity.Type fromBiome(Optional<RegistryKey<Biome>> optional) {
         return optional.isPresent() && SNOW.biomes.contains(optional.get()) ? SNOW : RED;
      }
   }
}
