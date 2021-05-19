package net.minecraft.recipe;

import com.google.gson.JsonObject;
import java.util.function.Function;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

public class SpecialRecipeSerializer<T extends Recipe<?>> implements RecipeSerializer<T> {
   private final Function<Identifier, T> id;

   public SpecialRecipeSerializer(Function<Identifier, T> function) {
      this.id = function;
   }

   public T read(Identifier id, JsonObject json) {
      return (Recipe)this.id.apply(id);
   }

   public T read(Identifier id, PacketByteBuf buf) {
      return (Recipe)this.id.apply(id);
   }

   public void write(PacketByteBuf buf, T recipe) {
   }
}
