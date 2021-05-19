package net.minecraft.entity.boss.dragon.phase;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.TargetPredicate;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class SittingScanningPhase extends AbstractSittingPhase {
   private static final TargetPredicate PLAYER_WITHIN_RANGE_PREDICATE = (new TargetPredicate()).setBaseMaxDistance(150.0D);
   private final TargetPredicate CLOSE_PLAYER_PREDICATE;
   private int ticks;

   public SittingScanningPhase(EnderDragonEntity enderDragonEntity) {
      super(enderDragonEntity);
      this.CLOSE_PLAYER_PREDICATE = (new TargetPredicate()).setBaseMaxDistance(20.0D).setPredicate((livingEntity) -> {
         return Math.abs(livingEntity.getY() - enderDragonEntity.getY()) <= 10.0D;
      });
   }

   public void serverTick() {
      ++this.ticks;
      LivingEntity livingEntity = this.dragon.world.getClosestPlayer(this.CLOSE_PLAYER_PREDICATE, this.dragon, this.dragon.getX(), this.dragon.getY(), this.dragon.getZ());
      if (livingEntity != null) {
         if (this.ticks > 25) {
            this.dragon.getPhaseManager().setPhase(PhaseType.SITTING_ATTACKING);
         } else {
            Vec3d vec3d = (new Vec3d(livingEntity.getX() - this.dragon.getX(), 0.0D, livingEntity.getZ() - this.dragon.getZ())).normalize();
            Vec3d vec3d2 = (new Vec3d((double)MathHelper.sin(this.dragon.yaw * 0.017453292F), 0.0D, (double)(-MathHelper.cos(this.dragon.yaw * 0.017453292F)))).normalize();
            float f = (float)vec3d2.dotProduct(vec3d);
            float g = (float)(Math.acos((double)f) * 57.2957763671875D) + 0.5F;
            if (g < 0.0F || g > 10.0F) {
               double d = livingEntity.getX() - this.dragon.partHead.getX();
               double e = livingEntity.getZ() - this.dragon.partHead.getZ();
               double h = MathHelper.clamp(MathHelper.wrapDegrees(180.0D - MathHelper.atan2(d, e) * 57.2957763671875D - (double)this.dragon.yaw), -100.0D, 100.0D);
               EnderDragonEntity var10000 = this.dragon;
               var10000.field_20865 *= 0.8F;
               float i = MathHelper.sqrt(d * d + e * e) + 1.0F;
               float j = i;
               if (i > 40.0F) {
                  i = 40.0F;
               }

               var10000 = this.dragon;
               var10000.field_20865 = (float)((double)var10000.field_20865 + h * (double)(0.7F / i / j));
               var10000 = this.dragon;
               var10000.yaw += this.dragon.field_20865;
            }
         }
      } else if (this.ticks >= 100) {
         livingEntity = this.dragon.world.getClosestPlayer(PLAYER_WITHIN_RANGE_PREDICATE, this.dragon, this.dragon.getX(), this.dragon.getY(), this.dragon.getZ());
         this.dragon.getPhaseManager().setPhase(PhaseType.TAKEOFF);
         if (livingEntity != null) {
            this.dragon.getPhaseManager().setPhase(PhaseType.CHARGING_PLAYER);
            ((ChargingPlayerPhase)this.dragon.getPhaseManager().create(PhaseType.CHARGING_PLAYER)).setTarget(new Vec3d(livingEntity.getX(), livingEntity.getY(), livingEntity.getZ()));
         }
      }

   }

   public void beginPhase() {
      this.ticks = 0;
   }

   public PhaseType<SittingScanningPhase> getType() {
      return PhaseType.SITTING_SCANNING;
   }
}
