package net.minecraft.entity.passive;

import java.util.Random;
import java.util.UUID;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.pathing.PathNodeType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.stat.Stats;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;
import org.jetbrains.annotations.Nullable;

public abstract class AnimalEntity extends PassiveEntity {
   private int loveTicks;
   private UUID lovingPlayer;

   protected AnimalEntity(EntityType<? extends AnimalEntity> entityType, World world) {
      super(entityType, world);
      this.setPathfindingPenalty(PathNodeType.DANGER_FIRE, 16.0F);
      this.setPathfindingPenalty(PathNodeType.DAMAGE_FIRE, -1.0F);
   }

   protected void mobTick() {
      if (this.getBreedingAge() != 0) {
         this.loveTicks = 0;
      }

      super.mobTick();
   }

   public void tickMovement() {
      super.tickMovement();
      if (this.getBreedingAge() != 0) {
         this.loveTicks = 0;
      }

      if (this.loveTicks > 0) {
         --this.loveTicks;
         if (this.loveTicks % 10 == 0) {
            double d = this.random.nextGaussian() * 0.02D;
            double e = this.random.nextGaussian() * 0.02D;
            double f = this.random.nextGaussian() * 0.02D;
            this.world.addParticle(ParticleTypes.HEART, this.getParticleX(1.0D), this.getRandomBodyY() + 0.5D, this.getParticleZ(1.0D), d, e, f);
         }
      }

   }

   public boolean damage(DamageSource source, float amount) {
      if (this.isInvulnerableTo(source)) {
         return false;
      } else {
         this.loveTicks = 0;
         return super.damage(source, amount);
      }
   }

   public float getPathfindingFavor(BlockPos pos, WorldView world) {
      return world.getBlockState(pos.down()).isOf(Blocks.GRASS_BLOCK) ? 10.0F : world.getBrightness(pos) - 0.5F;
   }

   public void writeCustomDataToTag(CompoundTag tag) {
      super.writeCustomDataToTag(tag);
      tag.putInt("InLove", this.loveTicks);
      if (this.lovingPlayer != null) {
         tag.putUuid("LoveCause", this.lovingPlayer);
      }

   }

   public double getHeightOffset() {
      return 0.14D;
   }

   public void readCustomDataFromTag(CompoundTag tag) {
      super.readCustomDataFromTag(tag);
      this.loveTicks = tag.getInt("InLove");
      this.lovingPlayer = tag.containsUuid("LoveCause") ? tag.getUuid("LoveCause") : null;
   }

   public static boolean isValidNaturalSpawn(EntityType<? extends AnimalEntity> type, WorldAccess world, SpawnReason spawnReason, BlockPos pos, Random random) {
      return world.getBlockState(pos.down()).isOf(Blocks.GRASS_BLOCK) && world.getBaseLightLevel(pos, 0) > 8;
   }

   public int getMinAmbientSoundDelay() {
      return 120;
   }

   public boolean canImmediatelyDespawn(double distanceSquared) {
      return false;
   }

   protected int getCurrentExperience(PlayerEntity player) {
      return 1 + this.world.random.nextInt(3);
   }

   public boolean isBreedingItem(ItemStack stack) {
      return stack.getItem() == Items.WHEAT;
   }

   public ActionResult interactMob(PlayerEntity player, Hand hand) {
      ItemStack itemStack = player.getStackInHand(hand);
      if (this.isBreedingItem(itemStack)) {
         int i = this.getBreedingAge();
         if (!this.world.isClient && i == 0 && this.canEat()) {
            this.eat(player, itemStack);
            this.lovePlayer(player);
            return ActionResult.SUCCESS;
         }

         if (this.isBaby()) {
            this.eat(player, itemStack);
            this.growUp((int)((float)(-i / 20) * 0.1F), true);
            return ActionResult.success(this.world.isClient);
         }

         if (this.world.isClient) {
            return ActionResult.CONSUME;
         }
      }

      return super.interactMob(player, hand);
   }

   protected void eat(PlayerEntity player, ItemStack stack) {
      if (!player.abilities.creativeMode) {
         stack.decrement(1);
      }

   }

   public boolean canEat() {
      return this.loveTicks <= 0;
   }

   public void lovePlayer(@Nullable PlayerEntity player) {
      this.loveTicks = 600;
      if (player != null) {
         this.lovingPlayer = player.getUuid();
      }

      this.world.sendEntityStatus(this, (byte)18);
   }

   public void setLoveTicks(int loveTicks) {
      this.loveTicks = loveTicks;
   }

   public int getLoveTicks() {
      return this.loveTicks;
   }

   @Nullable
   public ServerPlayerEntity getLovingPlayer() {
      if (this.lovingPlayer == null) {
         return null;
      } else {
         PlayerEntity playerEntity = this.world.getPlayerByUuid(this.lovingPlayer);
         return playerEntity instanceof ServerPlayerEntity ? (ServerPlayerEntity)playerEntity : null;
      }
   }

   public boolean isInLove() {
      return this.loveTicks > 0;
   }

   public void resetLoveTicks() {
      this.loveTicks = 0;
   }

   public boolean canBreedWith(AnimalEntity other) {
      if (other == this) {
         return false;
      } else if (other.getClass() != this.getClass()) {
         return false;
      } else {
         return this.isInLove() && other.isInLove();
      }
   }

   public void breed(ServerWorld serverWorld, AnimalEntity other) {
      PassiveEntity passiveEntity = this.createChild(serverWorld, other);
      if (passiveEntity != null) {
         ServerPlayerEntity serverPlayerEntity = this.getLovingPlayer();
         if (serverPlayerEntity == null && other.getLovingPlayer() != null) {
            serverPlayerEntity = other.getLovingPlayer();
         }

         if (serverPlayerEntity != null) {
            serverPlayerEntity.incrementStat(Stats.ANIMALS_BRED);
            Criteria.BRED_ANIMALS.trigger(serverPlayerEntity, this, other, passiveEntity);
         }

         this.setBreedingAge(6000);
         other.setBreedingAge(6000);
         this.resetLoveTicks();
         other.resetLoveTicks();
         passiveEntity.setBaby(true);
         passiveEntity.refreshPositionAndAngles(this.getX(), this.getY(), this.getZ(), 0.0F, 0.0F);
         serverWorld.spawnEntityAndPassengers(passiveEntity);
         serverWorld.sendEntityStatus(this, (byte)18);
         if (serverWorld.getGameRules().getBoolean(GameRules.DO_MOB_LOOT)) {
            serverWorld.spawnEntity(new ExperienceOrbEntity(serverWorld, this.getX(), this.getY(), this.getZ(), this.getRandom().nextInt(7) + 1));
         }

      }
   }

   @Environment(EnvType.CLIENT)
   public void handleStatus(byte status) {
      if (status == 18) {
         for(int i = 0; i < 7; ++i) {
            double d = this.random.nextGaussian() * 0.02D;
            double e = this.random.nextGaussian() * 0.02D;
            double f = this.random.nextGaussian() * 0.02D;
            this.world.addParticle(ParticleTypes.HEART, this.getParticleX(1.0D), this.getRandomBodyY() + 0.5D, this.getParticleZ(1.0D), d, e, f);
         }
      } else {
         super.handleStatus(status);
      }

   }
}
