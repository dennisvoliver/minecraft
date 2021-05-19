package net.minecraft.server;

import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import net.minecraft.advancement.Advancement;
import net.minecraft.advancement.AdvancementManager;
import net.minecraft.advancement.AdvancementPositioner;
import net.minecraft.loot.condition.LootConditionManager;
import net.minecraft.predicate.entity.AdvancementEntityPredicateDeserializer;
import net.minecraft.resource.JsonDataLoader;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.profiler.Profiler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

public class ServerAdvancementLoader extends JsonDataLoader {
   private static final Logger LOGGER = LogManager.getLogger();
   private static final Gson GSON = (new GsonBuilder()).create();
   private AdvancementManager manager = new AdvancementManager();
   private final LootConditionManager conditionManager;

   public ServerAdvancementLoader(LootConditionManager conditionManager) {
      super(GSON, "advancements");
      this.conditionManager = conditionManager;
   }

   protected void apply(Map<Identifier, JsonElement> map, ResourceManager resourceManager, Profiler profiler) {
      Map<Identifier, Advancement.Task> map2 = Maps.newHashMap();
      map.forEach((identifier, jsonElement) -> {
         try {
            JsonObject jsonObject = JsonHelper.asObject(jsonElement, "advancement");
            Advancement.Task task = Advancement.Task.fromJson(jsonObject, new AdvancementEntityPredicateDeserializer(identifier, this.conditionManager));
            map2.put(identifier, task);
         } catch (IllegalArgumentException | JsonParseException var6) {
            LOGGER.error((String)"Parsing error loading custom advancement {}: {}", (Object)identifier, (Object)var6.getMessage());
         }

      });
      AdvancementManager advancementManager = new AdvancementManager();
      advancementManager.load(map2);
      Iterator var6 = advancementManager.getRoots().iterator();

      while(var6.hasNext()) {
         Advancement advancement = (Advancement)var6.next();
         if (advancement.getDisplay() != null) {
            AdvancementPositioner.arrangeForTree(advancement);
         }
      }

      this.manager = advancementManager;
   }

   @Nullable
   public Advancement get(Identifier id) {
      return this.manager.get(id);
   }

   public Collection<Advancement> getAdvancements() {
      return this.manager.getAdvancements();
   }
}
