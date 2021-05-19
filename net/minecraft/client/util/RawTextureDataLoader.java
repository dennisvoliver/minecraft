package net.minecraft.client.util;

import java.io.IOException;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public class RawTextureDataLoader {
   @Deprecated
   public static int[] loadRawTextureData(ResourceManager resourceManager, Identifier identifier) throws IOException {
      Resource resource = resourceManager.getResource(identifier);
      Throwable var3 = null;

      Object var6;
      try {
         NativeImage nativeImage = NativeImage.read(resource.getInputStream());
         Throwable var5 = null;

         try {
            var6 = nativeImage.makePixelArray();
         } catch (Throwable var29) {
            var6 = var29;
            var5 = var29;
            throw var29;
         } finally {
            if (nativeImage != null) {
               if (var5 != null) {
                  try {
                     nativeImage.close();
                  } catch (Throwable var28) {
                     var5.addSuppressed(var28);
                  }
               } else {
                  nativeImage.close();
               }
            }

         }
      } catch (Throwable var31) {
         var3 = var31;
         throw var31;
      } finally {
         if (resource != null) {
            if (var3 != null) {
               try {
                  resource.close();
               } catch (Throwable var27) {
                  var3.addSuppressed(var27);
               }
            } else {
               resource.close();
            }
         }

      }

      return (int[])var6;
   }
}
