package net.minecraft.world;

import com.google.common.collect.Maps;
import com.mojang.datafixers.DataFixer;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;
import java.util.Iterator;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.SharedConstants;
import net.minecraft.datafixer.DataFixTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.nbt.NbtIo;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

public class PersistentStateManager {
   private static final Logger LOGGER = LogManager.getLogger();
   private final Map<String, PersistentState> loadedStates = Maps.newHashMap();
   private final DataFixer dataFixer;
   private final File directory;

   public PersistentStateManager(File directory, DataFixer dataFixer) {
      this.dataFixer = dataFixer;
      this.directory = directory;
   }

   private File getFile(String id) {
      return new File(this.directory, id + ".dat");
   }

   public <T extends PersistentState> T getOrCreate(Supplier<T> factory, String id) {
      T persistentState = this.get(factory, id);
      if (persistentState != null) {
         return persistentState;
      } else {
         T persistentState2 = (PersistentState)factory.get();
         this.set(persistentState2);
         return persistentState2;
      }
   }

   @Nullable
   public <T extends PersistentState> T get(Supplier<T> factory, String id) {
      PersistentState persistentState = (PersistentState)this.loadedStates.get(id);
      if (persistentState == null && !this.loadedStates.containsKey(id)) {
         persistentState = this.readFromFile(factory, id);
         this.loadedStates.put(id, persistentState);
      }

      return persistentState;
   }

   @Nullable
   private <T extends PersistentState> T readFromFile(Supplier<T> factory, String id) {
      try {
         File file = this.getFile(id);
         if (file.exists()) {
            T persistentState = (PersistentState)factory.get();
            CompoundTag compoundTag = this.readTag(id, SharedConstants.getGameVersion().getWorldVersion());
            persistentState.fromTag(compoundTag.getCompound("data"));
            return persistentState;
         }
      } catch (Exception var6) {
         LOGGER.error((String)"Error loading saved data: {}", (Object)id, (Object)var6);
      }

      return null;
   }

   public void set(PersistentState state) {
      this.loadedStates.put(state.getId(), state);
   }

   public CompoundTag readTag(String id, int dataVersion) throws IOException {
      File file = this.getFile(id);
      FileInputStream fileInputStream = new FileInputStream(file);
      Throwable var5 = null;

      Object var10;
      try {
         PushbackInputStream pushbackInputStream = new PushbackInputStream(fileInputStream, 2);
         Throwable var7 = null;

         try {
            CompoundTag compoundTag3;
            if (this.isCompressed(pushbackInputStream)) {
               compoundTag3 = NbtIo.readCompressed((InputStream)pushbackInputStream);
            } else {
               DataInputStream dataInputStream = new DataInputStream(pushbackInputStream);
               var10 = null;

               try {
                  compoundTag3 = NbtIo.read((DataInput)dataInputStream);
               } catch (Throwable var54) {
                  var10 = var54;
                  throw var54;
               } finally {
                  if (dataInputStream != null) {
                     if (var10 != null) {
                        try {
                           dataInputStream.close();
                        } catch (Throwable var53) {
                           ((Throwable)var10).addSuppressed(var53);
                        }
                     } else {
                        dataInputStream.close();
                     }
                  }

               }
            }

            int i = compoundTag3.contains("DataVersion", 99) ? compoundTag3.getInt("DataVersion") : 1343;
            var10 = NbtHelper.update(this.dataFixer, DataFixTypes.SAVED_DATA, compoundTag3, i, dataVersion);
         } catch (Throwable var56) {
            var7 = var56;
            throw var56;
         } finally {
            if (pushbackInputStream != null) {
               if (var7 != null) {
                  try {
                     pushbackInputStream.close();
                  } catch (Throwable var52) {
                     var7.addSuppressed(var52);
                  }
               } else {
                  pushbackInputStream.close();
               }
            }

         }
      } catch (Throwable var58) {
         var5 = var58;
         throw var58;
      } finally {
         if (fileInputStream != null) {
            if (var5 != null) {
               try {
                  fileInputStream.close();
               } catch (Throwable var51) {
                  var5.addSuppressed(var51);
               }
            } else {
               fileInputStream.close();
            }
         }

      }

      return (CompoundTag)var10;
   }

   private boolean isCompressed(PushbackInputStream pushbackInputStream) throws IOException {
      byte[] bs = new byte[2];
      boolean bl = false;
      int i = pushbackInputStream.read(bs, 0, 2);
      if (i == 2) {
         int j = (bs[1] & 255) << 8 | bs[0] & 255;
         if (j == 35615) {
            bl = true;
         }
      }

      if (i != 0) {
         pushbackInputStream.unread(bs, 0, i);
      }

      return bl;
   }

   public void save() {
      Iterator var1 = this.loadedStates.values().iterator();

      while(var1.hasNext()) {
         PersistentState persistentState = (PersistentState)var1.next();
         if (persistentState != null) {
            persistentState.save(this.getFile(persistentState.getId()));
         }
      }

   }
}
