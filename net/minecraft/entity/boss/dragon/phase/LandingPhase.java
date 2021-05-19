package net.minecraft.entity.boss.dragon.phase;

import java.util.Random;
import net.minecraft.entity.Entity;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Heightmap;
import net.minecraft.world.gen.feature.EndPortalFeature;
import org.jetbrains.annotations.Nullable;

public class LandingPhase extends AbstractPhase {
   private Vec3d target;

   public LandingPhase(EnderDragonEntity enderDragonEntity) {
      super(enderDragonEntity);
   }

   public void clientTick() {
      Vec3d vec3d = this.dragon.method_6834(1.0F).normalize();
      vec3d.rotateY(-0.7853982F);
      double d = this.dragon.partHead.getX();
      double e = this.dragon.partHead.getBodyY(0.5D);
      double f = this.dragon.partHead.getZ();

      for(int i = 0; i < 8; ++i) {
         Random random = this.dragon.getRandom();
         double g = d + random.nextGaussian() / 2.0D;
         double h = e + random.nextGaussian() / 2.0D;
         double j = f + random.nextGaussian() / 2.0D;
         Vec3d vec3d2 = this.dragon.getVelocity();
         this.dragon.world.addParticle(ParticleTypes.DRAGON_BREATH, g, h, j, -vec3d.x * 0.07999999821186066D + vec3d2.x, -vec3d.y * 0.30000001192092896D + vec3d2.y, -vec3d.z * 0.07999999821186066D + vec3d2.z);
         vec3d.rotateY(0.19634955F);
      }

   }

   public void serverTick() {
      if (this.target == null) {
         this.target = Vec3d.ofBottomCenter(this.dragon.world.getTopPosition(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, EndPortalFeature.ORIGIN));
      }

      if (this.target.squaredDistanceTo(this.dragon.getX(), this.dragon.getY(), this.dragon.getZ()) < 1.0D) {
         ((SittingFlamingPhase)this.dragon.getPhaseManager().create(PhaseType.SITTING_FLAMING)).method_6857();
         this.dragon.getPhaseManager().setPhase(PhaseType.SITTING_SCANNING);
      }

   }

   public float getMaxYAcceleration() {
      return 1.5F;
   }

   public float method_6847() {
      float f = MathHelper.sqrt(Entity.squaredHorizontalLength(this.dragon.getVelocity())) + 1.0F;
      float g = Math.min(f, 40.0F);
      return g / f;
   }

   public void beginPhase() {
      this.target = null;
   }

   @Nullable
   public Vec3d getTarget() {
      return this.target;
   }

   public PhaseType<LandingPhase> getType() {
      return PhaseType.LANDING;
   }
}
