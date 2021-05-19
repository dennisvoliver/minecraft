package net.minecraft.client.sound;

import com.google.common.collect.Lists;
import java.io.IOException;
import java.io.InputStream;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.List;
import javax.sound.sampled.AudioFormat;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.BufferUtils;
import org.lwjgl.PointerBuffer;
import org.lwjgl.stb.STBVorbis;
import org.lwjgl.stb.STBVorbisAlloc;
import org.lwjgl.stb.STBVorbisInfo;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

@Environment(EnvType.CLIENT)
public class OggAudioStream implements AudioStream {
   private long pointer;
   private final AudioFormat format;
   private final InputStream inputStream;
   private ByteBuffer buffer = MemoryUtil.memAlloc(8192);

   public OggAudioStream(InputStream inputStream) throws IOException {
      this.inputStream = inputStream;
      this.buffer.limit(0);
      MemoryStack memoryStack = MemoryStack.stackPush();
      Throwable var3 = null;

      try {
         IntBuffer intBuffer = memoryStack.mallocInt(1);
         IntBuffer intBuffer2 = memoryStack.mallocInt(1);

         while(this.pointer == 0L) {
            if (!this.readHeader()) {
               throw new IOException("Failed to find Ogg header");
            }

            int i = this.buffer.position();
            this.buffer.position(0);
            this.pointer = STBVorbis.stb_vorbis_open_pushdata(this.buffer, (IntBuffer)intBuffer, (IntBuffer)intBuffer2, (STBVorbisAlloc)null);
            this.buffer.position(i);
            int j = intBuffer2.get(0);
            if (j == 1) {
               this.increaseBufferSize();
            } else if (j != 0) {
               throw new IOException("Failed to read Ogg file " + j);
            }
         }

         this.buffer.position(this.buffer.position() + intBuffer.get(0));
         STBVorbisInfo sTBVorbisInfo = STBVorbisInfo.mallocStack(memoryStack);
         STBVorbis.stb_vorbis_get_info(this.pointer, sTBVorbisInfo);
         this.format = new AudioFormat((float)sTBVorbisInfo.sample_rate(), 16, sTBVorbisInfo.channels(), true, false);
      } catch (Throwable var15) {
         var3 = var15;
         throw var15;
      } finally {
         if (memoryStack != null) {
            if (var3 != null) {
               try {
                  memoryStack.close();
               } catch (Throwable var14) {
                  var3.addSuppressed(var14);
               }
            } else {
               memoryStack.close();
            }
         }

      }
   }

   private boolean readHeader() throws IOException {
      int i = this.buffer.limit();
      int j = this.buffer.capacity() - i;
      if (j == 0) {
         return true;
      } else {
         byte[] bs = new byte[j];
         int k = this.inputStream.read(bs);
         if (k == -1) {
            return false;
         } else {
            int l = this.buffer.position();
            this.buffer.limit(i + k);
            this.buffer.position(i);
            this.buffer.put(bs, 0, k);
            this.buffer.position(l);
            return true;
         }
      }
   }

   private void increaseBufferSize() {
      boolean bl = this.buffer.position() == 0;
      boolean bl2 = this.buffer.position() == this.buffer.limit();
      if (bl2 && !bl) {
         this.buffer.position(0);
         this.buffer.limit(0);
      } else {
         ByteBuffer byteBuffer = MemoryUtil.memAlloc(bl ? 2 * this.buffer.capacity() : this.buffer.capacity());
         byteBuffer.put(this.buffer);
         MemoryUtil.memFree((Buffer)this.buffer);
         byteBuffer.flip();
         this.buffer = byteBuffer;
      }

   }

