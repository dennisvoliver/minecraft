package net.minecraft.text;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.entity.Entity;
import net.minecraft.server.command.ServerCommandSource;
import org.jetbrains.annotations.Nullable;

/**
 * A {@link Text} that needs to be parsed when it is loaded into the game.
 */
public interface ParsableText {
   MutableText parse(@Nullable ServerCommandSource source, @Nullable Entity sender, int depth) throws CommandSyntaxException;
}
