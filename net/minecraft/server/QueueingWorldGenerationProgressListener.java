package net.minecraft.server;

import java.util.concurrent.Executor;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.thread.TaskExecutor;
import net.minecraft.world.chunk.ChunkStatus;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class QueueingWorldGenerationProgressListener implements WorldGenerationProgressListener {
   private final WorldGenerationProgressListener progressListener;
   private final TaskExecutor<Runnable> queue;

   public QueueingWorldGenerationProgressListener(WorldGenerationProgressListener progressListener, Executor executor) {
      this.progressListener = progressListener;
      this.queue = TaskExecutor.create(executor, "progressListener");
   }

   public void start(ChunkPos spawnPos) {
      this.queue.send(() -> {
         this.progressListener.start(spawnPos);
      });
   }

   public void setChunkStatus(ChunkPos pos, @Nullable ChunkStatus status) {
      this.queue.send(() -> {
         this.progressListener.setChunkStatus(pos, status);
      });
   }

   public void stop() {
      WorldGenerationProgressListener var10001 = this.progressListener;
      this.queue.send(var10001::stop);
   }
}
