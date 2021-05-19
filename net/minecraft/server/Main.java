package net.minecraft.server;

import com.google.common.collect.ImmutableSet;
import com.mojang.authlib.GameProfileRepository;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import com.mojang.datafixers.DataFixer;
import com.mojang.serialization.Lifecycle;
import java.awt.GraphicsEnvironment;
import java.io.File;
import java.io.OutputStream;
import java.net.Proxy;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.BooleanSupplier;
import joptsimple.NonOptionArgumentSpec;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import net.minecraft.Bootstrap;
import net.minecraft.datafixer.Schemas;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resource.DataPackSettings;
import net.minecraft.resource.FileResourcePackProvider;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourcePackManager;
import net.minecraft.resource.ResourcePackProvider;
import net.minecraft.resource.ResourcePackSource;
import net.minecraft.resource.ServerResourceManager;
import net.minecraft.resource.VanillaDataPackProvider;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.dedicated.EulaReader;
import net.minecraft.server.dedicated.MinecraftDedicatedServer;
import net.minecraft.server.dedicated.ServerPropertiesHandler;
import net.minecraft.server.dedicated.ServerPropertiesLoader;
import net.minecraft.text.Text;
import net.minecraft.util.UserCache;
import net.minecraft.util.Util;
import net.minecraft.util.WorldSavePath;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.dynamic.RegistryOps;
import net.minecraft.util.logging.UncaughtExceptionLogger;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.registry.DynamicRegistryManager;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.GameRules;
import net.minecraft.world.SaveProperties;
import net.minecraft.world.World;
import net.minecraft.world.gen.GeneratorOptions;
import net.minecraft.world.level.LevelInfo;
import net.minecraft.world.level.LevelProperties;
import net.minecraft.world.level.storage.LevelStorage;
import net.minecraft.world.updater.WorldUpdater;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Main {
   private static final Logger LOGGER = LogManager.getLogger();

   public static void main(String[] args) {
      OptionParser optionParser = new OptionParser();
      OptionSpec<Void> optionSpec = optionParser.accepts("nogui");
      OptionSpec<Void> optionSpec2 = optionParser.accepts("initSettings", "Initializes 'server.properties' and 'eula.txt', then quits");
      OptionSpec<Void> optionSpec3 = optionParser.accepts("demo");
      OptionSpec<Void> optionSpec4 = optionParser.accepts("bonusChest");
      OptionSpec<Void> optionSpec5 = optionParser.accepts("forceUpgrade");
      OptionSpec<Void> optionSpec6 = optionParser.accepts("eraseCache");
      OptionSpec<Void> optionSpec7 = optionParser.accepts("safeMode", "Loads level with vanilla datapack only");
      OptionSpec<Void> optionSpec8 = optionParser.accepts("help").forHelp();
      OptionSpec<String> optionSpec9 = optionParser.accepts("singleplayer").withRequiredArg();
      OptionSpec<String> optionSpec10 = optionParser.accepts("universe").withRequiredArg().defaultsTo(".");
      OptionSpec<String> optionSpec11 = optionParser.accepts("world").withRequiredArg();
      OptionSpec<Integer> optionSpec12 = optionParser.accepts("port").withRequiredArg().ofType(Integer.class).defaultsTo(-1);
      OptionSpec<String> optionSpec13 = optionParser.accepts("serverId").withRequiredArg();
      NonOptionArgumentSpec optionSpec14 = optionParser.nonOptions();

      try {
         OptionSet optionSet = optionParser.parse(args);
         if (optionSet.has((OptionSpec)optionSpec8)) {
            optionParser.printHelpOn((OutputStream)System.err);
            return;
         }

         CrashReport.initCrashReport();
         Bootstrap.initialize();
         Bootstrap.logMissing();
         Util.startTimerHack();
         DynamicRegistryManager.Impl impl = DynamicRegistryManager.create();
         Path path = Paths.get("server.properties");
         ServerPropertiesLoader serverPropertiesLoader = new ServerPropertiesLoader(impl, path);
         serverPropertiesLoader.store();
         Path path2 = Paths.get("eula.txt");
         EulaReader eulaReader = new EulaReader(path2);
         if (optionSet.has((OptionSpec)optionSpec2)) {
            LOGGER.info((String)"Initialized '{}' and '{}'", (Object)path.toAbsolutePath(), (Object)path2.toAbsolutePath());
            return;
         }

         if (!eulaReader.isEulaAgreedTo()) {
            LOGGER.info("You need to agree to the EULA in order to run the server. Go to eula.txt for more info.");
            return;
         }

         File file = new File((String)optionSet.valueOf((OptionSpec)optionSpec10));
         YggdrasilAuthenticationService yggdrasilAuthenticationService = new YggdrasilAuthenticationService(Proxy.NO_PROXY);
         MinecraftSessionService minecraftSessionService = yggdrasilAuthenticationService.createMinecraftSessionService();
         GameProfileRepository gameProfileRepository = yggdrasilAuthenticationService.createProfileRepository();
         UserCache userCache = new UserCache(gameProfileRepository, new File(file, MinecraftServer.USER_CACHE_FILE.getName()));
         String string = (String)Optional.ofNullable(optionSet.valueOf((OptionSpec)optionSpec11)).orElse(serverPropertiesLoader.getPropertiesHandler().levelName);
         LevelStorage levelStorage = LevelStorage.create(file.toPath());
         LevelStorage.Session session = levelStorage.createSession(string);
         MinecraftServer.convertLevel(session);
         DataPackSettings dataPackSettings = session.getDataPackSettings();
         boolean bl = optionSet.has((OptionSpec)optionSpec7);
         if (bl) {
            LOGGER.warn("Safe mode active, only vanilla datapack will be loaded");
         }

         ResourcePackManager resourcePackManager = new ResourcePackManager(new ResourcePackProvider[]{new VanillaDataPackProvider(), new FileResourcePackProvider(session.getDirectory(WorldSavePath.DATAPACKS).toFile(), ResourcePackSource.PACK_SOURCE_WORLD)});
         DataPackSettings dataPackSettings2 = MinecraftServer.loadDataPacks(resourcePackManager, dataPackSettings == null ? DataPackSettings.SAFE_MODE : dataPackSettings, bl);
         CompletableFuture completableFuture = ServerResourceManager.reload(resourcePackManager.createResourcePacks(), CommandManager.RegistrationEnvironment.DEDICATED, serverPropertiesLoader.getPropertiesHandler().functionPermissionLevel, Util.getMainWorkerExecutor(), Runnable::run);

         ServerResourceManager serverResourceManager2;
         try {
            serverResourceManager2 = (ServerResourceManager)completableFuture.get();
         } catch (Exception var41) {
            LOGGER.warn((String)"Failed to load datapacks, can't proceed with server load. You can either fix your datapacks or reset to vanilla with --safeMode", (Throwable)var41);
            resourcePackManager.close();
            return;
         }

         serverResourceManager2.loadRegistryTags();
         RegistryOps<Tag> registryOps = RegistryOps.of(NbtOps.INSTANCE, (ResourceManager)serverResourceManager2.getResourceManager(), impl);
         SaveProperties saveProperties = session.readLevelProperties(registryOps, dataPackSettings2);
         if (saveProperties == null) {
            LevelInfo levelInfo2;
            GeneratorOptions generatorOptions2;
            if (optionSet.has((OptionSpec)optionSpec3)) {
               levelInfo2 = MinecraftServer.DEMO_LEVEL_INFO;
               generatorOptions2 = GeneratorOptions.method_31112(impl);
            } else {
               ServerPropertiesHandler serverPropertiesHandler = serverPropertiesLoader.getPropertiesHandler();
               levelInfo2 = new LevelInfo(serverPropertiesHandler.levelName, serverPropertiesHandler.gameMode, serverPropertiesHandler.hardcore, serverPropertiesHandler.difficulty, false, new GameRules(), dataPackSettings2);
               generatorOptions2 = optionSet.has((OptionSpec)optionSpec4) ? serverPropertiesHandler.generatorOptions.withBonusChest() : serverPropertiesHandler.generatorOptions;
            }

            saveProperties = new LevelProperties(levelInfo2, generatorOptions2, Lifecycle.stable());
         }

         if (optionSet.has((OptionSpec)optionSpec5)) {
            forceUpgradeWorld(session, Schemas.getFixer(), optionSet.has((OptionSpec)optionSpec6), () -> {
               return true;
            }, ((SaveProperties)saveProperties).getGeneratorOptions().getWorlds());
         }

         session.backupLevelDataFile(impl, (SaveProperties)saveProperties);
         final MinecraftDedicatedServer minecraftDedicatedServer = (MinecraftDedicatedServer)MinecraftServer.startServer((serverThread) -> {
            MinecraftDedicatedServer minecraftDedicatedServer = new MinecraftDedicatedServer(serverThread, impl, session, resourcePackManager, serverResourceManager2, saveProperties, serverPropertiesLoader, Schemas.getFixer(), minecraftSessionService, gameProfileRepository, userCache, WorldGenerationProgressLogger::new);
            minecraftDedicatedServer.setServerName((String)optionSet.valueOf(optionSpec9));
            minecraftDedicatedServer.setServerPort((Integer)optionSet.valueOf(optionSpec12));
            minecraftDedicatedServer.setDemo(optionSet.has(optionSpec3));
            minecraftDedicatedServer.setServerId((String)optionSet.valueOf(optionSpec13));
            boolean bl = !optionSet.has(optionSpec) && !optionSet.valuesOf(optionSpec14).contains("nogui");
            if (bl && !GraphicsEnvironment.isHeadless()) {
               minecraftDedicatedServer.createGui();
            }

            return minecraftDedicatedServer;
         });
         Thread thread = new Thread("Server Shutdown Thread") {
            public void run() {
               minecraftDedicatedServer.stop(true);
            }
         };
         thread.setUncaughtExceptionHandler(new UncaughtExceptionLogger(LOGGER));
         Runtime.getRuntime().addShutdownHook(thread);
      } catch (Exception var42) {
         LOGGER.fatal((String)"Failed to start the minecraft server", (Throwable)var42);
      }

   }

   private static void forceUpgradeWorld(LevelStorage.Session session, DataFixer dataFixer, boolean eraseCache, BooleanSupplier booleanSupplier, ImmutableSet<RegistryKey<World>> worlds) {
      LOGGER.info("Forcing world upgrade!");
      WorldUpdater worldUpdater = new WorldUpdater(session, dataFixer, worlds, eraseCache);
      Text text = null;

      while(!worldUpdater.isDone()) {
         Text text2 = worldUpdater.getStatus();
         if (text != text2) {
            text = text2;
            LOGGER.info(worldUpdater.getStatus().getString());
         }

         int i = worldUpdater.getTotalChunkCount();
         if (i > 0) {
            int j = worldUpdater.getUpgradedChunkCount() + worldUpdater.getSkippedChunkCount();
            LOGGER.info((String)"{}% completed ({} / {} chunks)...", (Object)MathHelper.floor((float)j / (float)i * 100.0F), j, i);
         }

         if (!booleanSupplier.getAsBoolean()) {
            worldUpdater.cancel();
         } else {
            try {
               Thread.sleep(1000L);
            } catch (InterruptedException var10) {
            }
         }
      }

   }
}
