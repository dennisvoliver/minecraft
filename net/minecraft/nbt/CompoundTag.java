package net.minecraft.nbt;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.crash.CrashReportSection;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

public class CompoundTag implements Tag {
   public static final Codec<CompoundTag> CODEC;
   private static final Logger LOGGER;
   private static final Pattern PATTERN;
   public static final TagReader<CompoundTag> READER;
   private final Map<String, Tag> tags;

   protected CompoundTag(Map<String, Tag> tags) {
      this.tags = tags;
   }

   public CompoundTag() {
      this(Maps.newHashMap());
   }

   public void write(DataOutput output) throws IOException {
      Iterator var2 = this.tags.keySet().iterator();

      while(var2.hasNext()) {
         String string = (String)var2.next();
         Tag tag = (Tag)this.tags.get(string);
         write(string, tag, output);
      }

      output.writeByte(0);
   }

   public Set<String> getKeys() {
      return this.tags.keySet();
   }

   public byte getType() {
      return 10;
   }

   public TagReader<CompoundTag> getReader() {
      return READER;
   }

   public int getSize() {
      return this.tags.size();
   }

   @Nullable
   public Tag put(String key, Tag tag) {
      return (Tag)this.tags.put(key, tag);
   }

   public void putByte(String key, byte value) {
      this.tags.put(key, ByteTag.of(value));
   }

   public void putShort(String key, short value) {
      this.tags.put(key, ShortTag.of(value));
   }

   public void putInt(String key, int value) {
      this.tags.put(key, IntTag.of(value));
   }

   public void putLong(String key, long value) {
      this.tags.put(key, LongTag.of(value));
   }

   /**
    * Writes a {@link UUID} to its NBT representation in this {@code CompoundTag}.
    */
   public void putUuid(String key, UUID value) {
      this.tags.put(key, NbtHelper.fromUuid(value));
   }

   /**
    * Reads a {@link UUID} from its NBT representation in this {@code CompoundTag}.
    */
   public UUID getUuid(String key) {
      return NbtHelper.toUuid(this.get(key));
   }

   /**
    * Returns {@code true} if this {@code CompoundTag} contains a valid UUID representation associated with the given key.
    * A valid UUID is represented by an int array of length 4.
    */
   public boolean containsUuid(String key) {
      Tag tag = this.get(key);
      return tag != null && tag.getReader() == IntArrayTag.READER && ((IntArrayTag)tag).getIntArray().length == 4;
   }

   public void putFloat(String key, float value) {
      this.tags.put(key, FloatTag.of(value));
   }

   public void putDouble(String key, double value) {
      this.tags.put(key, DoubleTag.of(value));
   }

   public void putString(String key, String value) {
      this.tags.put(key, StringTag.of(value));
   }

   public void putByteArray(String key, byte[] value) {
      this.tags.put(key, new ByteArrayTag(value));
   }

   public void putIntArray(String key, int[] value) {
      this.tags.put(key, new IntArrayTag(value));
   }

   public void putIntArray(String key, List<Integer> value) {
      this.tags.put(key, new IntArrayTag(value));
   }

   public void putLongArray(String key, long[] value) {
      this.tags.put(key, new LongArrayTag(value));
   }

   public void putLongArray(String key, List<Long> value) {
      this.tags.put(key, new LongArrayTag(value));
   }

   public void putBoolean(String key, boolean value) {
      this.tags.put(key, ByteTag.of(value));
   }

   @Nullable
   public Tag get(String key) {
      return (Tag)this.tags.get(key);
   }

   public byte getType(String key) {
      Tag tag = (Tag)this.tags.get(key);
      return tag == null ? 0 : tag.getType();
   }

   public boolean contains(String key) {
      return this.tags.containsKey(key);
   }

   public boolean contains(String key, int type) {
      int i = this.getType(key);
      if (i == type) {
         return true;
      } else if (type != 99) {
         return false;
      } else {
         return i == 1 || i == 2 || i == 3 || i == 4 || i == 5 || i == 6;
      }
   }

   public byte getByte(String key) {
      try {
         if (this.contains(key, 99)) {
            return ((AbstractNumberTag)this.tags.get(key)).getByte();
         }
      } catch (ClassCastException var3) {
      }

      return 0;
   }

   public short getShort(String key) {
      try {
         if (this.contains(key, 99)) {
            return ((AbstractNumberTag)this.tags.get(key)).getShort();
         }
      } catch (ClassCastException var3) {
      }

      return 0;
   }

   public int getInt(String key) {
      try {
         if (this.contains(key, 99)) {
            return ((AbstractNumberTag)this.tags.get(key)).getInt();
         }
      } catch (ClassCastException var3) {
      }

      return 0;
   }

