package net.minecraft.entity.mob;

import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import java.util.UUID;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.block.BedBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityData;
import net.minecraft.entity.EntityInteraction;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.village.TradeOfferList;
import net.minecraft.village.VillagerData;
import net.minecraft.village.VillagerDataContainer;
import net.minecraft.village.VillagerProfession;
import net.minecraft.village.VillagerType;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

public class ZombieVillagerEntity extends ZombieEntity implements VillagerDataContainer {
   private static final TrackedData<Boolean> CONVERTING;
   private static final TrackedData<VillagerData> VILLAGER_DATA;
   private int conversionTimer;
   private UUID converter;
   private Tag gossipData;
   private CompoundTag offerData;
   private int xp;

   public ZombieVillagerEntity(EntityType<? extends ZombieVillagerEntity> entityType, World world) {
      super(entityType, world);
      this.setVillagerData(this.getVillagerData().withProfession((VillagerProfession)Registry.VILLAGER_PROFESSION.getRandom(this.random)));
   }

   protected void initDataTracker() {
      super.initDataTracker();
      this.dataTracker.startTracking(CONVERTING, false);
      this.dataTracker.startTracking(VILLAGER_DATA, new VillagerData(VillagerType.PLAINS, VillagerProfession.NONE, 1));
   }

   public void writeCustomDataToTag(CompoundTag tag) {
      super.writeCustomDataToTag(tag);
      DataResult var10000 = VillagerData.CODEC.encodeStart(NbtOps.INSTANCE, this.getVillagerData());
      Logger var10001 = LOGGER;
      var10001.getClass();
      var10000.resultOrPartial(var10001::error).ifPresent((tagx) -> {
         tag.put("VillagerData", tagx);
      });
      if (this.offerData != null) {
         tag.put("Offers", this.offerData);
      }

      if (this.gossipData != null) {
         tag.put("Gossips", this.gossipData);
      }

      tag.putInt("ConversionTime", this.isConverting() ? this.conversionTimer : -1);
      if (this.converter != null) {
         tag.putUuid("ConversionPlayer", this.converter);
      }

      tag.putInt("Xp", this.xp);
   }

   public void readCustomDataFromTag(CompoundTag tag) {
      super.readCustomDataFromTag(tag);
      if (tag.contains("VillagerData", 10)) {
         DataResult<VillagerData> dataResult = VillagerData.CODEC.parse(new Dynamic(NbtOps.INSTANCE, tag.get("VillagerData")));
         Logger var10001 = LOGGER;
         var10001.getClass();
         dataResult.resultOrPartial(var10001::error).ifPresent(this::setVillagerData);
      }

      if (tag.contains("Offers", 10)) {
         this.offerData = tag.getCompound("Offers");
      }

      if (tag.contains("Gossips", 10)) {
         this.gossipData = tag.getList("Gossips", 10);
      }

      if (tag.contains("ConversionTime", 99) && tag.getInt("ConversionTime") > -1) {
         this.setConverting(tag.containsUuid("ConversionPlayer") ? tag.getUuid("ConversionPlayer") : null, tag.getInt("ConversionTime"));
      }

      if (tag.contains("Xp", 3)) {
         this.xp = tag.getInt("Xp");
      }

   }

   public void tick() {
      if (!this.world.isClient && this.isAlive() && this.isConverting()) {
         int i = this.getConversionRate();
         this.conversionTimer -= i;
         if (this.conversionTimer <= 0) {
            this.finishConversion((ServerWorld)this.world);
         }
      }

      super.tick();
   }

   public ActionResult interactMob(PlayerEntity player, Hand hand) {
      ItemStack itemStack = player.getStackInHand(hand);
      if (itemStack.getItem() == Items.GOLDEN_APPLE) {
         if (this.hasStatusEffect(StatusEffects.WEAKNESS)) {
            if (!player.abilities.creativeMode) {
               itemStack.decrement(1);
            }

            if (!this.world.isClient) {
               this.setConverting(player.getUuid(), this.random.nextInt(2401) + 3600);
            }

            return ActionResult.SUCCESS;
         } else {
            return ActionResult.CONSUME;
         }
      } else {
         return super.interactMob(player, hand);
      }
   }

   protected boolean canConvertInWater() {
      return false;
   }

   public boolean canImmediatelyDespawn(double distanceSquared) {
      return !this.isConverting() && this.xp == 0;
   }

   public boolean isConverting() {
      return (Boolean)this.getDataTracker().get(CONVERTING);
   }

   private void setConverting(@Nullable UUID uuid, int delay) {
      this.converter = uuid;
      this.conversionTimer = delay;
      this.getDataTracker().set(CONVERTING, true);
      this.removeStatusEffect(StatusEffects.WEAKNESS);
      this.addStatusEffect(new StatusEffectInstance(StatusEffects.STRENGTH, delay, Math.min(this.world.getDifficulty().getId() - 1, 0)));
      this.world.sendEntityStatus(this, (byte)16);
   }

   @Environment(EnvType.CLIENT)
   public void handleStatus(byte status) {
      if (status == 16) {
         if (!this.isSilent()) {
            this.world.playSound(this.getX(), this.getEyeY(), this.getZ(), SoundEvents.ENTITY_ZOMBIE_VILLAGER_CURE, this.getSoundCategory(), 1.0F + this.random.nextFloat(), this.random.nextFloat() * 0.7F + 0.3F, false);
         }

      } else {
         super.handleStatus(status);
      }
   }

