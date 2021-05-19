package net.minecraft.client.network;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.exceptions.AuthenticationException;
import com.mojang.authlib.exceptions.AuthenticationUnavailableException;
import com.mojang.authlib.exceptions.InsufficientPrivilegesException;
import com.mojang.authlib.exceptions.InvalidCredentialsException;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import java.math.BigInteger;
import java.security.PublicKey;
import java.util.function.Consumer;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.DisconnectedScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ScreenTexts;
import net.minecraft.client.realms.gui.screen.DisconnectedRealmsScreen;
import net.minecraft.client.realms.gui.screen.RealmsScreen;
import net.minecraft.client.util.NetworkUtils;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.NetworkEncryptionUtils;
import net.minecraft.network.NetworkState;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.encryption.NetworkEncryptionException;
import net.minecraft.network.listener.ClientLoginPacketListener;
import net.minecraft.network.packet.c2s.login.LoginKeyC2SPacket;
import net.minecraft.network.packet.c2s.login.LoginQueryResponseC2SPacket;
import net.minecraft.network.packet.s2c.login.LoginCompressionS2CPacket;
import net.minecraft.network.packet.s2c.login.LoginDisconnectS2CPacket;
import net.minecraft.network.packet.s2c.login.LoginHelloS2CPacket;
import net.minecraft.network.packet.s2c.login.LoginQueryRequestS2CPacket;
import net.minecraft.network.packet.s2c.login.LoginSuccessS2CPacket;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class ClientLoginNetworkHandler implements ClientLoginPacketListener {
   private static final Logger LOGGER = LogManager.getLogger();
   private final MinecraftClient client;
   @Nullable
   private final Screen parentGui;
   private final Consumer<Text> statusConsumer;
   private final ClientConnection connection;
   private GameProfile profile;

   public ClientLoginNetworkHandler(ClientConnection connection, MinecraftClient client, @Nullable Screen parentGui, Consumer<Text> statusConsumer) {
      this.connection = connection;
      this.client = client;
      this.parentGui = parentGui;
      this.statusConsumer = statusConsumer;
   }

   public void onHello(LoginHelloS2CPacket packet) {
      Cipher cipher3;
      Cipher cipher4;
      String string2;
      LoginKeyC2SPacket loginKeyC2SPacket2;
      try {
         SecretKey secretKey = NetworkEncryptionUtils.generateKey();
         PublicKey publicKey = packet.getPublicKey();
         string2 = (new BigInteger(NetworkEncryptionUtils.generateServerId(packet.getServerId(), publicKey, secretKey))).toString(16);
         cipher3 = NetworkEncryptionUtils.cipherFromKey(2, secretKey);
         cipher4 = NetworkEncryptionUtils.cipherFromKey(1, secretKey);
         loginKeyC2SPacket2 = new LoginKeyC2SPacket(secretKey, publicKey, packet.getNonce());
      } catch (NetworkEncryptionException var8) {
         throw new IllegalStateException("Protocol error", var8);
      }

      this.statusConsumer.accept(new TranslatableText("connect.authorizing"));
      NetworkUtils.downloadExecutor.submit(() -> {
         Text text = this.joinServerSession(string2);
         if (text != null) {
            if (this.client.getCurrentServerEntry() == null || !this.client.getCurrentServerEntry().isLocal()) {
               this.connection.disconnect(text);
               return;
            }

            LOGGER.warn(text.getString());
         }

         this.statusConsumer.accept(new TranslatableText("connect.encrypting"));
         this.connection.send(loginKeyC2SPacket2, (future) -> {
            this.connection.setupEncryption(cipher3, cipher4);
         });
      });
   }

   @Nullable
   private Text joinServerSession(String serverId) {
      try {
         this.getSessionService().joinServer(this.client.getSession().getProfile(), this.client.getSession().getAccessToken(), serverId);
         return null;
      } catch (AuthenticationUnavailableException var3) {
         return new TranslatableText("disconnect.loginFailedInfo", new Object[]{new TranslatableText("disconnect.loginFailedInfo.serversUnavailable")});
      } catch (InvalidCredentialsException var4) {
         return new TranslatableText("disconnect.loginFailedInfo", new Object[]{new TranslatableText("disconnect.loginFailedInfo.invalidSession")});
      } catch (InsufficientPrivilegesException var5) {
         return new TranslatableText("disconnect.loginFailedInfo", new Object[]{new TranslatableText("disconnect.loginFailedInfo.insufficientPrivileges")});
      } catch (AuthenticationException var6) {
         return new TranslatableText("disconnect.loginFailedInfo", new Object[]{var6.getMessage()});
      }
   }

   private MinecraftSessionService getSessionService() {
      return this.client.getSessionService();
   }

   public void onLoginSuccess(LoginSuccessS2CPacket packet) {
      this.statusConsumer.accept(new TranslatableText("connect.joining"));
      this.profile = packet.getProfile();
      this.connection.setState(NetworkState.PLAY);
      this.connection.setPacketListener(new ClientPlayNetworkHandler(this.client, this.parentGui, this.connection, this.profile));
   }

   public void onDisconnected(Text reason) {
      if (this.parentGui != null && this.parentGui instanceof RealmsScreen) {
         this.client.openScreen(new DisconnectedRealmsScreen(this.parentGui, ScreenTexts.CONNECT_FAILED, reason));
      } else {
         this.client.openScreen(new DisconnectedScreen(this.parentGui, ScreenTexts.CONNECT_FAILED, reason));
      }

   }

   public ClientConnection getConnection() {
      return this.connection;
   }

   public void onDisconnect(LoginDisconnectS2CPacket packet) {
      this.connection.disconnect(packet.getReason());
   }

   public void onCompression(LoginCompressionS2CPacket packet) {
      if (!this.connection.isLocal()) {
         this.connection.setCompressionThreshold(packet.getCompressionThreshold());
      }

   }

   public void onQueryRequest(LoginQueryRequestS2CPacket packet) {
      this.statusConsumer.accept(new TranslatableText("connect.negotiating"));
      this.connection.send(new LoginQueryResponseC2SPacket(packet.getQueryId(), (PacketByteBuf)null));
   }
}
