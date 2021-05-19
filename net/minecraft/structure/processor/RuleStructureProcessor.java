package net.minecraft.structure.processor;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.UnmodifiableIterator;
import com.mojang.serialization.Codec;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import net.minecraft.block.BlockState;
import net.minecraft.structure.Structure;
import net.minecraft.structure.StructurePlacementData;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.WorldView;
import org.jetbrains.annotations.Nullable;

public class RuleStructureProcessor extends StructureProcessor {
   public static final Codec<RuleStructureProcessor> CODEC;
   private final ImmutableList<StructureProcessorRule> rules;

   public RuleStructureProcessor(List<? extends StructureProcessorRule> rules) {
      this.rules = ImmutableList.copyOf((Collection)rules);
   }

   @Nullable
   public Structure.StructureBlockInfo process(WorldView worldView, BlockPos pos, BlockPos blockPos, Structure.StructureBlockInfo structureBlockInfo, Structure.StructureBlockInfo structureBlockInfo2, StructurePlacementData structurePlacementData) {
      Random random = new Random(MathHelper.hashCode(structureBlockInfo2.pos));
      BlockState blockState = worldView.getBlockState(structureBlockInfo2.pos);
      UnmodifiableIterator var9 = this.rules.iterator();

      StructureProcessorRule structureProcessorRule;
      do {
         if (!var9.hasNext()) {
            return structureBlockInfo2;
         }

         structureProcessorRule = (StructureProcessorRule)var9.next();
      } while(!structureProcessorRule.test(structureBlockInfo2.state, blockState, structureBlockInfo.pos, structureBlockInfo2.pos, blockPos, random));

      return new Structure.StructureBlockInfo(structureBlockInfo2.pos, structureProcessorRule.getOutputState(), structureProcessorRule.getTag());
   }

   protected StructureProcessorType<?> getType() {
      return StructureProcessorType.RULE;
   }

   static {
      CODEC = StructureProcessorRule.CODEC.listOf().fieldOf("rules").xmap(RuleStructureProcessor::new, (ruleStructureProcessor) -> {
         return ruleStructureProcessor.rules;
      }).codec();
   }
}
