package net.minecraft.resource;

import java.io.Closeable;
import java.io.InputStream;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resource.metadata.ResourceMetadataReader;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

public interface Resource extends Closeable {
   @Environment(EnvType.CLIENT)
   Identifier getId();

   InputStream getInputStream();

   @Nullable
   @Environment(EnvType.CLIENT)
   <T> T getMetadata(ResourceMetadataReader<T> metaReader);

   String getResourcePackName();
}
