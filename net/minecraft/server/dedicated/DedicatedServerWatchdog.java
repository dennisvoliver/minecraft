package net.minecraft.server.dedicated;

import com.google.common.collect.Streams;
import java.io.File;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import java.util.stream.Collectors;
import net.minecraft.Bootstrap;
import net.minecraft.util.Util;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.crash.CrashReportSection;
import net.minecraft.world.GameRules;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class DedicatedServerWatchdog implements Runnable {
   private static final Logger LOGGER = LogManager.getLogger();
   private final MinecraftDedicatedServer server;
   private final long maxTickTime;

   public DedicatedServerWatchdog(MinecraftDedicatedServer server) {
      this.server = server;
      this.maxTickTime = server.getMaxTickTime();
   }

   public void run() {
      while(this.server.isRunning()) {
         long l = this.server.getServerStartTime();
         long m = Util.getMeasuringTimeMs();
         long n = m - l;
         if (n > this.maxTickTime) {
            LOGGER.fatal((String)"A single server tick took {} seconds (should be max {})", (Object)String.format(Locale.ROOT, "%.2f", (float)n / 1000.0F), (Object)String.format(Locale.ROOT, "%.2f", 0.05F));
            LOGGER.fatal("Considering it to be crashed, server will forcibly shutdown.");
            ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
            ThreadInfo[] threadInfos = threadMXBean.dumpAllThreads(true, true);
            StringBuilder stringBuilder = new StringBuilder();
            Error error = new Error("Watchdog");
            ThreadInfo[] var11 = threadInfos;
            int var12 = threadInfos.length;

            for(int var13 = 0; var13 < var12; ++var13) {
               ThreadInfo threadInfo = var11[var13];
               if (threadInfo.getThreadId() == this.server.getThread().getId()) {
                  error.setStackTrace(threadInfo.getStackTrace());
               }

               stringBuilder.append(threadInfo);
               stringBuilder.append("\n");
            }

            CrashReport crashReport = new CrashReport("Watching Server", error);
            this.server.populateCrashReport(crashReport);
            CrashReportSection crashReportSection = crashReport.addElement("Thread Dump");
            crashReportSection.add("Threads", (Object)stringBuilder);
            CrashReportSection crashReportSection2 = crashReport.addElement("Performance stats");
            crashReportSection2.add("Random tick rate", () -> {
               return ((GameRules.IntRule)this.server.getSaveProperties().getGameRules().get(GameRules.RANDOM_TICK_SPEED)).toString();
            });
            crashReportSection2.add("Level stats", () -> {
               return (String)Streams.stream(this.server.getWorlds()).map((serverWorld) -> {
                  return serverWorld.getRegistryKey() + ": " + serverWorld.method_31268();
               }).collect(Collectors.joining(",\n"));
            });
            Bootstrap.println("Crash report:\n" + crashReport.asString());
            File file = new File(new File(this.server.getRunDirectory(), "crash-reports"), "crash-" + (new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss")).format(new Date()) + "-server.txt");
            if (crashReport.writeToFile(file)) {
               LOGGER.error((String)"This crash report has been saved to: {}", (Object)file.getAbsolutePath());
            } else {
               LOGGER.error("We were unable to save this crash report to disk.");
            }

            this.shutdown();
         }

         try {
            Thread.sleep(l + this.maxTickTime - m);
         } catch (InterruptedException var15) {
         }
      }

   }

   private void shutdown() {
      try {
         Timer timer = new Timer();
         timer.schedule(new TimerTask() {
            public void run() {
               Runtime.getRuntime().halt(1);
            }
         }, 10000L);
         System.exit(1);
      } catch (Throwable var2) {
         Runtime.getRuntime().halt(1);
      }

   }
}