   public long getLong(String key) {
      try {
         if (this.contains(key, 99)) {
            return ((AbstractNumberTag)this.tags.get(key)).getLong();
         }
      } catch (ClassCastException var3) {
      }

      return 0L;
   }

   public float getFloat(String key) {
      try {
         if (this.contains(key, 99)) {
            return ((AbstractNumberTag)this.tags.get(key)).getFloat();
         }
      } catch (ClassCastException var3) {
      }

      return 0.0F;
   }

   public double getDouble(String key) {
      try {
         if (this.contains(key, 99)) {
            return ((AbstractNumberTag)this.tags.get(key)).getDouble();
         }
      } catch (ClassCastException var3) {
      }

      return 0.0D;
   }

   public String getString(String key) {
      try {
         if (this.contains(key, 8)) {
            return ((Tag)this.tags.get(key)).asString();
         }
      } catch (ClassCastException var3) {
      }

      return "";
   }

   public byte[] getByteArray(String key) {
      try {
         if (this.contains(key, 7)) {
            return ((ByteArrayTag)this.tags.get(key)).getByteArray();
         }
      } catch (ClassCastException var3) {
         throw new CrashException(this.createCrashReport(key, ByteArrayTag.READER, var3));
      }

      return new byte[0];
   }

   public int[] getIntArray(String key) {
      try {
         if (this.contains(key, 11)) {
            return ((IntArrayTag)this.tags.get(key)).getIntArray();
         }
      } catch (ClassCastException var3) {
         throw new CrashException(this.createCrashReport(key, IntArrayTag.READER, var3));
      }

      return new int[0];
   }

   public long[] getLongArray(String key) {
      try {
         if (this.contains(key, 12)) {
            return ((LongArrayTag)this.tags.get(key)).getLongArray();
         }
      } catch (ClassCastException var3) {
         throw new CrashException(this.createCrashReport(key, LongArrayTag.READER, var3));
      }

      return new long[0];
   }

   public CompoundTag getCompound(String key) {
      try {
         if (this.contains(key, 10)) {
            return (CompoundTag)this.tags.get(key);
         }
      } catch (ClassCastException var3) {
         throw new CrashException(this.createCrashReport(key, READER, var3));
      }

      return new CompoundTag();
   }

   public ListTag getList(String key, int type) {
      try {
         if (this.getType(key) == 9) {
            ListTag listTag = (ListTag)this.tags.get(key);
            if (!listTag.isEmpty() && listTag.getElementType() != type) {
               return new ListTag();
            }

            return listTag;
         }
      } catch (ClassCastException var4) {
         throw new CrashException(this.createCrashReport(key, ListTag.READER, var4));
      }

      return new ListTag();
   }

   public boolean getBoolean(String key) {
      return this.getByte(key) != 0;
   }

   public void remove(String key) {
      this.tags.remove(key);
   }

   public String toString() {
      StringBuilder stringBuilder = new StringBuilder("{");
      Collection<String> collection = this.tags.keySet();
      if (LOGGER.isDebugEnabled()) {
         List<String> list = Lists.newArrayList((Iterable)this.tags.keySet());
         Collections.sort(list);
         collection = list;
      }

      String string;
      for(Iterator var5 = ((Collection)collection).iterator(); var5.hasNext(); stringBuilder.append(escapeTagKey(string)).append(':').append(this.tags.get(string))) {
         string = (String)var5.next();
         if (stringBuilder.length() != 1) {
            stringBuilder.append(',');
         }
      }

      return stringBuilder.append('}').toString();
   }

   public boolean isEmpty() {
      return this.tags.isEmpty();
   }

   private CrashReport createCrashReport(String key, TagReader<?> tagReader, ClassCastException classCastException) {
      CrashReport crashReport = CrashReport.create(classCastException, "Reading NBT data");
      CrashReportSection crashReportSection = crashReport.addElement("Corrupt NBT tag", 1);
      crashReportSection.add("Tag type found", () -> {
         return ((Tag)this.tags.get(key)).getReader().getCrashReportName();
      });
      crashReportSection.add("Tag type expected", tagReader::getCrashReportName);
      crashReportSection.add("Tag name", (Object)key);
      return crashReport;
   }

