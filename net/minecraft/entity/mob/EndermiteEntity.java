package net.minecraft.entity.mob;

import java.util.Random;
import net.minecraft.block.BlockState;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityGroup;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.goal.FollowTargetGoal;
import net.minecraft.entity.ai.goal.LookAroundGoal;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.entity.ai.goal.RevengeGoal;
import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.entity.ai.goal.WanderAroundFarGoal;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;

public class EndermiteEntity extends HostileEntity {
   private int lifeTime;
   private boolean playerSpawned;

   public EndermiteEntity(EntityType<? extends EndermiteEntity> entityType, World world) {
      super(entityType, world);
      this.experiencePoints = 3;
   }

   protected void initGoals() {
      this.goalSelector.add(1, new SwimGoal(this));
      this.goalSelector.add(2, new MeleeAttackGoal(this, 1.0D, false));
      this.goalSelector.add(3, new WanderAroundFarGoal(this, 1.0D));
      this.goalSelector.add(7, new LookAtEntityGoal(this, PlayerEntity.class, 8.0F));
      this.goalSelector.add(8, new LookAroundGoal(this));
      this.targetSelector.add(1, (new RevengeGoal(this, new Class[0])).setGroupRevenge());
      this.targetSelector.add(2, new FollowTargetGoal(this, PlayerEntity.class, true));
   }

   protected float getActiveEyeHeight(EntityPose pose, EntityDimensions dimensions) {
      return 0.13F;
   }

   public static DefaultAttributeContainer.Builder createEndermiteAttributes() {
      return HostileEntity.createHostileAttributes().add(EntityAttributes.GENERIC_MAX_HEALTH, 8.0D).add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.25D).add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 2.0D);
   }

   protected boolean canClimb() {
      return false;
   }

   protected SoundEvent getAmbientSound() {
      return SoundEvents.ENTITY_ENDERMITE_AMBIENT;
   }

   protected SoundEvent getHurtSound(DamageSource source) {
      return SoundEvents.ENTITY_ENDERMITE_HURT;
   }

   protected SoundEvent getDeathSound() {
      return SoundEvents.ENTITY_ENDERMITE_DEATH;
   }

   protected void playStepSound(BlockPos pos, BlockState state) {
      this.playSound(SoundEvents.ENTITY_ENDERMITE_STEP, 0.15F, 1.0F);
   }

   public void readCustomDataFromTag(CompoundTag tag) {
      super.readCustomDataFromTag(tag);
      this.lifeTime = tag.getInt("Lifetime");
      this.playerSpawned = tag.getBoolean("PlayerSpawned");
   }

   public void writeCustomDataToTag(CompoundTag tag) {
      super.writeCustomDataToTag(tag);
      tag.putInt("Lifetime", this.lifeTime);
      tag.putBoolean("PlayerSpawned", this.playerSpawned);
   }

   public void tick() {
      this.bodyYaw = this.yaw;
      super.tick();
   }

   public void setYaw(float yaw) {
      this.yaw = yaw;
      super.setYaw(yaw);
   }

   public double getHeightOffset() {
      return 0.1D;
   }

   /**
    * Returns whether this endermite was spawned from an ender pearl thrown by a player.
    */
   public boolean isPlayerSpawned() {
      return this.playerSpawned;
   }

   public void setPlayerSpawned(boolean playerSpawned) {
      this.playerSpawned = playerSpawned;
   }

   public void tickMovement() {
      super.tickMovement();
      if (this.world.isClient) {
         for(int i = 0; i < 2; ++i) {
            this.world.addParticle(ParticleTypes.PORTAL, this.getParticleX(0.5D), this.getRandomBodyY(), this.getParticleZ(0.5D), (this.random.nextDouble() - 0.5D) * 2.0D, -this.random.nextDouble(), (this.random.nextDouble() - 0.5D) * 2.0D);
         }
      } else {
         if (!this.isPersistent()) {
            ++this.lifeTime;
         }

         if (this.lifeTime >= 2400) {
            this.remove();
         }
      }

   }

   public static boolean canSpawn(EntityType<EndermiteEntity> type, WorldAccess world, SpawnReason spawnReason, BlockPos pos, Random random) {
      if (canSpawnIgnoreLightLevel(type, world, spawnReason, pos, random)) {
         PlayerEntity playerEntity = world.getClosestPlayer((double)pos.getX() + 0.5D, (double)pos.getY() + 0.5D, (double)pos.getZ() + 0.5D, 5.0D, true);
         return playerEntity == null;
      } else {
         return false;
      }
   }

   public EntityGroup getGroup() {
      return EntityGroup.ARTHROPOD;
   }
}
