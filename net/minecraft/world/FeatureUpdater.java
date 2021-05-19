package net.minecraft.world;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongList;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.util.Util;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.gen.feature.StructureFeature;
import org.jetbrains.annotations.Nullable;

public class FeatureUpdater {
   private static final Map<String, String> OLD_TO_NEW = (Map)Util.make(Maps.newHashMap(), (hashMap) -> {
      hashMap.put("Village", "Village");
      hashMap.put("Mineshaft", "Mineshaft");
      hashMap.put("Mansion", "Mansion");
      hashMap.put("Igloo", "Temple");
      hashMap.put("Desert_Pyramid", "Temple");
      hashMap.put("Jungle_Pyramid", "Temple");
      hashMap.put("Swamp_Hut", "Temple");
      hashMap.put("Stronghold", "Stronghold");
      hashMap.put("Monument", "Monument");
      hashMap.put("Fortress", "Fortress");
      hashMap.put("EndCity", "EndCity");
   });
   private static final Map<String, String> ANCIENT_TO_OLD = (Map)Util.make(Maps.newHashMap(), (hashMap) -> {
      hashMap.put("Iglu", "Igloo");
      hashMap.put("TeDP", "Desert_Pyramid");
      hashMap.put("TeJP", "Jungle_Pyramid");
      hashMap.put("TeSH", "Swamp_Hut");
   });
   private final boolean needsUpdate;
   private final Map<String, Long2ObjectMap<CompoundTag>> featureIdToChunkTag = Maps.newHashMap();
   private final Map<String, ChunkUpdateState> updateStates = Maps.newHashMap();
   private final List<String> field_17658;
   private final List<String> field_17659;

   public FeatureUpdater(@Nullable PersistentStateManager persistentStateManager, List<String> list, List<String> list2) {
      this.field_17658 = list;
      this.field_17659 = list2;
      this.init(persistentStateManager);
      boolean bl = false;

      String string;
      for(Iterator var5 = this.field_17659.iterator(); var5.hasNext(); bl |= this.featureIdToChunkTag.get(string) != null) {
         string = (String)var5.next();
      }

      this.needsUpdate = bl;
   }

   public void markResolved(long l) {
      Iterator var3 = this.field_17658.iterator();

      while(var3.hasNext()) {
         String string = (String)var3.next();
         ChunkUpdateState chunkUpdateState = (ChunkUpdateState)this.updateStates.get(string);
         if (chunkUpdateState != null && chunkUpdateState.isRemaining(l)) {
            chunkUpdateState.markResolved(l);
            chunkUpdateState.markDirty();
         }
      }

   }

   public CompoundTag getUpdatedReferences(CompoundTag compoundTag) {
      CompoundTag compoundTag2 = compoundTag.getCompound("Level");
      ChunkPos chunkPos = new ChunkPos(compoundTag2.getInt("xPos"), compoundTag2.getInt("zPos"));
      if (this.needsUpdate(chunkPos.x, chunkPos.z)) {
         compoundTag = this.getUpdatedStarts(compoundTag, chunkPos);
      }

      CompoundTag compoundTag3 = compoundTag2.getCompound("Structures");
      CompoundTag compoundTag4 = compoundTag3.getCompound("References");
      Iterator var6 = this.field_17659.iterator();

      while(true) {
         String string;
         StructureFeature structureFeature;
         do {
            do {
               if (!var6.hasNext()) {
                  compoundTag3.put("References", compoundTag4);
                  compoundTag2.put("Structures", compoundTag3);
                  compoundTag.put("Level", compoundTag2);
                  return compoundTag;
               }

               string = (String)var6.next();
               structureFeature = (StructureFeature)StructureFeature.STRUCTURES.get(string.toLowerCase(Locale.ROOT));
            } while(compoundTag4.contains(string, 12));
         } while(structureFeature == null);

         int i = true;
         LongList longList = new LongArrayList();

         for(int j = chunkPos.x - 8; j <= chunkPos.x + 8; ++j) {
            for(int k = chunkPos.z - 8; k <= chunkPos.z + 8; ++k) {
               if (this.needsUpdate(j, k, string)) {
                  longList.add(ChunkPos.toLong(j, k));
               }
            }
         }

         compoundTag4.putLongArray(string, (List)longList);
      }
   }

   private boolean needsUpdate(int chunkX, int chunkZ, String id) {
      if (!this.needsUpdate) {
         return false;
      } else {
         return this.featureIdToChunkTag.get(id) != null && ((ChunkUpdateState)this.updateStates.get(OLD_TO_NEW.get(id))).contains(ChunkPos.toLong(chunkX, chunkZ));
      }
   }

   private boolean needsUpdate(int chunkX, int chunkZ) {
      if (!this.needsUpdate) {
         return false;
      } else {
         Iterator var3 = this.field_17659.iterator();

         String string;
         do {
            if (!var3.hasNext()) {
               return false;
            }

            string = (String)var3.next();
         } while(this.featureIdToChunkTag.get(string) == null || !((ChunkUpdateState)this.updateStates.get(OLD_TO_NEW.get(string))).isRemaining(ChunkPos.toLong(chunkX, chunkZ)));

         return true;
      }
   }

