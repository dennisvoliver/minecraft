package net.minecraft.command.argument;

import com.google.common.collect.Maps;
import com.google.common.collect.UnmodifiableIterator;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.exceptions.Dynamic3CommandExceptionType;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.command.CommandSource;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.StringNbtReader;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.Property;
import net.minecraft.tag.Tag;
import net.minecraft.tag.TagGroup;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.jetbrains.annotations.Nullable;

public class BlockArgumentParser {
   public static final SimpleCommandExceptionType DISALLOWED_TAG_EXCEPTION = new SimpleCommandExceptionType(new TranslatableText("argument.block.tag.disallowed"));
   public static final DynamicCommandExceptionType INVALID_BLOCK_ID_EXCEPTION = new DynamicCommandExceptionType((object) -> {
      return new TranslatableText("argument.block.id.invalid", new Object[]{object});
   });
   public static final Dynamic2CommandExceptionType UNKNOWN_PROPERTY_EXCEPTION = new Dynamic2CommandExceptionType((object, object2) -> {
      return new TranslatableText("argument.block.property.unknown", new Object[]{object, object2});
   });
   public static final Dynamic2CommandExceptionType DUPLICATE_PROPERTY_EXCEPTION = new Dynamic2CommandExceptionType((object, object2) -> {
      return new TranslatableText("argument.block.property.duplicate", new Object[]{object2, object});
   });
   public static final Dynamic3CommandExceptionType INVALID_PROPERTY_EXCEPTION = new Dynamic3CommandExceptionType((object, object2, object3) -> {
      return new TranslatableText("argument.block.property.invalid", new Object[]{object, object3, object2});
   });
   public static final Dynamic2CommandExceptionType EMPTY_PROPERTY_EXCEPTION = new Dynamic2CommandExceptionType((object, object2) -> {
      return new TranslatableText("argument.block.property.novalue", new Object[]{object, object2});
   });
   public static final SimpleCommandExceptionType UNCLOSED_PROPERTIES_EXCEPTION = new SimpleCommandExceptionType(new TranslatableText("argument.block.property.unclosed"));
   private static final BiFunction<SuggestionsBuilder, TagGroup<Block>, CompletableFuture<Suggestions>> SUGGEST_DEFAULT = (suggestionsBuilder, tagGroup) -> {
      return suggestionsBuilder.buildFuture();
   };
   private final StringReader reader;
   private final boolean allowTag;
   private final Map<Property<?>, Comparable<?>> blockProperties = Maps.newHashMap();
   private final Map<String, String> tagProperties = Maps.newHashMap();
   private Identifier blockId = new Identifier("");
   private StateManager<Block, BlockState> stateFactory;
   private BlockState blockState;
   @Nullable
   private CompoundTag data;
   private Identifier tagId = new Identifier("");
   private int cursorPos;
   private BiFunction<SuggestionsBuilder, TagGroup<Block>, CompletableFuture<Suggestions>> suggestions;

   public BlockArgumentParser(StringReader reader, boolean allowTag) {
      this.suggestions = SUGGEST_DEFAULT;
      this.reader = reader;
      this.allowTag = allowTag;
   }

   public Map<Property<?>, Comparable<?>> getBlockProperties() {
      return this.blockProperties;
   }

   @Nullable
   public BlockState getBlockState() {
      return this.blockState;
   }

   @Nullable
   public CompoundTag getNbtData() {
      return this.data;
   }

   @Nullable
   public Identifier getTagId() {
      return this.tagId;
   }

   public BlockArgumentParser parse(boolean allowNbt) throws CommandSyntaxException {
      this.suggestions = this::suggestBlockOrTagId;
      if (this.reader.canRead() && this.reader.peek() == '#') {
         this.parseTagId();
         this.suggestions = this::suggestSnbtOrTagProperties;
         if (this.reader.canRead() && this.reader.peek() == '[') {
            this.parseTagProperties();
            this.suggestions = this::suggestSnbt;
         }
      } else {
         this.parseBlockId();
         this.suggestions = this::suggestSnbtOrBlockProperties;
         if (this.reader.canRead() && this.reader.peek() == '[') {
            this.parseBlockProperties();
            this.suggestions = this::suggestSnbt;
         }
      }

      if (allowNbt && this.reader.canRead() && this.reader.peek() == '{') {
         this.suggestions = SUGGEST_DEFAULT;
         this.parseSnbt();
      }

      return this;
   }

