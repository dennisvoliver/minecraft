package net.minecraft.entity.mob;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityData;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.goal.FollowTargetGoal;
import net.minecraft.entity.ai.pathing.PathNodeType;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class WitherSkeletonEntity extends AbstractSkeletonEntity {
   public WitherSkeletonEntity(EntityType<? extends WitherSkeletonEntity> entityType, World world) {
      super(entityType, world);
      this.setPathfindingPenalty(PathNodeType.LAVA, 8.0F);
   }

   protected void initGoals() {
      this.targetSelector.add(3, new FollowTargetGoal(this, AbstractPiglinEntity.class, true));
      super.initGoals();
   }

   protected SoundEvent getAmbientSound() {
      return SoundEvents.ENTITY_WITHER_SKELETON_AMBIENT;
   }

   protected SoundEvent getHurtSound(DamageSource source) {
      return SoundEvents.ENTITY_WITHER_SKELETON_HURT;
   }

   protected SoundEvent getDeathSound() {
      return SoundEvents.ENTITY_WITHER_SKELETON_DEATH;
   }

   SoundEvent getStepSound() {
      return SoundEvents.ENTITY_WITHER_SKELETON_STEP;
   }

   protected void dropEquipment(DamageSource source, int lootingMultiplier, boolean allowDrops) {
      super.dropEquipment(source, lootingMultiplier, allowDrops);
      Entity entity = source.getAttacker();
      if (entity instanceof CreeperEntity) {
         CreeperEntity creeperEntity = (CreeperEntity)entity;
         if (creeperEntity.shouldDropHead()) {
            creeperEntity.onHeadDropped();
            this.dropItem(Items.WITHER_SKELETON_SKULL);
         }
      }

   }

   protected void initEquipment(LocalDifficulty difficulty) {
      this.equipStack(EquipmentSlot.MAINHAND, new ItemStack(Items.STONE_SWORD));
   }

   protected void updateEnchantments(LocalDifficulty difficulty) {
   }

   @Nullable
   public EntityData initialize(ServerWorldAccess world, LocalDifficulty difficulty, SpawnReason spawnReason, @Nullable EntityData entityData, @Nullable CompoundTag entityTag) {
      EntityData entityData2 = super.initialize(world, difficulty, spawnReason, entityData, entityTag);
      this.getAttributeInstance(EntityAttributes.GENERIC_ATTACK_DAMAGE).setBaseValue(4.0D);
      this.updateAttackType();
      return entityData2;
   }

   protected float getActiveEyeHeight(EntityPose pose, EntityDimensions dimensions) {
      return 2.1F;
   }

   public boolean tryAttack(Entity target) {
      if (!super.tryAttack(target)) {
         return false;
      } else {
         if (target instanceof LivingEntity) {
            ((LivingEntity)target).addStatusEffect(new StatusEffectInstance(StatusEffects.WITHER, 200));
         }

         return true;
      }
   }

   protected PersistentProjectileEntity createArrowProjectile(ItemStack arrow, float damageModifier) {
      PersistentProjectileEntity persistentProjectileEntity = super.createArrowProjectile(arrow, damageModifier);
      persistentProjectileEntity.setOnFireFor(100);
      return persistentProjectileEntity;
   }

   public boolean canHaveStatusEffect(StatusEffectInstance effect) {
      return effect.getEffectType() == StatusEffects.WITHER ? false : super.canHaveStatusEffect(effect);
   }
}
