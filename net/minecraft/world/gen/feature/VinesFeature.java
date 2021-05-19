package net.minecraft.world.gen.feature;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.VineBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.chunk.ChunkGenerator;

public class VinesFeature extends Feature<DefaultFeatureConfig> {
   private static final Direction[] DIRECTIONS = Direction.values();

   public VinesFeature(Codec<DefaultFeatureConfig> codec) {
      super(codec);
   }

   public boolean generate(StructureWorldAccess structureWorldAccess, ChunkGenerator chunkGenerator, Random random, BlockPos blockPos, DefaultFeatureConfig defaultFeatureConfig) {
      BlockPos.Mutable mutable = blockPos.mutableCopy();

      for(int i = 64; i < 256; ++i) {
         mutable.set(blockPos);
         mutable.move(random.nextInt(4) - random.nextInt(4), 0, random.nextInt(4) - random.nextInt(4));
         mutable.setY(i);
         if (structureWorldAccess.isAir(mutable)) {
            Direction[] var8 = DIRECTIONS;
            int var9 = var8.length;

            for(int var10 = 0; var10 < var9; ++var10) {
               Direction direction = var8[var10];
               if (direction != Direction.DOWN && VineBlock.shouldConnectTo(structureWorldAccess, mutable, direction)) {
                  structureWorldAccess.setBlockState(mutable, (BlockState)Blocks.VINE.getDefaultState().with(VineBlock.getFacingProperty(direction), true), 2);
                  break;
               }
            }
         }
      }

      return true;
   }
}
