package net.minecraft.server.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Util;
import net.minecraft.world.GameMode;
import net.minecraft.world.GameRules;

public class GameModeCommand {
   public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
      LiteralArgumentBuilder<ServerCommandSource> literalArgumentBuilder = (LiteralArgumentBuilder)CommandManager.literal("gamemode").requires((serverCommandSource) -> {
         return serverCommandSource.hasPermissionLevel(2);
      });
      GameMode[] var2 = GameMode.values();
      int var3 = var2.length;

      for(int var4 = 0; var4 < var3; ++var4) {
         GameMode gameMode = var2[var4];
         if (gameMode != GameMode.NOT_SET) {
            literalArgumentBuilder.then(((LiteralArgumentBuilder)CommandManager.literal(gameMode.getName()).executes((commandContext) -> {
               return execute(commandContext, Collections.singleton(((ServerCommandSource)commandContext.getSource()).getPlayer()), gameMode);
            })).then(CommandManager.argument("target", EntityArgumentType.players()).executes((commandContext) -> {
               return execute(commandContext, EntityArgumentType.getPlayers(commandContext, "target"), gameMode);
            })));
         }
      }

      dispatcher.register(literalArgumentBuilder);
   }

   private static void setGameMode(ServerCommandSource source, ServerPlayerEntity player, GameMode gameMode) {
      Text text = new TranslatableText("gameMode." + gameMode.getName());
      if (source.getEntity() == player) {
         source.sendFeedback(new TranslatableText("commands.gamemode.success.self", new Object[]{text}), true);
      } else {
         if (source.getWorld().getGameRules().getBoolean(GameRules.SEND_COMMAND_FEEDBACK)) {
            player.sendSystemMessage(new TranslatableText("gameMode.changed", new Object[]{text}), Util.NIL_UUID);
         }

         source.sendFeedback(new TranslatableText("commands.gamemode.success.other", new Object[]{player.getDisplayName(), text}), true);
      }

   }

   private static int execute(CommandContext<ServerCommandSource> context, Collection<ServerPlayerEntity> targets, GameMode gameMode) {
      int i = 0;
      Iterator var4 = targets.iterator();

      while(var4.hasNext()) {
         ServerPlayerEntity serverPlayerEntity = (ServerPlayerEntity)var4.next();
         if (serverPlayerEntity.interactionManager.getGameMode() != gameMode) {
            serverPlayerEntity.setGameMode(gameMode);
            setGameMode((ServerCommandSource)context.getSource(), serverPlayerEntity, gameMode);
            ++i;
         }
      }

      return i;
   }
}
