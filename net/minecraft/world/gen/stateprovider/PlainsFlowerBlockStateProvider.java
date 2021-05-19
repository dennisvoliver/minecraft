package net.minecraft.world.gen.stateprovider;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;

public class PlainsFlowerBlockStateProvider extends BlockStateProvider {
   public static final Codec<PlainsFlowerBlockStateProvider> CODEC = Codec.unit(() -> {
      return INSTANCE;
   });
   public static final PlainsFlowerBlockStateProvider INSTANCE = new PlainsFlowerBlockStateProvider();
   private static final BlockState[] TULIPS;
   private static final BlockState[] FLOWERS;

   protected BlockStateProviderType<?> getType() {
      return BlockStateProviderType.PLAIN_FLOWER_PROVIDER;
   }

   public BlockState getBlockState(Random random, BlockPos pos) {
      double d = Biome.FOLIAGE_NOISE.sample((double)pos.getX() / 200.0D, (double)pos.getZ() / 200.0D, false);
      if (d < -0.8D) {
         return (BlockState)Util.getRandom((Object[])TULIPS, random);
      } else {
         return random.nextInt(3) > 0 ? (BlockState)Util.getRandom((Object[])FLOWERS, random) : Blocks.DANDELION.getDefaultState();
      }
   }

   static {
      TULIPS = new BlockState[]{Blocks.ORANGE_TULIP.getDefaultState(), Blocks.RED_TULIP.getDefaultState(), Blocks.PINK_TULIP.getDefaultState(), Blocks.WHITE_TULIP.getDefaultState()};
      FLOWERS = new BlockState[]{Blocks.POPPY.getDefaultState(), Blocks.AZURE_BLUET.getDefaultState(), Blocks.OXEYE_DAISY.getDefaultState(), Blocks.CORNFLOWER.getDefaultState()};
   }
}
