package net.minecraft.world.gen.feature;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import java.util.List;
import net.minecraft.entity.EntityType;
import net.minecraft.structure.StructureManager;
import net.minecraft.structure.StructureStart;
import net.minecraft.structure.SwampHutGenerator;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.registry.DynamicRegistryManager;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.SpawnSettings;
import net.minecraft.world.gen.chunk.ChunkGenerator;

public class SwampHutFeature extends StructureFeature<DefaultFeatureConfig> {
   private static final List<SpawnSettings.SpawnEntry> MONSTER_SPAWNS;
   private static final List<SpawnSettings.SpawnEntry> CREATURE_SPAWNS;

   public SwampHutFeature(Codec<DefaultFeatureConfig> codec) {
      super(codec);
   }

   public StructureFeature.StructureStartFactory<DefaultFeatureConfig> getStructureStartFactory() {
      return SwampHutFeature.Start::new;
   }

   public List<SpawnSettings.SpawnEntry> getMonsterSpawns() {
      return MONSTER_SPAWNS;
   }

   public List<SpawnSettings.SpawnEntry> getCreatureSpawns() {
      return CREATURE_SPAWNS;
   }

   static {
      MONSTER_SPAWNS = ImmutableList.of(new SpawnSettings.SpawnEntry(EntityType.WITCH, 1, 1, 1));
      CREATURE_SPAWNS = ImmutableList.of(new SpawnSettings.SpawnEntry(EntityType.CAT, 1, 1, 1));
   }

   public static class Start extends StructureStart<DefaultFeatureConfig> {
      public Start(StructureFeature<DefaultFeatureConfig> structureFeature, int i, int j, BlockBox blockBox, int k, long l) {
         super(structureFeature, i, j, blockBox, k, l);
      }

      public void init(DynamicRegistryManager dynamicRegistryManager, ChunkGenerator chunkGenerator, StructureManager structureManager, int i, int j, Biome biome, DefaultFeatureConfig defaultFeatureConfig) {
         SwampHutGenerator swampHutGenerator = new SwampHutGenerator(this.random, i * 16, j * 16);
         this.children.add(swampHutGenerator);
         this.setBoundingBoxFromChildren();
      }
   }
}
