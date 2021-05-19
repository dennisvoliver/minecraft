package net.minecraft.tag;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * A class containing the single static instance of {@link TagManager} on the server.
 */
public class ServerTagManagerHolder {
   private static volatile TagManager tagManager = TagManager.create(TagGroup.create((Map)BlockTags.getRequiredTags().stream().collect(Collectors.toMap(Tag.Identified::getId, (identified) -> {
      return identified;
   }))), TagGroup.create((Map)ItemTags.getRequiredTags().stream().collect(Collectors.toMap(Tag.Identified::getId, (identified) -> {
      return identified;
   }))), TagGroup.create((Map)FluidTags.getRequiredTags().stream().collect(Collectors.toMap(Tag.Identified::getId, (identified) -> {
      return identified;
   }))), TagGroup.create((Map)EntityTypeTags.getRequiredTags().stream().collect(Collectors.toMap(Tag.Identified::getId, (identified) -> {
      return identified;
   }))));

   public static TagManager getTagManager() {
      return tagManager;
   }

   public static void setTagManager(TagManager tagManager) {
      ServerTagManagerHolder.tagManager = tagManager;
   }
}
