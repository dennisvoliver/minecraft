package net.minecraft.loot.entry;

import net.minecraft.loot.condition.LootCondition;

public class SequenceEntry extends CombinedEntry {
   SequenceEntry(LootPoolEntry[] lootPoolEntrys, LootCondition[] lootConditions) {
      super(lootPoolEntrys, lootConditions);
   }

   public LootPoolEntryType getType() {
      return LootPoolEntryTypes.GROUP;
   }

   protected EntryCombiner combine(EntryCombiner[] children) {
      switch(children.length) {
      case 0:
         return ALWAYS_TRUE;
      case 1:
         return children[0];
      case 2:
         EntryCombiner entryCombiner = children[0];
         EntryCombiner entryCombiner2 = children[1];
         return (lootContext, consumer) -> {
            entryCombiner.expand(lootContext, consumer);
            entryCombiner2.expand(lootContext, consumer);
            return true;
         };
      default:
         return (context, lootChoiceExpander) -> {
            EntryCombiner[] var3 = children;
            int var4 = children.length;

            for(int var5 = 0; var5 < var4; ++var5) {
               EntryCombiner entryCombiner = var3[var5];
               entryCombiner.expand(context, lootChoiceExpander);
            }

            return true;
         };
      }
   }
}
