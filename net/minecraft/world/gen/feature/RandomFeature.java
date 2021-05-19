package net.minecraft.world.gen.feature;

import com.mojang.serialization.Codec;
import java.util.Iterator;
import java.util.Random;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.chunk.ChunkGenerator;

public class RandomFeature extends Feature<RandomFeatureConfig> {
   public RandomFeature(Codec<RandomFeatureConfig> codec) {
      super(codec);
   }

   public boolean generate(StructureWorldAccess structureWorldAccess, ChunkGenerator chunkGenerator, Random random, BlockPos blockPos, RandomFeatureConfig randomFeatureConfig) {
      Iterator var6 = randomFeatureConfig.features.iterator();

      RandomFeatureEntry randomFeatureEntry;
      do {
         if (!var6.hasNext()) {
            return ((ConfiguredFeature)randomFeatureConfig.defaultFeature.get()).generate(structureWorldAccess, chunkGenerator, random, blockPos);
         }

         randomFeatureEntry = (RandomFeatureEntry)var6.next();
      } while(!(random.nextFloat() < randomFeatureEntry.chance));

      return randomFeatureEntry.generate(structureWorldAccess, chunkGenerator, random, blockPos);
   }
}
