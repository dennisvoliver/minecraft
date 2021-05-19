package net.minecraft.server.dedicated;

import java.nio.file.Path;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.registry.DynamicRegistryManager;
import net.minecraft.world.Difficulty;
import net.minecraft.world.GameMode;
import net.minecraft.world.gen.GeneratorOptions;

public class ServerPropertiesHandler extends AbstractPropertiesHandler<ServerPropertiesHandler> {
   public final boolean onlineMode = this.parseBoolean("online-mode", true);
   public final boolean preventProxyConnections = this.parseBoolean("prevent-proxy-connections", false);
   public final String serverIp = this.getString("server-ip", "");
   public final boolean spawnAnimals = this.parseBoolean("spawn-animals", true);
   public final boolean spawnNpcs = this.parseBoolean("spawn-npcs", true);
   public final boolean pvp = this.parseBoolean("pvp", true);
   public final boolean allowFlight = this.parseBoolean("allow-flight", false);
   public final String resourcePack = this.getString("resource-pack", "");
   public final String motd = this.getString("motd", "A Minecraft Server");
   public final boolean forceGameMode = this.parseBoolean("force-gamemode", false);
   public final boolean enforceWhitelist = this.parseBoolean("enforce-whitelist", false);
   public final Difficulty difficulty;
   public final GameMode gameMode;
   public final String levelName;
   public final int serverPort;
   public final int maxBuildHeight;
   public final Boolean announcePlayerAchievements;
   public final boolean enableQuery;
   public final int queryPort;
   public final boolean enableRcon;
   public final int rconPort;
   public final String rconPassword;
   public final String resourcePackHash;
   public final String resourcePackSha1;
   public final boolean hardcore;
   public final boolean allowNether;
   public final boolean spawnMonsters;
   public final boolean snooperEnabled;
   public final boolean useNativeTransport;
   public final boolean enableCommandBlock;
   public final int spawnProtection;
   public final int opPermissionLevel;
   public final int functionPermissionLevel;
   public final long maxTickTime;
   public final int rateLimit;
   public final int viewDistance;
   public final int maxPlayers;
   public final int networkCompressionThreshold;
   public final boolean broadcastRconToOps;
   public final boolean broadcastConsoleToOps;
   public final int maxWorldSize;
   public final boolean syncChunkWrites;
   public final boolean enableJmxMonitoring;
   public final boolean enableStatus;
   public final int entityBroadcastRangePercentage;
   public final String textFilteringConfig;
   public final AbstractPropertiesHandler<ServerPropertiesHandler>.PropertyAccessor<Integer> playerIdleTimeout;
   public final AbstractPropertiesHandler<ServerPropertiesHandler>.PropertyAccessor<Boolean> whiteList;
   public final GeneratorOptions generatorOptions;

   public ServerPropertiesHandler(Properties properties, DynamicRegistryManager dynamicRegistryManager) {
      super(properties);
      this.difficulty = (Difficulty)this.get("difficulty", combineParser(Difficulty::byOrdinal, Difficulty::byName), Difficulty::getName, Difficulty.EASY);
      this.gameMode = (GameMode)this.get("gamemode", combineParser(GameMode::byId, GameMode::byName), GameMode::getName, GameMode.SURVIVAL);
      this.levelName = this.getString("level-name", "world");
      this.serverPort = this.getInt("server-port", 25565);
      this.maxBuildHeight = this.transformedParseInt("max-build-height", (integer) -> {
         return MathHelper.clamp((integer + 8) / 16 * 16, 64, 256);
      }, 256);
      this.announcePlayerAchievements = this.getDeprecatedBoolean("announce-player-achievements");
      this.enableQuery = this.parseBoolean("enable-query", false);
      this.queryPort = this.getInt("query.port", 25565);
      this.enableRcon = this.parseBoolean("enable-rcon", false);
      this.rconPort = this.getInt("rcon.port", 25575);
      this.rconPassword = this.getString("rcon.password", "");
      this.resourcePackHash = this.getDeprecatedString("resource-pack-hash");
      this.resourcePackSha1 = this.getString("resource-pack-sha1", "");
      this.hardcore = this.parseBoolean("hardcore", false);
      this.allowNether = this.parseBoolean("allow-nether", true);
      this.spawnMonsters = this.parseBoolean("spawn-monsters", true);
      if (this.parseBoolean("snooper-enabled", true)) {
      }

      this.snooperEnabled = false;
      this.useNativeTransport = this.parseBoolean("use-native-transport", true);
      this.enableCommandBlock = this.parseBoolean("enable-command-block", false);
      this.spawnProtection = this.getInt("spawn-protection", 16);
      this.opPermissionLevel = this.getInt("op-permission-level", 4);
      this.functionPermissionLevel = this.getInt("function-permission-level", 2);
      this.maxTickTime = this.parseLong("max-tick-time", TimeUnit.MINUTES.toMillis(1L));
      this.rateLimit = this.getInt("rate-limit", 0);
      this.viewDistance = this.getInt("view-distance", 10);
      this.maxPlayers = this.getInt("max-players", 20);
      this.networkCompressionThreshold = this.getInt("network-compression-threshold", 256);
      this.broadcastRconToOps = this.parseBoolean("broadcast-rcon-to-ops", true);
      this.broadcastConsoleToOps = this.parseBoolean("broadcast-console-to-ops", true);
      this.maxWorldSize = this.transformedParseInt("max-world-size", (integer) -> {
         return MathHelper.clamp(integer, 1, 29999984);
      }, 29999984);
      this.syncChunkWrites = this.parseBoolean("sync-chunk-writes", true);
      this.enableJmxMonitoring = this.parseBoolean("enable-jmx-monitoring", false);
      this.enableStatus = this.parseBoolean("enable-status", true);
      this.entityBroadcastRangePercentage = this.transformedParseInt("entity-broadcast-range-percentage", (integer) -> {
         return MathHelper.clamp(integer, 10, 1000);
      }, 100);
      this.textFilteringConfig = this.getString("text-filtering-config", "");
      this.playerIdleTimeout = this.intAccessor("player-idle-timeout", 0);
      this.whiteList = this.booleanAccessor("white-list", false);
      this.generatorOptions = GeneratorOptions.fromProperties(dynamicRegistryManager, properties);
   }

   public static ServerPropertiesHandler load(DynamicRegistryManager dynamicRegistryManager, Path path) {
      return new ServerPropertiesHandler(loadProperties(path), dynamicRegistryManager);
   }

   protected ServerPropertiesHandler create(DynamicRegistryManager dynamicRegistryManager, Properties properties) {
      return new ServerPropertiesHandler(properties, dynamicRegistryManager);
   }
}
