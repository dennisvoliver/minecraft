package net.minecraft.entity.boss.dragon.phase;

import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Heightmap;
import net.minecraft.world.gen.feature.EndPortalFeature;
import org.jetbrains.annotations.Nullable;

public class DyingPhase extends AbstractPhase {
   private Vec3d target;
   private int ticks;

   public DyingPhase(EnderDragonEntity enderDragonEntity) {
      super(enderDragonEntity);
   }

   public void clientTick() {
      if (this.ticks++ % 10 == 0) {
         float f = (this.dragon.getRandom().nextFloat() - 0.5F) * 8.0F;
         float g = (this.dragon.getRandom().nextFloat() - 0.5F) * 4.0F;
         float h = (this.dragon.getRandom().nextFloat() - 0.5F) * 8.0F;
         this.dragon.world.addParticle(ParticleTypes.EXPLOSION_EMITTER, this.dragon.getX() + (double)f, this.dragon.getY() + 2.0D + (double)g, this.dragon.getZ() + (double)h, 0.0D, 0.0D, 0.0D);
      }

   }

   public void serverTick() {
      ++this.ticks;
      if (this.target == null) {
         BlockPos blockPos = this.dragon.world.getTopPosition(Heightmap.Type.MOTION_BLOCKING, EndPortalFeature.ORIGIN);
         this.target = Vec3d.ofBottomCenter(blockPos);
      }

      double d = this.target.squaredDistanceTo(this.dragon.getX(), this.dragon.getY(), this.dragon.getZ());
      if (!(d < 100.0D) && !(d > 22500.0D) && !this.dragon.horizontalCollision && !this.dragon.verticalCollision) {
         this.dragon.setHealth(1.0F);
      } else {
         this.dragon.setHealth(0.0F);
      }

   }

   public void beginPhase() {
      this.target = null;
      this.ticks = 0;
   }

   public float getMaxYAcceleration() {
      return 3.0F;
   }

   @Nullable
   public Vec3d getTarget() {
      return this.target;
   }

   public PhaseType<DyingPhase> getType() {
      return PhaseType.DYING;
   }
}
