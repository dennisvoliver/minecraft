package net.minecraft.command.argument.serialize;

import com.google.gson.JsonObject;
import com.mojang.brigadier.arguments.ArgumentType;
import java.util.function.Supplier;
import net.minecraft.network.PacketByteBuf;

public class ConstantArgumentSerializer<T extends ArgumentType<?>> implements ArgumentSerializer<T> {
   private final Supplier<T> supplier;

   public ConstantArgumentSerializer(Supplier<T> supplier) {
      this.supplier = supplier;
   }

   public void toPacket(T argumentType, PacketByteBuf packetByteBuf) {
   }

   public T fromPacket(PacketByteBuf packetByteBuf) {
      return (ArgumentType)this.supplier.get();
   }

   public void toJson(T argumentType, JsonObject jsonObject) {
   }
}
