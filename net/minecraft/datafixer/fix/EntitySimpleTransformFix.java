package net.minecraft.datafixer.fix;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;

public abstract class EntitySimpleTransformFix extends EntityTransformFix {
   public EntitySimpleTransformFix(String string, Schema schema, boolean bl) {
      super(string, schema, bl);
   }

   protected Pair<String, Typed<?>> transform(String choice, Typed<?> typed) {
      Pair<String, Dynamic<?>> pair = this.transform(choice, (Dynamic)typed.getOrCreate(DSL.remainderFinder()));
      return Pair.of(pair.getFirst(), typed.set(DSL.remainderFinder(), pair.getSecond()));
   }

   protected abstract Pair<String, Dynamic<?>> transform(String choice, Dynamic<?> dynamic);
}
