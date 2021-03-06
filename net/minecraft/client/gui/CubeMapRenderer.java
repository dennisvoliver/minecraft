package net.minecraft.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Matrix4f;

@Environment(EnvType.CLIENT)
public class CubeMapRenderer {
   private final Identifier[] faces = new Identifier[6];

   public CubeMapRenderer(Identifier faces) {
      for(int i = 0; i < 6; ++i) {
         this.faces[i] = new Identifier(faces.getNamespace(), faces.getPath() + '_' + i + ".png");
      }

   }

   public void draw(MinecraftClient client, float x, float y, float alpha) {
      Tessellator tessellator = Tessellator.getInstance();
      BufferBuilder bufferBuilder = tessellator.getBuffer();
      RenderSystem.matrixMode(5889);
      RenderSystem.pushMatrix();
      RenderSystem.loadIdentity();
      RenderSystem.multMatrix(Matrix4f.viewboxMatrix(85.0D, (float)client.getWindow().getFramebufferWidth() / (float)client.getWindow().getFramebufferHeight(), 0.05F, 10.0F));
      RenderSystem.matrixMode(5888);
      RenderSystem.pushMatrix();
      RenderSystem.loadIdentity();
      RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
      RenderSystem.rotatef(180.0F, 1.0F, 0.0F, 0.0F);
      RenderSystem.enableBlend();
      RenderSystem.disableAlphaTest();
      RenderSystem.disableCull();
      RenderSystem.depthMask(false);
      RenderSystem.defaultBlendFunc();
      int i = true;

      for(int j = 0; j < 4; ++j) {
         RenderSystem.pushMatrix();
         float f = ((float)(j % 2) / 2.0F - 0.5F) / 256.0F;
         float g = ((float)(j / 2) / 2.0F - 0.5F) / 256.0F;
         float h = 0.0F;
         RenderSystem.translatef(f, g, 0.0F);
         RenderSystem.rotatef(x, 1.0F, 0.0F, 0.0F);
         RenderSystem.rotatef(y, 0.0F, 1.0F, 0.0F);

         for(int k = 0; k < 6; ++k) {
            client.getTextureManager().bindTexture(this.faces[k]);
            bufferBuilder.begin(7, VertexFormats.POSITION_TEXTURE_COLOR);
            int l = Math.round(255.0F * alpha) / (j + 1);
            if (k == 0) {
               bufferBuilder.vertex(-1.0D, -1.0D, 1.0D).texture(0.0F, 0.0F).color(255, 255, 255, l).next();
               bufferBuilder.vertex(-1.0D, 1.0D, 1.0D).texture(0.0F, 1.0F).color(255, 255, 255, l).next();
               bufferBuilder.vertex(1.0D, 1.0D, 1.0D).texture(1.0F, 1.0F).color(255, 255, 255, l).next();
               bufferBuilder.vertex(1.0D, -1.0D, 1.0D).texture(1.0F, 0.0F).color(255, 255, 255, l).next();
            }

            if (k == 1) {
               bufferBuilder.vertex(1.0D, -1.0D, 1.0D).texture(0.0F, 0.0F).color(255, 255, 255, l).next();
               bufferBuilder.vertex(1.0D, 1.0D, 1.0D).texture(0.0F, 1.0F).color(255, 255, 255, l).next();
               bufferBuilder.vertex(1.0D, 1.0D, -1.0D).texture(1.0F, 1.0F).color(255, 255, 255, l).next();
               bufferBuilder.vertex(1.0D, -1.0D, -1.0D).texture(1.0F, 0.0F).color(255, 255, 255, l).next();
            }

            if (k == 2) {
               bufferBuilder.vertex(1.0D, -1.0D, -1.0D).texture(0.0F, 0.0F).color(255, 255, 255, l).next();
               bufferBuilder.vertex(1.0D, 1.0D, -1.0D).texture(0.0F, 1.0F).color(255, 255, 255, l).next();
               bufferBuilder.vertex(-1.0D, 1.0D, -1.0D).texture(1.0F, 1.0F).color(255, 255, 255, l).next();
               bufferBuilder.vertex(-1.0D, -1.0D, -1.0D).texture(1.0F, 0.0F).color(255, 255, 255, l).next();
            }

            if (k == 3) {
               bufferBuilder.vertex(-1.0D, -1.0D, -1.0D).texture(0.0F, 0.0F).color(255, 255, 255, l).next();
               bufferBuilder.vertex(-1.0D, 1.0D, -1.0D).texture(0.0F, 1.0F).color(255, 255, 255, l).next();
               bufferBuilder.vertex(-1.0D, 1.0D, 1.0D).texture(1.0F, 1.0F).color(255, 255, 255, l).next();
               bufferBuilder.vertex(-1.0D, -1.0D, 1.0D).texture(1.0F, 0.0F).color(255, 255, 255, l).next();
            }

            if (k == 4) {
               bufferBuilder.vertex(-1.0D, -1.0D, -1.0D).texture(0.0F, 0.0F).color(255, 255, 255, l).next();
               bufferBuilder.vertex(-1.0D, -1.0D, 1.0D).texture(0.0F, 1.0F).color(255, 255, 255, l).next();
               bufferBuilder.vertex(1.0D, -1.0D, 1.0D).texture(1.0F, 1.0F).color(255, 255, 255, l).next();
               bufferBuilder.vertex(1.0D, -1.0D, -1.0D).texture(1.0F, 0.0F).color(255, 255, 255, l).next();
            }

            if (k == 5) {
               bufferBuilder.vertex(-1.0D, 1.0D, 1.0D).texture(0.0F, 0.0F).color(255, 255, 255, l).next();
               bufferBuilder.vertex(-1.0D, 1.0D, -1.0D).texture(0.0F, 1.0F).color(255, 255, 255, l).next();
               bufferBuilder.vertex(1.0D, 1.0D, -1.0D).texture(1.0F, 1.0F).color(255, 255, 255, l).next();
               bufferBuilder.vertex(1.0D, 1.0D, 1.0D).texture(1.0F, 0.0F).color(255, 255, 255, l).next();
            }

            tessellator.draw();
         }

         RenderSystem.popMatrix();
         RenderSystem.colorMask(true, true, true, false);
      }

      RenderSystem.colorMask(true, true, true, true);
      RenderSystem.matrixMode(5889);
      RenderSystem.popMatrix();
      RenderSystem.matrixMode(5888);
      RenderSystem.popMatrix();
      RenderSystem.depthMask(true);
      RenderSystem.enableCull();
      RenderSystem.enableDepthTest();
   }

   public CompletableFuture<Void> loadTexturesAsync(TextureManager textureManager, Executor executor) {
      CompletableFuture<?>[] completableFutures = new CompletableFuture[6];

      for(int i = 0; i < completableFutures.length; ++i) {
         completableFutures[i] = textureManager.loadTextureAsync(this.faces[i], executor);
      }

      return CompletableFuture.allOf(completableFutures);
   }
}
