package net.minecraft.entity.boss.dragon.phase;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.pathing.Path;
import net.minecraft.entity.ai.pathing.PathNode;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.DragonFireballEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

public class StrafePlayerPhase extends AbstractPhase {
   private static final Logger LOGGER = LogManager.getLogger();
   private int field_7060;
   private Path field_7059;
   private Vec3d target;
   private LivingEntity field_7062;
   private boolean field_7058;

   public StrafePlayerPhase(EnderDragonEntity enderDragonEntity) {
      super(enderDragonEntity);
   }

   public void serverTick() {
      if (this.field_7062 == null) {
         LOGGER.warn("Skipping player strafe phase because no player was found");
         this.dragon.getPhaseManager().setPhase(PhaseType.HOLDING_PATTERN);
      } else {
         double j;
         double k;
         double n;
         if (this.field_7059 != null && this.field_7059.isFinished()) {
            j = this.field_7062.getX();
            k = this.field_7062.getZ();
            double f = j - this.dragon.getX();
            double g = k - this.dragon.getZ();
            n = (double)MathHelper.sqrt(f * f + g * g);
            double i = Math.min(0.4000000059604645D + n / 80.0D - 1.0D, 10.0D);
            this.target = new Vec3d(j, this.field_7062.getY() + i, k);
         }

         j = this.target == null ? 0.0D : this.target.squaredDistanceTo(this.dragon.getX(), this.dragon.getY(), this.dragon.getZ());
         if (j < 100.0D || j > 22500.0D) {
            this.method_6860();
         }

         k = 64.0D;
         if (this.field_7062.squaredDistanceTo(this.dragon) < 4096.0D) {
            if (this.dragon.canSee(this.field_7062)) {
               ++this.field_7060;
               Vec3d vec3d = (new Vec3d(this.field_7062.getX() - this.dragon.getX(), 0.0D, this.field_7062.getZ() - this.dragon.getZ())).normalize();
               Vec3d vec3d2 = (new Vec3d((double)MathHelper.sin(this.dragon.yaw * 0.017453292F), 0.0D, (double)(-MathHelper.cos(this.dragon.yaw * 0.017453292F)))).normalize();
               float l = (float)vec3d2.dotProduct(vec3d);
               float m = (float)(Math.acos((double)l) * 57.2957763671875D);
               m += 0.5F;
               if (this.field_7060 >= 5 && m >= 0.0F && m < 10.0F) {
                  n = 1.0D;
                  Vec3d vec3d3 = this.dragon.getRotationVec(1.0F);
                  double o = this.dragon.partHead.getX() - vec3d3.x * 1.0D;
                  double p = this.dragon.partHead.getBodyY(0.5D) + 0.5D;
                  double q = this.dragon.partHead.getZ() - vec3d3.z * 1.0D;
                  double r = this.field_7062.getX() - o;
                  double s = this.field_7062.getBodyY(0.5D) - p;
                  double t = this.field_7062.getZ() - q;
                  if (!this.dragon.isSilent()) {
                     this.dragon.world.syncWorldEvent((PlayerEntity)null, 1017, this.dragon.getBlockPos(), 0);
                  }

                  DragonFireballEntity dragonFireballEntity = new DragonFireballEntity(this.dragon.world, this.dragon, r, s, t);
                  dragonFireballEntity.refreshPositionAndAngles(o, p, q, 0.0F, 0.0F);
                  this.dragon.world.spawnEntity(dragonFireballEntity);
                  this.field_7060 = 0;
                  if (this.field_7059 != null) {
                     while(!this.field_7059.isFinished()) {
                        this.field_7059.next();
                     }
                  }

                  this.dragon.getPhaseManager().setPhase(PhaseType.HOLDING_PATTERN);
               }
            } else if (this.field_7060 > 0) {
               --this.field_7060;
            }
         } else if (this.field_7060 > 0) {
            --this.field_7060;
         }

      }
   }

   private void method_6860() {
      if (this.field_7059 == null || this.field_7059.isFinished()) {
         int i = this.dragon.getNearestPathNodeIndex();
         int j = i;
         if (this.dragon.getRandom().nextInt(8) == 0) {
            this.field_7058 = !this.field_7058;
            j = i + 6;
         }

         if (this.field_7058) {
            ++j;
         } else {
            --j;
         }

         if (this.dragon.getFight() != null && this.dragon.getFight().getAliveEndCrystals() > 0) {
            j %= 12;
            if (j < 0) {
               j += 12;
            }
         } else {
            j -= 12;
            j &= 7;
            j += 12;
         }

         this.field_7059 = this.dragon.findPath(i, j, (PathNode)null);
         if (this.field_7059 != null) {
            this.field_7059.next();
         }
      }

      this.method_6861();
   }

   private void method_6861() {
      if (this.field_7059 != null && !this.field_7059.isFinished()) {
         Vec3i vec3i = this.field_7059.method_31032();
         this.field_7059.next();
         double d = (double)vec3i.getX();
         double e = (double)vec3i.getZ();

         double f;
         do {
            f = (double)((float)vec3i.getY() + this.dragon.getRandom().nextFloat() * 20.0F);
         } while(f < (double)vec3i.getY());

         this.target = new Vec3d(d, f, e);
      }

   }

   public void beginPhase() {
      this.field_7060 = 0;
      this.target = null;
      this.field_7059 = null;
      this.field_7062 = null;
   }

   public void method_6862(LivingEntity livingEntity) {
      this.field_7062 = livingEntity;
      int i = this.dragon.getNearestPathNodeIndex();
      int j = this.dragon.getNearestPathNodeIndex(this.field_7062.getX(), this.field_7062.getY(), this.field_7062.getZ());
      int k = MathHelper.floor(this.field_7062.getX());
      int l = MathHelper.floor(this.field_7062.getZ());
      double d = (double)k - this.dragon.getX();
      double e = (double)l - this.dragon.getZ();
      double f = (double)MathHelper.sqrt(d * d + e * e);
      double g = Math.min(0.4000000059604645D + f / 80.0D - 1.0D, 10.0D);
      int m = MathHelper.floor(this.field_7062.getY() + g);
      PathNode pathNode = new PathNode(k, m, l);
      this.field_7059 = this.dragon.findPath(i, j, pathNode);
      if (this.field_7059 != null) {
         this.field_7059.next();
         this.method_6861();
      }

   }

   @Nullable
   public Vec3d getTarget() {
      return this.target;
   }

   public PhaseType<StrafePlayerPhase> getType() {
      return PhaseType.STRAFE_PLAYER;
   }
}
