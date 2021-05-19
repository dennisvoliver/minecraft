package net.minecraft.server.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import java.util.Collection;
import java.util.Collections;
import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.argument.IdentifierArgumentType;
import net.minecraft.command.argument.TextArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.boss.BossBar;
import net.minecraft.entity.boss.BossBarManager;
import net.minecraft.entity.boss.CommandBossBar;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.text.Texts;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;

public class BossBarCommand {
   private static final DynamicCommandExceptionType CREATE_FAILED_EXCEPTION = new DynamicCommandExceptionType((object) -> {
      return new TranslatableText("commands.bossbar.create.failed", new Object[]{object});
   });
   private static final DynamicCommandExceptionType UNKNOWN_EXCEPTION = new DynamicCommandExceptionType((object) -> {
      return new TranslatableText("commands.bossbar.unknown", new Object[]{object});
   });
   private static final SimpleCommandExceptionType SET_PLAYERS_UNCHANGED_EXCEPTION = new SimpleCommandExceptionType(new TranslatableText("commands.bossbar.set.players.unchanged"));
   private static final SimpleCommandExceptionType SET_NAME_UNCHANGED_EXCEPTION = new SimpleCommandExceptionType(new TranslatableText("commands.bossbar.set.name.unchanged"));
   private static final SimpleCommandExceptionType SET_COLOR_UNCHANGED_EXCEPTION = new SimpleCommandExceptionType(new TranslatableText("commands.bossbar.set.color.unchanged"));
   private static final SimpleCommandExceptionType SET_STYLE_UNCHANGED_EXCEPTION = new SimpleCommandExceptionType(new TranslatableText("commands.bossbar.set.style.unchanged"));
   private static final SimpleCommandExceptionType SET_VALUE_UNCHANGED_EXCEPTION = new SimpleCommandExceptionType(new TranslatableText("commands.bossbar.set.value.unchanged"));
   private static final SimpleCommandExceptionType SET_MAX_UNCHANGED_EXCEPTION = new SimpleCommandExceptionType(new TranslatableText("commands.bossbar.set.max.unchanged"));
   private static final SimpleCommandExceptionType SET_VISIBILITY_UNCHANGED_HIDDEN_EXCEPTION = new SimpleCommandExceptionType(new TranslatableText("commands.bossbar.set.visibility.unchanged.hidden"));
   private static final SimpleCommandExceptionType SET_VISIBILITY_UNCHANGED_VISIBLE_EXCEPTION = new SimpleCommandExceptionType(new TranslatableText("commands.bossbar.set.visibility.unchanged.visible"));
   public static final SuggestionProvider<ServerCommandSource> SUGGESTION_PROVIDER = (commandContext, suggestionsBuilder) -> {
      return CommandSource.suggestIdentifiers((Iterable)((ServerCommandSource)commandContext.getSource()).getMinecraftServer().getBossBarManager().getIds(), suggestionsBuilder);
   };

