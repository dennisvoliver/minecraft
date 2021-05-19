package net.minecraft.world.gen.feature;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.function.BiFunction;
import net.minecraft.block.BlockState;

public class FillLayerFeatureConfig implements FeatureConfig {
   public static final Codec<FillLayerFeatureConfig> CODEC = RecordCodecBuilder.create((instance) -> {
      return instance.group(Codec.intRange(0, 255).fieldOf("height").forGetter((fillLayerFeatureConfig) -> {
         return fillLayerFeatureConfig.height;
      }), BlockState.CODEC.fieldOf("state").forGetter((fillLayerFeatureConfig) -> {
         return fillLayerFeatureConfig.state;
      })).apply(instance, (BiFunction)(FillLayerFeatureConfig::new));
   });
   public final int height;
   public final BlockState state;

   public FillLayerFeatureConfig(int height, BlockState state) {
      this.height = height;
      this.state = state;
   }
}
