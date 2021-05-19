package net.minecraft.resource;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.Identifier;

public interface ResourceManager {
   @Environment(EnvType.CLIENT)
   Set<String> getAllNamespaces();

   Resource getResource(Identifier id) throws IOException;

   @Environment(EnvType.CLIENT)
   boolean containsResource(Identifier id);

   List<Resource> getAllResources(Identifier id) throws IOException;

   Collection<Identifier> findResources(String resourceType, Predicate<String> pathPredicate);

   @Environment(EnvType.CLIENT)
   Stream<ResourcePack> streamResourcePacks();

   public static enum Empty implements ResourceManager {
      INSTANCE;

      @Environment(EnvType.CLIENT)
      public Set<String> getAllNamespaces() {
         return ImmutableSet.of();
      }

      public Resource getResource(Identifier id) throws IOException {
         throw new FileNotFoundException(id.toString());
      }

      @Environment(EnvType.CLIENT)
      public boolean containsResource(Identifier id) {
         return false;
      }

      public List<Resource> getAllResources(Identifier id) {
         return ImmutableList.of();
      }

      public Collection<Identifier> findResources(String resourceType, Predicate<String> pathPredicate) {
         return ImmutableSet.of();
      }

      @Environment(EnvType.CLIENT)
      public Stream<ResourcePack> streamResourcePacks() {
         return Stream.of();
      }
   }
}