   private CompoundTag getUpdatedStarts(CompoundTag compoundTag, ChunkPos chunkPos) {
      CompoundTag compoundTag2 = compoundTag.getCompound("Level");
      CompoundTag compoundTag3 = compoundTag2.getCompound("Structures");
      CompoundTag compoundTag4 = compoundTag3.getCompound("Starts");
      Iterator var6 = this.field_17659.iterator();

      while(var6.hasNext()) {
         String string = (String)var6.next();
         Long2ObjectMap<CompoundTag> long2ObjectMap = (Long2ObjectMap)this.featureIdToChunkTag.get(string);
         if (long2ObjectMap != null) {
            long l = chunkPos.toLong();
            if (((ChunkUpdateState)this.updateStates.get(OLD_TO_NEW.get(string))).isRemaining(l)) {
               CompoundTag compoundTag5 = (CompoundTag)long2ObjectMap.get(l);
               if (compoundTag5 != null) {
                  compoundTag4.put(string, compoundTag5);
               }
            }
         }
      }

      compoundTag3.put("Starts", compoundTag4);
      compoundTag2.put("Structures", compoundTag3);
      compoundTag.put("Level", compoundTag2);
      return compoundTag;
   }

   private void init(@Nullable PersistentStateManager persistentStateManager) {
      if (persistentStateManager != null) {
         Iterator var2 = this.field_17658.iterator();

         while(var2.hasNext()) {
            String string = (String)var2.next();
            CompoundTag compoundTag = new CompoundTag();

            try {
               compoundTag = persistentStateManager.readTag(string, 1493).getCompound("data").getCompound("Features");
               if (compoundTag.isEmpty()) {
                  continue;
               }
            } catch (IOException var13) {
            }

            Iterator var5 = compoundTag.getKeys().iterator();

            while(var5.hasNext()) {
               String string2 = (String)var5.next();
               CompoundTag compoundTag2 = compoundTag.getCompound(string2);
               long l = ChunkPos.toLong(compoundTag2.getInt("ChunkX"), compoundTag2.getInt("ChunkZ"));
               ListTag listTag = compoundTag2.getList("Children", 10);
               String string5;
               if (!listTag.isEmpty()) {
                  string5 = listTag.getCompound(0).getString("id");
                  String string4 = (String)ANCIENT_TO_OLD.get(string5);
                  if (string4 != null) {
                     compoundTag2.putString("id", string4);
                  }
               }

               string5 = compoundTag2.getString("id");
               ((Long2ObjectMap)this.featureIdToChunkTag.computeIfAbsent(string5, (stringx) -> {
                  return new Long2ObjectOpenHashMap();
               })).put(l, compoundTag2);
            }

            String string6 = string + "_index";
            ChunkUpdateState chunkUpdateState = (ChunkUpdateState)persistentStateManager.getOrCreate(() -> {
               return new ChunkUpdateState(string6);
            }, string6);
            if (!chunkUpdateState.getAll().isEmpty()) {
               this.updateStates.put(string, chunkUpdateState);
            } else {
               ChunkUpdateState chunkUpdateState2 = new ChunkUpdateState(string6);
               this.updateStates.put(string, chunkUpdateState2);
               Iterator var17 = compoundTag.getKeys().iterator();

               while(var17.hasNext()) {
                  String string7 = (String)var17.next();
                  CompoundTag compoundTag3 = compoundTag.getCompound(string7);
                  chunkUpdateState2.add(ChunkPos.toLong(compoundTag3.getInt("ChunkX"), compoundTag3.getInt("ChunkZ")));
               }

               chunkUpdateState2.markDirty();
            }
         }

      }
   }

   public static FeatureUpdater create(RegistryKey<World> registryKey, @Nullable PersistentStateManager persistentStateManager) {
      if (registryKey == World.OVERWORLD) {
         return new FeatureUpdater(persistentStateManager, ImmutableList.of("Monument", "Stronghold", "Village", "Mineshaft", "Temple", "Mansion"), ImmutableList.of("Village", "Mineshaft", "Mansion", "Igloo", "Desert_Pyramid", "Jungle_Pyramid", "Swamp_Hut", "Stronghold", "Monument"));
      } else {
         ImmutableList list2;
         if (registryKey == World.NETHER) {
            list2 = ImmutableList.of("Fortress");
            return new FeatureUpdater(persistentStateManager, list2, list2);
         } else if (registryKey == World.END) {
            list2 = ImmutableList.of("EndCity");
            return new FeatureUpdater(persistentStateManager, list2, list2);
         } else {
            throw new RuntimeException(String.format("Unknown dimension type : %s", registryKey));
         }
      }
   }
}
