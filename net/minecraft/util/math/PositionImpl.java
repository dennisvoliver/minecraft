package net.minecraft.util.math;

public class PositionImpl implements Position {
   protected final double x;
   protected final double y;
   protected final double z;

   public PositionImpl(double x, double y, double z) {
      this.x = x;
      this.y = y;
      this.z = z;
   }

   public double getX() {
      return this.x;
   }

   public double getY() {
      return this.y;
   }

   public double getZ() {
      return this.z;
   }
}
