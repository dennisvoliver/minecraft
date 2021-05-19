package net.minecraft.tag;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;

/**
 * A tag is a set of objects.
 * 
 * <p>Tags simplifies reference to multiple objects, especially for
 * predicate (testing against) purposes.
 * 
 * <p>A tag is immutable by design. It has a builder, which is a mutable
 * equivalent.
 * 
 * <p>Its entries' iteration may be ordered
 * or unordered, depending on the configuration from the tag builder.
 */
public interface Tag<T> {
   static <T> Codec<Tag<T>> codec(Supplier<TagGroup<T>> groupGetter) {
      return Identifier.CODEC.flatXmap((id) -> {
         return (DataResult)Optional.ofNullable(((TagGroup)groupGetter.get()).getTag(id)).map(DataResult::success).orElseGet(() -> {
            return DataResult.error("Unknown tag: " + id);
         });
      }, (tag) -> {
         return (DataResult)Optional.ofNullable(((TagGroup)groupGetter.get()).getUncheckedTagId(tag)).map(DataResult::success).orElseGet(() -> {
            return DataResult.error("Unknown tag: " + tag);
         });
      });
   }

   boolean contains(T entry);

   List<T> values();

   default T getRandom(Random random) {
      List<T> list = this.values();
      return list.get(random.nextInt(list.size()));
   }

   static <T> Tag<T> of(Set<T> values) {
      return SetTag.of(values);
   }

   public interface Identified<T> extends Tag<T> {
      Identifier getId();
   }

   public static class OptionalTagEntry implements Tag.Entry {
      private final Identifier id;

      public OptionalTagEntry(Identifier id) {
         this.id = id;
      }

      public <T> boolean resolve(Function<Identifier, Tag<T>> tagGetter, Function<Identifier, T> objectGetter, Consumer<T> collector) {
         Tag<T> tag = (Tag)tagGetter.apply(this.id);
         if (tag != null) {
            tag.values().forEach(collector);
         }

         return true;
      }

      public void addToJson(JsonArray json) {
         JsonObject jsonObject = new JsonObject();
         jsonObject.addProperty("id", "#" + this.id);
         jsonObject.addProperty("required", false);
         json.add((JsonElement)jsonObject);
      }

      public String toString() {
         return "#" + this.id + "?";
      }
   }

   public static class TagEntry implements Tag.Entry {
      private final Identifier id;

      public TagEntry(Identifier id) {
         this.id = id;
      }

      public <T> boolean resolve(Function<Identifier, Tag<T>> tagGetter, Function<Identifier, T> objectGetter, Consumer<T> collector) {
         Tag<T> tag = (Tag)tagGetter.apply(this.id);
         if (tag == null) {
            return false;
         } else {
            tag.values().forEach(collector);
            return true;
         }
      }

      public void addToJson(JsonArray json) {
         json.add("#" + this.id);
      }

      public String toString() {
         return "#" + this.id;
      }
   }

   public static class OptionalObjectEntry implements Tag.Entry {
      private final Identifier id;

      public OptionalObjectEntry(Identifier id) {
         this.id = id;
      }

      public <T> boolean resolve(Function<Identifier, Tag<T>> tagGetter, Function<Identifier, T> objectGetter, Consumer<T> collector) {
         T object = objectGetter.apply(this.id);
         if (object != null) {
            collector.accept(object);
         }

         return true;
      }

      public void addToJson(JsonArray json) {
         JsonObject jsonObject = new JsonObject();
         jsonObject.addProperty("id", this.id.toString());
         jsonObject.addProperty("required", false);
         json.add((JsonElement)jsonObject);
      }

      public String toString() {
         return this.id.toString() + "?";
      }
   }

   public static class ObjectEntry implements Tag.Entry {
      private final Identifier id;

      public ObjectEntry(Identifier id) {
         this.id = id;
      }

      public <T> boolean resolve(Function<Identifier, Tag<T>> tagGetter, Function<Identifier, T> objectGetter, Consumer<T> collector) {
         T object = objectGetter.apply(this.id);
         if (object == null) {
            return false;
         } else {
            collector.accept(object);
            return true;
         }
      }

      public void addToJson(JsonArray json) {
         json.add(this.id.toString());
      }

      public String toString() {
         return this.id.toString();
      }
   }

   public interface Entry {
      <T> boolean resolve(Function<Identifier, Tag<T>> tagGetter, Function<Identifier, T> objectGetter, Consumer<T> collector);

