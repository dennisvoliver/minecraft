package net.minecraft.client.particle;

import java.util.Random;
import java.util.stream.Stream;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.ShapeContext;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.util.collection.ReusableStream;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

@Environment(EnvType.CLIENT)
public abstract class Particle {
   private static final Box EMPTY_BOUNDING_BOX = new Box(0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D);
   protected final ClientWorld world;
   protected double prevPosX;
   protected double prevPosY;
   protected double prevPosZ;
   protected double x;
   protected double y;
   protected double z;
   protected double velocityX;
   protected double velocityY;
   protected double velocityZ;
   private Box boundingBox;
   protected boolean onGround;
   protected boolean collidesWithWorld;
   private boolean field_21507;
   protected boolean dead;
   protected float spacingXZ;
   protected float spacingY;
   protected final Random random;
   protected int age;
   protected int maxAge;
   protected float gravityStrength;
   protected float colorRed;
   protected float colorGreen;
   protected float colorBlue;
   protected float colorAlpha;
   protected float angle;
   protected float prevAngle;

   protected Particle(ClientWorld world, double x, double y, double z) {
      this.boundingBox = EMPTY_BOUNDING_BOX;
      this.collidesWithWorld = true;
      this.spacingXZ = 0.6F;
      this.spacingY = 1.8F;
      this.random = new Random();
      this.colorRed = 1.0F;
      this.colorGreen = 1.0F;
      this.colorBlue = 1.0F;
      this.colorAlpha = 1.0F;
      this.world = world;
      this.setBoundingBoxSpacing(0.2F, 0.2F);
      this.setPos(x, y, z);
      this.prevPosX = x;
      this.prevPosY = y;
      this.prevPosZ = z;
      this.maxAge = (int)(4.0F / (this.random.nextFloat() * 0.9F + 0.1F));
   }

   public Particle(ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
      this(world, x, y, z);
      this.velocityX = velocityX + (Math.random() * 2.0D - 1.0D) * 0.4000000059604645D;
      this.velocityY = velocityY + (Math.random() * 2.0D - 1.0D) * 0.4000000059604645D;
      this.velocityZ = velocityZ + (Math.random() * 2.0D - 1.0D) * 0.4000000059604645D;
      float f = (float)(Math.random() + Math.random() + 1.0D) * 0.15F;
      float g = MathHelper.sqrt(this.velocityX * this.velocityX + this.velocityY * this.velocityY + this.velocityZ * this.velocityZ);
      this.velocityX = this.velocityX / (double)g * (double)f * 0.4000000059604645D;
      this.velocityY = this.velocityY / (double)g * (double)f * 0.4000000059604645D + 0.10000000149011612D;
      this.velocityZ = this.velocityZ / (double)g * (double)f * 0.4000000059604645D;
   }

   public Particle move(float speed) {
      this.velocityX *= (double)speed;
      this.velocityY = (this.velocityY - 0.10000000149011612D) * (double)speed + 0.10000000149011612D;
      this.velocityZ *= (double)speed;
      return this;
   }

   public Particle scale(float scale) {
      this.setBoundingBoxSpacing(0.2F * scale, 0.2F * scale);
      return this;
   }

   public void setColor(float red, float green, float blue) {
      this.colorRed = red;
      this.colorGreen = green;
      this.colorBlue = blue;
   }

   protected void setColorAlpha(float alpha) {
      this.colorAlpha = alpha;
   }

   public void setMaxAge(int maxAge) {
      this.maxAge = maxAge;
   }

   public int getMaxAge() {
      return this.maxAge;
   }

   public void tick() {
      this.prevPosX = this.x;
      this.prevPosY = this.y;
      this.prevPosZ = this.z;
      if (this.age++ >= this.maxAge) {
         this.markDead();
      } else {
         this.velocityY -= 0.04D * (double)this.gravityStrength;
         this.move(this.velocityX, this.velocityY, this.velocityZ);
         this.velocityX *= 0.9800000190734863D;
         this.velocityY *= 0.9800000190734863D;
         this.velocityZ *= 0.9800000190734863D;
         if (this.onGround) {
            this.velocityX *= 0.699999988079071D;
            this.velocityZ *= 0.699999988079071D;
         }

      }
   }

   public abstract void buildGeometry(VertexConsumer vertexConsumer, Camera camera, float tickDelta);

   public abstract ParticleTextureSheet getType();

   public String toString() {
      return this.getClass().getSimpleName() + ", Pos (" + this.x + "," + this.y + "," + this.z + "), RGBA (" + this.colorRed + "," + this.colorGreen + "," + this.colorBlue + "," + this.colorAlpha + "), Age " + this.age;
   }

   public void markDead() {
      this.dead = true;
   }

   protected void setBoundingBoxSpacing(float spacingXZ, float spacingY) {
      if (spacingXZ != this.spacingXZ || spacingY != this.spacingY) {
         this.spacingXZ = spacingXZ;
         this.spacingY = spacingY;
         Box box = this.getBoundingBox();
         double d = (box.minX + box.maxX - (double)spacingXZ) / 2.0D;
         double e = (box.minZ + box.maxZ - (double)spacingXZ) / 2.0D;
         this.setBoundingBox(new Box(d, box.minY, e, d + (double)this.spacingXZ, box.minY + (double)this.spacingY, e + (double)this.spacingXZ));
      }

   }

   public void setPos(double x, double y, double z) {
      this.x = x;
      this.y = y;
      this.z = z;
      float f = this.spacingXZ / 2.0F;
      float g = this.spacingY;
      this.setBoundingBox(new Box(x - (double)f, y, z - (double)f, x + (double)f, y + (double)g, z + (double)f));
   }

   public void move(double dx, double dy, double dz) {
      if (!this.field_21507) {
         double d = dx;
         double e = dy;
         if (this.collidesWithWorld && (dx != 0.0D || dy != 0.0D || dz != 0.0D)) {
            Vec3d vec3d = Entity.adjustMovementForCollisions((Entity)null, new Vec3d(dx, dy, dz), this.getBoundingBox(), this.world, ShapeContext.absent(), new ReusableStream(Stream.empty()));
            dx = vec3d.x;
            dy = vec3d.y;
            dz = vec3d.z;
         }

         if (dx != 0.0D || dy != 0.0D || dz != 0.0D) {
            this.setBoundingBox(this.getBoundingBox().offset(dx, dy, dz));
            this.repositionFromBoundingBox();
         }

         if (Math.abs(dy) >= 9.999999747378752E-6D && Math.abs(dy) < 9.999999747378752E-6D) {
            this.field_21507 = true;
         }

         this.onGround = dy != dy && e < 0.0D;
         if (d != dx) {
            this.velocityX = 0.0D;
         }

         if (dz != dz) {
            this.velocityZ = 0.0D;
         }

      }
   }

   protected void repositionFromBoundingBox() {
      Box box = this.getBoundingBox();
      this.x = (box.minX + box.maxX) / 2.0D;
      this.y = box.minY;
      this.z = (box.minZ + box.maxZ) / 2.0D;
   }

   protected int getColorMultiplier(float tint) {
      BlockPos blockPos = new BlockPos(this.x, this.y, this.z);
      return this.world.isChunkLoaded(blockPos) ? WorldRenderer.getLightmapCoordinates(this.world, blockPos) : 0;
   }

   public boolean isAlive() {
      return !this.dead;
   }

   public Box getBoundingBox() {
      return this.boundingBox;
   }

   public void setBoundingBox(Box box) {
      this.boundingBox = box;
   }
}
