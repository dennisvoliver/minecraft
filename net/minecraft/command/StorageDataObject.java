package net.minecraft.command;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import java.util.Locale;
import java.util.function.Function;
import net.minecraft.command.argument.IdentifierArgumentType;
import net.minecraft.command.argument.NbtPathArgumentType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.DataCommand;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;

public class StorageDataObject implements DataCommandObject {
   private static final SuggestionProvider<ServerCommandSource> SUGGESTION_PROVIDER = (commandContext, suggestionsBuilder) -> {
      return CommandSource.suggestIdentifiers(of(commandContext).getIds(), suggestionsBuilder);
   };
   public static final Function<String, DataCommand.ObjectType> TYPE_FACTORY = (string) -> {
      return new DataCommand.ObjectType() {
         public DataCommandObject getObject(CommandContext<ServerCommandSource> context) {
            return new StorageDataObject(StorageDataObject.of(context), IdentifierArgumentType.getIdentifier(context, string));
         }

         public ArgumentBuilder<ServerCommandSource, ?> addArgumentsToBuilder(ArgumentBuilder<ServerCommandSource, ?> argument, Function<ArgumentBuilder<ServerCommandSource, ?>, ArgumentBuilder<ServerCommandSource, ?>> argumentAdder) {
            return argument.then(CommandManager.literal("storage").then((ArgumentBuilder)argumentAdder.apply(CommandManager.argument(string, IdentifierArgumentType.identifier()).suggests(StorageDataObject.SUGGESTION_PROVIDER))));
         }
      };
   };
   private final DataCommandStorage storage;
   private final Identifier id;

   private static DataCommandStorage of(CommandContext<ServerCommandSource> commandContext) {
      return ((ServerCommandSource)commandContext.getSource()).getMinecraftServer().getDataCommandStorage();
   }

   private StorageDataObject(DataCommandStorage storage, Identifier id) {
      this.storage = storage;
      this.id = id;
   }

   public void setTag(CompoundTag tag) {
      this.storage.set(this.id, tag);
   }

   public CompoundTag getTag() {
      return this.storage.get(this.id);
   }

   public Text feedbackModify() {
      return new TranslatableText("commands.data.storage.modified", new Object[]{this.id});
   }

   public Text feedbackQuery(Tag tag) {
      return new TranslatableText("commands.data.storage.query", new Object[]{this.id, tag.toText()});
   }

   public Text feedbackGet(NbtPathArgumentType.NbtPath nbtPath, double scale, int result) {
      return new TranslatableText("commands.data.storage.get", new Object[]{nbtPath, this.id, String.format(Locale.ROOT, "%.2f", scale), result});
   }
}
