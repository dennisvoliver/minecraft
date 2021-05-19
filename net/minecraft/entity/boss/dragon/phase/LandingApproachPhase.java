package net.minecraft.entity.boss.dragon.phase;

import net.minecraft.entity.ai.TargetPredicate;
import net.minecraft.entity.ai.pathing.Path;
import net.minecraft.entity.ai.pathing.PathNode;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.Heightmap;
import net.minecraft.world.gen.feature.EndPortalFeature;
import org.jetbrains.annotations.Nullable;

public class LandingApproachPhase extends AbstractPhase {
   private static final TargetPredicate PLAYERS_IN_RANGE_PREDICATE = (new TargetPredicate()).setBaseMaxDistance(128.0D);
   private Path field_7047;
   private Vec3d target;

   public LandingApproachPhase(EnderDragonEntity enderDragonEntity) {
      super(enderDragonEntity);
   }

   public PhaseType<LandingApproachPhase> getType() {
      return PhaseType.LANDING_APPROACH;
   }

   public void beginPhase() {
      this.field_7047 = null;
      this.target = null;
   }

   public void serverTick() {
      double d = this.target == null ? 0.0D : this.target.squaredDistanceTo(this.dragon.getX(), this.dragon.getY(), this.dragon.getZ());
      if (d < 100.0D || d > 22500.0D || this.dragon.horizontalCollision || this.dragon.verticalCollision) {
         this.method_6844();
      }

   }

   @Nullable
   public Vec3d getTarget() {
      return this.target;
   }

   private void method_6844() {
      if (this.field_7047 == null || this.field_7047.isFinished()) {
         int i = this.dragon.getNearestPathNodeIndex();
         BlockPos blockPos = this.dragon.world.getTopPosition(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, EndPortalFeature.ORIGIN);
         PlayerEntity playerEntity = this.dragon.world.getClosestPlayer(PLAYERS_IN_RANGE_PREDICATE, (double)blockPos.getX(), (double)blockPos.getY(), (double)blockPos.getZ());
         int k;
         if (playerEntity != null) {
            Vec3d vec3d = (new Vec3d(playerEntity.getX(), 0.0D, playerEntity.getZ())).normalize();
            k = this.dragon.getNearestPathNodeIndex(-vec3d.x * 40.0D, 105.0D, -vec3d.z * 40.0D);
         } else {
            k = this.dragon.getNearestPathNodeIndex(40.0D, (double)blockPos.getY(), 0.0D);
         }

         PathNode pathNode = new PathNode(blockPos.getX(), blockPos.getY(), blockPos.getZ());
         this.field_7047 = this.dragon.findPath(i, k, pathNode);
         if (this.field_7047 != null) {
            this.field_7047.next();
         }
      }

      this.method_6845();
      if (this.field_7047 != null && this.field_7047.isFinished()) {
         this.dragon.getPhaseManager().setPhase(PhaseType.LANDING);
      }

   }

   private void method_6845() {
      if (this.field_7047 != null && !this.field_7047.isFinished()) {
         Vec3i vec3i = this.field_7047.method_31032();
         this.field_7047.next();
         double d = (double)vec3i.getX();
         double e = (double)vec3i.getZ();

         double f;
         do {
            f = (double)((float)vec3i.getY() + this.dragon.getRandom().nextFloat() * 20.0F);
         } while(f < (double)vec3i.getY());

         this.target = new Vec3d(d, f, e);
      }

   }
}
