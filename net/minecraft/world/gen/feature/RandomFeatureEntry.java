package net.minecraft.world.gen.feature;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Random;
import java.util.function.BiFunction;
import java.util.function.Supplier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.chunk.ChunkGenerator;

public class RandomFeatureEntry {
   public static final Codec<RandomFeatureEntry> CODEC = RecordCodecBuilder.create((instance) -> {
      return instance.group(ConfiguredFeature.REGISTRY_CODEC.fieldOf("feature").forGetter((randomFeatureEntry) -> {
         return randomFeatureEntry.feature;
      }), Codec.floatRange(0.0F, 1.0F).fieldOf("chance").forGetter((randomFeatureEntry) -> {
         return randomFeatureEntry.chance;
      })).apply(instance, (BiFunction)(RandomFeatureEntry::new));
   });
   public final Supplier<ConfiguredFeature<?, ?>> feature;
   public final float chance;

   public RandomFeatureEntry(ConfiguredFeature<?, ?> feature, float chance) {
      this(() -> {
         return feature;
      }, chance);
   }

   private RandomFeatureEntry(Supplier<ConfiguredFeature<?, ?>> feature, float chance) {
      this.feature = feature;
      this.chance = chance;
   }

   public boolean generate(StructureWorldAccess world, ChunkGenerator chunkGenerator, Random random, BlockPos pos) {
      return ((ConfiguredFeature)this.feature.get()).generate(world, chunkGenerator, random, pos);
   }
}
