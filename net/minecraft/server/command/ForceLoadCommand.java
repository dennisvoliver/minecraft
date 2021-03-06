package net.minecraft.server.command;

import com.google.common.base.Joiner;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.command.argument.BlockPosArgumentType;
import net.minecraft.command.argument.ColumnPosArgumentType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ColumnPos;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;

public class ForceLoadCommand {
   private static final Dynamic2CommandExceptionType TOO_BIG_EXCEPTION = new Dynamic2CommandExceptionType((object, object2) -> {
      return new TranslatableText("commands.forceload.toobig", new Object[]{object, object2});
   });
   private static final Dynamic2CommandExceptionType QUERY_FAILURE_EXCEPTION = new Dynamic2CommandExceptionType((object, object2) -> {
      return new TranslatableText("commands.forceload.query.failure", new Object[]{object, object2});
   });
   private static final SimpleCommandExceptionType ADDED_FAILURE_EXCEPTION = new SimpleCommandExceptionType(new TranslatableText("commands.forceload.added.failure"));
   private static final SimpleCommandExceptionType REMOVED_FAILURE_EXCEPTION = new SimpleCommandExceptionType(new TranslatableText("commands.forceload.removed.failure"));

   public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
      dispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)CommandManager.literal("forceload").requires((serverCommandSource) -> {
         return serverCommandSource.hasPermissionLevel(2);
      })).then(CommandManager.literal("add").then(((RequiredArgumentBuilder)CommandManager.argument("from", ColumnPosArgumentType.columnPos()).executes((commandContext) -> {
         return executeChange((ServerCommandSource)commandContext.getSource(), ColumnPosArgumentType.getColumnPos(commandContext, "from"), ColumnPosArgumentType.getColumnPos(commandContext, "from"), true);
      })).then(CommandManager.argument("to", ColumnPosArgumentType.columnPos()).executes((commandContext) -> {
         return executeChange((ServerCommandSource)commandContext.getSource(), ColumnPosArgumentType.getColumnPos(commandContext, "from"), ColumnPosArgumentType.getColumnPos(commandContext, "to"), true);
      }))))).then(((LiteralArgumentBuilder)CommandManager.literal("remove").then(((RequiredArgumentBuilder)CommandManager.argument("from", ColumnPosArgumentType.columnPos()).executes((commandContext) -> {
         return executeChange((ServerCommandSource)commandContext.getSource(), ColumnPosArgumentType.getColumnPos(commandContext, "from"), ColumnPosArgumentType.getColumnPos(commandContext, "from"), false);
      })).then(CommandManager.argument("to", ColumnPosArgumentType.columnPos()).executes((commandContext) -> {
         return executeChange((ServerCommandSource)commandContext.getSource(), ColumnPosArgumentType.getColumnPos(commandContext, "from"), ColumnPosArgumentType.getColumnPos(commandContext, "to"), false);
      })))).then(CommandManager.literal("all").executes((commandContext) -> {
         return executeRemoveAll((ServerCommandSource)commandContext.getSource());
      })))).then(((LiteralArgumentBuilder)CommandManager.literal("query").executes((commandContext) -> {
         return executeQuery((ServerCommandSource)commandContext.getSource());
      })).then(CommandManager.argument("pos", ColumnPosArgumentType.columnPos()).executes((commandContext) -> {
         return executeQuery((ServerCommandSource)commandContext.getSource(), ColumnPosArgumentType.getColumnPos(commandContext, "pos"));
      }))));
   }

   private static int executeQuery(ServerCommandSource source, ColumnPos pos) throws CommandSyntaxException {
      ChunkPos chunkPos = new ChunkPos(pos.x >> 4, pos.z >> 4);
      ServerWorld serverWorld = source.getWorld();
      RegistryKey<World> registryKey = serverWorld.getRegistryKey();
      boolean bl = serverWorld.getForcedChunks().contains(chunkPos.toLong());
      if (bl) {
         source.sendFeedback(new TranslatableText("commands.forceload.query.success", new Object[]{chunkPos, registryKey.getValue()}), false);
         return 1;
      } else {
         throw QUERY_FAILURE_EXCEPTION.create(chunkPos, registryKey.getValue());
      }
   }

   private static int executeQuery(ServerCommandSource source) {
      ServerWorld serverWorld = source.getWorld();
      RegistryKey<World> registryKey = serverWorld.getRegistryKey();
      LongSet longSet = serverWorld.getForcedChunks();
      int i = longSet.size();
      if (i > 0) {
         String string = Joiner.on(", ").join(longSet.stream().sorted().map(ChunkPos::new).map(ChunkPos::toString).iterator());
         if (i == 1) {
            source.sendFeedback(new TranslatableText("commands.forceload.list.single", new Object[]{registryKey.getValue(), string}), false);
         } else {
            source.sendFeedback(new TranslatableText("commands.forceload.list.multiple", new Object[]{i, registryKey.getValue(), string}), false);
         }
      } else {
         source.sendError(new TranslatableText("commands.forceload.added.none", new Object[]{registryKey.getValue()}));
      }

      return i;
   }

   private static int executeRemoveAll(ServerCommandSource source) {
      ServerWorld serverWorld = source.getWorld();
      RegistryKey<World> registryKey = serverWorld.getRegistryKey();
      LongSet longSet = serverWorld.getForcedChunks();
      longSet.forEach((l) -> {
         serverWorld.setChunkForced(ChunkPos.getPackedX(l), ChunkPos.getPackedZ(l), false);
      });
      source.sendFeedback(new TranslatableText("commands.forceload.removed.all", new Object[]{registryKey.getValue()}), true);
      return 0;
   }

   private static int executeChange(ServerCommandSource source, ColumnPos from, ColumnPos to, boolean forceLoaded) throws CommandSyntaxException {
      int i = Math.min(from.x, to.x);
      int j = Math.min(from.z, to.z);
      int k = Math.max(from.x, to.x);
      int l = Math.max(from.z, to.z);
      if (i >= -30000000 && j >= -30000000 && k < 30000000 && l < 30000000) {
         int m = i >> 4;
         int n = j >> 4;
         int o = k >> 4;
         int p = l >> 4;
         long q = ((long)(o - m) + 1L) * ((long)(p - n) + 1L);
         if (q > 256L) {
            throw TOO_BIG_EXCEPTION.create(256, q);
         } else {
            ServerWorld serverWorld = source.getWorld();
            RegistryKey<World> registryKey = serverWorld.getRegistryKey();
            ChunkPos chunkPos = null;
            int r = 0;

            for(int s = m; s <= o; ++s) {
               for(int t = n; t <= p; ++t) {
                  boolean bl = serverWorld.setChunkForced(s, t, forceLoaded);
                  if (bl) {
                     ++r;
                     if (chunkPos == null) {
                        chunkPos = new ChunkPos(s, t);
                     }
                  }
               }
            }

            if (r == 0) {
               throw (forceLoaded ? ADDED_FAILURE_EXCEPTION : REMOVED_FAILURE_EXCEPTION).create();
            } else {
               if (r == 1) {
                  source.sendFeedback(new TranslatableText("commands.forceload." + (forceLoaded ? "added" : "removed") + ".single", new Object[]{chunkPos, registryKey.getValue()}), true);
               } else {
                  ChunkPos chunkPos2 = new ChunkPos(m, n);
                  ChunkPos chunkPos3 = new ChunkPos(o, p);
                  source.sendFeedback(new TranslatableText("commands.forceload." + (forceLoaded ? "added" : "removed") + ".multiple", new Object[]{r, registryKey.getValue(), chunkPos2, chunkPos3}), true);
               }

               return r;
            }
         }
      } else {
         throw BlockPosArgumentType.OUT_OF_WORLD_EXCEPTION.create();
      }
   }
}
