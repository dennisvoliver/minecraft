package net.minecraft.datafixer.fix;

import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import net.minecraft.datafixer.TypeReferences;

public class BlockEntityUuidFix extends AbstractUuidFix {
   public BlockEntityUuidFix(Schema outputSchema) {
      super(outputSchema, TypeReferences.BLOCK_ENTITY);
   }

   protected TypeRewriteRule makeRule() {
      return this.fixTypeEverywhereTyped("BlockEntityUUIDFix", this.getInputSchema().getType(this.typeReference), (typed) -> {
         typed = this.updateTyped(typed, "minecraft:conduit", this::updateConduit);
         typed = this.updateTyped(typed, "minecraft:skull", this::updateSkull);
         return typed;
      });
   }

   private Dynamic<?> updateSkull(Dynamic<?> dynamic) {
      return (Dynamic)dynamic.get("Owner").get().map((dynamicx) -> {
         return (Dynamic)updateStringUuid(dynamicx, "Id", "Id").orElse(dynamicx);
      }).map((dynamic2) -> {
         return dynamic.remove("Owner").set("SkullOwner", dynamic2);
      }).result().orElse(dynamic);
   }

   private Dynamic<?> updateConduit(Dynamic<?> dynamic) {
      return (Dynamic)updateCompoundUuid(dynamic, "target_uuid", "Target").orElse(dynamic);
   }
}
