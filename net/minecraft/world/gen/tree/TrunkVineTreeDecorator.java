package net.minecraft.world.gen.tree;

import com.mojang.serialization.Codec;
import java.util.List;
import java.util.Random;
import java.util.Set;
import net.minecraft.block.VineBlock;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.feature.Feature;

public class TrunkVineTreeDecorator extends TreeDecorator {
   public static final Codec<TrunkVineTreeDecorator> CODEC = Codec.unit(() -> {
      return INSTANCE;
   });
   public static final TrunkVineTreeDecorator INSTANCE = new TrunkVineTreeDecorator();

   protected TreeDecoratorType<?> getType() {
      return TreeDecoratorType.TRUNK_VINE;
   }

   public void generate(StructureWorldAccess world, Random random, List<BlockPos> logPositions, List<BlockPos> leavesPositions, Set<BlockPos> placedStates, BlockBox box) {
      logPositions.forEach((pos) -> {
         BlockPos blockPos4;
         if (random.nextInt(3) > 0) {
            blockPos4 = pos.west();
            if (Feature.isAir(world, blockPos4)) {
               this.placeVine(world, blockPos4, VineBlock.EAST, placedStates, box);
            }
         }

         if (random.nextInt(3) > 0) {
            blockPos4 = pos.east();
            if (Feature.isAir(world, blockPos4)) {
               this.placeVine(world, blockPos4, VineBlock.WEST, placedStates, box);
            }
         }

         if (random.nextInt(3) > 0) {
            blockPos4 = pos.north();
            if (Feature.isAir(world, blockPos4)) {
               this.placeVine(world, blockPos4, VineBlock.SOUTH, placedStates, box);
            }
         }

         if (random.nextInt(3) > 0) {
            blockPos4 = pos.south();
            if (Feature.isAir(world, blockPos4)) {
               this.placeVine(world, blockPos4, VineBlock.NORTH, placedStates, box);
            }
         }

      });
   }
}
