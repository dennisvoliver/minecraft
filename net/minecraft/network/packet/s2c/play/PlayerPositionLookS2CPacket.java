package net.minecraft.network.packet.s2c.play;

import java.io.IOException;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.Set;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.listener.ClientPlayPacketListener;

public class PlayerPositionLookS2CPacket implements Packet<ClientPlayPacketListener> {
   private double x;
   private double y;
   private double z;
   private float yaw;
   private float pitch;
   private Set<PlayerPositionLookS2CPacket.Flag> flags;
   private int teleportId;

   public PlayerPositionLookS2CPacket() {
   }

   public PlayerPositionLookS2CPacket(double x, double y, double z, float yaw, float pitch, Set<PlayerPositionLookS2CPacket.Flag> flags, int teleportId) {
      this.x = x;
      this.y = y;
      this.z = z;
      this.yaw = yaw;
      this.pitch = pitch;
      this.flags = flags;
      this.teleportId = teleportId;
   }

   public void read(PacketByteBuf buf) throws IOException {
      this.x = buf.readDouble();
      this.y = buf.readDouble();
      this.z = buf.readDouble();
      this.yaw = buf.readFloat();
      this.pitch = buf.readFloat();
      this.flags = PlayerPositionLookS2CPacket.Flag.getFlags(buf.readUnsignedByte());
      this.teleportId = buf.readVarInt();
   }

   public void write(PacketByteBuf buf) throws IOException {
      buf.writeDouble(this.x);
      buf.writeDouble(this.y);
      buf.writeDouble(this.z);
      buf.writeFloat(this.yaw);
      buf.writeFloat(this.pitch);
      buf.writeByte(PlayerPositionLookS2CPacket.Flag.getBitfield(this.flags));
      buf.writeVarInt(this.teleportId);
   }

   public void apply(ClientPlayPacketListener clientPlayPacketListener) {
      clientPlayPacketListener.onPlayerPositionLook(this);
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

   @Environment(EnvType.CLIENT)
   public int getTeleportId() {
      return this.teleportId;
   }

   @Environment(EnvType.CLIENT)
   public Set<PlayerPositionLookS2CPacket.Flag> getFlags() {
      return this.flags;
   }

   public static enum Flag {
      X(0),
      Y(1),
      Z(2),
      Y_ROT(3),
      X_ROT(4);

      private final int shift;

      private Flag(int shift) {
         this.shift = shift;
      }

      private int getMask() {
         return 1 << this.shift;
      }

      private boolean isSet(int i) {
         return (i & this.getMask()) == this.getMask();
      }

      public static Set<PlayerPositionLookS2CPacket.Flag> getFlags(int i) {
         Set<PlayerPositionLookS2CPacket.Flag> set = EnumSet.noneOf(PlayerPositionLookS2CPacket.Flag.class);
         PlayerPositionLookS2CPacket.Flag[] var2 = values();
         int var3 = var2.length;

         for(int var4 = 0; var4 < var3; ++var4) {
            PlayerPositionLookS2CPacket.Flag flag = var2[var4];
            if (flag.isSet(i)) {
               set.add(flag);
            }
         }

         return set;
      }

      public static int getBitfield(Set<PlayerPositionLookS2CPacket.Flag> set) {
         int i = 0;

         PlayerPositionLookS2CPacket.Flag flag;
         for(Iterator var2 = set.iterator(); var2.hasNext(); i |= flag.getMask()) {
            flag = (PlayerPositionLookS2CPacket.Flag)var2.next();
         }

         return i;
      }
   }
}
