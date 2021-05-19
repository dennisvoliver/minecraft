package net.minecraft.server.integrated;

import com.google.common.collect.Lists;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.GameProfileRepository;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.BooleanSupplier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.SharedConstants;
import net.minecraft.client.ClientBrandRetriever;
import net.minecraft.client.MinecraftClient;
import net.minecraft.resource.ResourcePackManager;
import net.minecraft.resource.ServerResourceManager;
import net.minecraft.server.LanServerPinger;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.WorldGenerationProgressListenerFactory;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.UserCache;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.util.registry.DynamicRegistryManager;
import net.minecraft.util.snooper.Snooper;
import net.minecraft.world.GameMode;
import net.minecraft.world.SaveProperties;
import net.minecraft.world.level.storage.LevelStorage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Environment(EnvType.CLIENT)
public class IntegratedServer extends MinecraftServer {
   private static final Logger LOGGER = LogManager.getLogger();
   private final MinecraftClient client;
   private boolean paused;
   private int lanPort = -1;
   private LanServerPinger lanPinger;
   private UUID localPlayerUuid;

   public IntegratedServer(Thread serverThread, MinecraftClient client, DynamicRegistryManager.Impl registryManager, LevelStorage.Session session, ResourcePackManager resourcePackManager, ServerResourceManager serverResourceManager, SaveProperties saveProperties, MinecraftSessionService minecraftSessionService, GameProfileRepository gameProfileRepository, UserCache userCache, WorldGenerationProgressListenerFactory worldGenerationProgressListenerFactory) {
      super(serverThread, registryManager, session, saveProperties, resourcePackManager, client.getNetworkProxy(), client.getDataFixer(), serverResourceManager, minecraftSessionService, gameProfileRepository, userCache, worldGenerationProgressListenerFactory);
      this.setServerName(client.getSession().getUsername());
      this.setDemo(client.isDemo());
      this.setWorldHeight(256);
      this.setPlayerManager(new IntegratedPlayerManager(this, this.registryManager, this.saveHandler));
      this.client = client;
   }

   public boolean setupServer() {
      LOGGER.info("Starting integrated minecraft server version " + SharedConstants.getGameVersion().getName());
      this.setOnlineMode(true);
      this.setPvpEnabled(true);
      this.setFlightEnabled(true);
      this.method_31400();
      this.loadWorld();
      this.setMotd(this.getUserName() + " - " + this.getSaveProperties().getLevelName());
      return true;
   }

   public void tick(BooleanSupplier shouldKeepTicking) {
      boolean bl = this.paused;
      this.paused = MinecraftClient.getInstance().getNetworkHandler() != null && MinecraftClient.getInstance().isPaused();
      Profiler profiler = this.getProfiler();
      if (!bl && this.paused) {
         profiler.push("autoSave");
         LOGGER.info("Saving and pausing game...");
         this.getPlayerManager().saveAllPlayerData();
         this.save(false, false, false);
         profiler.pop();
      }

      if (!this.paused) {
         super.tick(shouldKeepTicking);
         int i = Math.max(2, this.client.options.viewDistance + -1);
         if (i != this.getPlayerManager().getViewDistance()) {
            LOGGER.info((String)"Changing view distance to {}, from {}", (Object)i, (Object)this.getPlayerManager().getViewDistance());
            this.getPlayerManager().setViewDistance(i);
         }

      }
   }

   public boolean shouldBroadcastRconToOps() {
      return true;
   }

   public boolean shouldBroadcastConsoleToOps() {
      return true;
   }

   public File getRunDirectory() {
      return this.client.runDirectory;
   }

   public boolean isDedicated() {
      return false;
   }

   public int getRateLimit() {
      return 0;
   }

   public boolean isUsingNativeTransport() {
      return false;
   }

   public void setCrashReport(CrashReport report) {
      this.client.setCrashReport(report);
   }

