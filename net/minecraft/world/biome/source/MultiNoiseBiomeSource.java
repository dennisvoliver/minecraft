package net.minecraft.world.biome.source;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Function3;
import com.mojang.datafixers.util.Function6;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.fastutil.doubles.DoubleList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.noise.DoublePerlinNoiseSampler;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryLookupCodec;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeKeys;
import net.minecraft.world.biome.BuiltinBiomes;
import net.minecraft.world.gen.ChunkRandom;

public class MultiNoiseBiomeSource extends BiomeSource {
   private static final MultiNoiseBiomeSource.NoiseParameters DEFAULT_NOISE_PARAMETERS = new MultiNoiseBiomeSource.NoiseParameters(-7, ImmutableList.of(1.0D, 1.0D));
   /**
    * Used to parse a custom biome source, when a preset hasn't been provided.
    */
   public static final MapCodec<MultiNoiseBiomeSource> CUSTOM_CODEC = RecordCodecBuilder.mapCodec((instance) -> {
      return instance.group(Codec.LONG.fieldOf("seed").forGetter((multiNoiseBiomeSource) -> {
         return multiNoiseBiomeSource.seed;
      }), RecordCodecBuilder.create((instancex) -> {
         return instancex.group(Biome.MixedNoisePoint.CODEC.fieldOf("parameters").forGetter(Pair::getFirst), Biome.REGISTRY_CODEC.fieldOf("biome").forGetter(Pair::getSecond)).apply(instancex, (BiFunction)(Pair::of));
      }).listOf().fieldOf("biomes").forGetter((multiNoiseBiomeSource) -> {
         return multiNoiseBiomeSource.biomePoints;
      }), MultiNoiseBiomeSource.NoiseParameters.CODEC.fieldOf("temperature_noise").forGetter((multiNoiseBiomeSource) -> {
         return multiNoiseBiomeSource.temperatureNoiseParameters;
      }), MultiNoiseBiomeSource.NoiseParameters.CODEC.fieldOf("humidity_noise").forGetter((multiNoiseBiomeSource) -> {
         return multiNoiseBiomeSource.humidityNoiseParameters;
      }), MultiNoiseBiomeSource.NoiseParameters.CODEC.fieldOf("altitude_noise").forGetter((multiNoiseBiomeSource) -> {
         return multiNoiseBiomeSource.altitudeNoiseParameters;
      }), MultiNoiseBiomeSource.NoiseParameters.CODEC.fieldOf("weirdness_noise").forGetter((multiNoiseBiomeSource) -> {
         return multiNoiseBiomeSource.weirdnessNoiseParameters;
      })).apply(instance, (Function6)(MultiNoiseBiomeSource::new));
   });
   public static final Codec<MultiNoiseBiomeSource> CODEC;
   private final MultiNoiseBiomeSource.NoiseParameters temperatureNoiseParameters;
   private final MultiNoiseBiomeSource.NoiseParameters humidityNoiseParameters;
   private final MultiNoiseBiomeSource.NoiseParameters altitudeNoiseParameters;
   private final MultiNoiseBiomeSource.NoiseParameters weirdnessNoiseParameters;
   private final DoublePerlinNoiseSampler temperatureNoise;
   private final DoublePerlinNoiseSampler humidityNoise;
   private final DoublePerlinNoiseSampler altitudeNoise;
   private final DoublePerlinNoiseSampler weirdnessNoise;
   private final List<Pair<Biome.MixedNoisePoint, Supplier<Biome>>> biomePoints;
   private final boolean threeDimensionalSampling;
   private final long seed;
   private final Optional<Pair<Registry<Biome>, MultiNoiseBiomeSource.Preset>> instance;

   private MultiNoiseBiomeSource(long seed, List<Pair<Biome.MixedNoisePoint, Supplier<Biome>>> biomePoints, Optional<Pair<Registry<Biome>, MultiNoiseBiomeSource.Preset>> instance) {
      this(seed, biomePoints, DEFAULT_NOISE_PARAMETERS, DEFAULT_NOISE_PARAMETERS, DEFAULT_NOISE_PARAMETERS, DEFAULT_NOISE_PARAMETERS, instance);
   }

   private MultiNoiseBiomeSource(long seed, List<Pair<Biome.MixedNoisePoint, Supplier<Biome>>> biomePoints, MultiNoiseBiomeSource.NoiseParameters temperatureNoiseParameters, MultiNoiseBiomeSource.NoiseParameters humidityNoiseParameters, MultiNoiseBiomeSource.NoiseParameters altitudeNoiseParameters, MultiNoiseBiomeSource.NoiseParameters weirdnessNoiseParameters) {
      this(seed, biomePoints, temperatureNoiseParameters, humidityNoiseParameters, altitudeNoiseParameters, weirdnessNoiseParameters, Optional.empty());
   }

