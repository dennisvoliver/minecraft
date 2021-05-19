package net.minecraft.server.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import net.minecraft.command.argument.AngleArgumentType;
import net.minecraft.command.argument.BlockPosArgumentType;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;

public class SpawnPointCommand {
   public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
      dispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)CommandManager.literal("spawnpoint").requires((serverCommandSource) -> {
         return serverCommandSource.hasPermissionLevel(2);
      })).executes((commandContext) -> {
         return execute((ServerCommandSource)commandContext.getSource(), Collections.singleton(((ServerCommandSource)commandContext.getSource()).getPlayer()), new BlockPos(((ServerCommandSource)commandContext.getSource()).getPosition()), 0.0F);
      })).then(((RequiredArgumentBuilder)CommandManager.argument("targets", EntityArgumentType.players()).executes((commandContext) -> {
         return execute((ServerCommandSource)commandContext.getSource(), EntityArgumentType.getPlayers(commandContext, "targets"), new BlockPos(((ServerCommandSource)commandContext.getSource()).getPosition()), 0.0F);
      })).then(((RequiredArgumentBuilder)CommandManager.argument("pos", BlockPosArgumentType.blockPos()).executes((commandContext) -> {
         return execute((ServerCommandSource)commandContext.getSource(), EntityArgumentType.getPlayers(commandContext, "targets"), BlockPosArgumentType.getBlockPos(commandContext, "pos"), 0.0F);
      })).then(CommandManager.argument("angle", AngleArgumentType.angle()).executes((commandContext) -> {
         return execute((ServerCommandSource)commandContext.getSource(), EntityArgumentType.getPlayers(commandContext, "targets"), BlockPosArgumentType.getBlockPos(commandContext, "pos"), AngleArgumentType.getAngle(commandContext, "angle"));
      })))));
   }

   private static int execute(ServerCommandSource source, Collection<ServerPlayerEntity> targets, BlockPos pos, float angle) {
      RegistryKey<World> registryKey = source.getWorld().getRegistryKey();
      Iterator var5 = targets.iterator();

      while(var5.hasNext()) {
         ServerPlayerEntity serverPlayerEntity = (ServerPlayerEntity)var5.next();
         serverPlayerEntity.setSpawnPoint(registryKey, pos, angle, true, false);
      }

      String string = registryKey.getValue().toString();
      if (targets.size() == 1) {
         source.sendFeedback(new TranslatableText("commands.spawnpoint.success.single", new Object[]{pos.getX(), pos.getY(), pos.getZ(), angle, string, ((ServerPlayerEntity)targets.iterator().next()).getDisplayName()}), true);
      } else {
         source.sendFeedback(new TranslatableText("commands.spawnpoint.success.multiple", new Object[]{pos.getX(), pos.getY(), pos.getZ(), angle, string, targets.size()}), true);
      }

      return targets.size();
   }
}
