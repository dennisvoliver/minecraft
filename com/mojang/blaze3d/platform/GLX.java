package com.mojang.blaze3d.platform;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.LongSupplier;
import java.util.function.Supplier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gl.GlDebug;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.Window;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.Version;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWErrorCallbackI;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GLCapabilities;
import oshi.SystemInfo;
import oshi.hardware.Processor;

@Environment(EnvType.CLIENT)
public class GLX {
   private static final Logger LOGGER = LogManager.getLogger();
   private static String capsString = "";
   private static String cpuInfo;
   private static final Map<Integer, String> LOOKUP_MAP = (Map)make(Maps.newHashMap(), (hashMap) -> {
      hashMap.put(0, "No error");
      hashMap.put(1280, "Enum parameter is invalid for this function");
      hashMap.put(1281, "Parameter is invalid for this function");
      hashMap.put(1282, "Current state is invalid for this function");
      hashMap.put(1283, "Stack overflow");
      hashMap.put(1284, "Stack underflow");
      hashMap.put(1285, "Out of memory");
      hashMap.put(1286, "Operation on incomplete framebuffer");
      hashMap.put(1286, "Operation on incomplete framebuffer");
   });

   public static String getOpenGLVersionString() {
      RenderSystem.assertThread(RenderSystem::isOnRenderThread);
      return GLFW.glfwGetCurrentContext() == 0L ? "NO CONTEXT" : GlStateManager.getString(7937) + " GL version " + GlStateManager.getString(7938) + ", " + GlStateManager.getString(7936);
   }

   public static int _getRefreshRate(Window window) {
      RenderSystem.assertThread(RenderSystem::isOnRenderThread);
      long l = GLFW.glfwGetWindowMonitor(window.getHandle());
      if (l == 0L) {
         l = GLFW.glfwGetPrimaryMonitor();
      }

      GLFWVidMode gLFWVidMode = l == 0L ? null : GLFW.glfwGetVideoMode(l);
      return gLFWVidMode == null ? 0 : gLFWVidMode.refreshRate();
   }

   public static String _getLWJGLVersion() {
      RenderSystem.assertThread(RenderSystem::isInInitPhase);
      return Version.getVersion();
   }

   public static LongSupplier _initGlfw() {
      RenderSystem.assertThread(RenderSystem::isInInitPhase);
      Window.acceptError((integer, stringx) -> {
         throw new IllegalStateException(String.format("GLFW error before init: [0x%X]%s", integer, stringx));
      });
      List<String> list = Lists.newArrayList();
      GLFWErrorCallback gLFWErrorCallback = GLFW.glfwSetErrorCallback((i, l) -> {
         list.add(String.format("GLFW error during init: [0x%X]%s", i, l));
      });
      if (!GLFW.glfwInit()) {
         throw new IllegalStateException("Failed to initialize GLFW, errors: " + Joiner.on(",").join((Iterable)list));
      } else {
         LongSupplier longSupplier2 = () -> {
            return (long)(GLFW.glfwGetTime() * 1.0E9D);
         };
         Iterator var3 = list.iterator();

         while(var3.hasNext()) {
            String string = (String)var3.next();
            LOGGER.error((String)"GLFW error collected during initialization: {}", (Object)string);
         }

         RenderSystem.setErrorCallback(gLFWErrorCallback);
         return longSupplier2;
      }
   }

   public static void _setGlfwErrorCallback(GLFWErrorCallbackI callback) {
      RenderSystem.assertThread(RenderSystem::isInInitPhase);
      GLFWErrorCallback gLFWErrorCallback = GLFW.glfwSetErrorCallback(callback);
      if (gLFWErrorCallback != null) {
         gLFWErrorCallback.free();
      }

   }

   public static boolean _shouldClose(Window window) {
      return GLFW.glfwWindowShouldClose(window.getHandle());
   }

   public static void _setupNvFogDistance() {
      RenderSystem.assertThread(RenderSystem::isOnRenderThread);
      if (GL.getCapabilities().GL_NV_fog_distance) {
         GlStateManager.fogi(34138, 34139);
      }

   }

   public static void _init(int debugVerbosity, boolean debugSync) {
      RenderSystem.assertThread(RenderSystem::isInInitPhase);
      GLCapabilities gLCapabilities = GL.getCapabilities();
      capsString = "Using framebuffer using " + GlStateManager.initFramebufferSupport(gLCapabilities);

      try {
         Processor[] processors = (new SystemInfo()).getHardware().getProcessors();
         cpuInfo = String.format("%dx %s", processors.length, processors[0]).replaceAll("\\s+", " ");
      } catch (Throwable var4) {
      }

      GlDebug.enableDebug(debugVerbosity, debugSync);
   }

   public static String _getCapsString() {
      return capsString;
   }

   public static String _getCpuInfo() {
      return cpuInfo == null ? "<unknown>" : cpuInfo;
   }

   public static void _renderCrosshair(int size, boolean drawX, boolean drawY, boolean drawZ) {
      RenderSystem.assertThread(RenderSystem::isOnRenderThread);
      GlStateManager.disableTexture();
      GlStateManager.depthMask(false);
      Tessellator tessellator = RenderSystem.renderThreadTesselator();
      BufferBuilder bufferBuilder = tessellator.getBuffer();
      GL11.glLineWidth(4.0F);
      bufferBuilder.begin(1, VertexFormats.POSITION_COLOR);
      if (drawX) {
         bufferBuilder.vertex(0.0D, 0.0D, 0.0D).color(0, 0, 0, 255).next();
         bufferBuilder.vertex((double)size, 0.0D, 0.0D).color(0, 0, 0, 255).next();
      }

      if (drawY) {
         bufferBuilder.vertex(0.0D, 0.0D, 0.0D).color(0, 0, 0, 255).next();
         bufferBuilder.vertex(0.0D, (double)size, 0.0D).color(0, 0, 0, 255).next();
      }

      if (drawZ) {
         bufferBuilder.vertex(0.0D, 0.0D, 0.0D).color(0, 0, 0, 255).next();
         bufferBuilder.vertex(0.0D, 0.0D, (double)size).color(0, 0, 0, 255).next();
      }

      tessellator.draw();
      GL11.glLineWidth(2.0F);
      bufferBuilder.begin(1, VertexFormats.POSITION_COLOR);
      if (drawX) {
         bufferBuilder.vertex(0.0D, 0.0D, 0.0D).color(255, 0, 0, 255).next();
         bufferBuilder.vertex((double)size, 0.0D, 0.0D).color(255, 0, 0, 255).next();
      }

      if (drawY) {
         bufferBuilder.vertex(0.0D, 0.0D, 0.0D).color(0, 255, 0, 255).next();
         bufferBuilder.vertex(0.0D, (double)size, 0.0D).color(0, 255, 0, 255).next();
      }

      if (drawZ) {
         bufferBuilder.vertex(0.0D, 0.0D, 0.0D).color(127, 127, 255, 255).next();
         bufferBuilder.vertex(0.0D, 0.0D, (double)size).color(127, 127, 255, 255).next();
      }

      tessellator.draw();
      GL11.glLineWidth(1.0F);
      GlStateManager.depthMask(true);
      GlStateManager.enableTexture();
   }

   public static String getErrorString(int code) {
      return (String)LOOKUP_MAP.get(code);
   }

   public static <T> T make(Supplier<T> factory) {
      return factory.get();
   }

   public static <T> T make(T object, Consumer<T> initializer) {
      initializer.accept(object);
      return object;
   }
}
