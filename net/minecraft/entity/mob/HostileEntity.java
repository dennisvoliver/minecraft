package net.minecraft.entity.mob;

import java.util.Random;
import java.util.function.Predicate;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.RangedWeaponItem;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Difficulty;
import net.minecraft.world.LightType;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;

public abstract class HostileEntity extends PathAwareEntity implements Monster {
   protected HostileEntity(EntityType<? extends HostileEntity> entityType, World world) {
      super(entityType, world);
      this.experiencePoints = 5;
   }

   public SoundCategory getSoundCategory() {
      return SoundCategory.HOSTILE;
   }

   public void tickMovement() {
      this.tickHandSwing();
      this.updateDespawnCounter();
      super.tickMovement();
   }

   protected void updateDespawnCounter() {
      float f = this.getBrightnessAtEyes();
      if (f > 0.5F) {
         this.despawnCounter += 2;
      }

   }

   protected boolean isDisallowedInPeaceful() {
      return true;
   }

   protected SoundEvent getSwimSound() {
      return SoundEvents.ENTITY_HOSTILE_SWIM;
   }

   protected SoundEvent getSplashSound() {
      return SoundEvents.ENTITY_HOSTILE_SPLASH;
   }

   public boolean damage(DamageSource source, float amount) {
      return this.isInvulnerableTo(source) ? false : super.damage(source, amount);
   }

   protected SoundEvent getHurtSound(DamageSource source) {
      return SoundEvents.ENTITY_HOSTILE_HURT;
   }

   protected SoundEvent getDeathSound() {
      return SoundEvents.ENTITY_HOSTILE_DEATH;
   }

   protected SoundEvent getFallSound(int distance) {
      return distance > 4 ? SoundEvents.ENTITY_HOSTILE_BIG_FALL : SoundEvents.ENTITY_HOSTILE_SMALL_FALL;
   }

   public float getPathfindingFavor(BlockPos pos, WorldView world) {
      return 0.5F - world.getBrightness(pos);
   }

   public static boolean isSpawnDark(ServerWorldAccess serverWorldAccess, BlockPos pos, Random random) {
      if (serverWorldAccess.getLightLevel(LightType.SKY, pos) > random.nextInt(32)) {
         return false;
      } else {
         int i = serverWorldAccess.toServerWorld().isThundering() ? serverWorldAccess.getLightLevel(pos, 10) : serverWorldAccess.getLightLevel(pos);
         return i <= random.nextInt(8);
      }
   }

   public static boolean canSpawnInDark(EntityType<? extends HostileEntity> type, ServerWorldAccess serverWorldAccess, SpawnReason spawnReason, BlockPos pos, Random random) {
      return serverWorldAccess.getDifficulty() != Difficulty.PEACEFUL && isSpawnDark(serverWorldAccess, pos, random) && canMobSpawn(type, serverWorldAccess, spawnReason, pos, random);
   }

   public static boolean canSpawnIgnoreLightLevel(EntityType<? extends HostileEntity> type, WorldAccess world, SpawnReason spawnReason, BlockPos pos, Random random) {
      return world.getDifficulty() != Difficulty.PEACEFUL && canMobSpawn(type, world, spawnReason, pos, random);
   }

   public static DefaultAttributeContainer.Builder createHostileAttributes() {
      return MobEntity.createMobAttributes().add(EntityAttributes.GENERIC_ATTACK_DAMAGE);
   }

   protected boolean canDropLootAndXp() {
      return true;
   }

   protected boolean shouldDropLoot() {
      return true;
   }

   public boolean isAngryAt(PlayerEntity player) {
      return true;
   }

   public ItemStack getArrowType(ItemStack stack) {
      if (stack.getItem() instanceof RangedWeaponItem) {
         Predicate<ItemStack> predicate = ((RangedWeaponItem)stack.getItem()).getHeldProjectiles();
         ItemStack itemStack = RangedWeaponItem.getHeldProjectile(this, predicate);
         return itemStack.isEmpty() ? new ItemStack(Items.ARROW) : itemStack;
      } else {
         return ItemStack.EMPTY;
      }
   }
}
