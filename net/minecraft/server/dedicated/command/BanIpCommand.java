package net.minecraft.server.dedicated.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.command.EntitySelector;
import net.minecraft.command.argument.MessageArgumentType;
import net.minecraft.server.BannedIpEntry;
import net.minecraft.server.BannedIpList;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import org.jetbrains.annotations.Nullable;

public class BanIpCommand {
   public static final Pattern PATTERN = Pattern.compile("^([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.([01]?\\d\\d?|2[0-4]\\d|25[0-5])$");
   private static final SimpleCommandExceptionType INVALID_IP_EXCEPTION = new SimpleCommandExceptionType(new TranslatableText("commands.banip.invalid"));
   private static final SimpleCommandExceptionType ALREADY_BANNED_EXCEPTION = new SimpleCommandExceptionType(new TranslatableText("commands.banip.failed"));

   public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
      dispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)CommandManager.literal("ban-ip").requires((serverCommandSource) -> {
         return serverCommandSource.hasPermissionLevel(3);
      })).then(((RequiredArgumentBuilder)CommandManager.argument("target", StringArgumentType.word()).executes((commandContext) -> {
         return checkIp((ServerCommandSource)commandContext.getSource(), StringArgumentType.getString(commandContext, "target"), (Text)null);
      })).then(CommandManager.argument("reason", MessageArgumentType.message()).executes((commandContext) -> {
         return checkIp((ServerCommandSource)commandContext.getSource(), StringArgumentType.getString(commandContext, "target"), MessageArgumentType.getMessage(commandContext, "reason"));
      }))));
   }

   private static int checkIp(ServerCommandSource source, String target, @Nullable Text reason) throws CommandSyntaxException {
      Matcher matcher = PATTERN.matcher(target);
      if (matcher.matches()) {
         return banIp(source, target, reason);
      } else {
         ServerPlayerEntity serverPlayerEntity = source.getMinecraftServer().getPlayerManager().getPlayer(target);
         if (serverPlayerEntity != null) {
            return banIp(source, serverPlayerEntity.getIp(), reason);
         } else {
            throw INVALID_IP_EXCEPTION.create();
         }
      }
   }

   private static int banIp(ServerCommandSource source, String targetIp, @Nullable Text reason) throws CommandSyntaxException {
      BannedIpList bannedIpList = source.getMinecraftServer().getPlayerManager().getIpBanList();
      if (bannedIpList.isBanned(targetIp)) {
         throw ALREADY_BANNED_EXCEPTION.create();
      } else {
         List<ServerPlayerEntity> list = source.getMinecraftServer().getPlayerManager().getPlayersByIp(targetIp);
         BannedIpEntry bannedIpEntry = new BannedIpEntry(targetIp, (Date)null, source.getName(), (Date)null, reason == null ? null : reason.getString());
         bannedIpList.add(bannedIpEntry);
         source.sendFeedback(new TranslatableText("commands.banip.success", new Object[]{targetIp, bannedIpEntry.getReason()}), true);
         if (!list.isEmpty()) {
            source.sendFeedback(new TranslatableText("commands.banip.info", new Object[]{list.size(), EntitySelector.getNames(list)}), true);
         }

         Iterator var6 = list.iterator();

         while(var6.hasNext()) {
            ServerPlayerEntity serverPlayerEntity = (ServerPlayerEntity)var6.next();
            serverPlayerEntity.networkHandler.disconnect(new TranslatableText("multiplayer.disconnect.ip_banned"));
         }

         return list.size();
      }
   }
}
