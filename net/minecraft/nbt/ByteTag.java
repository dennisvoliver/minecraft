package net.minecraft.nbt;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;

public class ByteTag extends AbstractNumberTag {
   public static final TagReader<ByteTag> READER = new TagReader<ByteTag>() {
      public ByteTag read(DataInput dataInput, int i, PositionTracker positionTracker) throws IOException {
         positionTracker.add(72L);
         return ByteTag.of(dataInput.readByte());
      }

      public String getCrashReportName() {
         return "BYTE";
      }

      public String getCommandFeedbackName() {
         return "TAG_Byte";
      }

      public boolean isImmutable() {
         return true;
      }
   };
   public static final ByteTag ZERO = of((byte)0);
   public static final ByteTag ONE = of((byte)1);
   private final byte value;

   private ByteTag(byte value) {
      this.value = value;
   }

   public static ByteTag of(byte value) {
      return ByteTag.Cache.VALUES[128 + value];
   }

   public static ByteTag of(boolean value) {
      return value ? ONE : ZERO;
   }

   public void write(DataOutput output) throws IOException {
      output.writeByte(this.value);
   }

   public byte getType() {
      return 1;
   }

   public TagReader<ByteTag> getReader() {
      return READER;
   }

   public String toString() {
      return this.value + "b";
   }

   public ByteTag copy() {
      return this;
   }

   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else {
         return o instanceof ByteTag && this.value == ((ByteTag)o).value;
      }
   }

   public int hashCode() {
      return this.value;
   }

   public Text toText(String indent, int depth) {
      Text text = (new LiteralText("b")).formatted(RED);
      return (new LiteralText(String.valueOf(this.value))).append(text).formatted(GOLD);
   }

   public long getLong() {
      return (long)this.value;
   }

   public int getInt() {
      return this.value;
   }

   public short getShort() {
      return (short)this.value;
   }

   public byte getByte() {
      return this.value;
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
      private static final ByteTag[] VALUES = new ByteTag[256];

      static {
         for(int i = 0; i < VALUES.length; ++i) {
            VALUES[i] = new ByteTag((byte)(i - 128));
         }

      }
   }
}
