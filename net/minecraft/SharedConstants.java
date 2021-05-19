package net.minecraft;

import com.mojang.bridge.game.GameVersion;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.netty.util.ResourceLeakDetector;
import io.netty.util.ResourceLeakDetector.Level;
import java.time.Duration;
import net.minecraft.command.TranslatableBuiltInExceptions;

public class SharedConstants {
   public static final Level RESOURCE_LEAK_DETECTOR_DISABLED;
   public static final long field_22251;
   /**
    * Specifies whether Minecraft should use choice type registrations from the game's schema when entity types or block entity types are created.
    */
   public static boolean useChoiceTypeRegistrations;
   public static boolean isDevelopment;
   public static final char[] INVALID_CHARS_LEVEL_NAME;
   private static GameVersion gameVersion;

   public static boolean isValidChar(char chr) {
      return chr != 167 && chr >= ' ' && chr != 127;
   }

   public static String stripInvalidChars(String s) {
      StringBuilder stringBuilder = new StringBuilder();
      char[] var2 = s.toCharArray();
      int var3 = var2.length;

      for(int var4 = 0; var4 < var3; ++var4) {
         char c = var2[var4];
         if (isValidChar(c)) {
            stringBuilder.append(c);
         }
      }

      return stringBuilder.toString();
   }

   public static GameVersion getGameVersion() {
      if (gameVersion == null) {
         gameVersion = MinecraftVersion.create();
      }

      return gameVersion;
   }

   public static int method_31372() {
      return 754;
   }

   static {
      RESOURCE_LEAK_DETECTOR_DISABLED = Level.DISABLED;
      field_22251 = Duration.ofMillis(300L).toNanos();
      useChoiceTypeRegistrations = true;
      INVALID_CHARS_LEVEL_NAME = new char[]{'/', '\n', '\r', '\t', '\u0000', '\f', '`', '?', '*', '\\', '<', '>', '|', '"', ':'};
      ResourceLeakDetector.setLevel(RESOURCE_LEAK_DETECTOR_DISABLED);
      CommandSyntaxException.ENABLE_COMMAND_STACK_TRACES = false;
      CommandSyntaxException.BUILT_IN_EXCEPTIONS = new TranslatableBuiltInExceptions();
   }
}
