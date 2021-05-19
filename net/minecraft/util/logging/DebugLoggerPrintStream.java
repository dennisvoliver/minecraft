package net.minecraft.util.logging;

import java.io.OutputStream;

public class DebugLoggerPrintStream extends LoggerPrintStream {
   public DebugLoggerPrintStream(String string, OutputStream outputStream) {
      super(string, outputStream);
   }

   protected void log(String message) {
      StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
      StackTraceElement stackTraceElement = stackTraceElements[Math.min(3, stackTraceElements.length)];
      LOGGER.info((String)"[{}]@.({}:{}): {}", (Object)this.name, stackTraceElement.getFileName(), stackTraceElement.getLineNumber(), message);
   }
}
