package net.minecraft.datafixer.fix;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import java.util.Objects;
import net.minecraft.datafixer.TypeReferences;

public class EntityHorseSplitFix extends EntityTransformFix {
   public EntityHorseSplitFix(Schema outputSchema, boolean changesType) {
      super("EntityHorseSplitFix", outputSchema, changesType);
   }

   protected Pair<String, Typed<?>> transform(String choice, Typed<?> typed) {
      Dynamic<?> dynamic = (Dynamic)typed.get(DSL.remainderFinder());
      if (Objects.equals("EntityHorse", choice)) {
         int i = dynamic.get("Type").asInt(0);
         String string5;
         switch(i) {
         case 0:
         default:
            string5 = "Horse";
            break;
         case 1:
            string5 = "Donkey";
            break;
         case 2:
            string5 = "Mule";
            break;
         case 3:
            string5 = "ZombieHorse";
            break;
         case 4:
            string5 = "SkeletonHorse";
         }

         dynamic.remove("Type");
         Type<?> type = (Type)this.getOutputSchema().findChoiceType(TypeReferences.ENTITY).types().get(string5);
         DataResult var10001 = typed.write();
         type.getClass();
         return Pair.of(string5, ((Pair)var10001.flatMap(type::readTyped).result().orElseThrow(() -> {
            return new IllegalStateException("Could not parse the new horse");
         })).getFirst());
      } else {
         return Pair.of(choice, typed);
      }
   }
}
