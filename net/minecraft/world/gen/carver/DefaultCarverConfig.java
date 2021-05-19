package net.minecraft.world.gen.carver;

import com.mojang.serialization.Codec;

public class DefaultCarverConfig implements CarverConfig {
   public static final Codec<DefaultCarverConfig> CODEC = Codec.unit(() -> {
      return INSTANCE;
   });
   public static final DefaultCarverConfig INSTANCE = new DefaultCarverConfig();
}
