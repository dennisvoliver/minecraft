package net.minecraft.server.command;

import com.google.common.collect.ImmutableList;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import java.util.Collection;
import java.util.Iterator;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.text.TranslatableText;

public class KillCommand {
   public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
      dispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)CommandManager.literal("kill").requires((serverCommandSource) -> {
         return serverCommandSource.hasPermissionLevel(2);
      })).executes((commandContext) -> {
         return execute((ServerCommandSource)commandContext.getSource(), ImmutableList.of(((ServerCommandSource)commandContext.getSource()).getEntityOrThrow()));
      })).then(CommandManager.argument("targets", EntityArgumentType.entities()).executes((commandContext) -> {
         return execute((ServerCommandSource)commandContext.getSource(), EntityArgumentType.getEntities(commandContext, "targets"));
      })));
   }

   private static int execute(ServerCommandSource source, Collection<? extends Entity> targets) {
      Iterator var2 = targets.iterator();

      while(var2.hasNext()) {
         Entity entity = (Entity)var2.next();
         entity.kill();
      }

      if (targets.size() == 1) {
         source.sendFeedback(new TranslatableText("commands.kill.success.single", new Object[]{((Entity)targets.iterator().next()).getDisplayName()}), true);
      } else {
         source.sendFeedback(new TranslatableText("commands.kill.success.multiple", new Object[]{targets.size()}), true);
      }

      return targets.size();
   }
}
