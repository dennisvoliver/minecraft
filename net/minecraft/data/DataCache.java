package net.minecraft.data;

import com.google.common.base.Charsets;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

public class DataCache {
   private static final Logger LOGGER = LogManager.getLogger();
   private final Path root;
   private final Path recordFile;
   private int unchanged;
   private final Map<Path, String> oldSha1 = Maps.newHashMap();
   private final Map<Path, String> newSha1 = Maps.newHashMap();
   private final Set<Path> ignores = Sets.newHashSet();

   public DataCache(Path path, String string) throws IOException {
      this.root = path;
      Path path2 = path.resolve(".cache");
      Files.createDirectories(path2);
      this.recordFile = path2.resolve(string);
      this.files().forEach((pathx) -> {
         String var10000 = (String)this.oldSha1.put(pathx, "");
      });
      if (Files.isReadable(this.recordFile)) {
         IOUtils.readLines(Files.newInputStream(this.recordFile), Charsets.UTF_8).forEach((stringx) -> {
            int i = stringx.indexOf(32);
            this.oldSha1.put(path.resolve(stringx.substring(i + 1)), stringx.substring(0, i));
         });
      }

   }

   public void write() throws IOException {
      this.deleteAll();

      BufferedWriter writer2;
      try {
         writer2 = Files.newBufferedWriter(this.recordFile);
      } catch (IOException var3) {
         LOGGER.warn((String)"Unable write cachefile {}: {}", (Object)this.recordFile, (Object)var3.toString());
         return;
      }

      IOUtils.writeLines((Collection)this.newSha1.entrySet().stream().map((entry) -> {
         return (String)entry.getValue() + ' ' + this.root.relativize((Path)entry.getKey());
      }).collect(Collectors.toList()), System.lineSeparator(), (Writer)writer2);
      writer2.close();
      LOGGER.debug((String)"Caching: cache hits: {}, created: {} removed: {}", (Object)this.unchanged, this.newSha1.size() - this.unchanged, this.oldSha1.size());
   }

   @Nullable
   public String getOldSha1(Path path) {
      return (String)this.oldSha1.get(path);
   }

   public void updateSha1(Path path, String string) {
      this.newSha1.put(path, string);
      if (Objects.equals(this.oldSha1.remove(path), string)) {
         ++this.unchanged;
      }

   }

   public boolean contains(Path path) {
      return this.oldSha1.containsKey(path);
   }

   public void ignore(Path path) {
      this.ignores.add(path);
   }

   private void deleteAll() throws IOException {
      this.files().forEach((path) -> {
         if (this.contains(path) && !this.ignores.contains(path)) {
            try {
               Files.delete(path);
            } catch (IOException var3) {
               LOGGER.debug((String)"Unable to delete: {} ({})", (Object)path, (Object)var3.toString());
            }
         }

      });
   }

   private Stream<Path> files() throws IOException {
      return Files.walk(this.root).filter((path) -> {
         return !Objects.equals(this.recordFile, path) && !Files.isDirectory(path, new LinkOption[0]);
      });
   }
}
