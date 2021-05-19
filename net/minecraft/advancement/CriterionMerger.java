package net.minecraft.advancement;

import java.util.Collection;
import java.util.Iterator;

public interface CriterionMerger {
   CriterionMerger AND = (collection) -> {
      String[][] strings = new String[collection.size()][];
      int i = 0;

      String string;
      for(Iterator var3 = collection.iterator(); var3.hasNext(); strings[i++] = new String[]{string}) {
         string = (String)var3.next();
      }

      return strings;
   };
   CriterionMerger OR = (collection) -> {
      return new String[][]{(String[])collection.toArray(new String[0])};
   };

   String[][] createRequirements(Collection<String> criteriaNames);
}
