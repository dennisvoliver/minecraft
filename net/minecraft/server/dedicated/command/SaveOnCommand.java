package net.minecraft.server.dedicated.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.util.Iterator;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.TranslatableText;

public class SaveOnCommand {
   private static final SimpleCommandExceptionType ALREADY_ON_EXCEPTION = new SimpleCommandExceptionType(new TranslatableText("commands.save.alreadyOn"));

   public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
      dispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)CommandManager.literal("save-on").requires((serverCommandSource) -> {
         return serverCommandSource.hasPermissionLevel(4);
      })).executes((commandContext) -> {
         ServerCommandSource serverCommandSource = (ServerCommandSource)commandContext.getSource();
         boolean bl = false;
         Iterator var3 = serverCommandSource.getMinecraftServer().getWorlds().iterator();

         while(var3.hasNext()) {
            ServerWorld serverWorld = (ServerWorld)var3.next();
            if (serverWorld != null && serverWorld.savingDisabled) {
               serverWorld.savingDisabled = false;
               bl = true;
            }
         }

         if (!bl) {
            throw ALREADY_ON_EXCEPTION.create();
         } else {
            serverCommandSource.sendFeedback(new TranslatableText("commands.save.enabled"), true);
            return 1;
         }
      }));
   }
}
