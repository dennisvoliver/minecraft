package net.minecraft.advancement;

import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.io.Files;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.internal.Streams;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.mojang.datafixers.DataFixer;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JsonOps;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.SharedConstants;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.advancement.criterion.Criterion;
import net.minecraft.advancement.criterion.CriterionConditions;
import net.minecraft.advancement.criterion.CriterionProgress;
import net.minecraft.datafixer.DataFixTypes;
import net.minecraft.network.MessageType;
import net.minecraft.network.packet.s2c.play.AdvancementUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.SelectAdvancementTabS2CPacket;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.ServerAdvancementLoader;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.world.GameRules;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

public class PlayerAdvancementTracker {
   private static final Logger LOGGER = LogManager.getLogger();
   private static final Gson GSON = (new GsonBuilder()).registerTypeAdapter(AdvancementProgress.class, new AdvancementProgress.Serializer()).registerTypeAdapter(Identifier.class, new Identifier.Serializer()).setPrettyPrinting().create();
   private static final TypeToken<Map<Identifier, AdvancementProgress>> JSON_TYPE = new TypeToken<Map<Identifier, AdvancementProgress>>() {
   };
   private final DataFixer field_25324;
   private final PlayerManager field_25325;
   private final File advancementFile;
   private final Map<Advancement, AdvancementProgress> advancementToProgress = Maps.newLinkedHashMap();
   private final Set<Advancement> visibleAdvancements = Sets.newLinkedHashSet();
   private final Set<Advancement> visibilityUpdates = Sets.newLinkedHashSet();
   private final Set<Advancement> progressUpdates = Sets.newLinkedHashSet();
   private ServerPlayerEntity owner;
   @Nullable
   private Advancement currentDisplayTab;
   private boolean dirty = true;

   public PlayerAdvancementTracker(DataFixer dataFixer, PlayerManager playerManager, ServerAdvancementLoader serverAdvancementLoader, File file, ServerPlayerEntity serverPlayerEntity) {
      this.field_25324 = dataFixer;
      this.field_25325 = playerManager;
      this.advancementFile = file;
      this.owner = serverPlayerEntity;
      this.load(serverAdvancementLoader);
   }

   public void setOwner(ServerPlayerEntity owner) {
      this.owner = owner;
   }

   public void clearCriteria() {
      Iterator var1 = Criteria.getCriteria().iterator();

      while(var1.hasNext()) {
         Criterion<?> criterion = (Criterion)var1.next();
         criterion.endTracking(this);
      }

   }

   public void reload(ServerAdvancementLoader advancementLoader) {
      this.clearCriteria();
      this.advancementToProgress.clear();
      this.visibleAdvancements.clear();
      this.visibilityUpdates.clear();
      this.progressUpdates.clear();
      this.dirty = true;
      this.currentDisplayTab = null;
      this.load(advancementLoader);
   }

   private void beginTrackingAllAdvancements(ServerAdvancementLoader advancementLoader) {
      Iterator var2 = advancementLoader.getAdvancements().iterator();

      while(var2.hasNext()) {
         Advancement advancement = (Advancement)var2.next();
         this.beginTracking(advancement);
      }

   }

   private void updateCompleted() {
      List<Advancement> list = Lists.newArrayList();
      Iterator var2 = this.advancementToProgress.entrySet().iterator();

      while(var2.hasNext()) {
         Entry<Advancement, AdvancementProgress> entry = (Entry)var2.next();
         if (((AdvancementProgress)entry.getValue()).isDone()) {
            list.add(entry.getKey());
            this.progressUpdates.add(entry.getKey());
         }
      }

      var2 = list.iterator();

      while(var2.hasNext()) {
         Advancement advancement = (Advancement)var2.next();
         this.updateDisplay(advancement);
      }

   }

   private void rewardEmptyAdvancements(ServerAdvancementLoader advancementLoader) {
      Iterator var2 = advancementLoader.getAdvancements().iterator();

      while(var2.hasNext()) {
         Advancement advancement = (Advancement)var2.next();
         if (advancement.getCriteria().isEmpty()) {
            this.grantCriterion(advancement, "");
            advancement.getRewards().apply(this.owner);
         }
      }

   }

