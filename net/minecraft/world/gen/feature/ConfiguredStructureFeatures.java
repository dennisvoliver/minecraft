package net.minecraft.world.gen.feature;

import net.minecraft.structure.BastionRemnantGenerator;
import net.minecraft.structure.DesertVillageData;
import net.minecraft.structure.PillagerOutpostGenerator;
import net.minecraft.structure.PlainsVillageData;
import net.minecraft.structure.SavannaVillageData;
import net.minecraft.structure.SnowyVillageData;
import net.minecraft.structure.TaigaVillageData;
import net.minecraft.util.registry.BuiltinRegistries;
import net.minecraft.world.gen.ProbabilityConfig;

public class ConfiguredStructureFeatures {
   public static final ConfiguredStructureFeature<StructurePoolFeatureConfig, ? extends StructureFeature<StructurePoolFeatureConfig>> PILLAGER_OUTPOST;
   public static final ConfiguredStructureFeature<MineshaftFeatureConfig, ? extends StructureFeature<MineshaftFeatureConfig>> MINESHAFT;
   public static final ConfiguredStructureFeature<MineshaftFeatureConfig, ? extends StructureFeature<MineshaftFeatureConfig>> MINESHAFT_MESA;
   public static final ConfiguredStructureFeature<DefaultFeatureConfig, ? extends StructureFeature<DefaultFeatureConfig>> MANSION;
   public static final ConfiguredStructureFeature<DefaultFeatureConfig, ? extends StructureFeature<DefaultFeatureConfig>> JUNGLE_PYRAMID;
   public static final ConfiguredStructureFeature<DefaultFeatureConfig, ? extends StructureFeature<DefaultFeatureConfig>> DESERT_PYRAMID;
   public static final ConfiguredStructureFeature<DefaultFeatureConfig, ? extends StructureFeature<DefaultFeatureConfig>> IGLOO;
   public static final ConfiguredStructureFeature<ShipwreckFeatureConfig, ? extends StructureFeature<ShipwreckFeatureConfig>> SHIPWRECK;
   public static final ConfiguredStructureFeature<ShipwreckFeatureConfig, ? extends StructureFeature<ShipwreckFeatureConfig>> SHIPWRECK_BEACHED;
   public static final ConfiguredStructureFeature<DefaultFeatureConfig, ? extends StructureFeature<DefaultFeatureConfig>> SWAMP_HUT;
   public static final ConfiguredStructureFeature<DefaultFeatureConfig, ? extends StructureFeature<DefaultFeatureConfig>> STRONGHOLD;
   public static final ConfiguredStructureFeature<DefaultFeatureConfig, ? extends StructureFeature<DefaultFeatureConfig>> MONUMENT;
   public static final ConfiguredStructureFeature<OceanRuinFeatureConfig, ? extends StructureFeature<OceanRuinFeatureConfig>> OCEAN_RUIN_COLD;
   public static final ConfiguredStructureFeature<OceanRuinFeatureConfig, ? extends StructureFeature<OceanRuinFeatureConfig>> OCEAN_RUIN_WARM;
   public static final ConfiguredStructureFeature<DefaultFeatureConfig, ? extends StructureFeature<DefaultFeatureConfig>> FORTRESS;
   public static final ConfiguredStructureFeature<DefaultFeatureConfig, ? extends StructureFeature<DefaultFeatureConfig>> NETHER_FOSSIL;
   public static final ConfiguredStructureFeature<DefaultFeatureConfig, ? extends StructureFeature<DefaultFeatureConfig>> END_CITY;
   public static final ConfiguredStructureFeature<ProbabilityConfig, ? extends StructureFeature<ProbabilityConfig>> BURIED_TREASURE;
   public static final ConfiguredStructureFeature<StructurePoolFeatureConfig, ? extends StructureFeature<StructurePoolFeatureConfig>> BASTION_REMNANT;
   public static final ConfiguredStructureFeature<StructurePoolFeatureConfig, ? extends StructureFeature<StructurePoolFeatureConfig>> VILLAGE_PLAINS;
   public static final ConfiguredStructureFeature<StructurePoolFeatureConfig, ? extends StructureFeature<StructurePoolFeatureConfig>> VILLAGE_DESERT;
   public static final ConfiguredStructureFeature<StructurePoolFeatureConfig, ? extends StructureFeature<StructurePoolFeatureConfig>> VILLAGE_SAVANNA;
   public static final ConfiguredStructureFeature<StructurePoolFeatureConfig, ? extends StructureFeature<StructurePoolFeatureConfig>> VILLAGE_SNOWY;
   public static final ConfiguredStructureFeature<StructurePoolFeatureConfig, ? extends StructureFeature<StructurePoolFeatureConfig>> VILLAGE_TAIGA;
   public static final ConfiguredStructureFeature<RuinedPortalFeatureConfig, ? extends StructureFeature<RuinedPortalFeatureConfig>> RUINED_PORTAL;
   public static final ConfiguredStructureFeature<RuinedPortalFeatureConfig, ? extends StructureFeature<RuinedPortalFeatureConfig>> RUINED_PORTAL_DESERT;
   public static final ConfiguredStructureFeature<RuinedPortalFeatureConfig, ? extends StructureFeature<RuinedPortalFeatureConfig>> RUINED_PORTAL_JUNGLE;
   public static final ConfiguredStructureFeature<RuinedPortalFeatureConfig, ? extends StructureFeature<RuinedPortalFeatureConfig>> RUINED_PORTAL_SWAMP;
   public static final ConfiguredStructureFeature<RuinedPortalFeatureConfig, ? extends StructureFeature<RuinedPortalFeatureConfig>> RUINED_PORTAL_MOUNTAIN;
   public static final ConfiguredStructureFeature<RuinedPortalFeatureConfig, ? extends StructureFeature<RuinedPortalFeatureConfig>> RUINED_PORTAL_OCEAN;
   public static final ConfiguredStructureFeature<RuinedPortalFeatureConfig, ? extends StructureFeature<RuinedPortalFeatureConfig>> RUINED_PORTAL_NETHER;

