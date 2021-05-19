package net.minecraft.world.gen.decorator;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.function.BiFunction;
import net.minecraft.world.gen.GenerationStep;

public class CarvingMaskDecoratorConfig implements DecoratorConfig {
   public static final Codec<CarvingMaskDecoratorConfig> CODEC = RecordCodecBuilder.create((instance) -> {
      return instance.group(GenerationStep.Carver.CODEC.fieldOf("step").forGetter((carvingMaskDecoratorConfig) -> {
         return carvingMaskDecoratorConfig.step;
      }), Codec.FLOAT.fieldOf("probability").forGetter((carvingMaskDecoratorConfig) -> {
         return carvingMaskDecoratorConfig.probability;
      })).apply(instance, (BiFunction)(CarvingMaskDecoratorConfig::new));
   });
   protected final GenerationStep.Carver step;
   protected final float probability;

   public CarvingMaskDecoratorConfig(GenerationStep.Carver step, float probability) {
      this.step = step;
      this.probability = probability;
   }
}
