package net.minecraft.entity.mob;

import java.util.EnumSet;
import java.util.Random;
import java.util.function.Predicate;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityGroup;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.control.LookControl;
import net.minecraft.entity.ai.control.MoveControl;
import net.minecraft.entity.ai.goal.FollowTargetGoal;
import net.minecraft.entity.ai.goal.GoToWalkTargetGoal;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.LookAroundGoal;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.ai.goal.WanderAroundGoal;
import net.minecraft.entity.ai.pathing.EntityNavigation;
import net.minecraft.entity.ai.pathing.PathNodeType;
import net.minecraft.entity.ai.pathing.SwimNavigation;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.passive.SquidEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.tag.FluidTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Difficulty;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;
import org.jetbrains.annotations.Nullable;

public class GuardianEntity extends HostileEntity {
   private static final TrackedData<Boolean> SPIKES_RETRACTED;
   private static final TrackedData<Integer> BEAM_TARGET_ID;
   private float spikesExtension;
   private float prevSpikesExtension;
   private float spikesExtensionRate;
   private float tailAngle;
   private float prevTailAngle;
   private LivingEntity cachedBeamTarget;
   private int beamTicks;
   private boolean flopping;
   protected WanderAroundGoal wanderGoal;

   public GuardianEntity(EntityType<? extends GuardianEntity> entityType, World world) {
      super(entityType, world);
      this.experiencePoints = 10;
      this.setPathfindingPenalty(PathNodeType.WATER, 0.0F);
      this.moveControl = new GuardianEntity.GuardianMoveControl(this);
      this.spikesExtension = this.random.nextFloat();
      this.prevSpikesExtension = this.spikesExtension;
   }

   protected void initGoals() {
      GoToWalkTargetGoal goToWalkTargetGoal = new GoToWalkTargetGoal(this, 1.0D);
      this.wanderGoal = new WanderAroundGoal(this, 1.0D, 80);
      this.goalSelector.add(4, new GuardianEntity.FireBeamGoal(this));
      this.goalSelector.add(5, goToWalkTargetGoal);
      this.goalSelector.add(7, this.wanderGoal);
      this.goalSelector.add(8, new LookAtEntityGoal(this, PlayerEntity.class, 8.0F));
      this.goalSelector.add(8, new LookAtEntityGoal(this, GuardianEntity.class, 12.0F, 0.01F));
      this.goalSelector.add(9, new LookAroundGoal(this));
      this.wanderGoal.setControls(EnumSet.of(Goal.Control.MOVE, Goal.Control.LOOK));
      goToWalkTargetGoal.setControls(EnumSet.of(Goal.Control.MOVE, Goal.Control.LOOK));
      this.targetSelector.add(1, new FollowTargetGoal(this, LivingEntity.class, 10, true, false, new GuardianEntity.GuardianTargetPredicate(this)));
   }

