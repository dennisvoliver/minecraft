package net.minecraft.client.render.model.json;

import com.google.common.collect.Maps;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import java.lang.reflect.Type;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.item.ModelPredicateProvider;
import net.minecraft.client.item.ModelPredicateProviderRegistry;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class ModelOverride {
   private final Identifier modelId;
   private final Map<Identifier, Float> predicateToThresholds;

   public ModelOverride(Identifier modelId, Map<Identifier, Float> predicateToThresholds) {
      this.modelId = modelId;
      this.predicateToThresholds = predicateToThresholds;
   }

   public Identifier getModelId() {
      return this.modelId;
   }

   boolean matches(ItemStack stack, @Nullable ClientWorld world, @Nullable LivingEntity entity) {
      Item item = stack.getItem();
      Iterator var5 = this.predicateToThresholds.entrySet().iterator();

      Entry entry;
      ModelPredicateProvider modelPredicateProvider;
      do {
         if (!var5.hasNext()) {
            return true;
         }

         entry = (Entry)var5.next();
         modelPredicateProvider = ModelPredicateProviderRegistry.get(item, (Identifier)entry.getKey());
      } while(modelPredicateProvider != null && !(modelPredicateProvider.call(stack, world, entity) < (Float)entry.getValue()));

      return false;
   }

   @Environment(EnvType.CLIENT)
   public static class Deserializer implements JsonDeserializer<ModelOverride> {
      protected Deserializer() {
      }

      public ModelOverride deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
         JsonObject jsonObject = jsonElement.getAsJsonObject();
         Identifier identifier = new Identifier(JsonHelper.getString(jsonObject, "model"));
         Map<Identifier, Float> map = this.deserializeMinPropertyValues(jsonObject);
         return new ModelOverride(identifier, map);
      }

      protected Map<Identifier, Float> deserializeMinPropertyValues(JsonObject object) {
         Map<Identifier, Float> map = Maps.newLinkedHashMap();
         JsonObject jsonObject = JsonHelper.getObject(object, "predicate");
         Iterator var4 = jsonObject.entrySet().iterator();

         while(var4.hasNext()) {
            Entry<String, JsonElement> entry = (Entry)var4.next();
            map.put(new Identifier((String)entry.getKey()), JsonHelper.asFloat((JsonElement)entry.getValue(), (String)entry.getKey()));
         }

         return map;
      }
   }
}