   private void load(ServerAdvancementLoader advancementLoader) {
      if (this.advancementFile.isFile()) {
         try {
            JsonReader jsonReader = new JsonReader(new StringReader(Files.toString(this.advancementFile, StandardCharsets.UTF_8)));
            Throwable var3 = null;

            try {
               jsonReader.setLenient(false);
               Dynamic<JsonElement> dynamic = new Dynamic(JsonOps.INSTANCE, Streams.parse(jsonReader));
               if (!dynamic.get("DataVersion").asNumber().result().isPresent()) {
                  dynamic = dynamic.set("DataVersion", dynamic.createInt(1343));
               }

               dynamic = this.field_25324.update(DataFixTypes.ADVANCEMENTS.getTypeReference(), dynamic, dynamic.get("DataVersion").asInt(0), SharedConstants.getGameVersion().getWorldVersion());
               dynamic = dynamic.remove("DataVersion");
               Map<Identifier, AdvancementProgress> map = (Map)GSON.getAdapter(JSON_TYPE).fromJsonTree((JsonElement)dynamic.getValue());
               if (map == null) {
                  throw new JsonParseException("Found null for advancements");
               }

               Stream<Entry<Identifier, AdvancementProgress>> stream = map.entrySet().stream().sorted(Comparator.comparing(Entry::getValue));
               Iterator var7 = ((List)stream.collect(Collectors.toList())).iterator();

               while(var7.hasNext()) {
                  Entry<Identifier, AdvancementProgress> entry = (Entry)var7.next();
                  Advancement advancement = advancementLoader.get((Identifier)entry.getKey());
                  if (advancement == null) {
                     LOGGER.warn((String)"Ignored advancement '{}' in progress file {} - it doesn't exist anymore?", (Object)entry.getKey(), (Object)this.advancementFile);
                  } else {
                     this.initProgress(advancement, (AdvancementProgress)entry.getValue());
                  }
               }
            } catch (Throwable var19) {
               var3 = var19;
               throw var19;
            } finally {
               if (jsonReader != null) {
                  if (var3 != null) {
                     try {
                        jsonReader.close();
                     } catch (Throwable var18) {
                        var3.addSuppressed(var18);
                     }
                  } else {
                     jsonReader.close();
                  }
               }

            }
         } catch (JsonParseException var21) {
            LOGGER.error((String)"Couldn't parse player advancements in {}", (Object)this.advancementFile, (Object)var21);
         } catch (IOException var22) {
            LOGGER.error((String)"Couldn't access player advancements in {}", (Object)this.advancementFile, (Object)var22);
         }
      }

      this.rewardEmptyAdvancements(advancementLoader);
      this.updateCompleted();
      this.beginTrackingAllAdvancements(advancementLoader);
   }

   public void save() {
      Map<Identifier, AdvancementProgress> map = Maps.newHashMap();
      Iterator var2 = this.advancementToProgress.entrySet().iterator();

      while(var2.hasNext()) {
         Entry<Advancement, AdvancementProgress> entry = (Entry)var2.next();
         AdvancementProgress advancementProgress = (AdvancementProgress)entry.getValue();
         if (advancementProgress.isAnyObtained()) {
            map.put(((Advancement)entry.getKey()).getId(), advancementProgress);
         }
      }

      if (this.advancementFile.getParentFile() != null) {
         this.advancementFile.getParentFile().mkdirs();
      }

      JsonElement jsonElement = GSON.toJsonTree(map);
      jsonElement.getAsJsonObject().addProperty("DataVersion", (Number)SharedConstants.getGameVersion().getWorldVersion());

      try {
         OutputStream outputStream = new FileOutputStream(this.advancementFile);
         Throwable var38 = null;

         try {
            Writer writer = new OutputStreamWriter(outputStream, Charsets.UTF_8.newEncoder());
            Throwable var6 = null;

            try {
               GSON.toJson((JsonElement)jsonElement, (Appendable)writer);
            } catch (Throwable var31) {
               var6 = var31;
               throw var31;
            } finally {
               if (writer != null) {
                  if (var6 != null) {
                     try {
                        writer.close();
                     } catch (Throwable var30) {
                        var6.addSuppressed(var30);
                     }
                  } else {
                     writer.close();
                  }
               }

            }
         } catch (Throwable var33) {
            var38 = var33;
            throw var33;
         } finally {
            if (outputStream != null) {
               if (var38 != null) {
                  try {
                     outputStream.close();
                  } catch (Throwable var29) {
                     var38.addSuppressed(var29);
                  }
               } else {
                  outputStream.close();
               }
            }

         }
      } catch (IOException var35) {
         LOGGER.error((String)"Couldn't save player advancements to {}", (Object)this.advancementFile, (Object)var35);
      }

   }

