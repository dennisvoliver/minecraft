package net.minecraft.data.server;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import net.minecraft.advancement.Advancement;
import net.minecraft.data.DataCache;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class AdvancementsProvider implements DataProvider {
   private static final Logger LOGGER = LogManager.getLogger();
   private static final Gson GSON = (new GsonBuilder()).setPrettyPrinting().create();
   private final DataGenerator root;
   private final List<Consumer<Consumer<Advancement>>> tabGenerators = ImmutableList.of(new EndTabAdvancementGenerator(), new HusbandryTabAdvancementGenerator(), new AdventureTabAdvancementGenerator(), new NetherTabAdvancementGenerator(), new StoryTabAdvancementGenerator());

   public AdvancementsProvider(DataGenerator root) {
      this.root = root;
   }

   public void run(DataCache cache) throws IOException {
      Path path = this.root.getOutput();
      Set<Identifier> set = Sets.newHashSet();
      Consumer<Advancement> consumer = (advancement) -> {
         if (!set.add(advancement.getId())) {
            throw new IllegalStateException("Duplicate advancement " + advancement.getId());
         } else {
            Path path2 = getOutput(path, advancement);

            try {
               DataProvider.writeToPath(GSON, cache, advancement.createTask().toJson(), path2);
            } catch (IOException var6) {
               LOGGER.error((String)"Couldn't save advancement {}", (Object)path2, (Object)var6);
            }

         }
      };
      Iterator var5 = this.tabGenerators.iterator();

      while(var5.hasNext()) {
         Consumer<Consumer<Advancement>> consumer2 = (Consumer)var5.next();
         consumer2.accept(consumer);
      }

   }

   private static Path getOutput(Path rootOutput, Advancement advancement) {
      return rootOutput.resolve("data/" + advancement.getId().getNamespace() + "/advancements/" + advancement.getId().getPath() + ".json");
   }

   public String getName() {
      return "Advancements";
   }
}
