package net.minecraft.entity.passive;

import java.util.UUID;
import net.minecraft.entity.EntityData;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.HorseArmorItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Util;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class HorseEntity extends HorseBaseEntity {
   private static final UUID HORSE_ARMOR_BONUS_ID = UUID.fromString("556E1665-8B10-40C8-8F9D-CF9B1667F295");
   private static final TrackedData<Integer> VARIANT;

   public HorseEntity(EntityType<? extends HorseEntity> entityType, World world) {
      super(entityType, world);
   }

   protected void initAttributes() {
      this.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH).setBaseValue((double)this.getChildHealthBonus());
      this.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED).setBaseValue(this.getChildMovementSpeedBonus());
      this.getAttributeInstance(EntityAttributes.HORSE_JUMP_STRENGTH).setBaseValue(this.getChildJumpStrengthBonus());
   }

   protected void initDataTracker() {
      super.initDataTracker();
      this.dataTracker.startTracking(VARIANT, 0);
   }

   public void writeCustomDataToTag(CompoundTag tag) {
      super.writeCustomDataToTag(tag);
      tag.putInt("Variant", this.getVariant());
      if (!this.items.getStack(1).isEmpty()) {
         tag.put("ArmorItem", this.items.getStack(1).toTag(new CompoundTag()));
      }

   }

   public ItemStack getArmorType() {
      return this.getEquippedStack(EquipmentSlot.CHEST);
   }

   private void equipArmor(ItemStack stack) {
      this.equipStack(EquipmentSlot.CHEST, stack);
      this.setEquipmentDropChance(EquipmentSlot.CHEST, 0.0F);
   }

   public void readCustomDataFromTag(CompoundTag tag) {
      super.readCustomDataFromTag(tag);
      this.setVariant(tag.getInt("Variant"));
      if (tag.contains("ArmorItem", 10)) {
         ItemStack itemStack = ItemStack.fromTag(tag.getCompound("ArmorItem"));
         if (!itemStack.isEmpty() && this.isHorseArmor(itemStack)) {
            this.items.setStack(1, itemStack);
         }
      }

      this.updateSaddle();
   }

   private void setVariant(int variant) {
      this.dataTracker.set(VARIANT, variant);
   }

   private int getVariant() {
      return (Integer)this.dataTracker.get(VARIANT);
   }

   private void setVariant(HorseColor color, HorseMarking marking) {
      this.setVariant(color.getIndex() & 255 | marking.getIndex() << 8 & '\uff00');
   }

   public HorseColor getColor() {
      return HorseColor.byIndex(this.getVariant() & 255);
   }

   public HorseMarking getMarking() {
      return HorseMarking.byIndex((this.getVariant() & '\uff00') >> 8);
   }

   protected void updateSaddle() {
      if (!this.world.isClient) {
         super.updateSaddle();
         this.setArmorTypeFromStack(this.items.getStack(1));
         this.setEquipmentDropChance(EquipmentSlot.CHEST, 0.0F);
      }
   }

   private void setArmorTypeFromStack(ItemStack stack) {
      this.equipArmor(stack);
      if (!this.world.isClient) {
         this.getAttributeInstance(EntityAttributes.GENERIC_ARMOR).removeModifier(HORSE_ARMOR_BONUS_ID);
         if (this.isHorseArmor(stack)) {
            int i = ((HorseArmorItem)stack.getItem()).getBonus();
            if (i != 0) {
               this.getAttributeInstance(EntityAttributes.GENERIC_ARMOR).addTemporaryModifier(new EntityAttributeModifier(HORSE_ARMOR_BONUS_ID, "Horse armor bonus", (double)i, EntityAttributeModifier.Operation.ADDITION));
            }
         }
      }

   }

   public void onInventoryChanged(Inventory sender) {
      ItemStack itemStack = this.getArmorType();
      super.onInventoryChanged(sender);
      ItemStack itemStack2 = this.getArmorType();
      if (this.age > 20 && this.isHorseArmor(itemStack2) && itemStack != itemStack2) {
         this.playSound(SoundEvents.ENTITY_HORSE_ARMOR, 0.5F, 1.0F);
      }

   }

   protected void playWalkSound(BlockSoundGroup group) {
      super.playWalkSound(group);
      if (this.random.nextInt(10) == 0) {
         this.playSound(SoundEvents.ENTITY_HORSE_BREATHE, group.getVolume() * 0.6F, group.getPitch());
      }

   }

   protected SoundEvent getAmbientSound() {
      super.getAmbientSound();
      return SoundEvents.ENTITY_HORSE_AMBIENT;
   }

   protected SoundEvent getDeathSound() {
      super.getDeathSound();
      return SoundEvents.ENTITY_HORSE_DEATH;
   }

   @Nullable
   protected SoundEvent getEatSound() {
      return SoundEvents.ENTITY_HORSE_EAT;
   }

   protected SoundEvent getHurtSound(DamageSource source) {
      super.getHurtSound(source);
      return SoundEvents.ENTITY_HORSE_HURT;
   }

   protected SoundEvent getAngrySound() {
      super.getAngrySound();
      return SoundEvents.ENTITY_HORSE_ANGRY;
   }

   public ActionResult interactMob(PlayerEntity player, Hand hand) {
      ItemStack itemStack = player.getStackInHand(hand);
      if (!this.isBaby()) {
         if (this.isTame() && player.shouldCancelInteraction()) {
            this.openInventory(player);
            return ActionResult.success(this.world.isClient);
         }

         if (this.hasPassengers()) {
            return super.interactMob(player, hand);
         }
      }

      if (!itemStack.isEmpty()) {
         if (this.isBreedingItem(itemStack)) {
            return this.method_30009(player, itemStack);
         }

         ActionResult actionResult = itemStack.useOnEntity(player, this, hand);
         if (actionResult.isAccepted()) {
            return actionResult;
         }

         if (!this.isTame()) {
            this.playAngrySound();
            return ActionResult.success(this.world.isClient);
         }

         boolean bl = !this.isBaby() && !this.isSaddled() && itemStack.getItem() == Items.SADDLE;
         if (this.isHorseArmor(itemStack) || bl) {
            this.openInventory(player);
            return ActionResult.success(this.world.isClient);
         }
      }

      if (this.isBaby()) {
         return super.interactMob(player, hand);
      } else {
         this.putPlayerOnBack(player);
         return ActionResult.success(this.world.isClient);
      }
   }

   public boolean canBreedWith(AnimalEntity other) {
      if (other == this) {
         return false;
      } else if (!(other instanceof DonkeyEntity) && !(other instanceof HorseEntity)) {
         return false;
      } else {
         return this.canBreed() && ((HorseBaseEntity)other).canBreed();
      }
   }

   public PassiveEntity createChild(ServerWorld world, PassiveEntity entity) {
      HorseBaseEntity horseBaseEntity2;
      if (entity instanceof DonkeyEntity) {
         horseBaseEntity2 = (HorseBaseEntity)EntityType.MULE.create(world);
      } else {
         HorseEntity horseEntity = (HorseEntity)entity;
         horseBaseEntity2 = (HorseBaseEntity)EntityType.HORSE.create(world);
         int i = this.random.nextInt(9);
         HorseColor horseColor3;
         if (i < 4) {
            horseColor3 = this.getColor();
         } else if (i < 8) {
            horseColor3 = horseEntity.getColor();
         } else {
            horseColor3 = (HorseColor)Util.getRandom((Object[])HorseColor.values(), this.random);
         }

         int j = this.random.nextInt(5);
         HorseMarking horseMarking3;
         if (j < 2) {
            horseMarking3 = this.getMarking();
         } else if (j < 4) {
            horseMarking3 = horseEntity.getMarking();
         } else {
            horseMarking3 = (HorseMarking)Util.getRandom((Object[])HorseMarking.values(), this.random);
         }

         ((HorseEntity)horseBaseEntity2).setVariant(horseColor3, horseMarking3);
      }

      this.setChildAttributes(entity, horseBaseEntity2);
      return horseBaseEntity2;
   }

   public boolean hasArmorSlot() {
      return true;
   }

   public boolean isHorseArmor(ItemStack item) {
      return item.getItem() instanceof HorseArmorItem;
   }

   @Nullable
   public EntityData initialize(ServerWorldAccess world, LocalDifficulty difficulty, SpawnReason spawnReason, @Nullable EntityData entityData, @Nullable CompoundTag entityTag) {
      HorseColor horseColor2;
      if (entityData instanceof HorseEntity.HorseData) {
         horseColor2 = ((HorseEntity.HorseData)entityData).color;
      } else {
         horseColor2 = (HorseColor)Util.getRandom((Object[])HorseColor.values(), this.random);
         entityData = new HorseEntity.HorseData(horseColor2);
      }

      this.setVariant(horseColor2, (HorseMarking)Util.getRandom((Object[])HorseMarking.values(), this.random));
      return super.initialize(world, difficulty, spawnReason, (EntityData)entityData, entityTag);
   }

   static {
      VARIANT = DataTracker.registerData(HorseEntity.class, TrackedDataHandlerRegistry.INTEGER);
   }

   public static class HorseData extends PassiveEntity.PassiveData {
      public final HorseColor color;

      public HorseData(HorseColor color) {
         super(true);
         this.color = color;
      }
   }
}
