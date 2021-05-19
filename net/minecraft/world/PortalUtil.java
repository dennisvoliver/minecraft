package net.minecraft.world;

import com.google.common.annotations.VisibleForTesting;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntStack;
import java.util.function.Predicate;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public class PortalUtil {
   /**
    * Gets the largest rectangle of blocks along two axes for which all blocks meet a predicate.
    * Used for getting rectangles of Nether portal blocks.
    */
   public static PortalUtil.Rectangle getLargestRectangle(BlockPos center, Direction.Axis primaryAxis, int primaryMaxBlocks, Direction.Axis secondaryAxis, int secondaryMaxBlocks, Predicate<BlockPos> predicate) {
      BlockPos.Mutable mutable = center.mutableCopy();
      Direction direction = Direction.get(Direction.AxisDirection.NEGATIVE, primaryAxis);
      Direction direction2 = direction.getOpposite();
      Direction direction3 = Direction.get(Direction.AxisDirection.NEGATIVE, secondaryAxis);
      Direction direction4 = direction3.getOpposite();
      int i = moveWhile(predicate, mutable.set(center), direction, primaryMaxBlocks);
      int j = moveWhile(predicate, mutable.set(center), direction2, primaryMaxBlocks);
      int k = i;
      PortalUtil.IntBounds[] intBoundss = new PortalUtil.IntBounds[i + 1 + j];
      intBoundss[i] = new PortalUtil.IntBounds(moveWhile(predicate, mutable.set(center), direction3, secondaryMaxBlocks), moveWhile(predicate, mutable.set(center), direction4, secondaryMaxBlocks));
      int l = intBoundss[i].min;

      int o;
      PortalUtil.IntBounds intBounds2;
      for(o = 1; o <= i; ++o) {
         intBounds2 = intBoundss[k - (o - 1)];
         intBoundss[k - o] = new PortalUtil.IntBounds(moveWhile(predicate, mutable.set(center).move(direction, o), direction3, intBounds2.min), moveWhile(predicate, mutable.set(center).move(direction, o), direction4, intBounds2.max));
      }

      for(o = 1; o <= j; ++o) {
         intBounds2 = intBoundss[k + o - 1];
         intBoundss[k + o] = new PortalUtil.IntBounds(moveWhile(predicate, mutable.set(center).move(direction2, o), direction3, intBounds2.min), moveWhile(predicate, mutable.set(center).move(direction2, o), direction4, intBounds2.max));
      }

      o = 0;
      int p = 0;
      int q = 0;
      int r = 0;
      int[] is = new int[intBoundss.length];

      for(int s = l; s >= 0; --s) {
         PortalUtil.IntBounds intBounds4;
         int w;
         int x;
         for(int t = 0; t < intBoundss.length; ++t) {
            intBounds4 = intBoundss[t];
            w = l - intBounds4.min;
            x = l + intBounds4.max;
            is[t] = s >= w && s <= x ? x + 1 - s : 0;
         }

         Pair<PortalUtil.IntBounds, Integer> pair = findLargestRectangle(is);
         intBounds4 = (PortalUtil.IntBounds)pair.getFirst();
         w = 1 + intBounds4.max - intBounds4.min;
         x = (Integer)pair.getSecond();
         if (w * x > q * r) {
            o = intBounds4.min;
            p = s;
            q = w;
            r = x;
         }
      }

      return new PortalUtil.Rectangle(center.offset(primaryAxis, o - k).offset(secondaryAxis, p - l), q, r);
   }

   private static int moveWhile(Predicate<BlockPos> predicate, BlockPos.Mutable mutable, Direction direction, int max) {
      int i;
      for(i = 0; i < max && predicate.test(mutable.move(direction)); ++i) {
      }

      return i;
   }

   /**
    * Finds the largest rectangle within a histogram, where the vertical bars each have
    * width 1 and height specified in {@code heights}.
    * 
    * @implNote This implementation solves the problem using a stack. The
    * stack maintains a collection of height limits of rectangles that may grow as the
    * array iteration continues. When a new height is encountered, each position {@code p}
    * in the stack would be popped if the rectangle with height limit at position {@code
    * p} can no longer extend right. The popped rectangle becomes the return value if it
    * has a larger area than the current candidate.
    * 
    * <p>When the rectangle area is calculated, the range is between {@code p0 + 1}, where
    * {@code p0} is the current top of stack after popping rectangles that can no longer
    * extend, and the current iterated position {@code i}.
    * 
    * @return the base of the rectangle as an inclusive range and the height of the
    * rectangle packed in a pair
    * @see <a href="https://leetcode.com/problems/largest-rectangle-in-histogram">Largest
    * Rectangle in Histogram - LeetCode</a>
    * 
    * @param heights the heights of bars in the histogram
    */
   @VisibleForTesting
   static Pair<PortalUtil.IntBounds, Integer> findLargestRectangle(int[] heights) {
      int i = 0;
      int j = 0;
      int k = 0;
      IntStack intStack = new IntArrayList();
      intStack.push(0);

      for(int l = 1; l <= heights.length; ++l) {
         int m = l == heights.length ? 0 : heights[l];

         while(!intStack.isEmpty()) {
            int n = heights[intStack.topInt()];
            if (m >= n) {
               intStack.push(l);
               break;
            }

            intStack.popInt();
            int o = intStack.isEmpty() ? 0 : intStack.topInt() + 1;
            if (n * (l - o) > k * (j - i)) {
               j = l;
               i = o;
               k = n;
            }
         }

         if (intStack.isEmpty()) {
            intStack.push(l);
         }
      }

      return new Pair(new PortalUtil.IntBounds(i, j - 1), k);
   }

   public static class Rectangle {
      public final BlockPos lowerLeft;
      public final int width;
      public final int height;

      public Rectangle(BlockPos lowerLeft, int width, int height) {
         this.lowerLeft = lowerLeft;
         this.width = width;
         this.height = height;
      }
   }

   public static class IntBounds {
      public final int min;
      public final int max;

      public IntBounds(int min, int max) {
         this.min = min;
         this.max = max;
      }

      public String toString() {
         return "IntBounds{min=" + this.min + ", max=" + this.max + '}';
      }
   }
}
