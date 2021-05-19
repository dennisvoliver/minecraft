package net.minecraft.resource;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;
import net.minecraft.resource.metadata.ResourceMetadataReader;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

public class DefaultResourcePack implements ResourcePack {
   public static Path resourcePath;
   private static final Logger LOGGER = LogManager.getLogger();
   public static Class<?> resourceClass;
   private static final Map<ResourceType, FileSystem> typeToFileSystem = (Map)Util.make(Maps.newHashMap(), (hashMap) -> {
      Class var1 = DefaultResourcePack.class;
      synchronized(DefaultResourcePack.class) {
         ResourceType[] var2 = ResourceType.values();
         int var3 = var2.length;

         for(int var4 = 0; var4 < var3; ++var4) {
            ResourceType resourceType = var2[var4];
            URL uRL = DefaultResourcePack.class.getResource("/" + resourceType.getDirectory() + "/.mcassetsroot");

            try {
               URI uRI = uRL.toURI();
               if ("jar".equals(uRI.getScheme())) {
                  FileSystem fileSystem2;
                  try {
                     fileSystem2 = FileSystems.getFileSystem(uRI);
                  } catch (FileSystemNotFoundException var11) {
                     fileSystem2 = FileSystems.newFileSystem(uRI, Collections.emptyMap());
                  }

                  hashMap.put(resourceType, fileSystem2);
               }
            } catch (IOException | URISyntaxException var12) {
               LOGGER.error((String)"Couldn't get a list of all vanilla resources", (Throwable)var12);
            }
         }

      }
   });
   public final Set<String> namespaces;

   public DefaultResourcePack(String... namespaces) {
      this.namespaces = ImmutableSet.copyOf((Object[])namespaces);
   }

   public InputStream openRoot(String fileName) throws IOException {
      if (!fileName.contains("/") && !fileName.contains("\\")) {
         if (resourcePath != null) {
            Path path = resourcePath.resolve(fileName);
            if (Files.exists(path, new LinkOption[0])) {
               return Files.newInputStream(path);
            }
         }

         return this.getInputStream(fileName);
      } else {
         throw new IllegalArgumentException("Root resources can only be filenames, not paths (no / allowed!)");
      }
   }

   public InputStream open(ResourceType type, Identifier id) throws IOException {
      InputStream inputStream = this.findInputStream(type, id);
      if (inputStream != null) {
         return inputStream;
      } else {
         throw new FileNotFoundException(id.getPath());
      }
   }

   public Collection<Identifier> findResources(ResourceType type, String namespace, String prefix, int maxDepth, Predicate<String> pathFilter) {
      Set<Identifier> set = Sets.newHashSet();
      URI uRI;
      if (resourcePath != null) {
         try {
            getIdentifiers(set, maxDepth, namespace, resourcePath.resolve(type.getDirectory()), prefix, pathFilter);
         } catch (IOException var15) {
         }

         if (type == ResourceType.CLIENT_RESOURCES) {
            Enumeration enumeration = null;

            try {
               enumeration = resourceClass.getClassLoader().getResources(type.getDirectory() + "/");
            } catch (IOException var14) {
            }

            while(enumeration != null && enumeration.hasMoreElements()) {
               try {
                  uRI = ((URL)enumeration.nextElement()).toURI();
                  if ("file".equals(uRI.getScheme())) {
                     getIdentifiers(set, maxDepth, namespace, Paths.get(uRI), prefix, pathFilter);
                  }
               } catch (IOException | URISyntaxException var13) {
               }
            }
         }
      }

      try {
         URL uRL = DefaultResourcePack.class.getResource("/" + type.getDirectory() + "/.mcassetsroot");
         if (uRL == null) {
            LOGGER.error("Couldn't find .mcassetsroot, cannot load vanilla resources");
            return set;
         }

         uRI = uRL.toURI();
         if ("file".equals(uRI.getScheme())) {
            URL uRL2 = new URL(uRL.toString().substring(0, uRL.toString().length() - ".mcassetsroot".length()));
            Path path = Paths.get(uRL2.toURI());
            getIdentifiers(set, maxDepth, namespace, path, prefix, pathFilter);
         } else if ("jar".equals(uRI.getScheme())) {
            Path path2 = ((FileSystem)typeToFileSystem.get(type)).getPath("/" + type.getDirectory());
            getIdentifiers(set, maxDepth, "minecraft", path2, prefix, pathFilter);
         } else {
            LOGGER.error((String)"Unsupported scheme {} trying to list vanilla resources (NYI?)", (Object)uRI);
         }
      } catch (NoSuchFileException | FileNotFoundException var11) {
      } catch (IOException | URISyntaxException var12) {
         LOGGER.error((String)"Couldn't get a list of all vanilla resources", (Throwable)var12);
      }

      return set;
   }

