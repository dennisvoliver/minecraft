package net.minecraft.util.registry;

import com.mojang.serialization.Lifecycle;
import java.util.Optional;
import java.util.Random;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DefaultedRegistry<T> extends SimpleRegistry<T> {
   private final Identifier defaultId;
   private T defaultValue;

   public DefaultedRegistry(String defaultId, RegistryKey<? extends Registry<T>> registryKey, Lifecycle lifecycle) {
      super(registryKey, lifecycle);
      this.defaultId = new Identifier(defaultId);
   }

   public <V extends T> V set(int rawId, RegistryKey<T> key, V entry, Lifecycle lifecycle) {
      if (this.defaultId.equals(key.getValue())) {
         this.defaultValue = entry;
      }

      return super.set(rawId, key, entry, lifecycle);
   }

   public int getRawId(@Nullable T entry) {
      int i = super.getRawId(entry);
      return i == -1 ? super.getRawId(this.defaultValue) : i;
   }

   @NotNull
   public Identifier getId(T entry) {
      Identifier identifier = super.getId(entry);
      return identifier == null ? this.defaultId : identifier;
   }

   @NotNull
   public T get(@Nullable Identifier id) {
      T object = super.get(id);
      return object == null ? this.defaultValue : object;
   }

   public Optional<T> getOrEmpty(@Nullable Identifier id) {
      return Optional.ofNullable(super.get(id));
   }

   @NotNull
   public T get(int index) {
      T object = super.get(index);
      return object == null ? this.defaultValue : object;
   }

   @NotNull
   public T getRandom(Random random) {
      T object = super.getRandom(random);
      return object == null ? this.defaultValue : object;
   }

   public Identifier getDefaultId() {
      return this.defaultId;
   }
}
