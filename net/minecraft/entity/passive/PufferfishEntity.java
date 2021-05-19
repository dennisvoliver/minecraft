package net.minecraft.entity.passive;

import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityGroup;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.packet.s2c.play.GameStateChangeS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.world.World;

public class PufferfishEntity extends FishEntity {
   private static final TrackedData<Integer> PUFF_STATE;
   private int inflateTicks;
   private int deflateTicks;
   private static final Predicate<LivingEntity> BLOW_UP_FILTER;

   public PufferfishEntity(EntityType<? extends PufferfishEntity> entityType, World world) {
      super(entityType, world);
   }

   protected void initDataTracker() {
      super.initDataTracker();
      this.dataTracker.startTracking(PUFF_STATE, 0);
   }

   public int getPuffState() {
      return (Integer)this.dataTracker.get(PUFF_STATE);
   }

   public void setPuffState(int puffState) {
      this.dataTracker.set(PUFF_STATE, puffState);
   }

   public void onTrackedDataSet(TrackedData<?> data) {
      if (PUFF_STATE.equals(data)) {
         this.calculateDimensions();
      }

      super.onTrackedDataSet(data);
   }

   public void writeCustomDataToTag(CompoundTag tag) {
      super.writeCustomDataToTag(tag);
      tag.putInt("PuffState", this.getPuffState());
   }

   public void readCustomDataFromTag(CompoundTag tag) {
      super.readCustomDataFromTag(tag);
      this.setPuffState(tag.getInt("PuffState"));
   }

   protected ItemStack getFishBucketItem() {
      return new ItemStack(Items.PUFFERFISH_BUCKET);
   }

   protected void initGoals() {
      super.initGoals();
      this.goalSelector.add(1, new PufferfishEntity.InflateGoal(this));
   }

   public void tick() {
      if (!this.world.isClient && this.isAlive() && this.canMoveVoluntarily()) {
         if (this.inflateTicks > 0) {
            if (this.getPuffState() == 0) {
               this.playSound(SoundEvents.ENTITY_PUFFER_FISH_BLOW_UP, this.getSoundVolume(), this.getSoundPitch());
               this.setPuffState(1);
            } else if (this.inflateTicks > 40 && this.getPuffState() == 1) {
               this.playSound(SoundEvents.ENTITY_PUFFER_FISH_BLOW_UP, this.getSoundVolume(), this.getSoundPitch());
               this.setPuffState(2);
            }

            ++this.inflateTicks;
         } else if (this.getPuffState() != 0) {
            if (this.deflateTicks > 60 && this.getPuffState() == 2) {
               this.playSound(SoundEvents.ENTITY_PUFFER_FISH_BLOW_OUT, this.getSoundVolume(), this.getSoundPitch());
               this.setPuffState(1);
            } else if (this.deflateTicks > 100 && this.getPuffState() == 1) {
               this.playSound(SoundEvents.ENTITY_PUFFER_FISH_BLOW_OUT, this.getSoundVolume(), this.getSoundPitch());
               this.setPuffState(0);
            }

            ++this.deflateTicks;
         }
      }

      super.tick();
   }

   public void tickMovement() {
      super.tickMovement();
      if (this.isAlive() && this.getPuffState() > 0) {
         List<MobEntity> list = this.world.getEntitiesByClass(MobEntity.class, this.getBoundingBox().expand(0.3D), BLOW_UP_FILTER);
         Iterator var2 = list.iterator();

         while(var2.hasNext()) {
            MobEntity mobEntity = (MobEntity)var2.next();
            if (mobEntity.isAlive()) {
               this.sting(mobEntity);
            }
         }
      }

   }

   private void sting(MobEntity mob) {
      int i = this.getPuffState();
      if (mob.damage(DamageSource.mob(this), (float)(1 + i))) {
         mob.addStatusEffect(new StatusEffectInstance(StatusEffects.POISON, 60 * i, 0));
         this.playSound(SoundEvents.ENTITY_PUFFER_FISH_STING, 1.0F, 1.0F);
      }

   }

   public void onPlayerCollision(PlayerEntity player) {
      int i = this.getPuffState();
      if (player instanceof ServerPlayerEntity && i > 0 && player.damage(DamageSource.mob(this), (float)(1 + i))) {
         if (!this.isSilent()) {
            ((ServerPlayerEntity)player).networkHandler.sendPacket(new GameStateChangeS2CPacket(GameStateChangeS2CPacket.PUFFERFISH_STING, 0.0F));
         }

         player.addStatusEffect(new StatusEffectInstance(StatusEffects.POISON, 60 * i, 0));
      }

   }

   protected SoundEvent getAmbientSound() {
      return SoundEvents.ENTITY_PUFFER_FISH_AMBIENT;
   }

   protected SoundEvent getDeathSound() {
      return SoundEvents.ENTITY_PUFFER_FISH_DEATH;
   }

   protected SoundEvent getHurtSound(DamageSource source) {
      return SoundEvents.ENTITY_PUFFER_FISH_HURT;
   }

   protected SoundEvent getFlopSound() {
      return SoundEvents.ENTITY_PUFFER_FISH_FLOP;
   }

   public EntityDimensions getDimensions(EntityPose pose) {
      return super.getDimensions(pose).scaled(getScaleForPuffState(this.getPuffState()));
   }

   private static float getScaleForPuffState(int puffState) {
      switch(puffState) {
      case 0:
         return 0.5F;
      case 1:
         return 0.7F;
      default:
         return 1.0F;
      }
   }

   static {
      PUFF_STATE = DataTracker.registerData(PufferfishEntity.class, TrackedDataHandlerRegistry.INTEGER);
      BLOW_UP_FILTER = (livingEntity) -> {
         if (livingEntity == null) {
            return false;
         } else if (!(livingEntity instanceof PlayerEntity) || !livingEntity.isSpectator() && !((PlayerEntity)livingEntity).isCreative()) {
            return livingEntity.getGroup() != EntityGroup.AQUATIC;
         } else {
            return false;
         }
      };
   }

   static class InflateGoal extends Goal {
      private final PufferfishEntity pufferfish;

      public InflateGoal(PufferfishEntity pufferfish) {
         this.pufferfish = pufferfish;
      }

      public boolean canStart() {
         List<LivingEntity> list = this.pufferfish.world.getEntitiesByClass(LivingEntity.class, this.pufferfish.getBoundingBox().expand(2.0D), PufferfishEntity.BLOW_UP_FILTER);
         return !list.isEmpty();
      }

      public void start() {
         this.pufferfish.inflateTicks = 1;
         this.pufferfish.deflateTicks = 0;
      }

      public void stop() {
         this.pufferfish.inflateTicks = 0;
      }

      public boolean shouldContinue() {
         List<LivingEntity> list = this.pufferfish.world.getEntitiesByClass(LivingEntity.class, this.pufferfish.getBoundingBox().expand(2.0D), PufferfishEntity.BLOW_UP_FILTER);
         return !list.isEmpty();
      }
   }
}