      void addToJson(JsonArray json);
   }

   /**
    * A builder class to ease the creation of tags. It can also be used as a
    * mutable form of a tag.
    */
   public static class Builder {
      private final List<Tag.TrackedEntry> entries = Lists.newArrayList();

      public static Tag.Builder create() {
         return new Tag.Builder();
      }

      public Tag.Builder add(Tag.TrackedEntry trackedEntry) {
         this.entries.add(trackedEntry);
         return this;
      }

      public Tag.Builder add(Tag.Entry entry, String source) {
         return this.add(new Tag.TrackedEntry(entry, source));
      }

      public Tag.Builder add(Identifier id, String source) {
         return this.add((Tag.Entry)(new Tag.ObjectEntry(id)), source);
      }

      public Tag.Builder addTag(Identifier id, String source) {
         return this.add((Tag.Entry)(new Tag.TagEntry(id)), source);
      }

      public <T> Optional<Tag<T>> build(Function<Identifier, Tag<T>> tagGetter, Function<Identifier, T> objectGetter) {
         com.google.common.collect.ImmutableSet.Builder<T> builder = ImmutableSet.builder();
         Iterator var4 = this.entries.iterator();

         Tag.Entry var10000;
         do {
            if (!var4.hasNext()) {
               return Optional.of(Tag.of(builder.build()));
            }

            Tag.TrackedEntry trackedEntry = (Tag.TrackedEntry)var4.next();
            var10000 = trackedEntry.getEntry();
            builder.getClass();
         } while(var10000.resolve(tagGetter, objectGetter, builder::add));

         return Optional.empty();
      }

      public Stream<Tag.TrackedEntry> streamEntries() {
         return this.entries.stream();
      }

      public <T> Stream<Tag.TrackedEntry> streamUnresolvedEntries(Function<Identifier, Tag<T>> tagGetter, Function<Identifier, T> objectGetter) {
         return this.streamEntries().filter((trackedEntry) -> {
            return !trackedEntry.getEntry().resolve(tagGetter, objectGetter, (object) -> {
            });
         });
      }

      public Tag.Builder read(JsonObject json, String source) {
         JsonArray jsonArray = JsonHelper.getArray(json, "values");
         List<Tag.Entry> list = Lists.newArrayList();
         Iterator var5 = jsonArray.iterator();

         while(var5.hasNext()) {
            JsonElement jsonElement = (JsonElement)var5.next();
            list.add(resolveEntry(jsonElement));
         }

         if (JsonHelper.getBoolean(json, "replace", false)) {
            this.entries.clear();
         }

         list.forEach((entry) -> {
            this.entries.add(new Tag.TrackedEntry(entry, source));
         });
         return this;
      }

      private static Tag.Entry resolveEntry(JsonElement json) {
         String string2;
         boolean bl2;
         if (json.isJsonObject()) {
            JsonObject jsonObject = json.getAsJsonObject();
            string2 = JsonHelper.getString(jsonObject, "id");
            bl2 = JsonHelper.getBoolean(jsonObject, "required", true);
         } else {
            string2 = JsonHelper.asString(json, "id");
            bl2 = true;
         }

         Identifier identifier2;
         if (string2.startsWith("#")) {
            identifier2 = new Identifier(string2.substring(1));
            return (Tag.Entry)(bl2 ? new Tag.TagEntry(identifier2) : new Tag.OptionalTagEntry(identifier2));
         } else {
            identifier2 = new Identifier(string2);
            return (Tag.Entry)(bl2 ? new Tag.ObjectEntry(identifier2) : new Tag.OptionalObjectEntry(identifier2));
         }
      }

      public JsonObject toJson() {
         JsonObject jsonObject = new JsonObject();
         JsonArray jsonArray = new JsonArray();
         Iterator var3 = this.entries.iterator();

         while(var3.hasNext()) {
            Tag.TrackedEntry trackedEntry = (Tag.TrackedEntry)var3.next();
            trackedEntry.getEntry().addToJson(jsonArray);
         }

         jsonObject.addProperty("replace", false);
         jsonObject.add("values", jsonArray);
         return jsonObject;
      }
   }

   public static class TrackedEntry {
      private final Tag.Entry entry;
      private final String source;

      private TrackedEntry(Tag.Entry entry, String source) {
         this.entry = entry;
         this.source = source;
      }

      public Tag.Entry getEntry() {
         return this.entry;
      }

      public String toString() {
         return this.entry.toString() + " (from " + this.source + ")";
      }
   }
}
