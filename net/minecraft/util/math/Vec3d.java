package net.minecraft.util.math;

import java.util.EnumSet;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.util.math.Vector3f;

public class Vec3d implements Position {
   public static final Vec3d ZERO = new Vec3d(0.0D, 0.0D, 0.0D);
   public final double x;
   public final double y;
   public final double z;

   @Environment(EnvType.CLIENT)
   public static Vec3d unpackRgb(int rgb) {
      double d = (double)(rgb >> 16 & 255) / 255.0D;
      double e = (double)(rgb >> 8 & 255) / 255.0D;
      double f = (double)(rgb & 255) / 255.0D;
      return new Vec3d(d, e, f);
   }

   public static Vec3d ofCenter(Vec3i vec) {
      return new Vec3d((double)vec.getX() + 0.5D, (double)vec.getY() + 0.5D, (double)vec.getZ() + 0.5D);
   }

   public static Vec3d of(Vec3i vec) {
      return new Vec3d((double)vec.getX(), (double)vec.getY(), (double)vec.getZ());
   }

   public static Vec3d ofBottomCenter(Vec3i vec) {
      return new Vec3d((double)vec.getX() + 0.5D, (double)vec.getY(), (double)vec.getZ() + 0.5D);
   }

   public static Vec3d ofCenter(Vec3i vec, double deltaY) {
      return new Vec3d((double)vec.getX() + 0.5D, (double)vec.getY() + deltaY, (double)vec.getZ() + 0.5D);
   }

   public Vec3d(double x, double y, double z) {
      this.x = x;
      this.y = y;
      this.z = z;
   }

   public Vec3d(Vector3f vec) {
      this((double)vec.getX(), (double)vec.getY(), (double)vec.getZ());
   }

   public Vec3d reverseSubtract(Vec3d vec) {
      return new Vec3d(vec.x - this.x, vec.y - this.y, vec.z - this.z);
   }

   public Vec3d normalize() {
      double d = (double)MathHelper.sqrt(this.x * this.x + this.y * this.y + this.z * this.z);
      return d < 1.0E-4D ? ZERO : new Vec3d(this.x / d, this.y / d, this.z / d);
   }

   public double dotProduct(Vec3d vec) {
      return this.x * vec.x + this.y * vec.y + this.z * vec.z;
   }

   public Vec3d crossProduct(Vec3d vec) {
      return new Vec3d(this.y * vec.z - this.z * vec.y, this.z * vec.x - this.x * vec.z, this.x * vec.y - this.y * vec.x);
   }

   public Vec3d subtract(Vec3d vec) {
      return this.subtract(vec.x, vec.y, vec.z);
   }

   public Vec3d subtract(double x, double y, double z) {
      return this.add(-x, -y, -z);
   }

   public Vec3d add(Vec3d vec) {
      return this.add(vec.x, vec.y, vec.z);
   }

   public Vec3d add(double x, double y, double z) {
      return new Vec3d(this.x + x, this.y + y, this.z + z);
   }

   public boolean isInRange(Position pos, double radius) {
      return this.squaredDistanceTo(pos.getX(), pos.getY(), pos.getZ()) < radius * radius;
   }

   public double distanceTo(Vec3d vec) {
      double d = vec.x - this.x;
      double e = vec.y - this.y;
      double f = vec.z - this.z;
      return (double)MathHelper.sqrt(d * d + e * e + f * f);
   }

   public double squaredDistanceTo(Vec3d vec) {
      double d = vec.x - this.x;
      double e = vec.y - this.y;
      double f = vec.z - this.z;
      return d * d + e * e + f * f;
   }

   public double squaredDistanceTo(double x, double y, double z) {
      double d = x - this.x;
      double e = y - this.y;
      double f = z - this.z;
      return d * d + e * e + f * f;
   }

   public Vec3d multiply(double mult) {
      return this.multiply(mult, mult, mult);
   }

