package net.minecraft.server.rcon;

import java.util.concurrent.atomic.AtomicInteger;
import net.minecraft.util.logging.UncaughtExceptionHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

public abstract class RconBase implements Runnable {
   private static final Logger LOGGER = LogManager.getLogger();
   private static final AtomicInteger THREAD_COUNTER = new AtomicInteger(0);
   protected volatile boolean running;
   protected final String description;
   @Nullable
   protected Thread thread;

   protected RconBase(String description) {
      this.description = description;
   }

   public synchronized boolean start() {
      if (this.running) {
         return true;
      } else {
         this.running = true;
         this.thread = new Thread(this, this.description + " #" + THREAD_COUNTER.incrementAndGet());
         this.thread.setUncaughtExceptionHandler(new UncaughtExceptionHandler(LOGGER));
         this.thread.start();
         LOGGER.info((String)"Thread {} started", (Object)this.description);
         return true;
      }
   }

   public synchronized void stop() {
      this.running = false;
      if (null != this.thread) {
         int i = 0;

         while(this.thread.isAlive()) {
            try {
               this.thread.join(1000L);
               ++i;
               if (i >= 5) {
                  LOGGER.warn((String)"Waited {} seconds attempting force stop!", (Object)i);
               } else if (this.thread.isAlive()) {
                  LOGGER.warn((String)"Thread {} ({}) failed to exit after {} second(s)", (Object)this, this.thread.getState(), i, new Exception("Stack:"));
                  this.thread.interrupt();
               }
            } catch (InterruptedException var3) {
            }
         }

         LOGGER.info((String)"Thread {} stopped", (Object)this.description);
         this.thread = null;
      }
   }

   public boolean isRunning() {
      return this.running;
   }
}
