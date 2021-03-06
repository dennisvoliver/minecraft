package net.minecraft.client.render.entity.model;

import com.google.common.collect.ImmutableList;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.model.ModelPart;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.GuardianEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

@Environment(EnvType.CLIENT)
public class GuardianEntityModel extends CompositeEntityModel<GuardianEntity> {
   private static final float[] field_17131 = new float[]{1.75F, 0.25F, 0.0F, 0.0F, 0.5F, 0.5F, 0.5F, 0.5F, 1.25F, 0.75F, 0.0F, 0.0F};
   private static final float[] field_17132 = new float[]{0.0F, 0.0F, 0.0F, 0.0F, 0.25F, 1.75F, 1.25F, 0.75F, 0.0F, 0.0F, 0.0F, 0.0F};
   private static final float[] field_17133 = new float[]{0.0F, 0.0F, 0.25F, 1.75F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.75F, 1.25F};
   private static final float[] field_17134 = new float[]{0.0F, 0.0F, 8.0F, -8.0F, -8.0F, 8.0F, 8.0F, -8.0F, 0.0F, 0.0F, 8.0F, -8.0F};
   private static final float[] field_17135 = new float[]{-8.0F, -8.0F, -8.0F, -8.0F, 0.0F, 0.0F, 0.0F, 0.0F, 8.0F, 8.0F, 8.0F, 8.0F};
   private static final float[] field_17136 = new float[]{8.0F, -8.0F, 0.0F, 0.0F, -8.0F, -8.0F, 8.0F, 8.0F, 8.0F, -8.0F, 0.0F, 0.0F};
   private final ModelPart body;
   private final ModelPart eye;
   private final ModelPart[] field_3380;
   private final ModelPart[] field_3378;

   public GuardianEntityModel() {
      this.textureWidth = 64;
      this.textureHeight = 64;
      this.field_3380 = new ModelPart[12];
      this.body = new ModelPart(this);
      this.body.setTextureOffset(0, 0).addCuboid(-6.0F, 10.0F, -8.0F, 12.0F, 12.0F, 16.0F);
      this.body.setTextureOffset(0, 28).addCuboid(-8.0F, 10.0F, -6.0F, 2.0F, 12.0F, 12.0F);
      this.body.setTextureOffset(0, 28).addCuboid(6.0F, 10.0F, -6.0F, 2.0F, 12.0F, 12.0F, true);
      this.body.setTextureOffset(16, 40).addCuboid(-6.0F, 8.0F, -6.0F, 12.0F, 2.0F, 12.0F);
      this.body.setTextureOffset(16, 40).addCuboid(-6.0F, 22.0F, -6.0F, 12.0F, 2.0F, 12.0F);

      for(int i = 0; i < this.field_3380.length; ++i) {
         this.field_3380[i] = new ModelPart(this, 0, 0);
         this.field_3380[i].addCuboid(-1.0F, -4.5F, -1.0F, 2.0F, 9.0F, 2.0F);
         this.body.addChild(this.field_3380[i]);
      }

      this.eye = new ModelPart(this, 8, 0);
      this.eye.addCuboid(-1.0F, 15.0F, 0.0F, 2.0F, 2.0F, 1.0F);
      this.body.addChild(this.eye);
      this.field_3378 = new ModelPart[3];
      this.field_3378[0] = new ModelPart(this, 40, 0);
      this.field_3378[0].addCuboid(-2.0F, 14.0F, 7.0F, 4.0F, 4.0F, 8.0F);
      this.field_3378[1] = new ModelPart(this, 0, 54);
      this.field_3378[1].addCuboid(0.0F, 14.0F, 0.0F, 3.0F, 3.0F, 7.0F);
      this.field_3378[2] = new ModelPart(this);
      this.field_3378[2].setTextureOffset(41, 32).addCuboid(0.0F, 14.0F, 0.0F, 2.0F, 2.0F, 6.0F);
      this.field_3378[2].setTextureOffset(25, 19).addCuboid(1.0F, 10.5F, 3.0F, 1.0F, 9.0F, 9.0F);
      this.body.addChild(this.field_3378[0]);
      this.field_3378[0].addChild(this.field_3378[1]);
      this.field_3378[1].addChild(this.field_3378[2]);
      this.method_24185(0.0F, 0.0F);
   }

   public Iterable<ModelPart> getParts() {
      return ImmutableList.of(this.body);
   }

   public void setAngles(GuardianEntity guardianEntity, float f, float g, float h, float i, float j) {
      float k = h - (float)guardianEntity.age;
      this.body.yaw = i * 0.017453292F;
      this.body.pitch = j * 0.017453292F;
      float l = (1.0F - guardianEntity.getTailAngle(k)) * 0.55F;
      this.method_24185(h, l);
      this.eye.pivotZ = -8.25F;
      Entity entity = MinecraftClient.getInstance().getCameraEntity();
      if (guardianEntity.hasBeamTarget()) {
         entity = guardianEntity.getBeamTarget();
      }

      if (entity != null) {
         Vec3d vec3d = ((Entity)entity).getCameraPosVec(0.0F);
         Vec3d vec3d2 = guardianEntity.getCameraPosVec(0.0F);
         double d = vec3d.y - vec3d2.y;
         if (d > 0.0D) {
            this.eye.pivotY = 0.0F;
         } else {
            this.eye.pivotY = 1.0F;
         }

         Vec3d vec3d3 = guardianEntity.getRotationVec(0.0F);
         vec3d3 = new Vec3d(vec3d3.x, 0.0D, vec3d3.z);
         Vec3d vec3d4 = (new Vec3d(vec3d2.x - vec3d.x, 0.0D, vec3d2.z - vec3d.z)).normalize().rotateY(1.5707964F);
         double e = vec3d3.dotProduct(vec3d4);
         this.eye.pivotX = MathHelper.sqrt((float)Math.abs(e)) * 2.0F * (float)Math.signum(e);
      }

      this.eye.visible = true;
      float m = guardianEntity.getSpikesExtension(k);
      this.field_3378[0].yaw = MathHelper.sin(m) * 3.1415927F * 0.05F;
      this.field_3378[1].yaw = MathHelper.sin(m) * 3.1415927F * 0.1F;
      this.field_3378[1].pivotX = -1.5F;
      this.field_3378[1].pivotY = 0.5F;
      this.field_3378[1].pivotZ = 14.0F;
      this.field_3378[2].yaw = MathHelper.sin(m) * 3.1415927F * 0.15F;
      this.field_3378[2].pivotX = 0.5F;
      this.field_3378[2].pivotY = 0.5F;
      this.field_3378[2].pivotZ = 6.0F;
   }

   private void method_24185(float f, float g) {
      for(int i = 0; i < 12; ++i) {
         this.field_3380[i].pitch = 3.1415927F * field_17131[i];
         this.field_3380[i].yaw = 3.1415927F * field_17132[i];
         this.field_3380[i].roll = 3.1415927F * field_17133[i];
         this.field_3380[i].pivotX = field_17134[i] * (1.0F + MathHelper.cos(f * 1.5F + (float)i) * 0.01F - g);
         this.field_3380[i].pivotY = 16.0F + field_17135[i] * (1.0F + MathHelper.cos(f * 1.5F + (float)i) * 0.01F - g);
         this.field_3380[i].pivotZ = field_17136[i] * (1.0F + MathHelper.cos(f * 1.5F + (float)i) * 0.01F - g);
      }

   }
}
