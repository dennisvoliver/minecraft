package net.minecraft.datafixer.fix;

import com.google.gson.JsonParseException;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import net.minecraft.datafixer.TypeReferences;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.JsonHelper;
import org.apache.commons.lang3.StringUtils;

public class ItemWrittenBookPagesStrictJsonFix extends DataFix {
   public ItemWrittenBookPagesStrictJsonFix(Schema outputSchema, boolean changesType) {
      super(outputSchema, changesType);
   }

   public Dynamic<?> fixBookPages(Dynamic<?> dynamic) {
      return dynamic.update("pages", (dynamic2) -> {
         DataResult var10000 = dynamic2.asStreamOpt().map((stream) -> {
            return stream.map((dynamic) -> {
               if (!dynamic.asString().result().isPresent()) {
                  return dynamic;
               } else {
                  String string = dynamic.asString("");
                  Text text = null;
                  if (!"null".equals(string) && !StringUtils.isEmpty(string)) {
                     if (string.charAt(0) == '"' && string.charAt(string.length() - 1) == '"' || string.charAt(0) == '{' && string.charAt(string.length() - 1) == '}') {
                        try {
                           text = (Text)JsonHelper.deserialize(BlockEntitySignTextStrictJsonFix.GSON, string, Text.class, true);
                           if (text == null) {
                              text = LiteralText.EMPTY;
                           }
                        } catch (JsonParseException var6) {
                        }

                        if (text == null) {
                           try {
                              text = Text.Serializer.fromJson(string);
                           } catch (JsonParseException var5) {
                           }
                        }

                        if (text == null) {
                           try {
                              text = Text.Serializer.fromLenientJson(string);
                           } catch (JsonParseException var4) {
                           }
                        }

                        if (text == null) {
                           text = new LiteralText(string);
                        }
                     } else {
                        text = new LiteralText(string);
                     }
                  } else {
                     text = LiteralText.EMPTY;
                  }

                  return dynamic.createString(Text.Serializer.toJson((Text)text));
               }
            });
         });
         dynamic.getClass();
         return (Dynamic)DataFixUtils.orElse(var10000.map(dynamic::createList).result(), dynamic.emptyList());
      });
   }

   public TypeRewriteRule makeRule() {
      Type<?> type = this.getInputSchema().getType(TypeReferences.ITEM_STACK);
      OpticFinder<?> opticFinder = type.findField("tag");
      return this.fixTypeEverywhereTyped("ItemWrittenBookPagesStrictJsonFix", type, (typed) -> {
         return typed.updateTyped(opticFinder, (typedx) -> {
            return typedx.update(DSL.remainderFinder(), this::fixBookPages);
         });
      });
   }
}
