package net.minecraft.util;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSyntaxException;
import java.lang.reflect.Type;
import java.util.function.Function;
import net.minecraft.util.registry.Registry;
import org.jetbrains.annotations.Nullable;

public class JsonSerializing {
   public static <E, T extends JsonSerializableType<E>> JsonSerializing.TypeHandler<E, T> createTypeHandler(Registry<T> registry, String rootFieldName, String idFieldName, Function<E, T> typeIdentification) {
      return new JsonSerializing.TypeHandler(registry, rootFieldName, idFieldName, typeIdentification);
   }

   public interface CustomSerializer<T> {
      JsonElement toJson(T object, JsonSerializationContext context);

      T fromJson(JsonElement json, JsonDeserializationContext context);
   }

   static class GsonSerializer<E, T extends JsonSerializableType<E>> implements JsonDeserializer<E>, com.google.gson.JsonSerializer<E> {
      private final Registry<T> registry;
      private final String rootFieldName;
      private final String idFieldName;
      private final Function<E, T> typeIdentification;
      @Nullable
      private final com.mojang.datafixers.util.Pair<T, JsonSerializing.CustomSerializer<? extends E>> elementSerializer;

      private GsonSerializer(Registry<T> registry, String rootFieldName, String idFieldName, Function<E, T> typeIdentification, @Nullable com.mojang.datafixers.util.Pair<T, JsonSerializing.CustomSerializer<? extends E>> pair) {
         this.registry = registry;
         this.rootFieldName = rootFieldName;
         this.idFieldName = idFieldName;
         this.typeIdentification = typeIdentification;
         this.elementSerializer = pair;
      }

      public E deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
         if (jsonElement.isJsonObject()) {
            JsonObject jsonObject = JsonHelper.asObject(jsonElement, this.rootFieldName);
            Identifier identifier = new Identifier(JsonHelper.getString(jsonObject, this.idFieldName));
            T jsonSerializableType = (JsonSerializableType)this.registry.get(identifier);
            if (jsonSerializableType == null) {
               throw new JsonSyntaxException("Unknown type '" + identifier + "'");
            } else {
               return jsonSerializableType.getJsonSerializer().fromJson(jsonObject, jsonDeserializationContext);
            }
         } else if (this.elementSerializer == null) {
            throw new UnsupportedOperationException("Object " + jsonElement + " can't be deserialized");
         } else {
            return ((JsonSerializing.CustomSerializer)this.elementSerializer.getSecond()).fromJson(jsonElement, jsonDeserializationContext);
         }
      }

      public JsonElement serialize(E object, Type type, JsonSerializationContext jsonSerializationContext) {
         T jsonSerializableType = (JsonSerializableType)this.typeIdentification.apply(object);
         if (this.elementSerializer != null && this.elementSerializer.getFirst() == jsonSerializableType) {
            return ((JsonSerializing.CustomSerializer)this.elementSerializer.getSecond()).toJson(object, jsonSerializationContext);
         } else if (jsonSerializableType == null) {
            throw new JsonSyntaxException("Unknown type: " + object);
         } else {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty(this.idFieldName, this.registry.getId(jsonSerializableType).toString());
            jsonSerializableType.getJsonSerializer().toJson(jsonObject, object, jsonSerializationContext);
            return jsonObject;
         }
      }
   }

   /**
    * A handler of JSON serializable types that can either obtain a type from
    * a registry to handle JSON conversion or handle with a custom logic bound
    * to a type.
    * 
    * <p>When the root element read is an object, the handler obtains the type
    * from registry to handle reading; otherwise, it falls back to custom
    * logic.</p>
    */
   public static class TypeHandler<E, T extends JsonSerializableType<E>> {
      private final Registry<T> registry;
      private final String rootFieldName;
      private final String idFieldName;
      private final Function<E, T> typeIdentification;
      @Nullable
      private com.mojang.datafixers.util.Pair<T, JsonSerializing.CustomSerializer<? extends E>> customSerializer;

      private TypeHandler(Registry<T> registry, String rootFieldName, String idFieldName, Function<E, T> typeIdentification) {
         this.registry = registry;
         this.rootFieldName = rootFieldName;
         this.idFieldName = idFieldName;
         this.typeIdentification = typeIdentification;
      }

      public Object createGsonSerializer() {
         return new JsonSerializing.GsonSerializer(this.registry, this.rootFieldName, this.idFieldName, this.typeIdentification, this.customSerializer);
      }
   }
}