   public boolean grantCriterion(Advancement advancement, String criterionName) {
      boolean bl = false;
      AdvancementProgress advancementProgress = this.getProgress(advancement);
      boolean bl2 = advancementProgress.isDone();
      if (advancementProgress.obtain(criterionName)) {
         this.endTrackingCompleted(advancement);
         this.progressUpdates.add(advancement);
         bl = true;
         if (!bl2 && advancementProgress.isDone()) {
            advancement.getRewards().apply(this.owner);
            if (advancement.getDisplay() != null && advancement.getDisplay().shouldAnnounceToChat() && this.owner.world.getGameRules().getBoolean(GameRules.ANNOUNCE_ADVANCEMENTS)) {
               this.field_25325.broadcastChatMessage(new TranslatableText("chat.type.advancement." + advancement.getDisplay().getFrame().getId(), new Object[]{this.owner.getDisplayName(), advancement.toHoverableText()}), MessageType.SYSTEM, Util.NIL_UUID);
            }
         }
      }

      if (advancementProgress.isDone()) {
         this.updateDisplay(advancement);
      }

      return bl;
   }

   public boolean revokeCriterion(Advancement advancement, String criterionName) {
      boolean bl = false;
      AdvancementProgress advancementProgress = this.getProgress(advancement);
      if (advancementProgress.reset(criterionName)) {
         this.beginTracking(advancement);
         this.progressUpdates.add(advancement);
         bl = true;
      }

      if (!advancementProgress.isAnyObtained()) {
         this.updateDisplay(advancement);
      }

      return bl;
   }

   private void beginTracking(Advancement advancement) {
      AdvancementProgress advancementProgress = this.getProgress(advancement);
      if (!advancementProgress.isDone()) {
         Iterator var3 = advancement.getCriteria().entrySet().iterator();

         while(var3.hasNext()) {
            Entry<String, AdvancementCriterion> entry = (Entry)var3.next();
            CriterionProgress criterionProgress = advancementProgress.getCriterionProgress((String)entry.getKey());
            if (criterionProgress != null && !criterionProgress.isObtained()) {
               CriterionConditions criterionConditions = ((AdvancementCriterion)entry.getValue()).getConditions();
               if (criterionConditions != null) {
                  Criterion<CriterionConditions> criterion = Criteria.getById(criterionConditions.getId());
                  if (criterion != null) {
                     criterion.beginTrackingCondition(this, new Criterion.ConditionsContainer(criterionConditions, advancement, (String)entry.getKey()));
                  }
               }
            }
         }

      }
   }

   private void endTrackingCompleted(Advancement advancement) {
      AdvancementProgress advancementProgress = this.getProgress(advancement);
      Iterator var3 = advancement.getCriteria().entrySet().iterator();

      while(true) {
         Entry entry;
         CriterionProgress criterionProgress;
         do {
            do {
               if (!var3.hasNext()) {
                  return;
               }

               entry = (Entry)var3.next();
               criterionProgress = advancementProgress.getCriterionProgress((String)entry.getKey());
            } while(criterionProgress == null);
         } while(!criterionProgress.isObtained() && !advancementProgress.isDone());

         CriterionConditions criterionConditions = ((AdvancementCriterion)entry.getValue()).getConditions();
         if (criterionConditions != null) {
            Criterion<CriterionConditions> criterion = Criteria.getById(criterionConditions.getId());
            if (criterion != null) {
               criterion.endTrackingCondition(this, new Criterion.ConditionsContainer(criterionConditions, advancement, (String)entry.getKey()));
            }
         }
      }
   }

