package net.minecraft.world.storage;

import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.util.ThrowableDeliverer;
import net.minecraft.util.math.ChunkPos;
import org.jetbrains.annotations.Nullable;

public final class RegionBasedStorage implements AutoCloseable {
   private final Long2ObjectLinkedOpenHashMap<RegionFile> cachedRegionFiles = new Long2ObjectLinkedOpenHashMap();
   private final File directory;
   private final boolean dsync;

   RegionBasedStorage(File directory, boolean dsync) {
      this.directory = directory;
      this.dsync = dsync;
   }

   private RegionFile getRegionFile(ChunkPos pos) throws IOException {
      long l = ChunkPos.toLong(pos.getRegionX(), pos.getRegionZ());
      RegionFile regionFile = (RegionFile)this.cachedRegionFiles.getAndMoveToFirst(l);
      if (regionFile != null) {
         return regionFile;
      } else {
         if (this.cachedRegionFiles.size() >= 256) {
            ((RegionFile)this.cachedRegionFiles.removeLast()).close();
         }

         if (!this.directory.exists()) {
            this.directory.mkdirs();
         }

         File file = new File(this.directory, "r." + pos.getRegionX() + "." + pos.getRegionZ() + ".mca");
         RegionFile regionFile2 = new RegionFile(file, this.directory, this.dsync);
         this.cachedRegionFiles.putAndMoveToFirst(l, regionFile2);
         return regionFile2;
      }
   }

   @Nullable
   public CompoundTag getTagAt(ChunkPos pos) throws IOException {
      RegionFile regionFile = this.getRegionFile(pos);
      DataInputStream dataInputStream = regionFile.getChunkInputStream(pos);
      Throwable var4 = null;

      CompoundTag var5;
      try {
         if (dataInputStream != null) {
            var5 = NbtIo.read((DataInput)dataInputStream);
            return var5;
         }

         var5 = null;
      } catch (Throwable var15) {
         var4 = var15;
         throw var15;
      } finally {
         if (dataInputStream != null) {
            if (var4 != null) {
               try {
                  dataInputStream.close();
               } catch (Throwable var14) {
                  var4.addSuppressed(var14);
               }
            } else {
               dataInputStream.close();
            }
         }

      }

      return var5;
   }

   protected void write(ChunkPos pos, CompoundTag tag) throws IOException {
      RegionFile regionFile = this.getRegionFile(pos);
      DataOutputStream dataOutputStream = regionFile.getChunkOutputStream(pos);
      Throwable var5 = null;

      try {
         NbtIo.write((CompoundTag)tag, (DataOutput)dataOutputStream);
      } catch (Throwable var14) {
         var5 = var14;
         throw var14;
      } finally {
         if (dataOutputStream != null) {
            if (var5 != null) {
               try {
                  dataOutputStream.close();
               } catch (Throwable var13) {
                  var5.addSuppressed(var13);
               }
            } else {
               dataOutputStream.close();
            }
         }

      }

   }

   public void close() throws IOException {
      ThrowableDeliverer<IOException> throwableDeliverer = new ThrowableDeliverer();
      ObjectIterator var2 = this.cachedRegionFiles.values().iterator();

      while(var2.hasNext()) {
         RegionFile regionFile = (RegionFile)var2.next();

         try {
            regionFile.close();
         } catch (IOException var5) {
            throwableDeliverer.add(var5);
         }
      }

      throwableDeliverer.deliver();
   }

   public void method_26982() throws IOException {
      ObjectIterator var1 = this.cachedRegionFiles.values().iterator();

      while(var1.hasNext()) {
         RegionFile regionFile = (RegionFile)var1.next();
         regionFile.method_26981();
      }

   }
}
