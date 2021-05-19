package net.minecraft.entity.mob;

import java.util.List;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityData;
import net.minecraft.entity.EntityGroup;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.TargetPredicate;
import net.minecraft.entity.ai.goal.FleeEntityGoal;
import net.minecraft.entity.ai.goal.FollowTargetGoal;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.ai.goal.RevengeGoal;
import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.entity.ai.goal.WanderAroundGoal;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.passive.IronGolemEntity;
import net.minecraft.entity.passive.MerchantEntity;
import net.minecraft.entity.passive.SheepEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.raid.RaiderEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.DyeColor;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class EvokerEntity extends SpellcastingIllagerEntity {
   private SheepEntity wololoTarget;

   public EvokerEntity(EntityType<? extends EvokerEntity> entityType, World world) {
      super(entityType, world);
      this.experiencePoints = 10;
   }

   protected void initGoals() {
      super.initGoals();
      this.goalSelector.add(0, new SwimGoal(this));
      this.goalSelector.add(1, new EvokerEntity.LookAtTargetOrWololoTarget());
      this.goalSelector.add(2, new FleeEntityGoal(this, PlayerEntity.class, 8.0F, 0.6D, 1.0D));
      this.goalSelector.add(4, new EvokerEntity.SummonVexGoal());
      this.goalSelector.add(5, new EvokerEntity.ConjureFangsGoal());
      this.goalSelector.add(6, new EvokerEntity.WololoGoal());
      this.goalSelector.add(8, new WanderAroundGoal(this, 0.6D));
      this.goalSelector.add(9, new LookAtEntityGoal(this, PlayerEntity.class, 3.0F, 1.0F));
      this.goalSelector.add(10, new LookAtEntityGoal(this, MobEntity.class, 8.0F));
      this.targetSelector.add(1, (new RevengeGoal(this, new Class[]{RaiderEntity.class})).setGroupRevenge());
      this.targetSelector.add(2, (new FollowTargetGoal(this, PlayerEntity.class, true)).setMaxTimeWithoutVisibility(300));
      this.targetSelector.add(3, (new FollowTargetGoal(this, MerchantEntity.class, false)).setMaxTimeWithoutVisibility(300));
      this.targetSelector.add(3, new FollowTargetGoal(this, IronGolemEntity.class, false));
   }

   public static DefaultAttributeContainer.Builder createEvokerAttributes() {
      return HostileEntity.createHostileAttributes().add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.5D).add(EntityAttributes.GENERIC_FOLLOW_RANGE, 12.0D).add(EntityAttributes.GENERIC_MAX_HEALTH, 24.0D);
   }

   protected void initDataTracker() {
      super.initDataTracker();
   }

   public void readCustomDataFromTag(CompoundTag tag) {
      super.readCustomDataFromTag(tag);
   }

   public SoundEvent getCelebratingSound() {
      return SoundEvents.ENTITY_EVOKER_CELEBRATE;
   }

   public void writeCustomDataToTag(CompoundTag tag) {
      super.writeCustomDataToTag(tag);
   }

   protected void mobTick() {
      super.mobTick();
   }

   public boolean isTeammate(Entity other) {
      if (other == null) {
         return false;
      } else if (other == this) {
         return true;
      } else if (super.isTeammate(other)) {
         return true;
      } else if (other instanceof VexEntity) {
         return this.isTeammate(((VexEntity)other).getOwner());
      } else if (other instanceof LivingEntity && ((LivingEntity)other).getGroup() == EntityGroup.ILLAGER) {
         return this.getScoreboardTeam() == null && other.getScoreboardTeam() == null;
      } else {
         return false;
      }
   }

   protected SoundEvent getAmbientSound() {
      return SoundEvents.ENTITY_EVOKER_AMBIENT;
   }

   protected SoundEvent getDeathSound() {
      return SoundEvents.ENTITY_EVOKER_DEATH;
   }

   protected SoundEvent getHurtSound(DamageSource source) {
      return SoundEvents.ENTITY_EVOKER_HURT;
   }

   private void setWololoTarget(@Nullable SheepEntity sheep) {
      this.wololoTarget = sheep;
   }

   @Nullable
   private SheepEntity getWololoTarget() {
      return this.wololoTarget;
   }

   protected SoundEvent getCastSpellSound() {
      return SoundEvents.ENTITY_EVOKER_CAST_SPELL;
   }

   public void addBonusForWave(int wave, boolean unused) {
   }

   public class WololoGoal extends SpellcastingIllagerEntity.CastSpellGoal {
      private final TargetPredicate convertibleSheepPredicate = (new TargetPredicate()).setBaseMaxDistance(16.0D).includeInvulnerable().setPredicate((livingEntity) -> {
         return ((SheepEntity)livingEntity).getColor() == DyeColor.BLUE;
      });

      public WololoGoal() {
         super();
      }

      public boolean canStart() {
         if (EvokerEntity.this.getTarget() != null) {
            return false;
         } else if (EvokerEntity.this.isSpellcasting()) {
            return false;
         } else if (EvokerEntity.this.age < this.startTime) {
            return false;
         } else if (!EvokerEntity.this.world.getGameRules().getBoolean(GameRules.DO_MOB_GRIEFING)) {
            return false;
         } else {
            List<SheepEntity> list = EvokerEntity.this.world.getTargets(SheepEntity.class, this.convertibleSheepPredicate, EvokerEntity.this, EvokerEntity.this.getBoundingBox().expand(16.0D, 4.0D, 16.0D));
            if (list.isEmpty()) {
               return false;
            } else {
               EvokerEntity.this.setWololoTarget((SheepEntity)list.get(EvokerEntity.this.random.nextInt(list.size())));
               return true;
            }
         }
      }

      public boolean shouldContinue() {
         return EvokerEntity.this.getWololoTarget() != null && this.spellCooldown > 0;
      }

      public void stop() {
         super.stop();
         EvokerEntity.this.setWololoTarget((SheepEntity)null);
      }

      protected void castSpell() {
         SheepEntity sheepEntity = EvokerEntity.this.getWololoTarget();
         if (sheepEntity != null && sheepEntity.isAlive()) {
            sheepEntity.setColor(DyeColor.RED);
         }

      }

      protected int getInitialCooldown() {
         return 40;
      }

      protected int getSpellTicks() {
         return 60;
      }

      protected int startTimeDelay() {
         return 140;
      }

      protected SoundEvent getSoundPrepare() {
         return SoundEvents.ENTITY_EVOKER_PREPARE_WOLOLO;
      }

      protected SpellcastingIllagerEntity.Spell getSpell() {
         return SpellcastingIllagerEntity.Spell.WOLOLO;
      }
   }

   class SummonVexGoal extends SpellcastingIllagerEntity.CastSpellGoal {
      private final TargetPredicate closeVexPredicate;

      private SummonVexGoal() {
         super();
         this.closeVexPredicate = (new TargetPredicate()).setBaseMaxDistance(16.0D).includeHidden().ignoreDistanceScalingFactor().includeInvulnerable().includeTeammates();
      }

      public boolean canStart() {
         if (!super.canStart()) {
            return false;
         } else {
            int i = EvokerEntity.this.world.getTargets(VexEntity.class, this.closeVexPredicate, EvokerEntity.this, EvokerEntity.this.getBoundingBox().expand(16.0D)).size();
            return EvokerEntity.this.random.nextInt(8) + 1 > i;
         }
      }

      protected int getSpellTicks() {
         return 100;
      }

      protected int startTimeDelay() {
         return 340;
      }

      protected void castSpell() {
         ServerWorld serverWorld = (ServerWorld)EvokerEntity.this.world;

         for(int i = 0; i < 3; ++i) {
            BlockPos blockPos = EvokerEntity.this.getBlockPos().add(-2 + EvokerEntity.this.random.nextInt(5), 1, -2 + EvokerEntity.this.random.nextInt(5));
            VexEntity vexEntity = (VexEntity)EntityType.VEX.create(EvokerEntity.this.world);
            vexEntity.refreshPositionAndAngles(blockPos, 0.0F, 0.0F);
            vexEntity.initialize(serverWorld, EvokerEntity.this.world.getLocalDifficulty(blockPos), SpawnReason.MOB_SUMMONED, (EntityData)null, (CompoundTag)null);
            vexEntity.setOwner(EvokerEntity.this);
            vexEntity.setBounds(blockPos);
            vexEntity.setLifeTicks(20 * (30 + EvokerEntity.this.random.nextInt(90)));
            serverWorld.spawnEntityAndPassengers(vexEntity);
         }

      }

      protected SoundEvent getSoundPrepare() {
         return SoundEvents.ENTITY_EVOKER_PREPARE_SUMMON;
      }

      protected SpellcastingIllagerEntity.Spell getSpell() {
         return SpellcastingIllagerEntity.Spell.SUMMON_VEX;
      }
   }

   class ConjureFangsGoal extends SpellcastingIllagerEntity.CastSpellGoal {
      private ConjureFangsGoal() {
         super();
      }

      protected int getSpellTicks() {
         return 40;
      }

      protected int startTimeDelay() {
         return 100;
      }

      protected void castSpell() {
         LivingEntity livingEntity = EvokerEntity.this.getTarget();
         double d = Math.min(livingEntity.getY(), EvokerEntity.this.getY());
         double e = Math.max(livingEntity.getY(), EvokerEntity.this.getY()) + 1.0D;
         float f = (float)MathHelper.atan2(livingEntity.getZ() - EvokerEntity.this.getZ(), livingEntity.getX() - EvokerEntity.this.getX());
         int j;
         if (EvokerEntity.this.squaredDistanceTo(livingEntity) < 9.0D) {
            float h;
            for(j = 0; j < 5; ++j) {
               h = f + (float)j * 3.1415927F * 0.4F;
               this.conjureFangs(EvokerEntity.this.getX() + (double)MathHelper.cos(h) * 1.5D, EvokerEntity.this.getZ() + (double)MathHelper.sin(h) * 1.5D, d, e, h, 0);
            }

            for(j = 0; j < 8; ++j) {
               h = f + (float)j * 3.1415927F * 2.0F / 8.0F + 1.2566371F;
               this.conjureFangs(EvokerEntity.this.getX() + (double)MathHelper.cos(h) * 2.5D, EvokerEntity.this.getZ() + (double)MathHelper.sin(h) * 2.5D, d, e, h, 3);
            }
         } else {
            for(j = 0; j < 16; ++j) {
               double l = 1.25D * (double)(j + 1);
               int m = 1 * j;
               this.conjureFangs(EvokerEntity.this.getX() + (double)MathHelper.cos(f) * l, EvokerEntity.this.getZ() + (double)MathHelper.sin(f) * l, d, e, f, m);
            }
         }

      }

      private void conjureFangs(double x, double z, double maxY, double y, float yaw, int warmup) {
         BlockPos blockPos = new BlockPos(x, y, z);
         boolean bl = false;
         double d = 0.0D;

         do {
            BlockPos blockPos2 = blockPos.down();
            BlockState blockState = EvokerEntity.this.world.getBlockState(blockPos2);
            if (blockState.isSideSolidFullSquare(EvokerEntity.this.world, blockPos2, Direction.UP)) {
               if (!EvokerEntity.this.world.isAir(blockPos)) {
                  BlockState blockState2 = EvokerEntity.this.world.getBlockState(blockPos);
                  VoxelShape voxelShape = blockState2.getCollisionShape(EvokerEntity.this.world, blockPos);
                  if (!voxelShape.isEmpty()) {
                     d = voxelShape.getMax(Direction.Axis.Y);
                  }
               }

               bl = true;
               break;
            }

            blockPos = blockPos.down();
         } while(blockPos.getY() >= MathHelper.floor(maxY) - 1);

         if (bl) {
            EvokerEntity.this.world.spawnEntity(new EvokerFangsEntity(EvokerEntity.this.world, x, (double)blockPos.getY() + d, z, yaw, warmup, EvokerEntity.this));
         }

      }

      protected SoundEvent getSoundPrepare() {
         return SoundEvents.ENTITY_EVOKER_PREPARE_ATTACK;
      }

      protected SpellcastingIllagerEntity.Spell getSpell() {
         return SpellcastingIllagerEntity.Spell.FANGS;
      }
   }

   class LookAtTargetOrWololoTarget extends SpellcastingIllagerEntity.LookAtTargetGoal {
      private LookAtTargetOrWololoTarget() {
         super();
      }

      public void tick() {
         if (EvokerEntity.this.getTarget() != null) {
            EvokerEntity.this.getLookControl().lookAt(EvokerEntity.this.getTarget(), (float)EvokerEntity.this.getBodyYawSpeed(), (float)EvokerEntity.this.getLookPitchSpeed());
         } else if (EvokerEntity.this.getWololoTarget() != null) {
            EvokerEntity.this.getLookControl().lookAt(EvokerEntity.this.getWololoTarget(), (float)EvokerEntity.this.getBodyYawSpeed(), (float)EvokerEntity.this.getLookPitchSpeed());
         }

      }
   }
}