   public void sendUpdate(ServerPlayerEntity player) {
      if (this.dirty || !this.visibilityUpdates.isEmpty() || !this.progressUpdates.isEmpty()) {
         Map<Identifier, AdvancementProgress> map = Maps.newHashMap();
         Set<Advancement> set = Sets.newLinkedHashSet();
         Set<Identifier> set2 = Sets.newLinkedHashSet();
         Iterator var5 = this.progressUpdates.iterator();

         Advancement advancement2;
         while(var5.hasNext()) {
            advancement2 = (Advancement)var5.next();
            if (this.visibleAdvancements.contains(advancement2)) {
               map.put(advancement2.getId(), this.advancementToProgress.get(advancement2));
            }
         }

         var5 = this.visibilityUpdates.iterator();

         while(var5.hasNext()) {
            advancement2 = (Advancement)var5.next();
            if (this.visibleAdvancements.contains(advancement2)) {
               set.add(advancement2);
            } else {
               set2.add(advancement2.getId());
            }
         }

         if (this.dirty || !map.isEmpty() || !set.isEmpty() || !set2.isEmpty()) {
            player.networkHandler.sendPacket(new AdvancementUpdateS2CPacket(this.dirty, set, set2, map));
            this.visibilityUpdates.clear();
            this.progressUpdates.clear();
         }
      }

      this.dirty = false;
   }

   public void setDisplayTab(@Nullable Advancement advancement) {
      Advancement advancement2 = this.currentDisplayTab;
      if (advancement != null && advancement.getParent() == null && advancement.getDisplay() != null) {
         this.currentDisplayTab = advancement;
      } else {
         this.currentDisplayTab = null;
      }

      if (advancement2 != this.currentDisplayTab) {
         this.owner.networkHandler.sendPacket(new SelectAdvancementTabS2CPacket(this.currentDisplayTab == null ? null : this.currentDisplayTab.getId()));
      }

   }

   public AdvancementProgress getProgress(Advancement advancement) {
      AdvancementProgress advancementProgress = (AdvancementProgress)this.advancementToProgress.get(advancement);
      if (advancementProgress == null) {
         advancementProgress = new AdvancementProgress();
         this.initProgress(advancement, advancementProgress);
      }

      return advancementProgress;
   }

   private void initProgress(Advancement advancement, AdvancementProgress progress) {
      progress.init(advancement.getCriteria(), advancement.getRequirements());
      this.advancementToProgress.put(advancement, progress);
   }

   private void updateDisplay(Advancement advancement) {
      boolean bl = this.canSee(advancement);
      boolean bl2 = this.visibleAdvancements.contains(advancement);
      if (bl && !bl2) {
         this.visibleAdvancements.add(advancement);
         this.visibilityUpdates.add(advancement);
         if (this.advancementToProgress.containsKey(advancement)) {
            this.progressUpdates.add(advancement);
         }
      } else if (!bl && bl2) {
         this.visibleAdvancements.remove(advancement);
         this.visibilityUpdates.add(advancement);
      }

      if (bl != bl2 && advancement.getParent() != null) {
         this.updateDisplay(advancement.getParent());
      }

      Iterator var4 = advancement.getChildren().iterator();

      while(var4.hasNext()) {
         Advancement advancement2 = (Advancement)var4.next();
         this.updateDisplay(advancement2);
      }

   }

   private boolean canSee(Advancement advancement) {
      for(int i = 0; advancement != null && i <= 2; ++i) {
         if (i == 0 && this.hasChildrenDone(advancement)) {
            return true;
         }

         if (advancement.getDisplay() == null) {
            return false;
         }

         AdvancementProgress advancementProgress = this.getProgress(advancement);
         if (advancementProgress.isDone()) {
            return true;
         }

         if (advancement.getDisplay().isHidden()) {
            return false;
         }

         advancement = advancement.getParent();
      }

      return false;
   }

   private boolean hasChildrenDone(Advancement advancement) {
      AdvancementProgress advancementProgress = this.getProgress(advancement);
      if (advancementProgress.isDone()) {
         return true;
      } else {
         Iterator var3 = advancement.getChildren().iterator();

         Advancement advancement2;
         do {
            if (!var3.hasNext()) {
               return false;
            }

            advancement2 = (Advancement)var3.next();
         } while(!this.hasChildrenDone(advancement2));

         return true;
      }
   }
}