   private void finishConversion(ServerWorld world) {
      VillagerEntity villagerEntity = (VillagerEntity)this.method_29243(EntityType.VILLAGER, false);
      EquipmentSlot[] var3 = EquipmentSlot.values();
      int var4 = var3.length;

      for(int var5 = 0; var5 < var4; ++var5) {
         EquipmentSlot equipmentSlot = var3[var5];
         ItemStack itemStack = this.getEquippedStack(equipmentSlot);
         if (!itemStack.isEmpty()) {
            if (EnchantmentHelper.hasBindingCurse(itemStack)) {
               villagerEntity.equip(equipmentSlot.getEntitySlotId() + 300, itemStack);
            } else {
               double d = (double)this.getDropChance(equipmentSlot);
               if (d > 1.0D) {
                  this.dropStack(itemStack);
               }
            }
         }
      }

      villagerEntity.setVillagerData(this.getVillagerData());
      if (this.gossipData != null) {
         villagerEntity.setGossipDataFromTag(this.gossipData);
      }

      if (this.offerData != null) {
         villagerEntity.setOffers(new TradeOfferList(this.offerData));
      }

      villagerEntity.setExperience(this.xp);
      villagerEntity.initialize(world, world.getLocalDifficulty(villagerEntity.getBlockPos()), SpawnReason.CONVERSION, (EntityData)null, (CompoundTag)null);
      if (this.converter != null) {
         PlayerEntity playerEntity = world.getPlayerByUuid(this.converter);
         if (playerEntity instanceof ServerPlayerEntity) {
            Criteria.CURED_ZOMBIE_VILLAGER.trigger((ServerPlayerEntity)playerEntity, this, villagerEntity);
            world.handleInteraction(EntityInteraction.ZOMBIE_VILLAGER_CURED, playerEntity, villagerEntity);
         }
      }

      villagerEntity.addStatusEffect(new StatusEffectInstance(StatusEffects.NAUSEA, 200, 0));
      if (!this.isSilent()) {
         world.syncWorldEvent((PlayerEntity)null, 1027, this.getBlockPos(), 0);
      }

   }

   private int getConversionRate() {
      int i = 1;
      if (this.random.nextFloat() < 0.01F) {
         int j = 0;
         BlockPos.Mutable mutable = new BlockPos.Mutable();

         for(int k = (int)this.getX() - 4; k < (int)this.getX() + 4 && j < 14; ++k) {
            for(int l = (int)this.getY() - 4; l < (int)this.getY() + 4 && j < 14; ++l) {
               for(int m = (int)this.getZ() - 4; m < (int)this.getZ() + 4 && j < 14; ++m) {
                  Block block = this.world.getBlockState(mutable.set(k, l, m)).getBlock();
                  if (block == Blocks.IRON_BARS || block instanceof BedBlock) {
                     if (this.random.nextFloat() < 0.3F) {
                        ++i;
                     }

                     ++j;
                  }
               }
            }
         }
      }

      return i;
   }

   protected float getSoundPitch() {
      return this.isBaby() ? (this.random.nextFloat() - this.random.nextFloat()) * 0.2F + 2.0F : (this.random.nextFloat() - this.random.nextFloat()) * 0.2F + 1.0F;
   }

   public SoundEvent getAmbientSound() {
      return SoundEvents.ENTITY_ZOMBIE_VILLAGER_AMBIENT;
   }

   public SoundEvent getHurtSound(DamageSource source) {
      return SoundEvents.ENTITY_ZOMBIE_VILLAGER_HURT;
   }

   public SoundEvent getDeathSound() {
      return SoundEvents.ENTITY_ZOMBIE_VILLAGER_DEATH;
   }

   public SoundEvent getStepSound() {
      return SoundEvents.ENTITY_ZOMBIE_VILLAGER_STEP;
   }

   protected ItemStack getSkull() {
      return ItemStack.EMPTY;
   }

   public void setOfferData(CompoundTag offerTag) {
      this.offerData = offerTag;
   }

   public void setGossipData(Tag gossipTag) {
      this.gossipData = gossipTag;
   }

   @Nullable
   public EntityData initialize(ServerWorldAccess world, LocalDifficulty difficulty, SpawnReason spawnReason, @Nullable EntityData entityData, @Nullable CompoundTag entityTag) {
      this.setVillagerData(this.getVillagerData().withType(VillagerType.forBiome(world.getBiomeKey(this.getBlockPos()))));
      return super.initialize(world, difficulty, spawnReason, entityData, entityTag);
   }

   public void setVillagerData(VillagerData data) {
      VillagerData villagerData = this.getVillagerData();
      if (villagerData.getProfession() != data.getProfession()) {
         this.offerData = null;
      }

      this.dataTracker.set(VILLAGER_DATA, data);
   }

   public VillagerData getVillagerData() {
      return (VillagerData)this.dataTracker.get(VILLAGER_DATA);
   }

   public void setXp(int xp) {
      this.xp = xp;
   }

   static {
      CONVERTING = DataTracker.registerData(ZombieVillagerEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
      VILLAGER_DATA = DataTracker.registerData(ZombieVillagerEntity.class, TrackedDataHandlerRegistry.VILLAGER_DATA);
   }
}
