package net.minecraft.server;

import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.ChunkStatus;
import org.jetbrains.annotations.Nullable;

public interface WorldGenerationProgressListener {
   void start(ChunkPos spawnPos);

   void setChunkStatus(ChunkPos pos, @Nullable ChunkStatus status);

   void stop();
}
