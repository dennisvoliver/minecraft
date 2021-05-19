package net.minecraft.server.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import java.util.Collection;
import java.util.Iterator;
import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.FunctionArgumentType;
import net.minecraft.server.function.CommandFunction;
import net.minecraft.server.function.CommandFunctionManager;
import net.minecraft.text.TranslatableText;

public class FunctionCommand {
   public static final SuggestionProvider<ServerCommandSource> SUGGESTION_PROVIDER = (commandContext, suggestionsBuilder) -> {
      CommandFunctionManager commandFunctionManager = ((ServerCommandSource)commandContext.getSource()).getMinecraftServer().getCommandFunctionManager();
      CommandSource.suggestIdentifiers(commandFunctionManager.method_29464(), suggestionsBuilder, "#");
      return CommandSource.suggestIdentifiers(commandFunctionManager.method_29463(), suggestionsBuilder);
   };

   public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
      dispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)CommandManager.literal("function").requires((serverCommandSource) -> {
         return serverCommandSource.hasPermissionLevel(2);
      })).then(CommandManager.argument("name", FunctionArgumentType.function()).suggests(SUGGESTION_PROVIDER).executes((commandContext) -> {
         return execute((ServerCommandSource)commandContext.getSource(), FunctionArgumentType.getFunctions(commandContext, "name"));
      })));
   }

   private static int execute(ServerCommandSource source, Collection<CommandFunction> functions) {
      int i = 0;

      CommandFunction commandFunction;
      for(Iterator var3 = functions.iterator(); var3.hasNext(); i += source.getMinecraftServer().getCommandFunctionManager().execute(commandFunction, source.withSilent().withMaxLevel(2))) {
         commandFunction = (CommandFunction)var3.next();
      }

      if (functions.size() == 1) {
         source.sendFeedback(new TranslatableText("commands.function.success.single", new Object[]{i, ((CommandFunction)functions.iterator().next()).getId()}), true);
      } else {
         source.sendFeedback(new TranslatableText("commands.function.success.multiple", new Object[]{i, functions.size()}), true);
      }

      return i;
   }
}
