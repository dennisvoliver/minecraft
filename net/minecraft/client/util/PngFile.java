package net.minecraft.client.util;

import java.io.EOFException;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SeekableByteChannel;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.lwjgl.stb.STBIEOFCallback;
import org.lwjgl.stb.STBIIOCallbacks;
import org.lwjgl.stb.STBIReadCallback;
import org.lwjgl.stb.STBISkipCallback;
import org.lwjgl.stb.STBImage;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

@Environment(EnvType.CLIENT)
public class PngFile {
   public final int width;
   public final int height;

   public PngFile(String name, InputStream in) throws IOException {
      MemoryStack memoryStack = MemoryStack.stackPush();
      Throwable var4 = null;

      try {
         PngFile.Reader reader = createReader(in);
         Throwable var6 = null;

         try {
            reader.getClass();
            STBIReadCallback sTBIReadCallback = STBIReadCallback.create(reader::read);
            Throwable var8 = null;

            try {
               reader.getClass();
               STBISkipCallback sTBISkipCallback = STBISkipCallback.create(reader::skip);
               Throwable var10 = null;

               try {
                  reader.getClass();
                  STBIEOFCallback sTBIEOFCallback = STBIEOFCallback.create(reader::eof);
                  Throwable var12 = null;

                  try {
                     STBIIOCallbacks sTBIIOCallbacks = STBIIOCallbacks.mallocStack(memoryStack);
                     sTBIIOCallbacks.read(sTBIReadCallback);
                     sTBIIOCallbacks.skip(sTBISkipCallback);
                     sTBIIOCallbacks.eof(sTBIEOFCallback);
                     IntBuffer intBuffer = memoryStack.mallocInt(1);
                     IntBuffer intBuffer2 = memoryStack.mallocInt(1);
                     IntBuffer intBuffer3 = memoryStack.mallocInt(1);
                     if (!STBImage.stbi_info_from_callbacks(sTBIIOCallbacks, 0L, intBuffer, intBuffer2, intBuffer3)) {
                        throw new IOException("Could not read info from the PNG file " + name + " " + STBImage.stbi_failure_reason());
                     }

                     this.width = intBuffer.get(0);
                     this.height = intBuffer2.get(0);
                  } catch (Throwable var122) {
                     var12 = var122;
                     throw var122;
                  } finally {
                     if (sTBIEOFCallback != null) {
                        if (var12 != null) {
                           try {
                              sTBIEOFCallback.close();
                           } catch (Throwable var121) {
                              var12.addSuppressed(var121);
                           }
                        } else {
                           sTBIEOFCallback.close();
                        }
                     }

                  }
               } catch (Throwable var124) {
                  var10 = var124;
                  throw var124;
               } finally {
                  if (sTBISkipCallback != null) {
                     if (var10 != null) {
                        try {
                           sTBISkipCallback.close();
                        } catch (Throwable var120) {
                           var10.addSuppressed(var120);
                        }
                     } else {
                        sTBISkipCallback.close();
                     }
                  }

               }
            } catch (Throwable var126) {
               var8 = var126;
               throw var126;
            } finally {
               if (sTBIReadCallback != null) {
                  if (var8 != null) {
                     try {
                        sTBIReadCallback.close();
                     } catch (Throwable var119) {
                        var8.addSuppressed(var119);
                     }
                  } else {
                     sTBIReadCallback.close();
                  }
               }

            }
         } catch (Throwable var128) {
            var6 = var128;
            throw var128;
         } finally {
            if (reader != null) {
               if (var6 != null) {
                  try {
                     reader.close();
                  } catch (Throwable var118) {
                     var6.addSuppressed(var118);
                  }
               } else {
                  reader.close();
               }
            }

         }
      } catch (Throwable var130) {
         var4 = var130;
         throw var130;
      } finally {
         if (memoryStack != null) {
            if (var4 != null) {
               try {
                  memoryStack.close();
               } catch (Throwable var117) {
                  var4.addSuppressed(var117);
               }
            } else {
               memoryStack.close();
            }
         }

      }

   }

