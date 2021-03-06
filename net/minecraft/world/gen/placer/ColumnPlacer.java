package net.minecraft.world.gen.placer;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Random;
import java.util.function.BiFunction;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.WorldAccess;

public class ColumnPlacer extends BlockPlacer {
   public static final Codec<ColumnPlacer> CODEC = RecordCodecBuilder.create((instance) -> {
      return instance.group(Codec.INT.fieldOf("min_size").forGetter((placer) -> {
         return placer.minSize;
      }), Codec.INT.fieldOf("extra_size").forGetter((placer) -> {
         return placer.extraSize;
      })).apply(instance, (BiFunction)(ColumnPlacer::new));
   });
   private final int minSize;
   private final int extraSize;

   public ColumnPlacer(int minSize, int extraSize) {
      this.minSize = minSize;
      this.extraSize = extraSize;
   }

   protected BlockPlacerType<?> getType() {
      return BlockPlacerType.COLUMN_PLACER;
   }

   public void generate(WorldAccess world, BlockPos pos, BlockState state, Random random) {
      BlockPos.Mutable mutable = pos.mutableCopy();
      int i = this.minSize + random.nextInt(random.nextInt(this.extraSize + 1) + 1);

      for(int j = 0; j < i; ++j) {
         world.setBlockState(mutable, state, 2);
         mutable.move(Direction.UP);
      }

   }
}
