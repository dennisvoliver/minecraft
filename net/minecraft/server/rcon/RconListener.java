package net.minecraft.server.rcon;

import com.google.common.collect.Lists;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Iterator;
import java.util.List;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.server.dedicated.ServerPropertiesHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

public class RconListener extends RconBase {
   private static final Logger SERVER_LOGGER = LogManager.getLogger();
   private final ServerSocket listener;
   private final String password;
   private final List<RconClient> clients = Lists.newArrayList();
   private final DedicatedServer server;

   private RconListener(DedicatedServer dedicatedServer, ServerSocket serverSocket, String string) {
      super("RCON Listener");
      this.server = dedicatedServer;
      this.listener = serverSocket;
      this.password = string;
   }

   private void removeStoppedClients() {
      this.clients.removeIf((rconClient) -> {
         return !rconClient.isRunning();
      });
   }

   public void run() {
      try {
         while(this.running) {
            try {
               Socket socket = this.listener.accept();
               RconClient rconClient = new RconClient(this.server, this.password, socket);
               rconClient.start();
               this.clients.add(rconClient);
               this.removeStoppedClients();
            } catch (SocketTimeoutException var7) {
               this.removeStoppedClients();
            } catch (IOException var8) {
               if (this.running) {
                  SERVER_LOGGER.info((String)"IO exception: ", (Throwable)var8);
               }
            }
         }
      } finally {
         this.closeSocket(this.listener);
      }

   }

   @Nullable
   public static RconListener create(DedicatedServer server) {
      ServerPropertiesHandler serverPropertiesHandler = server.getProperties();
      String string = server.getHostname();
      if (string.isEmpty()) {
         string = "0.0.0.0";
      }

      int i = serverPropertiesHandler.rconPort;
      if (0 < i && 65535 >= i) {
         String string2 = serverPropertiesHandler.rconPassword;
         if (string2.isEmpty()) {
            SERVER_LOGGER.warn("No rcon password set in server.properties, rcon disabled!");
            return null;
         } else {
            try {
               ServerSocket serverSocket = new ServerSocket(i, 0, InetAddress.getByName(string));
               serverSocket.setSoTimeout(500);
               RconListener rconListener = new RconListener(server, serverSocket, string2);
               if (!rconListener.start()) {
                  return null;
               } else {
                  SERVER_LOGGER.info((String)"RCON running on {}:{}", (Object)string, (Object)i);
                  return rconListener;
               }
            } catch (IOException var7) {
               SERVER_LOGGER.warn((String)"Unable to initialise RCON on {}:{}", (Object)string, i, var7);
               return null;
            }
         }
      } else {
         SERVER_LOGGER.warn((String)"Invalid rcon port {} found in server.properties, rcon disabled!", (Object)i);
         return null;
      }
   }

   public void stop() {
      this.running = false;
      this.closeSocket(this.listener);
      super.stop();
      Iterator var1 = this.clients.iterator();

      while(var1.hasNext()) {
         RconClient rconClient = (RconClient)var1.next();
         if (rconClient.isRunning()) {
            rconClient.stop();
         }
      }

      this.clients.clear();
   }

   private void closeSocket(ServerSocket socket) {
      SERVER_LOGGER.debug((String)"closeSocket: {}", (Object)socket);

      try {
         socket.close();
      } catch (IOException var3) {
         SERVER_LOGGER.warn((String)"Failed to close socket", (Throwable)var3);
      }

   }
}
