package net.minecraft.world.biome.source;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;
import com.mojang.serialization.Codec;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.function.Predicate;
import java.util.function.Supplier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;
import org.jetbrains.annotations.Nullable;

public class FixedBiomeSource extends BiomeSource {
   public static final Codec<FixedBiomeSource> CODEC;
   private final Supplier<Biome> biome;

   public FixedBiomeSource(Biome biome) {
      this(() -> {
         return biome;
      });
   }

   public FixedBiomeSource(Supplier<Biome> biome) {
      super((List)ImmutableList.of(biome.get()));
      this.biome = biome;
   }

   protected Codec<? extends BiomeSource> getCodec() {
      return CODEC;
   }

   @Environment(EnvType.CLIENT)
   public BiomeSource withSeed(long seed) {
      return this;
   }

   public Biome getBiomeForNoiseGen(int biomeX, int biomeY, int biomeZ) {
      return (Biome)this.biome.get();
   }

   @Nullable
   public BlockPos locateBiome(int x, int y, int z, int radius, int i, Predicate<Biome> predicate, Random random, boolean bl) {
      if (predicate.test(this.biome.get())) {
         return bl ? new BlockPos(x, y, z) : new BlockPos(x - radius + random.nextInt(radius * 2 + 1), y, z - radius + random.nextInt(radius * 2 + 1));
      } else {
         return null;
      }
   }

   public Set<Biome> getBiomesInArea(int x, int y, int z, int radius) {
      return Sets.newHashSet((Object[])((Biome)this.biome.get()));
   }

   static {
      CODEC = Biome.REGISTRY_CODEC.fieldOf("biome").xmap(FixedBiomeSource::new, (fixedBiomeSource) -> {
         return fixedBiomeSource.biome;
      }).stable().codec();
   }
}
