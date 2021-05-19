package net.minecraft.block.pattern;

import com.google.common.base.Joiner;
import com.google.common.base.Predicates;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.lang.reflect.Array;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Predicate;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

public class BlockPatternBuilder {
   private static final Joiner JOINER = Joiner.on(",");
   private final List<String[]> aisles = Lists.newArrayList();
   private final Map<Character, Predicate<CachedBlockPosition>> charMap = Maps.newHashMap();
   private int height;
   private int width;

   private BlockPatternBuilder() {
      this.charMap.put(' ', Predicates.alwaysTrue());
   }

   public BlockPatternBuilder aisle(String... pattern) {
      if (!ArrayUtils.isEmpty((Object[])pattern) && !StringUtils.isEmpty(pattern[0])) {
         if (this.aisles.isEmpty()) {
            this.height = pattern.length;
            this.width = pattern[0].length();
         }

         if (pattern.length != this.height) {
            throw new IllegalArgumentException("Expected aisle with height of " + this.height + ", but was given one with a height of " + pattern.length + ")");
         } else {
            String[] var2 = pattern;
            int var3 = pattern.length;

            for(int var4 = 0; var4 < var3; ++var4) {
               String string = var2[var4];
               if (string.length() != this.width) {
                  throw new IllegalArgumentException("Not all rows in the given aisle are the correct width (expected " + this.width + ", found one with " + string.length() + ")");
               }

               char[] var6 = string.toCharArray();
               int var7 = var6.length;

               for(int var8 = 0; var8 < var7; ++var8) {
                  char c = var6[var8];
                  if (!this.charMap.containsKey(c)) {
                     this.charMap.put(c, (Object)null);
                  }
               }
            }

            this.aisles.add(pattern);
            return this;
         }
      } else {
         throw new IllegalArgumentException("Empty pattern for aisle");
      }
   }

   public static BlockPatternBuilder start() {
      return new BlockPatternBuilder();
   }

   public BlockPatternBuilder where(char key, Predicate<CachedBlockPosition> predicate) {
      this.charMap.put(key, predicate);
      return this;
   }

   public BlockPattern build() {
      return new BlockPattern(this.bakePredicates());
   }

   private Predicate<CachedBlockPosition>[][][] bakePredicates() {
      this.validate();
      Predicate<CachedBlockPosition>[][][] predicates = (Predicate[][][])((Predicate[][][])Array.newInstance(Predicate.class, new int[]{this.aisles.size(), this.height, this.width}));

      for(int i = 0; i < this.aisles.size(); ++i) {
         for(int j = 0; j < this.height; ++j) {
            for(int k = 0; k < this.width; ++k) {
               predicates[i][j][k] = (Predicate)this.charMap.get(((String[])this.aisles.get(i))[j].charAt(k));
            }
         }
      }

      return predicates;
   }

   private void validate() {
      List<Character> list = Lists.newArrayList();
      Iterator var2 = this.charMap.entrySet().iterator();

      while(var2.hasNext()) {
         Entry<Character, Predicate<CachedBlockPosition>> entry = (Entry)var2.next();
         if (entry.getValue() == null) {
            list.add(entry.getKey());
         }
      }

      if (!list.isEmpty()) {
         throw new IllegalStateException("Predicates for character(s) " + JOINER.join((Iterable)list) + " are missing");
      }
   }
}
