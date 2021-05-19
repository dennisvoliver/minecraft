package net.minecraft.client.gui.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import java.util.function.Consumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.BackgroundHelper;
import net.minecraft.client.resource.metadata.TextureResourceMetadata;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.ResourceTexture;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.resource.DefaultResourcePack;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceReloadMonitor;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;

@Environment(EnvType.CLIENT)
public class SplashScreen extends Overlay {
   private static final Identifier LOGO = new Identifier("textures/gui/title/mojangstudios.png");
   private static final int BRAND_ARGB = BackgroundHelper.ColorMixer.getArgb(255, 239, 50, 61);
   private static final int BRAND_RGB;
   private final MinecraftClient client;
   private final ResourceReloadMonitor reloadMonitor;
   private final Consumer<Optional<Throwable>> exceptionHandler;
   private final boolean reloading;
   private float progress;
   private long applyCompleteTime = -1L;
   private long prepareCompleteTime = -1L;

   public SplashScreen(MinecraftClient client, ResourceReloadMonitor monitor, Consumer<Optional<Throwable>> exceptionHandler, boolean reloading) {
      this.client = client;
      this.reloadMonitor = monitor;
      this.exceptionHandler = exceptionHandler;
      this.reloading = reloading;
   }

   public static void init(MinecraftClient client) {
      client.getTextureManager().registerTexture(LOGO, new SplashScreen.LogoTexture());
   }

   public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
      int i = this.client.getWindow().getScaledWidth();
      int j = this.client.getWindow().getScaledHeight();
      long l = Util.getMeasuringTimeMs();
      if (this.reloading && (this.reloadMonitor.isPrepareStageComplete() || this.client.currentScreen != null) && this.prepareCompleteTime == -1L) {
         this.prepareCompleteTime = l;
      }

      float f = this.applyCompleteTime > -1L ? (float)(l - this.applyCompleteTime) / 1000.0F : -1.0F;
      float g = this.prepareCompleteTime > -1L ? (float)(l - this.prepareCompleteTime) / 500.0F : -1.0F;
      float o;
      int m;
      if (f >= 1.0F) {
         if (this.client.currentScreen != null) {
            this.client.currentScreen.render(matrices, 0, 0, delta);
         }

         m = MathHelper.ceil((1.0F - MathHelper.clamp(f - 1.0F, 0.0F, 1.0F)) * 255.0F);
         fill(matrices, 0, 0, i, j, BRAND_RGB | m << 24);
         o = 1.0F - MathHelper.clamp(f - 1.0F, 0.0F, 1.0F);
      } else if (this.reloading) {
         if (this.client.currentScreen != null && g < 1.0F) {
            this.client.currentScreen.render(matrices, mouseX, mouseY, delta);
         }

         m = MathHelper.ceil(MathHelper.clamp((double)g, 0.15D, 1.0D) * 255.0D);
         fill(matrices, 0, 0, i, j, BRAND_RGB | m << 24);
         o = MathHelper.clamp(g, 0.0F, 1.0F);
      } else {
         fill(matrices, 0, 0, i, j, BRAND_ARGB);
         o = 1.0F;
      }

      m = (int)((double)this.client.getWindow().getScaledWidth() * 0.5D);
      int q = (int)((double)this.client.getWindow().getScaledHeight() * 0.5D);
      double d = Math.min((double)this.client.getWindow().getScaledWidth() * 0.75D, (double)this.client.getWindow().getScaledHeight()) * 0.25D;
      int r = (int)(d * 0.5D);
      double e = d * 4.0D;
      int s = (int)(e * 0.5D);
      this.client.getTextureManager().bindTexture(LOGO);
      RenderSystem.enableBlend();
      RenderSystem.blendEquation(32774);
      RenderSystem.blendFunc(770, 1);
      RenderSystem.alphaFunc(516, 0.0F);
      RenderSystem.color4f(1.0F, 1.0F, 1.0F, o);
      drawTexture(matrices, m - s, q - r, s, (int)d, -0.0625F, 0.0F, 120, 60, 120, 120);
      drawTexture(matrices, m, q - r, s, (int)d, 0.0625F, 60.0F, 120, 60, 120, 120);
      RenderSystem.defaultBlendFunc();
      RenderSystem.defaultAlphaFunc();
      RenderSystem.disableBlend();
      int t = (int)((double)this.client.getWindow().getScaledHeight() * 0.8325D);
      float u = this.reloadMonitor.getProgress();
      this.progress = MathHelper.clamp(this.progress * 0.95F + u * 0.050000012F, 0.0F, 1.0F);
      if (f < 1.0F) {
         this.renderProgressBar(matrices, i / 2 - s, t - 5, i / 2 + s, t + 5, 1.0F - MathHelper.clamp(f, 0.0F, 1.0F));
      }

      if (f >= 2.0F) {
         this.client.setOverlay((Overlay)null);
      }

      if (this.applyCompleteTime == -1L && this.reloadMonitor.isApplyStageComplete() && (!this.reloading || g >= 2.0F)) {
         try {
            this.reloadMonitor.throwExceptions();
            this.exceptionHandler.accept(Optional.empty());
         } catch (Throwable var23) {
            this.exceptionHandler.accept(Optional.of(var23));
         }

         this.applyCompleteTime = Util.getMeasuringTimeMs();
         if (this.client.currentScreen != null) {
            this.client.currentScreen.init(this.client, this.client.getWindow().getScaledWidth(), this.client.getWindow().getScaledHeight());
         }
      }

   }

   private void renderProgressBar(MatrixStack matrices, int x1, int y1, int x2, int y2, float opacity) {
      int i = MathHelper.ceil((float)(x2 - x1 - 2) * this.progress);
      int j = Math.round(opacity * 255.0F);
      int k = BackgroundHelper.ColorMixer.getArgb(j, 255, 255, 255);
      fill(matrices, x1 + 1, y1, x2 - 1, y1 + 1, k);
      fill(matrices, x1 + 1, y2, x2 - 1, y2 - 1, k);
      fill(matrices, x1, y1, x1 + 1, y2, k);
      fill(matrices, x2, y1, x2 - 1, y2, k);
      fill(matrices, x1 + 2, y1 + 2, x1 + i, y2 - 2, k);
   }

   public boolean pausesGame() {
      return true;
   }

   static {
      BRAND_RGB = BRAND_ARGB & 16777215;
   }

   @Environment(EnvType.CLIENT)
   static class LogoTexture extends ResourceTexture {
      public LogoTexture() {
         super(SplashScreen.LOGO);
      }

      protected ResourceTexture.TextureData loadTextureData(ResourceManager resourceManager) {
         MinecraftClient minecraftClient = MinecraftClient.getInstance();
         DefaultResourcePack defaultResourcePack = minecraftClient.getResourcePackDownloader().getPack();

         try {
            InputStream inputStream = defaultResourcePack.open(ResourceType.CLIENT_RESOURCES, SplashScreen.LOGO);
            Throwable var5 = null;

            ResourceTexture.TextureData var6;
            try {
               var6 = new ResourceTexture.TextureData(new TextureResourceMetadata(true, true), NativeImage.read(inputStream));
            } catch (Throwable var16) {
               var5 = var16;
               throw var16;
            } finally {
               if (inputStream != null) {
                  if (var5 != null) {
                     try {
                        inputStream.close();
                     } catch (Throwable var15) {
                        var5.addSuppressed(var15);
                     }
                  } else {
                     inputStream.close();
                  }
               }

            }

            return var6;
         } catch (IOException var18) {
            return new ResourceTexture.TextureData(var18);
         }
      }
   }
}
