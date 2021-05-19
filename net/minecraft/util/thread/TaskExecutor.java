package net.minecraft.util.thread;

import it.unimi.dsi.fastutil.ints.Int2BooleanFunction;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.atomic.AtomicInteger;
import net.minecraft.SharedConstants;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class TaskExecutor<T> implements MessageListener<T>, AutoCloseable, Runnable {
   private static final Logger LOGGER = LogManager.getLogger();
   private final AtomicInteger stateFlags = new AtomicInteger(0);
   public final TaskQueue<? super T, ? extends Runnable> queue;
   private final Executor executor;
   private final String name;

   public static TaskExecutor<Runnable> create(Executor executor, String name) {
      return new TaskExecutor(new TaskQueue.Simple(new ConcurrentLinkedQueue()), executor, name);
   }

   public TaskExecutor(TaskQueue<? super T, ? extends Runnable> queue, Executor executor, String name) {
      this.executor = executor;
      this.queue = queue;
      this.name = name;
   }

   private boolean unpause() {
      int i;
      do {
         i = this.stateFlags.get();
         if ((i & 3) != 0) {
            return false;
         }
      } while(!this.stateFlags.compareAndSet(i, i | 2));

      return true;
   }

   private void pause() {
      int i;
      do {
         i = this.stateFlags.get();
      } while(!this.stateFlags.compareAndSet(i, i & -3));

   }

   private boolean hasMessages() {
      if ((this.stateFlags.get() & 1) != 0) {
         return false;
      } else {
         return !this.queue.isEmpty();
      }
   }

   public void close() {
      int i;
      do {
         i = this.stateFlags.get();
      } while(!this.stateFlags.compareAndSet(i, i | 1));

   }

   private boolean isUnpaused() {
      return (this.stateFlags.get() & 2) != 0;
   }

   private boolean runNext() {
      if (!this.isUnpaused()) {
         return false;
      } else {
         Runnable runnable = (Runnable)this.queue.poll();
         if (runnable == null) {
            return false;
         } else {
            String string2;
            Thread thread2;
            if (SharedConstants.isDevelopment) {
               thread2 = Thread.currentThread();
               string2 = thread2.getName();
               thread2.setName(this.name);
            } else {
               thread2 = null;
               string2 = null;
            }

            runnable.run();
            if (thread2 != null) {
               thread2.setName(string2);
            }

            return true;
         }
      }
   }

   public void run() {
      try {
         this.runWhile((i) -> {
            return i == 0;
         });
      } finally {
         this.pause();
         this.execute();
      }

   }

   public void send(T message) {
      this.queue.add(message);
      this.execute();
   }

   private void execute() {
      if (this.hasMessages() && this.unpause()) {
         try {
            this.executor.execute(this);
         } catch (RejectedExecutionException var4) {
            try {
               this.executor.execute(this);
            } catch (RejectedExecutionException var3) {
               LOGGER.error((String)"Cound not schedule mailbox", (Throwable)var3);
            }
         }
      }

   }

   private int runWhile(Int2BooleanFunction condition) {
      int i;
      for(i = 0; condition.get(i) && this.runNext(); ++i) {
      }

      return i;
   }

   public String toString() {
      return this.name + " " + this.stateFlags.get() + " " + this.queue.isEmpty();
   }

   public String getName() {
      return this.name;
   }
}
