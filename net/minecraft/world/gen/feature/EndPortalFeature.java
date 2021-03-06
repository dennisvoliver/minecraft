package net.minecraft.world.gen.feature;

import java.util.Iterator;
import java.util.Random;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.WallTorchBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.chunk.ChunkGenerator;

public class EndPortalFeature extends Feature<DefaultFeatureConfig> {
   public static final BlockPos ORIGIN;
   private final boolean open;

   public EndPortalFeature(boolean open) {
      super(DefaultFeatureConfig.CODEC);
      this.open = open;
   }

   public boolean generate(StructureWorldAccess structureWorldAccess, ChunkGenerator chunkGenerator, Random random, BlockPos blockPos, DefaultFeatureConfig defaultFeatureConfig) {
      Iterator var6 = BlockPos.iterate(new BlockPos(blockPos.getX() - 4, blockPos.getY() - 1, blockPos.getZ() - 4), new BlockPos(blockPos.getX() + 4, blockPos.getY() + 32, blockPos.getZ() + 4)).iterator();

      while(true) {
         BlockPos blockPos2;
         boolean bl;
         do {
            if (!var6.hasNext()) {
               for(int i = 0; i < 4; ++i) {
                  this.setBlockState(structureWorldAccess, blockPos.up(i), Blocks.BEDROCK.getDefaultState());
               }

               BlockPos blockPos3 = blockPos.up(2);
               Iterator var11 = Direction.Type.HORIZONTAL.iterator();

               while(var11.hasNext()) {
                  Direction direction = (Direction)var11.next();
                  this.setBlockState(structureWorldAccess, blockPos3.offset(direction), (BlockState)Blocks.WALL_TORCH.getDefaultState().with(WallTorchBlock.FACING, direction));
               }

               return true;
            }

            blockPos2 = (BlockPos)var6.next();
            bl = blockPos2.isWithinDistance(blockPos, 2.5D);
         } while(!bl && !blockPos2.isWithinDistance(blockPos, 3.5D));

         if (blockPos2.getY() < blockPos.getY()) {
            if (bl) {
               this.setBlockState(structureWorldAccess, blockPos2, Blocks.BEDROCK.getDefaultState());
            } else if (blockPos2.getY() < blockPos.getY()) {
               this.setBlockState(structureWorldAccess, blockPos2, Blocks.END_STONE.getDefaultState());
            }
         } else if (blockPos2.getY() > blockPos.getY()) {
            this.setBlockState(structureWorldAccess, blockPos2, Blocks.AIR.getDefaultState());
         } else if (!bl) {
            this.setBlockState(structureWorldAccess, blockPos2, Blocks.BEDROCK.getDefaultState());
         } else if (this.open) {
            this.setBlockState(structureWorldAccess, new BlockPos(blockPos2), Blocks.END_PORTAL.getDefaultState());
         } else {
            this.setBlockState(structureWorldAccess, new BlockPos(blockPos2), Blocks.AIR.getDefaultState());
         }
      }
   }

   static {
      ORIGIN = BlockPos.ORIGIN;
   }
}
