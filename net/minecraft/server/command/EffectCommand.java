package net.minecraft.server.command;

import com.google.common.collect.ImmutableList;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.util.Collection;
import java.util.Iterator;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.argument.MobEffectArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.text.TranslatableText;
import org.jetbrains.annotations.Nullable;

public class EffectCommand {
   private static final SimpleCommandExceptionType GIVE_FAILED_EXCEPTION = new SimpleCommandExceptionType(new TranslatableText("commands.effect.give.failed"));
   private static final SimpleCommandExceptionType CLEAR_EVERYTHING_FAILED_EXCEPTION = new SimpleCommandExceptionType(new TranslatableText("commands.effect.clear.everything.failed"));
   private static final SimpleCommandExceptionType CLEAR_SPECIFIC_FAILED_EXCEPTION = new SimpleCommandExceptionType(new TranslatableText("commands.effect.clear.specific.failed"));

   public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
      dispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)CommandManager.literal("effect").requires((serverCommandSource) -> {
         return serverCommandSource.hasPermissionLevel(2);
      })).then(((LiteralArgumentBuilder)CommandManager.literal("clear").executes((commandContext) -> {
         return executeClear((ServerCommandSource)commandContext.getSource(), ImmutableList.of(((ServerCommandSource)commandContext.getSource()).getEntityOrThrow()));
      })).then(((RequiredArgumentBuilder)CommandManager.argument("targets", EntityArgumentType.entities()).executes((commandContext) -> {
         return executeClear((ServerCommandSource)commandContext.getSource(), EntityArgumentType.getEntities(commandContext, "targets"));
      })).then(CommandManager.argument("effect", MobEffectArgumentType.mobEffect()).executes((commandContext) -> {
         return executeClear((ServerCommandSource)commandContext.getSource(), EntityArgumentType.getEntities(commandContext, "targets"), MobEffectArgumentType.getMobEffect(commandContext, "effect"));
      }))))).then(CommandManager.literal("give").then(CommandManager.argument("targets", EntityArgumentType.entities()).then(((RequiredArgumentBuilder)CommandManager.argument("effect", MobEffectArgumentType.mobEffect()).executes((commandContext) -> {
         return executeGive((ServerCommandSource)commandContext.getSource(), EntityArgumentType.getEntities(commandContext, "targets"), MobEffectArgumentType.getMobEffect(commandContext, "effect"), (Integer)null, 0, true);
      })).then(((RequiredArgumentBuilder)CommandManager.argument("seconds", IntegerArgumentType.integer(1, 1000000)).executes((commandContext) -> {
         return executeGive((ServerCommandSource)commandContext.getSource(), EntityArgumentType.getEntities(commandContext, "targets"), MobEffectArgumentType.getMobEffect(commandContext, "effect"), IntegerArgumentType.getInteger(commandContext, "seconds"), 0, true);
      })).then(((RequiredArgumentBuilder)CommandManager.argument("amplifier", IntegerArgumentType.integer(0, 255)).executes((commandContext) -> {
         return executeGive((ServerCommandSource)commandContext.getSource(), EntityArgumentType.getEntities(commandContext, "targets"), MobEffectArgumentType.getMobEffect(commandContext, "effect"), IntegerArgumentType.getInteger(commandContext, "seconds"), IntegerArgumentType.getInteger(commandContext, "amplifier"), true);
      })).then(CommandManager.argument("hideParticles", BoolArgumentType.bool()).executes((commandContext) -> {
         return executeGive((ServerCommandSource)commandContext.getSource(), EntityArgumentType.getEntities(commandContext, "targets"), MobEffectArgumentType.getMobEffect(commandContext, "effect"), IntegerArgumentType.getInteger(commandContext, "seconds"), IntegerArgumentType.getInteger(commandContext, "amplifier"), !BoolArgumentType.getBool(commandContext, "hideParticles"));
      }))))))));
   }

   private static int executeGive(ServerCommandSource source, Collection<? extends Entity> targets, StatusEffect effect, @Nullable Integer seconds, int amplifier, boolean showParticles) throws CommandSyntaxException {
      int i = 0;
      int m;
      if (seconds != null) {
         if (effect.isInstant()) {
            m = seconds;
         } else {
            m = seconds * 20;
         }
      } else if (effect.isInstant()) {
         m = 1;
      } else {
         m = 600;
      }

      Iterator var8 = targets.iterator();

      while(var8.hasNext()) {
         Entity entity = (Entity)var8.next();
         if (entity instanceof LivingEntity) {
            StatusEffectInstance statusEffectInstance = new StatusEffectInstance(effect, m, amplifier, false, showParticles);
            if (((LivingEntity)entity).addStatusEffect(statusEffectInstance)) {
               ++i;
            }
         }
      }

      if (i == 0) {
         throw GIVE_FAILED_EXCEPTION.create();
      } else {
         if (targets.size() == 1) {
            source.sendFeedback(new TranslatableText("commands.effect.give.success.single", new Object[]{effect.getName(), ((Entity)targets.iterator().next()).getDisplayName(), m / 20}), true);
         } else {
            source.sendFeedback(new TranslatableText("commands.effect.give.success.multiple", new Object[]{effect.getName(), targets.size(), m / 20}), true);
         }

         return i;
      }
   }

   private static int executeClear(ServerCommandSource source, Collection<? extends Entity> targets) throws CommandSyntaxException {
      int i = 0;
      Iterator var3 = targets.iterator();

      while(var3.hasNext()) {
         Entity entity = (Entity)var3.next();
         if (entity instanceof LivingEntity && ((LivingEntity)entity).clearStatusEffects()) {
            ++i;
         }
      }

      if (i == 0) {
         throw CLEAR_EVERYTHING_FAILED_EXCEPTION.create();
      } else {
         if (targets.size() == 1) {
            source.sendFeedback(new TranslatableText("commands.effect.clear.everything.success.single", new Object[]{((Entity)targets.iterator().next()).getDisplayName()}), true);
         } else {
            source.sendFeedback(new TranslatableText("commands.effect.clear.everything.success.multiple", new Object[]{targets.size()}), true);
         }

         return i;
      }
   }

   private static int executeClear(ServerCommandSource source, Collection<? extends Entity> targets, StatusEffect effect) throws CommandSyntaxException {
      int i = 0;
      Iterator var4 = targets.iterator();

      while(var4.hasNext()) {
         Entity entity = (Entity)var4.next();
         if (entity instanceof LivingEntity && ((LivingEntity)entity).removeStatusEffect(effect)) {
            ++i;
         }
      }

      if (i == 0) {
         throw CLEAR_SPECIFIC_FAILED_EXCEPTION.create();
      } else {
         if (targets.size() == 1) {
            source.sendFeedback(new TranslatableText("commands.effect.clear.specific.success.single", new Object[]{effect.getName(), ((Entity)targets.iterator().next()).getDisplayName()}), true);
         } else {
            source.sendFeedback(new TranslatableText("commands.effect.clear.specific.success.multiple", new Object[]{effect.getName(), targets.size()}), true);
         }

         return i;
      }
   }
}
