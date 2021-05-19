package net.minecraft.scoreboard;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.collect.ImmutableMap.Builder;
import java.util.Map;
import java.util.Optional;
import net.minecraft.stat.StatType;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class ScoreboardCriterion {
   public static final Map<String, ScoreboardCriterion> OBJECTIVES = Maps.newHashMap();
   public static final ScoreboardCriterion DUMMY = new ScoreboardCriterion("dummy");
   public static final ScoreboardCriterion TRIGGER = new ScoreboardCriterion("trigger");
   public static final ScoreboardCriterion DEATH_COUNT = new ScoreboardCriterion("deathCount");
   public static final ScoreboardCriterion PLAYER_KILL_COUNT = new ScoreboardCriterion("playerKillCount");
   public static final ScoreboardCriterion TOTAL_KILL_COUNT = new ScoreboardCriterion("totalKillCount");
   public static final ScoreboardCriterion HEALTH;
   public static final ScoreboardCriterion FOOD;
   public static final ScoreboardCriterion AIR;
   public static final ScoreboardCriterion ARMOR;
   public static final ScoreboardCriterion XP;
   public static final ScoreboardCriterion LEVEL;
   public static final ScoreboardCriterion[] TEAM_KILLS;
   public static final ScoreboardCriterion[] KILLED_BY_TEAMS;
   private final String name;
   private final boolean readOnly;
   private final ScoreboardCriterion.RenderType criterionType;

   public ScoreboardCriterion(String name) {
      this(name, false, ScoreboardCriterion.RenderType.INTEGER);
   }

   protected ScoreboardCriterion(String name, boolean readOnly, ScoreboardCriterion.RenderType renderType) {
      this.name = name;
      this.readOnly = readOnly;
      this.criterionType = renderType;
      OBJECTIVES.put(name, this);
   }

   public static Optional<ScoreboardCriterion> createStatCriterion(String name) {
      if (OBJECTIVES.containsKey(name)) {
         return Optional.of(OBJECTIVES.get(name));
      } else {
         int i = name.indexOf(58);
         return i < 0 ? Optional.empty() : Registry.STAT_TYPE.getOrEmpty(Identifier.splitOn(name.substring(0, i), '.')).flatMap((statType) -> {
            return createStatCriterion(statType, Identifier.splitOn(name.substring(i + 1), '.'));
         });
      }
   }

   private static <T> Optional<ScoreboardCriterion> createStatCriterion(StatType<T> statType, Identifier id) {
      Optional var10000 = statType.getRegistry().getOrEmpty(id);
      statType.getClass();
      return var10000.map(statType::getOrCreateStat);
   }

   public String getName() {
      return this.name;
   }

   public boolean isReadOnly() {
      return this.readOnly;
   }

   public ScoreboardCriterion.RenderType getCriterionType() {
      return this.criterionType;
   }

   static {
      HEALTH = new ScoreboardCriterion("health", true, ScoreboardCriterion.RenderType.HEARTS);
      FOOD = new ScoreboardCriterion("food", true, ScoreboardCriterion.RenderType.INTEGER);
      AIR = new ScoreboardCriterion("air", true, ScoreboardCriterion.RenderType.INTEGER);
      ARMOR = new ScoreboardCriterion("armor", true, ScoreboardCriterion.RenderType.INTEGER);
      XP = new ScoreboardCriterion("xp", true, ScoreboardCriterion.RenderType.INTEGER);
      LEVEL = new ScoreboardCriterion("level", true, ScoreboardCriterion.RenderType.INTEGER);
      TEAM_KILLS = new ScoreboardCriterion[]{new ScoreboardCriterion("teamkill." + Formatting.BLACK.getName()), new ScoreboardCriterion("teamkill." + Formatting.DARK_BLUE.getName()), new ScoreboardCriterion("teamkill." + Formatting.DARK_GREEN.getName()), new ScoreboardCriterion("teamkill." + Formatting.DARK_AQUA.getName()), new ScoreboardCriterion("teamkill." + Formatting.DARK_RED.getName()), new ScoreboardCriterion("teamkill." + Formatting.DARK_PURPLE.getName()), new ScoreboardCriterion("teamkill." + Formatting.GOLD.getName()), new ScoreboardCriterion("teamkill." + Formatting.GRAY.getName()), new ScoreboardCriterion("teamkill." + Formatting.DARK_GRAY.getName()), new ScoreboardCriterion("teamkill." + Formatting.BLUE.getName()), new ScoreboardCriterion("teamkill." + Formatting.GREEN.getName()), new ScoreboardCriterion("teamkill." + Formatting.AQUA.getName()), new ScoreboardCriterion("teamkill." + Formatting.RED.getName()), new ScoreboardCriterion("teamkill." + Formatting.LIGHT_PURPLE.getName()), new ScoreboardCriterion("teamkill." + Formatting.YELLOW.getName()), new ScoreboardCriterion("teamkill." + Formatting.WHITE.getName())};
      KILLED_BY_TEAMS = new ScoreboardCriterion[]{new ScoreboardCriterion("killedByTeam." + Formatting.BLACK.getName()), new ScoreboardCriterion("killedByTeam." + Formatting.DARK_BLUE.getName()), new ScoreboardCriterion("killedByTeam." + Formatting.DARK_GREEN.getName()), new ScoreboardCriterion("killedByTeam." + Formatting.DARK_AQUA.getName()), new ScoreboardCriterion("killedByTeam." + Formatting.DARK_RED.getName()), new ScoreboardCriterion("killedByTeam." + Formatting.DARK_PURPLE.getName()), new ScoreboardCriterion("killedByTeam." + Formatting.GOLD.getName()), new ScoreboardCriterion("killedByTeam." + Formatting.GRAY.getName()), new ScoreboardCriterion("killedByTeam." + Formatting.DARK_GRAY.getName()), new ScoreboardCriterion("killedByTeam." + Formatting.BLUE.getName()), new ScoreboardCriterion("killedByTeam." + Formatting.GREEN.getName()), new ScoreboardCriterion("killedByTeam." + Formatting.AQUA.getName()), new ScoreboardCriterion("killedByTeam." + Formatting.RED.getName()), new ScoreboardCriterion("killedByTeam." + Formatting.LIGHT_PURPLE.getName()), new ScoreboardCriterion("killedByTeam." + Formatting.YELLOW.getName()), new ScoreboardCriterion("killedByTeam." + Formatting.WHITE.getName())};
   }

   public static enum RenderType {
      INTEGER("integer"),
      HEARTS("hearts");

      private final String name;
      private static final Map<String, ScoreboardCriterion.RenderType> CRITERION_TYPES;

      private RenderType(String name) {
         this.name = name;
      }

      public String getName() {
         return this.name;
      }

      public static ScoreboardCriterion.RenderType getType(String name) {
         return (ScoreboardCriterion.RenderType)CRITERION_TYPES.getOrDefault(name, INTEGER);
      }

      static {
         Builder<String, ScoreboardCriterion.RenderType> builder = ImmutableMap.builder();
         ScoreboardCriterion.RenderType[] var1 = values();
         int var2 = var1.length;

         for(int var3 = 0; var3 < var2; ++var3) {
            ScoreboardCriterion.RenderType renderType = var1[var3];
            builder.put(renderType.name, renderType);
         }

         CRITERION_TYPES = builder.build();
      }
   }
}
