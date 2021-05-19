package net.minecraft.server.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.util.Collection;
import java.util.Iterator;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.argument.IdentifierArgumentType;
import net.minecraft.command.argument.Vec3ArgumentType;
import net.minecraft.command.suggestion.SuggestionProviders;
import net.minecraft.network.packet.s2c.play.PlaySoundIdS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class PlaySoundCommand {
   private static final SimpleCommandExceptionType FAILED_EXCEPTION = new SimpleCommandExceptionType(new TranslatableText("commands.playsound.failed"));

   public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
      RequiredArgumentBuilder<ServerCommandSource, Identifier> requiredArgumentBuilder = CommandManager.argument("sound", IdentifierArgumentType.identifier()).suggests(SuggestionProviders.AVAILABLE_SOUNDS);
      SoundCategory[] var2 = SoundCategory.values();
      int var3 = var2.length;

      for(int var4 = 0; var4 < var3; ++var4) {
         SoundCategory soundCategory = var2[var4];
         requiredArgumentBuilder.then(makeArgumentsForCategory(soundCategory));
      }

      dispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)CommandManager.literal("playsound").requires((serverCommandSource) -> {
         return serverCommandSource.hasPermissionLevel(2);
      })).then(requiredArgumentBuilder));
   }

   private static LiteralArgumentBuilder<ServerCommandSource> makeArgumentsForCategory(SoundCategory category) {
      return (LiteralArgumentBuilder)CommandManager.literal(category.getName()).then(((RequiredArgumentBuilder)CommandManager.argument("targets", EntityArgumentType.players()).executes((commandContext) -> {
         return execute((ServerCommandSource)commandContext.getSource(), EntityArgumentType.getPlayers(commandContext, "targets"), IdentifierArgumentType.getIdentifier(commandContext, "sound"), category, ((ServerCommandSource)commandContext.getSource()).getPosition(), 1.0F, 1.0F, 0.0F);
      })).then(((RequiredArgumentBuilder)CommandManager.argument("pos", Vec3ArgumentType.vec3()).executes((commandContext) -> {
         return execute((ServerCommandSource)commandContext.getSource(), EntityArgumentType.getPlayers(commandContext, "targets"), IdentifierArgumentType.getIdentifier(commandContext, "sound"), category, Vec3ArgumentType.getVec3(commandContext, "pos"), 1.0F, 1.0F, 0.0F);
      })).then(((RequiredArgumentBuilder)CommandManager.argument("volume", FloatArgumentType.floatArg(0.0F)).executes((commandContext) -> {
         return execute((ServerCommandSource)commandContext.getSource(), EntityArgumentType.getPlayers(commandContext, "targets"), IdentifierArgumentType.getIdentifier(commandContext, "sound"), category, Vec3ArgumentType.getVec3(commandContext, "pos"), (Float)commandContext.getArgument("volume", Float.class), 1.0F, 0.0F);
      })).then(((RequiredArgumentBuilder)CommandManager.argument("pitch", FloatArgumentType.floatArg(0.0F, 2.0F)).executes((commandContext) -> {
         return execute((ServerCommandSource)commandContext.getSource(), EntityArgumentType.getPlayers(commandContext, "targets"), IdentifierArgumentType.getIdentifier(commandContext, "sound"), category, Vec3ArgumentType.getVec3(commandContext, "pos"), (Float)commandContext.getArgument("volume", Float.class), (Float)commandContext.getArgument("pitch", Float.class), 0.0F);
      })).then(CommandManager.argument("minVolume", FloatArgumentType.floatArg(0.0F, 1.0F)).executes((commandContext) -> {
         return execute((ServerCommandSource)commandContext.getSource(), EntityArgumentType.getPlayers(commandContext, "targets"), IdentifierArgumentType.getIdentifier(commandContext, "sound"), category, Vec3ArgumentType.getVec3(commandContext, "pos"), (Float)commandContext.getArgument("volume", Float.class), (Float)commandContext.getArgument("pitch", Float.class), (Float)commandContext.getArgument("minVolume", Float.class));
      }))))));
   }

   private static int execute(ServerCommandSource source, Collection<ServerPlayerEntity> targets, Identifier sound, SoundCategory category, Vec3d pos, float volume, float pitch, float minVolume) throws CommandSyntaxException {
      double d = Math.pow(volume > 1.0F ? (double)(volume * 16.0F) : 16.0D, 2.0D);
      int i = 0;
      Iterator var11 = targets.iterator();

      while(true) {
         ServerPlayerEntity serverPlayerEntity;
         Vec3d vec3d;
         float j;
         while(true) {
            if (!var11.hasNext()) {
               if (i == 0) {
                  throw FAILED_EXCEPTION.create();
               }

               if (targets.size() == 1) {
                  source.sendFeedback(new TranslatableText("commands.playsound.success.single", new Object[]{sound, ((ServerPlayerEntity)targets.iterator().next()).getDisplayName()}), true);
               } else {
                  source.sendFeedback(new TranslatableText("commands.playsound.success.multiple", new Object[]{sound, targets.size()}), true);
               }

               return i;
            }

            serverPlayerEntity = (ServerPlayerEntity)var11.next();
            double e = pos.x - serverPlayerEntity.getX();
            double f = pos.y - serverPlayerEntity.getY();
            double g = pos.z - serverPlayerEntity.getZ();
            double h = e * e + f * f + g * g;
            vec3d = pos;
            j = volume;
            if (!(h > d)) {
               break;
            }

            if (!(minVolume <= 0.0F)) {
               double k = (double)MathHelper.sqrt(h);
               vec3d = new Vec3d(serverPlayerEntity.getX() + e / k * 2.0D, serverPlayerEntity.getY() + f / k * 2.0D, serverPlayerEntity.getZ() + g / k * 2.0D);
               j = minVolume;
               break;
            }
         }

         serverPlayerEntity.networkHandler.sendPacket(new PlaySoundIdS2CPacket(sound, category, vec3d, j, pitch));
         ++i;
      }
   }
}
