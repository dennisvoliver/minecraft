package net.minecraft.server.command;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;
import net.minecraft.command.BlockDataObject;
import net.minecraft.command.DataCommandObject;
import net.minecraft.command.EntityDataObject;
import net.minecraft.command.StorageDataObject;
import net.minecraft.command.argument.NbtCompoundTagArgumentType;
import net.minecraft.command.argument.NbtPathArgumentType;
import net.minecraft.command.argument.NbtTagArgumentType;
import net.minecraft.nbt.AbstractListTag;
import net.minecraft.nbt.AbstractNumberTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.math.MathHelper;

public class DataCommand {
   private static final SimpleCommandExceptionType MERGE_FAILED_EXCEPTION = new SimpleCommandExceptionType(new TranslatableText("commands.data.merge.failed"));
   private static final DynamicCommandExceptionType GET_INVALID_EXCEPTION = new DynamicCommandExceptionType((object) -> {
      return new TranslatableText("commands.data.get.invalid", new Object[]{object});
   });
   private static final DynamicCommandExceptionType GET_UNKNOWN_EXCEPTION = new DynamicCommandExceptionType((object) -> {
      return new TranslatableText("commands.data.get.unknown", new Object[]{object});
   });
   private static final SimpleCommandExceptionType GET_MULTIPLE_EXCEPTION = new SimpleCommandExceptionType(new TranslatableText("commands.data.get.multiple"));
   private static final DynamicCommandExceptionType MODIFY_EXPECTED_LIST_EXCEPTION = new DynamicCommandExceptionType((object) -> {
      return new TranslatableText("commands.data.modify.expected_list", new Object[]{object});
   });
   private static final DynamicCommandExceptionType MODIFY_EXPECTED_OBJECT_EXCEPTION = new DynamicCommandExceptionType((object) -> {
      return new TranslatableText("commands.data.modify.expected_object", new Object[]{object});
   });
   private static final DynamicCommandExceptionType MODIFY_INVALID_INDEX_EXCEPTION = new DynamicCommandExceptionType((object) -> {
      return new TranslatableText("commands.data.modify.invalid_index", new Object[]{object});
   });
   public static final List<Function<String, DataCommand.ObjectType>> OBJECT_TYPE_FACTORIES;
   public static final List<DataCommand.ObjectType> TARGET_OBJECT_TYPES;
   public static final List<DataCommand.ObjectType> SOURCE_OBJECT_TYPES;

   public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
      LiteralArgumentBuilder<ServerCommandSource> literalArgumentBuilder = (LiteralArgumentBuilder)CommandManager.literal("data").requires((serverCommandSource) -> {
         return serverCommandSource.hasPermissionLevel(2);
      });
      Iterator var2 = TARGET_OBJECT_TYPES.iterator();

