package net.minecraft.entity.ai.control;

import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.util.math.MathHelper;

public class FlightMoveControl extends MoveControl {
   private final int maxPitchChange;
   private final boolean noGravity;

   public FlightMoveControl(MobEntity entity, int maxPitchChange, boolean noGravity) {
      super(entity);
      this.maxPitchChange = maxPitchChange;
      this.noGravity = noGravity;
   }

   public void tick() {
      if (this.state == MoveControl.State.MOVE_TO) {
         this.state = MoveControl.State.WAIT;
         this.entity.setNoGravity(true);
         double d = this.targetX - this.entity.getX();
         double e = this.targetY - this.entity.getY();
         double f = this.targetZ - this.entity.getZ();
         double g = d * d + e * e + f * f;
         if (g < 2.500000277905201E-7D) {
            this.entity.setUpwardSpeed(0.0F);
            this.entity.setForwardSpeed(0.0F);
            return;
         }

         float h = (float)(MathHelper.atan2(f, d) * 57.2957763671875D) - 90.0F;
         this.entity.yaw = this.changeAngle(this.entity.yaw, h, 90.0F);
         float j;
         if (this.entity.isOnGround()) {
            j = (float)(this.speed * this.entity.getAttributeValue(EntityAttributes.GENERIC_MOVEMENT_SPEED));
         } else {
            j = (float)(this.speed * this.entity.getAttributeValue(EntityAttributes.GENERIC_FLYING_SPEED));
         }

         this.entity.setMovementSpeed(j);
         double k = (double)MathHelper.sqrt(d * d + f * f);
         float l = (float)(-(MathHelper.atan2(e, k) * 57.2957763671875D));
         this.entity.pitch = this.changeAngle(this.entity.pitch, l, (float)this.maxPitchChange);
         this.entity.setUpwardSpeed(e > 0.0D ? j : -j);
      } else {
         if (!this.noGravity) {
            this.entity.setNoGravity(false);
         }

         this.entity.setUpwardSpeed(0.0F);
         this.entity.setForwardSpeed(0.0F);
      }

   }
}
