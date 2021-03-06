package net.minecraft.nbt;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;

public class IntTag extends AbstractNumberTag {
   public static final TagReader<IntTag> READER = new TagReader<IntTag>() {
      public IntTag read(DataInput dataInput, int i, PositionTracker positionTracker) throws IOException {
         positionTracker.add(96L);
         return IntTag.of(dataInput.readInt());
      }

      public String getCrashReportName() {
         return "INT";
      }

      public String getCommandFeedbackName() {
         return "TAG_Int";
      }

      public boolean isImmutable() {
         return true;
      }
   };
   private final int value;

   private IntTag(int value) {
      this.value = value;
   }

   public static IntTag of(int value) {
      return value >= -128 && value <= 1024 ? IntTag.Cache.VALUES[value + 128] : new IntTag(value);
   }

   public void write(DataOutput output) throws IOException {
      output.writeInt(this.value);
   }

   public byte getType() {
      return 3;
   }

   public TagReader<IntTag> getReader() {
      return READER;
   }

   public String toString() {
      return String.valueOf(this.value);
   }

   public IntTag copy() {
      return this;
   }

   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else {
         return o instanceof IntTag && this.value == ((IntTag)o).value;
      }
   }

   public int hashCode() {
      return this.value;
   }

   public Text toText(String indent, int depth) {
      return (new LiteralText(String.valueOf(this.value))).formatted(GOLD);
   }

   public long getLong() {
      return (long)this.value;
   }

   public int getInt() {
      return this.value;
   }

   public short getShort() {
      return (short)(this.value & '\uffff');
   }

   public byte getByte() {
      return (byte)(this.value & 255);
   }

   public double getDouble() {
      return (double)this.value;
   }

   public float getFloat() {
      return (float)this.value;
   }

   public Number getNumber() {
      return this.value;
   }

   static class Cache {
      static final IntTag[] VALUES = new IntTag[1153];

      static {
         for(int i = 0; i < VALUES.length; ++i) {
            VALUES[i] = new IntTag(-128 + i);
         }

      }
   }
}