   private MultiNoiseBiomeSource(long seed, List<Pair<Biome.MixedNoisePoint, Supplier<Biome>>> biomePoints, MultiNoiseBiomeSource.NoiseParameters temperatureNoiseParameters, MultiNoiseBiomeSource.NoiseParameters humidityNoiseParameters, MultiNoiseBiomeSource.NoiseParameters altitudeNoiseParameters, MultiNoiseBiomeSource.NoiseParameters weirdnessNoiseParameters, Optional<Pair<Registry<Biome>, MultiNoiseBiomeSource.Preset>> instance) {
      super(biomePoints.stream().map(Pair::getSecond));
      this.seed = seed;
      this.instance = instance;
      this.temperatureNoiseParameters = temperatureNoiseParameters;
      this.humidityNoiseParameters = humidityNoiseParameters;
      this.altitudeNoiseParameters = altitudeNoiseParameters;
      this.weirdnessNoiseParameters = weirdnessNoiseParameters;
      this.temperatureNoise = DoublePerlinNoiseSampler.method_30846(new ChunkRandom(seed), temperatureNoiseParameters.getFirstOctave(), temperatureNoiseParameters.getAmplitudes());
      this.humidityNoise = DoublePerlinNoiseSampler.method_30846(new ChunkRandom(seed + 1L), humidityNoiseParameters.getFirstOctave(), humidityNoiseParameters.getAmplitudes());
      this.altitudeNoise = DoublePerlinNoiseSampler.method_30846(new ChunkRandom(seed + 2L), altitudeNoiseParameters.getFirstOctave(), altitudeNoiseParameters.getAmplitudes());
      this.weirdnessNoise = DoublePerlinNoiseSampler.method_30846(new ChunkRandom(seed + 3L), weirdnessNoiseParameters.getFirstOctave(), weirdnessNoiseParameters.getAmplitudes());
      this.biomePoints = biomePoints;
      this.threeDimensionalSampling = false;
   }

   protected Codec<? extends BiomeSource> getCodec() {
      return CODEC;
   }

   @Environment(EnvType.CLIENT)
   public BiomeSource withSeed(long seed) {
      return new MultiNoiseBiomeSource(seed, this.biomePoints, this.temperatureNoiseParameters, this.humidityNoiseParameters, this.altitudeNoiseParameters, this.weirdnessNoiseParameters, this.instance);
   }

   private Optional<MultiNoiseBiomeSource.Instance> getInstance() {
      return this.instance.map((pair) -> {
         return new MultiNoiseBiomeSource.Instance((MultiNoiseBiomeSource.Preset)pair.getSecond(), (Registry)pair.getFirst(), this.seed);
      });
   }

   public Biome getBiomeForNoiseGen(int biomeX, int biomeY, int biomeZ) {
      int i = this.threeDimensionalSampling ? biomeY : 0;
      Biome.MixedNoisePoint mixedNoisePoint = new Biome.MixedNoisePoint((float)this.temperatureNoise.sample((double)biomeX, (double)i, (double)biomeZ), (float)this.humidityNoise.sample((double)biomeX, (double)i, (double)biomeZ), (float)this.altitudeNoise.sample((double)biomeX, (double)i, (double)biomeZ), (float)this.weirdnessNoise.sample((double)biomeX, (double)i, (double)biomeZ), 0.0F);
      return (Biome)this.biomePoints.stream().min(Comparator.comparing((pair) -> {
         return ((Biome.MixedNoisePoint)pair.getFirst()).calculateDistanceTo(mixedNoisePoint);
      })).map(Pair::getSecond).map(Supplier::get).orElse(BuiltinBiomes.THE_VOID);
   }

   public boolean matchesInstance(long seed) {
      return this.seed == seed && this.instance.isPresent() && Objects.equals(((Pair)this.instance.get()).getSecond(), MultiNoiseBiomeSource.Preset.NETHER);
   }

   static {
      CODEC = Codec.mapEither(MultiNoiseBiomeSource.Instance.CODEC, CUSTOM_CODEC).xmap((either) -> {
         return (MultiNoiseBiomeSource)either.map(MultiNoiseBiomeSource.Instance::getBiomeSource, Function.identity());
      }, (multiNoiseBiomeSource) -> {
         return (Either)multiNoiseBiomeSource.getInstance().map(Either::left).orElseGet(() -> {
            return Either.right(multiNoiseBiomeSource);
         });
      }).codec();
   }

