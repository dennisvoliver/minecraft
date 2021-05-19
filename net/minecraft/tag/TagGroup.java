package net.minecraft.tag;

import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.ImmutableSet.Builder;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.DefaultedRegistry;
import net.minecraft.util.registry.Registry;
import org.jetbrains.annotations.Nullable;

/**
 * Contains the set of tags all of the same type.
 */
public interface TagGroup<T> {
   Map<Identifier, Tag<T>> getTags();

   @Nullable
   default Tag<T> getTag(Identifier id) {
      return (Tag)this.getTags().get(id);
   }

   Tag<T> getTagOrEmpty(Identifier id);

   @Nullable
   Identifier getUncheckedTagId(Tag<T> tag);

   default Identifier getTagId(Tag<T> tag) {
      Identifier identifier = this.getUncheckedTagId(tag);
      if (identifier == null) {
         throw new IllegalStateException("Unrecognized tag");
      } else {
         return identifier;
      }
   }

   default Collection<Identifier> getTagIds() {
      return this.getTags().keySet();
   }

   /**
    * Gets the identifiers of all tags an object is applicable to.
    */
   @Environment(EnvType.CLIENT)
   default Collection<Identifier> getTagsFor(T object) {
      List<Identifier> list = Lists.newArrayList();
      Iterator var3 = this.getTags().entrySet().iterator();

      while(var3.hasNext()) {
         Entry<Identifier, Tag<T>> entry = (Entry)var3.next();
         if (((Tag)entry.getValue()).contains(object)) {
            list.add(entry.getKey());
         }
      }

      return list;
   }

   default void toPacket(PacketByteBuf buf, DefaultedRegistry<T> registry) {
      Map<Identifier, Tag<T>> map = this.getTags();
      buf.writeVarInt(map.size());
      Iterator var4 = map.entrySet().iterator();

      while(var4.hasNext()) {
         Entry<Identifier, Tag<T>> entry = (Entry)var4.next();
         buf.writeIdentifier((Identifier)entry.getKey());
         buf.writeVarInt(((Tag)entry.getValue()).values().size());
         Iterator var6 = ((Tag)entry.getValue()).values().iterator();

         while(var6.hasNext()) {
            T object = var6.next();
            buf.writeVarInt(registry.getRawId(object));
         }
      }

   }

   static <T> TagGroup<T> fromPacket(PacketByteBuf buf, Registry<T> registry) {
      Map<Identifier, Tag<T>> map = Maps.newHashMap();
      int i = buf.readVarInt();

      for(int j = 0; j < i; ++j) {
         Identifier identifier = buf.readIdentifier();
         int k = buf.readVarInt();
         Builder<T> builder = ImmutableSet.builder();

         for(int l = 0; l < k; ++l) {
            builder.add(registry.get(buf.readVarInt()));
         }

         map.put(identifier, Tag.of(builder.build()));
      }

      return create(map);
   }

   static <T> TagGroup<T> createEmpty() {
      return create(ImmutableBiMap.of());
   }

   static <T> TagGroup<T> create(Map<Identifier, Tag<T>> tags) {
      final BiMap<Identifier, Tag<T>> biMap = ImmutableBiMap.copyOf(tags);
      return new TagGroup<T>() {
         private final Tag<T> emptyTag = SetTag.empty();

         public Tag<T> getTagOrEmpty(Identifier id) {
            return (Tag)biMap.getOrDefault(id, this.emptyTag);
         }

         @Nullable
         public Identifier getUncheckedTagId(Tag<T> tag) {
            return tag instanceof Tag.Identified ? ((Tag.Identified)tag).getId() : (Identifier)biMap.inverse().get(tag);
         }

         public Map<Identifier, Tag<T>> getTags() {
            return biMap;
         }
      };
   }
}
