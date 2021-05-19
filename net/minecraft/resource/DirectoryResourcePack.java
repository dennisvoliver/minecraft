package net.minecraft.resource;

import com.google.common.base.CharMatcher;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.function.Predicate;
import net.minecraft.util.Identifier;
import net.minecraft.util.InvalidIdentifierException;
import net.minecraft.util.Util;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

public class DirectoryResourcePack extends AbstractFileResourcePack {
   private static final Logger LOGGER = LogManager.getLogger();
   private static final boolean IS_WINDOWS;
   private static final CharMatcher BACKSLASH_MATCHER;

   public DirectoryResourcePack(File file) {
      super(file);
   }

   public static boolean isValidPath(File file, String filename) throws IOException {
      String string = file.getCanonicalPath();
      if (IS_WINDOWS) {
         string = BACKSLASH_MATCHER.replaceFrom(string, '/');
      }

      return string.endsWith(filename);
   }

   protected InputStream openFile(String name) throws IOException {
      File file = this.getFile(name);
      if (file == null) {
         throw new ResourceNotFoundException(this.base, name);
      } else {
         return new FileInputStream(file);
      }
   }

   protected boolean containsFile(String name) {
      return this.getFile(name) != null;
   }

   @Nullable
   private File getFile(String name) {
      try {
         File file = new File(this.base, name);
         if (file.isFile() && isValidPath(file, name)) {
            return file;
         }
      } catch (IOException var3) {
      }

      return null;
   }

   public Set<String> getNamespaces(ResourceType type) {
      Set<String> set = Sets.newHashSet();
      File file = new File(this.base, type.getDirectory());
      File[] files = file.listFiles(DirectoryFileFilter.DIRECTORY);
      if (files != null) {
         File[] var5 = files;
         int var6 = files.length;

         for(int var7 = 0; var7 < var6; ++var7) {
            File file2 = var5[var7];
            String string = relativize(file, file2);
            if (string.equals(string.toLowerCase(Locale.ROOT))) {
               set.add(string.substring(0, string.length() - 1));
            } else {
               this.warnNonLowerCaseNamespace(string);
            }
         }
      }

      return set;
   }

   public void close() {
   }

   public Collection<Identifier> findResources(ResourceType type, String namespace, String prefix, int maxDepth, Predicate<String> pathFilter) {
      File file = new File(this.base, type.getDirectory());
      List<Identifier> list = Lists.newArrayList();
      this.findFiles(new File(new File(file, namespace), prefix), maxDepth, namespace, list, prefix + "/", pathFilter);
      return list;
   }

   private void findFiles(File file, int maxDepth, String namespace, List<Identifier> found, String prefix, Predicate<String> pathFilter) {
      File[] files = file.listFiles();
      if (files != null) {
         File[] var8 = files;
         int var9 = files.length;

         for(int var10 = 0; var10 < var9; ++var10) {
            File file2 = var8[var10];
            if (file2.isDirectory()) {
               if (maxDepth > 0) {
                  this.findFiles(file2, maxDepth - 1, namespace, found, prefix + file2.getName() + "/", pathFilter);
               }
            } else if (!file2.getName().endsWith(".mcmeta") && pathFilter.test(file2.getName())) {
               try {
                  found.add(new Identifier(namespace, prefix + file2.getName()));
               } catch (InvalidIdentifierException var13) {
                  LOGGER.error(var13.getMessage());
               }
            }
         }
      }

   }

   static {
      IS_WINDOWS = Util.getOperatingSystem() == Util.OperatingSystem.WINDOWS;
      BACKSLASH_MATCHER = CharMatcher.is('\\');
   }
}
