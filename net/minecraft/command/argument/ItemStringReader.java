package net.minecraft.command.argument;

import com.google.common.collect.Maps;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import net.minecraft.command.CommandSource;
import net.minecraft.item.Item;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.StringNbtReader;
import net.minecraft.state.property.Property;
import net.minecraft.tag.TagGroup;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.jetbrains.annotations.Nullable;

public class ItemStringReader {
   public static final SimpleCommandExceptionType TAG_DISALLOWED_EXCEPTION = new SimpleCommandExceptionType(new TranslatableText("argument.item.tag.disallowed"));
   public static final DynamicCommandExceptionType ID_INVALID_EXCEPTION = new DynamicCommandExceptionType((object) -> {
      return new TranslatableText("argument.item.id.invalid", new Object[]{object});
   });
   private static final BiFunction<SuggestionsBuilder, TagGroup<Item>, CompletableFuture<Suggestions>> NBT_SUGGESTION_PROVIDER = (suggestionsBuilder, tagGroup) -> {
      return suggestionsBuilder.buildFuture();
   };
   private final StringReader reader;
   private final boolean allowTag;
   private final Map<Property<?>, Comparable<?>> field_10801 = Maps.newHashMap();
   private Item item;
   @Nullable
   private CompoundTag tag;
   private Identifier id = new Identifier("");
   private int cursor;
   private BiFunction<SuggestionsBuilder, TagGroup<Item>, CompletableFuture<Suggestions>> suggestions;

   public ItemStringReader(StringReader reader, boolean allowTag) {
      this.suggestions = NBT_SUGGESTION_PROVIDER;
      this.reader = reader;
      this.allowTag = allowTag;
   }

   public Item getItem() {
      return this.item;
   }

   @Nullable
   public CompoundTag getTag() {
      return this.tag;
   }

   public Identifier getId() {
      return this.id;
   }

   public void readItem() throws CommandSyntaxException {
      int i = this.reader.getCursor();
      Identifier identifier = Identifier.fromCommandInput(this.reader);
      this.item = (Item)Registry.ITEM.getOrEmpty(identifier).orElseThrow(() -> {
         this.reader.setCursor(i);
         return ID_INVALID_EXCEPTION.createWithContext(this.reader, identifier.toString());
      });
   }

   public void readTag() throws CommandSyntaxException {
      if (!this.allowTag) {
         throw TAG_DISALLOWED_EXCEPTION.create();
      } else {
         this.suggestions = this::suggestTag;
         this.reader.expect('#');
         this.cursor = this.reader.getCursor();
         this.id = Identifier.fromCommandInput(this.reader);
      }
   }

   public void readNbt() throws CommandSyntaxException {
      this.tag = (new StringNbtReader(this.reader)).parseCompoundTag();
   }

   public ItemStringReader consume() throws CommandSyntaxException {
      this.suggestions = this::suggestAny;
      if (this.reader.canRead() && this.reader.peek() == '#') {
         this.readTag();
      } else {
         this.readItem();
         this.suggestions = this::suggestItem;
      }

      if (this.reader.canRead() && this.reader.peek() == '{') {
         this.suggestions = NBT_SUGGESTION_PROVIDER;
         this.readNbt();
      }

      return this;
   }

   private CompletableFuture<Suggestions> suggestItem(SuggestionsBuilder suggestionsBuilder, TagGroup<Item> tagGroup) {
      if (suggestionsBuilder.getRemaining().isEmpty()) {
         suggestionsBuilder.suggest(String.valueOf('{'));
      }

      return suggestionsBuilder.buildFuture();
   }

   private CompletableFuture<Suggestions> suggestTag(SuggestionsBuilder suggestionsBuilder, TagGroup<Item> tagGroup) {
      return CommandSource.suggestIdentifiers((Iterable)tagGroup.getTagIds(), suggestionsBuilder.createOffset(this.cursor));
   }

   private CompletableFuture<Suggestions> suggestAny(SuggestionsBuilder suggestionsBuilder, TagGroup<Item> tagGroup) {
      if (this.allowTag) {
         CommandSource.suggestIdentifiers(tagGroup.getTagIds(), suggestionsBuilder, String.valueOf('#'));
      }

      return CommandSource.suggestIdentifiers((Iterable)Registry.ITEM.getIds(), suggestionsBuilder);
   }

   public CompletableFuture<Suggestions> getSuggestions(SuggestionsBuilder builder, TagGroup<Item> tagGroup) {
      return (CompletableFuture)this.suggestions.apply(builder.createOffset(this.reader.getCursor()), tagGroup);
   }
}
