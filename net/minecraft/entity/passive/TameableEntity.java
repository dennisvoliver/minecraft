package net.minecraft.entity.passive;

import java.util.Optional;
import java.util.UUID;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.scoreboard.AbstractTeam;
import net.minecraft.server.ServerConfigHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Util;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public abstract class TameableEntity extends AnimalEntity {
   protected static final TrackedData<Byte> TAMEABLE_FLAGS;
   protected static final TrackedData<Optional<UUID>> OWNER_UUID;
   private boolean sitting;

   protected TameableEntity(EntityType<? extends TameableEntity> entityType, World world) {
      super(entityType, world);
      this.onTamedChanged();
   }

   protected void initDataTracker() {
      super.initDataTracker();
      this.dataTracker.startTracking(TAMEABLE_FLAGS, (byte)0);
      this.dataTracker.startTracking(OWNER_UUID, Optional.empty());
   }

   public void writeCustomDataToTag(CompoundTag tag) {
      super.writeCustomDataToTag(tag);
      if (this.getOwnerUuid() != null) {
         tag.putUuid("Owner", this.getOwnerUuid());
      }

      tag.putBoolean("Sitting", this.sitting);
   }

   public void readCustomDataFromTag(CompoundTag tag) {
      super.readCustomDataFromTag(tag);
      UUID uUID2;
      if (tag.containsUuid("Owner")) {
         uUID2 = tag.getUuid("Owner");
      } else {
         String string = tag.getString("Owner");
         uUID2 = ServerConfigHandler.getPlayerUuidByName(this.getServer(), string);
      }

      if (uUID2 != null) {
         try {
            this.setOwnerUuid(uUID2);
            this.setTamed(true);
         } catch (Throwable var4) {
            this.setTamed(false);
         }
      }

      this.sitting = tag.getBoolean("Sitting");
      this.setInSittingPose(this.sitting);
   }

   public boolean canBeLeashedBy(PlayerEntity player) {
      return !this.isLeashed();
   }

   @Environment(EnvType.CLIENT)
   protected void showEmoteParticle(boolean positive) {
      ParticleEffect particleEffect = ParticleTypes.HEART;
      if (!positive) {
         particleEffect = ParticleTypes.SMOKE;
      }

      for(int i = 0; i < 7; ++i) {
         double d = this.random.nextGaussian() * 0.02D;
         double e = this.random.nextGaussian() * 0.02D;
         double f = this.random.nextGaussian() * 0.02D;
         this.world.addParticle(particleEffect, this.getParticleX(1.0D), this.getRandomBodyY() + 0.5D, this.getParticleZ(1.0D), d, e, f);
      }

   }

   @Environment(EnvType.CLIENT)
   public void handleStatus(byte status) {
      if (status == 7) {
         this.showEmoteParticle(true);
      } else if (status == 6) {
         this.showEmoteParticle(false);
      } else {
         super.handleStatus(status);
      }

   }

   public boolean isTamed() {
      return ((Byte)this.dataTracker.get(TAMEABLE_FLAGS) & 4) != 0;
   }

   public void setTamed(boolean tamed) {
      byte b = (Byte)this.dataTracker.get(TAMEABLE_FLAGS);
      if (tamed) {
         this.dataTracker.set(TAMEABLE_FLAGS, (byte)(b | 4));
      } else {
         this.dataTracker.set(TAMEABLE_FLAGS, (byte)(b & -5));
      }

      this.onTamedChanged();
   }

   protected void onTamedChanged() {
   }

   public boolean isInSittingPose() {
      return ((Byte)this.dataTracker.get(TAMEABLE_FLAGS) & 1) != 0;
   }

   public void setInSittingPose(boolean inSittingPose) {
      byte b = (Byte)this.dataTracker.get(TAMEABLE_FLAGS);
      if (inSittingPose) {
         this.dataTracker.set(TAMEABLE_FLAGS, (byte)(b | 1));
      } else {
         this.dataTracker.set(TAMEABLE_FLAGS, (byte)(b & -2));
      }

   }

   @Nullable
   public UUID getOwnerUuid() {
      return (UUID)((Optional)this.dataTracker.get(OWNER_UUID)).orElse((Object)null);
   }

   public void setOwnerUuid(@Nullable UUID uuid) {
      this.dataTracker.set(OWNER_UUID, Optional.ofNullable(uuid));
   }

   public void setOwner(PlayerEntity player) {
      this.setTamed(true);
      this.setOwnerUuid(player.getUuid());
      if (player instanceof ServerPlayerEntity) {
         Criteria.TAME_ANIMAL.trigger((ServerPlayerEntity)player, this);
      }

   }

   @Nullable
   public LivingEntity getOwner() {
      try {
         UUID uUID = this.getOwnerUuid();
         return uUID == null ? null : this.world.getPlayerByUuid(uUID);
      } catch (IllegalArgumentException var2) {
         return null;
      }
   }

   public boolean canTarget(LivingEntity target) {
      return this.isOwner(target) ? false : super.canTarget(target);
   }

   public boolean isOwner(LivingEntity entity) {
      return entity == this.getOwner();
   }

   public boolean canAttackWithOwner(LivingEntity target, LivingEntity owner) {
      return true;
   }

   public AbstractTeam getScoreboardTeam() {
      if (this.isTamed()) {
         LivingEntity livingEntity = this.getOwner();
         if (livingEntity != null) {
            return livingEntity.getScoreboardTeam();
         }
      }

      return super.getScoreboardTeam();
   }

   public boolean isTeammate(Entity other) {
      if (this.isTamed()) {
         LivingEntity livingEntity = this.getOwner();
         if (other == livingEntity) {
            return true;
         }

         if (livingEntity != null) {
            return livingEntity.isTeammate(other);
         }
      }

      return super.isTeammate(other);
   }

   public void onDeath(DamageSource source) {
      if (!this.world.isClient && this.world.getGameRules().getBoolean(GameRules.SHOW_DEATH_MESSAGES) && this.getOwner() instanceof ServerPlayerEntity) {
         this.getOwner().sendSystemMessage(this.getDamageTracker().getDeathMessage(), Util.NIL_UUID);
      }

      super.onDeath(source);
   }

   public boolean isSitting() {
      return this.sitting;
   }

   public void setSitting(boolean sitting) {
      this.sitting = sitting;
   }

   static {
      TAMEABLE_FLAGS = DataTracker.registerData(TameableEntity.class, TrackedDataHandlerRegistry.BYTE);
      OWNER_UUID = DataTracker.registerData(TameableEntity.class, TrackedDataHandlerRegistry.OPTIONAL_UUID);
   }
}
