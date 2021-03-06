package net.minecraft.network.packet.s2c.play;

import java.io.IOException;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.Entity;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.listener.ClientPlayPacketListener;

public class VehicleMoveS2CPacket implements Packet<ClientPlayPacketListener> {
   private double x;
   private double y;
   private double z;
   private float yaw;
   private float pitch;

   public VehicleMoveS2CPacket() {
   }

   public VehicleMoveS2CPacket(Entity entity) {
      this.x = entity.getX();
      this.y = entity.getY();
      this.z = entity.getZ();
      this.yaw = entity.yaw;
      this.pitch = entity.pitch;
   }

   public void read(PacketByteBuf buf) throws IOException {
      this.x = buf.readDouble();
      this.y = buf.readDouble();
      this.z = buf.readDouble();
      this.yaw = buf.readFloat();
      this.pitch = buf.readFloat();
   }

   public void write(PacketByteBuf buf) throws IOException {
      buf.writeDouble(this.x);
      buf.writeDouble(this.y);
      buf.writeDouble(this.z);
      buf.writeFloat(this.yaw);
      buf.writeFloat(this.pitch);
   }

   public void apply(ClientPlayPacketListener clientPlayPacketListener) {
      clientPlayPacketListener.onVehicleMove(this);
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
   public float getYaw() {
      return this.yaw;
   }

   @Environment(EnvType.CLIENT)
   public float getPitch() {
      return this.pitch;
   }
}
