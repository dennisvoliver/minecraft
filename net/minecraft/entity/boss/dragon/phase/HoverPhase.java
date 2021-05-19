package net.minecraft.entity.boss.dragon.phase;

import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

public class HoverPhase extends AbstractPhase {
   private Vec3d target;

   public HoverPhase(EnderDragonEntity enderDragonEntity) {
      super(enderDragonEntity);
   }

   public void serverTick() {
      if (this.target == null) {
         this.target = this.dragon.getPos();
      }

   }

   public boolean isSittingOrHovering() {
      return true;
   }

   public void beginPhase() {
      this.target = null;
   }

   public float getMaxYAcceleration() {
      return 1.0F;
   }

   @Nullable
   public Vec3d getTarget() {
      return this.target;
   }

   public PhaseType<HoverPhase> getType() {
      return PhaseType.HOVER;
   }
}
