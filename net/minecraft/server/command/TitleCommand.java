package net.minecraft.server.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.Collection;
import java.util.Iterator;
import java.util.Locale;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.argument.TextArgumentType;
import net.minecraft.network.packet.s2c.play.TitleS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.text.Texts;
import net.minecraft.text.TranslatableText;

public class TitleCommand {
   public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
      dispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)CommandManager.literal("title").requires((serverCommandSource) -> {
         return serverCommandSource.hasPermissionLevel(2);
      })).then(((RequiredArgumentBuilder)((RequiredArgumentBuilder)((RequiredArgumentBuilder)((RequiredArgumentBuilder)((RequiredArgumentBuilder)CommandManager.argument("targets", EntityArgumentType.players()).then(CommandManager.literal("clear").executes((commandContext) -> {
         return executeClear((ServerCommandSource)commandContext.getSource(), EntityArgumentType.getPlayers(commandContext, "targets"));
      }))).then(CommandManager.literal("reset").executes((commandContext) -> {
         return executeReset((ServerCommandSource)commandContext.getSource(), EntityArgumentType.getPlayers(commandContext, "targets"));
      }))).then(CommandManager.literal("title").then(CommandManager.argument("title", TextArgumentType.text()).executes((commandContext) -> {
         return executeTitle((ServerCommandSource)commandContext.getSource(), EntityArgumentType.getPlayers(commandContext, "targets"), TextArgumentType.getTextArgument(commandContext, "title"), TitleS2CPacket.Action.TITLE);
      })))).then(CommandManager.literal("subtitle").then(CommandManager.argument("title", TextArgumentType.text()).executes((commandContext) -> {
         return executeTitle((ServerCommandSource)commandContext.getSource(), EntityArgumentType.getPlayers(commandContext, "targets"), TextArgumentType.getTextArgument(commandContext, "title"), TitleS2CPacket.Action.SUBTITLE);
      })))).then(CommandManager.literal("actionbar").then(CommandManager.argument("title", TextArgumentType.text()).executes((commandContext) -> {
         return executeTitle((ServerCommandSource)commandContext.getSource(), EntityArgumentType.getPlayers(commandContext, "targets"), TextArgumentType.getTextArgument(commandContext, "title"), TitleS2CPacket.Action.ACTIONBAR);
      })))).then(CommandManager.literal("times").then(CommandManager.argument("fadeIn", IntegerArgumentType.integer(0)).then(CommandManager.argument("stay", IntegerArgumentType.integer(0)).then(CommandManager.argument("fadeOut", IntegerArgumentType.integer(0)).executes((commandContext) -> {
         return executeTimes((ServerCommandSource)commandContext.getSource(), EntityArgumentType.getPlayers(commandContext, "targets"), IntegerArgumentType.getInteger(commandContext, "fadeIn"), IntegerArgumentType.getInteger(commandContext, "stay"), IntegerArgumentType.getInteger(commandContext, "fadeOut"));
      })))))));
   }

   private static int executeClear(ServerCommandSource source, Collection<ServerPlayerEntity> targets) {
      TitleS2CPacket titleS2CPacket = new TitleS2CPacket(TitleS2CPacket.Action.CLEAR, (Text)null);
      Iterator var3 = targets.iterator();

      while(var3.hasNext()) {
         ServerPlayerEntity serverPlayerEntity = (ServerPlayerEntity)var3.next();
         serverPlayerEntity.networkHandler.sendPacket(titleS2CPacket);
      }

      if (targets.size() == 1) {
         source.sendFeedback(new TranslatableText("commands.title.cleared.single", new Object[]{((ServerPlayerEntity)targets.iterator().next()).getDisplayName()}), true);
      } else {
         source.sendFeedback(new TranslatableText("commands.title.cleared.multiple", new Object[]{targets.size()}), true);
      }

      return targets.size();
   }

   private static int executeReset(ServerCommandSource source, Collection<ServerPlayerEntity> targets) {
      TitleS2CPacket titleS2CPacket = new TitleS2CPacket(TitleS2CPacket.Action.RESET, (Text)null);
      Iterator var3 = targets.iterator();

      while(var3.hasNext()) {
         ServerPlayerEntity serverPlayerEntity = (ServerPlayerEntity)var3.next();
         serverPlayerEntity.networkHandler.sendPacket(titleS2CPacket);
      }

      if (targets.size() == 1) {
         source.sendFeedback(new TranslatableText("commands.title.reset.single", new Object[]{((ServerPlayerEntity)targets.iterator().next()).getDisplayName()}), true);
      } else {
         source.sendFeedback(new TranslatableText("commands.title.reset.multiple", new Object[]{targets.size()}), true);
      }

      return targets.size();
   }

   private static int executeTitle(ServerCommandSource source, Collection<ServerPlayerEntity> targets, Text title, TitleS2CPacket.Action type) throws CommandSyntaxException {
      Iterator var4 = targets.iterator();

      while(var4.hasNext()) {
         ServerPlayerEntity serverPlayerEntity = (ServerPlayerEntity)var4.next();
         serverPlayerEntity.networkHandler.sendPacket(new TitleS2CPacket(type, Texts.parse(source, title, serverPlayerEntity, 0)));
      }

      if (targets.size() == 1) {
         source.sendFeedback(new TranslatableText("commands.title.show." + type.name().toLowerCase(Locale.ROOT) + ".single", new Object[]{((ServerPlayerEntity)targets.iterator().next()).getDisplayName()}), true);
      } else {
         source.sendFeedback(new TranslatableText("commands.title.show." + type.name().toLowerCase(Locale.ROOT) + ".multiple", new Object[]{targets.size()}), true);
      }

      return targets.size();
   }

   private static int executeTimes(ServerCommandSource source, Collection<ServerPlayerEntity> targets, int fadeIn, int stay, int fadeOut) {
      TitleS2CPacket titleS2CPacket = new TitleS2CPacket(fadeIn, stay, fadeOut);
      Iterator var6 = targets.iterator();

      while(var6.hasNext()) {
         ServerPlayerEntity serverPlayerEntity = (ServerPlayerEntity)var6.next();
         serverPlayerEntity.networkHandler.sendPacket(titleS2CPacket);
      }

      if (targets.size() == 1) {
         source.sendFeedback(new TranslatableText("commands.title.times.single", new Object[]{((ServerPlayerEntity)targets.iterator().next()).getDisplayName()}), true);
      } else {
         source.sendFeedback(new TranslatableText("commands.title.times.multiple", new Object[]{targets.size()}), true);
      }

      return targets.size();
   }
}
