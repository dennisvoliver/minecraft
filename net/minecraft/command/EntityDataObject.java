package net.minecraft.command;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.util.Locale;
import java.util.UUID;
import java.util.function.Function;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.argument.NbtPathArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.predicate.NbtPredicate;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.DataCommand;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;

public class EntityDataObject implements DataCommandObject {
   private static final SimpleCommandExceptionType INVALID_ENTITY_EXCEPTION = new SimpleCommandExceptionType(new TranslatableText("commands.data.entity.invalid"));
   public static final Function<String, DataCommand.ObjectType> TYPE_FACTORY = (string) -> {
      return new DataCommand.ObjectType() {
         public DataCommandObject getObject(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
            return new EntityDataObject(EntityArgumentType.getEntity(context, string));
         }

         public ArgumentBuilder<ServerCommandSource, ?> addArgumentsToBuilder(ArgumentBuilder<ServerCommandSource, ?> argument, Function<ArgumentBuilder<ServerCommandSource, ?>, ArgumentBuilder<ServerCommandSource, ?>> argumentAdder) {
            return argument.then(CommandManager.literal("entity").then((ArgumentBuilder)argumentAdder.apply(CommandManager.argument(string, EntityArgumentType.entity()))));
         }
      };
   };
   private final Entity entity;

   public EntityDataObject(Entity entity) {
      this.entity = entity;
   }

   public void setTag(CompoundTag tag) throws CommandSyntaxException {
      if (this.entity instanceof PlayerEntity) {
         throw INVALID_ENTITY_EXCEPTION.create();
      } else {
         UUID uUID = this.entity.getUuid();
         this.entity.fromTag(tag);
         this.entity.setUuid(uUID);
      }
   }

   public CompoundTag getTag() {
      return NbtPredicate.entityToTag(this.entity);
   }

   public Text feedbackModify() {
      return new TranslatableText("commands.data.entity.modified", new Object[]{this.entity.getDisplayName()});
   }

   public Text feedbackQuery(Tag tag) {
      return new TranslatableText("commands.data.entity.query", new Object[]{this.entity.getDisplayName(), tag.toText()});
   }

   public Text feedbackGet(NbtPathArgumentType.NbtPath nbtPath, double scale, int result) {
      return new TranslatableText("commands.data.entity.get", new Object[]{nbtPath, this.entity.getDisplayName(), String.format(Locale.ROOT, "%.2f", scale), result});
   }
}