   public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
      dispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)CommandManager.literal("bossbar").requires((serverCommandSource) -> {
         return serverCommandSource.hasPermissionLevel(2);
      })).then(CommandManager.literal("add").then(CommandManager.argument("id", IdentifierArgumentType.identifier()).then(CommandManager.argument("name", TextArgumentType.text()).executes((commandContext) -> {
         return addBossBar((ServerCommandSource)commandContext.getSource(), IdentifierArgumentType.getIdentifier(commandContext, "id"), TextArgumentType.getTextArgument(commandContext, "name"));
      }))))).then(CommandManager.literal("remove").then(CommandManager.argument("id", IdentifierArgumentType.identifier()).suggests(SUGGESTION_PROVIDER).executes((commandContext) -> {
         return removeBossBar((ServerCommandSource)commandContext.getSource(), getBossBar(commandContext));
      })))).then(CommandManager.literal("list").executes((commandContext) -> {
         return listBossBars((ServerCommandSource)commandContext.getSource());
      }))).then(CommandManager.literal("set").then(((RequiredArgumentBuilder)((RequiredArgumentBuilder)((RequiredArgumentBuilder)((RequiredArgumentBuilder)((RequiredArgumentBuilder)((RequiredArgumentBuilder)CommandManager.argument("id", IdentifierArgumentType.identifier()).suggests(SUGGESTION_PROVIDER).then(CommandManager.literal("name").then(CommandManager.argument("name", TextArgumentType.text()).executes((commandContext) -> {
         return setName((ServerCommandSource)commandContext.getSource(), getBossBar(commandContext), TextArgumentType.getTextArgument(commandContext, "name"));
      })))).then(((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)CommandManager.literal("color").then(CommandManager.literal("pink").executes((commandContext) -> {
         return setColor((ServerCommandSource)commandContext.getSource(), getBossBar(commandContext), BossBar.Color.PINK);
      }))).then(CommandManager.literal("blue").executes((commandContext) -> {
         return setColor((ServerCommandSource)commandContext.getSource(), getBossBar(commandContext), BossBar.Color.BLUE);
      }))).then(CommandManager.literal("red").executes((commandContext) -> {
         return setColor((ServerCommandSource)commandContext.getSource(), getBossBar(commandContext), BossBar.Color.RED);
      }))).then(CommandManager.literal("green").executes((commandContext) -> {
         return setColor((ServerCommandSource)commandContext.getSource(), getBossBar(commandContext), BossBar.Color.GREEN);
      }))).then(CommandManager.literal("yellow").executes((commandContext) -> {
         return setColor((ServerCommandSource)commandContext.getSource(), getBossBar(commandContext), BossBar.Color.YELLOW);
      }))).then(CommandManager.literal("purple").executes((commandContext) -> {
         return setColor((ServerCommandSource)commandContext.getSource(), getBossBar(commandContext), BossBar.Color.PURPLE);
      }))).then(CommandManager.literal("white").executes((commandContext) -> {
         return setColor((ServerCommandSource)commandContext.getSource(), getBossBar(commandContext), BossBar.Color.WHITE);
      })))).then(((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)CommandManager.literal("style").then(CommandManager.literal("progress").executes((commandContext) -> {
         return setStyle((ServerCommandSource)commandContext.getSource(), getBossBar(commandContext), BossBar.Style.PROGRESS);
      }))).then(CommandManager.literal("notched_6").executes((commandContext) -> {
         return setStyle((ServerCommandSource)commandContext.getSource(), getBossBar(commandContext), BossBar.Style.NOTCHED_6);
      }))).then(CommandManager.literal("notched_10").executes((commandContext) -> {
         return setStyle((ServerCommandSource)commandContext.getSource(), getBossBar(commandContext), BossBar.Style.NOTCHED_10);
      }))).then(CommandManager.literal("notched_12").executes((commandContext) -> {
         return setStyle((ServerCommandSource)commandContext.getSource(), getBossBar(commandContext), BossBar.Style.NOTCHED_12);
      }))).then(CommandManager.literal("notched_20").executes((commandContext) -> {
         return setStyle((ServerCommandSource)commandContext.getSource(), getBossBar(commandContext), BossBar.Style.NOTCHED_20);
      })))).then(CommandManager.literal("value").then(CommandManager.argument("value", IntegerArgumentType.integer(0)).executes((commandContext) -> {
         return setValue((ServerCommandSource)commandContext.getSource(), getBossBar(commandContext), IntegerArgumentType.getInteger(commandContext, "value"));
      })))).then(CommandManager.literal("max").then(CommandManager.argument("max", IntegerArgumentType.integer(1)).executes((commandContext) -> {
         return setMaxValue((ServerCommandSource)commandContext.getSource(), getBossBar(commandContext), IntegerArgumentType.getInteger(commandContext, "max"));
      })))).then(CommandManager.literal("visible").then(CommandManager.argument("visible", BoolArgumentType.bool()).executes((commandContext) -> {
         return setVisible((ServerCommandSource)commandContext.getSource(), getBossBar(commandContext), BoolArgumentType.getBool(commandContext, "visible"));
      })))).then(((LiteralArgumentBuilder)CommandManager.literal("players").executes((commandContext) -> {
         return setPlayers((ServerCommandSource)commandContext.getSource(), getBossBar(commandContext), Collections.emptyList());
      })).then(CommandManager.argument("targets", EntityArgumentType.players()).executes((commandContext) -> {
         return setPlayers((ServerCommandSource)commandContext.getSource(), getBossBar(commandContext), EntityArgumentType.getOptionalPlayers(commandContext, "targets"));
      })))))).then(CommandManager.literal("get").then(((RequiredArgumentBuilder)((RequiredArgumentBuilder)((RequiredArgumentBuilder)CommandManager.argument("id", IdentifierArgumentType.identifier()).suggests(SUGGESTION_PROVIDER).then(CommandManager.literal("value").executes((commandContext) -> {
         return getValue((ServerCommandSource)commandContext.getSource(), getBossBar(commandContext));
      }))).then(CommandManager.literal("max").executes((commandContext) -> {
         return getMaxValue((ServerCommandSource)commandContext.getSource(), getBossBar(commandContext));
      }))).then(CommandManager.literal("visible").executes((commandContext) -> {
         return isVisible((ServerCommandSource)commandContext.getSource(), getBossBar(commandContext));
      }))).then(CommandManager.literal("players").executes((commandContext) -> {
         return getPlayers((ServerCommandSource)commandContext.getSource(), getBossBar(commandContext));
      })))));
   }

   private static int getValue(ServerCommandSource source, CommandBossBar bossBar) {
      source.sendFeedback(new TranslatableText("commands.bossbar.get.value", new Object[]{bossBar.toHoverableText(), bossBar.getValue()}), true);
      return bossBar.getValue();
   }

   private static int getMaxValue(ServerCommandSource source, CommandBossBar bossBar) {
      source.sendFeedback(new TranslatableText("commands.bossbar.get.max", new Object[]{bossBar.toHoverableText(), bossBar.getMaxValue()}), true);
      return bossBar.getMaxValue();
   }

   private static int isVisible(ServerCommandSource source, CommandBossBar bossBar) {
      if (bossBar.isVisible()) {
         source.sendFeedback(new TranslatableText("commands.bossbar.get.visible.visible", new Object[]{bossBar.toHoverableText()}), true);
         return 1;
      } else {
         source.sendFeedback(new TranslatableText("commands.bossbar.get.visible.hidden", new Object[]{bossBar.toHoverableText()}), true);
         return 0;
      }
   }

   private static int getPlayers(ServerCommandSource source, CommandBossBar bossBar) {
      if (bossBar.getPlayers().isEmpty()) {
         source.sendFeedback(new TranslatableText("commands.bossbar.get.players.none", new Object[]{bossBar.toHoverableText()}), true);
      } else {
         source.sendFeedback(new TranslatableText("commands.bossbar.get.players.some", new Object[]{bossBar.toHoverableText(), bossBar.getPlayers().size(), Texts.join(bossBar.getPlayers(), PlayerEntity::getDisplayName)}), true);
      }

      return bossBar.getPlayers().size();
   }

   private static int setVisible(ServerCommandSource source, CommandBossBar bossBar, boolean visible) throws CommandSyntaxException {
      if (bossBar.isVisible() == visible) {
         if (visible) {
            throw SET_VISIBILITY_UNCHANGED_VISIBLE_EXCEPTION.create();
         } else {
            throw SET_VISIBILITY_UNCHANGED_HIDDEN_EXCEPTION.create();
         }
      } else {
         bossBar.setVisible(visible);
         if (visible) {
            source.sendFeedback(new TranslatableText("commands.bossbar.set.visible.success.visible", new Object[]{bossBar.toHoverableText()}), true);
         } else {
            source.sendFeedback(new TranslatableText("commands.bossbar.set.visible.success.hidden", new Object[]{bossBar.toHoverableText()}), true);
         }

         return 0;
      }
   }

   private static int setValue(ServerCommandSource source, CommandBossBar bossBar, int value) throws CommandSyntaxException {
      if (bossBar.getValue() == value) {
         throw SET_VALUE_UNCHANGED_EXCEPTION.create();
      } else {
         bossBar.setValue(value);
         source.sendFeedback(new TranslatableText("commands.bossbar.set.value.success", new Object[]{bossBar.toHoverableText(), value}), true);
         return value;
      }
   }

   private static int setMaxValue(ServerCommandSource source, CommandBossBar bossBar, int value) throws CommandSyntaxException {
      if (bossBar.getMaxValue() == value) {
         throw SET_MAX_UNCHANGED_EXCEPTION.create();
      } else {
         bossBar.setMaxValue(value);
         source.sendFeedback(new TranslatableText("commands.bossbar.set.max.success", new Object[]{bossBar.toHoverableText(), value}), true);
         return value;
      }
   }

   private static int setColor(ServerCommandSource source, CommandBossBar bossBar, BossBar.Color color) throws CommandSyntaxException {
      if (bossBar.getColor().equals(color)) {
         throw SET_COLOR_UNCHANGED_EXCEPTION.create();
      } else {
         bossBar.setColor(color);
         source.sendFeedback(new TranslatableText("commands.bossbar.set.color.success", new Object[]{bossBar.toHoverableText()}), true);
         return 0;
      }
   }

   private static int setStyle(ServerCommandSource source, CommandBossBar bossBar, BossBar.Style style) throws CommandSyntaxException {
      if (bossBar.getOverlay().equals(style)) {
         throw SET_STYLE_UNCHANGED_EXCEPTION.create();
      } else {
         bossBar.setOverlay(style);
         source.sendFeedback(new TranslatableText("commands.bossbar.set.style.success", new Object[]{bossBar.toHoverableText()}), true);
         return 0;
      }
   }

   private static int setName(ServerCommandSource source, CommandBossBar bossBar, Text name) throws CommandSyntaxException {
      Text text = Texts.parse(source, name, (Entity)null, 0);
      if (bossBar.getName().equals(text)) {
         throw SET_NAME_UNCHANGED_EXCEPTION.create();
      } else {
         bossBar.setName(text);
         source.sendFeedback(new TranslatableText("commands.bossbar.set.name.success", new Object[]{bossBar.toHoverableText()}), true);
         return 0;
      }
   }

   private static int setPlayers(ServerCommandSource source, CommandBossBar bossBar, Collection<ServerPlayerEntity> players) throws CommandSyntaxException {
      boolean bl = bossBar.addPlayers(players);
      if (!bl) {
         throw SET_PLAYERS_UNCHANGED_EXCEPTION.create();
      } else {
         if (bossBar.getPlayers().isEmpty()) {
            source.sendFeedback(new TranslatableText("commands.bossbar.set.players.success.none", new Object[]{bossBar.toHoverableText()}), true);
         } else {
            source.sendFeedback(new TranslatableText("commands.bossbar.set.players.success.some", new Object[]{bossBar.toHoverableText(), players.size(), Texts.join(players, PlayerEntity::getDisplayName)}), true);
         }

         return bossBar.getPlayers().size();
      }
   }

   private static int listBossBars(ServerCommandSource source) {
      Collection<CommandBossBar> collection = source.getMinecraftServer().getBossBarManager().getAll();
      if (collection.isEmpty()) {
         source.sendFeedback(new TranslatableText("commands.bossbar.list.bars.none"), false);
      } else {
         source.sendFeedback(new TranslatableText("commands.bossbar.list.bars.some", new Object[]{collection.size(), Texts.join(collection, CommandBossBar::toHoverableText)}), false);
      }

      return collection.size();
   }

   private static int addBossBar(ServerCommandSource source, Identifier name, Text displayName) throws CommandSyntaxException {
      BossBarManager bossBarManager = source.getMinecraftServer().getBossBarManager();
      if (bossBarManager.get(name) != null) {
         throw CREATE_FAILED_EXCEPTION.create(name.toString());
      } else {
         CommandBossBar commandBossBar = bossBarManager.add(name, Texts.parse(source, displayName, (Entity)null, 0));
         source.sendFeedback(new TranslatableText("commands.bossbar.create.success", new Object[]{commandBossBar.toHoverableText()}), true);
         return bossBarManager.getAll().size();
      }
   }

   private static int removeBossBar(ServerCommandSource source, CommandBossBar bossBar) {
      BossBarManager bossBarManager = source.getMinecraftServer().getBossBarManager();
      bossBar.clearPlayers();
      bossBarManager.remove(bossBar);
      source.sendFeedback(new TranslatableText("commands.bossbar.remove.success", new Object[]{bossBar.toHoverableText()}), true);
      return bossBarManager.getAll().size();
   }

   public static CommandBossBar getBossBar(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
      Identifier identifier = IdentifierArgumentType.getIdentifier(context, "id");
      CommandBossBar commandBossBar = ((ServerCommandSource)context.getSource()).getMinecraftServer().getBossBarManager().get(identifier);
      if (commandBossBar == null) {
         throw UNKNOWN_EXCEPTION.create(identifier.toString());
      } else {
         return commandBossBar;
      }
   }
}
