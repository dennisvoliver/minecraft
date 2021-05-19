package net.minecraft.world;

import com.mojang.serialization.Lifecycle;
import java.util.Set;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resource.DataPackSettings;
import net.minecraft.util.crash.CrashReportSection;
import net.minecraft.util.registry.DynamicRegistryManager;
import net.minecraft.world.gen.GeneratorOptions;
import net.minecraft.world.level.LevelInfo;
import net.minecraft.world.level.ServerWorldProperties;
import org.jetbrains.annotations.Nullable;

public interface SaveProperties {
   DataPackSettings getDataPackSettings();

   void updateLevelInfo(DataPackSettings dataPackSettings);

   boolean isModded();

   Set<String> getServerBrands();

   void addServerBrand(String brand, boolean modded);

   default void populateCrashReport(CrashReportSection reportSection) {
      reportSection.add("Known server brands", () -> {
         return String.join(", ", this.getServerBrands());
      });
      reportSection.add("Level was modded", () -> {
         return Boolean.toString(this.isModded());
      });
      reportSection.add("Level storage version", () -> {
         int i = this.getVersion();
         return String.format("0x%05X - %s", i, this.getFormatName(i));
      });
   }

   default String getFormatName(int id) {
      switch(id) {
      case 19132:
         return "McRegion";
      case 19133:
         return "Anvil";
      default:
         return "Unknown?";
      }
   }

   @Nullable
   CompoundTag getCustomBossEvents();

   void setCustomBossEvents(@Nullable CompoundTag tag);

   ServerWorldProperties getMainWorldProperties();

   @Environment(EnvType.CLIENT)
   LevelInfo getLevelInfo();

   CompoundTag cloneWorldTag(DynamicRegistryManager dynamicRegistryManager, @Nullable CompoundTag compoundTag);

   boolean isHardcore();

   int getVersion();

   String getLevelName();

   GameMode getGameMode();

   void setGameMode(GameMode gameMode);

   boolean areCommandsAllowed();

   Difficulty getDifficulty();

   void setDifficulty(Difficulty difficulty);

   boolean isDifficultyLocked();

   void setDifficultyLocked(boolean locked);

   GameRules getGameRules();

   CompoundTag getPlayerData();

   CompoundTag getDragonFight();

   void setDragonFight(CompoundTag tag);

   GeneratorOptions getGeneratorOptions();

   @Environment(EnvType.CLIENT)
   Lifecycle getLifecycle();
}
