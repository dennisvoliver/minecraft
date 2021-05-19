package net.minecraft.entity.mob;

import com.google.common.collect.Maps;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.CrossbowUser;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityData;
import net.minecraft.entity.EntityGroup;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.goal.CrossbowAttackGoal;
import net.minecraft.entity.ai.goal.FollowTargetGoal;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.ai.goal.RevengeGoal;
import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.entity.ai.goal.WanderAroundGoal;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.passive.IronGolemEntity;
import net.minecraft.entity.passive.MerchantEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.entity.raid.RaiderEntity;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.BannerItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.RangedWeaponItem;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.village.raid.Raid;
import net.minecraft.world.Difficulty;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import org.jetbrains.annotations.Nullable;

public class PillagerEntity extends IllagerEntity implements CrossbowUser {
   private static final TrackedData<Boolean> CHARGING;
   private final SimpleInventory inventory = new SimpleInventory(5);

   public PillagerEntity(EntityType<? extends PillagerEntity> entityType, World world) {
      super(entityType, world);
   }

   protected void initGoals() {
      super.initGoals();
      this.goalSelector.add(0, new SwimGoal(this));
      this.goalSelector.add(2, new RaiderEntity.PatrolApproachGoal(this, 10.0F));
      this.goalSelector.add(3, new CrossbowAttackGoal(this, 1.0D, 8.0F));
      this.goalSelector.add(8, new WanderAroundGoal(this, 0.6D));
      this.goalSelector.add(9, new LookAtEntityGoal(this, PlayerEntity.class, 15.0F, 1.0F));
      this.goalSelector.add(10, new LookAtEntityGoal(this, MobEntity.class, 15.0F));
      this.targetSelector.add(1, (new RevengeGoal(this, new Class[]{RaiderEntity.class})).setGroupRevenge());
      this.targetSelector.add(2, new FollowTargetGoal(this, PlayerEntity.class, true));
      this.targetSelector.add(3, new FollowTargetGoal(this, MerchantEntity.class, false));
      this.targetSelector.add(3, new FollowTargetGoal(this, IronGolemEntity.class, true));
   }

