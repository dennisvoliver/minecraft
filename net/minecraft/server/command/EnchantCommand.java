package net.minecraft.server.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.util.Collection;
import java.util.Iterator;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.argument.ItemEnchantmentArgumentType;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.TranslatableText;

public class EnchantCommand {
   private static final DynamicCommandExceptionType FAILED_ENTITY_EXCEPTION = new DynamicCommandExceptionType((object) -> {
      return new TranslatableText("commands.enchant.failed.entity", new Object[]{object});
   });
   private static final DynamicCommandExceptionType FAILED_ITEMLESS_EXCEPTION = new DynamicCommandExceptionType((object) -> {
      return new TranslatableText("commands.enchant.failed.itemless", new Object[]{object});
   });
   private static final DynamicCommandExceptionType FAILED_INCOMPATIBLE_EXCEPTION = new DynamicCommandExceptionType((object) -> {
      return new TranslatableText("commands.enchant.failed.incompatible", new Object[]{object});
   });
   private static final Dynamic2CommandExceptionType FAILED_LEVEL_EXCEPTION = new Dynamic2CommandExceptionType((object, object2) -> {
      return new TranslatableText("commands.enchant.failed.level", new Object[]{object, object2});
   });
   private static final SimpleCommandExceptionType FAILED_EXCEPTION = new SimpleCommandExceptionType(new TranslatableText("commands.enchant.failed"));

   public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
      dispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)CommandManager.literal("enchant").requires((serverCommandSource) -> {
         return serverCommandSource.hasPermissionLevel(2);
      })).then(CommandManager.argument("targets", EntityArgumentType.entities()).then(((RequiredArgumentBuilder)CommandManager.argument("enchantment", ItemEnchantmentArgumentType.itemEnchantment()).executes((commandContext) -> {
         return execute((ServerCommandSource)commandContext.getSource(), EntityArgumentType.getEntities(commandContext, "targets"), ItemEnchantmentArgumentType.getEnchantment(commandContext, "enchantment"), 1);
      })).then(CommandManager.argument("level", IntegerArgumentType.integer(0)).executes((commandContext) -> {
         return execute((ServerCommandSource)commandContext.getSource(), EntityArgumentType.getEntities(commandContext, "targets"), ItemEnchantmentArgumentType.getEnchantment(commandContext, "enchantment"), IntegerArgumentType.getInteger(commandContext, "level"));
      })))));
   }

   private static int execute(ServerCommandSource source, Collection<? extends Entity> targets, Enchantment enchantment, int level) throws CommandSyntaxException {
      if (level > enchantment.getMaxLevel()) {
         throw FAILED_LEVEL_EXCEPTION.create(level, enchantment.getMaxLevel());
      } else {
         int i = 0;
         Iterator var5 = targets.iterator();

         while(true) {
            while(true) {
               while(true) {
                  while(var5.hasNext()) {
                     Entity entity = (Entity)var5.next();
                     if (entity instanceof LivingEntity) {
                        LivingEntity livingEntity = (LivingEntity)entity;
                        ItemStack itemStack = livingEntity.getMainHandStack();
                        if (!itemStack.isEmpty()) {
                           if (enchantment.isAcceptableItem(itemStack) && EnchantmentHelper.isCompatible(EnchantmentHelper.get(itemStack).keySet(), enchantment)) {
                              itemStack.addEnchantment(enchantment, level);
                              ++i;
                           } else if (targets.size() == 1) {
                              throw FAILED_INCOMPATIBLE_EXCEPTION.create(itemStack.getItem().getName(itemStack).getString());
                           }
                        } else if (targets.size() == 1) {
                           throw FAILED_ITEMLESS_EXCEPTION.create(livingEntity.getName().getString());
                        }
                     } else if (targets.size() == 1) {
                        throw FAILED_ENTITY_EXCEPTION.create(entity.getName().getString());
                     }
                  }

                  if (i == 0) {
                     throw FAILED_EXCEPTION.create();
                  }

                  if (targets.size() == 1) {
                     source.sendFeedback(new TranslatableText("commands.enchant.success.single", new Object[]{enchantment.getName(level), ((Entity)targets.iterator().next()).getDisplayName()}), true);
                  } else {
                     source.sendFeedback(new TranslatableText("commands.enchant.success.multiple", new Object[]{enchantment.getName(level), targets.size()}), true);
                  }

                  return i;
               }
            }
         }
      }
   }
}
