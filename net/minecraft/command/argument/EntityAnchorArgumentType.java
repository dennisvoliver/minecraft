package net.minecraft.command.argument;

import com.google.common.collect.Maps;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.Entity;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Util;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

public class EntityAnchorArgumentType implements ArgumentType<EntityAnchorArgumentType.EntityAnchor> {
   private static final Collection<String> EXAMPLES = Arrays.asList("eyes", "feet");
   private static final DynamicCommandExceptionType INVALID_ANCHOR_EXCEPTION = new DynamicCommandExceptionType((object) -> {
      return new TranslatableText("argument.anchor.invalid", new Object[]{object});
   });

   public static EntityAnchorArgumentType.EntityAnchor getEntityAnchor(CommandContext<ServerCommandSource> commandContext, String string) {
      return (EntityAnchorArgumentType.EntityAnchor)commandContext.getArgument(string, EntityAnchorArgumentType.EntityAnchor.class);
   }

   public static EntityAnchorArgumentType entityAnchor() {
      return new EntityAnchorArgumentType();
   }

   public EntityAnchorArgumentType.EntityAnchor parse(StringReader stringReader) throws CommandSyntaxException {
      int i = stringReader.getCursor();
      String string = stringReader.readUnquotedString();
      EntityAnchorArgumentType.EntityAnchor entityAnchor = EntityAnchorArgumentType.EntityAnchor.fromId(string);
      if (entityAnchor == null) {
         stringReader.setCursor(i);
         throw INVALID_ANCHOR_EXCEPTION.createWithContext(stringReader, string);
      } else {
         return entityAnchor;
      }
   }

   public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
      return CommandSource.suggestMatching((Iterable)EntityAnchorArgumentType.EntityAnchor.anchors.keySet(), builder);
   }

   public Collection<String> getExamples() {
      return EXAMPLES;
   }

   public static enum EntityAnchor {
      FEET("feet", (vec3d, entity) -> {
         return vec3d;
      }),
      EYES("eyes", (vec3d, entity) -> {
         return new Vec3d(vec3d.x, vec3d.y + (double)entity.getStandingEyeHeight(), vec3d.z);
      });

      private static final Map<String, EntityAnchorArgumentType.EntityAnchor> anchors = (Map)Util.make(Maps.newHashMap(), (hashMap) -> {
         EntityAnchorArgumentType.EntityAnchor[] var1 = values();
         int var2 = var1.length;

         for(int var3 = 0; var3 < var2; ++var3) {
            EntityAnchorArgumentType.EntityAnchor entityAnchor = var1[var3];
            hashMap.put(entityAnchor.id, entityAnchor);
         }

      });
      private final String id;
      private final BiFunction<Vec3d, Entity, Vec3d> offset;

      private EntityAnchor(String id, BiFunction<Vec3d, Entity, Vec3d> offset) {
         this.id = id;
         this.offset = offset;
      }

      @Nullable
      public static EntityAnchorArgumentType.EntityAnchor fromId(String id) {
         return (EntityAnchorArgumentType.EntityAnchor)anchors.get(id);
      }

      public Vec3d positionAt(Entity entity) {
         return (Vec3d)this.offset.apply(entity.getPos(), entity);
      }

      public Vec3d positionAt(ServerCommandSource serverCommandSource) {
         Entity entity = serverCommandSource.getEntity();
         return entity == null ? serverCommandSource.getPosition() : (Vec3d)this.offset.apply(serverCommandSource.getPosition(), entity);
      }
   }
}
