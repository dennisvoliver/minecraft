package net.minecraft.resource;

import java.util.function.Consumer;

public class VanillaDataPackProvider implements ResourcePackProvider {
   private final DefaultResourcePack pack = new DefaultResourcePack(new String[]{"minecraft"});

   public void register(Consumer<ResourcePackProfile> consumer, ResourcePackProfile.Factory factory) {
      ResourcePackProfile resourcePackProfile = ResourcePackProfile.of("vanilla", false, () -> {
         return this.pack;
      }, factory, ResourcePackProfile.InsertionPosition.BOTTOM, ResourcePackSource.PACK_SOURCE_BUILTIN);
      if (resourcePackProfile != null) {
         consumer.accept(resourcePackProfile);
      }

   }
}
