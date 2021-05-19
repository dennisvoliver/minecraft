package net.minecraft.server.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.exceptions.Dynamic3CommandExceptionType;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import java.util.UUID;
import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.argument.IdentifierArgumentType;
import net.minecraft.command.argument.UuidArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.AttributeContainer;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.registry.Registry;

public class AttributeCommand {
   private static final SuggestionProvider<ServerCommandSource> SUGGESTION_PROVIDER = (commandContext, suggestionsBuilder) -> {
      return CommandSource.suggestIdentifiers((Iterable)Registry.ATTRIBUTE.getIds(), suggestionsBuilder);
   };
   private static final DynamicCommandExceptionType ENTITY_FAILED_EXCEPTION = new DynamicCommandExceptionType((object) -> {
      return new TranslatableText("commands.attribute.failed.entity", new Object[]{object});
   });
   private static final Dynamic2CommandExceptionType NO_ATTRIBUTE_EXCEPTION = new Dynamic2CommandExceptionType((object, object2) -> {
      return new TranslatableText("commands.attribute.failed.no_attribute", new Object[]{object, object2});
   });
   private static final Dynamic3CommandExceptionType NO_MODIFIER_EXCEPTION = new Dynamic3CommandExceptionType((object, object2, object3) -> {
      return new TranslatableText("commands.attribute.failed.no_modifier", new Object[]{object2, object, object3});
   });
   private static final Dynamic3CommandExceptionType MODIFIER_ALREADY_PRESENT_EXCEPTION = new Dynamic3CommandExceptionType((object, object2, object3) -> {
      return new TranslatableText("commands.attribute.failed.modifier_already_present", new Object[]{object3, object2, object});
   });

