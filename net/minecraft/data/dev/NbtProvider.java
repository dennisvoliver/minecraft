package net.minecraft.data.dev;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import net.minecraft.data.DataCache;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.text.Text;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

public class NbtProvider implements DataProvider {
   private static final Logger LOGGER = LogManager.getLogger();
   private final DataGenerator root;

   public NbtProvider(DataGenerator dataGenerator) {
      this.root = dataGenerator;
   }

   public void run(DataCache cache) throws IOException {
      Path path = this.root.getOutput();
      Iterator var3 = this.root.getInputs().iterator();

      while(var3.hasNext()) {
         Path path2 = (Path)var3.next();
         Files.walk(path2).filter((pathx) -> {
            return pathx.toString().endsWith(".nbt");
         }).forEach((path3) -> {
            convertNbtToSnbt(path3, this.getLocation(path2, path3), path);
         });
      }

   }

   public String getName() {
      return "NBT to SNBT";
   }

   private String getLocation(Path targetPath, Path rootPath) {
      String string = targetPath.relativize(rootPath).toString().replaceAll("\\\\", "/");
      return string.substring(0, string.length() - ".nbt".length());
   }

   @Nullable
   public static Path convertNbtToSnbt(Path inputPath, String location, Path outputPath) {
      try {
         CompoundTag compoundTag = NbtIo.readCompressed(Files.newInputStream(inputPath));
         Text text = compoundTag.toText("    ", 0);
         String string = text.getString() + "\n";
         Path path = outputPath.resolve(location + ".snbt");
         Files.createDirectories(path.getParent());
         BufferedWriter bufferedWriter = Files.newBufferedWriter(path);
         Throwable var8 = null;

         try {
            bufferedWriter.write(string);
         } catch (Throwable var18) {
            var8 = var18;
            throw var18;
         } finally {
            if (bufferedWriter != null) {
               if (var8 != null) {
                  try {
                     bufferedWriter.close();
                  } catch (Throwable var17) {
                     var8.addSuppressed(var17);
                  }
               } else {
                  bufferedWriter.close();
               }
            }

         }

         LOGGER.info((String)"Converted {} from NBT to SNBT", (Object)location);
         return path;
      } catch (IOException var20) {
         LOGGER.error((String)"Couldn't convert {} from NBT to SNBT at {}", (Object)location, inputPath, var20);
         return null;
      }
   }
}
