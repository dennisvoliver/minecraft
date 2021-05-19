package net.minecraft.world.level;

import java.util.UUID;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.crash.CrashReportSection;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Difficulty;
import net.minecraft.world.GameMode;
import net.minecraft.world.GameRules;
import net.minecraft.world.SaveProperties;
import net.minecraft.world.border.WorldBorder;
import net.minecraft.world.timer.Timer;

public class UnmodifiableLevelProperties implements ServerWorldProperties {
   private final SaveProperties field_24179;
   private final ServerWorldProperties properties;

   public UnmodifiableLevelProperties(SaveProperties saveProperties, ServerWorldProperties serverWorldProperties) {
      this.field_24179 = saveProperties;
      this.properties = serverWorldProperties;
   }

   public int getSpawnX() {
      return this.properties.getSpawnX();
   }

   public int getSpawnY() {
      return this.properties.getSpawnY();
   }

   public int getSpawnZ() {
      return this.properties.getSpawnZ();
   }

   public float getSpawnAngle() {
      return this.properties.getSpawnAngle();
   }

   public long getTime() {
      return this.properties.getTime();
   }

   public long getTimeOfDay() {
      return this.properties.getTimeOfDay();
   }

   public String getLevelName() {
      return this.field_24179.getLevelName();
   }

   public int getClearWeatherTime() {
      return this.properties.getClearWeatherTime();
   }

   public void setClearWeatherTime(int clearWeatherTime) {
   }

   public boolean isThundering() {
      return this.properties.isThundering();
   }

   public int getThunderTime() {
      return this.properties.getThunderTime();
   }

   public boolean isRaining() {
      return this.properties.isRaining();
   }

   public int getRainTime() {
      return this.properties.getRainTime();
   }

   public GameMode getGameMode() {
      return this.field_24179.getGameMode();
   }

   public void setSpawnX(int spawnX) {
   }

   public void setSpawnY(int spawnY) {
   }

   public void setSpawnZ(int spawnZ) {
   }

   public void setSpawnAngle(float angle) {
   }

   public void setTime(long time) {
   }

   public void setTimeOfDay(long timeOfDay) {
   }

   public void setSpawnPos(BlockPos pos, float angle) {
   }

   public void setThundering(boolean thundering) {
   }

   public void setThunderTime(int thunderTime) {
   }

   public void setRaining(boolean raining) {
   }

   public void setRainTime(int rainTime) {
   }

   public void setGameMode(GameMode gameMode) {
   }

   public boolean isHardcore() {
      return this.field_24179.isHardcore();
   }

   public boolean areCommandsAllowed() {
      return this.field_24179.areCommandsAllowed();
   }

   public boolean isInitialized() {
      return this.properties.isInitialized();
   }

   public void setInitialized(boolean initialized) {
   }

   public GameRules getGameRules() {
      return this.field_24179.getGameRules();
   }

   public WorldBorder.Properties getWorldBorder() {
      return this.properties.getWorldBorder();
   }

   public void setWorldBorder(WorldBorder.Properties properties) {
   }

   public Difficulty getDifficulty() {
      return this.field_24179.getDifficulty();
   }

   public boolean isDifficultyLocked() {
      return this.field_24179.isDifficultyLocked();
   }

   public Timer<MinecraftServer> getScheduledEvents() {
      return this.properties.getScheduledEvents();
   }

   public int getWanderingTraderSpawnDelay() {
      return 0;
   }

   public void setWanderingTraderSpawnDelay(int wanderingTraderSpawnDelay) {
   }

   public int getWanderingTraderSpawnChance() {
      return 0;
   }

   public void setWanderingTraderSpawnChance(int wanderingTraderSpawnChance) {
   }

   public void setWanderingTraderId(UUID uuid) {
   }

   public void populateCrashReport(CrashReportSection reportSection) {
      reportSection.add("Derived", (Object)true);
      this.properties.populateCrashReport(reportSection);
   }
}
