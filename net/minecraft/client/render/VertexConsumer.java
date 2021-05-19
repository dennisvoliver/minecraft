package net.minecraft.client.render;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.util.math.Vector3f;
import net.minecraft.client.util.math.Vector4f;
import net.minecraft.util.math.Matrix3f;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Vec3i;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.system.MemoryStack;

@Environment(EnvType.CLIENT)
public interface VertexConsumer {
   Logger LOGGER = LogManager.getLogger();

   VertexConsumer vertex(double x, double y, double z);

   VertexConsumer color(int red, int green, int blue, int alpha);

   VertexConsumer texture(float u, float v);

   VertexConsumer overlay(int u, int v);

   VertexConsumer light(int u, int v);

   VertexConsumer normal(float x, float y, float z);

   void next();

   default void vertex(float x, float y, float z, float red, float green, float blue, float alpha, float u, float v, int overlay, int light, float normalX, float normalY, float normalZ) {
      this.vertex((double)x, (double)y, (double)z);
      this.color(red, green, blue, alpha);
      this.texture(u, v);
      this.overlay(overlay);
      this.light(light);
      this.normal(normalX, normalY, normalZ);
      this.next();
   }

   default VertexConsumer color(float red, float green, float blue, float alpha) {
      return this.color((int)(red * 255.0F), (int)(green * 255.0F), (int)(blue * 255.0F), (int)(alpha * 255.0F));
   }

   default VertexConsumer light(int uv) {
      return this.light(uv & '\uffff', uv >> 16 & '\uffff');
   }

   default VertexConsumer overlay(int uv) {
      return this.overlay(uv & '\uffff', uv >> 16 & '\uffff');
   }

   default void quad(MatrixStack.Entry matrixEntry, BakedQuad quad, float red, float green, float blue, int light, int overlay) {
      this.quad(matrixEntry, quad, new float[]{1.0F, 1.0F, 1.0F, 1.0F}, red, green, blue, new int[]{light, light, light, light}, overlay, false);
   }

   default void quad(MatrixStack.Entry matrixEntry, BakedQuad quad, float[] brightnesses, float red, float green, float blue, int[] lights, int overlay, boolean useQuadColorData) {
      int[] is = quad.getVertexData();
      Vec3i vec3i = quad.getFace().getVector();
      Vector3f vector3f = new Vector3f((float)vec3i.getX(), (float)vec3i.getY(), (float)vec3i.getZ());
      Matrix4f matrix4f = matrixEntry.getModel();
      vector3f.transform(matrixEntry.getNormal());
      int i = true;
      int j = is.length / 8;
      MemoryStack memoryStack = MemoryStack.stackPush();
      Throwable var17 = null;

      try {
         ByteBuffer byteBuffer = memoryStack.malloc(VertexFormats.POSITION_COLOR_TEXTURE_LIGHT_NORMAL.getVertexSize());
         IntBuffer intBuffer = byteBuffer.asIntBuffer();

         for(int k = 0; k < j; ++k) {
            intBuffer.clear();
            intBuffer.put(is, k * 8, 8);
            float f = byteBuffer.getFloat(0);
            float g = byteBuffer.getFloat(4);
            float h = byteBuffer.getFloat(8);
            float r;
            float s;
            float t;
            float v;
            float w;
            if (useQuadColorData) {
               float l = (float)(byteBuffer.get(12) & 255) / 255.0F;
               v = (float)(byteBuffer.get(13) & 255) / 255.0F;
               w = (float)(byteBuffer.get(14) & 255) / 255.0F;
               r = l * brightnesses[k] * red;
               s = v * brightnesses[k] * green;
               t = w * brightnesses[k] * blue;
            } else {
               r = brightnesses[k] * red;
               s = brightnesses[k] * green;
               t = brightnesses[k] * blue;
            }

            int u = lights[k];
            v = byteBuffer.getFloat(16);
            w = byteBuffer.getFloat(20);
            Vector4f vector4f = new Vector4f(f, g, h, 1.0F);
            vector4f.transform(matrix4f);
            this.vertex(vector4f.getX(), vector4f.getY(), vector4f.getZ(), r, s, t, 1.0F, v, w, overlay, u, vector3f.getX(), vector3f.getY(), vector3f.getZ());
         }
      } catch (Throwable var38) {
         var17 = var38;
         throw var38;
      } finally {
         if (memoryStack != null) {
            if (var17 != null) {
               try {
                  memoryStack.close();
               } catch (Throwable var37) {
                  var17.addSuppressed(var37);
               }
            } else {
               memoryStack.close();
            }
         }

      }

   }

   default VertexConsumer vertex(Matrix4f matrix, float x, float y, float z) {
      Vector4f vector4f = new Vector4f(x, y, z, 1.0F);
      vector4f.transform(matrix);
      return this.vertex((double)vector4f.getX(), (double)vector4f.getY(), (double)vector4f.getZ());
   }

   default VertexConsumer normal(Matrix3f matrix, float x, float y, float z) {
      Vector3f vector3f = new Vector3f(x, y, z);
      vector3f.transform(matrix);
      return this.normal(vector3f.getX(), vector3f.getY(), vector3f.getZ());
   }
}
