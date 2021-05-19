package net.minecraft.client.gl;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.mojang.blaze3d.systems.RenderSystem;
import java.io.Closeable;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.math.Matrix4f;
import org.apache.commons.io.IOUtils;

@Environment(EnvType.CLIENT)
public class ShaderEffect implements AutoCloseable {
   private final Framebuffer mainTarget;
   private final ResourceManager resourceManager;
   private final String name;
   private final List<PostProcessShader> passes = Lists.newArrayList();
   private final Map<String, Framebuffer> targetsByName = Maps.newHashMap();
   private final List<Framebuffer> defaultSizedTargets = Lists.newArrayList();
   private Matrix4f projectionMatrix;
   private int width;
   private int height;
   private float time;
   private float lastTickDelta;

   public ShaderEffect(TextureManager textureManager, ResourceManager resourceManager, Framebuffer framebuffer, Identifier location) throws IOException, JsonSyntaxException {
      this.resourceManager = resourceManager;
      this.mainTarget = framebuffer;
      this.time = 0.0F;
      this.lastTickDelta = 0.0F;
      this.width = framebuffer.viewportWidth;
      this.height = framebuffer.viewportHeight;
      this.name = location.toString();
      this.setupProjectionMatrix();
      this.parseEffect(textureManager, location);
   }

