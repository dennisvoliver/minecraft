package net.minecraft.world.gen.decorator;

import com.mojang.serialization.Codec;

public class NopeDecoratorConfig implements DecoratorConfig {
   public static final Codec<NopeDecoratorConfig> CODEC = Codec.unit(() -> {
      return INSTANCE;
   });
   public static final NopeDecoratorConfig INSTANCE = new NopeDecoratorConfig();
}
