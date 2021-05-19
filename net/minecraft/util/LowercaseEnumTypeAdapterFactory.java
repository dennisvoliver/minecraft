package net.minecraft.util;

import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.util.Locale;
import java.util.Map;
import org.jetbrains.annotations.Nullable;

public class LowercaseEnumTypeAdapterFactory implements TypeAdapterFactory {
   @Nullable
   public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> typeToken) {
      Class<T> class_ = typeToken.getRawType();
      if (!class_.isEnum()) {
         return null;
      } else {
         final Map<String, T> map = Maps.newHashMap();
         Object[] var5 = class_.getEnumConstants();
         int var6 = var5.length;

         for(int var7 = 0; var7 < var6; ++var7) {
            T object = var5[var7];
            map.put(this.getKey(object), object);
         }

         return new TypeAdapter<T>() {
            public void write(JsonWriter jsonWriter, T object) throws IOException {
               if (object == null) {
                  jsonWriter.nullValue();
               } else {
                  jsonWriter.value(LowercaseEnumTypeAdapterFactory.this.getKey(object));
               }

            }

            @Nullable
            public T read(JsonReader jsonReader) throws IOException {
               if (jsonReader.peek() == JsonToken.NULL) {
                  jsonReader.nextNull();
                  return null;
               } else {
                  return map.get(jsonReader.nextString());
               }
            }
         };
      }
   }

   private String getKey(Object o) {
      return o instanceof Enum ? ((Enum)o).name().toLowerCase(Locale.ROOT) : o.toString().toLowerCase(Locale.ROOT);
   }
}
