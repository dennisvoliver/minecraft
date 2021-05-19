package net.minecraft.structure.rule;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Random;
import java.util.function.BiFunction;
import net.minecraft.block.BlockState;

public class RandomBlockStateMatchRuleTest extends RuleTest {
   public static final Codec<RandomBlockStateMatchRuleTest> CODEC = RecordCodecBuilder.create((instance) -> {
      return instance.group(BlockState.CODEC.fieldOf("block_state").forGetter((randomBlockStateMatchRuleTest) -> {
         return randomBlockStateMatchRuleTest.blockState;
      }), Codec.FLOAT.fieldOf("probability").forGetter((randomBlockStateMatchRuleTest) -> {
         return randomBlockStateMatchRuleTest.probability;
      })).apply(instance, (BiFunction)(RandomBlockStateMatchRuleTest::new));
   });
   private final BlockState blockState;
   private final float probability;

   public RandomBlockStateMatchRuleTest(BlockState blockState, float probability) {
      this.blockState = blockState;
      this.probability = probability;
   }

   public boolean test(BlockState state, Random random) {
      return state == this.blockState && random.nextFloat() < this.probability;
   }

   protected RuleTestType<?> getType() {
      return RuleTestType.RANDOM_BLOCKSTATE_MATCH;
   }
}
