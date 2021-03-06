package net.minecraft.server.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import java.util.Collection;
import java.util.Iterator;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import java.util.function.ToIntFunction;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.math.MathHelper;

public class ExperienceCommand {
   private static final SimpleCommandExceptionType SET_POINT_INVALID_EXCEPTION = new SimpleCommandExceptionType(new TranslatableText("commands.experience.set.points.invalid"));

   public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
      LiteralCommandNode<ServerCommandSource> literalCommandNode = dispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)CommandManager.literal("experience").requires((serverCommandSource) -> {
         return serverCommandSource.hasPermissionLevel(2);
      })).then(CommandManager.literal("add").then(CommandManager.argument("targets", EntityArgumentType.players()).then(((RequiredArgumentBuilder)((RequiredArgumentBuilder)CommandManager.argument("amount", IntegerArgumentType.integer()).executes((commandContext) -> {
         return executeAdd((ServerCommandSource)commandContext.getSource(), EntityArgumentType.getPlayers(commandContext, "targets"), IntegerArgumentType.getInteger(commandContext, "amount"), ExperienceCommand.Component.POINTS);
      })).then(CommandManager.literal("points").executes((commandContext) -> {
         return executeAdd((ServerCommandSource)commandContext.getSource(), EntityArgumentType.getPlayers(commandContext, "targets"), IntegerArgumentType.getInteger(commandContext, "amount"), ExperienceCommand.Component.POINTS);
      }))).then(CommandManager.literal("levels").executes((commandContext) -> {
         return executeAdd((ServerCommandSource)commandContext.getSource(), EntityArgumentType.getPlayers(commandContext, "targets"), IntegerArgumentType.getInteger(commandContext, "amount"), ExperienceCommand.Component.LEVELS);
      })))))).then(CommandManager.literal("set").then(CommandManager.argument("targets", EntityArgumentType.players()).then(((RequiredArgumentBuilder)((RequiredArgumentBuilder)CommandManager.argument("amount", IntegerArgumentType.integer(0)).executes((commandContext) -> {
         return executeSet((ServerCommandSource)commandContext.getSource(), EntityArgumentType.getPlayers(commandContext, "targets"), IntegerArgumentType.getInteger(commandContext, "amount"), ExperienceCommand.Component.POINTS);
      })).then(CommandManager.literal("points").executes((commandContext) -> {
         return executeSet((ServerCommandSource)commandContext.getSource(), EntityArgumentType.getPlayers(commandContext, "targets"), IntegerArgumentType.getInteger(commandContext, "amount"), ExperienceCommand.Component.POINTS);
      }))).then(CommandManager.literal("levels").executes((commandContext) -> {
         return executeSet((ServerCommandSource)commandContext.getSource(), EntityArgumentType.getPlayers(commandContext, "targets"), IntegerArgumentType.getInteger(commandContext, "amount"), ExperienceCommand.Component.LEVELS);
      })))))).then(CommandManager.literal("query").then(((RequiredArgumentBuilder)CommandManager.argument("targets", EntityArgumentType.player()).then(CommandManager.literal("points").executes((commandContext) -> {
         return executeQuery((ServerCommandSource)commandContext.getSource(), EntityArgumentType.getPlayer(commandContext, "targets"), ExperienceCommand.Component.POINTS);
      }))).then(CommandManager.literal("levels").executes((commandContext) -> {
         return executeQuery((ServerCommandSource)commandContext.getSource(), EntityArgumentType.getPlayer(commandContext, "targets"), ExperienceCommand.Component.LEVELS);
      })))));
      dispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)CommandManager.literal("xp").requires((serverCommandSource) -> {
         return serverCommandSource.hasPermissionLevel(2);
      })).redirect(literalCommandNode));
   }

   private static int executeQuery(ServerCommandSource source, ServerPlayerEntity player, ExperienceCommand.Component component) {
      int i = component.getter.applyAsInt(player);
      source.sendFeedback(new TranslatableText("commands.experience.query." + component.name, new Object[]{player.getDisplayName(), i}), false);
      return i;
   }

   private static int executeAdd(ServerCommandSource source, Collection<? extends ServerPlayerEntity> targets, int amount, ExperienceCommand.Component component) {
      Iterator var4 = targets.iterator();

      while(var4.hasNext()) {
         ServerPlayerEntity serverPlayerEntity = (ServerPlayerEntity)var4.next();
         component.adder.accept(serverPlayerEntity, amount);
      }

      if (targets.size() == 1) {
         source.sendFeedback(new TranslatableText("commands.experience.add." + component.name + ".success.single", new Object[]{amount, ((ServerPlayerEntity)targets.iterator().next()).getDisplayName()}), true);
      } else {
         source.sendFeedback(new TranslatableText("commands.experience.add." + component.name + ".success.multiple", new Object[]{amount, targets.size()}), true);
      }

      return targets.size();
   }

   private static int executeSet(ServerCommandSource source, Collection<? extends ServerPlayerEntity> targets, int amount, ExperienceCommand.Component component) throws CommandSyntaxException {
      int i = 0;
      Iterator var5 = targets.iterator();

      while(var5.hasNext()) {
         ServerPlayerEntity serverPlayerEntity = (ServerPlayerEntity)var5.next();
         if (component.setter.test(serverPlayerEntity, amount)) {
            ++i;
         }
      }

      if (i == 0) {
         throw SET_POINT_INVALID_EXCEPTION.create();
      } else {
         if (targets.size() == 1) {
            source.sendFeedback(new TranslatableText("commands.experience.set." + component.name + ".success.single", new Object[]{amount, ((ServerPlayerEntity)targets.iterator().next()).getDisplayName()}), true);
         } else {
            source.sendFeedback(new TranslatableText("commands.experience.set." + component.name + ".success.multiple", new Object[]{amount, targets.size()}), true);
         }

         return targets.size();
      }
   }

   static enum Component {
      POINTS("points", PlayerEntity::addExperience, (serverPlayerEntity, integer) -> {
         if (integer >= serverPlayerEntity.getNextLevelExperience()) {
            return false;
         } else {
            serverPlayerEntity.setExperiencePoints(integer);
            return true;
         }
      }, (serverPlayerEntity) -> {
         return MathHelper.floor(serverPlayerEntity.experienceProgress * (float)serverPlayerEntity.getNextLevelExperience());
      }),
      LEVELS("levels", ServerPlayerEntity::addExperienceLevels, (serverPlayerEntity, integer) -> {
         serverPlayerEntity.setExperienceLevel(integer);
         return true;
      }, (serverPlayerEntity) -> {
         return serverPlayerEntity.experienceLevel;
      });

      public final BiConsumer<ServerPlayerEntity, Integer> adder;
      public final BiPredicate<ServerPlayerEntity, Integer> setter;
      public final String name;
      private final ToIntFunction<ServerPlayerEntity> getter;

      private Component(String name, BiConsumer<ServerPlayerEntity, Integer> adder, BiPredicate<ServerPlayerEntity, Integer> setter, ToIntFunction<ServerPlayerEntity> getter) {
         this.adder = adder;
         this.name = name;
         this.setter = setter;
         this.getter = getter;
      }
   }
}
