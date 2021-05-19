package net.minecraft.resource;

import com.google.gson.JsonObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resource.metadata.ResourceMetadataReader;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.Nullable;

public class ResourceImpl implements Resource {
   private final String packName;
   private final Identifier id;
   private final InputStream inputStream;
   private final InputStream metaInputStream;
   @Environment(EnvType.CLIENT)
   private boolean readMetadata;
   @Environment(EnvType.CLIENT)
   private JsonObject metadata;

   public ResourceImpl(String packName, Identifier id, InputStream inputStream, @Nullable InputStream metaInputStream) {
      this.packName = packName;
      this.id = id;
      this.inputStream = inputStream;
      this.metaInputStream = metaInputStream;
   }

   @Environment(EnvType.CLIENT)
   public Identifier getId() {
      return this.id;
   }

   public InputStream getInputStream() {
      return this.inputStream;
   }

   @Environment(EnvType.CLIENT)
   public boolean hasMetadata() {
      return this.metaInputStream != null;
   }

   @Nullable
   @Environment(EnvType.CLIENT)
   public <T> T getMetadata(ResourceMetadataReader<T> metaReader) {
      if (!this.hasMetadata()) {
         return null;
      } else {
         if (this.metadata == null && !this.readMetadata) {
            this.readMetadata = true;
            BufferedReader bufferedReader = null;

            try {
               bufferedReader = new BufferedReader(new InputStreamReader(this.metaInputStream, StandardCharsets.UTF_8));
               this.metadata = JsonHelper.deserialize((Reader)bufferedReader);
            } finally {
               IOUtils.closeQuietly((Reader)bufferedReader);
            }
         }

         if (this.metadata == null) {
            return null;
         } else {
            String string = metaReader.getKey();
            return this.metadata.has(string) ? metaReader.fromJson(JsonHelper.getObject(this.metadata, string)) : null;
         }
      }
   }

   public String getResourcePackName() {
      return this.packName;
   }

   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (!(o instanceof ResourceImpl)) {
         return false;
      } else {
         ResourceImpl resourceImpl = (ResourceImpl)o;
         if (this.id != null) {
            if (!this.id.equals(resourceImpl.id)) {
               return false;
            }
         } else if (resourceImpl.id != null) {
            return false;
         }

         if (this.packName != null) {
            if (!this.packName.equals(resourceImpl.packName)) {
               return false;
            }
         } else if (resourceImpl.packName != null) {
            return false;
         }

         return true;
      }
   }

   public int hashCode() {
      int i = this.packName != null ? this.packName.hashCode() : 0;
      i = 31 * i + (this.id != null ? this.id.hashCode() : 0);
      return i;
   }

   public void close() throws IOException {
      this.inputStream.close();
      if (this.metaInputStream != null) {
         this.metaInputStream.close();
      }

   }
}
