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
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.TranslatableText;

public class OpCommand {
   private static final SimpleCommandExceptionType ALREADY_OPPED_EXCEPTION = new SimpleCommandExceptionType(new TranslatableText("commands.op.failed"));

   public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
      dispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)CommandManager.literal("op").requires((serverCommandSource) -> {
         return serverCommandSource.hasPermissionLevel(3);
      })).then(CommandManager.argument("targets", GameProfileArgumentType.gameProfile()).suggests((commandContext, suggestionsBuilder) -> {
         PlayerManager playerManager = ((ServerCommandSource)commandContext.getSource()).getMinecraftServer().getPlayerManager();
         return CommandSource.suggestMatching(playerManager.getPlayerList().stream().filter((serverPlayerEntity) -> {
            return !playerManager.isOperator(serverPlayerEntity.getGameProfile());
         }).map((serverPlayerEntity) -> {
            return serverPlayerEntity.getGameProfile().getName();
         }), suggestionsBuilder);
      }).executes((commandContext) -> {
         return op((ServerCommandSource)commandContext.getSource(), GameProfileArgumentType.getProfileArgument(commandContext, "targets"));
      })));
   }

   private static int op(ServerCommandSource source, Collection<GameProfile> targets) throws CommandSyntaxException {
      PlayerManager playerManager = source.getMinecraftServer().getPlayerManager();
      int i = 0;
      Iterator var4 = targets.iterator();

      while(var4.hasNext()) {
         GameProfile gameProfile = (GameProfile)var4.next();
         if (!playerManager.isOperator(gameProfile)) {
            playerManager.addToOperators(gameProfile);
            ++i;
            source.sendFeedback(new TranslatableText("commands.op.success", new Object[]{((GameProfile)targets.iterator().next()).getName()}), true);
         }
      }

      if (i == 0) {
         throw ALREADY_OPPED_EXCEPTION.create();
      } else {
         return i;
      }
   }
}
