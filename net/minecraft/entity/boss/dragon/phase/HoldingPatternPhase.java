package net.minecraft.entity.boss.dragon.phase;

import net.minecraft.entity.ai.TargetPredicate;
import net.minecraft.entity.ai.pathing.Path;
import net.minecraft.entity.ai.pathing.PathNode;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.Heightmap;
import net.minecraft.world.gen.feature.EndPortalFeature;
import org.jetbrains.annotations.Nullable;

public class HoldingPatternPhase extends AbstractPhase {
   private static final TargetPredicate PLAYERS_IN_RANGE_PREDICATE = (new TargetPredicate()).setBaseMaxDistance(64.0D);
   private Path field_7043;
   private Vec3d target;
   private boolean field_7044;

   public HoldingPatternPhase(EnderDragonEntity enderDragonEntity) {
      super(enderDragonEntity);
   }

   public PhaseType<HoldingPatternPhase> getType() {
      return PhaseType.HOLDING_PATTERN;
   }

   public void serverTick() {
      double d = this.target == null ? 0.0D : this.target.squaredDistanceTo(this.dragon.getX(), this.dragon.getY(), this.dragon.getZ());
      if (d < 100.0D || d > 22500.0D || this.dragon.horizontalCollision || this.dragon.verticalCollision) {
         this.method_6841();
      }

   }

   public void beginPhase() {
      this.field_7043 = null;
      this.target = null;
   }

   @Nullable
   public Vec3d getTarget() {
      return this.target;
   }

   private void method_6841() {
      int k;
      if (this.field_7043 != null && this.field_7043.isFinished()) {
         BlockPos blockPos = this.dragon.world.getTopPosition(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, new BlockPos(EndPortalFeature.ORIGIN));
         k = this.dragon.getFight() == null ? 0 : this.dragon.getFight().getAliveEndCrystals();
         if (this.dragon.getRandom().nextInt(k + 3) == 0) {
            this.dragon.getPhaseManager().setPhase(PhaseType.LANDING_APPROACH);
            return;
         }

         double d = 64.0D;
         PlayerEntity playerEntity = this.dragon.world.getClosestPlayer(PLAYERS_IN_RANGE_PREDICATE, (double)blockPos.getX(), (double)blockPos.getY(), (double)blockPos.getZ());
         if (playerEntity != null) {
            d = blockPos.getSquaredDistance(playerEntity.getPos(), true) / 512.0D;
         }

         if (playerEntity != null && !playerEntity.abilities.invulnerable && (this.dragon.getRandom().nextInt(MathHelper.abs((int)d) + 2) == 0 || this.dragon.getRandom().nextInt(k + 2) == 0)) {
            this.method_6843(playerEntity);
            return;
         }
      }

      if (this.field_7043 == null || this.field_7043.isFinished()) {
         int j = this.dragon.getNearestPathNodeIndex();
         k = j;
         if (this.dragon.getRandom().nextInt(8) == 0) {
            this.field_7044 = !this.field_7044;
            k = j + 6;
         }

         if (this.field_7044) {
            ++k;
         } else {
            --k;
         }

         if (this.dragon.getFight() != null && this.dragon.getFight().getAliveEndCrystals() >= 0) {
            k %= 12;
            if (k < 0) {
               k += 12;
            }
         } else {
            k -= 12;
            k &= 7;
            k += 12;
         }

         this.field_7043 = this.dragon.findPath(j, k, (PathNode)null);
         if (this.field_7043 != null) {
            this.field_7043.next();
         }
      }

      this.method_6842();
   }

   private void method_6843(PlayerEntity playerEntity) {
      this.dragon.getPhaseManager().setPhase(PhaseType.STRAFE_PLAYER);
      ((StrafePlayerPhase)this.dragon.getPhaseManager().create(PhaseType.STRAFE_PLAYER)).method_6862(playerEntity);
   }

   private void method_6842() {
      if (this.field_7043 != null && !this.field_7043.isFinished()) {
         Vec3i vec3i = this.field_7043.method_31032();
         this.field_7043.next();
         double d = (double)vec3i.getX();
         double e = (double)vec3i.getZ();

         double f;
         do {
            f = (double)((float)vec3i.getY() + this.dragon.getRandom().nextFloat() * 20.0F);
         } while(f < (double)vec3i.getY());

         this.target = new Vec3d(d, f, e);
      }

   }

   public void crystalDestroyed(EndCrystalEntity crystal, BlockPos pos, DamageSource source, @Nullable PlayerEntity player) {
      if (player != null && !player.abilities.invulnerable) {
         this.method_6843(player);
      }

   }
}
