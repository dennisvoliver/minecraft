package net.minecraft.entity.mob;

import java.util.Comparator;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityData;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityGroup;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.TargetPredicate;
import net.minecraft.entity.ai.control.BodyControl;
import net.minecraft.entity.ai.control.LookControl;
import net.minecraft.entity.ai.control.MoveControl;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.passive.CatEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Heightmap;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class PhantomEntity extends FlyingEntity implements Monster {
   private static final TrackedData<Integer> SIZE;
   private Vec3d targetPosition;
   private BlockPos circlingCenter;
   private PhantomEntity.PhantomMovementType movementType;

   public PhantomEntity(EntityType<? extends PhantomEntity> entityType, World world) {
      super(entityType, world);
      this.targetPosition = Vec3d.ZERO;
      this.circlingCenter = BlockPos.ORIGIN;
      this.movementType = PhantomEntity.PhantomMovementType.CIRCLE;
      this.experiencePoints = 5;
      this.moveControl = new PhantomEntity.PhantomMoveControl(this);
      this.lookControl = new PhantomEntity.PhantomLookControl(this);
   }

   protected BodyControl createBodyControl() {
      return new PhantomEntity.PhantomBodyControl(this);
   }

   protected void initGoals() {
      this.goalSelector.add(1, new PhantomEntity.StartAttackGoal());
      this.goalSelector.add(2, new PhantomEntity.SwoopMovementGoal());
      this.goalSelector.add(3, new PhantomEntity.CircleMovementGoal());
      this.targetSelector.add(1, new PhantomEntity.FindTargetGoal());
   }

   protected void initDataTracker() {
      super.initDataTracker();
      this.dataTracker.startTracking(SIZE, 0);
   }

   public void setPhantomSize(int size) {
      this.dataTracker.set(SIZE, MathHelper.clamp(size, 0, 64));
   }

   private void onSizeChanged() {
      this.calculateDimensions();
      this.getAttributeInstance(EntityAttributes.GENERIC_ATTACK_DAMAGE).setBaseValue((double)(6 + this.getPhantomSize()));
   }

   public int getPhantomSize() {
      return (Integer)this.dataTracker.get(SIZE);
   }

   protected float getActiveEyeHeight(EntityPose pose, EntityDimensions dimensions) {
      return dimensions.height * 0.35F;
   }

   public void onTrackedDataSet(TrackedData<?> data) {
      if (SIZE.equals(data)) {
         this.onSizeChanged();
      }

      super.onTrackedDataSet(data);
   }

   protected boolean isDisallowedInPeaceful() {
      return true;
   }

   public void tick() {
      super.tick();
      if (this.world.isClient) {
         float f = MathHelper.cos((float)(this.getEntityId() * 3 + this.age) * 0.13F + 3.1415927F);
         float g = MathHelper.cos((float)(this.getEntityId() * 3 + this.age + 1) * 0.13F + 3.1415927F);
         if (f > 0.0F && g <= 0.0F) {
            this.world.playSound(this.getX(), this.getY(), this.getZ(), SoundEvents.ENTITY_PHANTOM_FLAP, this.getSoundCategory(), 0.95F + this.random.nextFloat() * 0.05F, 0.95F + this.random.nextFloat() * 0.05F, false);
         }

         int i = this.getPhantomSize();
         float h = MathHelper.cos(this.yaw * 0.017453292F) * (1.3F + 0.21F * (float)i);
         float j = MathHelper.sin(this.yaw * 0.017453292F) * (1.3F + 0.21F * (float)i);
         float k = (0.3F + f * 0.45F) * ((float)i * 0.2F + 1.0F);
         this.world.addParticle(ParticleTypes.MYCELIUM, this.getX() + (double)h, this.getY() + (double)k, this.getZ() + (double)j, 0.0D, 0.0D, 0.0D);
         this.world.addParticle(ParticleTypes.MYCELIUM, this.getX() - (double)h, this.getY() + (double)k, this.getZ() - (double)j, 0.0D, 0.0D, 0.0D);
      }

   }

   public void tickMovement() {
      if (this.isAlive() && this.isAffectedByDaylight()) {
         this.setOnFireFor(8);
      }

      super.tickMovement();
   }

   protected void mobTick() {
      super.mobTick();
   }

   public EntityData initialize(ServerWorldAccess world, LocalDifficulty difficulty, SpawnReason spawnReason, @Nullable EntityData entityData, @Nullable CompoundTag entityTag) {
      this.circlingCenter = this.getBlockPos().up(5);
      this.setPhantomSize(0);
      return super.initialize(world, difficulty, spawnReason, entityData, entityTag);
   }

   public void readCustomDataFromTag(CompoundTag tag) {
      super.readCustomDataFromTag(tag);
      if (tag.contains("AX")) {
         this.circlingCenter = new BlockPos(tag.getInt("AX"), tag.getInt("AY"), tag.getInt("AZ"));
      }

      this.setPhantomSize(tag.getInt("Size"));
   }

   public void writeCustomDataToTag(CompoundTag tag) {
      super.writeCustomDataToTag(tag);
      tag.putInt("AX", this.circlingCenter.getX());
      tag.putInt("AY", this.circlingCenter.getY());
      tag.putInt("AZ", this.circlingCenter.getZ());
      tag.putInt("Size", this.getPhantomSize());
   }

   @Environment(EnvType.CLIENT)
   public boolean shouldRender(double distance) {
      return true;
   }

   public SoundCategory getSoundCategory() {
      return SoundCategory.HOSTILE;
   }

   protected SoundEvent getAmbientSound() {
      return SoundEvents.ENTITY_PHANTOM_AMBIENT;
   }

   protected SoundEvent getHurtSound(DamageSource source) {
      return SoundEvents.ENTITY_PHANTOM_HURT;
   }

   protected SoundEvent getDeathSound() {
      return SoundEvents.ENTITY_PHANTOM_DEATH;
   }

   public EntityGroup getGroup() {
      return EntityGroup.UNDEAD;
   }

   protected float getSoundVolume() {
      return 1.0F;
   }

   public boolean canTarget(EntityType<?> type) {
      return true;
   }

   public EntityDimensions getDimensions(EntityPose pose) {
      int i = this.getPhantomSize();
      EntityDimensions entityDimensions = super.getDimensions(pose);
      float f = (entityDimensions.width + 0.2F * (float)i) / entityDimensions.width;
      return entityDimensions.scaled(f);
   }

   static {
      SIZE = DataTracker.registerData(PhantomEntity.class, TrackedDataHandlerRegistry.INTEGER);
   }

   class FindTargetGoal extends Goal {
      private final TargetPredicate PLAYERS_IN_RANGE_PREDICATE;
      private int delay;

      private FindTargetGoal() {
         this.PLAYERS_IN_RANGE_PREDICATE = (new TargetPredicate()).setBaseMaxDistance(64.0D);
         this.delay = 20;
      }

      public boolean canStart() {
         if (this.delay > 0) {
            --this.delay;
            return false;
         } else {
            this.delay = 60;
            List<PlayerEntity> list = PhantomEntity.this.world.getPlayers(this.PLAYERS_IN_RANGE_PREDICATE, PhantomEntity.this, PhantomEntity.this.getBoundingBox().expand(16.0D, 64.0D, 16.0D));
            if (!list.isEmpty()) {
               list.sort(Comparator.comparing(Entity::getY).reversed());
               Iterator var2 = list.iterator();

               while(var2.hasNext()) {
                  PlayerEntity playerEntity = (PlayerEntity)var2.next();
                  if (PhantomEntity.this.isTarget(playerEntity, TargetPredicate.DEFAULT)) {
                     PhantomEntity.this.setTarget(playerEntity);
                     return true;
                  }
               }
            }

            return false;
         }
      }

      public boolean shouldContinue() {
         LivingEntity livingEntity = PhantomEntity.this.getTarget();
         return livingEntity != null ? PhantomEntity.this.isTarget(livingEntity, TargetPredicate.DEFAULT) : false;
      }
   }

   class StartAttackGoal extends Goal {
      private int cooldown;

      private StartAttackGoal() {
      }

      public boolean canStart() {
         LivingEntity livingEntity = PhantomEntity.this.getTarget();
         return livingEntity != null ? PhantomEntity.this.isTarget(PhantomEntity.this.getTarget(), TargetPredicate.DEFAULT) : false;
      }

      public void start() {
         this.cooldown = 10;
         PhantomEntity.this.movementType = PhantomEntity.PhantomMovementType.CIRCLE;
         this.startSwoop();
      }

      public void stop() {
         PhantomEntity.this.circlingCenter = PhantomEntity.this.world.getTopPosition(Heightmap.Type.MOTION_BLOCKING, PhantomEntity.this.circlingCenter).up(10 + PhantomEntity.this.random.nextInt(20));
      }

      public void tick() {
         if (PhantomEntity.this.movementType == PhantomEntity.PhantomMovementType.CIRCLE) {
            --this.cooldown;
            if (this.cooldown <= 0) {
               PhantomEntity.this.movementType = PhantomEntity.PhantomMovementType.SWOOP;
               this.startSwoop();
               this.cooldown = (8 + PhantomEntity.this.random.nextInt(4)) * 20;
               PhantomEntity.this.playSound(SoundEvents.ENTITY_PHANTOM_SWOOP, 10.0F, 0.95F + PhantomEntity.this.random.nextFloat() * 0.1F);
            }
         }

      }

      private void startSwoop() {
         PhantomEntity.this.circlingCenter = PhantomEntity.this.getTarget().getBlockPos().up(20 + PhantomEntity.this.random.nextInt(20));
         if (PhantomEntity.this.circlingCenter.getY() < PhantomEntity.this.world.getSeaLevel()) {
            PhantomEntity.this.circlingCenter = new BlockPos(PhantomEntity.this.circlingCenter.getX(), PhantomEntity.this.world.getSeaLevel() + 1, PhantomEntity.this.circlingCenter.getZ());
         }

      }
   }

   class SwoopMovementGoal extends PhantomEntity.MovementGoal {
      private SwoopMovementGoal() {
         super();
      }

      public boolean canStart() {
         return PhantomEntity.this.getTarget() != null && PhantomEntity.this.movementType == PhantomEntity.PhantomMovementType.SWOOP;
      }

      public boolean shouldContinue() {
         LivingEntity livingEntity = PhantomEntity.this.getTarget();
         if (livingEntity == null) {
            return false;
         } else if (!livingEntity.isAlive()) {
            return false;
         } else if (livingEntity instanceof PlayerEntity && (((PlayerEntity)livingEntity).isSpectator() || ((PlayerEntity)livingEntity).isCreative())) {
            return false;
         } else if (!this.canStart()) {
            return false;
         } else {
            if (PhantomEntity.this.age % 20 == 0) {
               List<CatEntity> list = PhantomEntity.this.world.getEntitiesByClass(CatEntity.class, PhantomEntity.this.getBoundingBox().expand(16.0D), EntityPredicates.VALID_ENTITY);
               if (!list.isEmpty()) {
                  Iterator var3 = list.iterator();

                  while(var3.hasNext()) {
                     CatEntity catEntity = (CatEntity)var3.next();
                     catEntity.hiss();
                  }

                  return false;
               }
            }

            return true;
         }
      }

      public void start() {
      }

      public void stop() {
         PhantomEntity.this.setTarget((LivingEntity)null);
         PhantomEntity.this.movementType = PhantomEntity.PhantomMovementType.CIRCLE;
      }

      public void tick() {
         LivingEntity livingEntity = PhantomEntity.this.getTarget();
         PhantomEntity.this.targetPosition = new Vec3d(livingEntity.getX(), livingEntity.getBodyY(0.5D), livingEntity.getZ());
         if (PhantomEntity.this.getBoundingBox().expand(0.20000000298023224D).intersects(livingEntity.getBoundingBox())) {
            PhantomEntity.this.tryAttack(livingEntity);
            PhantomEntity.this.movementType = PhantomEntity.PhantomMovementType.CIRCLE;
            if (!PhantomEntity.this.isSilent()) {
               PhantomEntity.this.world.syncWorldEvent(1039, PhantomEntity.this.getBlockPos(), 0);
            }
         } else if (PhantomEntity.this.horizontalCollision || PhantomEntity.this.hurtTime > 0) {
            PhantomEntity.this.movementType = PhantomEntity.PhantomMovementType.CIRCLE;
         }

      }
   }

   class CircleMovementGoal extends PhantomEntity.MovementGoal {
      private float angle;
      private float radius;
      private float yOffset;
      private float circlingDirection;

      private CircleMovementGoal() {
         super();
      }

      public boolean canStart() {
         return PhantomEntity.this.getTarget() == null || PhantomEntity.this.movementType == PhantomEntity.PhantomMovementType.CIRCLE;
      }

      public void start() {
         this.radius = 5.0F + PhantomEntity.this.random.nextFloat() * 10.0F;
         this.yOffset = -4.0F + PhantomEntity.this.random.nextFloat() * 9.0F;
         this.circlingDirection = PhantomEntity.this.random.nextBoolean() ? 1.0F : -1.0F;
         this.adjustDirection();
      }

      public void tick() {
         if (PhantomEntity.this.random.nextInt(350) == 0) {
            this.yOffset = -4.0F + PhantomEntity.this.random.nextFloat() * 9.0F;
         }

         if (PhantomEntity.this.random.nextInt(250) == 0) {
            ++this.radius;
            if (this.radius > 15.0F) {
               this.radius = 5.0F;
               this.circlingDirection = -this.circlingDirection;
            }
         }

         if (PhantomEntity.this.random.nextInt(450) == 0) {
            this.angle = PhantomEntity.this.random.nextFloat() * 2.0F * 3.1415927F;
            this.adjustDirection();
         }

         if (this.isNearTarget()) {
            this.adjustDirection();
         }

         if (PhantomEntity.this.targetPosition.y < PhantomEntity.this.getY() && !PhantomEntity.this.world.isAir(PhantomEntity.this.getBlockPos().down(1))) {
            this.yOffset = Math.max(1.0F, this.yOffset);
            this.adjustDirection();
         }

         if (PhantomEntity.this.targetPosition.y > PhantomEntity.this.getY() && !PhantomEntity.this.world.isAir(PhantomEntity.this.getBlockPos().up(1))) {
            this.yOffset = Math.min(-1.0F, this.yOffset);
            this.adjustDirection();
         }

      }

      private void adjustDirection() {
         if (BlockPos.ORIGIN.equals(PhantomEntity.this.circlingCenter)) {
            PhantomEntity.this.circlingCenter = PhantomEntity.this.getBlockPos();
         }

         this.angle += this.circlingDirection * 15.0F * 0.017453292F;
         PhantomEntity.this.targetPosition = Vec3d.of(PhantomEntity.this.circlingCenter).add((double)(this.radius * MathHelper.cos(this.angle)), (double)(-4.0F + this.yOffset), (double)(this.radius * MathHelper.sin(this.angle)));
      }
   }

   abstract class MovementGoal extends Goal {
      public MovementGoal() {
         this.setControls(EnumSet.of(Goal.Control.MOVE));
      }

      protected boolean isNearTarget() {
         return PhantomEntity.this.targetPosition.squaredDistanceTo(PhantomEntity.this.getX(), PhantomEntity.this.getY(), PhantomEntity.this.getZ()) < 4.0D;
      }
   }

   class PhantomLookControl extends LookControl {
      public PhantomLookControl(MobEntity entity) {
         super(entity);
      }

      public void tick() {
      }
   }

   class PhantomBodyControl extends BodyControl {
      public PhantomBodyControl(MobEntity entity) {
         super(entity);
      }

      public void tick() {
         PhantomEntity.this.headYaw = PhantomEntity.this.bodyYaw;
         PhantomEntity.this.bodyYaw = PhantomEntity.this.yaw;
      }
   }

   class PhantomMoveControl extends MoveControl {
      /**
       * The movement speed that the phantom tends towards
       */
      private float targetSpeed = 0.1F;

      public PhantomMoveControl(MobEntity owner) {
         super(owner);
      }

      public void tick() {
         if (PhantomEntity.this.horizontalCollision) {
            PhantomEntity var10000 = PhantomEntity.this;
            var10000.yaw += 180.0F;
            this.targetSpeed = 0.1F;
         }

         float f = (float)(PhantomEntity.this.targetPosition.x - PhantomEntity.this.getX());
         float g = (float)(PhantomEntity.this.targetPosition.y - PhantomEntity.this.getY());
         float h = (float)(PhantomEntity.this.targetPosition.z - PhantomEntity.this.getZ());
         double d = (double)MathHelper.sqrt(f * f + h * h);
         double e = 1.0D - (double)MathHelper.abs(g * 0.7F) / d;
         f = (float)((double)f * e);
         h = (float)((double)h * e);
         d = (double)MathHelper.sqrt(f * f + h * h);
         double i = (double)MathHelper.sqrt(f * f + h * h + g * g);
         float j = PhantomEntity.this.yaw;
         float k = (float)MathHelper.atan2((double)h, (double)f);
         float l = MathHelper.wrapDegrees(PhantomEntity.this.yaw + 90.0F);
         float m = MathHelper.wrapDegrees(k * 57.295776F);
         PhantomEntity.this.yaw = MathHelper.stepUnwrappedAngleTowards(l, m, 4.0F) - 90.0F;
         PhantomEntity.this.bodyYaw = PhantomEntity.this.yaw;
         if (MathHelper.angleBetween(j, PhantomEntity.this.yaw) < 3.0F) {
            this.targetSpeed = MathHelper.stepTowards(this.targetSpeed, 1.8F, 0.005F * (1.8F / this.targetSpeed));
         } else {
            this.targetSpeed = MathHelper.stepTowards(this.targetSpeed, 0.2F, 0.025F);
         }

         float n = (float)(-(MathHelper.atan2((double)(-g), d) * 57.2957763671875D));
         PhantomEntity.this.pitch = n;
         float o = PhantomEntity.this.yaw + 90.0F;
         double p = (double)(this.targetSpeed * MathHelper.cos(o * 0.017453292F)) * Math.abs((double)f / i);
         double q = (double)(this.targetSpeed * MathHelper.sin(o * 0.017453292F)) * Math.abs((double)h / i);
         double r = (double)(this.targetSpeed * MathHelper.sin(n * 0.017453292F)) * Math.abs((double)g / i);
         Vec3d vec3d = PhantomEntity.this.getVelocity();
         PhantomEntity.this.setVelocity(vec3d.add((new Vec3d(p, r, q)).subtract(vec3d).multiply(0.2D)));
      }
   }

   static enum PhantomMovementType {
      CIRCLE,
      SWOOP;
   }
}