   public CompoundTag copy() {
      Map<String, Tag> map = Maps.newHashMap(Maps.transformValues(this.tags, Tag::copy));
      return new CompoundTag(map);
   }

   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else {
         return o instanceof CompoundTag && Objects.equals(this.tags, ((CompoundTag)o).tags);
      }
   }

   public int hashCode() {
      return this.tags.hashCode();
   }

   private static void write(String key, Tag tag, DataOutput output) throws IOException {
      output.writeByte(tag.getType());
      if (tag.getType() != 0) {
         output.writeUTF(key);
         tag.write(output);
      }
   }

   private static byte readByte(DataInput input, PositionTracker tracker) throws IOException {
      return input.readByte();
   }

   private static String readString(DataInput input, PositionTracker tracker) throws IOException {
      return input.readUTF();
   }

   private static Tag read(TagReader<?> reader, String key, DataInput input, int depth, PositionTracker tracker) {
      try {
         return reader.read(input, depth, tracker);
      } catch (IOException var8) {
         CrashReport crashReport = CrashReport.create(var8, "Loading NBT data");
         CrashReportSection crashReportSection = crashReport.addElement("NBT Tag");
         crashReportSection.add("Tag name", (Object)key);
         crashReportSection.add("Tag type", (Object)reader.getCrashReportName());
         throw new CrashException(crashReport);
      }
   }

   public CompoundTag copyFrom(CompoundTag source) {
      Iterator var2 = source.tags.keySet().iterator();

      while(var2.hasNext()) {
         String string = (String)var2.next();
         Tag tag = (Tag)source.tags.get(string);
         if (tag.getType() == 10) {
            if (this.contains(string, 10)) {
               CompoundTag compoundTag = this.getCompound(string);
               compoundTag.copyFrom((CompoundTag)tag);
            } else {
               this.put(string, tag.copy());
            }
         } else {
            this.put(string, tag.copy());
         }
      }

      return this;
   }

   protected static String escapeTagKey(String key) {
      return PATTERN.matcher(key).matches() ? key : StringTag.escape(key);
   }

   protected static Text prettyPrintTagKey(String key) {
      if (PATTERN.matcher(key).matches()) {
         return (new LiteralText(key)).formatted(AQUA);
      } else {
         String string = StringTag.escape(key);
         String string2 = string.substring(0, 1);
         Text text = (new LiteralText(string.substring(1, string.length() - 1))).formatted(AQUA);
         return (new LiteralText(string2)).append(text).append(string2);
      }
   }

   public Text toText(String indent, int depth) {
      if (this.tags.isEmpty()) {
         return new LiteralText("{}");
      } else {
         MutableText mutableText = new LiteralText("{");
         Collection<String> collection = this.tags.keySet();
         if (LOGGER.isDebugEnabled()) {
            List<String> list = Lists.newArrayList((Iterable)this.tags.keySet());
            Collections.sort(list);
            collection = list;
         }

         if (!indent.isEmpty()) {
            mutableText.append("\n");
         }

         MutableText mutableText2;
         for(Iterator iterator = ((Collection)collection).iterator(); iterator.hasNext(); mutableText.append((Text)mutableText2)) {
            String string = (String)iterator.next();
            mutableText2 = (new LiteralText(Strings.repeat(indent, depth + 1))).append(prettyPrintTagKey(string)).append(String.valueOf(':')).append(" ").append(((Tag)this.tags.get(string)).toText(indent, depth + 1));
            if (iterator.hasNext()) {
               mutableText2.append(String.valueOf(',')).append(indent.isEmpty() ? " " : "\n");
            }
         }

         if (!indent.isEmpty()) {
            mutableText.append("\n").append(Strings.repeat(indent, depth));
         }

         mutableText.append("}");
         return mutableText;
      }
   }

   protected Map<String, Tag> toMap() {
      return Collections.unmodifiableMap(this.tags);
   }

   static {
      CODEC = Codec.PASSTHROUGH.comapFlatMap((dynamic) -> {
         Tag tag = (Tag)dynamic.convert(NbtOps.INSTANCE).getValue();
         return tag instanceof CompoundTag ? DataResult.success((CompoundTag)tag) : DataResult.error("Not a compound tag: " + tag);
      }, (compoundTag) -> {
         return new Dynamic(NbtOps.INSTANCE, compoundTag);
      });
      LOGGER = LogManager.getLogger();
      PATTERN = Pattern.compile("[A-Za-z0-9._+-]+");
      READER = new TagReader<CompoundTag>() {
         public CompoundTag read(DataInput dataInput, int i, PositionTracker positionTracker) throws IOException {
            positionTracker.add(384L);
            if (i > 512) {
               throw new RuntimeException("Tried to read NBT tag with too high complexity, depth > 512");
            } else {
               HashMap map = Maps.newHashMap();

               byte b;
               while((b = CompoundTag.readByte(dataInput, positionTracker)) != 0) {
                  String string = CompoundTag.readString(dataInput, positionTracker);
                  positionTracker.add((long)(224 + 16 * string.length()));
                  Tag tag = CompoundTag.read(TagReaders.of(b), string, dataInput, i + 1, positionTracker);
                  if (map.put(string, tag) != null) {
                     positionTracker.add(288L);
                  }
               }

               return new CompoundTag(map);
            }
         }

         public String getCrashReportName() {
            return "COMPOUND";
         }

         public String getCommandFeedbackName() {
            return "TAG_Compound";
         }
      };
   }
}
