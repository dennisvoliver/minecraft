package net.minecraft.command.argument;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.function.CommandFunction;
import net.minecraft.tag.Tag;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;

public class FunctionArgumentType implements ArgumentType<FunctionArgumentType.FunctionArgument> {
   private static final Collection<String> EXAMPLES = Arrays.asList("foo", "foo:bar", "#foo");
   private static final DynamicCommandExceptionType UNKNOWN_FUNCTION_TAG_EXCEPTION = new DynamicCommandExceptionType((object) -> {
      return new TranslatableText("arguments.function.tag.unknown", new Object[]{object});
   });
   private static final DynamicCommandExceptionType UNKNOWN_FUNCTION_EXCEPTION = new DynamicCommandExceptionType((object) -> {
      return new TranslatableText("arguments.function.unknown", new Object[]{object});
   });

   public static FunctionArgumentType function() {
      return new FunctionArgumentType();
   }

   public FunctionArgumentType.FunctionArgument parse(StringReader stringReader) throws CommandSyntaxException {
      final Identifier identifier2;
      if (stringReader.canRead() && stringReader.peek() == '#') {
         stringReader.skip();
         identifier2 = Identifier.fromCommandInput(stringReader);
         return new FunctionArgumentType.FunctionArgument() {
            public Collection<CommandFunction> getFunctions(CommandContext<ServerCommandSource> commandContext) throws CommandSyntaxException {
               Tag<CommandFunction> tag = FunctionArgumentType.getFunctionTag(commandContext, identifier2);
               return tag.values();
            }

            public Pair<Identifier, Either<CommandFunction, Tag<CommandFunction>>> getFunctionOrTag(CommandContext<ServerCommandSource> commandContext) throws CommandSyntaxException {
               return Pair.of(identifier2, Either.right(FunctionArgumentType.getFunctionTag(commandContext, identifier2)));
            }
         };
      } else {
         identifier2 = Identifier.fromCommandInput(stringReader);
         return new FunctionArgumentType.FunctionArgument() {
            public Collection<CommandFunction> getFunctions(CommandContext<ServerCommandSource> commandContext) throws CommandSyntaxException {
               return Collections.singleton(FunctionArgumentType.getFunction(commandContext, identifier2));
            }

            public Pair<Identifier, Either<CommandFunction, Tag<CommandFunction>>> getFunctionOrTag(CommandContext<ServerCommandSource> commandContext) throws CommandSyntaxException {
               return Pair.of(identifier2, Either.left(FunctionArgumentType.getFunction(commandContext, identifier2)));
            }
         };
      }
   }

   private static CommandFunction getFunction(CommandContext<ServerCommandSource> context, Identifier id) throws CommandSyntaxException {
      return (CommandFunction)((ServerCommandSource)context.getSource()).getMinecraftServer().getCommandFunctionManager().getFunction(id).orElseThrow(() -> {
         return UNKNOWN_FUNCTION_EXCEPTION.create(id.toString());
      });
   }

   private static Tag<CommandFunction> getFunctionTag(CommandContext<ServerCommandSource> context, Identifier id) throws CommandSyntaxException {
      Tag<CommandFunction> tag = ((ServerCommandSource)context.getSource()).getMinecraftServer().getCommandFunctionManager().method_29462(id);
      if (tag == null) {
         throw UNKNOWN_FUNCTION_TAG_EXCEPTION.create(id.toString());
      } else {
         return tag;
      }
   }

   public static Collection<CommandFunction> getFunctions(CommandContext<ServerCommandSource> context, String name) throws CommandSyntaxException {
      return ((FunctionArgumentType.FunctionArgument)context.getArgument(name, FunctionArgumentType.FunctionArgument.class)).getFunctions(context);
   }

   public static Pair<Identifier, Either<CommandFunction, Tag<CommandFunction>>> getFunctionOrTag(CommandContext<ServerCommandSource> commandContext, String string) throws CommandSyntaxException {
      return ((FunctionArgumentType.FunctionArgument)commandContext.getArgument(string, FunctionArgumentType.FunctionArgument.class)).getFunctionOrTag(commandContext);
   }

   public Collection<String> getExamples() {
      return EXAMPLES;
   }

   public interface FunctionArgument {
      Collection<CommandFunction> getFunctions(CommandContext<ServerCommandSource> commandContext) throws CommandSyntaxException;

      Pair<Identifier, Either<CommandFunction, Tag<CommandFunction>>> getFunctionOrTag(CommandContext<ServerCommandSource> commandContext) throws CommandSyntaxException;
   }
}