   public static DefaultAttributeContainer.Builder createGuardianAttributes() {
      return HostileEntity.createHostileAttributes().add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 6.0D).add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.5D).add(EntityAttributes.GENERIC_FOLLOW_RANGE, 16.0D).add(EntityAttributes.GENERIC_MAX_HEALTH, 30.0D);
   }

   protected EntityNavigation createNavigation(World world) {
      return new SwimNavigation(this, world);
   }

   protected void initDataTracker() {
      super.initDataTracker();
      this.dataTracker.startTracking(SPIKES_RETRACTED, false);
      this.dataTracker.startTracking(BEAM_TARGET_ID, 0);
   }

   public boolean canBreatheInWater() {
      return true;
   }

   public EntityGroup getGroup() {
      return EntityGroup.AQUATIC;
   }

   public boolean areSpikesRetracted() {
      return (Boolean)this.dataTracker.get(SPIKES_RETRACTED);
   }

   private void setSpikesRetracted(boolean retracted) {
      this.dataTracker.set(SPIKES_RETRACTED, retracted);
   }

   public int getWarmupTime() {
      return 80;
   }

   private void setBeamTarget(int entityId) {
      this.dataTracker.set(BEAM_TARGET_ID, entityId);
   }

   public boolean hasBeamTarget() {
      return (Integer)this.dataTracker.get(BEAM_TARGET_ID) != 0;
   }

   @Nullable
   public LivingEntity getBeamTarget() {
      if (!this.hasBeamTarget()) {
         return null;
      } else if (this.world.isClient) {
         if (this.cachedBeamTarget != null) {
            return this.cachedBeamTarget;
         } else {
            Entity entity = this.world.getEntityById((Integer)this.dataTracker.get(BEAM_TARGET_ID));
            if (entity instanceof LivingEntity) {
               this.cachedBeamTarget = (LivingEntity)entity;
               return this.cachedBeamTarget;
            } else {
               return null;
            }
         }
      } else {
         return this.getTarget();
      }
   }

   public void onTrackedDataSet(TrackedData<?> data) {
      super.onTrackedDataSet(data);
      if (BEAM_TARGET_ID.equals(data)) {
         this.beamTicks = 0;
         this.cachedBeamTarget = null;
      }

   }

   public int getMinAmbientSoundDelay() {
      return 160;
   }

   protected SoundEvent getAmbientSound() {
      return this.isInsideWaterOrBubbleColumn() ? SoundEvents.ENTITY_GUARDIAN_AMBIENT : SoundEvents.ENTITY_GUARDIAN_AMBIENT_LAND;
   }

   protected SoundEvent getHurtSound(DamageSource source) {
      return this.isInsideWaterOrBubbleColumn() ? SoundEvents.ENTITY_GUARDIAN_HURT : SoundEvents.ENTITY_GUARDIAN_HURT_LAND;
   }

   protected SoundEvent getDeathSound() {
      return this.isInsideWaterOrBubbleColumn() ? SoundEvents.ENTITY_GUARDIAN_DEATH : SoundEvents.ENTITY_GUARDIAN_DEATH_LAND;
   }

   protected boolean canClimb() {
      return false;
   }

   protected float getActiveEyeHeight(EntityPose pose, EntityDimensions dimensions) {
      return dimensions.height * 0.5F;
   }

   public float getPathfindingFavor(BlockPos pos, WorldView world) {
      return world.getFluidState(pos).isIn(FluidTags.WATER) ? 10.0F + world.getBrightness(pos) - 0.5F : super.getPathfindingFavor(pos, world);
   }

   public void tickMovement() {
      if (this.isAlive()) {
         if (this.world.isClient) {
            this.prevSpikesExtension = this.spikesExtension;
            Vec3d vec3d2;
            if (!this.isTouchingWater()) {
               this.spikesExtensionRate = 2.0F;
               vec3d2 = this.getVelocity();
               if (vec3d2.y > 0.0D && this.flopping && !this.isSilent()) {
                  this.world.playSound(this.getX(), this.getY(), this.getZ(), this.getFlopSound(), this.getSoundCategory(), 1.0F, 1.0F, false);
               }

               this.flopping = vec3d2.y < 0.0D && this.world.isTopSolid(this.getBlockPos().down(), this);
            } else if (this.areSpikesRetracted()) {
               if (this.spikesExtensionRate < 0.5F) {
                  this.spikesExtensionRate = 4.0F;
               } else {
                  this.spikesExtensionRate += (0.5F - this.spikesExtensionRate) * 0.1F;
               }
            } else {
               this.spikesExtensionRate += (0.125F - this.spikesExtensionRate) * 0.2F;
            }

            this.spikesExtension += this.spikesExtensionRate;
            this.prevTailAngle = this.tailAngle;
            if (!this.isInsideWaterOrBubbleColumn()) {
               this.tailAngle = this.random.nextFloat();
            } else if (this.areSpikesRetracted()) {
               this.tailAngle += (0.0F - this.tailAngle) * 0.25F;
            } else {
               this.tailAngle += (1.0F - this.tailAngle) * 0.06F;
            }

            if (this.areSpikesRetracted() && this.isTouchingWater()) {
               vec3d2 = this.getRotationVec(0.0F);

               for(int i = 0; i < 2; ++i) {
                  this.world.addParticle(ParticleTypes.BUBBLE, this.getParticleX(0.5D) - vec3d2.x * 1.5D, this.getRandomBodyY() - vec3d2.y * 1.5D, this.getParticleZ(0.5D) - vec3d2.z * 1.5D, 0.0D, 0.0D, 0.0D);
               }
            }

            if (this.hasBeamTarget()) {
               if (this.beamTicks < this.getWarmupTime()) {
                  ++this.beamTicks;
               }

               LivingEntity livingEntity = this.getBeamTarget();
               if (livingEntity != null) {
                  this.getLookControl().lookAt(livingEntity, 90.0F, 90.0F);
                  this.getLookControl().tick();
                  double d = (double)this.getBeamProgress(0.0F);
                  double e = livingEntity.getX() - this.getX();
                  double f = livingEntity.getBodyY(0.5D) - this.getEyeY();
                  double g = livingEntity.getZ() - this.getZ();
                  double h = Math.sqrt(e * e + f * f + g * g);
                  e /= h;
                  f /= h;
                  g /= h;
                  double j = this.random.nextDouble();

                  while(j < h) {
                     j += 1.8D - d + this.random.nextDouble() * (1.7D - d);
                     this.world.addParticle(ParticleTypes.BUBBLE, this.getX() + e * j, this.getEyeY() + f * j, this.getZ() + g * j, 0.0D, 0.0D, 0.0D);
                  }
               }
            }
         }

         if (this.isInsideWaterOrBubbleColumn()) {
            this.setAir(300);
         } else if (this.onGround) {
            this.setVelocity(this.getVelocity().add((double)((this.random.nextFloat() * 2.0F - 1.0F) * 0.4F), 0.5D, (double)((this.random.nextFloat() * 2.0F - 1.0F) * 0.4F)));
            this.yaw = this.random.nextFloat() * 360.0F;
            this.onGround = false;
            this.velocityDirty = true;
         }

         if (this.hasBeamTarget()) {
            this.yaw = this.headYaw;
         }
      }

      super.tickMovement();
   }

   protected SoundEvent getFlopSound() {
      return SoundEvents.ENTITY_GUARDIAN_FLOP;
   }

   @Environment(EnvType.CLIENT)
   public float getSpikesExtension(float tickDelta) {
      return MathHelper.lerp(tickDelta, this.prevSpikesExtension, this.spikesExtension);
   }

   @Environment(EnvType.CLIENT)
   public float getTailAngle(float tickDelta) {
      return MathHelper.lerp(tickDelta, this.prevTailAngle, this.tailAngle);
   }

   public float getBeamProgress(float tickDelta) {
      return ((float)this.beamTicks + tickDelta) / (float)this.getWarmupTime();
   }

   public boolean canSpawn(WorldView world) {
      return world.intersectsEntities(this);
   }

   public static boolean canSpawn(EntityType<? extends GuardianEntity> type, WorldAccess world, SpawnReason spawnReason, BlockPos pos, Random random) {
      return (random.nextInt(20) == 0 || !world.isSkyVisibleAllowingSea(pos)) && world.getDifficulty() != Difficulty.PEACEFUL && (spawnReason == SpawnReason.SPAWNER || world.getFluidState(pos).isIn(FluidTags.WATER));
   }

   public boolean damage(DamageSource source, float amount) {
      if (!this.areSpikesRetracted() && !source.getMagic() && source.getSource() instanceof LivingEntity) {
         LivingEntity livingEntity = (LivingEntity)source.getSource();
         if (!source.isExplosive()) {
            livingEntity.damage(DamageSource.thorns(this), 2.0F);
         }
      }

      if (this.wanderGoal != null) {
         this.wanderGoal.ignoreChanceOnce();
      }

      return super.damage(source, amount);
   }

   public int getLookPitchSpeed() {
      return 180;
   }

   public void travel(Vec3d movementInput) {
      if (this.canMoveVoluntarily() && this.isTouchingWater()) {
         this.updateVelocity(0.1F, movementInput);
         this.move(MovementType.SELF, this.getVelocity());
         this.setVelocity(this.getVelocity().multiply(0.9D));
         if (!this.areSpikesRetracted() && this.getTarget() == null) {
            this.setVelocity(this.getVelocity().add(0.0D, -0.005D, 0.0D));
         }
      } else {
         super.travel(movementInput);
      }

   }

   static {
      SPIKES_RETRACTED = DataTracker.registerData(GuardianEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
      BEAM_TARGET_ID = DataTracker.registerData(GuardianEntity.class, TrackedDataHandlerRegistry.INTEGER);
   }

   static class GuardianMoveControl extends MoveControl {
      private final GuardianEntity guardian;

      public GuardianMoveControl(GuardianEntity guardian) {
         super(guardian);
         this.guardian = guardian;
      }

      public void tick() {
         if (this.state == MoveControl.State.MOVE_TO && !this.guardian.getNavigation().isIdle()) {
            Vec3d vec3d = new Vec3d(this.targetX - this.guardian.getX(), this.targetY - this.guardian.getY(), this.targetZ - this.guardian.getZ());
            double d = vec3d.length();
            double e = vec3d.x / d;
            double f = vec3d.y / d;
            double g = vec3d.z / d;
            float h = (float)(MathHelper.atan2(vec3d.z, vec3d.x) * 57.2957763671875D) - 90.0F;
            this.guardian.yaw = this.changeAngle(this.guardian.yaw, h, 90.0F);
            this.guardian.bodyYaw = this.guardian.yaw;
            float i = (float)(this.speed * this.guardian.getAttributeValue(EntityAttributes.GENERIC_MOVEMENT_SPEED));
            float j = MathHelper.lerp(0.125F, this.guardian.getMovementSpeed(), i);
            this.guardian.setMovementSpeed(j);
            double k = Math.sin((double)(this.guardian.age + this.guardian.getEntityId()) * 0.5D) * 0.05D;
            double l = Math.cos((double)(this.guardian.yaw * 0.017453292F));
            double m = Math.sin((double)(this.guardian.yaw * 0.017453292F));
            double n = Math.sin((double)(this.guardian.age + this.guardian.getEntityId()) * 0.75D) * 0.05D;
            this.guardian.setVelocity(this.guardian.getVelocity().add(k * l, n * (m + l) * 0.25D + (double)j * f * 0.1D, k * m));
            LookControl lookControl = this.guardian.getLookControl();
            double o = this.guardian.getX() + e * 2.0D;
            double p = this.guardian.getEyeY() + f / d;
            double q = this.guardian.getZ() + g * 2.0D;
            double r = lookControl.getLookX();
            double s = lookControl.getLookY();
            double t = lookControl.getLookZ();
            if (!lookControl.isActive()) {
               r = o;
               s = p;
               t = q;
            }

            this.guardian.getLookControl().lookAt(MathHelper.lerp(0.125D, r, o), MathHelper.lerp(0.125D, s, p), MathHelper.lerp(0.125D, t, q), 10.0F, 40.0F);
            this.guardian.setSpikesRetracted(true);
         } else {
            this.guardian.setMovementSpeed(0.0F);
            this.guardian.setSpikesRetracted(false);
         }
      }
   }

   static class FireBeamGoal extends Goal {
      private final GuardianEntity guardian;
      private int beamTicks;
      private final boolean elder;

      public FireBeamGoal(GuardianEntity guardian) {
         this.guardian = guardian;
         this.elder = guardian instanceof ElderGuardianEntity;
         this.setControls(EnumSet.of(Goal.Control.MOVE, Goal.Control.LOOK));
      }

      public boolean canStart() {
         LivingEntity livingEntity = this.guardian.getTarget();
         return livingEntity != null && livingEntity.isAlive();
      }

      public boolean shouldContinue() {
         return super.shouldContinue() && (this.elder || this.guardian.squaredDistanceTo(this.guardian.getTarget()) > 9.0D);
      }

      public void start() {
         this.beamTicks = -10;
         this.guardian.getNavigation().stop();
         this.guardian.getLookControl().lookAt(this.guardian.getTarget(), 90.0F, 90.0F);
         this.guardian.velocityDirty = true;
      }

      public void stop() {
         this.guardian.setBeamTarget(0);
         this.guardian.setTarget((LivingEntity)null);
         this.guardian.wanderGoal.ignoreChanceOnce();
      }

      public void tick() {
         LivingEntity livingEntity = this.guardian.getTarget();
         this.guardian.getNavigation().stop();
         this.guardian.getLookControl().lookAt(livingEntity, 90.0F, 90.0F);
         if (!this.guardian.canSee(livingEntity)) {
            this.guardian.setTarget((LivingEntity)null);
         } else {
            ++this.beamTicks;
            if (this.beamTicks == 0) {
               this.guardian.setBeamTarget(this.guardian.getTarget().getEntityId());
               if (!this.guardian.isSilent()) {
                  this.guardian.world.sendEntityStatus(this.guardian, (byte)21);
               }
            } else if (this.beamTicks >= this.guardian.getWarmupTime()) {
               float f = 1.0F;
               if (this.guardian.world.getDifficulty() == Difficulty.HARD) {
                  f += 2.0F;
               }

               if (this.elder) {
                  f += 2.0F;
               }

               livingEntity.damage(DamageSource.magic(this.guardian, this.guardian), f);
               livingEntity.damage(DamageSource.mob(this.guardian), (float)this.guardian.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE));
               this.guardian.setTarget((LivingEntity)null);
            }

            super.tick();
         }
      }
   }

   static class GuardianTargetPredicate implements Predicate<LivingEntity> {
      private final GuardianEntity owner;

      public GuardianTargetPredicate(GuardianEntity owner) {
         this.owner = owner;
      }

      public boolean test(@Nullable LivingEntity livingEntity) {
         return (livingEntity instanceof PlayerEntity || livingEntity instanceof SquidEntity) && livingEntity.squaredDistanceTo(this.owner) > 9.0D;
      }
   }
}
