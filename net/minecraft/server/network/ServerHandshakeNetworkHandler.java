package net.minecraft.server.network;

import net.minecraft.SharedConstants;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.NetworkState;
import net.minecraft.network.listener.ServerHandshakePacketListener;
import net.minecraft.network.packet.c2s.handshake.HandshakeC2SPacket;
import net.minecraft.network.packet.s2c.login.LoginDisconnectS2CPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;

public class ServerHandshakeNetworkHandler implements ServerHandshakePacketListener {
   private static final Text IGNORING_STATUS_REQUEST_MESSAGE = new LiteralText("Ignoring status request");
   private final MinecraftServer server;
   private final ClientConnection connection;

   public ServerHandshakeNetworkHandler(MinecraftServer server, ClientConnection connection) {
      this.server = server;
      this.connection = connection;
   }

   public void onHandshake(HandshakeC2SPacket packet) {
      switch(packet.getIntendedState()) {
      case LOGIN:
         this.connection.setState(NetworkState.LOGIN);
         if (packet.getProtocolVersion() != SharedConstants.getGameVersion().getProtocolVersion()) {
            TranslatableText text2;
            if (packet.getProtocolVersion() < 754) {
               text2 = new TranslatableText("multiplayer.disconnect.outdated_client", new Object[]{SharedConstants.getGameVersion().getName()});
            } else {
               text2 = new TranslatableText("multiplayer.disconnect.incompatible", new Object[]{SharedConstants.getGameVersion().getName()});
            }

            this.connection.send(new LoginDisconnectS2CPacket(text2));
            this.connection.disconnect(text2);
         } else {
            this.connection.setPacketListener(new ServerLoginNetworkHandler(this.server, this.connection));
         }
         break;
      case STATUS:
         if (this.server.acceptsStatusQuery()) {
            this.connection.setState(NetworkState.STATUS);
            this.connection.setPacketListener(new ServerQueryNetworkHandler(this.server, this.connection));
         } else {
            this.connection.disconnect(IGNORING_STATUS_REQUEST_MESSAGE);
         }
         break;
      default:
         throw new UnsupportedOperationException("Invalid intention " + packet.getIntendedState());
      }

   }

   public void onDisconnected(Text reason) {
   }

   public ClientConnection getConnection() {
      return this.connection;
   }
}
