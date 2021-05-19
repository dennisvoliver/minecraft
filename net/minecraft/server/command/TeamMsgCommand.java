package net.minecraft.server.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import java.util.Iterator;
import java.util.List;
import net.minecraft.command.argument.MessageArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;

public class TeamMsgCommand {
   private static final Style STYLE;
   private static final SimpleCommandExceptionType NO_TEAM_EXCEPTION;

   public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
      LiteralCommandNode<ServerCommandSource> literalCommandNode = dispatcher.register((LiteralArgumentBuilder)CommandManager.literal("teammsg").then(CommandManager.argument("message", MessageArgumentType.message()).executes((commandContext) -> {
         return execute((ServerCommandSource)commandContext.getSource(), MessageArgumentType.getMessage(commandContext, "message"));
      })));
      dispatcher.register((LiteralArgumentBuilder)CommandManager.literal("tm").redirect(literalCommandNode));
   }

   private static int execute(ServerCommandSource source, Text message) throws CommandSyntaxException {
      Entity entity = source.getEntityOrThrow();
      Team team = (Team)entity.getScoreboardTeam();
      if (team == null) {
         throw NO_TEAM_EXCEPTION.create();
      } else {
         Text text = team.getFormattedName().fillStyle(STYLE);
         List<ServerPlayerEntity> list = source.getMinecraftServer().getPlayerManager().getPlayerList();
         Iterator var6 = list.iterator();

         while(var6.hasNext()) {
            ServerPlayerEntity serverPlayerEntity = (ServerPlayerEntity)var6.next();
            if (serverPlayerEntity == entity) {
               serverPlayerEntity.sendSystemMessage(new TranslatableText("chat.type.team.sent", new Object[]{text, source.getDisplayName(), message}), entity.getUuid());
            } else if (serverPlayerEntity.getScoreboardTeam() == team) {
               serverPlayerEntity.sendSystemMessage(new TranslatableText("chat.type.team.text", new Object[]{text, source.getDisplayName(), message}), entity.getUuid());
            }
         }

         return list.size();
      }
   }

   static {
      STYLE = Style.EMPTY.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TranslatableText("chat.type.team.hover"))).withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/teammsg "));
      NO_TEAM_EXCEPTION = new SimpleCommandExceptionType(new TranslatableText("commands.teammsg.failed.noteam"));
   }
}
