package net.minecraft.client.render.model;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.mojang.datafixers.util.Pair;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.model.json.ModelVariantMap;
import net.minecraft.client.render.model.json.MultipartModelComponent;
import net.minecraft.client.render.model.json.WeightedUnbakedModel;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.state.StateManager;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class MultipartUnbakedModel implements UnbakedModel {
   private final StateManager<Block, BlockState> stateFactory;
   private final List<MultipartModelComponent> components;

   public MultipartUnbakedModel(StateManager<Block, BlockState> stateFactory, List<MultipartModelComponent> components) {
      this.stateFactory = stateFactory;
      this.components = components;
   }

   public List<MultipartModelComponent> getComponents() {
      return this.components;
   }

   public Set<WeightedUnbakedModel> getModels() {
      Set<WeightedUnbakedModel> set = Sets.newHashSet();
      Iterator var2 = this.components.iterator();

      while(var2.hasNext()) {
         MultipartModelComponent multipartModelComponent = (MultipartModelComponent)var2.next();
         set.add(multipartModelComponent.getModel());
      }

      return set;
   }

   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (!(o instanceof MultipartUnbakedModel)) {
         return false;
      } else {
         MultipartUnbakedModel multipartUnbakedModel = (MultipartUnbakedModel)o;
         return Objects.equals(this.stateFactory, multipartUnbakedModel.stateFactory) && Objects.equals(this.components, multipartUnbakedModel.components);
      }
   }

   public int hashCode() {
      return Objects.hash(new Object[]{this.stateFactory, this.components});
   }

   public Collection<Identifier> getModelDependencies() {
      return (Collection)this.getComponents().stream().flatMap((multipartModelComponent) -> {
         return multipartModelComponent.getModel().getModelDependencies().stream();
      }).collect(Collectors.toSet());
   }

   public Collection<SpriteIdentifier> getTextureDependencies(Function<Identifier, UnbakedModel> unbakedModelGetter, Set<Pair<String, String>> unresolvedTextureReferences) {
      return (Collection)this.getComponents().stream().flatMap((multipartModelComponent) -> {
         return multipartModelComponent.getModel().getTextureDependencies(unbakedModelGetter, unresolvedTextureReferences).stream();
      }).collect(Collectors.toSet());
   }

   @Nullable
   public BakedModel bake(ModelLoader loader, Function<SpriteIdentifier, Sprite> textureGetter, ModelBakeSettings rotationContainer, Identifier modelId) {
      MultipartBakedModel.Builder builder = new MultipartBakedModel.Builder();
      Iterator var6 = this.getComponents().iterator();

      while(var6.hasNext()) {
         MultipartModelComponent multipartModelComponent = (MultipartModelComponent)var6.next();
         BakedModel bakedModel = multipartModelComponent.getModel().bake(loader, textureGetter, rotationContainer, modelId);
         if (bakedModel != null) {
            builder.addComponent(multipartModelComponent.getPredicate(this.stateFactory), bakedModel);
         }
      }

      return builder.build();
   }

   @Environment(EnvType.CLIENT)
   public static class Deserializer implements JsonDeserializer<MultipartUnbakedModel> {
      private final ModelVariantMap.DeserializationContext context;

      public Deserializer(ModelVariantMap.DeserializationContext context) {
         this.context = context;
      }

      public MultipartUnbakedModel deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
         return new MultipartUnbakedModel(this.context.getStateFactory(), this.deserializeComponents(jsonDeserializationContext, jsonElement.getAsJsonArray()));
      }

      private List<MultipartModelComponent> deserializeComponents(JsonDeserializationContext context, JsonArray array) {
         List<MultipartModelComponent> list = Lists.newArrayList();
         Iterator var4 = array.iterator();

         while(var4.hasNext()) {
            JsonElement jsonElement = (JsonElement)var4.next();
            list.add(context.deserialize(jsonElement, MultipartModelComponent.class));
         }

         return list;
      }
   }
}
