package net.minecraft.nbt;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;

public class FloatTag extends AbstractNumberTag {
   public static final FloatTag ZERO = new FloatTag(0.0F);
   public static final TagReader<FloatTag> READER = new TagReader<FloatTag>() {
      public FloatTag read(DataInput dataInput, int i, PositionTracker positionTracker) throws IOException {
         positionTracker.add(96L);
         return FloatTag.of(dataInput.readFloat());
      }

      public String getCrashReportName() {
         return "FLOAT";
      }

      public String getCommandFeedbackName() {
         return "TAG_Float";
      }

      public boolean isImmutable() {
         return true;
      }
   };
   private final float value;

   private FloatTag(float value) {
      this.value = value;
   }

   public static FloatTag of(float value) {
      return value == 0.0F ? ZERO : new FloatTag(value);
   }

   public void write(DataOutput output) throws IOException {
      output.writeFloat(this.value);
   }

   public byte getType() {
      return 5;
   }

   public TagReader<FloatTag> getReader() {
      return READER;
   }

   public String toString() {
      return this.value + "f";
   }

   public FloatTag copy() {
      return this;
   }

   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else {
         return o instanceof FloatTag && this.value == ((FloatTag)o).value;
      }
   }

   public int hashCode() {
      return Float.floatToIntBits(this.value);
   }

   public Text toText(String indent, int depth) {
      Text text = (new LiteralText("f")).formatted(RED);
      return (new LiteralText(String.valueOf(this.value))).append(text).formatted(GOLD);
   }

   public long getLong() {
      return (long)this.value;
   }

   public int getInt() {
      return MathHelper.floor(this.value);
   }

   public short getShort() {
      return (short)(MathHelper.floor(this.value) & '\uffff');
   }

   public byte getByte() {
      return (byte)(MathHelper.floor(this.value) & 255);
   }

   public double getDouble() {
      return (double)this.value;
   }

   public float getFloat() {
      return this.value;
   }

   public Number getNumber() {
      return this.value;
   }
}
