package net.minecraft.world.gen.decorator;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.function.BiFunction;

public class DepthAverageDecoratorConfig implements DecoratorConfig {
   public static final Codec<DepthAverageDecoratorConfig> CODEC = RecordCodecBuilder.create((instance) -> {
      return instance.group(Codec.INT.fieldOf("baseline").forGetter((depthAverageDecoratorConfig) -> {
         return depthAverageDecoratorConfig.baseline;
      }), Codec.INT.fieldOf("spread").forGetter((depthAverageDecoratorConfig) -> {
         return depthAverageDecoratorConfig.spread;
      })).apply(instance, (BiFunction)(DepthAverageDecoratorConfig::new));
   });
   public final int baseline;
   public final int spread;

   public DepthAverageDecoratorConfig(int baseline, int spread) {
      this.baseline = baseline;
      this.spread = spread;
   }
}
