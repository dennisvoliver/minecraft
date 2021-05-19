package net.minecraft.structure.rule;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.block.BlockState;

public class AlwaysTrueRuleTest extends RuleTest {
   public static final Codec<AlwaysTrueRuleTest> CODEC = Codec.unit(() -> {
      return INSTANCE;
   });
   public static final AlwaysTrueRuleTest INSTANCE = new AlwaysTrueRuleTest();

   private AlwaysTrueRuleTest() {
   }

   public boolean test(BlockState state, Random random) {
      return true;
   }

   protected RuleTestType<?> getType() {
      return RuleTestType.ALWAYS_TRUE;
   }
}
