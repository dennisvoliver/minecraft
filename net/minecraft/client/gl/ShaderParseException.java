package net.minecraft.client.gl;

import com.google.common.collect.Lists;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class ShaderParseException extends IOException {
   private final List<ShaderParseException.JsonStackTrace> traces = Lists.newArrayList();
   private final String message;

   public ShaderParseException(String message) {
      this.traces.add(new ShaderParseException.JsonStackTrace());
      this.message = message;
   }

   public ShaderParseException(String message, Throwable cause) {
      super(cause);
      this.traces.add(new ShaderParseException.JsonStackTrace());
      this.message = message;
   }

   public void addFaultyElement(String jsonKey) {
      ((ShaderParseException.JsonStackTrace)this.traces.get(0)).add(jsonKey);
   }

   public void addFaultyFile(String path) {
      ((ShaderParseException.JsonStackTrace)this.traces.get(0)).fileName = path;
      this.traces.add(0, new ShaderParseException.JsonStackTrace());
   }

   public String getMessage() {
      return "Invalid " + this.traces.get(this.traces.size() - 1) + ": " + this.message;
   }

   public static ShaderParseException wrap(Exception cause) {
      if (cause instanceof ShaderParseException) {
         return (ShaderParseException)cause;
      } else {
         String string = cause.getMessage();
         if (cause instanceof FileNotFoundException) {
            string = "File not found";
         }

         return new ShaderParseException(string, cause);
      }
   }

   @Environment(EnvType.CLIENT)
   public static class JsonStackTrace {
      @Nullable
      private String fileName;
      private final List<String> faultyElements;

      private JsonStackTrace() {
         this.faultyElements = Lists.newArrayList();
      }

      private void add(String element) {
         this.faultyElements.add(0, element);
      }

      public String joinStackTrace() {
         return StringUtils.join((Iterable)this.faultyElements, "->");
      }

      public String toString() {
         if (this.fileName != null) {
            return this.faultyElements.isEmpty() ? this.fileName : this.fileName + " " + this.joinStackTrace();
         } else {
            return this.faultyElements.isEmpty() ? "(Unknown file)" : "(Unknown file) " + this.joinStackTrace();
         }
      }
   }
}
