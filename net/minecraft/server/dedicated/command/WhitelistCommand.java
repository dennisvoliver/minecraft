package net.minecraft.server.dedicated.command;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.util.Collection;
import java.util.Iterator;
import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.GameProfileArgumentType;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.Whitelist;
import net.minecraft.server.WhitelistEntry;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Texts;
import net.minecraft.text.TranslatableText;

public class WhitelistCommand {
   private static final SimpleCommandExceptionType ALREADY_ON_EXCEPTION = new SimpleCommandExceptionType(new TranslatableText("commands.whitelist.alreadyOn"));
   private static final SimpleCommandExceptionType ALREADY_OFF_EXCEPTION = new SimpleCommandExceptionType(new TranslatableText("commands.whitelist.alreadyOff"));
   private static final SimpleCommandExceptionType ADD_FAILED_EXCEPTION = new SimpleCommandExceptionType(new TranslatableText("commands.whitelist.add.failed"));
   private static final SimpleCommandExceptionType REMOVE_FAILED_EXCEPTION = new SimpleCommandExceptionType(new TranslatableText("commands.whitelist.remove.failed"));

   public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
      dispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)CommandManager.literal("whitelist").requires((serverCommandSource) -> {
         return serverCommandSource.hasPermissionLevel(3);
      })).then(CommandManager.literal("on").executes((commandContext) -> {
         return executeOn((ServerCommandSource)commandContext.getSource());
      }))).then(CommandManager.literal("off").executes((commandContext) -> {
         return executeOff((ServerCommandSource)commandContext.getSource());
      }))).then(CommandManager.literal("list").executes((commandContext) -> {
         return executeList((ServerCommandSource)commandContext.getSource());
      }))).then(CommandManager.literal("add").then(CommandManager.argument("targets", GameProfileArgumentType.gameProfile()).suggests((commandContext, suggestionsBuilder) -> {
         PlayerManager playerManager = ((ServerCommandSource)commandContext.getSource()).getMinecraftServer().getPlayerManager();
         return CommandSource.suggestMatching(playerManager.getPlayerList().stream().filter((serverPlayerEntity) -> {
            return !playerManager.getWhitelist().isAllowed(serverPlayerEntity.getGameProfile());
         }).map((serverPlayerEntity) -> {
            return serverPlayerEntity.getGameProfile().getName();
         }), suggestionsBuilder);
      }).executes((commandContext) -> {
         return executeAdd((ServerCommandSource)commandContext.getSource(), GameProfileArgumentType.getProfileArgument(commandContext, "targets"));
      })))).then(CommandManager.literal("remove").then(CommandManager.argument("targets", GameProfileArgumentType.gameProfile()).suggests((commandContext, suggestionsBuilder) -> {
         return CommandSource.suggestMatching(((ServerCommandSource)commandContext.getSource()).getMinecraftServer().getPlayerManager().getWhitelistedNames(), suggestionsBuilder);
      }).executes((commandContext) -> {
         return executeRemove((ServerCommandSource)commandContext.getSource(), GameProfileArgumentType.getProfileArgument(commandContext, "targets"));
      })))).then(CommandManager.literal("reload").executes((commandContext) -> {
         return executeReload((ServerCommandSource)commandContext.getSource());
      })));
   }

   private static int executeReload(ServerCommandSource source) {
      source.getMinecraftServer().getPlayerManager().reloadWhitelist();
      source.sendFeedback(new TranslatableText("commands.whitelist.reloaded"), true);
      source.getMinecraftServer().kickNonWhitelistedPlayers(source);
      return 1;
   }

   private static int executeAdd(ServerCommandSource source, Collection<GameProfile> targets) throws CommandSyntaxException {
      Whitelist whitelist = source.getMinecraftServer().getPlayerManager().getWhitelist();
      int i = 0;
      Iterator var4 = targets.iterator();

      while(var4.hasNext()) {
         GameProfile gameProfile = (GameProfile)var4.next();
         if (!whitelist.isAllowed(gameProfile)) {
            WhitelistEntry whitelistEntry = new WhitelistEntry(gameProfile);
            whitelist.add(whitelistEntry);
            source.sendFeedback(new TranslatableText("commands.whitelist.add.success", new Object[]{Texts.toText(gameProfile)}), true);
            ++i;
         }
      }

      if (i == 0) {
         throw ADD_FAILED_EXCEPTION.create();
      } else {
         return i;
      }
   }

   private static int executeRemove(ServerCommandSource source, Collection<GameProfile> targets) throws CommandSyntaxException {
      Whitelist whitelist = source.getMinecraftServer().getPlayerManager().getWhitelist();
      int i = 0;
      Iterator var4 = targets.iterator();

      while(var4.hasNext()) {
         GameProfile gameProfile = (GameProfile)var4.next();
         if (whitelist.isAllowed(gameProfile)) {
            WhitelistEntry whitelistEntry = new WhitelistEntry(gameProfile);
            whitelist.remove(whitelistEntry);
            source.sendFeedback(new TranslatableText("commands.whitelist.remove.success", new Object[]{Texts.toText(gameProfile)}), true);
            ++i;
         }
      }

      if (i == 0) {
         throw REMOVE_FAILED_EXCEPTION.create();
      } else {
         source.getMinecraftServer().kickNonWhitelistedPlayers(source);
         return i;
      }
   }

   private static int executeOn(ServerCommandSource source) throws CommandSyntaxException {
      PlayerManager playerManager = source.getMinecraftServer().getPlayerManager();
      if (playerManager.isWhitelistEnabled()) {
         throw ALREADY_ON_EXCEPTION.create();
      } else {
         playerManager.setWhitelistEnabled(true);
         source.sendFeedback(new TranslatableText("commands.whitelist.enabled"), true);
         source.getMinecraftServer().kickNonWhitelistedPlayers(source);
         return 1;
      }
   }

   private static int executeOff(ServerCommandSource source) throws CommandSyntaxException {
      PlayerManager playerManager = source.getMinecraftServer().getPlayerManager();
      if (!playerManager.isWhitelistEnabled()) {
         throw ALREADY_OFF_EXCEPTION.create();
      } else {
         playerManager.setWhitelistEnabled(false);
         source.sendFeedback(new TranslatableText("commands.whitelist.disabled"), true);
         return 1;
      }
   }

   private static int executeList(ServerCommandSource source) {
      String[] strings = source.getMinecraftServer().getPlayerManager().getWhitelistedNames();
      if (strings.length == 0) {
         source.sendFeedback(new TranslatableText("commands.whitelist.none"), false);
      } else {
         source.sendFeedback(new TranslatableText("commands.whitelist.list", new Object[]{strings.length, String.join(", ", strings)}), false);
      }

      return strings.length;
   }
}
