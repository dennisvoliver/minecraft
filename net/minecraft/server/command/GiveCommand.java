package net.minecraft.server.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.Collection;
import java.util.Iterator;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.argument.ItemStackArgument;
import net.minecraft.command.argument.ItemStackArgumentType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.TranslatableText;

public class GiveCommand {
   public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
      dispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)CommandManager.literal("give").requires((serverCommandSource) -> {
         return serverCommandSource.hasPermissionLevel(2);
      })).then(CommandManager.argument("targets", EntityArgumentType.players()).then(((RequiredArgumentBuilder)CommandManager.argument("item", ItemStackArgumentType.itemStack()).executes((commandContext) -> {
         return execute((ServerCommandSource)commandContext.getSource(), ItemStackArgumentType.getItemStackArgument(commandContext, "item"), EntityArgumentType.getPlayers(commandContext, "targets"), 1);
      })).then(CommandManager.argument("count", IntegerArgumentType.integer(1)).executes((commandContext) -> {
         return execute((ServerCommandSource)commandContext.getSource(), ItemStackArgumentType.getItemStackArgument(commandContext, "item"), EntityArgumentType.getPlayers(commandContext, "targets"), IntegerArgumentType.getInteger(commandContext, "count"));
      })))));
   }

   private static int execute(ServerCommandSource source, ItemStackArgument item, Collection<ServerPlayerEntity> targets, int count) throws CommandSyntaxException {
      Iterator var4 = targets.iterator();

      label40:
      while(var4.hasNext()) {
         ServerPlayerEntity serverPlayerEntity = (ServerPlayerEntity)var4.next();
         int i = count;

         while(true) {
            while(true) {
               if (i <= 0) {
                  continue label40;
               }

               int j = Math.min(item.getItem().getMaxCount(), i);
               i -= j;
               ItemStack itemStack = item.createStack(j, false);
               boolean bl = serverPlayerEntity.inventory.insertStack(itemStack);
               ItemEntity itemEntity;
               if (bl && itemStack.isEmpty()) {
                  itemStack.setCount(1);
                  itemEntity = serverPlayerEntity.dropItem(itemStack, false);
                  if (itemEntity != null) {
                     itemEntity.setDespawnImmediately();
                  }

                  serverPlayerEntity.world.playSound((PlayerEntity)null, serverPlayerEntity.getX(), serverPlayerEntity.getY(), serverPlayerEntity.getZ(), SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.PLAYERS, 0.2F, ((serverPlayerEntity.getRandom().nextFloat() - serverPlayerEntity.getRandom().nextFloat()) * 0.7F + 1.0F) * 2.0F);
                  serverPlayerEntity.playerScreenHandler.sendContentUpdates();
               } else {
                  itemEntity = serverPlayerEntity.dropItem(itemStack, false);
                  if (itemEntity != null) {
                     itemEntity.resetPickupDelay();
                     itemEntity.setOwner(serverPlayerEntity.getUuid());
                  }
               }
            }
         }
      }

      if (targets.size() == 1) {
         source.sendFeedback(new TranslatableText("commands.give.success.single", new Object[]{count, item.createStack(count, false).toHoverableText(), ((ServerPlayerEntity)targets.iterator().next()).getDisplayName()}), true);
      } else {
         source.sendFeedback(new TranslatableText("commands.give.success.single", new Object[]{count, item.createStack(count, false).toHoverableText(), targets.size()}), true);
      }

      return targets.size();
   }
}
