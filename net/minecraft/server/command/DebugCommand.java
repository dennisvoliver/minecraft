package net.minecraft.server.command;

import com.google.common.collect.ImmutableMap;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.spi.FileSystemProvider;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import net.minecraft.SharedConstants;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.profiler.ProfileResult;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

public class DebugCommand {
   private static final Logger LOGGER = LogManager.getLogger();
   private static final SimpleCommandExceptionType NOT_RUNNING_EXCEPTION = new SimpleCommandExceptionType(new TranslatableText("commands.debug.notRunning"));
   private static final SimpleCommandExceptionType ALREADY_RUNNING_EXCEPTION = new SimpleCommandExceptionType(new TranslatableText("commands.debug.alreadyRunning"));
   @Nullable
   private static final FileSystemProvider FILE_SYSTEM_PROVIDER = (FileSystemProvider)FileSystemProvider.installedProviders().stream().filter((fileSystemProvider) -> {
      return fileSystemProvider.getScheme().equalsIgnoreCase("jar");
   }).findFirst().orElse((Object)null);

   public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
      dispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)CommandManager.literal("debug").requires((serverCommandSource) -> {
         return serverCommandSource.hasPermissionLevel(3);
      })).then(CommandManager.literal("start").executes((commandContext) -> {
         return executeStart((ServerCommandSource)commandContext.getSource());
      }))).then(CommandManager.literal("stop").executes((commandContext) -> {
         return executeStop((ServerCommandSource)commandContext.getSource());
      }))).then(CommandManager.literal("report").executes((commandContext) -> {
         return createDebugReport((ServerCommandSource)commandContext.getSource());
      })));
   }

   private static int executeStart(ServerCommandSource source) throws CommandSyntaxException {
      MinecraftServer minecraftServer = source.getMinecraftServer();
      if (minecraftServer.isDebugRunning()) {
         throw ALREADY_RUNNING_EXCEPTION.create();
      } else {
         minecraftServer.enableProfiler();
         source.sendFeedback(new TranslatableText("commands.debug.started", new Object[]{"Started the debug profiler. Type '/debug stop' to stop it."}), true);
         return 0;
      }
   }

   private static int executeStop(ServerCommandSource source) throws CommandSyntaxException {
      MinecraftServer minecraftServer = source.getMinecraftServer();
      if (!minecraftServer.isDebugRunning()) {
         throw NOT_RUNNING_EXCEPTION.create();
      } else {
         ProfileResult profileResult = minecraftServer.stopDebug();
         File file = new File(minecraftServer.getFile("debug"), "profile-results-" + (new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss")).format(new Date()) + ".txt");
         profileResult.save(file);
         float f = (float)profileResult.getTimeSpan() / 1.0E9F;
         float g = (float)profileResult.getTickSpan() / f;
         source.sendFeedback(new TranslatableText("commands.debug.stopped", new Object[]{String.format(Locale.ROOT, "%.2f", f), profileResult.getTickSpan(), String.format("%.2f", g)}), true);
         return MathHelper.floor(g);
      }
   }

   private static int createDebugReport(ServerCommandSource source) {
      MinecraftServer minecraftServer = source.getMinecraftServer();
      String string = "debug-report-" + (new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss")).format(new Date());

      try {
         Path path = minecraftServer.getFile("debug").toPath();
         Files.createDirectories(path);
         Path path2;
         if (!SharedConstants.isDevelopment && FILE_SYSTEM_PROVIDER != null) {
            path2 = path.resolve(string + ".zip");
            FileSystem fileSystem = FILE_SYSTEM_PROVIDER.newFileSystem(path2, ImmutableMap.of("create", "true"));
            Throwable var6 = null;

            try {
               minecraftServer.dump(fileSystem.getPath("/"));
            } catch (Throwable var16) {
               var6 = var16;
               throw var16;
            } finally {
               if (fileSystem != null) {
                  if (var6 != null) {
                     try {
                        fileSystem.close();
                     } catch (Throwable var15) {
                        var6.addSuppressed(var15);
                     }
                  } else {
                     fileSystem.close();
                  }
               }

            }
         } else {
            path2 = path.resolve(string);
            minecraftServer.dump(path2);
         }

         source.sendFeedback(new TranslatableText("commands.debug.reportSaved", new Object[]{string}), false);
         return 1;
      } catch (IOException var18) {
         LOGGER.error((String)"Failed to save debug dump", (Throwable)var18);
         source.sendError(new TranslatableText("commands.debug.reportFailed"));
         return 0;
      }
   }
}
