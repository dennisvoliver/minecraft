package net.minecraft.entity.mob;

import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.LeavesBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.FollowTargetGoal;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.entity.ai.goal.RevengeGoal;
import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.entity.ai.goal.WanderAroundFarGoal;
import net.minecraft.entity.ai.pathing.EntityNavigation;
import net.minecraft.entity.ai.pathing.LandPathNodeMaker;
import net.minecraft.entity.ai.pathing.MobNavigation;
import net.minecraft.entity.ai.pathing.PathNodeNavigator;
import net.minecraft.entity.ai.pathing.PathNodeType;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.passive.IronGolemEntity;
import net.minecraft.entity.passive.MerchantEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.raid.RaiderEntity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.tag.EntityTypeTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.BlockView;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import org.jetbrains.annotations.Nullable;

public class RavagerEntity extends RaiderEntity {
   private static final Predicate<Entity> IS_NOT_RAVAGER = (entity) -> {
      return entity.isAlive() && !(entity instanceof RavagerEntity);
   };
   private int attackTick;
   private int stunTick;
   private int roarTick;

   public RavagerEntity(EntityType<? extends RavagerEntity> entityType, World world) {
      super(entityType, world);
      this.stepHeight = 1.0F;
      this.experiencePoints = 20;
   }

   protected void initGoals() {
      super.initGoals();
      this.goalSelector.add(0, new SwimGoal(this));
      this.goalSelector.add(4, new RavagerEntity.AttackGoal());
      this.goalSelector.add(5, new WanderAroundFarGoal(this, 0.4D));
      this.goalSelector.add(6, new LookAtEntityGoal(this, PlayerEntity.class, 6.0F));
      this.goalSelector.add(10, new LookAtEntityGoal(this, MobEntity.class, 8.0F));
      this.targetSelector.add(2, (new RevengeGoal(this, new Class[]{RaiderEntity.class})).setGroupRevenge());
      this.targetSelector.add(3, new FollowTargetGoal(this, PlayerEntity.class, true));
      this.targetSelector.add(4, new FollowTargetGoal(this, MerchantEntity.class, true));
      this.targetSelector.add(4, new FollowTargetGoal(this, IronGolemEntity.class, true));
   }

   protected void updateGoalControls() {
      boolean bl = !(this.getPrimaryPassenger() instanceof MobEntity) || this.getPrimaryPassenger().getType().isIn(EntityTypeTags.RAIDERS);
      boolean bl2 = !(this.getVehicle() instanceof BoatEntity);
      this.goalSelector.setControlEnabled(Goal.Control.MOVE, bl);
      this.goalSelector.setControlEnabled(Goal.Control.JUMP, bl && bl2);
      this.goalSelector.setControlEnabled(Goal.Control.LOOK, bl);
      this.goalSelector.setControlEnabled(Goal.Control.TARGET, bl);
   }