   private CompletableFuture<Suggestions> suggestBlockPropertiesOrEnd(SuggestionsBuilder suggestionsBuilder, TagGroup<Block> tagGroup) {
      if (suggestionsBuilder.getRemaining().isEmpty()) {
         suggestionsBuilder.suggest(String.valueOf(']'));
      }

      return this.suggestBlockProperties(suggestionsBuilder, tagGroup);
   }

   private CompletableFuture<Suggestions> suggestTagPropertiesOrEnd(SuggestionsBuilder suggestionsBuilder, TagGroup<Block> tagGroup) {
      if (suggestionsBuilder.getRemaining().isEmpty()) {
         suggestionsBuilder.suggest(String.valueOf(']'));
      }

      return this.suggestTagProperties(suggestionsBuilder, tagGroup);
   }

   private CompletableFuture<Suggestions> suggestBlockProperties(SuggestionsBuilder suggestionsBuilder, TagGroup<Block> tagGroup) {
      String string = suggestionsBuilder.getRemaining().toLowerCase(Locale.ROOT);
      Iterator var4 = this.blockState.getProperties().iterator();

      while(var4.hasNext()) {
         Property<?> property = (Property)var4.next();
         if (!this.blockProperties.containsKey(property) && property.getName().startsWith(string)) {
            suggestionsBuilder.suggest(property.getName() + '=');
         }
      }

      return suggestionsBuilder.buildFuture();
   }

   private CompletableFuture<Suggestions> suggestTagProperties(SuggestionsBuilder suggestionsBuilder, TagGroup<Block> tagGroup) {
      String string = suggestionsBuilder.getRemaining().toLowerCase(Locale.ROOT);
      if (this.tagId != null && !this.tagId.getPath().isEmpty()) {
         Tag<Block> tag = tagGroup.getTag(this.tagId);
         if (tag != null) {
            Iterator var5 = tag.values().iterator();

            while(var5.hasNext()) {
               Block block = (Block)var5.next();
               Iterator var7 = block.getStateManager().getProperties().iterator();

               while(var7.hasNext()) {
                  Property<?> property = (Property)var7.next();
                  if (!this.tagProperties.containsKey(property.getName()) && property.getName().startsWith(string)) {
                     suggestionsBuilder.suggest(property.getName() + '=');
                  }
               }
            }
         }
      }

      return suggestionsBuilder.buildFuture();
   }

   private CompletableFuture<Suggestions> suggestSnbt(SuggestionsBuilder suggestionsBuilder, TagGroup<Block> tagGroup) {
      if (suggestionsBuilder.getRemaining().isEmpty() && this.hasBlockEntity(tagGroup)) {
         suggestionsBuilder.suggest(String.valueOf('{'));
      }

      return suggestionsBuilder.buildFuture();
   }

   private boolean hasBlockEntity(TagGroup<Block> tagGroup) {
      if (this.blockState != null) {
         return this.blockState.getBlock().hasBlockEntity();
      } else {
         if (this.tagId != null) {
            Tag<Block> tag = tagGroup.getTag(this.tagId);
            if (tag != null) {
               Iterator var3 = tag.values().iterator();

               while(var3.hasNext()) {
                  Block block = (Block)var3.next();
                  if (block.hasBlockEntity()) {
                     return true;
                  }
               }
            }
         }

         return false;
      }
   }

   private CompletableFuture<Suggestions> suggestEqualsCharacter(SuggestionsBuilder suggestionsBuilder, TagGroup<Block> tagGroup) {
      if (suggestionsBuilder.getRemaining().isEmpty()) {
         suggestionsBuilder.suggest(String.valueOf('='));
      }

      return suggestionsBuilder.buildFuture();
   }

