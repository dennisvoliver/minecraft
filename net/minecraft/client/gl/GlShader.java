package net.minecraft.client.gl;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.texture.TextureUtil;
import org.apache.commons.lang3.StringUtils;

@Environment(EnvType.CLIENT)
public class GlShader {
   private final GlShader.Type shaderType;
   private final String name;
   private final int shaderRef;
   private int refCount;

   private GlShader(GlShader.Type shaderType, int shaderRef, String name) {
      this.shaderType = shaderType;
      this.shaderRef = shaderRef;
      this.name = name;
   }

   public void attachTo(GlProgram glProgram) {
      RenderSystem.assertThread(RenderSystem::isOnRenderThread);
      ++this.refCount;
      GlStateManager.attachShader(glProgram.getProgramRef(), this.shaderRef);
   }

   public void release() {
      RenderSystem.assertThread(RenderSystem::isOnRenderThread);
      --this.refCount;
      if (this.refCount <= 0) {
         GlStateManager.deleteShader(this.shaderRef);
         this.shaderType.getLoadedShaders().remove(this.name);
      }

   }

   public String getName() {
      return this.name;
   }

   public static GlShader createFromResource(GlShader.Type type, String name, InputStream sourceCode, String string) throws IOException {
      RenderSystem.assertThread(RenderSystem::isOnRenderThread);
      String string2 = TextureUtil.readAllToString(sourceCode);
      if (string2 == null) {
         throw new IOException("Could not load program " + type.getName());
      } else {
         int i = GlStateManager.createShader(type.getGlType());
         GlStateManager.shaderSource(i, string2);
         GlStateManager.compileShader(i);
         if (GlStateManager.getShader(i, 35713) == 0) {
            String string3 = StringUtils.trim(GlStateManager.getShaderInfoLog(i, 32768));
            throw new IOException("Couldn't compile " + type.getName() + " program (" + string + ", " + name + ") : " + string3);
         } else {
            GlShader glShader = new GlShader(type, i, name);
            type.getLoadedShaders().put(name, glShader);
            return glShader;
         }
      }
   }

   @Environment(EnvType.CLIENT)
   public static enum Type {
      VERTEX("vertex", ".vsh", 35633),
      FRAGMENT("fragment", ".fsh", 35632);

      private final String name;
      private final String fileExtension;
      private final int glType;
      private final Map<String, GlShader> loadedShaders = Maps.newHashMap();

      private Type(String string2, String string3, int j) {
         this.name = string2;
         this.fileExtension = string3;
         this.glType = j;
      }

      public String getName() {
         return this.name;
      }

      public String getFileExtension() {
         return this.fileExtension;
      }

      private int getGlType() {
         return this.glType;
      }

      public Map<String, GlShader> getLoadedShaders() {
         return this.loadedShaders;
      }
   }
}