   public static DefaultAttributeContainer.Builder createPillagerAttributes() {
      return HostileEntity.createHostileAttributes().add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.3499999940395355D).add(EntityAttributes.GENERIC_MAX_HEALTH, 24.0D).add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 5.0D).add(EntityAttributes.GENERIC_FOLLOW_RANGE, 32.0D);
   }

   protected void initDataTracker() {
      super.initDataTracker();
      this.dataTracker.startTracking(CHARGING, false);
   }

   public boolean canUseRangedWeapon(RangedWeaponItem weapon) {
      return weapon == Items.CROSSBOW;
   }

   @Environment(EnvType.CLIENT)
   public boolean isCharging() {
      return (Boolean)this.dataTracker.get(CHARGING);
   }

   public void setCharging(boolean charging) {
      this.dataTracker.set(CHARGING, charging);
   }

   public void postShoot() {
      this.despawnCounter = 0;
   }

   public void writeCustomDataToTag(CompoundTag tag) {
      super.writeCustomDataToTag(tag);
      ListTag listTag = new ListTag();

      for(int i = 0; i < this.inventory.size(); ++i) {
         ItemStack itemStack = this.inventory.getStack(i);
         if (!itemStack.isEmpty()) {
            listTag.add(itemStack.toTag(new CompoundTag()));
         }
      }

      tag.put("Inventory", listTag);
   }

   @Environment(EnvType.CLIENT)
   public IllagerEntity.State getState() {
      if (this.isCharging()) {
         return IllagerEntity.State.CROSSBOW_CHARGE;
      } else if (this.isHolding(Items.CROSSBOW)) {
         return IllagerEntity.State.CROSSBOW_HOLD;
      } else {
         return this.isAttacking() ? IllagerEntity.State.ATTACKING : IllagerEntity.State.NEUTRAL;
      }
   }

   public void readCustomDataFromTag(CompoundTag tag) {
      super.readCustomDataFromTag(tag);
      ListTag listTag = tag.getList("Inventory", 10);

      for(int i = 0; i < listTag.size(); ++i) {
         ItemStack itemStack = ItemStack.fromTag(listTag.getCompound(i));
         if (!itemStack.isEmpty()) {
            this.inventory.addStack(itemStack);
         }
      }

      this.setCanPickUpLoot(true);
   }

   public float getPathfindingFavor(BlockPos pos, WorldView world) {
      BlockState blockState = world.getBlockState(pos.down());
      return !blockState.isOf(Blocks.GRASS_BLOCK) && !blockState.isOf(Blocks.SAND) ? 0.5F - world.getBrightness(pos) : 10.0F;
   }

   public int getLimitPerChunk() {
      return 1;
   }

   @Nullable
   public EntityData initialize(ServerWorldAccess world, LocalDifficulty difficulty, SpawnReason spawnReason, @Nullable EntityData entityData, @Nullable CompoundTag entityTag) {
      this.initEquipment(difficulty);
      this.updateEnchantments(difficulty);
      return super.initialize(world, difficulty, spawnReason, entityData, entityTag);
   }

   protected void initEquipment(LocalDifficulty difficulty) {
      this.equipStack(EquipmentSlot.MAINHAND, new ItemStack(Items.CROSSBOW));
   }

   protected void method_30759(float f) {
      super.method_30759(f);
      if (this.random.nextInt(300) == 0) {
         ItemStack itemStack = this.getMainHandStack();
         if (itemStack.getItem() == Items.CROSSBOW) {
            Map<Enchantment, Integer> map = EnchantmentHelper.get(itemStack);
            map.putIfAbsent(Enchantments.PIERCING, 1);
            EnchantmentHelper.set(map, itemStack);
            this.equipStack(EquipmentSlot.MAINHAND, itemStack);
         }
      }

   }

   public boolean isTeammate(Entity other) {
      if (super.isTeammate(other)) {
         return true;
      } else if (other instanceof LivingEntity && ((LivingEntity)other).getGroup() == EntityGroup.ILLAGER) {
         return this.getScoreboardTeam() == null && other.getScoreboardTeam() == null;
      } else {
         return false;
      }
   }

   protected SoundEvent getAmbientSound() {
      return SoundEvents.ENTITY_PILLAGER_AMBIENT;
   }

   protected SoundEvent getDeathSound() {
      return SoundEvents.ENTITY_PILLAGER_DEATH;
   }

   protected SoundEvent getHurtSound(DamageSource source) {
      return SoundEvents.ENTITY_PILLAGER_HURT;
   }

   public void attack(LivingEntity target, float pullProgress) {
      this.shoot(this, 1.6F);
   }

   public void shoot(LivingEntity target, ItemStack crossbow, ProjectileEntity projectile, float multiShotSpray) {
      this.shoot(this, target, projectile, multiShotSpray, 1.6F);
   }

   protected void loot(ItemEntity item) {
      ItemStack itemStack = item.getStack();
      if (itemStack.getItem() instanceof BannerItem) {
         super.loot(item);
      } else {
         Item item2 = itemStack.getItem();
         if (this.method_7111(item2)) {
            this.method_29499(item);
            ItemStack itemStack2 = this.inventory.addStack(itemStack);
            if (itemStack2.isEmpty()) {
               item.remove();
            } else {
               itemStack.setCount(itemStack2.getCount());
            }
         }
      }

   }

   private boolean method_7111(Item item) {
      return this.hasActiveRaid() && item == Items.WHITE_BANNER;
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

   public void addBonusForWave(int wave, boolean unused) {
      Raid raid = this.getRaid();
      boolean bl = this.random.nextFloat() <= raid.getEnchantmentChance();
      if (bl) {
         ItemStack itemStack = new ItemStack(Items.CROSSBOW);
         Map<Enchantment, Integer> map = Maps.newHashMap();
         if (wave > raid.getMaxWaves(Difficulty.NORMAL)) {
            map.put(Enchantments.QUICK_CHARGE, 2);
         } else if (wave > raid.getMaxWaves(Difficulty.EASY)) {
            map.put(Enchantments.QUICK_CHARGE, 1);
         }

         map.put(Enchantments.MULTISHOT, 1);
         EnchantmentHelper.set(map, itemStack);
         this.equipStack(EquipmentSlot.MAINHAND, itemStack);
      }

   }

   public SoundEvent getCelebratingSound() {
      return SoundEvents.ENTITY_PILLAGER_CELEBRATE;
   }

   static {
      CHARGING = DataTracker.registerData(PillagerEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
   }
}