   public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
      dispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)CommandManager.literal("attribute").requires((serverCommandSource) -> {
         return serverCommandSource.hasPermissionLevel(2);
      })).then(CommandManager.argument("target", EntityArgumentType.entity()).then(((RequiredArgumentBuilder)((RequiredArgumentBuilder)CommandManager.argument("attribute", IdentifierArgumentType.identifier()).suggests(SUGGESTION_PROVIDER).then(((LiteralArgumentBuilder)CommandManager.literal("get").executes((commandContext) -> {
         return executeValueGet((ServerCommandSource)commandContext.getSource(), EntityArgumentType.getEntity(commandContext, "target"), IdentifierArgumentType.method_27575(commandContext, "attribute"), 1.0D);
      })).then(CommandManager.argument("scale", DoubleArgumentType.doubleArg()).executes((commandContext) -> {
         return executeValueGet((ServerCommandSource)commandContext.getSource(), EntityArgumentType.getEntity(commandContext, "target"), IdentifierArgumentType.method_27575(commandContext, "attribute"), DoubleArgumentType.getDouble(commandContext, "scale"));
      })))).then(((LiteralArgumentBuilder)CommandManager.literal("base").then(CommandManager.literal("set").then(CommandManager.argument("value", DoubleArgumentType.doubleArg()).executes((commandContext) -> {
         return executeBaseValueSet((ServerCommandSource)commandContext.getSource(), EntityArgumentType.getEntity(commandContext, "target"), IdentifierArgumentType.method_27575(commandContext, "attribute"), DoubleArgumentType.getDouble(commandContext, "value"));
      })))).then(((LiteralArgumentBuilder)CommandManager.literal("get").executes((commandContext) -> {
         return executeBaseValueGet((ServerCommandSource)commandContext.getSource(), EntityArgumentType.getEntity(commandContext, "target"), IdentifierArgumentType.method_27575(commandContext, "attribute"), 1.0D);
      })).then(CommandManager.argument("scale", DoubleArgumentType.doubleArg()).executes((commandContext) -> {
         return executeBaseValueGet((ServerCommandSource)commandContext.getSource(), EntityArgumentType.getEntity(commandContext, "target"), IdentifierArgumentType.method_27575(commandContext, "attribute"), DoubleArgumentType.getDouble(commandContext, "scale"));
      }))))).then(((LiteralArgumentBuilder)((LiteralArgumentBuilder)CommandManager.literal("modifier").then(CommandManager.literal("add").then(CommandManager.argument("uuid", UuidArgumentType.uuid()).then(CommandManager.argument("name", StringArgumentType.string()).then(((RequiredArgumentBuilder)((RequiredArgumentBuilder)CommandManager.argument("value", DoubleArgumentType.doubleArg()).then(CommandManager.literal("add").executes((commandContext) -> {
         return executeModifierAdd((ServerCommandSource)commandContext.getSource(), EntityArgumentType.getEntity(commandContext, "target"), IdentifierArgumentType.method_27575(commandContext, "attribute"), UuidArgumentType.getUuid(commandContext, "uuid"), StringArgumentType.getString(commandContext, "name"), DoubleArgumentType.getDouble(commandContext, "value"), EntityAttributeModifier.Operation.ADDITION);
      }))).then(CommandManager.literal("multiply").executes((commandContext) -> {
         return executeModifierAdd((ServerCommandSource)commandContext.getSource(), EntityArgumentType.getEntity(commandContext, "target"), IdentifierArgumentType.method_27575(commandContext, "attribute"), UuidArgumentType.getUuid(commandContext, "uuid"), StringArgumentType.getString(commandContext, "name"), DoubleArgumentType.getDouble(commandContext, "value"), EntityAttributeModifier.Operation.MULTIPLY_TOTAL);
      }))).then(CommandManager.literal("multiply_base").executes((commandContext) -> {
         return executeModifierAdd((ServerCommandSource)commandContext.getSource(), EntityArgumentType.getEntity(commandContext, "target"), IdentifierArgumentType.method_27575(commandContext, "attribute"), UuidArgumentType.getUuid(commandContext, "uuid"), StringArgumentType.getString(commandContext, "name"), DoubleArgumentType.getDouble(commandContext, "value"), EntityAttributeModifier.Operation.MULTIPLY_BASE);
      }))))))).then(CommandManager.literal("remove").then(CommandManager.argument("uuid", UuidArgumentType.uuid()).executes((commandContext) -> {
         return executeModifierRemove((ServerCommandSource)commandContext.getSource(), EntityArgumentType.getEntity(commandContext, "target"), IdentifierArgumentType.method_27575(commandContext, "attribute"), UuidArgumentType.getUuid(commandContext, "uuid"));
      })))).then(CommandManager.literal("value").then(CommandManager.literal("get").then(((RequiredArgumentBuilder)CommandManager.argument("uuid", UuidArgumentType.uuid()).executes((commandContext) -> {
         return executeModifierValueGet((ServerCommandSource)commandContext.getSource(), EntityArgumentType.getEntity(commandContext, "target"), IdentifierArgumentType.method_27575(commandContext, "attribute"), UuidArgumentType.getUuid(commandContext, "uuid"), 1.0D);
      })).then(CommandManager.argument("scale", DoubleArgumentType.doubleArg()).executes((commandContext) -> {
         return executeModifierValueGet((ServerCommandSource)commandContext.getSource(), EntityArgumentType.getEntity(commandContext, "target"), IdentifierArgumentType.method_27575(commandContext, "attribute"), UuidArgumentType.getUuid(commandContext, "uuid"), DoubleArgumentType.getDouble(commandContext, "scale"));
      })))))))));
   }

   private static EntityAttributeInstance getAttributeInstance(Entity entity, EntityAttribute attribute) throws CommandSyntaxException {
      EntityAttributeInstance entityAttributeInstance = getLivingEntity(entity).getAttributes().getCustomInstance(attribute);
      if (entityAttributeInstance == null) {
         throw NO_ATTRIBUTE_EXCEPTION.create(entity.getName(), new TranslatableText(attribute.getTranslationKey()));
      } else {
         return entityAttributeInstance;
      }
   }

   private static LivingEntity getLivingEntity(Entity entity) throws CommandSyntaxException {
      if (!(entity instanceof LivingEntity)) {
         throw ENTITY_FAILED_EXCEPTION.create(entity.getName());
      } else {
         return (LivingEntity)entity;
      }
   }

   private static LivingEntity getLivingEntityWithAttribute(Entity entity, EntityAttribute attribute) throws CommandSyntaxException {
      LivingEntity livingEntity = getLivingEntity(entity);
      if (!livingEntity.getAttributes().hasAttribute(attribute)) {
         throw NO_ATTRIBUTE_EXCEPTION.create(entity.getName(), new TranslatableText(attribute.getTranslationKey()));
      } else {
         return livingEntity;
      }
   }

   private static int executeValueGet(ServerCommandSource source, Entity target, EntityAttribute attribute, double multiplier) throws CommandSyntaxException {
      LivingEntity livingEntity = getLivingEntityWithAttribute(target, attribute);
      double d = livingEntity.getAttributeValue(attribute);
      source.sendFeedback(new TranslatableText("commands.attribute.value.get.success", new Object[]{new TranslatableText(attribute.getTranslationKey()), target.getName(), d}), false);
      return (int)(d * multiplier);
   }

   private static int executeBaseValueGet(ServerCommandSource source, Entity target, EntityAttribute attribute, double multiplier) throws CommandSyntaxException {
      LivingEntity livingEntity = getLivingEntityWithAttribute(target, attribute);
      double d = livingEntity.getAttributeBaseValue(attribute);
      source.sendFeedback(new TranslatableText("commands.attribute.base_value.get.success", new Object[]{new TranslatableText(attribute.getTranslationKey()), target.getName(), d}), false);
      return (int)(d * multiplier);
   }

   private static int executeModifierValueGet(ServerCommandSource source, Entity target, EntityAttribute attribute, UUID uuid, double multiplier) throws CommandSyntaxException {
      LivingEntity livingEntity = getLivingEntityWithAttribute(target, attribute);
      AttributeContainer attributeContainer = livingEntity.getAttributes();
      if (!attributeContainer.hasModifierForAttribute(attribute, uuid)) {
         throw NO_MODIFIER_EXCEPTION.create(target.getName(), new TranslatableText(attribute.getTranslationKey()), uuid);
      } else {
         double d = attributeContainer.getModifierValue(attribute, uuid);
         source.sendFeedback(new TranslatableText("commands.attribute.modifier.value.get.success", new Object[]{uuid, new TranslatableText(attribute.getTranslationKey()), target.getName(), d}), false);
         return (int)(d * multiplier);
      }
   }

   private static int executeBaseValueSet(ServerCommandSource source, Entity target, EntityAttribute attribute, double value) throws CommandSyntaxException {
      getAttributeInstance(target, attribute).setBaseValue(value);
      source.sendFeedback(new TranslatableText("commands.attribute.base_value.set.success", new Object[]{new TranslatableText(attribute.getTranslationKey()), target.getName(), value}), false);
      return 1;
   }

   private static int executeModifierAdd(ServerCommandSource source, Entity target, EntityAttribute attribute, UUID uuid, String name, double value, EntityAttributeModifier.Operation operation) throws CommandSyntaxException {
      EntityAttributeInstance entityAttributeInstance = getAttributeInstance(target, attribute);
      EntityAttributeModifier entityAttributeModifier = new EntityAttributeModifier(uuid, name, value, operation);
      if (entityAttributeInstance.hasModifier(entityAttributeModifier)) {
         throw MODIFIER_ALREADY_PRESENT_EXCEPTION.create(target.getName(), new TranslatableText(attribute.getTranslationKey()), uuid);
      } else {
         entityAttributeInstance.addPersistentModifier(entityAttributeModifier);
         source.sendFeedback(new TranslatableText("commands.attribute.modifier.add.success", new Object[]{uuid, new TranslatableText(attribute.getTranslationKey()), target.getName()}), false);
         return 1;
      }
   }

   private static int executeModifierRemove(ServerCommandSource source, Entity target, EntityAttribute attribute, UUID uuid) throws CommandSyntaxException {
      EntityAttributeInstance entityAttributeInstance = getAttributeInstance(target, attribute);
      if (entityAttributeInstance.tryRemoveModifier(uuid)) {
         source.sendFeedback(new TranslatableText("commands.attribute.modifier.remove.success", new Object[]{uuid, new TranslatableText(attribute.getTranslationKey()), target.getName()}), false);
         return 1;
      } else {
         throw NO_MODIFIER_EXCEPTION.create(target.getName(), new TranslatableText(attribute.getTranslationKey()), uuid);
      }
   }
}
