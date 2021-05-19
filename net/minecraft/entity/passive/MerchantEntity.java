package net.minecraft.entity.passive;

import com.google.common.collect.Sets;
import java.util.Iterator;
import java.util.Set;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityData;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.Npc;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.pathing.PathNodeType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.village.Merchant;
import net.minecraft.village.TradeOffer;
import net.minecraft.village.TradeOfferList;
import net.minecraft.village.TradeOffers;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public abstract class MerchantEntity extends PassiveEntity implements Npc, Merchant {
   private static final TrackedData<Integer> HEAD_ROLLING_TIME_LEFT;
   @Nullable
   private PlayerEntity customer;
   @Nullable
   protected TradeOfferList offers;
   private final SimpleInventory inventory = new SimpleInventory(8);

   public MerchantEntity(EntityType<? extends MerchantEntity> entityType, World world) {
      super(entityType, world);
      this.setPathfindingPenalty(PathNodeType.DANGER_FIRE, 16.0F);
      this.setPathfindingPenalty(PathNodeType.DAMAGE_FIRE, -1.0F);
   }

   public EntityData initialize(ServerWorldAccess world, LocalDifficulty difficulty, SpawnReason spawnReason, @Nullable EntityData entityData, @Nullable CompoundTag entityTag) {
      if (entityData == null) {
         entityData = new PassiveEntity.PassiveData(false);
      }

      return super.initialize(world, difficulty, spawnReason, (EntityData)entityData, entityTag);
   }

   public int getHeadRollingTimeLeft() {
      return (Integer)this.dataTracker.get(HEAD_ROLLING_TIME_LEFT);
   }

   public void setHeadRollingTimeLeft(int ticks) {
      this.dataTracker.set(HEAD_ROLLING_TIME_LEFT, ticks);
   }

   public int getExperience() {
      return 0;
   }

   protected float getActiveEyeHeight(EntityPose pose, EntityDimensions dimensions) {
      return this.isBaby() ? 0.81F : 1.62F;
   }

   protected void initDataTracker() {
      super.initDataTracker();
      this.dataTracker.startTracking(HEAD_ROLLING_TIME_LEFT, 0);
   }

   public void setCurrentCustomer(@Nullable PlayerEntity customer) {
      this.customer = customer;
   }

   @Nullable
   public PlayerEntity getCurrentCustomer() {
      return this.customer;
   }

   public boolean hasCustomer() {
      return this.customer != null;
   }

   public TradeOfferList getOffers() {
      if (this.offers == null) {
         this.offers = new TradeOfferList();
         this.fillRecipes();
      }

      return this.offers;
   }

   @Environment(EnvType.CLIENT)
   public void setOffersFromServer(@Nullable TradeOfferList offers) {
   }

   public void setExperienceFromServer(int experience) {
   }

   public void trade(TradeOffer offer) {
      offer.use();
      this.ambientSoundChance = -this.getMinAmbientSoundDelay();
      this.afterUsing(offer);
      if (this.customer instanceof ServerPlayerEntity) {
         Criteria.VILLAGER_TRADE.handle((ServerPlayerEntity)this.customer, this, offer.getMutableSellItem());
      }

   }

   protected abstract void afterUsing(TradeOffer offer);

   public boolean isLeveledMerchant() {
      return true;
   }

   public void onSellingItem(ItemStack stack) {
      if (!this.world.isClient && this.ambientSoundChance > -this.getMinAmbientSoundDelay() + 20) {
         this.ambientSoundChance = -this.getMinAmbientSoundDelay();
         this.playSound(this.getTradingSound(!stack.isEmpty()), this.getSoundVolume(), this.getSoundPitch());
      }

   }

   public SoundEvent getYesSound() {
      return SoundEvents.ENTITY_VILLAGER_YES;
   }

   protected SoundEvent getTradingSound(boolean sold) {
      return sold ? SoundEvents.ENTITY_VILLAGER_YES : SoundEvents.ENTITY_VILLAGER_NO;
   }

   public void playCelebrateSound() {
      this.playSound(SoundEvents.ENTITY_VILLAGER_CELEBRATE, this.getSoundVolume(), this.getSoundPitch());
   }

   public void writeCustomDataToTag(CompoundTag tag) {
      super.writeCustomDataToTag(tag);
      TradeOfferList tradeOfferList = this.getOffers();
      if (!tradeOfferList.isEmpty()) {
         tag.put("Offers", tradeOfferList.toTag());
      }

      tag.put("Inventory", this.inventory.getTags());
   }

   public void readCustomDataFromTag(CompoundTag tag) {
      super.readCustomDataFromTag(tag);
      if (tag.contains("Offers", 10)) {
         this.offers = new TradeOfferList(tag.getCompound("Offers"));
      }

      this.inventory.readTags(tag.getList("Inventory", 10));
   }

   @Nullable
   public Entity moveToWorld(ServerWorld destination) {
      this.resetCustomer();
      return super.moveToWorld(destination);
   }

   protected void resetCustomer() {
      this.setCurrentCustomer((PlayerEntity)null);
   }

   public void onDeath(DamageSource source) {
      super.onDeath(source);
      this.resetCustomer();
   }

   @Environment(EnvType.CLIENT)
   protected void produceParticles(ParticleEffect parameters) {
      for(int i = 0; i < 5; ++i) {
         double d = this.random.nextGaussian() * 0.02D;
         double e = this.random.nextGaussian() * 0.02D;
         double f = this.random.nextGaussian() * 0.02D;
         this.world.addParticle(parameters, this.getParticleX(1.0D), this.getRandomBodyY() + 1.0D, this.getParticleZ(1.0D), d, e, f);
      }

   }

   public boolean canBeLeashedBy(PlayerEntity player) {
      return false;
   }

   public SimpleInventory getInventory() {
      return this.inventory;
   }

   public boolean equip(int slot, ItemStack item) {
      if (super.equip(slot, item)) {
         return true;
      } else {
         int i = slot - 300;
         if (i >= 0 && i < this.inventory.size()) {
            this.inventory.setStack(i, item);
            return true;
         } else {
            return false;
         }
      }
   }

   public World getMerchantWorld() {
      return this.world;
   }

   protected abstract void fillRecipes();

   protected void fillRecipesFromPool(TradeOfferList recipeList, TradeOffers.Factory[] pool, int count) {
      Set<Integer> set = Sets.newHashSet();
      if (pool.length > count) {
         while(set.size() < count) {
            set.add(this.random.nextInt(pool.length));
         }
      } else {
         for(int i = 0; i < pool.length; ++i) {
            set.add(i);
         }
      }

      Iterator var9 = set.iterator();

      while(var9.hasNext()) {
         Integer integer = (Integer)var9.next();
         TradeOffers.Factory factory = pool[integer];
         TradeOffer tradeOffer = factory.create(this, this.random);
         if (tradeOffer != null) {
            recipeList.add(tradeOffer);
         }
      }

   }

   @Environment(EnvType.CLIENT)
   public Vec3d method_30951(float f) {
      float g = MathHelper.lerp(f, this.prevBodyYaw, this.bodyYaw) * 0.017453292F;
      Vec3d vec3d = new Vec3d(0.0D, this.getBoundingBox().getYLength() - 1.0D, 0.2D);
      return this.method_30950(f).add(vec3d.rotateY(-g));
   }

   static {
      HEAD_ROLLING_TIME_LEFT = DataTracker.registerData(MerchantEntity.class, TrackedDataHandlerRegistry.INTEGER);
   }
}
