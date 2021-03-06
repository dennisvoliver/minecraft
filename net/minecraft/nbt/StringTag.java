package net.minecraft.nbt;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Objects;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;

public class StringTag implements Tag {
   public static final TagReader<StringTag> READER = new TagReader<StringTag>() {
      public StringTag read(DataInput dataInput, int i, PositionTracker positionTracker) throws IOException {
         positionTracker.add(288L);
         String string = dataInput.readUTF();
         positionTracker.add((long)(16 * string.length()));
         return StringTag.of(string);
      }

      public String getCrashReportName() {
         return "STRING";
      }

      public String getCommandFeedbackName() {
         return "TAG_String";
      }

      public boolean isImmutable() {
         return true;
      }
   };
   private static final StringTag EMPTY = new StringTag("");
   private final String value;

   private StringTag(String value) {
      Objects.requireNonNull(value, "Null string not allowed");
      this.value = value;
   }

   public static StringTag of(String value) {
      return value.isEmpty() ? EMPTY : new StringTag(value);
   }

   public void write(DataOutput output) throws IOException {
      output.writeUTF(this.value);
   }

   public byte getType() {
      return 8;
   }

   public TagReader<StringTag> getReader() {
      return READER;
   }

   public String toString() {
      return escape(this.value);
   }

   public StringTag copy() {
      return this;
   }

   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else {
         return o instanceof StringTag && Objects.equals(this.value, ((StringTag)o).value);
      }
   }

   public int hashCode() {
      return this.value.hashCode();
   }

   public String asString() {
      return this.value;
   }

   public Text toText(String indent, int depth) {
      String string = escape(this.value);
      String string2 = string.substring(0, 1);
      Text text = (new LiteralText(string.substring(1, string.length() - 1))).formatted(GREEN);
      return (new LiteralText(string2)).append(text).append(string2);
   }

   public static String escape(String value) {
      StringBuilder stringBuilder = new StringBuilder(" ");
      char c = 0;

      for(int i = 0; i < value.length(); ++i) {
         char d = value.charAt(i);
         if (d == '\\') {
            stringBuilder.append('\\');
         } else if (d == '"' || d == '\'') {
            if (c == 0) {
               c = d == '"' ? 39 : 34;
            }

            if (c == d) {
               stringBuilder.append('\\');
            }
         }

         stringBuilder.append(d);
      }

      if (c == 0) {
         c = 34;
      }

      stringBuilder.setCharAt(0, (char)c);
      stringBuilder.append((char)c);
      return stringBuilder.toString();
   }
}
