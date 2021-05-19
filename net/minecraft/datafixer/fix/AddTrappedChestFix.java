package net.minecraft.datafixer.fix;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.types.templates.List.ListType;
import com.mojang.datafixers.types.templates.TaggedChoice.TaggedChoiceType;
import com.mojang.serialization.Dynamic;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import net.minecraft.datafixer.TypeReferences;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

public class AddTrappedChestFix extends DataFix {
   private static final Logger LOGGER = LogManager.getLogger();

   public AddTrappedChestFix(Schema outputSchema, boolean changesType) {
      super(outputSchema, changesType);
   }

   public TypeRewriteRule makeRule() {
      Type<?> type = this.getOutputSchema().getType(TypeReferences.CHUNK);
      Type<?> type2 = type.findFieldType("Level");
      Type<?> type3 = type2.findFieldType("TileEntities");
      if (!(type3 instanceof ListType)) {
         throw new IllegalStateException("Tile entity type is not a list type.");
      } else {
         ListType<?> listType = (ListType)type3;
         OpticFinder<? extends List<?>> opticFinder = DSL.fieldFinder("TileEntities", listType);
         Type<?> type4 = this.getInputSchema().getType(TypeReferences.CHUNK);
         OpticFinder<?> opticFinder2 = type4.findField("Level");
         OpticFinder<?> opticFinder3 = opticFinder2.type().findField("Sections");
         Type<?> type5 = opticFinder3.type();
         if (!(type5 instanceof ListType)) {
            throw new IllegalStateException("Expecting sections to be a list.");
         } else {
            Type<?> type6 = ((ListType)type5).getElement();
            OpticFinder<?> opticFinder4 = DSL.typeFinder(type6);
            return TypeRewriteRule.seq((new ChoiceTypesFix(this.getOutputSchema(), "AddTrappedChestFix", TypeReferences.BLOCK_ENTITY)).makeRule(), this.fixTypeEverywhereTyped("Trapped Chest fix", type4, (typed) -> {
               return typed.updateTyped(opticFinder2, (typedx) -> {
                  Optional<? extends Typed<?>> optional = typedx.getOptionalTyped(opticFinder3);
                  if (!optional.isPresent()) {
                     return typedx;
                  } else {
                     List<? extends Typed<?>> list = ((Typed)optional.get()).getAllTyped(opticFinder4);
                     IntSet intSet = new IntOpenHashSet();
                     Iterator var8 = list.iterator();

                     while(true) {
                        AddTrappedChestFix.ListFixer listFixer;
                        do {
                           if (!var8.hasNext()) {
                              Dynamic<?> dynamic = (Dynamic)typedx.get(DSL.remainderFinder());
                              int k = dynamic.get("xPos").asInt(0);
                              int l = dynamic.get("zPos").asInt(0);
                              TaggedChoiceType<String> taggedChoiceType = this.getInputSchema().findChoiceType(TypeReferences.BLOCK_ENTITY);
                              return typedx.updateTyped(opticFinder, (typed) -> {
                                 return typed.updateTyped(taggedChoiceType.finder(), (typedx) -> {
                                    Dynamic<?> dynamic = (Dynamic)typedx.getOrCreate(DSL.remainderFinder());
                                    int kx = dynamic.get("x").asInt(0) - (k << 4);
                                    int lx = dynamic.get("y").asInt(0);
                                    int m = dynamic.get("z").asInt(0) - (l << 4);
                                    return intSet.contains(LeavesFix.method_5051(kx, lx, m)) ? typedx.update(taggedChoiceType.finder(), (pair) -> {
                                       return pair.mapFirst((string) -> {
                                          if (!Objects.equals(string, "minecraft:chest")) {
                                             LOGGER.warn("Block Entity was expected to be a chest");
                                          }

                                          return "minecraft:trapped_chest";
                                       });
                                    }) : typedx;
                                 });
                              });
                           }

                           Typed<?> typed2 = (Typed)var8.next();
                           listFixer = new AddTrappedChestFix.ListFixer(typed2, this.getInputSchema());
                        } while(listFixer.isFixed());

                        for(int i = 0; i < 4096; ++i) {
                           int j = listFixer.needsFix(i);
                           if (listFixer.isTarget(j)) {
                              intSet.add(listFixer.method_5077() << 12 | i);
                           }
                        }
                     }
                  }
               });
            }));
         }
      }
   }

   public static final class ListFixer extends LeavesFix.ListFixer {
      @Nullable
      private IntSet targets;

      public ListFixer(Typed<?> typed, Schema schema) {
         super(typed, schema);
      }

      protected boolean needsFix() {
         this.targets = new IntOpenHashSet();

         for(int i = 0; i < this.properties.size(); ++i) {
            Dynamic<?> dynamic = (Dynamic)this.properties.get(i);
            String string = dynamic.get("Name").asString("");
            if (Objects.equals(string, "minecraft:trapped_chest")) {
               this.targets.add(i);
            }
         }

         return this.targets.isEmpty();
      }

      public boolean isTarget(int index) {
         return this.targets.contains(index);
      }
   }
}