   public static DefaultAttributeContainer.Builder createRavagerAttributes() {
      return HostileEntity.createHostileAttributes().add(EntityAttributes.GENERIC_MAX_HEALTH, 100.0D).add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.3D).add(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE, 0.75D).add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 12.0D).add(EntityAttributes.GENERIC_ATTACK_KNOCKBACK, 1.5D).add(EntityAttributes.GENERIC_FOLLOW_RANGE, 32.0D);
   }

   public void writeCustomDataToTag(CompoundTag tag) {
      super.writeCustomDataToTag(tag);
      tag.putInt("AttackTick", this.attackTick);
      tag.putInt("StunTick", this.stunTick);
      tag.putInt("RoarTick", this.roarTick);
   }

   public void readCustomDataFromTag(CompoundTag tag) {
      super.readCustomDataFromTag(tag);
      this.attackTick = tag.getInt("AttackTick");
      this.stunTick = tag.getInt("StunTick");
      this.roarTick = tag.getInt("RoarTick");
   }

   public SoundEvent getCelebratingSound() {
      return SoundEvents.ENTITY_RAVAGER_CELEBRATE;
   }

   protected EntityNavigation createNavigation(World world) {
      return new RavagerEntity.Navigation(this, world);
   }

   public int getBodyYawSpeed() {
      return 45;
   }

   public double getMountedHeightOffset() {
      return 2.1D;
   }

   public boolean canBeControlledByRider() {
      return !this.isAiDisabled() && this.getPrimaryPassenger() instanceof LivingEntity;
   }

   @Nullable
   public Entity getPrimaryPassenger() {
      return this.getPassengerList().isEmpty() ? null : (Entity)this.getPassengerList().get(0);
   }

   public void tickMovement() {
      super.tickMovement();
      if (this.isAlive()) {
         if (this.isImmobile()) {
            this.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED).setBaseValue(0.0D);
         } else {
            double d = this.getTarget() != null ? 0.35D : 0.3D;
            double e = this.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED).getBaseValue();
            this.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED).setBaseValue(MathHelper.lerp(0.1D, e, d));
         }

         if (this.horizontalCollision && this.world.getGameRules().getBoolean(GameRules.DO_MOB_GRIEFING)) {
            boolean bl = false;
            Box box = this.getBoundingBox().expand(0.2D);
            Iterator var8 = BlockPos.iterate(MathHelper.floor(box.minX), MathHelper.floor(box.minY), MathHelper.floor(box.minZ), MathHelper.floor(box.maxX), MathHelper.floor(box.maxY), MathHelper.floor(box.maxZ)).iterator();

            label60:
            while(true) {
               BlockPos blockPos;
               Block block;
               do {
                  if (!var8.hasNext()) {
                     if (!bl && this.onGround) {
                        this.jump();
                     }
                     break label60;
                  }

                  blockPos = (BlockPos)var8.next();
                  BlockState blockState = this.world.getBlockState(blockPos);
                  block = blockState.getBlock();
               } while(!(block instanceof LeavesBlock));

               bl = this.world.breakBlock(blockPos, true, this) || bl;
            }
         }

         if (this.roarTick > 0) {
            --this.roarTick;
            if (this.roarTick == 10) {
               this.roar();
            }
         }

         if (this.attackTick > 0) {
            --this.attackTick;
         }

         if (this.stunTick > 0) {
            --this.stunTick;
            this.spawnStunnedParticles();
            if (this.stunTick == 0) {
               this.playSound(SoundEvents.ENTITY_RAVAGER_ROAR, 1.0F, 1.0F);
               this.roarTick = 20;
            }
         }

      }
   }

   private void spawnStunnedParticles() {
      if (this.random.nextInt(6) == 0) {
         double d = this.getX() - (double)this.getWidth() * Math.sin((double)(this.bodyYaw * 0.017453292F)) + (this.random.nextDouble() * 0.6D - 0.3D);
         double e = this.getY() + (double)this.getHeight() - 0.3D;
         double f = this.getZ() + (double)this.getWidth() * Math.cos((double)(this.bodyYaw * 0.017453292F)) + (this.random.nextDouble() * 0.6D - 0.3D);
         this.world.addParticle(ParticleTypes.ENTITY_EFFECT, d, e, f, 0.4980392156862745D, 0.5137254901960784D, 0.5725490196078431D);
      }

   }

   protected boolean isImmobile() {
      return super.isImmobile() || this.attackTick > 0 || this.stunTick > 0 || this.roarTick > 0;
   }

   public boolean canSee(Entity entity) {
      return this.stunTick <= 0 && this.roarTick <= 0 ? super.canSee(entity) : false;
   }

   protected void knockback(LivingEntity target) {
      if (this.roarTick == 0) {
         if (this.random.nextDouble() < 0.5D) {
            this.stunTick = 40;
            this.playSound(SoundEvents.ENTITY_RAVAGER_STUNNED, 1.0F, 1.0F);
            this.world.sendEntityStatus(this, (byte)39);
            target.pushAwayFrom(this);
         } else {
            this.knockBack(target);
         }

         target.velocityModified = true;
      }

   }

   private void roar() {
      if (this.isAlive()) {
         List<Entity> list = this.world.getEntitiesByClass(LivingEntity.class, this.getBoundingBox().expand(4.0D), IS_NOT_RAVAGER);

         Entity entity;
         for(Iterator var2 = list.iterator(); var2.hasNext(); this.knockBack(entity)) {
            entity = (Entity)var2.next();
            if (!(entity instanceof IllagerEntity)) {
               entity.damage(DamageSource.mob(this), 6.0F);
            }
         }

         Vec3d vec3d = this.getBoundingBox().getCenter();

         for(int i = 0; i < 40; ++i) {
            double d = this.random.nextGaussian() * 0.2D;
            double e = this.random.nextGaussian() * 0.2D;
            double f = this.random.nextGaussian() * 0.2D;
            this.world.addParticle(ParticleTypes.POOF, vec3d.x, vec3d.y, vec3d.z, d, e, f);
         }
      }

   }

   private void knockBack(Entity entity) {
      double d = entity.getX() - this.getX();
      double e = entity.getZ() - this.getZ();
      double f = Math.max(d * d + e * e, 0.001D);
      entity.addVelocity(d / f * 4.0D, 0.2D, e / f * 4.0D);
   }

   @Environment(EnvType.CLIENT)
   public void handleStatus(byte status) {
      if (status == 4) {
         this.attackTick = 10;
         this.playSound(SoundEvents.ENTITY_RAVAGER_ATTACK, 1.0F, 1.0F);
      } else if (status == 39) {
         this.stunTick = 40;
      }

      super.handleStatus(status);
   }

   @Environment(EnvType.CLIENT)
   public int getAttackTick() {
      return this.attackTick;
   }

   @Environment(EnvType.CLIENT)
   public int getStunTick() {
      return this.stunTick;
   }

   @Environment(EnvType.CLIENT)
   public int getRoarTick() {
      return this.roarTick;
   }

   public boolean tryAttack(Entity target) {
      this.attackTick = 10;
      this.world.sendEntityStatus(this, (byte)4);
      this.playSound(SoundEvents.ENTITY_RAVAGER_ATTACK, 1.0F, 1.0F);
      return super.tryAttack(target);
   }

   @Nullable
   protected SoundEvent getAmbientSound() {
      return SoundEvents.ENTITY_RAVAGER_AMBIENT;
   }

   protected SoundEvent getHurtSound(DamageSource source) {
      return SoundEvents.ENTITY_RAVAGER_HURT;
   }

   protected SoundEvent getDeathSound() {
      return SoundEvents.ENTITY_RAVAGER_DEATH;
   }

   protected void playStepSound(BlockPos pos, BlockState state) {
      this.playSound(SoundEvents.ENTITY_RAVAGER_STEP, 0.15F, 1.0F);
   }

   public boolean canSpawn(WorldView world) {
      return !world.containsFluid(this.getBoundingBox());
   }

   public void addBonusForWave(int wave, boolean unused) {
   }

   public boolean canLead() {
      return false;
   }

   static class PathNodeMaker extends LandPathNodeMaker {
      private PathNodeMaker() {
      }

      protected PathNodeType adjustNodeType(BlockView world, boolean canOpenDoors, boolean canEnterOpenDoors, BlockPos pos, PathNodeType type) {
         return type == PathNodeType.LEAVES ? PathNodeType.OPEN : super.adjustNodeType(world, canOpenDoors, canEnterOpenDoors, pos, type);
      }
   }

   static class Navigation extends MobNavigation {
      public Navigation(MobEntity mobEntity, World world) {
         super(mobEntity, world);
      }

      protected PathNodeNavigator createPathNodeNavigator(int range) {
         this.nodeMaker = new RavagerEntity.PathNodeMaker();
         return new PathNodeNavigator(this.nodeMaker, range);
      }
   }

   class AttackGoal extends MeleeAttackGoal {
      public AttackGoal() {
         super(RavagerEntity.this, 1.0D, true);
      }

      protected double getSquaredMaxAttackDistance(LivingEntity entity) {
         float f = RavagerEntity.this.getWidth() - 0.1F;
         return (double)(f * 2.0F * f * 2.0F + entity.getWidth());
      }
   }
}
