package net.minecraft.loot.function;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSyntaxException;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.UniformLootTableRange;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.context.LootContext;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.Util;
import net.minecraft.util.registry.Registry;
import org.jetbrains.annotations.Nullable;

public class SetAttributesLootFunction extends ConditionalLootFunction {
   private final List<SetAttributesLootFunction.Attribute> attributes;

   private SetAttributesLootFunction(LootCondition[] conditions, List<SetAttributesLootFunction.Attribute> attributes) {
      super(conditions);
      this.attributes = ImmutableList.copyOf((Collection)attributes);
   }

   public LootFunctionType getType() {
      return LootFunctionTypes.SET_ATTRIBUTES;
   }

   public ItemStack process(ItemStack stack, LootContext context) {
      Random random = context.getRandom();
      Iterator var4 = this.attributes.iterator();

      while(var4.hasNext()) {
         SetAttributesLootFunction.Attribute attribute = (SetAttributesLootFunction.Attribute)var4.next();
         UUID uUID = attribute.id;
         if (uUID == null) {
            uUID = UUID.randomUUID();
         }

         EquipmentSlot equipmentSlot = (EquipmentSlot)Util.getRandom((Object[])attribute.slots, random);
         stack.addAttributeModifier(attribute.attribute, new EntityAttributeModifier(uUID, attribute.name, (double)attribute.amountRange.nextFloat(random), attribute.operation), equipmentSlot);
      }

      return stack;
   }

   static class Attribute {
      private final String name;
      private final EntityAttribute attribute;
      private final EntityAttributeModifier.Operation operation;
      private final UniformLootTableRange amountRange;
      @Nullable
      private final UUID id;
      private final EquipmentSlot[] slots;

      private Attribute(String name, EntityAttribute entityAttribute, EntityAttributeModifier.Operation operation, UniformLootTableRange amountRange, EquipmentSlot[] slots, @Nullable UUID id) {
         this.name = name;
         this.attribute = entityAttribute;
         this.operation = operation;
         this.amountRange = amountRange;
         this.id = id;
         this.slots = slots;
      }

      public JsonObject serialize(JsonSerializationContext context) {
         JsonObject jsonObject = new JsonObject();
         jsonObject.addProperty("name", this.name);
         jsonObject.addProperty("attribute", Registry.ATTRIBUTE.getId(this.attribute).toString());
         jsonObject.addProperty("operation", getName(this.operation));
         jsonObject.add("amount", context.serialize(this.amountRange));
         if (this.id != null) {
            jsonObject.addProperty("id", this.id.toString());
         }

         if (this.slots.length == 1) {
            jsonObject.addProperty("slot", this.slots[0].getName());
         } else {
            JsonArray jsonArray = new JsonArray();
            EquipmentSlot[] var4 = this.slots;
            int var5 = var4.length;

            for(int var6 = 0; var6 < var5; ++var6) {
               EquipmentSlot equipmentSlot = var4[var6];
               jsonArray.add((JsonElement)(new JsonPrimitive(equipmentSlot.getName())));
            }

            jsonObject.add("slot", jsonArray);
         }

         return jsonObject;
      }

