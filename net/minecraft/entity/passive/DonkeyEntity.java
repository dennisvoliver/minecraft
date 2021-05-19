package net.minecraft.entity.passive;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class DonkeyEntity extends AbstractDonkeyEntity {
   public DonkeyEntity(EntityType<? extends DonkeyEntity> entityType, World world) {
      super(entityType, world);
   }

   protected SoundEvent getAmbientSound() {
      super.getAmbientSound();
      return SoundEvents.ENTITY_DONKEY_AMBIENT;
   }

   protected SoundEvent getAngrySound() {
      super.getAngrySound();
      return SoundEvents.ENTITY_DONKEY_ANGRY;
   }

   protected SoundEvent getDeathSound() {
      super.getDeathSound();
      return SoundEvents.ENTITY_DONKEY_DEATH;
   }

   @Nullable
   protected SoundEvent getEatSound() {
      return SoundEvents.ENTITY_DONKEY_EAT;
   }

   protected SoundEvent getHurtSound(DamageSource source) {
      super.getHurtSound(source);
      return SoundEvents.ENTITY_DONKEY_HURT;
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
      EntityType<? extends HorseBaseEntity> entityType = entity instanceof HorseEntity ? EntityType.MULE : EntityType.DONKEY;
      HorseBaseEntity horseBaseEntity = (HorseBaseEntity)entityType.create(world);
      this.setChildAttributes(entity, horseBaseEntity);
      return horseBaseEntity;
   }
}
