package net.minecraft.world.gen.decorator;

import com.mojang.datafixers.util.Function3;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public class CountExtraDecoratorConfig implements DecoratorConfig {
   public static final Codec<CountExtraDecoratorConfig> CODEC = RecordCodecBuilder.create((instance) -> {
      return instance.group(Codec.INT.fieldOf("count").forGetter((countExtraDecoratorConfig) -> {
         return countExtraDecoratorConfig.count;
      }), Codec.FLOAT.fieldOf("extra_chance").forGetter((countExtraDecoratorConfig) -> {
         return countExtraDecoratorConfig.extraChance;
      }), Codec.INT.fieldOf("extra_count").forGetter((countExtraDecoratorConfig) -> {
         return countExtraDecoratorConfig.extraCount;
      })).apply(instance, (Function3)(CountExtraDecoratorConfig::new));
   });
   public final int count;
   public final float extraChance;
   public final int extraCount;

   public CountExtraDecoratorConfig(int count, float extraChance, int extraCount) {
      this.count = count;
      this.extraChance = extraChance;
      this.extraCount = extraCount;
   }
}
