package net.minecraft.entity.mob;

import net.minecraft.entity.EntityGroup;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.passive.HorseBaseEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class ZombieHorseEntity extends HorseBaseEntity {
   public ZombieHorseEntity(EntityType<? extends ZombieHorseEntity> entityType, World world) {
      super(entityType, world);
   }

   public static DefaultAttributeContainer.Builder createZombieHorseAttributes() {
      return createBaseHorseAttributes().add(EntityAttributes.GENERIC_MAX_HEALTH, 15.0D).add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.20000000298023224D);
   }

   protected void initAttributes() {
      this.getAttributeInstance(EntityAttributes.HORSE_JUMP_STRENGTH).setBaseValue(this.getChildJumpStrengthBonus());
   }

   public EntityGroup getGroup() {
      return EntityGroup.UNDEAD;
   }

   protected SoundEvent getAmbientSound() {
      super.getAmbientSound();
      return SoundEvents.ENTITY_ZOMBIE_HORSE_AMBIENT;
   }

   protected SoundEvent getDeathSound() {
      super.getDeathSound();
      return SoundEvents.ENTITY_ZOMBIE_HORSE_DEATH;
   }

   protected SoundEvent getHurtSound(DamageSource source) {
      super.getHurtSound(source);
      return SoundEvents.ENTITY_ZOMBIE_HORSE_HURT;
   }

   @Nullable
   public PassiveEntity createChild(ServerWorld world, PassiveEntity entity) {
      return (PassiveEntity)EntityType.ZOMBIE_HORSE.create(world);
   }

   public ActionResult interactMob(PlayerEntity player, Hand hand) {
      ItemStack itemStack = player.getStackInHand(hand);
      if (!this.isTame()) {
         return ActionResult.PASS;
      } else if (this.isBaby()) {
         return super.interactMob(player, hand);
      } else if (player.shouldCancelInteraction()) {
         this.openInventory(player);
         return ActionResult.success(this.world.isClient);
      } else if (this.hasPassengers()) {
         return super.interactMob(player, hand);
      } else {
         if (!itemStack.isEmpty()) {
            if (itemStack.getItem() == Items.SADDLE && !this.isSaddled()) {
               this.openInventory(player);
               return ActionResult.success(this.world.isClient);
            }

            ActionResult actionResult = itemStack.useOnEntity(player, this, hand);
            if (actionResult.isAccepted()) {
               return actionResult;
            }
         }

         this.putPlayerOnBack(player);
         return ActionResult.success(this.world.isClient);
      }
   }

   protected void initCustomGoals() {
   }
}
