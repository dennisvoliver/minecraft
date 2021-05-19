package net.minecraft.entity.passive;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.world.World;

public class SalmonEntity extends SchoolingFishEntity {
   public SalmonEntity(EntityType<? extends SalmonEntity> entityType, World world) {
      super(entityType, world);
   }

   public int getMaxGroupSize() {
      return 5;
   }

   protected ItemStack getFishBucketItem() {
      return new ItemStack(Items.SALMON_BUCKET);
   }

   protected SoundEvent getAmbientSound() {
      return SoundEvents.ENTITY_SALMON_AMBIENT;
   }

   protected SoundEvent getDeathSound() {
      return SoundEvents.ENTITY_SALMON_DEATH;
   }

   protected SoundEvent getHurtSound(DamageSource source) {
      return SoundEvents.ENTITY_SALMON_HURT;
   }

   protected SoundEvent getFlopSound() {
      return SoundEvents.ENTITY_SALMON_FLOP;
   }
}
