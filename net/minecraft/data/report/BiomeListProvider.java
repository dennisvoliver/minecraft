package net.minecraft.data.report;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.Optional;
import java.util.Map.Entry;
import java.util.function.Function;
import net.minecraft.data.DataCache;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.BuiltinRegistries;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.biome.Biome;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class BiomeListProvider implements DataProvider {
   private static final Logger LOGGER = LogManager.getLogger();
   private static final Gson GSON = (new GsonBuilder()).setPrettyPrinting().create();
   private final DataGenerator dataGenerator;

   public BiomeListProvider(DataGenerator dataGenerator) {
      this.dataGenerator = dataGenerator;
   }

   public void run(DataCache cache) {
      Path path = this.dataGenerator.getOutput();
      Iterator var3 = BuiltinRegistries.BIOME.getEntries().iterator();

      while(var3.hasNext()) {
         Entry<RegistryKey<Biome>, Biome> entry = (Entry)var3.next();
         Path path2 = getPath(path, ((RegistryKey)entry.getKey()).getValue());
         Biome biome = (Biome)entry.getValue();
         Function function = JsonOps.INSTANCE.withEncoder(Biome.REGISTRY_CODEC);

         try {
            Optional<JsonElement> optional = ((DataResult)function.apply(() -> {
               return biome;
            })).result();
            if (optional.isPresent()) {
               DataProvider.writeToPath(GSON, cache, (JsonElement)optional.get(), path2);
            } else {
               LOGGER.error((String)"Couldn't serialize biome {}", (Object)path2);
            }
         } catch (IOException var9) {
            LOGGER.error((String)"Couldn't save biome {}", (Object)path2, (Object)var9);
         }
      }

   }

   private static Path getPath(Path path, Identifier identifier) {
      return path.resolve("reports/biomes/" + identifier.getPath() + ".json");
   }

   public String getName() {
      return "Biomes";
   }
}
