package net.minecraft.client.render.debug;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;

@Environment(EnvType.CLIENT)
public class ChunkBorderDebugRenderer implements DebugRenderer.Renderer {
   private final MinecraftClient client;

   public ChunkBorderDebugRenderer(MinecraftClient client) {
      this.client = client;
   }

   public void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, double cameraX, double cameraY, double cameraZ) {
      RenderSystem.enableDepthTest();
      RenderSystem.shadeModel(7425);
      RenderSystem.enableAlphaTest();
      RenderSystem.defaultAlphaFunc();
      Entity entity = this.client.gameRenderer.getCamera().getFocusedEntity();
      Tessellator tessellator = Tessellator.getInstance();
      BufferBuilder bufferBuilder = tessellator.getBuffer();
      double d = 0.0D - cameraY;
      double e = 256.0D - cameraY;
      RenderSystem.disableTexture();
      RenderSystem.disableBlend();
      double f = (double)(entity.chunkX << 4) - cameraX;
      double g = (double)(entity.chunkZ << 4) - cameraZ;
      RenderSystem.lineWidth(1.0F);
      bufferBuilder.begin(3, VertexFormats.POSITION_COLOR);

      int p;
      int o;
      for(p = -16; p <= 32; p += 16) {
         for(o = -16; o <= 32; o += 16) {
            bufferBuilder.vertex(f + (double)p, d, g + (double)o).color(1.0F, 0.0F, 0.0F, 0.0F).next();
            bufferBuilder.vertex(f + (double)p, d, g + (double)o).color(1.0F, 0.0F, 0.0F, 0.5F).next();
            bufferBuilder.vertex(f + (double)p, e, g + (double)o).color(1.0F, 0.0F, 0.0F, 0.5F).next();
            bufferBuilder.vertex(f + (double)p, e, g + (double)o).color(1.0F, 0.0F, 0.0F, 0.0F).next();
         }
      }

      for(p = 2; p < 16; p += 2) {
         bufferBuilder.vertex(f + (double)p, d, g).color(1.0F, 1.0F, 0.0F, 0.0F).next();
         bufferBuilder.vertex(f + (double)p, d, g).color(1.0F, 1.0F, 0.0F, 1.0F).next();
         bufferBuilder.vertex(f + (double)p, e, g).color(1.0F, 1.0F, 0.0F, 1.0F).next();
         bufferBuilder.vertex(f + (double)p, e, g).color(1.0F, 1.0F, 0.0F, 0.0F).next();
         bufferBuilder.vertex(f + (double)p, d, g + 16.0D).color(1.0F, 1.0F, 0.0F, 0.0F).next();
         bufferBuilder.vertex(f + (double)p, d, g + 16.0D).color(1.0F, 1.0F, 0.0F, 1.0F).next();
         bufferBuilder.vertex(f + (double)p, e, g + 16.0D).color(1.0F, 1.0F, 0.0F, 1.0F).next();
         bufferBuilder.vertex(f + (double)p, e, g + 16.0D).color(1.0F, 1.0F, 0.0F, 0.0F).next();
      }

      for(p = 2; p < 16; p += 2) {
         bufferBuilder.vertex(f, d, g + (double)p).color(1.0F, 1.0F, 0.0F, 0.0F).next();
         bufferBuilder.vertex(f, d, g + (double)p).color(1.0F, 1.0F, 0.0F, 1.0F).next();
         bufferBuilder.vertex(f, e, g + (double)p).color(1.0F, 1.0F, 0.0F, 1.0F).next();
         bufferBuilder.vertex(f, e, g + (double)p).color(1.0F, 1.0F, 0.0F, 0.0F).next();
         bufferBuilder.vertex(f + 16.0D, d, g + (double)p).color(1.0F, 1.0F, 0.0F, 0.0F).next();
         bufferBuilder.vertex(f + 16.0D, d, g + (double)p).color(1.0F, 1.0F, 0.0F, 1.0F).next();
         bufferBuilder.vertex(f + 16.0D, e, g + (double)p).color(1.0F, 1.0F, 0.0F, 1.0F).next();
         bufferBuilder.vertex(f + 16.0D, e, g + (double)p).color(1.0F, 1.0F, 0.0F, 0.0F).next();
      }

      double q;
      for(p = 0; p <= 256; p += 2) {
         q = (double)p - cameraY;
         bufferBuilder.vertex(f, q, g).color(1.0F, 1.0F, 0.0F, 0.0F).next();
         bufferBuilder.vertex(f, q, g).color(1.0F, 1.0F, 0.0F, 1.0F).next();
         bufferBuilder.vertex(f, q, g + 16.0D).color(1.0F, 1.0F, 0.0F, 1.0F).next();
         bufferBuilder.vertex(f + 16.0D, q, g + 16.0D).color(1.0F, 1.0F, 0.0F, 1.0F).next();
         bufferBuilder.vertex(f + 16.0D, q, g).color(1.0F, 1.0F, 0.0F, 1.0F).next();
         bufferBuilder.vertex(f, q, g).color(1.0F, 1.0F, 0.0F, 1.0F).next();
         bufferBuilder.vertex(f, q, g).color(1.0F, 1.0F, 0.0F, 0.0F).next();
      }

      tessellator.draw();
      RenderSystem.lineWidth(2.0F);
      bufferBuilder.begin(3, VertexFormats.POSITION_COLOR);

      for(p = 0; p <= 16; p += 16) {
         for(o = 0; o <= 16; o += 16) {
            bufferBuilder.vertex(f + (double)p, d, g + (double)o).color(0.25F, 0.25F, 1.0F, 0.0F).next();
            bufferBuilder.vertex(f + (double)p, d, g + (double)o).color(0.25F, 0.25F, 1.0F, 1.0F).next();
            bufferBuilder.vertex(f + (double)p, e, g + (double)o).color(0.25F, 0.25F, 1.0F, 1.0F).next();
            bufferBuilder.vertex(f + (double)p, e, g + (double)o).color(0.25F, 0.25F, 1.0F, 0.0F).next();
         }
      }

      for(p = 0; p <= 256; p += 16) {
         q = (double)p - cameraY;
         bufferBuilder.vertex(f, q, g).color(0.25F, 0.25F, 1.0F, 0.0F).next();
         bufferBuilder.vertex(f, q, g).color(0.25F, 0.25F, 1.0F, 1.0F).next();
         bufferBuilder.vertex(f, q, g + 16.0D).color(0.25F, 0.25F, 1.0F, 1.0F).next();
         bufferBuilder.vertex(f + 16.0D, q, g + 16.0D).color(0.25F, 0.25F, 1.0F, 1.0F).next();
         bufferBuilder.vertex(f + 16.0D, q, g).color(0.25F, 0.25F, 1.0F, 1.0F).next();
         bufferBuilder.vertex(f, q, g).color(0.25F, 0.25F, 1.0F, 1.0F).next();
         bufferBuilder.vertex(f, q, g).color(0.25F, 0.25F, 1.0F, 0.0F).next();
      }

      tessellator.draw();
      RenderSystem.lineWidth(1.0F);
      RenderSystem.enableBlend();
      RenderSystem.enableTexture();
      RenderSystem.shadeModel(7424);
   }
}
