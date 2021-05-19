package net.minecraft.server.command;

import com.google.common.collect.Lists;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.pattern.CachedBlockPosition;
import net.minecraft.command.argument.BlockPosArgumentType;
import net.minecraft.command.argument.BlockPredicateArgumentType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Clearable;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

public class CloneCommand {
   private static final SimpleCommandExceptionType OVERLAP_EXCEPTION = new SimpleCommandExceptionType(new TranslatableText("commands.clone.overlap"));
   private static final Dynamic2CommandExceptionType TOO_BIG_EXCEPTION = new Dynamic2CommandExceptionType((object, object2) -> {
      return new TranslatableText("commands.clone.toobig", new Object[]{object, object2});
   });
   private static final SimpleCommandExceptionType FAILED_EXCEPTION = new SimpleCommandExceptionType(new TranslatableText("commands.clone.failed"));
   public static final Predicate<CachedBlockPosition> IS_AIR_PREDICATE = (cachedBlockPosition) -> {
      return !cachedBlockPosition.getBlockState().isAir();
   };

   public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
      dispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)CommandManager.literal("clone").requires((serverCommandSource) -> {
         return serverCommandSource.hasPermissionLevel(2);
      })).then(CommandManager.argument("begin", BlockPosArgumentType.blockPos()).then(CommandManager.argument("end", BlockPosArgumentType.blockPos()).then(((RequiredArgumentBuilder)((RequiredArgumentBuilder)((RequiredArgumentBuilder)CommandManager.argument("destination", BlockPosArgumentType.blockPos()).executes((commandContext) -> {
         return execute((ServerCommandSource)commandContext.getSource(), BlockPosArgumentType.getLoadedBlockPos(commandContext, "begin"), BlockPosArgumentType.getLoadedBlockPos(commandContext, "end"), BlockPosArgumentType.getLoadedBlockPos(commandContext, "destination"), (cachedBlockPosition) -> {
            return true;
         }, CloneCommand.Mode.NORMAL);
      })).then(((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)CommandManager.literal("replace").executes((commandContext) -> {
         return execute((ServerCommandSource)commandContext.getSource(), BlockPosArgumentType.getLoadedBlockPos(commandContext, "begin"), BlockPosArgumentType.getLoadedBlockPos(commandContext, "end"), BlockPosArgumentType.getLoadedBlockPos(commandContext, "destination"), (cachedBlockPosition) -> {
            return true;
         }, CloneCommand.Mode.NORMAL);
      })).then(CommandManager.literal("force").executes((commandContext) -> {
         return execute((ServerCommandSource)commandContext.getSource(), BlockPosArgumentType.getLoadedBlockPos(commandContext, "begin"), BlockPosArgumentType.getLoadedBlockPos(commandContext, "end"), BlockPosArgumentType.getLoadedBlockPos(commandContext, "destination"), (cachedBlockPosition) -> {
            return true;
         }, CloneCommand.Mode.FORCE);
      }))).then(CommandManager.literal("move").executes((commandContext) -> {
         return execute((ServerCommandSource)commandContext.getSource(), BlockPosArgumentType.getLoadedBlockPos(commandContext, "begin"), BlockPosArgumentType.getLoadedBlockPos(commandContext, "end"), BlockPosArgumentType.getLoadedBlockPos(commandContext, "destination"), (cachedBlockPosition) -> {
            return true;
         }, CloneCommand.Mode.MOVE);
      }))).then(CommandManager.literal("normal").executes((commandContext) -> {
         return execute((ServerCommandSource)commandContext.getSource(), BlockPosArgumentType.getLoadedBlockPos(commandContext, "begin"), BlockPosArgumentType.getLoadedBlockPos(commandContext, "end"), BlockPosArgumentType.getLoadedBlockPos(commandContext, "destination"), (cachedBlockPosition) -> {
            return true;
         }, CloneCommand.Mode.NORMAL);
      })))).then(((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)CommandManager.literal("masked").executes((commandContext) -> {
         return execute((ServerCommandSource)commandContext.getSource(), BlockPosArgumentType.getLoadedBlockPos(commandContext, "begin"), BlockPosArgumentType.getLoadedBlockPos(commandContext, "end"), BlockPosArgumentType.getLoadedBlockPos(commandContext, "destination"), IS_AIR_PREDICATE, CloneCommand.Mode.NORMAL);
      })).then(CommandManager.literal("force").executes((commandContext) -> {
         return execute((ServerCommandSource)commandContext.getSource(), BlockPosArgumentType.getLoadedBlockPos(commandContext, "begin"), BlockPosArgumentType.getLoadedBlockPos(commandContext, "end"), BlockPosArgumentType.getLoadedBlockPos(commandContext, "destination"), IS_AIR_PREDICATE, CloneCommand.Mode.FORCE);
      }))).then(CommandManager.literal("move").executes((commandContext) -> {
         return execute((ServerCommandSource)commandContext.getSource(), BlockPosArgumentType.getLoadedBlockPos(commandContext, "begin"), BlockPosArgumentType.getLoadedBlockPos(commandContext, "end"), BlockPosArgumentType.getLoadedBlockPos(commandContext, "destination"), IS_AIR_PREDICATE, CloneCommand.Mode.MOVE);
      }))).then(CommandManager.literal("normal").executes((commandContext) -> {
         return execute((ServerCommandSource)commandContext.getSource(), BlockPosArgumentType.getLoadedBlockPos(commandContext, "begin"), BlockPosArgumentType.getLoadedBlockPos(commandContext, "end"), BlockPosArgumentType.getLoadedBlockPos(commandContext, "destination"), IS_AIR_PREDICATE, CloneCommand.Mode.NORMAL);
      })))).then(CommandManager.literal("filtered").then(((RequiredArgumentBuilder)((RequiredArgumentBuilder)((RequiredArgumentBuilder)CommandManager.argument("filter", BlockPredicateArgumentType.blockPredicate()).executes((commandContext) -> {
         return execute((ServerCommandSource)commandContext.getSource(), BlockPosArgumentType.getLoadedBlockPos(commandContext, "begin"), BlockPosArgumentType.getLoadedBlockPos(commandContext, "end"), BlockPosArgumentType.getLoadedBlockPos(commandContext, "destination"), BlockPredicateArgumentType.getBlockPredicate(commandContext, "filter"), CloneCommand.Mode.NORMAL);
      })).then(CommandManager.literal("force").executes((commandContext) -> {
         return execute((ServerCommandSource)commandContext.getSource(), BlockPosArgumentType.getLoadedBlockPos(commandContext, "begin"), BlockPosArgumentType.getLoadedBlockPos(commandContext, "end"), BlockPosArgumentType.getLoadedBlockPos(commandContext, "destination"), BlockPredicateArgumentType.getBlockPredicate(commandContext, "filter"), CloneCommand.Mode.FORCE);
      }))).then(CommandManager.literal("move").executes((commandContext) -> {
         return execute((ServerCommandSource)commandContext.getSource(), BlockPosArgumentType.getLoadedBlockPos(commandContext, "begin"), BlockPosArgumentType.getLoadedBlockPos(commandContext, "end"), BlockPosArgumentType.getLoadedBlockPos(commandContext, "destination"), BlockPredicateArgumentType.getBlockPredicate(commandContext, "filter"), CloneCommand.Mode.MOVE);
      }))).then(CommandManager.literal("normal").executes((commandContext) -> {
         return execute((ServerCommandSource)commandContext.getSource(), BlockPosArgumentType.getLoadedBlockPos(commandContext, "begin"), BlockPosArgumentType.getLoadedBlockPos(commandContext, "end"), BlockPosArgumentType.getLoadedBlockPos(commandContext, "destination"), BlockPredicateArgumentType.getBlockPredicate(commandContext, "filter"), CloneCommand.Mode.NORMAL);
      }))))))));
   }

   private static int execute(ServerCommandSource source, BlockPos begin, BlockPos end, BlockPos destination, Predicate<CachedBlockPosition> filter, CloneCommand.Mode mode) throws CommandSyntaxException {
      BlockBox blockBox = new BlockBox(begin, end);
      BlockPos blockPos = destination.add(blockBox.getDimensions());
      BlockBox blockBox2 = new BlockBox(destination, blockPos);
      if (!mode.allowsOverlap() && blockBox2.intersects(blockBox)) {
         throw OVERLAP_EXCEPTION.create();
      } else {
         int i = blockBox.getBlockCountX() * blockBox.getBlockCountY() * blockBox.getBlockCountZ();
         if (i > 32768) {
            throw TOO_BIG_EXCEPTION.create(32768, i);
         } else {
            ServerWorld serverWorld = source.getWorld();
            if (serverWorld.isRegionLoaded(begin, end) && serverWorld.isRegionLoaded(destination, blockPos)) {
               List<CloneCommand.BlockInfo> list = Lists.newArrayList();
               List<CloneCommand.BlockInfo> list2 = Lists.newArrayList();
               List<CloneCommand.BlockInfo> list3 = Lists.newArrayList();
               Deque<BlockPos> deque = Lists.newLinkedList();
               BlockPos blockPos2 = new BlockPos(blockBox2.minX - blockBox.minX, blockBox2.minY - blockBox.minY, blockBox2.minZ - blockBox.minZ);

               int m;
               for(int j = blockBox.minZ; j <= blockBox.maxZ; ++j) {
                  for(int k = blockBox.minY; k <= blockBox.maxY; ++k) {
                     for(m = blockBox.minX; m <= blockBox.maxX; ++m) {
                        BlockPos blockPos3 = new BlockPos(m, k, j);
                        BlockPos blockPos4 = blockPos3.add(blockPos2);
                        CachedBlockPosition cachedBlockPosition = new CachedBlockPosition(serverWorld, blockPos3, false);
                        BlockState blockState = cachedBlockPosition.getBlockState();
                        if (filter.test(cachedBlockPosition)) {
                           BlockEntity blockEntity = serverWorld.getBlockEntity(blockPos3);
                           if (blockEntity != null) {
                              CompoundTag compoundTag = blockEntity.toTag(new CompoundTag());
                              list2.add(new CloneCommand.BlockInfo(blockPos4, blockState, compoundTag));
                              deque.addLast(blockPos3);
                           } else if (!blockState.isOpaqueFullCube(serverWorld, blockPos3) && !blockState.isFullCube(serverWorld, blockPos3)) {
                              list3.add(new CloneCommand.BlockInfo(blockPos4, blockState, (CompoundTag)null));
                              deque.addFirst(blockPos3);
                           } else {
                              list.add(new CloneCommand.BlockInfo(blockPos4, blockState, (CompoundTag)null));
                              deque.addLast(blockPos3);
                           }
                        }
                     }
                  }
               }

               if (mode == CloneCommand.Mode.MOVE) {
                  Iterator var25 = deque.iterator();

                  BlockPos blockPos6;
                  while(var25.hasNext()) {
                     blockPos6 = (BlockPos)var25.next();
                     BlockEntity blockEntity2 = serverWorld.getBlockEntity(blockPos6);
                     Clearable.clear(blockEntity2);
                     serverWorld.setBlockState(blockPos6, Blocks.BARRIER.getDefaultState(), 2);
                  }

                  var25 = deque.iterator();

                  while(var25.hasNext()) {
                     blockPos6 = (BlockPos)var25.next();
                     serverWorld.setBlockState(blockPos6, Blocks.AIR.getDefaultState(), 3);
                  }
               }

               List<CloneCommand.BlockInfo> list4 = Lists.newArrayList();
               list4.addAll(list);
               list4.addAll(list2);
               list4.addAll(list3);
               List<CloneCommand.BlockInfo> list5 = Lists.reverse(list4);
               Iterator var30 = list5.iterator();

               while(var30.hasNext()) {
                  CloneCommand.BlockInfo blockInfo = (CloneCommand.BlockInfo)var30.next();
                  BlockEntity blockEntity3 = serverWorld.getBlockEntity(blockInfo.pos);
                  Clearable.clear(blockEntity3);
                  serverWorld.setBlockState(blockInfo.pos, Blocks.BARRIER.getDefaultState(), 2);
               }

               m = 0;
               Iterator var32 = list4.iterator();

               CloneCommand.BlockInfo blockInfo4;
               while(var32.hasNext()) {
                  blockInfo4 = (CloneCommand.BlockInfo)var32.next();
                  if (serverWorld.setBlockState(blockInfo4.pos, blockInfo4.state, 2)) {
                     ++m;
                  }
               }

               for(var32 = list2.iterator(); var32.hasNext(); serverWorld.setBlockState(blockInfo4.pos, blockInfo4.state, 2)) {
                  blockInfo4 = (CloneCommand.BlockInfo)var32.next();
                  BlockEntity blockEntity4 = serverWorld.getBlockEntity(blockInfo4.pos);
                  if (blockInfo4.blockEntityTag != null && blockEntity4 != null) {
                     blockInfo4.blockEntityTag.putInt("x", blockInfo4.pos.getX());
                     blockInfo4.blockEntityTag.putInt("y", blockInfo4.pos.getY());
                     blockInfo4.blockEntityTag.putInt("z", blockInfo4.pos.getZ());
                     blockEntity4.fromTag(blockInfo4.state, blockInfo4.blockEntityTag);
                     blockEntity4.markDirty();
                  }
               }

               var32 = list5.iterator();

               while(var32.hasNext()) {
                  blockInfo4 = (CloneCommand.BlockInfo)var32.next();
                  serverWorld.updateNeighbors(blockInfo4.pos, blockInfo4.state.getBlock());
               }

               serverWorld.getBlockTickScheduler().copyScheduledTicks(blockBox, blockPos2);
               if (m == 0) {
                  throw FAILED_EXCEPTION.create();
               } else {
                  source.sendFeedback(new TranslatableText("commands.clone.success", new Object[]{m}), true);
                  return m;
               }
            } else {
               throw BlockPosArgumentType.UNLOADED_EXCEPTION.create();
            }
         }
      }
   }

   static class BlockInfo {
      public final BlockPos pos;
      public final BlockState state;
      @Nullable
      public final CompoundTag blockEntityTag;

      public BlockInfo(BlockPos pos, BlockState state, @Nullable CompoundTag blockEntityTag) {
         this.pos = pos;
         this.state = state;
         this.blockEntityTag = blockEntityTag;
      }
   }

   static enum Mode {
      FORCE(true),
      MOVE(true),
      NORMAL(false);

      private final boolean allowsOverlap;

      private Mode(boolean allowsOverlap) {
         this.allowsOverlap = allowsOverlap;
      }

      public boolean allowsOverlap() {
         return this.allowsOverlap;
      }
   }
}
