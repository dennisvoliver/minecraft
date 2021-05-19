package net.minecraft.client.search;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import it.unimi.dsi.fastutil.Arrays;
import it.unimi.dsi.fastutil.Swapper;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntComparator;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Provides an efficient way to search for a text in multiple texts.
 */
@Environment(EnvType.CLIENT)
public class SuffixArray<T> {
   private static final boolean PRINT_COMPARISONS = Boolean.parseBoolean(System.getProperty("SuffixArray.printComparisons", "false"));
   private static final boolean PRINT_ARRAY = Boolean.parseBoolean(System.getProperty("SuffixArray.printArray", "false"));
   private static final Logger LOGGER = LogManager.getLogger();
   protected final List<T> objects = Lists.newArrayList();
   private final IntList characters = new IntArrayList();
   private final IntList textStarts = new IntArrayList();
   private IntList suffixIndexToObjectIndex = new IntArrayList();
   private IntList offsetInText = new IntArrayList();
   private int maxTextLength;

   /**
    * Adds a text with the corresponding object.
    * 
    * <p>You are not allowed to call this method after calling {@link #build()} method.
    * 
    * <p>Takes O({@code text.length()}) time.
    */
   public void add(T object, String text) {
      this.maxTextLength = Math.max(this.maxTextLength, text.length());
      int i = this.objects.size();
      this.objects.add(object);
      this.textStarts.add(this.characters.size());

      for(int j = 0; j < text.length(); ++j) {
         this.suffixIndexToObjectIndex.add(i);
         this.offsetInText.add(j);
         this.characters.add(text.charAt(j));
      }

      this.suffixIndexToObjectIndex.add(i);
      this.offsetInText.add(text.length());
      this.characters.add(-1);
   }

   /**
    * Builds a suffix array with added texts.
    * 
    * <p>You are not allowed to call this method multiple times.
    * 
    * <p>Takes O(N * log N * log M) time on average where N is the sum of all text
    * length added, and M is the maximum text length added.
    */
   public void build() {
      int i = this.characters.size();
      int[] is = new int[i];
      final int[] js = new int[i];
      final int[] ks = new int[i];
      int[] ls = new int[i];
      IntComparator intComparator = new IntComparator() {
         public int compare(int i, int j) {
            return js[i] == js[j] ? Integer.compare(ks[i], ks[j]) : Integer.compare(js[i], js[j]);
         }

         public int compare(Integer integer, Integer integer2) {
            return this.compare(integer, integer2);
         }
      };
      Swapper swapper = (ix, j) -> {
         if (ix != j) {
            int k = js[ix];
            js[ix] = js[j];
            js[j] = k;
            k = ks[ix];
            ks[ix] = ks[j];
            ks[j] = k;
            k = ls[ix];
            ls[ix] = ls[j];
            ls[j] = k;
         }

      };

      int k;
      for(k = 0; k < i; ++k) {
         is[k] = this.characters.getInt(k);
      }

      k = 1;

      for(int l = Math.min(i, this.maxTextLength); k * 2 < l; k *= 2) {
         int n;
         for(n = 0; n < i; ls[n] = n++) {
            js[n] = is[n];
            ks[n] = n + k < i ? is[n + k] : -2;
         }

         Arrays.quickSort(0, i, intComparator, swapper);

         for(n = 0; n < i; ++n) {
            if (n > 0 && js[n] == js[n - 1] && ks[n] == ks[n - 1]) {
               is[ls[n]] = is[ls[n - 1]];
            } else {
               is[ls[n]] = n;
            }
         }
      }

      IntList intList = this.suffixIndexToObjectIndex;
      IntList intList2 = this.offsetInText;
      this.suffixIndexToObjectIndex = new IntArrayList(intList.size());
      this.offsetInText = new IntArrayList(intList2.size());

      for(int o = 0; o < i; ++o) {
         int p = ls[o];
         this.suffixIndexToObjectIndex.add(intList.getInt(p));
         this.offsetInText.add(intList2.getInt(p));
      }

      if (PRINT_ARRAY) {
         this.printArray();
      }

   }

   private void printArray() {
      for(int i = 0; i < this.suffixIndexToObjectIndex.size(); ++i) {
         LOGGER.debug((String)"{} {}", (Object)i, (Object)this.getDebugString(i));
      }

      LOGGER.debug("");
   }

   private String getDebugString(int suffixIndex) {
      int i = this.offsetInText.getInt(suffixIndex);
      int j = this.textStarts.getInt(this.suffixIndexToObjectIndex.getInt(suffixIndex));
      StringBuilder stringBuilder = new StringBuilder();

      for(int k = 0; j + k < this.characters.size(); ++k) {
         if (k == i) {
            stringBuilder.append('^');
         }

         int l = this.characters.get(j + k);
         if (l == -1) {
            break;
         }

         stringBuilder.append((char)l);
      }

      return stringBuilder.toString();
   }

   private int compare(String string, int suffixIndex) {
      int i = this.textStarts.getInt(this.suffixIndexToObjectIndex.getInt(suffixIndex));
      int j = this.offsetInText.getInt(suffixIndex);

      for(int k = 0; k < string.length(); ++k) {
         int l = this.characters.getInt(i + j + k);
         if (l == -1) {
            return 1;
         }

         char c = string.charAt(k);
         char d = (char)l;
         if (c < d) {
            return -1;
         }

         if (c > d) {
            return 1;
         }
      }

      return 0;
   }

   /**
    * Retrieves all objects of which corresponding texts contain {@code text}.
    * 
    * <p>You have to call {@link #build()} method before calling this method.
    * 
    * <p>Takes O({@code text.length()} * log N) time to find objects where N is the
    * sum of all text length added. Takes O(X + Y * log Y) time to collect found
    * objects into a list where X is the number of occurrences of {@code text} in all
    * texts added, and Y is the number of found objects.
    */
   public List<T> findAll(String text) {
      int i = this.suffixIndexToObjectIndex.size();
      int j = 0;
      int k = i;

      int n;
      int o;
      while(j < k) {
         n = j + (k - j) / 2;
         o = this.compare(text, n);
         if (PRINT_COMPARISONS) {
            LOGGER.debug((String)"comparing lower \"{}\" with {} \"{}\": {}", (Object)text, n, this.getDebugString(n), o);
         }

         if (o > 0) {
            j = n + 1;
         } else {
            k = n;
         }
      }

      if (j >= 0 && j < i) {
         n = j;
         k = i;

         while(j < k) {
            o = j + (k - j) / 2;
            int p = this.compare(text, o);
            if (PRINT_COMPARISONS) {
               LOGGER.debug((String)"comparing upper \"{}\" with {} \"{}\": {}", (Object)text, o, this.getDebugString(o), p);
            }

            if (p >= 0) {
               j = o + 1;
            } else {
               k = o;
            }
         }

         o = j;
         IntSet intSet = new IntOpenHashSet();

         for(int r = n; r < o; ++r) {
            intSet.add(this.suffixIndexToObjectIndex.getInt(r));
         }

         int[] is = intSet.toIntArray();
         java.util.Arrays.sort(is);
         Set<T> set = Sets.newLinkedHashSet();
         int[] var10 = is;
         int var11 = is.length;

         for(int var12 = 0; var12 < var11; ++var12) {
            int s = var10[var12];
            set.add(this.objects.get(s));
         }

         return Lists.newArrayList((Iterable)set);
      } else {
         return Collections.emptyList();
      }
   }
}
