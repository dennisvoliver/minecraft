package net.minecraft.command;

import com.google.common.collect.Maps;
import java.util.Iterator;
import java.util.Map;
import java.util.stream.Stream;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Identifier;
import net.minecraft.world.PersistentStateManager;

public class DataCommandStorage {
   private final Map<String, DataCommandStorage.PersistentState> storages = Maps.newHashMap();
   private final PersistentStateManager stateManager;

   public DataCommandStorage(PersistentStateManager stateManager) {
      this.stateManager = stateManager;
   }

   private DataCommandStorage.PersistentState createStorage(String namespace, String saveKey) {
      DataCommandStorage.PersistentState persistentState = new DataCommandStorage.PersistentState(saveKey);
      this.storages.put(namespace, persistentState);
      return persistentState;
   }

   public CompoundTag get(Identifier id) {
      String string = id.getNamespace();
      String string2 = getSaveKey(string);
      DataCommandStorage.PersistentState persistentState = (DataCommandStorage.PersistentState)this.stateManager.get(() -> {
         return this.createStorage(string, string2);
      }, string2);
      return persistentState != null ? persistentState.get(id.getPath()) : new CompoundTag();
   }

   public void set(Identifier id, CompoundTag tag) {
      String string = id.getNamespace();
      String string2 = getSaveKey(string);
      ((DataCommandStorage.PersistentState)this.stateManager.getOrCreate(() -> {
         return this.createStorage(string, string2);
      }, string2)).set(id.getPath(), tag);
   }

   public Stream<Identifier> getIds() {
      return this.storages.entrySet().stream().flatMap((entry) -> {
         return ((DataCommandStorage.PersistentState)entry.getValue()).getIds((String)entry.getKey());
      });
   }

   private static String getSaveKey(String namespace) {
      return "command_storage_" + namespace;
   }

   static class PersistentState extends net.minecraft.world.PersistentState {
      private final Map<String, CompoundTag> map = Maps.newHashMap();

      public PersistentState(String string) {
         super(string);
      }

      public void fromTag(CompoundTag tag) {
         CompoundTag compoundTag = tag.getCompound("contents");
         Iterator var3 = compoundTag.getKeys().iterator();

         while(var3.hasNext()) {
            String string = (String)var3.next();
            this.map.put(string, compoundTag.getCompound(string));
         }

      }

      public CompoundTag toTag(CompoundTag tag) {
         CompoundTag compoundTag = new CompoundTag();
         this.map.forEach((string, compoundTag2) -> {
            compoundTag.put(string, compoundTag2.copy());
         });
         tag.put("contents", compoundTag);
         return tag;
      }

      public CompoundTag get(String name) {
         CompoundTag compoundTag = (CompoundTag)this.map.get(name);
         return compoundTag != null ? compoundTag : new CompoundTag();
      }

      public void set(String name, CompoundTag tag) {
         if (tag.isEmpty()) {
            this.map.remove(name);
         } else {
            this.map.put(name, tag);
         }

         this.markDirty();
      }

      public Stream<Identifier> getIds(String namespace) {
         return this.map.keySet().stream().map((string2) -> {
            return new Identifier(namespace, string2);
         });
      }
   }
}
