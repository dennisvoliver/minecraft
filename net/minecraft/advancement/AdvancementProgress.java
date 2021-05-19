package net.minecraft.advancement;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.advancement.criterion.CriterionProgress;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.JsonHelper;
import org.jetbrains.annotations.Nullable;

public class AdvancementProgress implements Comparable<AdvancementProgress> {
   private final Map<String, CriterionProgress> criteriaProgresses = Maps.newHashMap();
   private String[][] requirements = new String[0][];

   public void init(Map<String, AdvancementCriterion> criteria, String[][] requirements) {
      Set<String> set = criteria.keySet();
      this.criteriaProgresses.entrySet().removeIf((entry) -> {
         return !set.contains(entry.getKey());
      });
      Iterator var4 = set.iterator();

      while(var4.hasNext()) {
         String string = (String)var4.next();
         if (!this.criteriaProgresses.containsKey(string)) {
            this.criteriaProgresses.put(string, new CriterionProgress());
         }
      }

      this.requirements = requirements;
   }

   public boolean isDone() {
      if (this.requirements.length == 0) {
         return false;
      } else {
         String[][] var1 = this.requirements;
         int var2 = var1.length;

         for(int var3 = 0; var3 < var2; ++var3) {
            String[] strings = var1[var3];
            boolean bl = false;
            String[] var6 = strings;
            int var7 = strings.length;

            for(int var8 = 0; var8 < var7; ++var8) {
               String string = var6[var8];
               CriterionProgress criterionProgress = this.getCriterionProgress(string);
               if (criterionProgress != null && criterionProgress.isObtained()) {
                  bl = true;
                  break;
               }
            }

            if (!bl) {
               return false;
            }
         }

         return true;
      }
   }

   public boolean isAnyObtained() {
      Iterator var1 = this.criteriaProgresses.values().iterator();

      CriterionProgress criterionProgress;
      do {
         if (!var1.hasNext()) {
            return false;
         }

         criterionProgress = (CriterionProgress)var1.next();
      } while(!criterionProgress.isObtained());

      return true;
   }

   public boolean obtain(String name) {
      CriterionProgress criterionProgress = (CriterionProgress)this.criteriaProgresses.get(name);
      if (criterionProgress != null && !criterionProgress.isObtained()) {
         criterionProgress.obtain();
         return true;
      } else {
         return false;
      }
   }

   public boolean reset(String name) {
      CriterionProgress criterionProgress = (CriterionProgress)this.criteriaProgresses.get(name);
      if (criterionProgress != null && criterionProgress.isObtained()) {
         criterionProgress.reset();
         return true;
      } else {
         return false;
      }
   }

   public String toString() {
      return "AdvancementProgress{criteria=" + this.criteriaProgresses + ", requirements=" + Arrays.deepToString(this.requirements) + '}';
   }

   public void toPacket(PacketByteBuf buf) {
      buf.writeVarInt(this.criteriaProgresses.size());
      Iterator var2 = this.criteriaProgresses.entrySet().iterator();

      while(var2.hasNext()) {
         Entry<String, CriterionProgress> entry = (Entry)var2.next();
         buf.writeString((String)entry.getKey());
         ((CriterionProgress)entry.getValue()).toPacket(buf);
      }

   }

   public static AdvancementProgress fromPacket(PacketByteBuf buf) {
      AdvancementProgress advancementProgress = new AdvancementProgress();
      int i = buf.readVarInt();

      for(int j = 0; j < i; ++j) {
         advancementProgress.criteriaProgresses.put(buf.readString(32767), CriterionProgress.fromPacket(buf));
      }

      return advancementProgress;
   }

   @Nullable
   public CriterionProgress getCriterionProgress(String name) {
      return (CriterionProgress)this.criteriaProgresses.get(name);
   }

   @Environment(EnvType.CLIENT)
   public float getProgressBarPercentage() {
      if (this.criteriaProgresses.isEmpty()) {
         return 0.0F;
      } else {
         float f = (float)this.requirements.length;
         float g = (float)this.countObtainedRequirements();
         return g / f;
      }
   }

   @Nullable
   @Environment(EnvType.CLIENT)
   public String getProgressBarFraction() {
      if (this.criteriaProgresses.isEmpty()) {
         return null;
      } else {
         int i = this.requirements.length;
         if (i <= 1) {
            return null;
         } else {
            int j = this.countObtainedRequirements();
            return j + "/" + i;
         }
      }
   }

