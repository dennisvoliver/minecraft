package net.minecraft.resource;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.Unit;
import net.minecraft.util.Util;
import net.minecraft.util.profiler.DummyProfiler;

public class ResourceReloader<S> implements ResourceReloadMonitor {
   protected final ResourceManager manager;
   protected final CompletableFuture<Unit> prepareStageFuture = new CompletableFuture();
   protected final CompletableFuture<List<S>> applyStageFuture;
   private final Set<ResourceReloadListener> waitingListeners;
   private final int listenerCount;
   private int applyingCount;
   private int appliedCount;
   private final AtomicInteger preparingCount = new AtomicInteger();
   private final AtomicInteger preparedCount = new AtomicInteger();

   public static ResourceReloader<Void> create(ResourceManager manager, List<ResourceReloadListener> listeners, Executor prepareExecutor, Executor applyExecutor, CompletableFuture<Unit> initialStage) {
      return new ResourceReloader(prepareExecutor, applyExecutor, manager, listeners, (synchronizer, resourceManager, resourceReloadListener, executor2, executor3) -> {
         return resourceReloadListener.reload(synchronizer, resourceManager, DummyProfiler.INSTANCE, DummyProfiler.INSTANCE, prepareExecutor, executor3);
      }, initialStage);
   }

   protected ResourceReloader(Executor prepareExecutor, final Executor applyExecutor, ResourceManager manager, List<ResourceReloadListener> listeners, ResourceReloader.Factory<S> creator, CompletableFuture<Unit> initialStage) {
      this.manager = manager;
      this.listenerCount = listeners.size();
      this.preparingCount.incrementAndGet();
      AtomicInteger var10001 = this.preparedCount;
      initialStage.thenRun(var10001::incrementAndGet);
      List<CompletableFuture<S>> list = Lists.newArrayList();
      final CompletableFuture<?> completableFuture = initialStage;
      this.waitingListeners = Sets.newHashSet((Iterable)listeners);

      CompletableFuture completableFuture3;
      for(Iterator var9 = listeners.iterator(); var9.hasNext(); completableFuture = completableFuture3) {
         final ResourceReloadListener resourceReloadListener = (ResourceReloadListener)var9.next();
         completableFuture3 = creator.create(new ResourceReloadListener.Synchronizer() {
            public <T> CompletableFuture<T> whenPrepared(T preparedObject) {
               applyExecutor.execute(() -> {
                  ResourceReloader.this.waitingListeners.remove(resourceReloadListener);
                  if (ResourceReloader.this.waitingListeners.isEmpty()) {
                     ResourceReloader.this.prepareStageFuture.complete(Unit.INSTANCE);
                  }

               });
               return ResourceReloader.this.prepareStageFuture.thenCombine(completableFuture, (unit, object2) -> {
                  return preparedObject;
               });
            }
         }, manager, resourceReloadListener, (runnable) -> {
            this.preparingCount.incrementAndGet();
            prepareExecutor.execute(() -> {
               runnable.run();
               this.preparedCount.incrementAndGet();
            });
         }, (runnable) -> {
            ++this.applyingCount;
            applyExecutor.execute(() -> {
               runnable.run();
               ++this.appliedCount;
            });
         });
         list.add(completableFuture3);
      }

      this.applyStageFuture = Util.combine(list);
   }

   public CompletableFuture<Unit> whenComplete() {
      return this.applyStageFuture.thenApply((list) -> {
         return Unit.INSTANCE;
      });
   }

   @Environment(EnvType.CLIENT)
   public float getProgress() {
      int i = this.listenerCount - this.waitingListeners.size();
      float f = (float)(this.preparedCount.get() * 2 + this.appliedCount * 2 + i * 1);
      float g = (float)(this.preparingCount.get() * 2 + this.applyingCount * 2 + this.listenerCount * 1);
      return f / g;
   }

   @Environment(EnvType.CLIENT)
   public boolean isPrepareStageComplete() {
      return this.prepareStageFuture.isDone();
   }

   @Environment(EnvType.CLIENT)
   public boolean isApplyStageComplete() {
      return this.applyStageFuture.isDone();
   }

   @Environment(EnvType.CLIENT)
   public void throwExceptions() {
      if (this.applyStageFuture.isCompletedExceptionally()) {
         this.applyStageFuture.join();
      }

   }

   public interface Factory<S> {
      CompletableFuture<S> create(ResourceReloadListener.Synchronizer helper, ResourceManager manager, ResourceReloadListener listener, Executor prepareExecutor, Executor applyExecutor);
   }
}
