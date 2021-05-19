package net.minecraft.entity.mob;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;
import java.util.List;
import java.util.Optional;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityGroup;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.Activity;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.sensor.Sensor;
import net.minecraft.entity.ai.brain.sensor.SensorType;
import net.minecraft.entity.ai.brain.task.ConditionalTask;
import net.minecraft.entity.ai.brain.task.FollowMobTask;
import net.minecraft.entity.ai.brain.task.ForgetAttackTargetTask;
import net.minecraft.entity.ai.brain.task.GoTowardsLookTarget;
import net.minecraft.entity.ai.brain.task.LookAroundTask;
import net.minecraft.entity.ai.brain.task.LookTargetUtil;
import net.minecraft.entity.ai.brain.task.MeleeAttackTask;
import net.minecraft.entity.ai.brain.task.RandomTask;
import net.minecraft.entity.ai.brain.task.RangedApproachTask;
import net.minecraft.entity.ai.brain.task.StrollTask;
import net.minecraft.entity.ai.brain.task.TimeLimitedTask;
import net.minecraft.entity.ai.brain.task.UpdateAttackTargetTask;
import net.minecraft.entity.ai.brain.task.WaitTask;
import net.minecraft.entity.ai.brain.task.WanderAroundTask;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.server.network.DebugInfoSender;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.IntRange;
import net.minecraft.world.World;

public class ZoglinEntity extends HostileEntity implements Monster, Hoglin {
   private static final TrackedData<Boolean> BABY;
   private int movementCooldownTicks;
   protected static final ImmutableList<? extends SensorType<? extends Sensor<? super ZoglinEntity>>> USED_SENSORS;
   protected static final ImmutableList<? extends MemoryModuleType<?>> USED_MEMORY_MODULES;

   public ZoglinEntity(EntityType<? extends ZoglinEntity> entityType, World world) {
      super(entityType, world);
      this.experiencePoints = 5;
   }

   protected Brain.Profile<ZoglinEntity> createBrainProfile() {
      return Brain.createProfile(USED_MEMORY_MODULES, USED_SENSORS);
   }

   protected Brain<?> deserializeBrain(Dynamic<?> dynamic) {
      Brain<ZoglinEntity> brain = this.createBrainProfile().deserialize(dynamic);
      method_26928(brain);
      method_26929(brain);
      method_26930(brain);
      brain.setCoreActivities(ImmutableSet.of(Activity.CORE));
      brain.setDefaultActivity(Activity.IDLE);
      brain.resetPossibleActivities();
      return brain;
   }

   private static void method_26928(Brain<ZoglinEntity> brain) {
      brain.setTaskList(Activity.CORE, 0, ImmutableList.of(new LookAroundTask(45, 90), new WanderAroundTask()));
   }

   private static void method_26929(Brain<ZoglinEntity> brain) {
      brain.setTaskList(Activity.IDLE, 10, ImmutableList.of(new UpdateAttackTargetTask(ZoglinEntity::method_26934), new TimeLimitedTask(new FollowMobTask(8.0F), IntRange.between(30, 60)), new RandomTask(ImmutableList.of(Pair.of(new StrollTask(0.4F), 2), Pair.of(new GoTowardsLookTarget(0.4F, 3), 2), Pair.of(new WaitTask(30, 60), 1)))));
   }

   private static void method_26930(Brain<ZoglinEntity> brain) {
      brain.setTaskList(Activity.FIGHT, 10, ImmutableList.of(new RangedApproachTask(1.0F), new ConditionalTask(ZoglinEntity::isAdult, new MeleeAttackTask(40)), new ConditionalTask(ZoglinEntity::isBaby, new MeleeAttackTask(15)), new ForgetAttackTargetTask()), MemoryModuleType.ATTACK_TARGET);
   }

   private Optional<? extends LivingEntity> method_26934() {
      return ((List)this.getBrain().getOptionalMemory(MemoryModuleType.VISIBLE_MOBS).orElse(ImmutableList.of())).stream().filter(ZoglinEntity::method_26936).findFirst();
   }

   private static boolean method_26936(LivingEntity livingEntity) {
      EntityType<?> entityType = livingEntity.getType();
      return entityType != EntityType.ZOGLIN && entityType != EntityType.CREEPER && EntityPredicates.EXCEPT_CREATIVE_SPECTATOR_OR_PEACEFUL.test(livingEntity);
   }

   protected void initDataTracker() {
      super.initDataTracker();
      this.dataTracker.startTracking(BABY, false);
   }

   public void onTrackedDataSet(TrackedData<?> data) {
      super.onTrackedDataSet(data);
      if (BABY.equals(data)) {
         this.calculateDimensions();
      }

   }

