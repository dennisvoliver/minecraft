package net.minecraft.world.gen.decorator;

import com.mojang.serialization.Codec;

public class ChanceDecoratorConfig implements DecoratorConfig {
   public static final Codec<ChanceDecoratorConfig> CODEC;
   public final int chance;

   public ChanceDecoratorConfig(int chance) {
      this.chance = chance;
   }

   static {
      CODEC = Codec.INT.fieldOf("chance").xmap(ChanceDecoratorConfig::new, (chanceDecoratorConfig) -> {
         return chanceDecoratorConfig.chance;
      }).codec();
   }
}
