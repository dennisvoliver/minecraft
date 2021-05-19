package net.minecraft.entity.mob;

import java.util.EnumSet;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public abstract class SpellcastingIllagerEntity extends IllagerEntity {
   private static final TrackedData<Byte> SPELL;
   protected int spellTicks;
   private SpellcastingIllagerEntity.Spell spell;

   protected SpellcastingIllagerEntity(EntityType<? extends SpellcastingIllagerEntity> entityType, World world) {
      super(entityType, world);
      this.spell = SpellcastingIllagerEntity.Spell.NONE;
   }

   protected void initDataTracker() {
      super.initDataTracker();
      this.dataTracker.startTracking(SPELL, (byte)0);
   }

   public void readCustomDataFromTag(CompoundTag tag) {
      super.readCustomDataFromTag(tag);
      this.spellTicks = tag.getInt("SpellTicks");
   }

   public void writeCustomDataToTag(CompoundTag tag) {
      super.writeCustomDataToTag(tag);
      tag.putInt("SpellTicks", this.spellTicks);
   }

   @Environment(EnvType.CLIENT)
   public IllagerEntity.State getState() {
      if (this.isSpellcasting()) {
         return IllagerEntity.State.SPELLCASTING;
      } else {
         return this.isCelebrating() ? IllagerEntity.State.CELEBRATING : IllagerEntity.State.CROSSED;
      }
   }

   public boolean isSpellcasting() {
      if (this.world.isClient) {
         return (Byte)this.dataTracker.get(SPELL) > 0;
      } else {
         return this.spellTicks > 0;
      }
   }

   public void setSpell(SpellcastingIllagerEntity.Spell spell) {
      this.spell = spell;
      this.dataTracker.set(SPELL, (byte)spell.id);
   }

   protected SpellcastingIllagerEntity.Spell getSpell() {
      return !this.world.isClient ? this.spell : SpellcastingIllagerEntity.Spell.byId((Byte)this.dataTracker.get(SPELL));
   }

   protected void mobTick() {
      super.mobTick();
      if (this.spellTicks > 0) {
         --this.spellTicks;
      }

   }

   public void tick() {
      super.tick();
      if (this.world.isClient && this.isSpellcasting()) {
         SpellcastingIllagerEntity.Spell spell = this.getSpell();
         double d = spell.particleVelocity[0];
         double e = spell.particleVelocity[1];
         double f = spell.particleVelocity[2];
         float g = this.bodyYaw * 0.017453292F + MathHelper.cos((float)this.age * 0.6662F) * 0.25F;
         float h = MathHelper.cos(g);
         float i = MathHelper.sin(g);
         this.world.addParticle(ParticleTypes.ENTITY_EFFECT, this.getX() + (double)h * 0.6D, this.getY() + 1.8D, this.getZ() + (double)i * 0.6D, d, e, f);
         this.world.addParticle(ParticleTypes.ENTITY_EFFECT, this.getX() - (double)h * 0.6D, this.getY() + 1.8D, this.getZ() - (double)i * 0.6D, d, e, f);
      }

   }

   protected int getSpellTicks() {
      return this.spellTicks;
   }

   protected abstract SoundEvent getCastSpellSound();

   static {
      SPELL = DataTracker.registerData(SpellcastingIllagerEntity.class, TrackedDataHandlerRegistry.BYTE);
   }

   public static enum Spell {
      NONE(0, 0.0D, 0.0D, 0.0D),
      SUMMON_VEX(1, 0.7D, 0.7D, 0.8D),
      FANGS(2, 0.4D, 0.3D, 0.35D),
      WOLOLO(3, 0.7D, 0.5D, 0.2D),
      DISAPPEAR(4, 0.3D, 0.3D, 0.8D),
      BLINDNESS(5, 0.1D, 0.1D, 0.2D);

      private final int id;
      private final double[] particleVelocity;

      private Spell(int id, double particleVelocityX, double particleVelocityY, double particleVelocityZ) {
         this.id = id;
         this.particleVelocity = new double[]{particleVelocityX, particleVelocityY, particleVelocityZ};
      }

      public static SpellcastingIllagerEntity.Spell byId(int id) {
         SpellcastingIllagerEntity.Spell[] var1 = values();
         int var2 = var1.length;

         for(int var3 = 0; var3 < var2; ++var3) {
            SpellcastingIllagerEntity.Spell spell = var1[var3];
            if (id == spell.id) {
               return spell;
            }
         }

         return NONE;
      }
   }

   public abstract class CastSpellGoal extends Goal {
      protected int spellCooldown;
      protected int startTime;

      protected CastSpellGoal() {
      }

      public boolean canStart() {
         LivingEntity livingEntity = SpellcastingIllagerEntity.this.getTarget();
         if (livingEntity != null && livingEntity.isAlive()) {
            if (SpellcastingIllagerEntity.this.isSpellcasting()) {
               return false;
            } else {
               return SpellcastingIllagerEntity.this.age >= this.startTime;
            }
         } else {
            return false;
         }
      }

      public boolean shouldContinue() {
         LivingEntity livingEntity = SpellcastingIllagerEntity.this.getTarget();
         return livingEntity != null && livingEntity.isAlive() && this.spellCooldown > 0;
      }

      public void start() {
         this.spellCooldown = this.getInitialCooldown();
         SpellcastingIllagerEntity.this.spellTicks = this.getSpellTicks();
         this.startTime = SpellcastingIllagerEntity.this.age + this.startTimeDelay();
         SoundEvent soundEvent = this.getSoundPrepare();
         if (soundEvent != null) {
            SpellcastingIllagerEntity.this.playSound(soundEvent, 1.0F, 1.0F);
         }

         SpellcastingIllagerEntity.this.setSpell(this.getSpell());
      }

      public void tick() {
         --this.spellCooldown;
         if (this.spellCooldown == 0) {
            this.castSpell();
            SpellcastingIllagerEntity.this.playSound(SpellcastingIllagerEntity.this.getCastSpellSound(), 1.0F, 1.0F);
         }

      }

      protected abstract void castSpell();

      protected int getInitialCooldown() {
         return 20;
      }

      protected abstract int getSpellTicks();

      protected abstract int startTimeDelay();

      @Nullable
      protected abstract SoundEvent getSoundPrepare();

      protected abstract SpellcastingIllagerEntity.Spell getSpell();
   }

   public class LookAtTargetGoal extends Goal {
      public LookAtTargetGoal() {
         this.setControls(EnumSet.of(Goal.Control.MOVE, Goal.Control.LOOK));
      }

      public boolean canStart() {
         return SpellcastingIllagerEntity.this.getSpellTicks() > 0;
      }

      public void start() {
         super.start();
         SpellcastingIllagerEntity.this.navigation.stop();
      }

      public void stop() {
         super.stop();
         SpellcastingIllagerEntity.this.setSpell(SpellcastingIllagerEntity.Spell.NONE);
      }

      public void tick() {
         if (SpellcastingIllagerEntity.this.getTarget() != null) {
            SpellcastingIllagerEntity.this.getLookControl().lookAt(SpellcastingIllagerEntity.this.getTarget(), (float)SpellcastingIllagerEntity.this.getBodyYawSpeed(), (float)SpellcastingIllagerEntity.this.getLookPitchSpeed());
         }

      }
   }
}