   public static DefaultAttributeContainer.Builder createZoglinAttributes() {
      return HostileEntity.createHostileAttributes().add(EntityAttributes.GENERIC_MAX_HEALTH, 40.0D).add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.30000001192092896D).add(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE, 0.6000000238418579D).add(EntityAttributes.GENERIC_ATTACK_KNOCKBACK, 1.0D).add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 6.0D);
   }

   public boolean isAdult() {
      return !this.isBaby();
   }

   public boolean tryAttack(Entity target) {
      if (!(target instanceof LivingEntity)) {
         return false;
      } else {
         this.movementCooldownTicks = 10;
         this.world.sendEntityStatus(this, (byte)4);
         this.playSound(SoundEvents.ENTITY_ZOGLIN_ATTACK, 1.0F, this.getSoundPitch());
         return Hoglin.tryAttack(this, (LivingEntity)target);
      }
   }

   public boolean canBeLeashedBy(PlayerEntity player) {
      return !this.isLeashed();
   }

   protected void knockback(LivingEntity target) {
      if (!this.isBaby()) {
         Hoglin.knockback(this, target);
      }

   }

   public double getMountedHeightOffset() {
      return (double)this.getHeight() - (this.isBaby() ? 0.2D : 0.15D);
   }

   public boolean damage(DamageSource source, float amount) {
      boolean bl = super.damage(source, amount);
      if (this.world.isClient) {
         return false;
      } else if (bl && source.getAttacker() instanceof LivingEntity) {
         LivingEntity livingEntity = (LivingEntity)source.getAttacker();
         if (EntityPredicates.EXCEPT_CREATIVE_SPECTATOR_OR_PEACEFUL.test(livingEntity) && !LookTargetUtil.isNewTargetTooFar(this, livingEntity, 4.0D)) {
            this.method_26938(livingEntity);
         }

         return bl;
      } else {
         return bl;
      }
   }

   private void method_26938(LivingEntity livingEntity) {
      this.brain.forget(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE);
      this.brain.remember(MemoryModuleType.ATTACK_TARGET, livingEntity, 200L);
   }

   public Brain<ZoglinEntity> getBrain() {
      return super.getBrain();
   }

   protected void method_26931() {
      Activity activity = (Activity)this.brain.getFirstPossibleNonCoreActivity().orElse((Object)null);
      this.brain.resetPossibleActivities((List)ImmutableList.of(Activity.FIGHT, Activity.IDLE));
      Activity activity2 = (Activity)this.brain.getFirstPossibleNonCoreActivity().orElse((Object)null);
      if (activity2 == Activity.FIGHT && activity != Activity.FIGHT) {
         this.playAngrySound();
      }

      this.setAttacking(this.brain.hasMemoryModule(MemoryModuleType.ATTACK_TARGET));
   }

   protected void mobTick() {
      this.world.getProfiler().push("zoglinBrain");
      this.getBrain().tick((ServerWorld)this.world, this);
      this.world.getProfiler().pop();
      this.method_26931();
   }

   public void setBaby(boolean baby) {
      this.getDataTracker().set(BABY, baby);
      if (!this.world.isClient && baby) {
         this.getAttributeInstance(EntityAttributes.GENERIC_ATTACK_DAMAGE).setBaseValue(0.5D);
      }

   }

   public boolean isBaby() {
      return (Boolean)this.getDataTracker().get(BABY);
   }

   public void tickMovement() {
      if (this.movementCooldownTicks > 0) {
         --this.movementCooldownTicks;
      }

      super.tickMovement();
   }

   @Environment(EnvType.CLIENT)
   public void handleStatus(byte status) {
      if (status == 4) {
         this.movementCooldownTicks = 10;
         this.playSound(SoundEvents.ENTITY_ZOGLIN_ATTACK, 1.0F, this.getSoundPitch());
      } else {
         super.handleStatus(status);
      }

   }

   @Environment(EnvType.CLIENT)
   public int getMovementCooldownTicks() {
      return this.movementCooldownTicks;
   }

   protected SoundEvent getAmbientSound() {
      if (this.world.isClient) {
         return null;
      } else {
         return this.brain.hasMemoryModule(MemoryModuleType.ATTACK_TARGET) ? SoundEvents.ENTITY_ZOGLIN_ANGRY : SoundEvents.ENTITY_ZOGLIN_AMBIENT;
      }
   }

   protected SoundEvent getHurtSound(DamageSource source) {
      return SoundEvents.ENTITY_ZOGLIN_HURT;
   }

   protected SoundEvent getDeathSound() {
      return SoundEvents.ENTITY_ZOGLIN_DEATH;
   }

   protected void playStepSound(BlockPos pos, BlockState state) {
      this.playSound(SoundEvents.ENTITY_ZOGLIN_STEP, 0.15F, 1.0F);
   }

   protected void playAngrySound() {
      this.playSound(SoundEvents.ENTITY_ZOGLIN_ANGRY, 1.0F, this.getSoundPitch());
   }

   protected void sendAiDebugData() {
      super.sendAiDebugData();
      DebugInfoSender.sendBrainDebugData(this);
   }

   public EntityGroup getGroup() {
      return EntityGroup.UNDEAD;
   }

   public void writeCustomDataToTag(CompoundTag tag) {
      super.writeCustomDataToTag(tag);
      if (this.isBaby()) {
         tag.putBoolean("IsBaby", true);
      }

   }

   public void readCustomDataFromTag(CompoundTag tag) {
      super.readCustomDataFromTag(tag);
      if (tag.getBoolean("IsBaby")) {
         this.setBaby(true);
      }

   }

   static {
      BABY = DataTracker.registerData(ZoglinEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
      USED_SENSORS = ImmutableList.of(SensorType.NEAREST_LIVING_ENTITIES, SensorType.NEAREST_PLAYERS);
      USED_MEMORY_MODULES = ImmutableList.of(MemoryModuleType.MOBS, MemoryModuleType.VISIBLE_MOBS, MemoryModuleType.NEAREST_VISIBLE_PLAYER, MemoryModuleType.NEAREST_VISIBLE_TARGETABLE_PLAYER, MemoryModuleType.LOOK_TARGET, MemoryModuleType.WALK_TARGET, MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE, MemoryModuleType.PATH, MemoryModuleType.ATTACK_TARGET, MemoryModuleType.ATTACK_COOLING_DOWN);
   }
}
