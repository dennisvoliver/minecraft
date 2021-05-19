package net.minecraft.util.math;

import com.google.common.collect.Lists;
import java.util.Iterator;
import java.util.List;

/**
 * Represents a density field in an area. Consider visualizing it like real
 * life gravity's distortion of space.
 */
public class GravityField {
   private final List<GravityField.Point> points = Lists.newArrayList();

   /**
    * Adds a point to the gravity field.
    */
   public void addPoint(BlockPos pos, double mass) {
      if (mass != 0.0D) {
         this.points.add(new GravityField.Point(pos, mass));
      }

   }

   /**
    * Calculate the gravity on a potential point at {@code pos} with {@code mass}.
    */
   public double calculate(BlockPos pos, double mass) {
      if (mass == 0.0D) {
         return 0.0D;
      } else {
         double d = 0.0D;

         GravityField.Point point;
         for(Iterator var6 = this.points.iterator(); var6.hasNext(); d += point.getGravityFactor(pos)) {
            point = (GravityField.Point)var6.next();
         }

         return d * mass;
      }
   }

   static class Point {
      private final BlockPos pos;
      private final double mass;

      public Point(BlockPos pos, double mass) {
         this.pos = pos;
         this.mass = mass;
      }

      public double getGravityFactor(BlockPos pos) {
         double d = this.pos.getSquaredDistance(pos);
         return d == 0.0D ? Double.POSITIVE_INFINITY : this.mass / Math.sqrt(d);
      }
   }
}