   @Environment(EnvType.CLIENT)
   public Vec3d negate() {
      return this.multiply(-1.0D);
   }

   public Vec3d multiply(Vec3d mult) {
      return this.multiply(mult.x, mult.y, mult.z);
   }

   public Vec3d multiply(double multX, double multY, double multZ) {
      return new Vec3d(this.x * multX, this.y * multY, this.z * multZ);
   }

   public double length() {
      return (double)MathHelper.sqrt(this.x * this.x + this.y * this.y + this.z * this.z);
   }

   public double lengthSquared() {
      return this.x * this.x + this.y * this.y + this.z * this.z;
   }

   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (!(o instanceof Vec3d)) {
         return false;
      } else {
         Vec3d vec3d = (Vec3d)o;
         if (Double.compare(vec3d.x, this.x) != 0) {
            return false;
         } else if (Double.compare(vec3d.y, this.y) != 0) {
            return false;
         } else {
            return Double.compare(vec3d.z, this.z) == 0;
         }
      }
   }

   public int hashCode() {
      long l = Double.doubleToLongBits(this.x);
      int i = (int)(l ^ l >>> 32);
      l = Double.doubleToLongBits(this.y);
      i = 31 * i + (int)(l ^ l >>> 32);
      l = Double.doubleToLongBits(this.z);
      i = 31 * i + (int)(l ^ l >>> 32);
      return i;
   }

   public String toString() {
      return "(" + this.x + ", " + this.y + ", " + this.z + ")";
   }

   public Vec3d rotateX(float angle) {
      float f = MathHelper.cos(angle);
      float g = MathHelper.sin(angle);
      double d = this.x;
      double e = this.y * (double)f + this.z * (double)g;
      double h = this.z * (double)f - this.y * (double)g;
      return new Vec3d(d, e, h);
   }

   public Vec3d rotateY(float angle) {
      float f = MathHelper.cos(angle);
      float g = MathHelper.sin(angle);
      double d = this.x * (double)f + this.z * (double)g;
      double e = this.y;
      double h = this.z * (double)f - this.x * (double)g;
      return new Vec3d(d, e, h);
   }

   @Environment(EnvType.CLIENT)
   public Vec3d rotateZ(float angle) {
      float f = MathHelper.cos(angle);
      float g = MathHelper.sin(angle);
      double d = this.x * (double)f + this.y * (double)g;
      double e = this.y * (double)f - this.x * (double)g;
      double h = this.z;
      return new Vec3d(d, e, h);
   }

   @Environment(EnvType.CLIENT)
   public static Vec3d fromPolar(Vec2f polar) {
      return fromPolar(polar.x, polar.y);
   }

   @Environment(EnvType.CLIENT)
   public static Vec3d fromPolar(float pitch, float yaw) {
      float f = MathHelper.cos(-yaw * 0.017453292F - 3.1415927F);
      float g = MathHelper.sin(-yaw * 0.017453292F - 3.1415927F);
      float h = -MathHelper.cos(-pitch * 0.017453292F);
      float i = MathHelper.sin(-pitch * 0.017453292F);
      return new Vec3d((double)(g * h), (double)i, (double)(f * h));
   }

   public Vec3d floorAlongAxes(EnumSet<Direction.Axis> axes) {
      double d = axes.contains(Direction.Axis.X) ? (double)MathHelper.floor(this.x) : this.x;
      double e = axes.contains(Direction.Axis.Y) ? (double)MathHelper.floor(this.y) : this.y;
      double f = axes.contains(Direction.Axis.Z) ? (double)MathHelper.floor(this.z) : this.z;
      return new Vec3d(d, e, f);
   }

   public double getComponentAlongAxis(Direction.Axis axis) {
      return axis.choose(this.x, this.y, this.z);
   }

   public final double getX() {
      return this.x;
   }

   public final double getY() {
      return this.y;
   }

   public final double getZ() {
      return this.z;
   }
}