   private CompletableFuture<Suggestions> suggestCommaOrEnd(SuggestionsBuilder suggestionsBuilder, TagGroup<Block> tagGroup) {
      if (suggestionsBuilder.getRemaining().isEmpty()) {
         suggestionsBuilder.suggest(String.valueOf(']'));
      }

      if (suggestionsBuilder.getRemaining().isEmpty() && this.blockProperties.size() < this.blockState.getProperties().size()) {
         suggestionsBuilder.suggest(String.valueOf(','));
      }

      return suggestionsBuilder.buildFuture();
   }

   private static <T extends Comparable<T>> SuggestionsBuilder suggestPropertyValues(SuggestionsBuilder suggestionsBuilder, Property<T> property) {
      Iterator var2 = property.getValues().iterator();

      while(var2.hasNext()) {
         T comparable = (Comparable)var2.next();
         if (comparable instanceof Integer) {
            suggestionsBuilder.suggest((Integer)comparable);
         } else {
            suggestionsBuilder.suggest(property.name(comparable));
         }
      }

      return suggestionsBuilder;
   }

   private CompletableFuture<Suggestions> suggestTagPropertyValues(SuggestionsBuilder suggestionsBuilder, TagGroup<Block> tagGroup, String string) {
      boolean bl = false;
      if (this.tagId != null && !this.tagId.getPath().isEmpty()) {
         Tag<Block> tag = tagGroup.getTag(this.tagId);
         if (tag != null) {
            Iterator var6 = tag.values().iterator();

            label40:
            while(true) {
               while(true) {
                  Block block;
                  do {
                     if (!var6.hasNext()) {
                        break label40;
                     }

                     block = (Block)var6.next();
                     Property<?> property = block.getStateManager().getProperty(string);
                     if (property != null) {
                        suggestPropertyValues(suggestionsBuilder, property);
                     }
                  } while(bl);

                  Iterator var9 = block.getStateManager().getProperties().iterator();

                  while(var9.hasNext()) {
                     Property<?> property2 = (Property)var9.next();
                     if (!this.tagProperties.containsKey(property2.getName())) {
                        bl = true;
                        break;
                     }
                  }
               }
            }
         }
      }

      if (bl) {
         suggestionsBuilder.suggest(String.valueOf(','));
      }

      suggestionsBuilder.suggest(String.valueOf(']'));
      return suggestionsBuilder.buildFuture();
   }

   private CompletableFuture<Suggestions> suggestSnbtOrTagProperties(SuggestionsBuilder suggestionsBuilder, TagGroup<Block> tagGroup) {
      if (suggestionsBuilder.getRemaining().isEmpty()) {
         Tag<Block> tag = tagGroup.getTag(this.tagId);
         if (tag != null) {
            boolean bl = false;
            boolean bl2 = false;
            Iterator var6 = tag.values().iterator();

            while(var6.hasNext()) {
               Block block = (Block)var6.next();
               bl |= !block.getStateManager().getProperties().isEmpty();
               bl2 |= block.hasBlockEntity();
               if (bl && bl2) {
                  break;
               }
            }

            if (bl) {
               suggestionsBuilder.suggest(String.valueOf('['));
            }

            if (bl2) {
               suggestionsBuilder.suggest(String.valueOf('{'));
            }
         }
      }

      return this.suggestIdentifiers(suggestionsBuilder, tagGroup);
   }

   private CompletableFuture<Suggestions> suggestSnbtOrBlockProperties(SuggestionsBuilder suggestionsBuilder, TagGroup<Block> tagGroup) {
      if (suggestionsBuilder.getRemaining().isEmpty()) {
         if (!this.blockState.getBlock().getStateManager().getProperties().isEmpty()) {
            suggestionsBuilder.suggest(String.valueOf('['));
         }

         if (this.blockState.getBlock().hasBlockEntity()) {
            suggestionsBuilder.suggest(String.valueOf('{'));
         }
      }

      return suggestionsBuilder.buildFuture();
   }