      public static SetAttributesLootFunction.Attribute deserialize(JsonObject json, JsonDeserializationContext context) {
         String string = JsonHelper.getString(json, "name");
         Identifier identifier = new Identifier(JsonHelper.getString(json, "attribute"));
         EntityAttribute entityAttribute = (EntityAttribute)Registry.ATTRIBUTE.get(identifier);
         if (entityAttribute == null) {
            throw new JsonSyntaxException("Unknown attribute: " + identifier);
         } else {
            EntityAttributeModifier.Operation operation = fromName(JsonHelper.getString(json, "operation"));
            UniformLootTableRange uniformLootTableRange = (UniformLootTableRange)JsonHelper.deserialize(json, "amount", context, UniformLootTableRange.class);
            UUID uUID = null;
            EquipmentSlot[] equipmentSlots2;
            if (JsonHelper.hasString(json, "slot")) {
               equipmentSlots2 = new EquipmentSlot[]{EquipmentSlot.byName(JsonHelper.getString(json, "slot"))};
            } else {
               if (!JsonHelper.hasArray(json, "slot")) {
                  throw new JsonSyntaxException("Invalid or missing attribute modifier slot; must be either string or array of strings.");
               }

               JsonArray jsonArray = JsonHelper.getArray(json, "slot");
               equipmentSlots2 = new EquipmentSlot[jsonArray.size()];
               int i = 0;

               JsonElement jsonElement;
               for(Iterator var11 = jsonArray.iterator(); var11.hasNext(); equipmentSlots2[i++] = EquipmentSlot.byName(JsonHelper.asString(jsonElement, "slot"))) {
                  jsonElement = (JsonElement)var11.next();
               }

               if (equipmentSlots2.length == 0) {
                  throw new JsonSyntaxException("Invalid attribute modifier slot; must contain at least one entry.");
               }
            }

            if (json.has("id")) {
               String string2 = JsonHelper.getString(json, "id");

               try {
                  uUID = UUID.fromString(string2);
               } catch (IllegalArgumentException var13) {
                  throw new JsonSyntaxException("Invalid attribute modifier id '" + string2 + "' (must be UUID format, with dashes)");
               }
            }

            return new SetAttributesLootFunction.Attribute(string, entityAttribute, operation, uniformLootTableRange, equipmentSlots2, uUID);
         }
      }

      private static String getName(EntityAttributeModifier.Operation operation) {
         switch(operation) {
         case ADDITION:
            return "addition";
         case MULTIPLY_BASE:
            return "multiply_base";
         case MULTIPLY_TOTAL:
            return "multiply_total";
         default:
            throw new IllegalArgumentException("Unknown operation " + operation);
         }
      }

      private static EntityAttributeModifier.Operation fromName(String name) {
         byte var2 = -1;
         switch(name.hashCode()) {
         case -1226589444:
            if (name.equals("addition")) {
               var2 = 0;
            }
            break;
         case -78229492:
            if (name.equals("multiply_base")) {
               var2 = 1;
            }
            break;
         case 1886894441:
            if (name.equals("multiply_total")) {
               var2 = 2;
            }
         }

         switch(var2) {
         case 0:
            return EntityAttributeModifier.Operation.ADDITION;
         case 1:
            return EntityAttributeModifier.Operation.MULTIPLY_BASE;
         case 2:
            return EntityAttributeModifier.Operation.MULTIPLY_TOTAL;
         default:
            throw new JsonSyntaxException("Unknown attribute modifier operation " + name);
         }
      }
   }

   public static class Serializer extends ConditionalLootFunction.Serializer<SetAttributesLootFunction> {
      public void toJson(JsonObject jsonObject, SetAttributesLootFunction setAttributesLootFunction, JsonSerializationContext jsonSerializationContext) {
         super.toJson(jsonObject, (ConditionalLootFunction)setAttributesLootFunction, jsonSerializationContext);
         JsonArray jsonArray = new JsonArray();
         Iterator var5 = setAttributesLootFunction.attributes.iterator();

         while(var5.hasNext()) {
            SetAttributesLootFunction.Attribute attribute = (SetAttributesLootFunction.Attribute)var5.next();
            jsonArray.add((JsonElement)attribute.serialize(jsonSerializationContext));
         }

         jsonObject.add("modifiers", jsonArray);
      }

      public SetAttributesLootFunction fromJson(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext, LootCondition[] lootConditions) {
         JsonArray jsonArray = JsonHelper.getArray(jsonObject, "modifiers");
         List<SetAttributesLootFunction.Attribute> list = Lists.newArrayListWithExpectedSize(jsonArray.size());
         Iterator var6 = jsonArray.iterator();

         while(var6.hasNext()) {
            JsonElement jsonElement = (JsonElement)var6.next();
            list.add(SetAttributesLootFunction.Attribute.deserialize(JsonHelper.asObject(jsonElement, "modifier"), jsonDeserializationContext));
         }

         if (list.isEmpty()) {
            throw new JsonSyntaxException("Invalid attribute modifiers array; cannot be empty");
         } else {
            return new SetAttributesLootFunction(lootConditions, list);
         }
      }
   }
}
