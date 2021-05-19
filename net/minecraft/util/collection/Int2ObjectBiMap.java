package net.minecraft.util.collection;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Iterators;
import java.util.Arrays;
import java.util.Iterator;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;

public class Int2ObjectBiMap<K> implements IndexedIterable<K> {
   private static final Object EMPTY = null;
   private K[] values;
   private int[] ids;
   private K[] idToValues;
   private int nextId;
   private int size;

   public Int2ObjectBiMap(int size) {
      size = (int)((float)size / 0.8F);
      this.values = (Object[])(new Object[size]);
      this.ids = new int[size];
      this.idToValues = (Object[])(new Object[size]);
   }

   public int getRawId(@Nullable K entry) {
      return this.getIdFromIndex(this.findIndex(entry, this.getIdealIndex(entry)));
   }

   @Nullable
   public K get(int index) {
      return index >= 0 && index < this.idToValues.length ? this.idToValues[index] : null;
   }

   private int getIdFromIndex(int index) {
      return index == -1 ? -1 : this.ids[index];
   }

   public int add(K value) {
      int i = this.nextId();
      this.put(value, i);
      return i;
   }

   private int nextId() {
      while(this.nextId < this.idToValues.length && this.idToValues[this.nextId] != null) {
         ++this.nextId;
      }

      return this.nextId;
   }

   private void resize(int newSize) {
      K[] objects = this.values;
      int[] is = this.ids;
      this.values = (Object[])(new Object[newSize]);
      this.ids = new int[newSize];
      this.idToValues = (Object[])(new Object[newSize]);
      this.nextId = 0;
      this.size = 0;

      for(int i = 0; i < objects.length; ++i) {
         if (objects[i] != null) {
            this.put(objects[i], is[i]);
         }
      }

   }

   public void put(K value, int id) {
      int i = Math.max(id, this.size + 1);
      int j;
      if ((float)i >= (float)this.values.length * 0.8F) {
         for(j = this.values.length << 1; j < id; j <<= 1) {
         }

         this.resize(j);
      }

      j = this.findFree(this.getIdealIndex(value));
      this.values[j] = value;
      this.ids[j] = id;
      this.idToValues[id] = value;
      ++this.size;
      if (id == this.nextId) {
         ++this.nextId;
      }

   }

   private int getIdealIndex(@Nullable K value) {
      return (MathHelper.idealHash(System.identityHashCode(value)) & Integer.MAX_VALUE) % this.values.length;
   }

   private int findIndex(@Nullable K value, int id) {
      int j;
      for(j = id; j < this.values.length; ++j) {
         if (this.values[j] == value) {
            return j;
         }

         if (this.values[j] == EMPTY) {
            return -1;
         }
      }

      for(j = 0; j < id; ++j) {
         if (this.values[j] == value) {
            return j;
         }

         if (this.values[j] == EMPTY) {
            return -1;
         }
      }

      return -1;
   }

   private int findFree(int size) {
      int j;
      for(j = size; j < this.values.length; ++j) {
         if (this.values[j] == EMPTY) {
            return j;
         }
      }

      for(j = 0; j < size; ++j) {
         if (this.values[j] == EMPTY) {
            return j;
         }
      }

      throw new RuntimeException("Overflowed :(");
   }

   public Iterator<K> iterator() {
      return Iterators.filter(Iterators.forArray(this.idToValues), (Predicate)Predicates.notNull());
   }

   public void clear() {
      Arrays.fill(this.values, (Object)null);
      Arrays.fill(this.idToValues, (Object)null);
      this.nextId = 0;
      this.size = 0;
   }

   public int size() {
      return this.size;
   }
}
