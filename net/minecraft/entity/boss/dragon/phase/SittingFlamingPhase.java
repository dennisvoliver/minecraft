package net.minecraft.entity.boss.dragon.phase;

import net.minecraft.entity.AreaEffectCloudEntity;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class SittingFlamingPhase extends AbstractSittingPhase {
   private int ticks;
   private int field_7052;
   private AreaEffectCloudEntity field_7051;

   public SittingFlamingPhase(EnderDragonEntity enderDragonEntity) {
      super(enderDragonEntity);
   }

   public void clientTick() {
      ++this.ticks;
      if (this.ticks % 2 == 0 && this.ticks < 10) {
         Vec3d vec3d = this.dragon.method_6834(1.0F).normalize();
         vec3d.rotateY(-0.7853982F);
         double d = this.dragon.partHead.getX();
         double e = this.dragon.partHead.getBodyY(0.5D);
         double f = this.dragon.partHead.getZ();

         for(int i = 0; i < 8; ++i) {
            double g = d + this.dragon.getRandom().nextGaussian() / 2.0D;
            double h = e + this.dragon.getRandom().nextGaussian() / 2.0D;
            double j = f + this.dragon.getRandom().nextGaussian() / 2.0D;

            for(int k = 0; k < 6; ++k) {
               this.dragon.world.addParticle(ParticleTypes.DRAGON_BREATH, g, h, j, -vec3d.x * 0.07999999821186066D * (double)k, -vec3d.y * 0.6000000238418579D, -vec3d.z * 0.07999999821186066D * (double)k);
            }

            vec3d.rotateY(0.19634955F);
         }
      }

   }

   public void serverTick() {
      ++this.ticks;
      if (this.ticks >= 200) {
         if (this.field_7052 >= 4) {
            this.dragon.getPhaseManager().setPhase(PhaseType.TAKEOFF);
         } else {
            this.dragon.getPhaseManager().setPhase(PhaseType.SITTING_SCANNING);
         }
      } else if (this.ticks == 10) {
         Vec3d vec3d = (new Vec3d(this.dragon.partHead.getX() - this.dragon.getX(), 0.0D, this.dragon.partHead.getZ() - this.dragon.getZ())).normalize();
         float f = 5.0F;
         double d = this.dragon.partHead.getX() + vec3d.x * 5.0D / 2.0D;
         double e = this.dragon.partHead.getZ() + vec3d.z * 5.0D / 2.0D;
         double g = this.dragon.partHead.getBodyY(0.5D);
         double h = g;
         BlockPos.Mutable mutable = new BlockPos.Mutable(d, g, e);

         while(this.dragon.world.isAir(mutable)) {
            --h;
            if (h < 0.0D) {
               h = g;
               break;
            }

            mutable.set(d, h, e);
         }

         h = (double)(MathHelper.floor(h) + 1);
         this.field_7051 = new AreaEffectCloudEntity(this.dragon.world, d, h, e);
         this.field_7051.setOwner(this.dragon);
         this.field_7051.setRadius(5.0F);
         this.field_7051.setDuration(200);
         this.field_7051.setParticleType(ParticleTypes.DRAGON_BREATH);
         this.field_7051.addEffect(new StatusEffectInstance(StatusEffects.INSTANT_DAMAGE));
         this.dragon.world.spawnEntity(this.field_7051);
      }

   }

   public void beginPhase() {
      this.ticks = 0;
      ++this.field_7052;
   }

   public void endPhase() {
      if (this.field_7051 != null) {
         this.field_7051.remove();
         this.field_7051 = null;
      }

   }

   public PhaseType<SittingFlamingPhase> getType() {
      return PhaseType.SITTING_FLAMING;
   }

   public void method_6857() {
      this.field_7052 = 0;
   }
}
