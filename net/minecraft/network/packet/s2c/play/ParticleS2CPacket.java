package net.minecraft.network.packet.s2c.play;

import java.io.IOException;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleType;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.registry.Registry;

public class ParticleS2CPacket implements Packet<ClientPlayPacketListener> {
   private double x;
   private double y;
   private double z;
   private float offsetX;
   private float offsetY;
   private float offsetZ;
   private float speed;
   private int count;
   private boolean longDistance;
   private ParticleEffect parameters;

   public ParticleS2CPacket() {
   }

   public <T extends ParticleEffect> ParticleS2CPacket(T parameters, boolean longDistance, double x, double y, double z, float offsetX, float offsetY, float offsetZ, float speed, int count) {
      this.parameters = parameters;
      this.longDistance = longDistance;
      this.x = x;
      this.y = y;
      this.z = z;
      this.offsetX = offsetX;
      this.offsetY = offsetY;
      this.offsetZ = offsetZ;
      this.speed = speed;
      this.count = count;
   }

   public void read(PacketByteBuf buf) throws IOException {
      ParticleType<?> particleType = (ParticleType)Registry.PARTICLE_TYPE.get(buf.readInt());
      if (particleType == null) {
         particleType = ParticleTypes.BARRIER;
      }

      this.longDistance = buf.readBoolean();
      this.x = buf.readDouble();
      this.y = buf.readDouble();
      this.z = buf.readDouble();
      this.offsetX = buf.readFloat();
      this.offsetY = buf.readFloat();
      this.offsetZ = buf.readFloat();
      this.speed = buf.readFloat();
      this.count = buf.readInt();
      this.parameters = this.readParticleParameters(buf, (ParticleType)particleType);
   }

   private <T extends ParticleEffect> T readParticleParameters(PacketByteBuf buf, ParticleType<T> type) {
      return type.getParametersFactory().read(type, buf);
   }

   public void write(PacketByteBuf buf) throws IOException {
      buf.writeInt(Registry.PARTICLE_TYPE.getRawId(this.parameters.getType()));
      buf.writeBoolean(this.longDistance);
      buf.writeDouble(this.x);
      buf.writeDouble(this.y);
      buf.writeDouble(this.z);
      buf.writeFloat(this.offsetX);
      buf.writeFloat(this.offsetY);
      buf.writeFloat(this.offsetZ);
      buf.writeFloat(this.speed);
      buf.writeInt(this.count);
      this.parameters.write(buf);
   }

   @Environment(EnvType.CLIENT)
   public boolean isLongDistance() {
      return this.longDistance;
   }

   @Environment(EnvType.CLIENT)
   public double getX() {
      return this.x;
   }

   @Environment(EnvType.CLIENT)
   public double getY() {
      return this.y;
   }

   @Environment(EnvType.CLIENT)
   public double getZ() {
      return this.z;
   }

   @Environment(EnvType.CLIENT)
   public float getOffsetX() {
      return this.offsetX;
   }

   @Environment(EnvType.CLIENT)
   public float getOffsetY() {
      return this.offsetY;
   }

   @Environment(EnvType.CLIENT)
   public float getOffsetZ() {
      return this.offsetZ;
   }

   @Environment(EnvType.CLIENT)
   public float getSpeed() {
      return this.speed;
   }

   @Environment(EnvType.CLIENT)
   public int getCount() {
      return this.count;
   }

   @Environment(EnvType.CLIENT)
   public ParticleEffect getParameters() {
      return this.parameters;
   }

   public void apply(ClientPlayPacketListener clientPlayPacketListener) {
      clientPlayPacketListener.onParticle(this);
   }
}
