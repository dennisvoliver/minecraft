package net.minecraft.loot.entry;

import net.minecraft.loot.condition.LootCondition;

public class GroupEntry extends CombinedEntry {
   GroupEntry(LootPoolEntry[] lootPoolEntrys, LootCondition[] lootConditions) {
      super(lootPoolEntrys, lootConditions);
   }

   public LootPoolEntryType getType() {
      return LootPoolEntryTypes.SEQUENCE;
   }

   protected EntryCombiner combine(EntryCombiner[] children) {
      switch(children.length) {
      case 0:
         return ALWAYS_TRUE;
      case 1:
         return children[0];
      case 2:
         return children[0].and(children[1]);
      default:
         return (context, lootChoiceExpander) -> {
            EntryCombiner[] var3 = children;
            int var4 = children.length;

            for(int var5 = 0; var5 < var4; ++var5) {
               EntryCombiner entryCombiner = var3[var5];
               if (!entryCombiner.expand(context, lootChoiceExpander)) {
                  return false;
               }
            }

            return true;
         };
      }
   }
}
