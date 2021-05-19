package net.minecraft.structure.rule;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.registry.Registry;

public class BlockMatchRuleTest extends RuleTest {
   public static final Codec<BlockMatchRuleTest> CODEC;
   private final Block block;

   public BlockMatchRuleTest(Block block) {
      this.block = block;
   }

   public boolean test(BlockState state, Random random) {
      return state.isOf(this.block);
   }

   protected RuleTestType<?> getType() {
      return RuleTestType.BLOCK_MATCH;
   }

   static {
      CODEC = Registry.BLOCK.fieldOf("block").xmap(BlockMatchRuleTest::new, (blockMatchRuleTest) -> {
         return blockMatchRuleTest.block;
      }).codec();
   }
}