   private CompletableFuture<Suggestions> suggestIdentifiers(SuggestionsBuilder suggestionsBuilder, TagGroup<Block> tagGroup) {
      return CommandSource.suggestIdentifiers((Iterable)tagGroup.getTagIds(), suggestionsBuilder.createOffset(this.cursorPos).add(suggestionsBuilder));
   }

   private CompletableFuture<Suggestions> suggestBlockOrTagId(SuggestionsBuilder suggestionsBuilder, TagGroup<Block> tagGroup) {
      if (this.allowTag) {
         CommandSource.suggestIdentifiers(tagGroup.getTagIds(), suggestionsBuilder, String.valueOf('#'));
      }

      CommandSource.suggestIdentifiers((Iterable)Registry.BLOCK.getIds(), suggestionsBuilder);
      return suggestionsBuilder.buildFuture();
   }

   public void parseBlockId() throws CommandSyntaxException {
      int i = this.reader.getCursor();
      this.blockId = Identifier.fromCommandInput(this.reader);
      Block block = (Block)Registry.BLOCK.getOrEmpty(this.blockId).orElseThrow(() -> {
         this.reader.setCursor(i);
         return INVALID_BLOCK_ID_EXCEPTION.createWithContext(this.reader, this.blockId.toString());
      });
      this.stateFactory = block.getStateManager();
      this.blockState = block.getDefaultState();
   }

   public void parseTagId() throws CommandSyntaxException {
      if (!this.allowTag) {
         throw DISALLOWED_TAG_EXCEPTION.create();
      } else {
         this.suggestions = this::suggestIdentifiers;
         this.reader.expect('#');
         this.cursorPos = this.reader.getCursor();
         this.tagId = Identifier.fromCommandInput(this.reader);
      }
   }

   public void parseBlockProperties() throws CommandSyntaxException {
      this.reader.skip();
      this.suggestions = this::suggestBlockPropertiesOrEnd;
      this.reader.skipWhitespace();

      while(this.reader.canRead() && this.reader.peek() != ']') {
         this.reader.skipWhitespace();
         int i = this.reader.getCursor();
         String string = this.reader.readString();
         Property<?> property = this.stateFactory.getProperty(string);
         if (property == null) {
            this.reader.setCursor(i);
            throw UNKNOWN_PROPERTY_EXCEPTION.createWithContext(this.reader, this.blockId.toString(), string);
         }

         if (this.blockProperties.containsKey(property)) {
            this.reader.setCursor(i);
            throw DUPLICATE_PROPERTY_EXCEPTION.createWithContext(this.reader, this.blockId.toString(), string);
         }

         this.reader.skipWhitespace();
         this.suggestions = this::suggestEqualsCharacter;
         if (this.reader.canRead() && this.reader.peek() == '=') {
            this.reader.skip();
            this.reader.skipWhitespace();
            this.suggestions = (suggestionsBuilder, tagGroup) -> {
               return suggestPropertyValues(suggestionsBuilder, property).buildFuture();
            };
            int j = this.reader.getCursor();
            this.parsePropertyValue(property, this.reader.readString(), j);
            this.suggestions = this::suggestCommaOrEnd;
            this.reader.skipWhitespace();
            if (!this.reader.canRead()) {
               continue;
            }

            if (this.reader.peek() == ',') {
               this.reader.skip();
               this.suggestions = this::suggestBlockProperties;
               continue;
            }

            if (this.reader.peek() != ']') {
               throw UNCLOSED_PROPERTIES_EXCEPTION.createWithContext(this.reader);
            }
            break;
         }

         throw EMPTY_PROPERTY_EXCEPTION.createWithContext(this.reader, this.blockId.toString(), string);
      }

      if (this.reader.canRead()) {
         this.reader.skip();
      } else {
         throw UNCLOSED_PROPERTIES_EXCEPTION.createWithContext(this.reader);
      }
   }