   private static void getIdentifiers(Collection<Identifier> collection, int maxDepth, String namespace, Path path, String searchLocation, Predicate<String> predicate) throws IOException {
      Path path2 = path.resolve(namespace);
      Stream<Path> stream = Files.walk(path2.resolve(searchLocation), maxDepth, new FileVisitOption[0]);
      Throwable var8 = null;

      try {
         stream.filter((pathx) -> {
            return !pathx.endsWith(".mcmeta") && Files.isRegularFile(pathx, new LinkOption[0]) && predicate.test(pathx.getFileName().toString());
         }).map((path2x) -> {
            return new Identifier(namespace, path2.relativize(path2x).toString().replaceAll("\\\\", "/"));
         }).forEach(collection::add);
      } catch (Throwable var17) {
         var8 = var17;
         throw var17;
      } finally {
         if (stream != null) {
            if (var8 != null) {
               try {
                  stream.close();
               } catch (Throwable var16) {
                  var8.addSuppressed(var16);
               }
            } else {
               stream.close();
            }
         }

      }

   }

   @Nullable
   protected InputStream findInputStream(ResourceType type, Identifier id) {
      String string = getPath(type, id);
      if (resourcePath != null) {
         Path path = resourcePath.resolve(type.getDirectory() + "/" + id.getNamespace() + "/" + id.getPath());
         if (Files.exists(path, new LinkOption[0])) {
            try {
               return Files.newInputStream(path);
            } catch (IOException var7) {
            }
         }
      }

      try {
         URL uRL = DefaultResourcePack.class.getResource(string);
         return isValidUrl(string, uRL) ? uRL.openStream() : null;
      } catch (IOException var6) {
         return DefaultResourcePack.class.getResourceAsStream(string);
      }
   }

   private static String getPath(ResourceType type, Identifier id) {
      return "/" + type.getDirectory() + "/" + id.getNamespace() + "/" + id.getPath();
   }

   private static boolean isValidUrl(String fileName, @Nullable URL url) throws IOException {
      return url != null && (url.getProtocol().equals("jar") || DirectoryResourcePack.isValidPath(new File(url.getFile()), fileName));
   }

   @Nullable
   protected InputStream getInputStream(String path) {
      return DefaultResourcePack.class.getResourceAsStream("/" + path);
   }

   public boolean contains(ResourceType type, Identifier id) {
      String string = getPath(type, id);
      if (resourcePath != null) {
         Path path = resourcePath.resolve(type.getDirectory() + "/" + id.getNamespace() + "/" + id.getPath());
         if (Files.exists(path, new LinkOption[0])) {
            return true;
         }
      }

      try {
         URL uRL = DefaultResourcePack.class.getResource(string);
         return isValidUrl(string, uRL);
      } catch (IOException var5) {
         return false;
      }
   }

   public Set<String> getNamespaces(ResourceType type) {
      return this.namespaces;
   }

   @Nullable
   public <T> T parseMetadata(ResourceMetadataReader<T> metaReader) throws IOException {
      try {
         InputStream inputStream = this.openRoot("pack.mcmeta");
         Throwable var3 = null;

         Object var4;
         try {
            var4 = AbstractFileResourcePack.parseMetadata(metaReader, inputStream);
         } catch (Throwable var14) {
            var3 = var14;
            throw var14;
         } finally {
            if (inputStream != null) {
               if (var3 != null) {
                  try {
                     inputStream.close();
                  } catch (Throwable var13) {
                     var3.addSuppressed(var13);
                  }
               } else {
                  inputStream.close();
               }
            }

         }

         return var4;
      } catch (FileNotFoundException | RuntimeException var16) {
         return null;
      }
   }

   public String getName() {
      return "Default";
   }

   public void close() {
   }
}
