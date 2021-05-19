package net.minecraft.structure.rule;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.util.math.BlockPos;

public class AlwaysTruePosRuleTest extends PosRuleTest {
   public static final Codec<AlwaysTruePosRuleTest> CODEC = Codec.unit(() -> {
      return INSTANCE;
   });
   public static final AlwaysTruePosRuleTest INSTANCE = new AlwaysTruePosRuleTest();

   private AlwaysTruePosRuleTest() {
   }

   public boolean test(BlockPos blockPos, BlockPos blockPos2, BlockPos blockPos3, Random random) {
      return true;
   }

   protected PosRuleTestType<?> getType() {
      return PosRuleTestType.ALWAYS_TRUE;
   }
}
