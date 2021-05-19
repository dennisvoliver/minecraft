package net.minecraft.server.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.argument.IdentifierArgumentType;
import net.minecraft.command.suggestion.SuggestionProviders;
import net.minecraft.recipe.Recipe;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.TranslatableText;

public class RecipeCommand {
   private static final SimpleCommandExceptionType GIVE_FAILED_EXCEPTION = new SimpleCommandExceptionType(new TranslatableText("commands.recipe.give.failed"));
   private static final SimpleCommandExceptionType TAKE_FAILED_EXCEPTION = new SimpleCommandExceptionType(new TranslatableText("commands.recipe.take.failed"));

   public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
      dispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)CommandManager.literal("recipe").requires((serverCommandSource) -> {
         return serverCommandSource.hasPermissionLevel(2);
      })).then(CommandManager.literal("give").then(((RequiredArgumentBuilder)CommandManager.argument("targets", EntityArgumentType.players()).then(CommandManager.argument("recipe", IdentifierArgumentType.identifier()).suggests(SuggestionProviders.ALL_RECIPES).executes((commandContext) -> {
         return executeGive((ServerCommandSource)commandContext.getSource(), EntityArgumentType.getPlayers(commandContext, "targets"), Collections.singleton(IdentifierArgumentType.getRecipeArgument(commandContext, "recipe")));
      }))).then(CommandManager.literal("*").executes((commandContext) -> {
         return executeGive((ServerCommandSource)commandContext.getSource(), EntityArgumentType.getPlayers(commandContext, "targets"), ((ServerCommandSource)commandContext.getSource()).getMinecraftServer().getRecipeManager().values());
      }))))).then(CommandManager.literal("take").then(((RequiredArgumentBuilder)CommandManager.argument("targets", EntityArgumentType.players()).then(CommandManager.argument("recipe", IdentifierArgumentType.identifier()).suggests(SuggestionProviders.ALL_RECIPES).executes((commandContext) -> {
         return executeTake((ServerCommandSource)commandContext.getSource(), EntityArgumentType.getPlayers(commandContext, "targets"), Collections.singleton(IdentifierArgumentType.getRecipeArgument(commandContext, "recipe")));
      }))).then(CommandManager.literal("*").executes((commandContext) -> {
         return executeTake((ServerCommandSource)commandContext.getSource(), EntityArgumentType.getPlayers(commandContext, "targets"), ((ServerCommandSource)commandContext.getSource()).getMinecraftServer().getRecipeManager().values());
      })))));
   }

   private static int executeGive(ServerCommandSource source, Collection<ServerPlayerEntity> targets, Collection<Recipe<?>> recipes) throws CommandSyntaxException {
      int i = 0;

      ServerPlayerEntity serverPlayerEntity;
      for(Iterator var4 = targets.iterator(); var4.hasNext(); i += serverPlayerEntity.unlockRecipes(recipes)) {
         serverPlayerEntity = (ServerPlayerEntity)var4.next();
      }

      if (i == 0) {
         throw GIVE_FAILED_EXCEPTION.create();
      } else {
         if (targets.size() == 1) {
            source.sendFeedback(new TranslatableText("commands.recipe.give.success.single", new Object[]{recipes.size(), ((ServerPlayerEntity)targets.iterator().next()).getDisplayName()}), true);
         } else {
            source.sendFeedback(new TranslatableText("commands.recipe.give.success.multiple", new Object[]{recipes.size(), targets.size()}), true);
         }

         return i;
      }
   }

   private static int executeTake(ServerCommandSource source, Collection<ServerPlayerEntity> targets, Collection<Recipe<?>> recipes) throws CommandSyntaxException {
      int i = 0;

      ServerPlayerEntity serverPlayerEntity;
      for(Iterator var4 = targets.iterator(); var4.hasNext(); i += serverPlayerEntity.lockRecipes(recipes)) {
         serverPlayerEntity = (ServerPlayerEntity)var4.next();
      }

      if (i == 0) {
         throw TAKE_FAILED_EXCEPTION.create();
      } else {
         if (targets.size() == 1) {
            source.sendFeedback(new TranslatableText("commands.recipe.take.success.single", new Object[]{recipes.size(), ((ServerPlayerEntity)targets.iterator().next()).getDisplayName()}), true);
         } else {
            source.sendFeedback(new TranslatableText("commands.recipe.take.success.multiple", new Object[]{recipes.size(), targets.size()}), true);
         }

         return i;
      }
   }
}
