package net.minecraft.util;

import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.SharedConstants;

public class FileNameUtil {
   private static final Pattern FILE_NAME_WITH_COUNT = Pattern.compile("(<name>.*) \\((<count>\\d*)\\)", 66);
   private static final Pattern RESERVED_WINDOWS_NAMES = Pattern.compile(".*\\.|(?:COM|CLOCK\\$|CON|PRN|AUX|NUL|COM[1-9]|LPT[1-9])(?:\\..*)?", 2);

   @Environment(EnvType.CLIENT)
   public static String getNextUniqueName(Path path, String name, String extension) throws IOException {
      char[] var3 = SharedConstants.INVALID_CHARS_LEVEL_NAME;
      int i = var3.length;

      for(int var5 = 0; var5 < i; ++var5) {
         char c = var3[var5];
         name = name.replace(c, '_');
      }

      name = name.replaceAll("[./\"]", "_");
      if (RESERVED_WINDOWS_NAMES.matcher(name).matches()) {
         name = "_" + name + "_";
      }

      Matcher matcher = FILE_NAME_WITH_COUNT.matcher(name);
      i = 0;
      if (matcher.matches()) {
         name = matcher.group("name");
         i = Integer.parseInt(matcher.group("count"));
      }

      if (name.length() > 255 - extension.length()) {
         name = name.substring(0, 255 - extension.length());
      }

      while(true) {
         String string = name;
         if (i != 0) {
            String string2 = " (" + i + ")";
            int j = 255 - string2.length();
            if (name.length() > j) {
               string = name.substring(0, j);
            }

            string = string + string2;
         }

         string = string + extension;
         Path path2 = path.resolve(string);

         try {
            Path path3 = Files.createDirectory(path2);
            Files.deleteIfExists(path3);
            return path.relativize(path3).toString();
         } catch (FileAlreadyExistsException var8) {
            ++i;
         }
      }
   }

   public static boolean isNormal(Path path) {
      Path path2 = path.normalize();
      return path2.equals(path);
   }

   public static boolean isAllowedName(Path path) {
      Iterator var1 = path.iterator();

      Path path2;
      do {
         if (!var1.hasNext()) {
            return true;
         }

         path2 = (Path)var1.next();
      } while(!RESERVED_WINDOWS_NAMES.matcher(path2.toString()).matches());

      return false;
   }

   public static Path getResourcePath(Path path, String resourceName, String extension) {
      String string = resourceName + extension;
      Path path2 = Paths.get(string);
      if (path2.endsWith(extension)) {
         throw new InvalidPathException(string, "empty resource name");
      } else {
         return path.resolve(path2);
      }
   }
}