   private void parseEffect(TextureManager textureManager, Identifier location) throws IOException, JsonSyntaxException {
      Resource resource = null;

      try {
         resource = this.resourceManager.getResource(location);
         JsonObject jsonObject = JsonHelper.deserialize((Reader)(new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8)));
         Iterator var7;
         JsonElement jsonElement2;
         ShaderParseException shaderParseException2;
         JsonArray jsonArray2;
         int j;
         if (JsonHelper.hasArray(jsonObject, "targets")) {
            jsonArray2 = jsonObject.getAsJsonArray("targets");
            j = 0;

            for(var7 = jsonArray2.iterator(); var7.hasNext(); ++j) {
               jsonElement2 = (JsonElement)var7.next();

               try {
                  this.parseTarget(jsonElement2);
               } catch (Exception var17) {
                  shaderParseException2 = ShaderParseException.wrap(var17);
                  shaderParseException2.addFaultyElement("targets[" + j + "]");
                  throw shaderParseException2;
               }
            }
         }

         if (JsonHelper.hasArray(jsonObject, "passes")) {
            jsonArray2 = jsonObject.getAsJsonArray("passes");
            j = 0;

            for(var7 = jsonArray2.iterator(); var7.hasNext(); ++j) {
               jsonElement2 = (JsonElement)var7.next();

               try {
                  this.parsePass(textureManager, jsonElement2);
               } catch (Exception var16) {
                  shaderParseException2 = ShaderParseException.wrap(var16);
                  shaderParseException2.addFaultyElement("passes[" + j + "]");
                  throw shaderParseException2;
               }
            }
         }
      } catch (Exception var18) {
         String string2;
         if (resource != null) {
            string2 = " (" + resource.getResourcePackName() + ")";
         } else {
            string2 = "";
         }

         ShaderParseException shaderParseException3 = ShaderParseException.wrap(var18);
         shaderParseException3.addFaultyFile(location.getPath() + string2);
         throw shaderParseException3;
      } finally {
         IOUtils.closeQuietly((Closeable)resource);
      }

   }

   private void parseTarget(JsonElement jsonTarget) throws ShaderParseException {
      if (JsonHelper.isString(jsonTarget)) {
         this.addTarget(jsonTarget.getAsString(), this.width, this.height);
      } else {
         JsonObject jsonObject = JsonHelper.asObject(jsonTarget, "target");
         String string = JsonHelper.getString(jsonObject, "name");
         int i = JsonHelper.getInt(jsonObject, "width", this.width);
         int j = JsonHelper.getInt(jsonObject, "height", this.height);
         if (this.targetsByName.containsKey(string)) {
            throw new ShaderParseException(string + " is already defined");
         }

         this.addTarget(string, i, j);
      }

   }

   private void parsePass(TextureManager textureManager, JsonElement jsonPass) throws IOException {
      JsonObject jsonObject = JsonHelper.asObject(jsonPass, "pass");
      String string = JsonHelper.getString(jsonObject, "name");
      String string2 = JsonHelper.getString(jsonObject, "intarget");
      String string3 = JsonHelper.getString(jsonObject, "outtarget");
      Framebuffer framebuffer = this.getTarget(string2);
      Framebuffer framebuffer2 = this.getTarget(string3);
      if (framebuffer == null) {
         throw new ShaderParseException("Input target '" + string2 + "' does not exist");
      } else if (framebuffer2 == null) {
         throw new ShaderParseException("Output target '" + string3 + "' does not exist");
      } else {
         PostProcessShader postProcessShader = this.addPass(string, framebuffer, framebuffer2);
         JsonArray jsonArray = JsonHelper.getArray(jsonObject, "auxtargets", (JsonArray)null);
         if (jsonArray != null) {
            int i = 0;

            for(Iterator var12 = jsonArray.iterator(); var12.hasNext(); ++i) {
               JsonElement jsonElement = (JsonElement)var12.next();

               try {
                  JsonObject jsonObject2 = JsonHelper.asObject(jsonElement, "auxtarget");
                  String string4 = JsonHelper.getString(jsonObject2, "name");
                  String string5 = JsonHelper.getString(jsonObject2, "id");
                  boolean bl2;
                  String string7;
                  if (string5.endsWith(":depth")) {
                     bl2 = true;
                     string7 = string5.substring(0, string5.lastIndexOf(58));
                  } else {
                     bl2 = false;
                     string7 = string5;
                  }

                  Framebuffer framebuffer3 = this.getTarget(string7);
                  if (framebuffer3 == null) {
                     if (bl2) {
                        throw new ShaderParseException("Render target '" + string7 + "' can't be used as depth buffer");
                     }

                     Identifier identifier = new Identifier("textures/effect/" + string7 + ".png");
                     Resource resource = null;

                     try {
                        resource = this.resourceManager.getResource(identifier);
                     } catch (FileNotFoundException var31) {
                        throw new ShaderParseException("Render target or texture '" + string7 + "' does not exist");
                     } finally {
                        IOUtils.closeQuietly((Closeable)resource);
                     }

                     textureManager.bindTexture(identifier);
                     AbstractTexture abstractTexture = textureManager.getTexture(identifier);
                     int j = JsonHelper.getInt(jsonObject2, "width");
                     int k = JsonHelper.getInt(jsonObject2, "height");
                     boolean var25 = JsonHelper.getBoolean(jsonObject2, "bilinear");
                     if (var25) {
                        RenderSystem.texParameter(3553, 10241, 9729);
                        RenderSystem.texParameter(3553, 10240, 9729);
                     } else {
                        RenderSystem.texParameter(3553, 10241, 9728);
                        RenderSystem.texParameter(3553, 10240, 9728);
                     }

                     postProcessShader.addAuxTarget(string4, abstractTexture::getGlId, j, k);
                  } else if (bl2) {
                     postProcessShader.addAuxTarget(string4, framebuffer3::getDepthAttachment, framebuffer3.textureWidth, framebuffer3.textureHeight);
                  } else {
                     postProcessShader.addAuxTarget(string4, framebuffer3::getColorAttachment, framebuffer3.textureWidth, framebuffer3.textureHeight);
                  }
               } catch (Exception var33) {
                  ShaderParseException shaderParseException = ShaderParseException.wrap(var33);
                  shaderParseException.addFaultyElement("auxtargets[" + i + "]");
                  throw shaderParseException;
               }
            }
         }

         JsonArray jsonArray2 = JsonHelper.getArray(jsonObject, "uniforms", (JsonArray)null);
         if (jsonArray2 != null) {
            int l = 0;

            for(Iterator var36 = jsonArray2.iterator(); var36.hasNext(); ++l) {
               JsonElement jsonElement2 = (JsonElement)var36.next();

               try {
                  this.parseUniform(jsonElement2);
               } catch (Exception var30) {
                  ShaderParseException shaderParseException2 = ShaderParseException.wrap(var30);
                  shaderParseException2.addFaultyElement("uniforms[" + l + "]");
                  throw shaderParseException2;
               }
            }
         }

      }
   }

   private void parseUniform(JsonElement jsonUniform) throws ShaderParseException {
      JsonObject jsonObject = JsonHelper.asObject(jsonUniform, "uniform");
      String string = JsonHelper.getString(jsonObject, "name");
      GlUniform glUniform = ((PostProcessShader)this.passes.get(this.passes.size() - 1)).getProgram().getUniformByName(string);
      if (glUniform == null) {
         throw new ShaderParseException("Uniform '" + string + "' does not exist");
      } else {
         float[] fs = new float[4];
         int i = 0;
         JsonArray jsonArray = JsonHelper.getArray(jsonObject, "values");

         for(Iterator var8 = jsonArray.iterator(); var8.hasNext(); ++i) {
            JsonElement jsonElement = (JsonElement)var8.next();

            try {
               fs[i] = JsonHelper.asFloat(jsonElement, "value");
            } catch (Exception var12) {
               ShaderParseException shaderParseException = ShaderParseException.wrap(var12);
               shaderParseException.addFaultyElement("values[" + i + "]");
               throw shaderParseException;
            }
         }

         switch(i) {
         case 0:
         default:
            break;
         case 1:
            glUniform.set(fs[0]);
            break;
         case 2:
            glUniform.set(fs[0], fs[1]);
            break;
         case 3:
            glUniform.set(fs[0], fs[1], fs[2]);
            break;
         case 4:
            glUniform.set(fs[0], fs[1], fs[2], fs[3]);
         }

      }
   }

   public Framebuffer getSecondaryTarget(String name) {
      return (Framebuffer)this.targetsByName.get(name);
   }

   public void addTarget(String name, int width, int height) {
      Framebuffer framebuffer = new Framebuffer(width, height, true, MinecraftClient.IS_SYSTEM_MAC);
      framebuffer.setClearColor(0.0F, 0.0F, 0.0F, 0.0F);
      this.targetsByName.put(name, framebuffer);
      if (width == this.width && height == this.height) {
         this.defaultSizedTargets.add(framebuffer);
      }

   }

   public void close() {
      Iterator var1 = this.targetsByName.values().iterator();

      while(var1.hasNext()) {
         Framebuffer framebuffer = (Framebuffer)var1.next();
         framebuffer.delete();
      }

      var1 = this.passes.iterator();

      while(var1.hasNext()) {
         PostProcessShader postProcessShader = (PostProcessShader)var1.next();
         postProcessShader.close();
      }

      this.passes.clear();
   }

   public PostProcessShader addPass(String programName, Framebuffer source, Framebuffer dest) throws IOException {
      PostProcessShader postProcessShader = new PostProcessShader(this.resourceManager, programName, source, dest);
      this.passes.add(this.passes.size(), postProcessShader);
      return postProcessShader;
   }

   private void setupProjectionMatrix() {
      this.projectionMatrix = Matrix4f.projectionMatrix((float)this.mainTarget.textureWidth, (float)this.mainTarget.textureHeight, 0.1F, 1000.0F);
   }

   public void setupDimensions(int targetsWidth, int targetsHeight) {
      this.width = this.mainTarget.textureWidth;
      this.height = this.mainTarget.textureHeight;
      this.setupProjectionMatrix();
      Iterator var3 = this.passes.iterator();

      while(var3.hasNext()) {
         PostProcessShader postProcessShader = (PostProcessShader)var3.next();
         postProcessShader.setProjectionMatrix(this.projectionMatrix);
      }

      var3 = this.defaultSizedTargets.iterator();

      while(var3.hasNext()) {
         Framebuffer framebuffer = (Framebuffer)var3.next();
         framebuffer.resize(targetsWidth, targetsHeight, MinecraftClient.IS_SYSTEM_MAC);
      }

   }

   public void render(float tickDelta) {
      if (tickDelta < this.lastTickDelta) {
         this.time += 1.0F - this.lastTickDelta;
         this.time += tickDelta;
      } else {
         this.time += tickDelta - this.lastTickDelta;
      }

      for(this.lastTickDelta = tickDelta; this.time > 20.0F; this.time -= 20.0F) {
      }

      Iterator var2 = this.passes.iterator();

      while(var2.hasNext()) {
         PostProcessShader postProcessShader = (PostProcessShader)var2.next();
         postProcessShader.render(this.time / 20.0F);
      }

   }

   public final String getName() {
      return this.name;
   }

   private Framebuffer getTarget(String name) {
      if (name == null) {
         return null;
      } else {
         return name.equals("minecraft:main") ? this.mainTarget : (Framebuffer)this.targetsByName.get(name);
      }
   }
}
