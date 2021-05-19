package net.minecraft.world.gen.decorator;

import com.mojang.datafixers.util.Function3;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public class RangeDecoratorConfig implements DecoratorConfig {
   public static final Codec<RangeDecoratorConfig> CODEC = RecordCodecBuilder.create((instance) -> {
      return instance.group(Codec.INT.fieldOf("bottom_offset").orElse(0).forGetter((rangeDecoratorConfig) -> {
         return rangeDecoratorConfig.bottomOffset;
      }), Codec.INT.fieldOf("top_offset").orElse(0).forGetter((rangeDecoratorConfig) -> {
         return rangeDecoratorConfig.topOffset;
      }), Codec.INT.fieldOf("maximum").orElse(0).forGetter((rangeDecoratorConfig) -> {
         return rangeDecoratorConfig.maximum;
      })).apply(instance, (Function3)(RangeDecoratorConfig::new));
   });
   public final int bottomOffset;
   public final int topOffset;
   public final int maximum;

   public RangeDecoratorConfig(int bottomOffset, int topOffset, int maximum) {
      this.bottomOffset = bottomOffset;
      this.topOffset = topOffset;
      this.maximum = maximum;
   }
}
