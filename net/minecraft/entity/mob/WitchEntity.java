package net.minecraft.entity.mob;

import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.RangedAttackMob;
import net.minecraft.entity.ai.goal.DisableableFollowTargetGoal;
import net.minecraft.entity.ai.goal.LookAroundGoal;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.ai.goal.ProjectileAttackGoal;
import net.minecraft.entity.ai.goal.RaidGoal;
import net.minecraft.entity.ai.goal.RevengeGoal;
import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.entity.ai.goal.WanderAroundFarGoal;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.thrown.PotionEntity;
import net.minecraft.entity.raid.RaiderEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionUtil;
import net.minecraft.potion.Potions;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.tag.FluidTags;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class WitchEntity extends RaiderEntity implements RangedAttackMob {
   private static final UUID DRINKING_SPEED_PENALTY_MODIFIER_ID = UUID.fromString("5CD17E52-A79A-43D3-A529-90FDE04B181E");
   private static final EntityAttributeModifier DRINKING_SPEED_PENALTY_MODIFIER;
   private static final TrackedData<Boolean> DRINKING;
   private int drinkTimeLeft;
   private RaidGoal<RaiderEntity> raidGoal;
   private DisableableFollowTargetGoal<PlayerEntity> attackPlayerGoal;

   public WitchEntity(EntityType<? extends WitchEntity> entityType, World world) {
      super(entityType, world);
   }

   protected void initGoals() {
      super.initGoals();
      this.raidGoal = new RaidGoal(this, RaiderEntity.class, true, (livingEntity) -> {
         return livingEntity != null && this.hasActiveRaid() && livingEntity.getType() != EntityType.WITCH;
      });
      this.attackPlayerGoal = new DisableableFollowTargetGoal(this, PlayerEntity.class, 10, true, false, (Predicate)null);
      this.goalSelector.add(1, new SwimGoal(this));
      this.goalSelector.add(2, new ProjectileAttackGoal(this, 1.0D, 60, 10.0F));
      this.goalSelector.add(2, new WanderAroundFarGoal(this, 1.0D));
      this.goalSelector.add(3, new LookAtEntityGoal(this, PlayerEntity.class, 8.0F));
      this.goalSelector.add(3, new LookAroundGoal(this));
      this.targetSelector.add(1, new RevengeGoal(this, new Class[]{RaiderEntity.class}));
      this.targetSelector.add(2, this.raidGoal);
      this.targetSelector.add(3, this.attackPlayerGoal);
   }

   protected void initDataTracker() {
      super.initDataTracker();
      this.getDataTracker().startTracking(DRINKING, false);
   }

   protected SoundEvent getAmbientSound() {
      return SoundEvents.ENTITY_WITCH_AMBIENT;
   }

   protected SoundEvent getHurtSound(DamageSource source) {
      return SoundEvents.ENTITY_WITCH_HURT;
   }

   protected SoundEvent getDeathSound() {
      return SoundEvents.ENTITY_WITCH_DEATH;
   }

   public void setDrinking(boolean drinking) {
      this.getDataTracker().set(DRINKING, drinking);
   }

   public boolean isDrinking() {
      return (Boolean)this.getDataTracker().get(DRINKING);
   }

   public static DefaultAttributeContainer.Builder createWitchAttributes() {
      return HostileEntity.createHostileAttributes().add(EntityAttributes.GENERIC_MAX_HEALTH, 26.0D).add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.25D);
   }

   public void tickMovement() {
      if (!this.world.isClient && this.isAlive()) {
         this.raidGoal.decreaseCooldown();
         if (this.raidGoal.getCooldown() <= 0) {
            this.attackPlayerGoal.setEnabled(true);
         } else {
            this.attackPlayerGoal.setEnabled(false);
         }

         if (this.isDrinking()) {
            if (this.drinkTimeLeft-- <= 0) {
               this.setDrinking(false);
               ItemStack itemStack = this.getMainHandStack();
               this.equipStack(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
               if (itemStack.getItem() == Items.POTION) {
                  List<StatusEffectInstance> list = PotionUtil.getPotionEffects(itemStack);
                  if (list != null) {
                     Iterator var3 = list.iterator();

                     while(var3.hasNext()) {
                        StatusEffectInstance statusEffectInstance = (StatusEffectInstance)var3.next();
                        this.addStatusEffect(new StatusEffectInstance(statusEffectInstance));
                     }
                  }
               }

               this.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED).removeModifier(DRINKING_SPEED_PENALTY_MODIFIER);
            }
         } else {
            Potion potion = null;
            if (this.random.nextFloat() < 0.15F && this.isSubmergedIn(FluidTags.WATER) && !this.hasStatusEffect(StatusEffects.WATER_BREATHING)) {
               potion = Potions.WATER_BREATHING;
            } else if (this.random.nextFloat() < 0.15F && (this.isOnFire() || this.getRecentDamageSource() != null && this.getRecentDamageSource().isFire()) && !this.hasStatusEffect(StatusEffects.FIRE_RESISTANCE)) {
               potion = Potions.FIRE_RESISTANCE;
            } else if (this.random.nextFloat() < 0.05F && this.getHealth() < this.getMaxHealth()) {
               potion = Potions.HEALING;
            } else if (this.random.nextFloat() < 0.5F && this.getTarget() != null && !this.hasStatusEffect(StatusEffects.SPEED) && this.getTarget().squaredDistanceTo(this) > 121.0D) {
               potion = Potions.SWIFTNESS;
            }

            if (potion != null) {
               this.equipStack(EquipmentSlot.MAINHAND, PotionUtil.setPotion(new ItemStack(Items.POTION), potion));
               this.drinkTimeLeft = this.getMainHandStack().getMaxUseTime();
               this.setDrinking(true);
               if (!this.isSilent()) {
                  this.world.playSound((PlayerEntity)null, this.getX(), this.getY(), this.getZ(), SoundEvents.ENTITY_WITCH_DRINK, this.getSoundCategory(), 1.0F, 0.8F + this.random.nextFloat() * 0.4F);
               }

               EntityAttributeInstance entityAttributeInstance = this.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED);
               entityAttributeInstance.removeModifier(DRINKING_SPEED_PENALTY_MODIFIER);
               entityAttributeInstance.addTemporaryModifier(DRINKING_SPEED_PENALTY_MODIFIER);
            }
         }

         if (this.random.nextFloat() < 7.5E-4F) {
            this.world.sendEntityStatus(this, (byte)15);
         }
      }

      super.tickMovement();
   }

   public SoundEvent getCelebratingSound() {
      return SoundEvents.ENTITY_WITCH_CELEBRATE;
   }

   @Environment(EnvType.CLIENT)
   public void handleStatus(byte status) {
      if (status == 15) {
         for(int i = 0; i < this.random.nextInt(35) + 10; ++i) {
            this.world.addParticle(ParticleTypes.WITCH, this.getX() + this.random.nextGaussian() * 0.12999999523162842D, this.getBoundingBox().maxY + 0.5D + this.random.nextGaussian() * 0.12999999523162842D, this.getZ() + this.random.nextGaussian() * 0.12999999523162842D, 0.0D, 0.0D, 0.0D);
         }
      } else {
         super.handleStatus(status);
      }

   }

   protected float applyEnchantmentsToDamage(DamageSource source, float amount) {
      amount = super.applyEnchantmentsToDamage(source, amount);
      if (source.getAttacker() == this) {
         amount = 0.0F;
      }

      if (source.getMagic()) {
         amount = (float)((double)amount * 0.15D);
      }

      return amount;
   }

   public void attack(LivingEntity target, float pullProgress) {
      if (!this.isDrinking()) {
         Vec3d vec3d = target.getVelocity();
         double d = target.getX() + vec3d.x - this.getX();
         double e = target.getEyeY() - 1.100000023841858D - this.getY();
         double f = target.getZ() + vec3d.z - this.getZ();
         float g = MathHelper.sqrt(d * d + f * f);
         Potion potion = Potions.HARMING;
         if (target instanceof RaiderEntity) {
            if (target.getHealth() <= 4.0F) {
               potion = Potions.HEALING;
            } else {
               potion = Potions.REGENERATION;
            }

            this.setTarget((LivingEntity)null);
         } else if (g >= 8.0F && !target.hasStatusEffect(StatusEffects.SLOWNESS)) {
            potion = Potions.SLOWNESS;
         } else if (target.getHealth() >= 8.0F && !target.hasStatusEffect(StatusEffects.POISON)) {
            potion = Potions.POISON;
         } else if (g <= 3.0F && !target.hasStatusEffect(StatusEffects.WEAKNESS) && this.random.nextFloat() < 0.25F) {
            potion = Potions.WEAKNESS;
         }

         PotionEntity potionEntity = new PotionEntity(this.world, this);
         potionEntity.setItem(PotionUtil.setPotion(new ItemStack(Items.SPLASH_POTION), potion));
         potionEntity.pitch -= -20.0F;
         potionEntity.setVelocity(d, e + (double)(g * 0.2F), f, 0.75F, 8.0F);
         if (!this.isSilent()) {
            this.world.playSound((PlayerEntity)null, this.getX(), this.getY(), this.getZ(), SoundEvents.ENTITY_WITCH_THROW, this.getSoundCategory(), 1.0F, 0.8F + this.random.nextFloat() * 0.4F);
         }

         this.world.spawnEntity(potionEntity);
      }
   }

   protected float getActiveEyeHeight(EntityPose pose, EntityDimensions dimensions) {
      return 1.62F;
   }

   public void addBonusForWave(int wave, boolean unused) {
   }

   public boolean canLead() {
      return false;
   }

   static {
      DRINKING_SPEED_PENALTY_MODIFIER = new EntityAttributeModifier(DRINKING_SPEED_PENALTY_MODIFIER_ID, "Drinking speed penalty", -0.25D, EntityAttributeModifier.Operation.ADDITION);
      DRINKING = DataTracker.registerData(WitchEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
   }
}