   @Environment(EnvType.CLIENT)
   private int countObtainedRequirements() {
      int i = 0;
      String[][] var2 = this.requirements;
      int var3 = var2.length;

      for(int var4 = 0; var4 < var3; ++var4) {
         String[] strings = var2[var4];
         boolean bl = false;
         String[] var7 = strings;
         int var8 = strings.length;

         for(int var9 = 0; var9 < var8; ++var9) {
            String string = var7[var9];
            CriterionProgress criterionProgress = this.getCriterionProgress(string);
            if (criterionProgress != null && criterionProgress.isObtained()) {
               bl = true;
               break;
            }
         }

         if (bl) {
            ++i;
         }
      }

      return i;
   }

   public Iterable<String> getUnobtainedCriteria() {
      List<String> list = Lists.newArrayList();
      Iterator var2 = this.criteriaProgresses.entrySet().iterator();

      while(var2.hasNext()) {
         Entry<String, CriterionProgress> entry = (Entry)var2.next();
         if (!((CriterionProgress)entry.getValue()).isObtained()) {
            list.add(entry.getKey());
         }
      }

      return list;
   }

   public Iterable<String> getObtainedCriteria() {
      List<String> list = Lists.newArrayList();
      Iterator var2 = this.criteriaProgresses.entrySet().iterator();

      while(var2.hasNext()) {
         Entry<String, CriterionProgress> entry = (Entry)var2.next();
         if (((CriterionProgress)entry.getValue()).isObtained()) {
            list.add(entry.getKey());
         }
      }

      return list;
   }

   @Nullable
   public Date getEarliestProgressObtainDate() {
      Date date = null;
      Iterator var2 = this.criteriaProgresses.values().iterator();

      while(true) {
         CriterionProgress criterionProgress;
         do {
            do {
               if (!var2.hasNext()) {
                  return date;
               }

               criterionProgress = (CriterionProgress)var2.next();
            } while(!criterionProgress.isObtained());
         } while(date != null && !criterionProgress.getObtainedDate().before(date));

         date = criterionProgress.getObtainedDate();
      }
   }

   public int compareTo(AdvancementProgress advancementProgress) {
      Date date = this.getEarliestProgressObtainDate();
      Date date2 = advancementProgress.getEarliestProgressObtainDate();
      if (date == null && date2 != null) {
         return 1;
      } else if (date != null && date2 == null) {
         return -1;
      } else {
         return date == null && date2 == null ? 0 : date.compareTo(date2);
      }
   }

   public static class Serializer implements JsonDeserializer<AdvancementProgress>, JsonSerializer<AdvancementProgress> {
      public JsonElement serialize(AdvancementProgress advancementProgress, Type type, JsonSerializationContext jsonSerializationContext) {
         JsonObject jsonObject = new JsonObject();
         JsonObject jsonObject2 = new JsonObject();
         Iterator var6 = advancementProgress.criteriaProgresses.entrySet().iterator();

         while(var6.hasNext()) {
            Entry<String, CriterionProgress> entry = (Entry)var6.next();
            CriterionProgress criterionProgress = (CriterionProgress)entry.getValue();
            if (criterionProgress.isObtained()) {
               jsonObject2.add((String)entry.getKey(), criterionProgress.toJson());
            }
         }

         if (!jsonObject2.entrySet().isEmpty()) {
            jsonObject.add("criteria", jsonObject2);
         }

         jsonObject.addProperty("done", advancementProgress.isDone());
         return jsonObject;
      }

      public AdvancementProgress deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
         JsonObject jsonObject = JsonHelper.asObject(jsonElement, "advancement");
         JsonObject jsonObject2 = JsonHelper.getObject(jsonObject, "criteria", new JsonObject());
         AdvancementProgress advancementProgress = new AdvancementProgress();
         Iterator var7 = jsonObject2.entrySet().iterator();

         while(var7.hasNext()) {
            Entry<String, JsonElement> entry = (Entry)var7.next();
            String string = (String)entry.getKey();
            advancementProgress.criteriaProgresses.put(string, CriterionProgress.obtainedAt(JsonHelper.asString((JsonElement)entry.getValue(), string)));
         }

         return advancementProgress;
      }
   }
}
