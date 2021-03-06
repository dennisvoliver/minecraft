package net.minecraft.network.packet.c2s.login;

import com.mojang.authlib.GameProfile;
import java.io.IOException;
import java.util.UUID;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.listener.ServerLoginPacketListener;

public class LoginHelloC2SPacket implements Packet<ServerLoginPacketListener> {
   private GameProfile profile;

   public LoginHelloC2SPacket() {
   }

   public LoginHelloC2SPacket(GameProfile profile) {
      this.profile = profile;
   }

   public void read(PacketByteBuf buf) throws IOException {
      this.profile = new GameProfile((UUID)null, buf.readString(16));
   }

   public void write(PacketByteBuf buf) throws IOException {
      buf.writeString(this.profile.getName());
   }

   public void apply(ServerLoginPacketListener serverLoginPacketListener) {
      serverLoginPacketListener.onHello(this);
   }

   public GameProfile getProfile() {
      return this.profile;
   }
}
