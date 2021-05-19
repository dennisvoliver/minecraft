package net.minecraft.nbt;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;

public class EndTag implements Tag {
   public static final TagReader<EndTag> READER = new TagReader<EndTag>() {
      public EndTag read(DataInput dataInput, int i, PositionTracker positionTracker) {
         positionTracker.add(64L);
         return EndTag.INSTANCE;
      }

      public String getCrashReportName() {
         return "END";
      }

      public String getCommandFeedbackName() {
         return "TAG_End";
      }

      public boolean isImmutable() {
         return true;
      }
   };
   public static final EndTag INSTANCE = new EndTag();

   private EndTag() {
   }

   public void write(DataOutput output) throws IOException {
   }

   public byte getType() {
      return 0;
   }

   public TagReader<EndTag> getReader() {
      return READER;
   }

   public String toString() {
      return "END";
   }

   public EndTag copy() {
      return this;
   }

   public Text toText(String indent, int depth) {
      return LiteralText.EMPTY;
   }
}
