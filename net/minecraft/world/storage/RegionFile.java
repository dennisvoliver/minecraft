package net.minecraft.world.storage;

import com.google.common.annotations.VisibleForTesting;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import net.minecraft.util.Util;
import net.minecraft.util.math.ChunkPos;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

public class RegionFile implements AutoCloseable {
   private static final Logger LOGGER = LogManager.getLogger();
   private static final ByteBuffer ZERO = ByteBuffer.allocateDirect(1);
   private final FileChannel channel;
   private final Path directory;
   private final ChunkStreamVersion outputChunkStreamVersion;
   private final ByteBuffer header;
   private final IntBuffer sectorData;
   private final IntBuffer saveTimes;
   @VisibleForTesting
   protected final SectorMap sectors;

   public RegionFile(File file, File directory, boolean dsync) throws IOException {
      this(file.toPath(), directory.toPath(), ChunkStreamVersion.DEFLATE, dsync);
   }

   public RegionFile(Path file, Path directory, ChunkStreamVersion outputChunkStreamVersion, boolean dsync) throws IOException {
      this.header = ByteBuffer.allocateDirect(8192);
      this.sectors = new SectorMap();
      this.outputChunkStreamVersion = outputChunkStreamVersion;
      if (!Files.isDirectory(directory, new LinkOption[0])) {
         throw new IllegalArgumentException("Expected directory, got " + directory.toAbsolutePath());
      } else {
         this.directory = directory;
         this.sectorData = this.header.asIntBuffer();
         this.sectorData.limit(1024);
         this.header.position(4096);
         this.saveTimes = this.header.asIntBuffer();
         if (dsync) {
            this.channel = FileChannel.open(file, StandardOpenOption.CREATE, StandardOpenOption.READ, StandardOpenOption.WRITE, StandardOpenOption.DSYNC);
         } else {
            this.channel = FileChannel.open(file, StandardOpenOption.CREATE, StandardOpenOption.READ, StandardOpenOption.WRITE);
         }

         this.sectors.allocate(0, 2);
         this.header.position(0);
         int i = this.channel.read(this.header, 0L);
         if (i != -1) {
            if (i != 8192) {
               LOGGER.warn((String)"Region file {} has truncated header: {}", (Object)file, (Object)i);
            }

            long l = Files.size(file);

            for(int j = 0; j < 1024; ++j) {
               int k = this.sectorData.get(j);
               if (k != 0) {
                  int m = getOffset(k);
                  int n = getSize(k);
                  if (m < 2) {
                     LOGGER.warn((String)"Region file {} has invalid sector at index: {}; sector {} overlaps with header", (Object)file, j, m);
                     this.sectorData.put(j, 0);
                  } else if (n == 0) {
                     LOGGER.warn((String)"Region file {} has an invalid sector at index: {}; size has to be > 0", (Object)file, (Object)j);
                     this.sectorData.put(j, 0);
                  } else if ((long)m * 4096L > l) {
                     LOGGER.warn((String)"Region file {} has an invalid sector at index: {}; sector {} is out of bounds", (Object)file, j, m);
                     this.sectorData.put(j, 0);
                  } else {
                     this.sectors.allocate(m, n);
                  }
               }
            }
         }

      }
   }

   private Path getExternalChunkPath(ChunkPos chunkPos) {
      String string = "c." + chunkPos.x + "." + chunkPos.z + ".mcc";
      return this.directory.resolve(string);
   }

