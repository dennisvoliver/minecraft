package net.minecraft.world.level;

import java.util.UUID;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.crash.CrashReportSection;
import net.minecraft.world.GameMode;
import net.minecraft.world.MutableWorldProperties;
import net.minecraft.world.border.WorldBorder;
import net.minecraft.world.timer.Timer;

public interface ServerWorldProperties extends MutableWorldProperties {
   String getLevelName();

   void setThundering(boolean thundering);

   int getRainTime();

   void setRainTime(int rainTime);

   void setThunderTime(int thunderTime);

   int getThunderTime();

   default void populateCrashReport(CrashReportSection reportSection) {
      MutableWorldProperties.super.populateCrashReport(reportSection);
      reportSection.add("Level name", this::getLevelName);
      reportSection.add("Level game mode", () -> {
         return String.format("Game mode: %s (ID %d). Hardcore: %b. Cheats: %b", this.getGameMode().getName(), this.getGameMode().getId(), this.isHardcore(), this.areCommandsAllowed());
      });
      reportSection.add("Level weather", () -> {
         return String.format("Rain time: %d (now: %b), thunder time: %d (now: %b)", this.getRainTime(), this.isRaining(), this.getThunderTime(), this.isThundering());
      });
   }

   int getClearWeatherTime();

   void setClearWeatherTime(int clearWeatherTime);

   int getWanderingTraderSpawnDelay();

   void setWanderingTraderSpawnDelay(int wanderingTraderSpawnDelay);

   int getWanderingTraderSpawnChance();

   void setWanderingTraderSpawnChance(int wanderingTraderSpawnChance);

   void setWanderingTraderId(UUID uuid);

   GameMode getGameMode();

   void setWorldBorder(WorldBorder.Properties properties);

   WorldBorder.Properties getWorldBorder();

   boolean isInitialized();

   void setInitialized(boolean initialized);

   boolean areCommandsAllowed();

   void setGameMode(GameMode gameMode);

   Timer<MinecraftServer> getScheduledEvents();

   void setTime(long time);

   void setTimeOfDay(long timeOfDay);
}
