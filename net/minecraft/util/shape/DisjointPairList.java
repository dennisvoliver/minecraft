package net.minecraft.util.shape;

import it.unimi.dsi.fastutil.doubles.AbstractDoubleList;
import it.unimi.dsi.fastutil.doubles.DoubleList;

public class DisjointPairList extends AbstractDoubleList implements PairList {
   private final DoubleList first;
   private final DoubleList second;
   private final boolean inverted;

   public DisjointPairList(DoubleList first, DoubleList second, boolean inverted) {
      this.first = first;
      this.second = second;
      this.inverted = inverted;
   }

   public int size() {
      return this.first.size() + this.second.size();
   }

   public boolean forEachPair(PairList.Consumer predicate) {
      return this.inverted ? this.iterateSections((i, j, k) -> {
         return predicate.merge(j, i, k);
      }) : this.iterateSections(predicate);
   }

   private boolean iterateSections(PairList.Consumer consumer) {
      int i = this.first.size() - 1;

      int k;
      for(k = 0; k < i; ++k) {
         if (!consumer.merge(k, -1, k)) {
            return false;
         }
      }

      if (!consumer.merge(i, -1, i)) {
         return false;
      } else {
         for(k = 0; k < this.second.size(); ++k) {
            if (!consumer.merge(i, k, i + 1 + k)) {
               return false;
            }
         }

         return true;
      }
   }

   public double getDouble(int position) {
      return position < this.first.size() ? this.first.getDouble(position) : this.second.getDouble(position - this.first.size());
   }

   public DoubleList getPairs() {
      return this;
   }
}