   public CrashReport populateCrashReport(CrashReport report) {
      report = super.populateCrashReport(report);
      report.getSystemDetailsSection().add("Type", (Object)"Integrated Server (map_client.txt)");
      report.getSystemDetailsSection().add("Is Modded", () -> {
         return (String)this.getModdedStatusMessage().orElse("Probably not. Jar signature remains and both client + server brands are untouched.");
      });
      return report;
   }

   public Optional<String> getModdedStatusMessage() {
      String string = ClientBrandRetriever.getClientModName();
      if (!string.equals("vanilla")) {
         return Optional.of("Definitely; Client brand changed to '" + string + "'");
      } else {
         string = this.getServerModName();
         if (!"vanilla".equals(string)) {
            return Optional.of("Definitely; Server brand changed to '" + string + "'");
         } else {
            return MinecraftClient.class.getSigners() == null ? Optional.of("Very likely; Jar signature invalidated") : Optional.empty();
         }
      }
   }

   public void addSnooperInfo(Snooper snooper) {
      super.addSnooperInfo(snooper);
      snooper.addInfo("snooper_partner", this.client.getSnooper().getToken());
   }

   public boolean openToLan(GameMode gameMode, boolean cheatsAllowed, int port) {
      try {
         this.getNetworkIo().bind((InetAddress)null, port);
         LOGGER.info((String)"Started serving on {}", (Object)port);
         this.lanPort = port;
         this.lanPinger = new LanServerPinger(this.getServerMotd(), port + "");
         this.lanPinger.start();
         this.getPlayerManager().setGameMode(gameMode);
         this.getPlayerManager().setCheatsAllowed(cheatsAllowed);
         int i = this.getPermissionLevel(this.client.player.getGameProfile());
         this.client.player.setClientPermissionLevel(i);
         Iterator var5 = this.getPlayerManager().getPlayerList().iterator();

         while(var5.hasNext()) {
            ServerPlayerEntity serverPlayerEntity = (ServerPlayerEntity)var5.next();
            this.getCommandManager().sendCommandTree(serverPlayerEntity);
         }

         return true;
      } catch (IOException var7) {
         return false;
      }
   }

   public void shutdown() {
      super.shutdown();
      if (this.lanPinger != null) {
         this.lanPinger.interrupt();
         this.lanPinger = null;
      }

   }

   public void stop(boolean bl) {
      this.submitAndJoin(() -> {
         List<ServerPlayerEntity> list = Lists.newArrayList((Iterable)this.getPlayerManager().getPlayerList());
         Iterator var2 = list.iterator();

         while(var2.hasNext()) {
            ServerPlayerEntity serverPlayerEntity = (ServerPlayerEntity)var2.next();
            if (!serverPlayerEntity.getUuid().equals(this.localPlayerUuid)) {
               this.getPlayerManager().remove(serverPlayerEntity);
            }
         }

      });
      super.stop(bl);
      if (this.lanPinger != null) {
         this.lanPinger.interrupt();
         this.lanPinger = null;
      }

   }

   public boolean isRemote() {
      return this.lanPort > -1;
   }

   public int getServerPort() {
      return this.lanPort;
   }

   public void setDefaultGameMode(GameMode gameMode) {
      super.setDefaultGameMode(gameMode);
      this.getPlayerManager().setGameMode(gameMode);
   }

   public boolean areCommandBlocksEnabled() {
      return true;
   }

   public int getOpPermissionLevel() {
      return 2;
   }

   public int getFunctionPermissionLevel() {
      return 2;
   }

   public void setLocalPlayerUuid(UUID localPlayerUuid) {
      this.localPlayerUuid = localPlayerUuid;
   }

   public boolean isHost(GameProfile profile) {
      return profile.getName().equalsIgnoreCase(this.getUserName());
   }

   public int adjustTrackingDistance(int initialDistance) {
      return (int)(this.client.options.entityDistanceScaling * (float)initialDistance);
   }

   public boolean syncChunkWrites() {
      return this.client.options.syncChunkWrites;
   }
}
