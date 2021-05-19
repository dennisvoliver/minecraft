package net.minecraft.server.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import java.util.Iterator;
import net.minecraft.command.argument.TimeArgumentType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.TranslatableText;

public class TimeCommand {
   public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
      dispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)CommandManager.literal("time").requires((serverCommandSource) -> {
         return serverCommandSource.hasPermissionLevel(2);
      })).then(((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)CommandManager.literal("set").then(CommandManager.literal("day").executes((commandContext) -> {
         return executeSet((ServerCommandSource)commandContext.getSource(), 1000);
      }))).then(CommandManager.literal("noon").executes((commandContext) -> {
         return executeSet((ServerCommandSource)commandContext.getSource(), 6000);
      }))).then(CommandManager.literal("night").executes((commandContext) -> {
         return executeSet((ServerCommandSource)commandContext.getSource(), 13000);
      }))).then(CommandManager.literal("midnight").executes((commandContext) -> {
         return executeSet((ServerCommandSource)commandContext.getSource(), 18000);
      }))).then(CommandManager.argument("time", TimeArgumentType.time()).executes((commandContext) -> {
         return executeSet((ServerCommandSource)commandContext.getSource(), IntegerArgumentType.getInteger(commandContext, "time"));
      })))).then(CommandManager.literal("add").then(CommandManager.argument("time", TimeArgumentType.time()).executes((commandContext) -> {
         return executeAdd((ServerCommandSource)commandContext.getSource(), IntegerArgumentType.getInteger(commandContext, "time"));
      })))).then(((LiteralArgumentBuilder)((LiteralArgumentBuilder)CommandManager.literal("query").then(CommandManager.literal("daytime").executes((commandContext) -> {
         return executeQuery((ServerCommandSource)commandContext.getSource(), getDayTime(((ServerCommandSource)commandContext.getSource()).getWorld()));
      }))).then(CommandManager.literal("gametime").executes((commandContext) -> {
         return executeQuery((ServerCommandSource)commandContext.getSource(), (int)(((ServerCommandSource)commandContext.getSource()).getWorld().getTime() % 2147483647L));
      }))).then(CommandManager.literal("day").executes((commandContext) -> {
         return executeQuery((ServerCommandSource)commandContext.getSource(), (int)(((ServerCommandSource)commandContext.getSource()).getWorld().getTimeOfDay() / 24000L % 2147483647L));
      }))));
   }

   private static int getDayTime(ServerWorld world) {
      return (int)(world.getTimeOfDay() % 24000L);
   }

   private static int executeQuery(ServerCommandSource source, int time) {
      source.sendFeedback(new TranslatableText("commands.time.query", new Object[]{time}), false);
      return time;
   }

   public static int executeSet(ServerCommandSource source, int time) {
      Iterator var2 = source.getMinecraftServer().getWorlds().iterator();

      while(var2.hasNext()) {
         ServerWorld serverWorld = (ServerWorld)var2.next();
         serverWorld.setTimeOfDay((long)time);
      }

      source.sendFeedback(new TranslatableText("commands.time.set", new Object[]{time}), true);
      return getDayTime(source.getWorld());
   }

   public static int executeAdd(ServerCommandSource source, int time) {
      Iterator var2 = source.getMinecraftServer().getWorlds().iterator();

      while(var2.hasNext()) {
         ServerWorld serverWorld = (ServerWorld)var2.next();
         serverWorld.setTimeOfDay(serverWorld.getTimeOfDay() + (long)time);
      }

      int i = getDayTime(source.getWorld());
      source.sendFeedback(new TranslatableText("commands.time.set", new Object[]{i}), true);
      return i;
   }
}
