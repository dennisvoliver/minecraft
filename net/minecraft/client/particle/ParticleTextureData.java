package net.minecraft.client.particle;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Streams;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class ParticleTextureData {
   @Nullable
   private final List<Identifier> textureList;

   private ParticleTextureData(@Nullable List<Identifier> textureList) {
      this.textureList = textureList;
   }

   @Nullable
   public List<Identifier> getTextureList() {
      return this.textureList;
   }

   public static ParticleTextureData load(JsonObject jsonObject) {
      JsonArray jsonArray = JsonHelper.getArray(jsonObject, "textures", (JsonArray)null);
      List list2;
      if (jsonArray != null) {
         list2 = (List)Streams.stream((Iterable)jsonArray).map((jsonElement) -> {
            return JsonHelper.asString(jsonElement, "texture");
         }).map(Identifier::new).collect(ImmutableList.toImmutableList());
      } else {
         list2 = null;
      }

      return new ParticleTextureData(list2);
   }
}