   private boolean readOggFile(OggAudioStream.ChannelList channelList) throws IOException {
      if (this.pointer == 0L) {
         return false;
      } else {
         MemoryStack memoryStack = MemoryStack.stackPush();
         Throwable var3 = null;

         try {
            PointerBuffer pointerBuffer = memoryStack.mallocPointer(1);
            IntBuffer intBuffer = memoryStack.mallocInt(1);
            IntBuffer intBuffer2 = memoryStack.mallocInt(1);

            while(true) {
               int i = STBVorbis.stb_vorbis_decode_frame_pushdata(this.pointer, this.buffer, intBuffer, pointerBuffer, intBuffer2);
               this.buffer.position(this.buffer.position() + i);
               int j = STBVorbis.stb_vorbis_get_error(this.pointer);
               if (j == 1) {
                  this.increaseBufferSize();
                  if (!this.readHeader()) {
                     boolean var25 = false;
                     return var25;
                  }
               } else {
                  if (j != 0) {
                     throw new IOException("Failed to read Ogg file " + j);
                  }

                  int k = intBuffer2.get(0);
                  if (k != 0) {
                     int l = intBuffer.get(0);
                     PointerBuffer pointerBuffer2 = pointerBuffer.getPointerBuffer(l);
                     boolean var12;
                     if (l != 1) {
                        if (l == 2) {
                           this.readChannels(pointerBuffer2.getFloatBuffer(0, k), pointerBuffer2.getFloatBuffer(1, k), channelList);
                           var12 = true;
                           return var12;
                        }

                        throw new IllegalStateException("Invalid number of channels: " + l);
                     }

                     this.readChannels(pointerBuffer2.getFloatBuffer(0, k), channelList);
                     var12 = true;
                     return var12;
                  }
               }
            }
         } catch (Throwable var23) {
            var3 = var23;
            throw var23;
         } finally {
            if (memoryStack != null) {
               if (var3 != null) {
                  try {
                     memoryStack.close();
                  } catch (Throwable var22) {
                     var3.addSuppressed(var22);
                  }
               } else {
                  memoryStack.close();
               }
            }

         }
      }
   }

   private void readChannels(FloatBuffer floatBuffer, OggAudioStream.ChannelList channelList) {
      while(floatBuffer.hasRemaining()) {
         channelList.addChannel(floatBuffer.get());
      }

   }

   private void readChannels(FloatBuffer floatBuffer, FloatBuffer floatBuffer2, OggAudioStream.ChannelList channelList) {
      while(floatBuffer.hasRemaining() && floatBuffer2.hasRemaining()) {
         channelList.addChannel(floatBuffer.get());
         channelList.addChannel(floatBuffer2.get());
      }

   }

   public void close() throws IOException {
      if (this.pointer != 0L) {
         STBVorbis.stb_vorbis_close(this.pointer);
         this.pointer = 0L;
      }

      MemoryUtil.memFree((Buffer)this.buffer);
      this.inputStream.close();
   }

   public AudioFormat getFormat() {
      return this.format;
   }

   public ByteBuffer getBuffer(int size) throws IOException {
      OggAudioStream.ChannelList channelList = new OggAudioStream.ChannelList(size + 8192);

      while(this.readOggFile(channelList) && channelList.currentBufferSize < size) {
      }

      return channelList.getBuffer();
   }

   public ByteBuffer getBuffer() throws IOException {
      OggAudioStream.ChannelList channelList = new OggAudioStream.ChannelList(16384);

      while(this.readOggFile(channelList)) {
      }

      return channelList.getBuffer();
   }

   @Environment(EnvType.CLIENT)
   static class ChannelList {
      private final List<ByteBuffer> buffers = Lists.newArrayList();
      private final int size;
      private int currentBufferSize;
      private ByteBuffer buffer;

      public ChannelList(int size) {
         this.size = size + 1 & -2;
         this.init();
      }

      private void init() {
         this.buffer = BufferUtils.createByteBuffer(this.size);
      }

      public void addChannel(float f) {
         if (this.buffer.remaining() == 0) {
            this.buffer.flip();
            this.buffers.add(this.buffer);
            this.init();
         }

         int i = MathHelper.clamp((int)(f * 32767.5F - 0.5F), -32768, 32767);
         this.buffer.putShort((short)i);
         this.currentBufferSize += 2;
      }

      public ByteBuffer getBuffer() {
         this.buffer.flip();
         if (this.buffers.isEmpty()) {
            return this.buffer;
         } else {
            ByteBuffer byteBuffer = BufferUtils.createByteBuffer(this.currentBufferSize);
            this.buffers.forEach(byteBuffer::put);
            byteBuffer.put(this.buffer);
            byteBuffer.flip();
            return byteBuffer;
         }
      }
   }
}
