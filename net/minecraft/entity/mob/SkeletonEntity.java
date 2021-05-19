package net.minecraft.entity.mob;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.item.Items;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.world.World;

public class SkeletonEntity extends AbstractSkeletonEntity {
   public SkeletonEntity(EntityType<? extends SkeletonEntity> entityType, World world) {
      super(entityType, world);
   }

   protected SoundEvent getAmbientSound() {
      return SoundEvents.ENTITY_SKELETON_AMBIENT;
   }

   protected SoundEvent getHurtSound(DamageSource source) {
      return SoundEvents.ENTITY_SKELETON_HURT;
   }

   protected SoundEvent getDeathSound() {
      return SoundEvents.ENTITY_SKELETON_DEATH;
   }

   SoundEvent getStepSound() {
      return SoundEvents.ENTITY_SKELETON_STEP;
   }

   protected void dropEquipment(DamageSource source, int lootingMultiplier, boolean allowDrops) {
      super.dropEquipment(source, lootingMultiplier, allowDrops);
      Entity entity = source.getAttacker();
      if (entity instanceof CreeperEntity) {
         CreeperEntity creeperEntity = (CreeperEntity)entity;
         if (creeperEntity.shouldDropHead()) {
            creeperEntity.onHeadDropped();
            this.dropItem(Items.SKELETON_SKULL);
         }
      }

   }
}
