package net.minecraft.structure.rule;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.block.BlockState;

public class BlockStateMatchRuleTest extends RuleTest {
   public static final Codec<BlockStateMatchRuleTest> CODEC;
   private final BlockState blockState;

   public BlockStateMatchRuleTest(BlockState blockState) {
      this.blockState = blockState;
   }

   public boolean test(BlockState state, Random random) {
      return state == this.blockState;
   }

   protected RuleTestType<?> getType() {
      return RuleTestType.BLOCKSTATE_MATCH;
   }

   static {
      CODEC = BlockState.CODEC.fieldOf("block_state").xmap(BlockStateMatchRuleTest::new, (blockStateMatchRuleTest) -> {
         return blockStateMatchRuleTest.blockState;
      }).codec();
   }
}