   public static class Preset {
      private static final Map<Identifier, MultiNoiseBiomeSource.Preset> BY_IDENTIFIER = Maps.newHashMap();
      public static final MultiNoiseBiomeSource.Preset NETHER = new MultiNoiseBiomeSource.Preset(new Identifier("nether"), (preset, registry, long_) -> {
         return new MultiNoiseBiomeSource(long_, ImmutableList.of(Pair.of(new Biome.MixedNoisePoint(0.0F, 0.0F, 0.0F, 0.0F, 0.0F), () -> {
            return (Biome)registry.getOrThrow(BiomeKeys.NETHER_WASTES);
         }), Pair.of(new Biome.MixedNoisePoint(0.0F, -0.5F, 0.0F, 0.0F, 0.0F), () -> {
            return (Biome)registry.getOrThrow(BiomeKeys.SOUL_SAND_VALLEY);
         }), Pair.of(new Biome.MixedNoisePoint(0.4F, 0.0F, 0.0F, 0.0F, 0.0F), () -> {
            return (Biome)registry.getOrThrow(BiomeKeys.CRIMSON_FOREST);
         }), Pair.of(new Biome.MixedNoisePoint(0.0F, 0.5F, 0.0F, 0.0F, 0.375F), () -> {
            return (Biome)registry.getOrThrow(BiomeKeys.WARPED_FOREST);
         }), Pair.of(new Biome.MixedNoisePoint(-0.5F, 0.0F, 0.0F, 0.0F, 0.175F), () -> {
            return (Biome)registry.getOrThrow(BiomeKeys.BASALT_DELTAS);
         })), Optional.of(Pair.of(registry, preset)));
      });
      private final Identifier id;
      private final Function3<MultiNoiseBiomeSource.Preset, Registry<Biome>, Long, MultiNoiseBiomeSource> biomeSourceFunction;

      public Preset(Identifier id, Function3<MultiNoiseBiomeSource.Preset, Registry<Biome>, Long, MultiNoiseBiomeSource> biomeSourceFunction) {
         this.id = id;
         this.biomeSourceFunction = biomeSourceFunction;
         BY_IDENTIFIER.put(id, this);
      }

      public MultiNoiseBiomeSource getBiomeSource(Registry<Biome> biomeRegistry, long seed) {
         return (MultiNoiseBiomeSource)this.biomeSourceFunction.apply(this, biomeRegistry, seed);
      }
   }

   static final class Instance {
      public static final MapCodec<MultiNoiseBiomeSource.Instance> CODEC = RecordCodecBuilder.mapCodec((instance) -> {
         return instance.group(Identifier.CODEC.flatXmap((identifier) -> {
            return (DataResult)Optional.ofNullable(MultiNoiseBiomeSource.Preset.BY_IDENTIFIER.get(identifier)).map(DataResult::success).orElseGet(() -> {
               return DataResult.error("Unknown preset: " + identifier);
            });
         }, (preset) -> {
            return DataResult.success(preset.id);
         }).fieldOf("preset").stable().forGetter(MultiNoiseBiomeSource.Instance::getPreset), RegistryLookupCodec.of(Registry.BIOME_KEY).forGetter(MultiNoiseBiomeSource.Instance::getBiomeRegistry), Codec.LONG.fieldOf("seed").stable().forGetter(MultiNoiseBiomeSource.Instance::getSeed)).apply(instance, (App)instance.stable(MultiNoiseBiomeSource.Instance::new));
      });
      private final MultiNoiseBiomeSource.Preset preset;
      private final Registry<Biome> biomeRegistry;
      private final long seed;

      private Instance(MultiNoiseBiomeSource.Preset preset, Registry<Biome> biomeRegistry, long seed) {
         this.preset = preset;
         this.biomeRegistry = biomeRegistry;
         this.seed = seed;
      }

      public MultiNoiseBiomeSource.Preset getPreset() {
         return this.preset;
      }

      public Registry<Biome> getBiomeRegistry() {
         return this.biomeRegistry;
      }

      public long getSeed() {
         return this.seed;
      }

      public MultiNoiseBiomeSource getBiomeSource() {
         return this.preset.getBiomeSource(this.biomeRegistry, this.seed);
      }
   }

   static class NoiseParameters {
      private final int firstOctave;
      private final DoubleList amplitudes;
      public static final Codec<MultiNoiseBiomeSource.NoiseParameters> CODEC = RecordCodecBuilder.create((instance) -> {
         return instance.group(Codec.INT.fieldOf("firstOctave").forGetter(MultiNoiseBiomeSource.NoiseParameters::getFirstOctave), Codec.DOUBLE.listOf().fieldOf("amplitudes").forGetter(MultiNoiseBiomeSource.NoiseParameters::getAmplitudes)).apply(instance, (BiFunction)(MultiNoiseBiomeSource.NoiseParameters::new));
      });

      public NoiseParameters(int firstOctave, List<Double> amplitudes) {
         this.firstOctave = firstOctave;
         this.amplitudes = new DoubleArrayList(amplitudes);
      }

      public int getFirstOctave() {
         return this.firstOctave;
      }

      public DoubleList getAmplitudes() {
         return this.amplitudes;
      }
   }
}
