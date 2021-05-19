package net.minecraft.datafixer.fix;

import com.google.common.collect.Lists;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.types.templates.TaggedChoice.TaggedChoiceType;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;
import java.util.List;
import java.util.Objects;
import net.minecraft.datafixer.TypeReferences;

public class EntityMinecartIdentifiersFix extends DataFix {
   private static final List<String> MINECARTS = Lists.newArrayList((Object[])("MinecartRideable", "MinecartChest", "MinecartFurnace"));

   public EntityMinecartIdentifiersFix(Schema outputSchema, boolean changesType) {
      super(outputSchema, changesType);
   }

   public TypeRewriteRule makeRule() {
      TaggedChoiceType<String> taggedChoiceType = this.getInputSchema().findChoiceType(TypeReferences.ENTITY);
      TaggedChoiceType<String> taggedChoiceType2 = this.getOutputSchema().findChoiceType(TypeReferences.ENTITY);
      return this.fixTypeEverywhere("EntityMinecartIdentifiersFix", taggedChoiceType, taggedChoiceType2, (dynamicOps) -> {
         return (pair) -> {
            if (!Objects.equals(pair.getFirst(), "Minecart")) {
               return pair;
            } else {
               Typed<? extends Pair<String, ?>> typed = (Typed)taggedChoiceType.point(dynamicOps, "Minecart", pair.getSecond()).orElseThrow(IllegalStateException::new);
               Dynamic<?> dynamic = (Dynamic)typed.getOrCreate(DSL.remainderFinder());
               int i = dynamic.get("Type").asInt(0);
               String string2;
               if (i > 0 && i < MINECARTS.size()) {
                  string2 = (String)MINECARTS.get(i);
               } else {
                  string2 = "MinecartRideable";
               }

               return Pair.of(string2, typed.write().map((dynamicx) -> {
                  return ((Type)taggedChoiceType2.types().get(string2)).read(dynamicx);
               }).result().orElseThrow(() -> {
                  return new IllegalStateException("Could not read the new minecart.");
               }));
            }
         };
      });
   }
}