      while(var2.hasNext()) {
         DataCommand.ObjectType objectType = (DataCommand.ObjectType)var2.next();
         ((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)literalArgumentBuilder.then(objectType.addArgumentsToBuilder(CommandManager.literal("merge"), (argumentBuilder) -> {
            return argumentBuilder.then(CommandManager.argument("nbt", NbtCompoundTagArgumentType.nbtCompound()).executes((commandContext) -> {
               return executeMerge((ServerCommandSource)commandContext.getSource(), objectType.getObject(commandContext), NbtCompoundTagArgumentType.getCompoundTag(commandContext, "nbt"));
            }));
         }))).then(objectType.addArgumentsToBuilder(CommandManager.literal("get"), (argumentBuilder) -> {
            return argumentBuilder.executes((commandContext) -> {
               return executeGet((ServerCommandSource)commandContext.getSource(), objectType.getObject(commandContext));
            }).then(((RequiredArgumentBuilder)CommandManager.argument("path", NbtPathArgumentType.nbtPath()).executes((commandContext) -> {
               return executeGet((ServerCommandSource)commandContext.getSource(), objectType.getObject(commandContext), NbtPathArgumentType.getNbtPath(commandContext, "path"));
            })).then(CommandManager.argument("scale", DoubleArgumentType.doubleArg()).executes((commandContext) -> {
               return executeGet((ServerCommandSource)commandContext.getSource(), objectType.getObject(commandContext), NbtPathArgumentType.getNbtPath(commandContext, "path"), DoubleArgumentType.getDouble(commandContext, "scale"));
            })));
         }))).then(objectType.addArgumentsToBuilder(CommandManager.literal("remove"), (argumentBuilder) -> {
            return argumentBuilder.then(CommandManager.argument("path", NbtPathArgumentType.nbtPath()).executes((commandContext) -> {
               return executeRemove((ServerCommandSource)commandContext.getSource(), objectType.getObject(commandContext), NbtPathArgumentType.getNbtPath(commandContext, "path"));
            }));
         }))).then(addModifyArgument((argumentBuilder, modifyArgumentCreator) -> {
            argumentBuilder.then(CommandManager.literal("insert").then(CommandManager.argument("index", IntegerArgumentType.integer()).then(modifyArgumentCreator.create((commandContext, compoundTag, nbtPath, list) -> {
               int i = IntegerArgumentType.getInteger(commandContext, "index");
               return executeInsert(i, compoundTag, nbtPath, list);
            })))).then(CommandManager.literal("prepend").then(modifyArgumentCreator.create((commandContext, compoundTag, nbtPath, list) -> {
               return executeInsert(0, compoundTag, nbtPath, list);
            }))).then(CommandManager.literal("append").then(modifyArgumentCreator.create((commandContext, compoundTag, nbtPath, list) -> {
               return executeInsert(-1, compoundTag, nbtPath, list);
            }))).then(CommandManager.literal("set").then(modifyArgumentCreator.create((commandContext, compoundTag, nbtPath, list) -> {
               Tag var10002 = (Tag)Iterables.getLast(list);
               var10002.getClass();
               return nbtPath.put(compoundTag, var10002::copy);
            }))).then(CommandManager.literal("merge").then(modifyArgumentCreator.create((commandContext, compoundTag, nbtPath, list) -> {
               Collection<Tag> collection = nbtPath.getOrInit(compoundTag, CompoundTag::new);
               int i = 0;

               CompoundTag compoundTag2;
               CompoundTag compoundTag3;
               for(Iterator var6 = collection.iterator(); var6.hasNext(); i += compoundTag3.equals(compoundTag2) ? 0 : 1) {
                  Tag tag = (Tag)var6.next();
                  if (!(tag instanceof CompoundTag)) {
                     throw MODIFY_EXPECTED_OBJECT_EXCEPTION.create(tag);
                  }

                  compoundTag2 = (CompoundTag)tag;
                  compoundTag3 = compoundTag2.copy();
                  Iterator var10 = list.iterator();

                  while(var10.hasNext()) {
                     Tag tag2 = (Tag)var10.next();
                     if (!(tag2 instanceof CompoundTag)) {
                        throw MODIFY_EXPECTED_OBJECT_EXCEPTION.create(tag2);
                     }

                     compoundTag2.copyFrom((CompoundTag)tag2);
                  }
               }

               return i;
            })));
         }));
      }

      dispatcher.register(literalArgumentBuilder);
   }

   private static int executeInsert(int integer, CompoundTag sourceTag, NbtPathArgumentType.NbtPath path, List<Tag> tags) throws CommandSyntaxException {
      Collection<Tag> collection = path.getOrInit(sourceTag, ListTag::new);
      int i = 0;

      boolean bl;
      for(Iterator var6 = collection.iterator(); var6.hasNext(); i += bl ? 1 : 0) {
         Tag tag = (Tag)var6.next();
         if (!(tag instanceof AbstractListTag)) {
            throw MODIFY_EXPECTED_LIST_EXCEPTION.create(tag);
         }

         bl = false;
         AbstractListTag<?> abstractListTag = (AbstractListTag)tag;
         int j = integer < 0 ? abstractListTag.size() + integer + 1 : integer;
         Iterator var11 = tags.iterator();

         while(var11.hasNext()) {
            Tag tag2 = (Tag)var11.next();

            try {
               if (abstractListTag.addTag(j, tag2.copy())) {
                  ++j;
                  bl = true;
               }
            } catch (IndexOutOfBoundsException var14) {
               throw MODIFY_INVALID_INDEX_EXCEPTION.create(j);
            }
         }
      }

      return i;
   }

   private static ArgumentBuilder<ServerCommandSource, ?> addModifyArgument(BiConsumer<ArgumentBuilder<ServerCommandSource, ?>, DataCommand.ModifyArgumentCreator> subArgumentAdder) {
      LiteralArgumentBuilder<ServerCommandSource> literalArgumentBuilder = CommandManager.literal("modify");
      Iterator var2 = TARGET_OBJECT_TYPES.iterator();

      while(var2.hasNext()) {
         DataCommand.ObjectType objectType = (DataCommand.ObjectType)var2.next();
         objectType.addArgumentsToBuilder(literalArgumentBuilder, (argumentBuilder) -> {
            ArgumentBuilder<ServerCommandSource, ?> argumentBuilder2 = CommandManager.argument("targetPath", NbtPathArgumentType.nbtPath());
            Iterator var4 = SOURCE_OBJECT_TYPES.iterator();

            while(var4.hasNext()) {
               DataCommand.ObjectType objectType2 = (DataCommand.ObjectType)var4.next();
               subArgumentAdder.accept(argumentBuilder2, (modifyOperation) -> {
                  return objectType2.addArgumentsToBuilder(CommandManager.literal("from"), (argumentBuilder) -> {
                     return argumentBuilder.executes((commandContext) -> {
                        List<Tag> list = Collections.singletonList(objectType2.getObject(commandContext).getTag());
                        return executeModify(commandContext, objectType, modifyOperation, list);
                     }).then(CommandManager.argument("sourcePath", NbtPathArgumentType.nbtPath()).executes((commandContext) -> {
                        DataCommandObject dataCommandObject = objectType2.getObject(commandContext);
                        NbtPathArgumentType.NbtPath nbtPath = NbtPathArgumentType.getNbtPath(commandContext, "sourcePath");
                        List<Tag> list = nbtPath.get(dataCommandObject.getTag());
                        return executeModify(commandContext, objectType, modifyOperation, list);
                     }));
                  });
               });
            }

            subArgumentAdder.accept(argumentBuilder2, (modifyOperation) -> {
               return (LiteralArgumentBuilder)CommandManager.literal("value").then(CommandManager.argument("value", NbtTagArgumentType.nbtTag()).executes((commandContext) -> {
                  List<Tag> list = Collections.singletonList(NbtTagArgumentType.getTag(commandContext, "value"));
                  return executeModify(commandContext, objectType, modifyOperation, list);
               }));
            });
            return argumentBuilder.then((ArgumentBuilder)argumentBuilder2);
         });
      }

      return literalArgumentBuilder;
   }

   private static int executeModify(CommandContext<ServerCommandSource> context, DataCommand.ObjectType objectType, DataCommand.ModifyOperation modifier, List<Tag> tags) throws CommandSyntaxException {
      DataCommandObject dataCommandObject = objectType.getObject(context);
      NbtPathArgumentType.NbtPath nbtPath = NbtPathArgumentType.getNbtPath(context, "targetPath");
      CompoundTag compoundTag = dataCommandObject.getTag();
      int i = modifier.modify(context, compoundTag, nbtPath, tags);
      if (i == 0) {
         throw MERGE_FAILED_EXCEPTION.create();
      } else {
         dataCommandObject.setTag(compoundTag);
         ((ServerCommandSource)context.getSource()).sendFeedback(dataCommandObject.feedbackModify(), true);
         return i;
      }
   }

   private static int executeRemove(ServerCommandSource source, DataCommandObject object, NbtPathArgumentType.NbtPath path) throws CommandSyntaxException {
      CompoundTag compoundTag = object.getTag();
      int i = path.remove(compoundTag);
      if (i == 0) {
         throw MERGE_FAILED_EXCEPTION.create();
      } else {
         object.setTag(compoundTag);
         source.sendFeedback(object.feedbackModify(), true);
         return i;
      }
   }

   private static Tag getTag(NbtPathArgumentType.NbtPath path, DataCommandObject object) throws CommandSyntaxException {
      Collection<Tag> collection = path.get(object.getTag());
      Iterator<Tag> iterator = collection.iterator();
      Tag tag = (Tag)iterator.next();
      if (iterator.hasNext()) {
         throw GET_MULTIPLE_EXCEPTION.create();
      } else {
         return tag;
      }
   }

   private static int executeGet(ServerCommandSource source, DataCommandObject object, NbtPathArgumentType.NbtPath path) throws CommandSyntaxException {
      Tag tag = getTag(path, object);
      int m;
      if (tag instanceof AbstractNumberTag) {
         m = MathHelper.floor(((AbstractNumberTag)tag).getDouble());
      } else if (tag instanceof AbstractListTag) {
         m = ((AbstractListTag)tag).size();
      } else if (tag instanceof CompoundTag) {
         m = ((CompoundTag)tag).getSize();
      } else {
         if (!(tag instanceof StringTag)) {
            throw GET_UNKNOWN_EXCEPTION.create(path.toString());
         }

         m = tag.asString().length();
      }

      source.sendFeedback(object.feedbackQuery(tag), false);
      return m;
   }

   private static int executeGet(ServerCommandSource source, DataCommandObject object, NbtPathArgumentType.NbtPath path, double scale) throws CommandSyntaxException {
      Tag tag = getTag(path, object);
      if (!(tag instanceof AbstractNumberTag)) {
         throw GET_INVALID_EXCEPTION.create(path.toString());
      } else {
         int i = MathHelper.floor(((AbstractNumberTag)tag).getDouble() * scale);
         source.sendFeedback(object.feedbackGet(path, scale, i), false);
         return i;
      }
   }

   private static int executeGet(ServerCommandSource source, DataCommandObject object) throws CommandSyntaxException {
      source.sendFeedback(object.feedbackQuery(object.getTag()), false);
      return 1;
   }

   private static int executeMerge(ServerCommandSource source, DataCommandObject object, CompoundTag tag) throws CommandSyntaxException {
      CompoundTag compoundTag = object.getTag();
      CompoundTag compoundTag2 = compoundTag.copy().copyFrom(tag);
      if (compoundTag.equals(compoundTag2)) {
         throw MERGE_FAILED_EXCEPTION.create();
      } else {
         object.setTag(compoundTag2);
         source.sendFeedback(object.feedbackModify(), true);
         return 1;
      }
   }

   static {
      OBJECT_TYPE_FACTORIES = ImmutableList.of(EntityDataObject.TYPE_FACTORY, BlockDataObject.TYPE_FACTORY, StorageDataObject.TYPE_FACTORY);
      TARGET_OBJECT_TYPES = (List)OBJECT_TYPE_FACTORIES.stream().map((function) -> {
         return (DataCommand.ObjectType)function.apply("target");
      }).collect(ImmutableList.toImmutableList());
      SOURCE_OBJECT_TYPES = (List)OBJECT_TYPE_FACTORIES.stream().map((function) -> {
         return (DataCommand.ObjectType)function.apply("source");
      }).collect(ImmutableList.toImmutableList());
   }

   public interface ObjectType {
      DataCommandObject getObject(CommandContext<ServerCommandSource> context) throws CommandSyntaxException;

      ArgumentBuilder<ServerCommandSource, ?> addArgumentsToBuilder(ArgumentBuilder<ServerCommandSource, ?> argument, Function<ArgumentBuilder<ServerCommandSource, ?>, ArgumentBuilder<ServerCommandSource, ?>> argumentAdder);
   }

   interface ModifyArgumentCreator {
      ArgumentBuilder<ServerCommandSource, ?> create(DataCommand.ModifyOperation modifier);
   }

   interface ModifyOperation {
      int modify(CommandContext<ServerCommandSource> context, CompoundTag sourceTag, NbtPathArgumentType.NbtPath path, List<Tag> tags) throws CommandSyntaxException;
   }
}
