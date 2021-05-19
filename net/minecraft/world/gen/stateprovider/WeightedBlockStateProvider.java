package net.minecraft.world.gen.stateprovider;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import java.util.Random;
import net.minecraft.block.BlockState;
import net.minecraft.util.collection.WeightedList;
import net.minecraft.util.math.BlockPos;

public class WeightedBlockStateProvider extends BlockStateProvider {
   public static final Codec<WeightedBlockStateProvider> CODEC;
   private final WeightedList<BlockState> states;

   private static DataResult<WeightedBlockStateProvider> wrap(WeightedList<BlockState> states) {
      return states.isEmpty() ? DataResult.error("WeightedStateProvider with no states") : DataResult.success(new WeightedBlockStateProvider(states));
   }

   private WeightedBlockStateProvider(WeightedList<BlockState> states) {
      this.states = states;
   }

   protected BlockStateProviderType<?> getType() {
      return BlockStateProviderType.WEIGHTED_STATE_PROVIDER;
   }

   public WeightedBlockStateProvider() {
      this(new WeightedList());
   }

   public WeightedBlockStateProvider addState(BlockState state, int weight) {
      this.states.add(state, weight);
      return this;
   }

   public BlockState getBlockState(Random random, BlockPos pos) {
      return (BlockState)this.states.pickRandom(random);
   }

   static {
      CODEC = WeightedList.createCodec(BlockState.CODEC).comapFlatMap(WeightedBlockStateProvider::wrap, (weightedBlockStateProvider) -> {
         return weightedBlockStateProvider.states;
      }).fieldOf("entries").codec();
   }
}
