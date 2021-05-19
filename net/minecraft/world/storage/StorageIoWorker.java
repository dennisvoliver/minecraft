package net.minecraft.world.storage;

import com.google.common.collect.Maps;
import com.mojang.datafixers.util.Either;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Unit;
import net.minecraft.util.Util;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.thread.TaskExecutor;
import net.minecraft.util.thread.TaskQueue;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

public class StorageIoWorker implements AutoCloseable {
   private static final Logger LOGGER = LogManager.getLogger();
   private final AtomicBoolean closed = new AtomicBoolean();
   private final TaskExecutor<TaskQueue.PrioritizedTask> field_24468;
   private final RegionBasedStorage storage;
   private final Map<ChunkPos, StorageIoWorker.Result> results = Maps.newLinkedHashMap();

   protected StorageIoWorker(File file, boolean bl, String string) {
      this.storage = new RegionBasedStorage(file, bl);
      this.field_24468 = new TaskExecutor(new TaskQueue.Prioritized(StorageIoWorker.Priority.values().length), Util.getIoWorkerExecutor(), "IOWorker-" + string);
   }

   public CompletableFuture<Void> setResult(ChunkPos pos, CompoundTag nbt) {
      return this.run(() -> {
         StorageIoWorker.Result result = (StorageIoWorker.Result)this.results.computeIfAbsent(pos, (chunkPos) -> {
            return new StorageIoWorker.Result(nbt);
         });
         result.nbt = nbt;
         return Either.left(result.future);
      }).thenCompose(Function.identity());
   }

   @Nullable
   public CompoundTag getNbt(ChunkPos pos) throws IOException {
      CompletableFuture completableFuture = this.run(() -> {
         StorageIoWorker.Result result = (StorageIoWorker.Result)this.results.get(pos);
         if (result != null) {
            return Either.left(result.nbt);
         } else {
            try {
               CompoundTag compoundTag = this.storage.getTagAt(pos);
               return Either.left(compoundTag);
            } catch (Exception var4) {
               LOGGER.warn((String)"Failed to read chunk {}", (Object)pos, (Object)var4);
               return Either.right(var4);
            }
         }
      });

      try {
         return (CompoundTag)completableFuture.join();
      } catch (CompletionException var4) {
         if (var4.getCause() instanceof IOException) {
            throw (IOException)var4.getCause();
         } else {
            throw var4;
         }
      }
   }

   public CompletableFuture<Void> completeAll() {
      CompletableFuture<Void> completableFuture = this.run(() -> {
         return Either.left(CompletableFuture.allOf((CompletableFuture[])this.results.values().stream().map((result) -> {
            return result.future;
         }).toArray((i) -> {
            return new CompletableFuture[i];
         })));
      }).thenCompose(Function.identity());
      return completableFuture.thenCompose((void_) -> {
         return this.run(() -> {
            try {
               this.storage.method_26982();
               return Either.left((Object)null);
            } catch (Exception var2) {
               LOGGER.warn((String)"Failed to synchronized chunks", (Throwable)var2);
               return Either.right(var2);
            }
         });
      });
   }

   private <T> CompletableFuture<T> run(Supplier<Either<T, Exception>> supplier) {
      return this.field_24468.method_27918((messageListener) -> {
         return new TaskQueue.PrioritizedTask(StorageIoWorker.Priority.HIGH.ordinal(), () -> {
            if (!this.closed.get()) {
               messageListener.send(supplier.get());
            }

            this.method_27945();
         });
      });
   }

   private void writeResult() {
      Iterator<Entry<ChunkPos, StorageIoWorker.Result>> iterator = this.results.entrySet().iterator();
      if (iterator.hasNext()) {
         Entry<ChunkPos, StorageIoWorker.Result> entry = (Entry)iterator.next();
         iterator.remove();
         this.write((ChunkPos)entry.getKey(), (StorageIoWorker.Result)entry.getValue());
         this.method_27945();
      }
   }

   private void method_27945() {
      this.field_24468.send(new TaskQueue.PrioritizedTask(StorageIoWorker.Priority.LOW.ordinal(), this::writeResult));
   }

   private void write(ChunkPos pos, StorageIoWorker.Result result) {
      try {
         this.storage.write(pos, result.nbt);
         result.future.complete((Object)null);
      } catch (Exception var4) {
         LOGGER.error((String)"Failed to store chunk {}", (Object)pos, (Object)var4);
         result.future.completeExceptionally(var4);
      }

   }

   public void close() throws IOException {
      if (this.closed.compareAndSet(false, true)) {
         CompletableFuture completableFuture = this.field_24468.ask((messageListener) -> {
            return new TaskQueue.PrioritizedTask(StorageIoWorker.Priority.HIGH.ordinal(), () -> {
               messageListener.send(Unit.INSTANCE);
            });
         });

         try {
            completableFuture.join();
         } catch (CompletionException var4) {
            if (var4.getCause() instanceof IOException) {
               throw (IOException)var4.getCause();
            }

            throw var4;
         }

         this.field_24468.close();
         this.results.forEach(this::write);
         this.results.clear();

         try {
            this.storage.close();
         } catch (Exception var3) {
            LOGGER.error((String)"Failed to close storage", (Throwable)var3);
         }

      }
   }

   static class Result {
      private CompoundTag nbt;
      private final CompletableFuture<Void> future = new CompletableFuture();

      public Result(CompoundTag compoundTag) {
         this.nbt = compoundTag;
      }
   }

   static enum Priority {
      HIGH,
      LOW;
   }
}