   @Nullable
   public synchronized DataInputStream getChunkInputStream(ChunkPos pos) throws IOException {
      int i = this.getSectorData(pos);
      if (i == 0) {
         return null;
      } else {
         int j = getOffset(i);
         int k = getSize(i);
         int l = k * 4096;
         ByteBuffer byteBuffer = ByteBuffer.allocate(l);
         this.channel.read(byteBuffer, (long)(j * 4096));
         byteBuffer.flip();
         if (byteBuffer.remaining() < 5) {
            LOGGER.error((String)"Chunk {} header is truncated: expected {} but read {}", (Object)pos, l, byteBuffer.remaining());
            return null;
         } else {
            int m = byteBuffer.getInt();
            byte b = byteBuffer.get();
            if (m == 0) {
               LOGGER.warn((String)"Chunk {} is allocated, but stream is missing", (Object)pos);
               return null;
            } else {
               int n = m - 1;
               if (hasChunkStreamVersionId(b)) {
                  if (n != 0) {
                     LOGGER.warn("Chunk has both internal and external streams");
                  }

                  return this.method_22408(pos, getChunkStreamVersionId(b));
               } else if (n > byteBuffer.remaining()) {
                  LOGGER.error((String)"Chunk {} stream is truncated: expected {} but read {}", (Object)pos, n, byteBuffer.remaining());
                  return null;
               } else if (n < 0) {
                  LOGGER.error((String)"Declared size {} of chunk {} is negative", (Object)m, (Object)pos);
                  return null;
               } else {
                  return this.method_22409(pos, b, getInputStream(byteBuffer, n));
               }
            }
         }
      }
   }

   private static boolean hasChunkStreamVersionId(byte b) {
      return (b & 128) != 0;
   }

   private static byte getChunkStreamVersionId(byte b) {
      return (byte)(b & -129);
   }

   @Nullable
   private DataInputStream method_22409(ChunkPos chunkPos, byte b, InputStream inputStream) throws IOException {
      ChunkStreamVersion chunkStreamVersion = ChunkStreamVersion.get(b);
      if (chunkStreamVersion == null) {
         LOGGER.error((String)"Chunk {} has invalid chunk stream version {}", (Object)chunkPos, (Object)b);
         return null;
      } else {
         return new DataInputStream(new BufferedInputStream(chunkStreamVersion.wrap(inputStream)));
      }
   }

   @Nullable
   private DataInputStream method_22408(ChunkPos chunkPos, byte b) throws IOException {
      Path path = this.getExternalChunkPath(chunkPos);
      if (!Files.isRegularFile(path, new LinkOption[0])) {
         LOGGER.error((String)"External chunk path {} is not file", (Object)path);
         return null;
      } else {
         return this.method_22409(chunkPos, b, Files.newInputStream(path));
      }
   }

   private static ByteArrayInputStream getInputStream(ByteBuffer buffer, int length) {
      return new ByteArrayInputStream(buffer.array(), buffer.position(), length);
   }

   private int packSectorData(int offset, int size) {
      return offset << 8 | size;
   }

   private static int getSize(int sectorData) {
      return sectorData & 255;
   }

   private static int getOffset(int sectorData) {
      return sectorData >> 8 & 16777215;
   }

   private static int getSectorCount(int byteCount) {
      return (byteCount + 4096 - 1) / 4096;
   }

   public boolean isChunkValid(ChunkPos pos) {
      int i = this.getSectorData(pos);
      if (i == 0) {
         return false;
      } else {
         int j = getOffset(i);
         int k = getSize(i);
         ByteBuffer byteBuffer = ByteBuffer.allocate(5);

         try {
            this.channel.read(byteBuffer, (long)(j * 4096));
            byteBuffer.flip();
            if (byteBuffer.remaining() != 5) {
               return false;
            } else {
               int l = byteBuffer.getInt();
               byte b = byteBuffer.get();
               if (hasChunkStreamVersionId(b)) {
                  if (!ChunkStreamVersion.exists(getChunkStreamVersionId(b))) {
                     return false;
                  }

                  if (!Files.isRegularFile(this.getExternalChunkPath(pos), new LinkOption[0])) {
                     return false;
                  }
               } else {
                  if (!ChunkStreamVersion.exists(b)) {
                     return false;
                  }

                  if (l == 0) {
                     return false;
                  }

                  int m = l - 1;
                  if (m < 0 || m > 4096 * k) {
                     return false;
                  }
               }

               return true;
            }
         } catch (IOException var9) {
            return false;
         }
      }
   }

   public DataOutputStream getChunkOutputStream(ChunkPos pos) throws IOException {
      return new DataOutputStream(new BufferedOutputStream(this.outputChunkStreamVersion.wrap((OutputStream)(new RegionFile.ChunkBuffer(pos)))));
   }

   public void method_26981() throws IOException {
      this.channel.force(true);
   }

