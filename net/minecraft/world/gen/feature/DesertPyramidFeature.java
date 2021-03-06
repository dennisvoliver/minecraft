package net.minecraft.world.gen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.structure.DesertTempleGenerator;
import net.minecraft.structure.StructureManager;
import net.minecraft.structure.StructureStart;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.registry.DynamicRegistryManager;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.chunk.ChunkGenerator;

public class DesertPyramidFeature extends StructureFeature<DefaultFeatureConfig> {
   public DesertPyramidFeature(Codec<DefaultFeatureConfig> codec) {
      super(codec);
   }

   public StructureFeature.StructureStartFactory<DefaultFeatureConfig> getStructureStartFactory() {
      return DesertPyramidFeature.Start::new;
   }

   public static class Start extends StructureStart<DefaultFeatureConfig> {
      public Start(StructureFeature<DefaultFeatureConfig> structureFeature, int i, int j, BlockBox blockBox, int k, long l) {
         super(structureFeature, i, j, blockBox, k, l);
      }

      public void init(DynamicRegistryManager dynamicRegistryManager, ChunkGenerator chunkGenerator, StructureManager structureManager, int i, int j, Biome biome, DefaultFeatureConfig defaultFeatureConfig) {
         DesertTempleGenerator desertTempleGenerator = new DesertTempleGenerator(this.random, i * 16, j * 16);
         this.children.add(desertTempleGenerator);
         this.setBoundingBoxFromChildren();
      }
   }
}
