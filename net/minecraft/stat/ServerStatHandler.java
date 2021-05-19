package net.minecraft.stat;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.internal.Streams;
import com.google.gson.stream.JsonReader;
import com.mojang.datafixers.DataFixer;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.Map.Entry;
import net.minecraft.SharedConstants;
import net.minecraft.datafixer.DataFixTypes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.network.packet.s2c.play.StatisticsS2CPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.registry.Registry;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ServerStatHandler extends StatHandler {
   private static final Logger LOGGER = LogManager.getLogger();
   private final MinecraftServer server;
   private final File file;
   private final Set<Stat<?>> pendingStats = Sets.newHashSet();
   private int lastStatsUpdate = -300;

   public ServerStatHandler(MinecraftServer server, File file) {
      this.server = server;
      this.file = file;
      if (file.isFile()) {
         try {
            this.parse(server.getDataFixer(), FileUtils.readFileToString(file));
         } catch (IOException var4) {
            LOGGER.error((String)"Couldn't read statistics file {}", (Object)file, (Object)var4);
         } catch (JsonParseException var5) {
            LOGGER.error((String)"Couldn't parse statistics file {}", (Object)file, (Object)var5);
         }
      }

   }

   public void save() {
      try {
         FileUtils.writeStringToFile(this.file, this.asString());
      } catch (IOException var2) {
         LOGGER.error((String)"Couldn't save stats", (Throwable)var2);
      }

   }

   public void setStat(PlayerEntity player, Stat<?> stat, int value) {
      super.setStat(player, stat, value);
      this.pendingStats.add(stat);
   }

   private Set<Stat<?>> takePendingStats() {
      Set<Stat<?>> set = Sets.newHashSet((Iterable)this.pendingStats);
      this.pendingStats.clear();
      return set;
   }

   public void parse(DataFixer dataFixer, String json) {
      try {
         JsonReader jsonReader = new JsonReader(new StringReader(json));
         Throwable var4 = null;

         try {
            jsonReader.setLenient(false);
            JsonElement jsonElement = Streams.parse(jsonReader);
            if (!jsonElement.isJsonNull()) {
               CompoundTag compoundTag = jsonToCompound(jsonElement.getAsJsonObject());
               if (!compoundTag.contains("DataVersion", 99)) {
                  compoundTag.putInt("DataVersion", 1343);
               }

               compoundTag = NbtHelper.update(dataFixer, DataFixTypes.STATS, compoundTag, compoundTag.getInt("DataVersion"));
               if (compoundTag.contains("stats", 10)) {
                  CompoundTag compoundTag2 = compoundTag.getCompound("stats");
                  Iterator var8 = compoundTag2.getKeys().iterator();

                  while(var8.hasNext()) {
                     String string = (String)var8.next();
                     if (compoundTag2.contains(string, 10)) {
                        Util.ifPresentOrElse(Registry.STAT_TYPE.getOrEmpty(new Identifier(string)), (statType) -> {
                           CompoundTag compoundTag2x = compoundTag2.getCompound(string);
                           Iterator var5 = compoundTag2x.getKeys().iterator();

                           while(var5.hasNext()) {
                              String string2 = (String)var5.next();
                              if (compoundTag2x.contains(string2, 99)) {
                                 Util.ifPresentOrElse(this.createStat(statType, string2), (stat) -> {
                                    this.statMap.put(stat, compoundTag2x.getInt(string2));
                                 }, () -> {
                                    LOGGER.warn((String)"Invalid statistic in {}: Don't know what {} is", (Object)this.file, (Object)string2);
                                 });
                              } else {
                                 LOGGER.warn((String)"Invalid statistic value in {}: Don't know what {} is for key {}", (Object)this.file, compoundTag2x.get(string2), string2);
                              }
                           }

                        }, () -> {
                           LOGGER.warn((String)"Invalid statistic type in {}: Don't know what {} is", (Object)this.file, (Object)string);
                        });
                     }
                  }
               }

               return;
            }

            LOGGER.error((String)"Unable to parse Stat data from {}", (Object)this.file);
         } catch (Throwable var19) {
            var4 = var19;
            throw var19;
         } finally {
            if (jsonReader != null) {
               if (var4 != null) {
                  try {
                     jsonReader.close();
                  } catch (Throwable var18) {
                     var4.addSuppressed(var18);
                  }
               } else {
                  jsonReader.close();
               }
            }

         }

      } catch (IOException | JsonParseException var21) {
         LOGGER.error((String)"Unable to parse Stat data from {}", (Object)this.file, (Object)var21);
      }
   }

   private <T> Optional<Stat<T>> createStat(StatType<T> type, String id) {
      Optional var10000 = Optional.ofNullable(Identifier.tryParse(id));
      Registry var10001 = type.getRegistry();
      var10001.getClass();
      var10000 = var10000.flatMap(var10001::getOrEmpty);
      type.getClass();
      return var10000.map(type::getOrCreateStat);
   }

   private static CompoundTag jsonToCompound(JsonObject jsonObject) {
      CompoundTag compoundTag = new CompoundTag();
      Iterator var2 = jsonObject.entrySet().iterator();

      while(var2.hasNext()) {
         Entry<String, JsonElement> entry = (Entry)var2.next();
         JsonElement jsonElement = (JsonElement)entry.getValue();
         if (jsonElement.isJsonObject()) {
            compoundTag.put((String)entry.getKey(), jsonToCompound(jsonElement.getAsJsonObject()));
         } else if (jsonElement.isJsonPrimitive()) {
            JsonPrimitive jsonPrimitive = jsonElement.getAsJsonPrimitive();
            if (jsonPrimitive.isNumber()) {
               compoundTag.putInt((String)entry.getKey(), jsonPrimitive.getAsInt());
            }
         }
      }

      return compoundTag;
   }

   protected String asString() {
      Map<StatType<?>, JsonObject> map = Maps.newHashMap();
      ObjectIterator var2 = this.statMap.object2IntEntrySet().iterator();

      while(var2.hasNext()) {
         it.unimi.dsi.fastutil.objects.Object2IntMap.Entry<Stat<?>> entry = (it.unimi.dsi.fastutil.objects.Object2IntMap.Entry)var2.next();
         Stat<?> stat = (Stat)entry.getKey();
         ((JsonObject)map.computeIfAbsent(stat.getType(), (statType) -> {
            return new JsonObject();
         })).addProperty(getStatId(stat).toString(), (Number)entry.getIntValue());
      }

      JsonObject jsonObject = new JsonObject();
      Iterator var6 = map.entrySet().iterator();

      while(var6.hasNext()) {
         Entry<StatType<?>, JsonObject> entry2 = (Entry)var6.next();
         jsonObject.add(Registry.STAT_TYPE.getId(entry2.getKey()).toString(), (JsonElement)entry2.getValue());
      }

      JsonObject jsonObject2 = new JsonObject();
      jsonObject2.add("stats", jsonObject);
      jsonObject2.addProperty("DataVersion", (Number)SharedConstants.getGameVersion().getWorldVersion());
      return jsonObject2.toString();
   }

   private static <T> Identifier getStatId(Stat<T> stat) {
      return stat.getType().getRegistry().getId(stat.getValue());
   }

   public void updateStatSet() {
      this.pendingStats.addAll(this.statMap.keySet());
   }

   public void sendStats(ServerPlayerEntity player) {
      int i = this.server.getTicks();
      Object2IntMap<Stat<?>> object2IntMap = new Object2IntOpenHashMap();
      if (i - this.lastStatsUpdate > 300) {
         this.lastStatsUpdate = i;
         Iterator var4 = this.takePendingStats().iterator();

         while(var4.hasNext()) {
            Stat<?> stat = (Stat)var4.next();
            object2IntMap.put(stat, this.getStat(stat));
         }
      }

      player.networkHandler.sendPacket(new StatisticsS2CPacket(object2IntMap));
   }
}
