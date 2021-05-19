package net.minecraft.util.thread;

import com.google.common.collect.Queues;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Queue;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.jetbrains.annotations.Nullable;

public interface TaskQueue<T, F> {
   @Nullable
   F poll();

   boolean add(T message);

   boolean isEmpty();

   public static final class Prioritized implements TaskQueue<TaskQueue.PrioritizedTask, Runnable> {
      private final List<Queue<Runnable>> queues;

      public Prioritized(int priorityCount) {
         this.queues = (List)IntStream.range(0, priorityCount).mapToObj((i) -> {
            return Queues.newConcurrentLinkedQueue();
         }).collect(Collectors.toList());
      }

      @Nullable
      public Runnable poll() {
         Iterator var1 = this.queues.iterator();

         Runnable runnable;
         do {
            if (!var1.hasNext()) {
               return null;
            }

            Queue<Runnable> queue = (Queue)var1.next();
            runnable = (Runnable)queue.poll();
         } while(runnable == null);

         return runnable;
      }

      public boolean add(TaskQueue.PrioritizedTask prioritizedTask) {
         int i = prioritizedTask.getPriority();
         ((Queue)this.queues.get(i)).add(prioritizedTask);
         return true;
      }

      public boolean isEmpty() {
         return this.queues.stream().allMatch(Collection::isEmpty);
      }
   }

   public static final class PrioritizedTask implements Runnable {
      private final int priority;
      private final Runnable runnable;

      public PrioritizedTask(int priority, Runnable runnable) {
         this.priority = priority;
         this.runnable = runnable;
      }

      public void run() {
         this.runnable.run();
      }

      public int getPriority() {
         return this.priority;
      }
   }

   public static final class Simple<T> implements TaskQueue<T, T> {
      private final Queue<T> queue;

      public Simple(Queue<T> queue) {
         this.queue = queue;
      }

      @Nullable
      public T poll() {
         return this.queue.poll();
      }

      public boolean add(T message) {
         return this.queue.add(message);
      }

      public boolean isEmpty() {
         return this.queue.isEmpty();
      }
   }
}
