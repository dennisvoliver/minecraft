package net.minecraft.structure.rule;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Random;
import java.util.function.BiFunction;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.registry.Registry;

public class RandomBlockMatchRuleTest extends RuleTest {
   public static final Codec<RandomBlockMatchRuleTest> CODEC = RecordCodecBuilder.create((instance) -> {
      return instance.group(Registry.BLOCK.fieldOf("block").forGetter((randomBlockMatchRuleTest) -> {
         return randomBlockMatchRuleTest.block;
      }), Codec.FLOAT.fieldOf("probability").forGetter((randomBlockMatchRuleTest) -> {
         return randomBlockMatchRuleTest.probability;
      })).apply(instance, (BiFunction)(RandomBlockMatchRuleTest::new));
   });
   private final Block block;
   private final float probability;

   public RandomBlockMatchRuleTest(Block block, float probability) {
      this.block = block;
      this.probability = probability;
   }

   public boolean test(BlockState state, Random random) {
      return state.isOf(this.block) && random.nextFloat() < this.probability;
   }

   protected RuleTestType<?> getType() {
      return RuleTestType.RANDOM_BLOCK_MATCH;
   }
}
