package net.minecraft.world.gen.feature;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.function.BiFunction;
import net.minecraft.block.BlockState;

public class EmeraldOreFeatureConfig implements FeatureConfig {
   public static final Codec<EmeraldOreFeatureConfig> CODEC = RecordCodecBuilder.create((instance) -> {
      return instance.group(BlockState.CODEC.fieldOf("target").forGetter((emeraldOreFeatureConfig) -> {
         return emeraldOreFeatureConfig.target;
      }), BlockState.CODEC.fieldOf("state").forGetter((emeraldOreFeatureConfig) -> {
         return emeraldOreFeatureConfig.state;
      })).apply(instance, (BiFunction)(EmeraldOreFeatureConfig::new));
   });
   public final BlockState target;
   public final BlockState state;

   public EmeraldOreFeatureConfig(BlockState target, BlockState state) {
      this.target = target;
      this.state = state;
   }
}
