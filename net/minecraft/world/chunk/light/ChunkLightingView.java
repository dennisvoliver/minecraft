package net.minecraft.world.chunk.light;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.world.chunk.ChunkNibbleArray;
import org.jetbrains.annotations.Nullable;

public interface ChunkLightingView extends LightingView {
   @Nullable
   ChunkNibbleArray getLightSection(ChunkSectionPos pos);

   int getLightLevel(BlockPos blockPos);

   public static enum Empty implements ChunkLightingView {
      INSTANCE;

      @Nullable
      public ChunkNibbleArray getLightSection(ChunkSectionPos pos) {
         return null;
      }

      public int getLightLevel(BlockPos blockPos) {
         return 0;
      }

      public void setSectionStatus(ChunkSectionPos pos, boolean notReady) {
      }
   }
}