   public void parseTagProperties() throws CommandSyntaxException {
      this.reader.skip();
      this.suggestions = this::suggestTagPropertiesOrEnd;
      int i = -1;
      this.reader.skipWhitespace();

      while(true) {
         if (this.reader.canRead() && this.reader.peek() != ']') {
            this.reader.skipWhitespace();
            int j = this.reader.getCursor();
            String string = this.reader.readString();
            if (this.tagProperties.containsKey(string)) {
               this.reader.setCursor(j);
               throw DUPLICATE_PROPERTY_EXCEPTION.createWithContext(this.reader, this.blockId.toString(), string);
            }

            this.reader.skipWhitespace();
            if (!this.reader.canRead() || this.reader.peek() != '=') {
               this.reader.setCursor(j);
               throw EMPTY_PROPERTY_EXCEPTION.createWithContext(this.reader, this.blockId.toString(), string);
            }

            this.reader.skip();
            this.reader.skipWhitespace();
            this.suggestions = (suggestionsBuilder, tagGroup) -> {
               return this.suggestTagPropertyValues(suggestionsBuilder, tagGroup, string);
            };
            i = this.reader.getCursor();
            String string2 = this.reader.readString();
            this.tagProperties.put(string, string2);
            this.reader.skipWhitespace();
            if (!this.reader.canRead()) {
               continue;
            }

            i = -1;
            if (this.reader.peek() == ',') {
               this.reader.skip();
               this.suggestions = this::suggestTagProperties;
               continue;
            }

            if (this.reader.peek() != ']') {
               throw UNCLOSED_PROPERTIES_EXCEPTION.createWithContext(this.reader);
            }
         }

         if (this.reader.canRead()) {
            this.reader.skip();
            return;
         }

         if (i >= 0) {
            this.reader.setCursor(i);
         }

         throw UNCLOSED_PROPERTIES_EXCEPTION.createWithContext(this.reader);
      }
   }

   public void parseSnbt() throws CommandSyntaxException {
      this.data = (new StringNbtReader(this.reader)).parseCompoundTag();
   }

   private <T extends Comparable<T>> void parsePropertyValue(Property<T> property, String string, int i) throws CommandSyntaxException {
      Optional<T> optional = property.parse(string);
      if (optional.isPresent()) {
         this.blockState = (BlockState)this.blockState.with(property, (Comparable)optional.get());
         this.blockProperties.put(property, optional.get());
      } else {
         this.reader.setCursor(i);
         throw INVALID_PROPERTY_EXCEPTION.createWithContext(this.reader, this.blockId.toString(), property.getName(), string);
      }
   }

   public static String stringifyBlockState(BlockState blockState) {
      StringBuilder stringBuilder = new StringBuilder(Registry.BLOCK.getId(blockState.getBlock()).toString());
      if (!blockState.getProperties().isEmpty()) {
         stringBuilder.append('[');
         boolean bl = false;

         for(UnmodifiableIterator var3 = blockState.getEntries().entrySet().iterator(); var3.hasNext(); bl = true) {
            Entry<Property<?>, Comparable<?>> entry = (Entry)var3.next();
            if (bl) {
               stringBuilder.append(',');
            }

            stringifyProperty(stringBuilder, (Property)entry.getKey(), (Comparable)entry.getValue());
         }

         stringBuilder.append(']');
      }

      return stringBuilder.toString();
   }

   private static <T extends Comparable<T>> void stringifyProperty(StringBuilder stringBuilder, Property<T> property, Comparable<?> comparable) {
      stringBuilder.append(property.getName());
      stringBuilder.append('=');
      stringBuilder.append(property.name(comparable));
   }

   public CompletableFuture<Suggestions> getSuggestions(SuggestionsBuilder suggestionsBuilder, TagGroup<Block> tagGroup) {
      return (CompletableFuture)this.suggestions.apply(suggestionsBuilder.createOffset(this.reader.getCursor()), tagGroup);
   }

   public Map<String, String> getProperties() {
      return this.tagProperties;
   }
}