   protected synchronized void writeChunk(ChunkPos pos, ByteBuffer byteBuffer) throws IOException {
      int i = getIndex(pos);
      int j = this.sectorData.get(i);
      int k = getOffset(j);
      int l = getSize(j);
      int m = byteBuffer.remaining();
      int n = getSectorCount(m);
      int p;
      RegionFile.OutputAction outputAction2;
      if (n >= 256) {
         Path path = this.getExternalChunkPath(pos);
         LOGGER.warn((String)"Saving oversized chunk {} ({} bytes} to external file {}", (Object)pos, m, path);
         n = 1;
         p = this.sectors.allocate(n);
         outputAction2 = this.writeSafely(path, byteBuffer);
         ByteBuffer byteBuffer2 = this.method_22406();
         this.channel.write(byteBuffer2, (long)(p * 4096));
      } else {
         p = this.sectors.allocate(n);
         outputAction2 = () -> {
            Files.deleteIfExists(this.getExternalChunkPath(pos));
         };
         this.channel.write(byteBuffer, (long)(p * 4096));
      }

      int q = (int)(Util.getEpochTimeMs() / 1000L);
      this.sectorData.put(i, this.packSectorData(p, n));
      this.saveTimes.put(i, q);
      this.writeHeader();
      outputAction2.run();
      if (k != 0) {
         this.sectors.free(k, l);
      }

   }

   private ByteBuffer method_22406() {
      ByteBuffer byteBuffer = ByteBuffer.allocate(5);
      byteBuffer.putInt(1);
      byteBuffer.put((byte)(this.outputChunkStreamVersion.getId() | 128));
      byteBuffer.flip();
      return byteBuffer;
   }

   private RegionFile.OutputAction writeSafely(Path path, ByteBuffer byteBuffer) throws IOException {
      Path path2 = Files.createTempFile(this.directory, "tmp", (String)null);
      FileChannel fileChannel = FileChannel.open(path2, StandardOpenOption.CREATE, StandardOpenOption.WRITE);
      Throwable var5 = null;

      try {
         byteBuffer.position(5);
         fileChannel.write(byteBuffer);
      } catch (Throwable var14) {
         var5 = var14;
         throw var14;
      } finally {
         if (fileChannel != null) {
            if (var5 != null) {
               try {
                  fileChannel.close();
               } catch (Throwable var13) {
                  var5.addSuppressed(var13);
               }
            } else {
               fileChannel.close();
            }
         }

      }

      return () -> {
         Files.move(path2, path, StandardCopyOption.REPLACE_EXISTING);
      };
   }

   private void writeHeader() throws IOException {
      this.header.position(0);
      this.channel.write(this.header, 0L);
   }

   private int getSectorData(ChunkPos pos) {
      return this.sectorData.get(getIndex(pos));
   }

   public boolean hasChunk(ChunkPos pos) {
      return this.getSectorData(pos) != 0;
   }

   private static int getIndex(ChunkPos pos) {
      return pos.getRegionRelativeX() + pos.getRegionRelativeZ() * 32;
   }

   public void close() throws IOException {
      try {
         this.fillLastSector();
      } finally {
         try {
            this.channel.force(true);
         } finally {
            this.channel.close();
         }
      }

   }

   private void fillLastSector() throws IOException {
      int i = (int)this.channel.size();
      int j = getSectorCount(i) * 4096;
      if (i != j) {
         ByteBuffer byteBuffer = ZERO.duplicate();
         byteBuffer.position(0);
         this.channel.write(byteBuffer, (long)(j - 1));
      }

   }

   interface OutputAction {
      void run() throws IOException;
   }

   class ChunkBuffer extends ByteArrayOutputStream {
      private final ChunkPos pos;

      public ChunkBuffer(ChunkPos chunkPos) {
         super(8096);
         super.write(0);
         super.write(0);
         super.write(0);
         super.write(0);
         super.write(RegionFile.this.outputChunkStreamVersion.getId());
         this.pos = chunkPos;
      }

      public void close() throws IOException {
         ByteBuffer byteBuffer = ByteBuffer.wrap(this.buf, 0, this.count);
         byteBuffer.putInt(0, this.count - 5 + 1);
         RegionFile.this.writeChunk(this.pos, byteBuffer);
      }
   }
}