   private static PngFile.Reader createReader(InputStream is) {
      return (PngFile.Reader)(is instanceof FileInputStream ? new PngFile.SeekableChannelReader(((FileInputStream)is).getChannel()) : new PngFile.ChannelReader(Channels.newChannel(is)));
   }

   @Environment(EnvType.CLIENT)
   static class ChannelReader extends PngFile.Reader {
      private final ReadableByteChannel channel;
      private long buffer;
      private int bufferSize;
      private int bufferPosition;
      private int readPosition;

      private ChannelReader(ReadableByteChannel readableByteChannel) {
         super(null);
         this.buffer = MemoryUtil.nmemAlloc(128L);
         this.bufferSize = 128;
         this.channel = readableByteChannel;
      }

      private void readToBuffer(int size) throws IOException {
         ByteBuffer byteBuffer = MemoryUtil.memByteBuffer(this.buffer, this.bufferSize);
         if (size + this.readPosition > this.bufferSize) {
            this.bufferSize = size + this.readPosition;
            byteBuffer = MemoryUtil.memRealloc(byteBuffer, this.bufferSize);
            this.buffer = MemoryUtil.memAddress(byteBuffer);
         }

         byteBuffer.position(this.bufferPosition);

         while(size + this.readPosition > this.bufferPosition) {
            try {
               int i = this.channel.read(byteBuffer);
               if (i == -1) {
                  break;
               }
            } finally {
               this.bufferPosition = byteBuffer.position();
            }
         }

      }

      public int read(long data, int size) throws IOException {
         this.readToBuffer(size);
         if (size + this.readPosition > this.bufferPosition) {
            size = this.bufferPosition - this.readPosition;
         }

         MemoryUtil.memCopy(this.buffer + (long)this.readPosition, data, (long)size);
         this.readPosition += size;
         return size;
      }

      public void skip(int n) throws IOException {
         if (n > 0) {
            this.readToBuffer(n);
            if (n + this.readPosition > this.bufferPosition) {
               throw new EOFException("Can't skip past the EOF.");
            }
         }

         if (this.readPosition + n < 0) {
            throw new IOException("Can't seek before the beginning: " + (this.readPosition + n));
         } else {
            this.readPosition += n;
         }
      }

      public void close() throws IOException {
         MemoryUtil.nmemFree(this.buffer);
         this.channel.close();
      }
   }

   @Environment(EnvType.CLIENT)
   static class SeekableChannelReader extends PngFile.Reader {
      private final SeekableByteChannel channel;

      private SeekableChannelReader(SeekableByteChannel channel) {
         super(null);
         this.channel = channel;
      }

      public int read(long data, int size) throws IOException {
         ByteBuffer byteBuffer = MemoryUtil.memByteBuffer(data, size);
         return this.channel.read(byteBuffer);
      }

      public void skip(int n) throws IOException {
         this.channel.position(this.channel.position() + (long)n);
      }

      public int eof(long user) {
         return super.eof(user) != 0 && this.channel.isOpen() ? 1 : 0;
      }

      public void close() throws IOException {
         this.channel.close();
      }
   }

   @Environment(EnvType.CLIENT)
   abstract static class Reader implements AutoCloseable {
      protected boolean errored;

      private Reader() {
      }

      int read(long user, long data, int size) {
         try {
            return this.read(data, size);
         } catch (IOException var7) {
            this.errored = true;
            return 0;
         }
      }

      void skip(long user, int n) {
         try {
            this.skip(n);
         } catch (IOException var5) {
            this.errored = true;
         }

      }

      int eof(long user) {
         return this.errored ? 1 : 0;
      }

      protected abstract int read(long data, int size) throws IOException;

      protected abstract void skip(int n) throws IOException;

      public abstract void close() throws IOException;
   }
}
