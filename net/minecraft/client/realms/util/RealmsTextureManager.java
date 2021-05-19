package net.minecraft.client.realms.util;

import com.google.common.collect.Maps;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.authlib.minecraft.MinecraftProfileTexture.Type;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.util.UUIDTypeAdapter;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.util.Map;
import java.util.UUID;
import javax.imageio.ImageIO;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.TextureUtil;
import net.minecraft.client.util.DefaultSkinHelper;
import net.minecraft.util.Identifier;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class RealmsTextureManager {
   private static final Map<String, RealmsTextureManager.RealmsTexture> textures = Maps.newHashMap();
   private static final Map<String, Boolean> skinFetchStatus = Maps.newHashMap();
   private static final Map<String, String> fetchedSkins = Maps.newHashMap();
   private static final Logger LOGGER = LogManager.getLogger();
   private static final Identifier field_22730 = new Identifier("textures/gui/presets/isles.png");

   public static void bindWorldTemplate(String id, @Nullable String image) {
      if (image == null) {
         MinecraftClient.getInstance().getTextureManager().bindTexture(field_22730);
      } else {
         int i = getTextureId(id, image);
         RenderSystem.bindTexture(i);
      }
   }

   public static void withBoundFace(String uuid, Runnable r) {
      RenderSystem.pushTextureAttributes();

      try {
         bindFace(uuid);
         r.run();
      } finally {
         RenderSystem.popAttributes();
      }

   }

   private static void bindDefaultFace(UUID uuid) {
      MinecraftClient.getInstance().getTextureManager().bindTexture(DefaultSkinHelper.getTexture(uuid));
   }

   private static void bindFace(final String uuid) {
      UUID uUID = UUIDTypeAdapter.fromString(uuid);
      if (textures.containsKey(uuid)) {
         RenderSystem.bindTexture(((RealmsTextureManager.RealmsTexture)textures.get(uuid)).textureId);
      } else if (skinFetchStatus.containsKey(uuid)) {
         if (!(Boolean)skinFetchStatus.get(uuid)) {
            bindDefaultFace(uUID);
         } else if (fetchedSkins.containsKey(uuid)) {
            int i = getTextureId(uuid, (String)fetchedSkins.get(uuid));
            RenderSystem.bindTexture(i);
         } else {
            bindDefaultFace(uUID);
         }

      } else {
         skinFetchStatus.put(uuid, false);
         bindDefaultFace(uUID);
         Thread thread = new Thread("Realms Texture Downloader") {
            public void run() {
               Map<Type, MinecraftProfileTexture> map = RealmsUtil.getTextures(uuid);
               if (map.containsKey(Type.SKIN)) {
                  MinecraftProfileTexture minecraftProfileTexture = (MinecraftProfileTexture)map.get(Type.SKIN);
                  String string = minecraftProfileTexture.getUrl();
                  HttpURLConnection httpURLConnection = null;
                  RealmsTextureManager.LOGGER.debug((String)"Downloading http texture from {}", (Object)string);

                  try {
                     httpURLConnection = (HttpURLConnection)(new URL(string)).openConnection(MinecraftClient.getInstance().getNetworkProxy());
                     httpURLConnection.setDoInput(true);
                     httpURLConnection.setDoOutput(false);
                     httpURLConnection.connect();
                     if (httpURLConnection.getResponseCode() / 100 == 2) {
                        BufferedImage bufferedImage2;
                        try {
                           bufferedImage2 = ImageIO.read(httpURLConnection.getInputStream());
                        } catch (Exception var17) {
                           RealmsTextureManager.skinFetchStatus.remove(uuid);
                           return;
                        } finally {
                           IOUtils.closeQuietly(httpURLConnection.getInputStream());
                        }

                        bufferedImage2 = (new SkinProcessor()).process(bufferedImage2);
                        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                        ImageIO.write(bufferedImage2, "png", byteArrayOutputStream);
                        RealmsTextureManager.fetchedSkins.put(uuid, (new Base64()).encodeToString(byteArrayOutputStream.toByteArray()));
                        RealmsTextureManager.skinFetchStatus.put(uuid, true);
                        return;
                     }

                     RealmsTextureManager.skinFetchStatus.remove(uuid);
                  } catch (Exception var19) {
                     RealmsTextureManager.LOGGER.error((String)"Couldn't download http texture", (Throwable)var19);
                     RealmsTextureManager.skinFetchStatus.remove(uuid);
                     return;
                  } finally {
                     if (httpURLConnection != null) {
                        httpURLConnection.disconnect();
                     }

                  }

               } else {
                  RealmsTextureManager.skinFetchStatus.put(uuid, true);
               }
            }
         };
         thread.setDaemon(true);
         thread.start();
      }
   }

   private static int getTextureId(String id, String image) {
      int j;
      if (textures.containsKey(id)) {
         RealmsTextureManager.RealmsTexture realmsTexture = (RealmsTextureManager.RealmsTexture)textures.get(id);
         if (realmsTexture.image.equals(image)) {
            return realmsTexture.textureId;
         }

         RenderSystem.deleteTexture(realmsTexture.textureId);
         j = realmsTexture.textureId;
      } else {
         j = GlStateManager.genTextures();
      }

      IntBuffer intBuffer = null;
      int k = 0;
      int l = 0;

      try {
         ByteArrayInputStream inputStream = new ByteArrayInputStream((new Base64()).decode(image));

         BufferedImage bufferedImage2;
         try {
            bufferedImage2 = ImageIO.read(inputStream);
         } finally {
            IOUtils.closeQuietly((InputStream)inputStream);
         }

         k = bufferedImage2.getWidth();
         l = bufferedImage2.getHeight();
         int[] is = new int[k * l];
         bufferedImage2.getRGB(0, 0, k, l, is, 0, k);
         intBuffer = ByteBuffer.allocateDirect(4 * k * l).order(ByteOrder.nativeOrder()).asIntBuffer();
         intBuffer.put(is);
         intBuffer.flip();
      } catch (IOException var12) {
         var12.printStackTrace();
      }

      RenderSystem.activeTexture(33984);
      RenderSystem.bindTexture(j);
      TextureUtil.uploadImage(intBuffer, k, l);
      textures.put(id, new RealmsTextureManager.RealmsTexture(image, j));
      return j;
   }

   @Environment(EnvType.CLIENT)
   public static class RealmsTexture {
      private final String image;
      private final int textureId;

      public RealmsTexture(String image, int textureId) {
         this.image = image;
         this.textureId = textureId;
      }
   }
}
