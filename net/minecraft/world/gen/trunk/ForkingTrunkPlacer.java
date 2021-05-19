package net.minecraft.world.gen.trunk;

import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Function3;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Random;
import java.util.Set;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.ModifiableTestableWorld;
import net.minecraft.world.gen.feature.TreeFeatureConfig;
import net.minecraft.world.gen.foliage.FoliagePlacer;

public class ForkingTrunkPlacer extends TrunkPlacer {
   public static final Codec<ForkingTrunkPlacer> CODEC = RecordCodecBuilder.create((instance) -> {
      return method_28904(instance).apply(instance, (Function3)(ForkingTrunkPlacer::new));
   });

   public ForkingTrunkPlacer(int i, int j, int k) {
      super(i, j, k);
   }

   protected TrunkPlacerType<?> getType() {
      return TrunkPlacerType.FORKING_TRUNK_PLACER;
   }

   public List<FoliagePlacer.TreeNode> generate(ModifiableTestableWorld world, Random random, int trunkHeight, BlockPos pos, Set<BlockPos> placedStates, BlockBox box, TreeFeatureConfig config) {
      setToDirt(world, pos.down());
      List<FoliagePlacer.TreeNode> list = Lists.newArrayList();
      Direction direction = Direction.Type.HORIZONTAL.random(random);
      int i = trunkHeight - random.nextInt(4) - 1;
      int j = 3 - random.nextInt(3);
      BlockPos.Mutable mutable = new BlockPos.Mutable();
      int k = pos.getX();
      int l = pos.getZ();
      int m = 0;

      int o;
      for(int n = 0; n < trunkHeight; ++n) {
         o = pos.getY() + n;
         if (n >= i && j > 0) {
            k += direction.getOffsetX();
            l += direction.getOffsetZ();
            --j;
         }

         if (getAndSetState(world, random, mutable.set(k, o, l), placedStates, box, config)) {
            m = o + 1;
         }
      }

      list.add(new FoliagePlacer.TreeNode(new BlockPos(k, m, l), 1, false));
      k = pos.getX();
      l = pos.getZ();
      Direction direction2 = Direction.Type.HORIZONTAL.random(random);
      if (direction2 != direction) {
         o = i - random.nextInt(2) - 1;
         int q = 1 + random.nextInt(3);
         m = 0;

         for(int r = o; r < trunkHeight && q > 0; --q) {
            if (r >= 1) {
               int s = pos.getY() + r;
               k += direction2.getOffsetX();
               l += direction2.getOffsetZ();
               if (getAndSetState(world, random, mutable.set(k, s, l), placedStates, box, config)) {
                  m = s + 1;
               }
            }

            ++r;
         }

         if (m > 1) {
            list.add(new FoliagePlacer.TreeNode(new BlockPos(k, m, l), 0, false));
         }
      }

      return list;
   }
}
