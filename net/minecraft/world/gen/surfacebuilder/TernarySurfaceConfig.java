package net.minecraft.world.gen.surfacebuilder;

import com.mojang.datafixers.util.Function3;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.block.BlockState;

public class TernarySurfaceConfig implements SurfaceConfig {
   public static final Codec<TernarySurfaceConfig> CODEC = RecordCodecBuilder.create((instance) -> {
      return instance.group(BlockState.CODEC.fieldOf("top_material").forGetter((config) -> {
         return config.topMaterial;
      }), BlockState.CODEC.fieldOf("under_material").forGetter((config) -> {
         return config.underMaterial;
      }), BlockState.CODEC.fieldOf("underwater_material").forGetter((config) -> {
         return config.underwaterMaterial;
      })).apply(instance, (Function3)(TernarySurfaceConfig::new));
   });
   private final BlockState topMaterial;
   private final BlockState underMaterial;
   private final BlockState underwaterMaterial;

   public TernarySurfaceConfig(BlockState topMaterial, BlockState underMaterial, BlockState underwaterMaterial) {
      this.topMaterial = topMaterial;
      this.underMaterial = underMaterial;
      this.underwaterMaterial = underwaterMaterial;
   }

   public BlockState getTopMaterial() {
      return this.topMaterial;
   }

   public BlockState getUnderMaterial() {
      return this.underMaterial;
   }

   public BlockState getUnderwaterMaterial() {
      return this.underwaterMaterial;
   }
}