   private static <FC extends FeatureConfig, F extends StructureFeature<FC>> ConfiguredStructureFeature<FC, F> register(String id, ConfiguredStructureFeature<FC, F> configuredStructureFeature) {
      return (ConfiguredStructureFeature)BuiltinRegistries.add(BuiltinRegistries.CONFIGURED_STRUCTURE_FEATURE, (String)id, configuredStructureFeature);
   }

   static {
      PILLAGER_OUTPOST = register("pillager_outpost", StructureFeature.PILLAGER_OUTPOST.configure(new StructurePoolFeatureConfig(() -> {
         return PillagerOutpostGenerator.field_26252;
      }, 7)));
      MINESHAFT = register("mineshaft", StructureFeature.MINESHAFT.configure(new MineshaftFeatureConfig(0.004F, MineshaftFeature.Type.NORMAL)));
      MINESHAFT_MESA = register("mineshaft_mesa", StructureFeature.MINESHAFT.configure(new MineshaftFeatureConfig(0.004F, MineshaftFeature.Type.MESA)));
      MANSION = register("mansion", StructureFeature.MANSION.configure(DefaultFeatureConfig.INSTANCE));
      JUNGLE_PYRAMID = register("jungle_pyramid", StructureFeature.JUNGLE_PYRAMID.configure(DefaultFeatureConfig.INSTANCE));
      DESERT_PYRAMID = register("desert_pyramid", StructureFeature.DESERT_PYRAMID.configure(DefaultFeatureConfig.INSTANCE));
      IGLOO = register("igloo", StructureFeature.IGLOO.configure(DefaultFeatureConfig.INSTANCE));
      SHIPWRECK = register("shipwreck", StructureFeature.SHIPWRECK.configure(new ShipwreckFeatureConfig(false)));
      SHIPWRECK_BEACHED = register("shipwreck_beached", StructureFeature.SHIPWRECK.configure(new ShipwreckFeatureConfig(true)));
      SWAMP_HUT = register("swamp_hut", StructureFeature.SWAMP_HUT.configure(DefaultFeatureConfig.INSTANCE));
      STRONGHOLD = register("stronghold", StructureFeature.STRONGHOLD.configure(DefaultFeatureConfig.INSTANCE));
      MONUMENT = register("monument", StructureFeature.MONUMENT.configure(DefaultFeatureConfig.INSTANCE));
      OCEAN_RUIN_COLD = register("ocean_ruin_cold", StructureFeature.OCEAN_RUIN.configure(new OceanRuinFeatureConfig(OceanRuinFeature.BiomeType.COLD, 0.3F, 0.9F)));
      OCEAN_RUIN_WARM = register("ocean_ruin_warm", StructureFeature.OCEAN_RUIN.configure(new OceanRuinFeatureConfig(OceanRuinFeature.BiomeType.WARM, 0.3F, 0.9F)));
      FORTRESS = register("fortress", StructureFeature.FORTRESS.configure(DefaultFeatureConfig.INSTANCE));
      NETHER_FOSSIL = register("nether_fossil", StructureFeature.NETHER_FOSSIL.configure(DefaultFeatureConfig.INSTANCE));
      END_CITY = register("end_city", StructureFeature.END_CITY.configure(DefaultFeatureConfig.INSTANCE));
      BURIED_TREASURE = register("buried_treasure", StructureFeature.BURIED_TREASURE.configure(new ProbabilityConfig(0.01F)));
      BASTION_REMNANT = register("bastion_remnant", StructureFeature.BASTION_REMNANT.configure(new StructurePoolFeatureConfig(() -> {
         return BastionRemnantGenerator.field_25941;
      }, 6)));
      VILLAGE_PLAINS = register("village_plains", StructureFeature.VILLAGE.configure(new StructurePoolFeatureConfig(() -> {
         return PlainsVillageData.field_26253;
      }, 6)));
      VILLAGE_DESERT = register("village_desert", StructureFeature.VILLAGE.configure(new StructurePoolFeatureConfig(() -> {
         return DesertVillageData.field_25948;
      }, 6)));
      VILLAGE_SAVANNA = register("village_savanna", StructureFeature.VILLAGE.configure(new StructurePoolFeatureConfig(() -> {
         return SavannaVillageData.field_26285;
      }, 6)));
      VILLAGE_SNOWY = register("village_snowy", StructureFeature.VILLAGE.configure(new StructurePoolFeatureConfig(() -> {
         return SnowyVillageData.field_26286;
      }, 6)));
      VILLAGE_TAIGA = register("village_taiga", StructureFeature.VILLAGE.configure(new StructurePoolFeatureConfig(() -> {
         return TaigaVillageData.field_26341;
      }, 6)));
      RUINED_PORTAL = register("ruined_portal", StructureFeature.RUINED_PORTAL.configure(new RuinedPortalFeatureConfig(RuinedPortalFeature.Type.STANDARD)));
      RUINED_PORTAL_DESERT = register("ruined_portal_desert", StructureFeature.RUINED_PORTAL.configure(new RuinedPortalFeatureConfig(RuinedPortalFeature.Type.DESERT)));
      RUINED_PORTAL_JUNGLE = register("ruined_portal_jungle", StructureFeature.RUINED_PORTAL.configure(new RuinedPortalFeatureConfig(RuinedPortalFeature.Type.JUNGLE)));
      RUINED_PORTAL_SWAMP = register("ruined_portal_swamp", StructureFeature.RUINED_PORTAL.configure(new RuinedPortalFeatureConfig(RuinedPortalFeature.Type.SWAMP)));
      RUINED_PORTAL_MOUNTAIN = register("ruined_portal_mountain", StructureFeature.RUINED_PORTAL.configure(new RuinedPortalFeatureConfig(RuinedPortalFeature.Type.MOUNTAIN)));
      RUINED_PORTAL_OCEAN = register("ruined_portal_ocean", StructureFeature.RUINED_PORTAL.configure(new RuinedPortalFeatureConfig(RuinedPortalFeature.Type.OCEAN)));
      RUINED_PORTAL_NETHER = register("ruined_portal_nether", StructureFeature.RUINED_PORTAL.configure(new RuinedPortalFeatureConfig(RuinedPortalFeature.Type.NETHER)));
   }
}
