package net.minecraft.world.gen;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Objects;
import java.util.Random;
import java.util.function.BiFunction;
import java.util.function.Function;

public class UniformIntDistribution {
   public static final Codec<UniformIntDistribution> CODEC;
   private final int base;
   private final int spread;

   public static Codec<UniformIntDistribution> createValidatedCodec(int minBase, int maxBase, int maxSpread) {
      Function<UniformIntDistribution, DataResult<UniformIntDistribution>> function = (uniformIntDistribution) -> {
         if (uniformIntDistribution.base >= minBase && uniformIntDistribution.base <= maxBase) {
            return uniformIntDistribution.spread <= maxSpread ? DataResult.success(uniformIntDistribution) : DataResult.error("Spread too big: " + uniformIntDistribution.spread + " > " + maxSpread);
         } else {
            return DataResult.error("Base value out of range: " + uniformIntDistribution.base + " [" + minBase + "-" + maxBase + "]");
         }
      };
      return CODEC.flatXmap(function, function);
   }

   private UniformIntDistribution(int base, int spread) {
      this.base = base;
      this.spread = spread;
   }

   /**
    * Creates a distribution with a constant value.
    * 
    * @param value the constant value
    */
   public static UniformIntDistribution of(int value) {
      return new UniformIntDistribution(value, 0);
   }

   public static UniformIntDistribution of(int base, int spread) {
      return new UniformIntDistribution(base, spread);
   }

   public int getValue(Random random) {
      return this.spread == 0 ? this.base : this.base + random.nextInt(this.spread + 1);
   }

   public boolean equals(Object object) {
      if (this == object) {
         return true;
      } else if (object != null && this.getClass() == object.getClass()) {
         UniformIntDistribution uniformIntDistribution = (UniformIntDistribution)object;
         return this.base == uniformIntDistribution.base && this.spread == uniformIntDistribution.spread;
      } else {
         return false;
      }
   }

   public int hashCode() {
      return Objects.hash(new Object[]{this.base, this.spread});
   }

   public String toString() {
      return "[" + this.base + '-' + (this.base + this.spread) + ']';
   }

   static {
      CODEC = Codec.either(Codec.INT, RecordCodecBuilder.create((instance) -> {
         return instance.group(Codec.INT.fieldOf("base").forGetter((uniformIntDistribution) -> {
            return uniformIntDistribution.base;
         }), Codec.INT.fieldOf("spread").forGetter((uniformIntDistribution) -> {
            return uniformIntDistribution.spread;
         })).apply(instance, (BiFunction)(UniformIntDistribution::new));
      }).comapFlatMap((uniformIntDistribution) -> {
         return uniformIntDistribution.spread < 0 ? DataResult.error("Spread must be non-negative, got: " + uniformIntDistribution.spread) : DataResult.success(uniformIntDistribution);
      }, Function.identity())).xmap((either) -> {
         return (UniformIntDistribution)either.map(UniformIntDistribution::of, (uniformIntDistribution) -> {
            return uniformIntDistribution;
         });
      }, (uniformIntDistribution) -> {
         return uniformIntDistribution.spread == 0 ? Either.left(uniformIntDistribution.base) : Either.right(uniformIntDistribution);
      });
   }
}
