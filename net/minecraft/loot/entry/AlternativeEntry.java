package net.minecraft.loot.entry;

import com.google.common.collect.Lists;
import java.util.List;
import net.minecraft.loot.LootTableReporter;
import net.minecraft.loot.condition.LootCondition;
import org.apache.commons.lang3.ArrayUtils;

public class AlternativeEntry extends CombinedEntry {
   AlternativeEntry(LootPoolEntry[] lootPoolEntrys, LootCondition[] lootConditions) {
      super(lootPoolEntrys, lootConditions);
   }

   public LootPoolEntryType getType() {
      return LootPoolEntryTypes.ALTERNATIVES;
   }

   protected EntryCombiner combine(EntryCombiner[] children) {
      switch(children.length) {
      case 0:
         return ALWAYS_FALSE;
      case 1:
         return children[0];
      case 2:
         return children[0].or(children[1]);
      default:
         return (context, lootChoiceExpander) -> {
            EntryCombiner[] var3 = children;
            int var4 = children.length;

            for(int var5 = 0; var5 < var4; ++var5) {
               EntryCombiner entryCombiner = var3[var5];
               if (entryCombiner.expand(context, lootChoiceExpander)) {
                  return true;
               }
            }

            return false;
         };
      }
   }

   public void validate(LootTableReporter reporter) {
      super.validate(reporter);

      for(int i = 0; i < this.children.length - 1; ++i) {
         if (ArrayUtils.isEmpty((Object[])this.children[i].conditions)) {
            reporter.report("Unreachable entry!");
         }
      }

   }

   public static AlternativeEntry.Builder builder(LootPoolEntry.Builder<?>... children) {
      return new AlternativeEntry.Builder(children);
   }

   public static class Builder extends LootPoolEntry.Builder<AlternativeEntry.Builder> {
      private final List<LootPoolEntry> children = Lists.newArrayList();

      public Builder(LootPoolEntry.Builder<?>... children) {
         LootPoolEntry.Builder[] var2 = children;
         int var3 = children.length;

         for(int var4 = 0; var4 < var3; ++var4) {
            LootPoolEntry.Builder<?> builder = var2[var4];
            this.children.add(builder.build());
         }

      }

      protected AlternativeEntry.Builder getThisBuilder() {
         return this;
      }

      public AlternativeEntry.Builder alternatively(LootPoolEntry.Builder<?> builder) {
         this.children.add(builder.build());
         return this;
      }

      public LootPoolEntry build() {
         return new AlternativeEntry((LootPoolEntry[])this.children.toArray(new LootPoolEntry[0]), this.getConditions());
      }
   }
}
