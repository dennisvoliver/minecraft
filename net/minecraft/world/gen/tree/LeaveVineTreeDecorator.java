package net.minecraft.world.gen.tree;

import com.mojang.serialization.Codec;
import java.util.List;
import java.util.Random;
import java.util.Set;
import net.minecraft.block.VineBlock;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ModifiableTestableWorld;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.feature.Feature;

public class LeaveVineTreeDecorator extends TreeDecorator {
   public static final Codec<LeaveVineTreeDecorator> CODEC = Codec.unit(() -> {
      return INSTANCE;
   });
   public static final LeaveVineTreeDecorator INSTANCE = new LeaveVineTreeDecorator();

   protected TreeDecoratorType<?> getType() {
      return TreeDecoratorType.LEAVE_VINE;
   }

   public void generate(StructureWorldAccess world, Random random, List<BlockPos> logPositions, List<BlockPos> leavesPositions, Set<BlockPos> placedStates, BlockBox box) {
      leavesPositions.forEach((pos) -> {
         BlockPos blockPos4;
         if (random.nextInt(4) == 0) {
            blockPos4 = pos.west();
            if (Feature.isAir(world, blockPos4)) {
               this.placeVines(world, blockPos4, VineBlock.EAST, placedStates, box);
            }
         }

         if (random.nextInt(4) == 0) {
            blockPos4 = pos.east();
            if (Feature.isAir(world, blockPos4)) {
               this.placeVines(world, blockPos4, VineBlock.WEST, placedStates, box);
            }
         }

         if (random.nextInt(4) == 0) {
            blockPos4 = pos.north();
            if (Feature.isAir(world, blockPos4)) {
               this.placeVines(world, blockPos4, VineBlock.SOUTH, placedStates, box);
            }
         }

         if (random.nextInt(4) == 0) {
            blockPos4 = pos.south();
            if (Feature.isAir(world, blockPos4)) {
               this.placeVines(world, blockPos4, VineBlock.NORTH, placedStates, box);
            }
         }

      });
   }

   /**
    * Places a vine at a given position and then up to 4 more vines going downwards.
    */
   private void placeVines(ModifiableTestableWorld world, BlockPos pos, BooleanProperty side, Set<BlockPos> placedStates, BlockBox box) {
      this.placeVine(world, pos, side, placedStates, box);
      int i = 4;

      for(pos = pos.down(); Feature.isAir(world, pos) && i > 0; --i) {
         this.placeVine(world, pos, side, placedStates, box);
         pos = pos.down();
      }

   }
}
