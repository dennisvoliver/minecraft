package net.minecraft.server.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import net.minecraft.command.argument.AngleArgumentType;
import net.minecraft.command.argument.BlockPosArgumentType;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.math.BlockPos;

public class SetWorldSpawnCommand {
   public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
      dispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)CommandManager.literal("setworldspawn").requires((serverCommandSource) -> {
         return serverCommandSource.hasPermissionLevel(2);
      })).executes((commandContext) -> {
         return execute((ServerCommandSource)commandContext.getSource(), new BlockPos(((ServerCommandSource)commandContext.getSource()).getPosition()), 0.0F);
      })).then(((RequiredArgumentBuilder)CommandManager.argument("pos", BlockPosArgumentType.blockPos()).executes((commandContext) -> {
         return execute((ServerCommandSource)commandContext.getSource(), BlockPosArgumentType.getBlockPos(commandContext, "pos"), 0.0F);
      })).then(CommandManager.argument("angle", AngleArgumentType.angle()).executes((commandContext) -> {
         return execute((ServerCommandSource)commandContext.getSource(), BlockPosArgumentType.getBlockPos(commandContext, "pos"), AngleArgumentType.getAngle(commandContext, "angle"));
      }))));
   }

   private static int execute(ServerCommandSource source, BlockPos pos, float angle) {
      source.getWorld().setSpawnPos(pos, angle);
      source.sendFeedback(new TranslatableText("commands.setworldspawn.success", new Object[]{pos.getX(), pos.getY(), pos.getZ(), angle}), true);
      return 1;
   }
}
