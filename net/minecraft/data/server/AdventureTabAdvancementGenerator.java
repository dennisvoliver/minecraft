package net.minecraft.data.server;

import com.google.common.collect.ImmutableList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.advancement.Advancement;
import net.minecraft.advancement.AdvancementFrame;
import net.minecraft.advancement.AdvancementRewards;
import net.minecraft.advancement.CriterionMerger;
import net.minecraft.advancement.criterion.ChanneledLightningCriterion;
import net.minecraft.advancement.criterion.CriterionConditions;
import net.minecraft.advancement.criterion.KilledByCrossbowCriterion;
import net.minecraft.advancement.criterion.LocationArrivalCriterion;
import net.minecraft.advancement.criterion.OnKilledCriterion;
import net.minecraft.advancement.criterion.PlayerHurtEntityCriterion;
import net.minecraft.advancement.criterion.ShotCrossbowCriterion;
import net.minecraft.advancement.criterion.SlideDownBlockCriterion;
import net.minecraft.advancement.criterion.SummonedEntityCriterion;
import net.minecraft.advancement.criterion.TargetHitCriterion;
import net.minecraft.advancement.criterion.UsedTotemCriterion;
import net.minecraft.advancement.criterion.VillagerTradeCriterion;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityType;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.predicate.DamagePredicate;
import net.minecraft.predicate.NumberRange;
import net.minecraft.predicate.entity.DamageSourcePredicate;
import net.minecraft.predicate.entity.DistancePredicate;
import net.minecraft.predicate.entity.EntityEquipmentPredicate;
import net.minecraft.predicate.entity.EntityPredicate;
import net.minecraft.predicate.entity.LocationPredicate;
import net.minecraft.tag.EntityTypeTags;
import net.minecraft.tag.Tag;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.village.raid.Raid;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeKeys;

public class AdventureTabAdvancementGenerator implements Consumer<Consumer<Advancement>> {
   private static final List<RegistryKey<Biome>> BIOMES;
   private static final EntityType<?>[] MONSTERS;

   public void accept(Consumer<Advancement> consumer) {
      Advancement advancement = Advancement.Task.create().display((ItemConvertible)Items.MAP, new TranslatableText("advancements.adventure.root.title"), new TranslatableText("advancements.adventure.root.description"), new Identifier("textures/gui/advancements/backgrounds/adventure.png"), AdvancementFrame.TASK, false, false, false).criteriaMerger(CriterionMerger.OR).criterion("killed_something", (CriterionConditions)OnKilledCriterion.Conditions.createPlayerKilledEntity()).criterion("killed_by_something", (CriterionConditions)OnKilledCriterion.Conditions.createEntityKilledPlayer()).build(consumer, "adventure/root");
      Advancement advancement2 = Advancement.Task.create().parent(advancement).display((ItemConvertible)Blocks.RED_BED, new TranslatableText("advancements.adventure.sleep_in_bed.title"), new TranslatableText("advancements.adventure.sleep_in_bed.description"), (Identifier)null, AdvancementFrame.TASK, true, true, false).criterion("slept_in_bed", (CriterionConditions)LocationArrivalCriterion.Conditions.createSleptInBed()).build(consumer, "adventure/sleep_in_bed");
      requireListedBiomesVisited(Advancement.Task.create(), BIOMES).parent(advancement2).display((ItemConvertible)Items.DIAMOND_BOOTS, new TranslatableText("advancements.adventure.adventuring_time.title"), new TranslatableText("advancements.adventure.adventuring_time.description"), (Identifier)null, AdvancementFrame.CHALLENGE, true, true, false).rewards(AdvancementRewards.Builder.experience(500)).build(consumer, "adventure/adventuring_time");
      Advancement advancement3 = Advancement.Task.create().parent(advancement).display((ItemConvertible)Items.EMERALD, new TranslatableText("advancements.adventure.trade.title"), new TranslatableText("advancements.adventure.trade.description"), (Identifier)null, AdvancementFrame.TASK, true, true, false).criterion("traded", (CriterionConditions)VillagerTradeCriterion.Conditions.any()).build(consumer, "adventure/trade");
      Advancement advancement4 = this.requireListedMobsKilled(Advancement.Task.create()).parent(advancement).display((ItemConvertible)Items.IRON_SWORD, new TranslatableText("advancements.adventure.kill_a_mob.title"), new TranslatableText("advancements.adventure.kill_a_mob.description"), (Identifier)null, AdvancementFrame.TASK, true, true, false).criteriaMerger(CriterionMerger.OR).build(consumer, "adventure/kill_a_mob");
      this.requireListedMobsKilled(Advancement.Task.create()).parent(advancement4).display((ItemConvertible)Items.DIAMOND_SWORD, new TranslatableText("advancements.adventure.kill_all_mobs.title"), new TranslatableText("advancements.adventure.kill_all_mobs.description"), (Identifier)null, AdvancementFrame.CHALLENGE, true, true, false).rewards(AdvancementRewards.Builder.experience(100)).build(consumer, "adventure/kill_all_mobs");
      Advancement advancement5 = Advancement.Task.create().parent(advancement4).display((ItemConvertible)Items.BOW, new TranslatableText("advancements.adventure.shoot_arrow.title"), new TranslatableText("advancements.adventure.shoot_arrow.description"), (Identifier)null, AdvancementFrame.TASK, true, true, false).criterion("shot_arrow", (CriterionConditions)PlayerHurtEntityCriterion.Conditions.create(DamagePredicate.Builder.create().type(DamageSourcePredicate.Builder.create().projectile(true).directEntity(EntityPredicate.Builder.create().type((Tag)EntityTypeTags.ARROWS))))).build(consumer, "adventure/shoot_arrow");
      Advancement advancement6 = Advancement.Task.create().parent(advancement4).display((ItemConvertible)Items.TRIDENT, new TranslatableText("advancements.adventure.throw_trident.title"), new TranslatableText("advancements.adventure.throw_trident.description"), (Identifier)null, AdvancementFrame.TASK, true, true, false).criterion("shot_trident", (CriterionConditions)PlayerHurtEntityCriterion.Conditions.create(DamagePredicate.Builder.create().type(DamageSourcePredicate.Builder.create().projectile(true).directEntity(EntityPredicate.Builder.create().type(EntityType.TRIDENT))))).build(consumer, "adventure/throw_trident");
      Advancement.Task.create().parent(advancement6).display((ItemConvertible)Items.TRIDENT, new TranslatableText("advancements.adventure.very_very_frightening.title"), new TranslatableText("advancements.adventure.very_very_frightening.description"), (Identifier)null, AdvancementFrame.TASK, true, true, false).criterion("struck_villager", (CriterionConditions)ChanneledLightningCriterion.Conditions.create(EntityPredicate.Builder.create().type(EntityType.VILLAGER).build())).build(consumer, "adventure/very_very_frightening");
      Advancement.Task.create().parent(advancement3).display((ItemConvertible)Blocks.CARVED_PUMPKIN, new TranslatableText("advancements.adventure.summon_iron_golem.title"), new TranslatableText("advancements.adventure.summon_iron_golem.description"), (Identifier)null, AdvancementFrame.GOAL, true, true, false).criterion("summoned_golem", (CriterionConditions)SummonedEntityCriterion.Conditions.create(EntityPredicate.Builder.create().type(EntityType.IRON_GOLEM))).build(consumer, "adventure/summon_iron_golem");
      Advancement.Task.create().parent(advancement5).display((ItemConvertible)Items.ARROW, new TranslatableText("advancements.adventure.sniper_duel.title"), new TranslatableText("advancements.adventure.sniper_duel.description"), (Identifier)null, AdvancementFrame.CHALLENGE, true, true, false).rewards(AdvancementRewards.Builder.experience(50)).criterion("killed_skeleton", (CriterionConditions)OnKilledCriterion.Conditions.createPlayerKilledEntity(EntityPredicate.Builder.create().type(EntityType.SKELETON).distance(DistancePredicate.horizontal(NumberRange.FloatRange.atLeast(50.0F))), DamageSourcePredicate.Builder.create().projectile(true))).build(consumer, "adventure/sniper_duel");
      Advancement.Task.create().parent(advancement4).display((ItemConvertible)Items.TOTEM_OF_UNDYING, new TranslatableText("advancements.adventure.totem_of_undying.title"), new TranslatableText("advancements.adventure.totem_of_undying.description"), (Identifier)null, AdvancementFrame.GOAL, true, true, false).criterion("used_totem", (CriterionConditions)UsedTotemCriterion.Conditions.create(Items.TOTEM_OF_UNDYING)).build(consumer, "adventure/totem_of_undying");
      Advancement advancement7 = Advancement.Task.create().parent(advancement).display((ItemConvertible)Items.CROSSBOW, new TranslatableText("advancements.adventure.ol_betsy.title"), new TranslatableText("advancements.adventure.ol_betsy.description"), (Identifier)null, AdvancementFrame.TASK, true, true, false).criterion("shot_crossbow", (CriterionConditions)ShotCrossbowCriterion.Conditions.create(Items.CROSSBOW)).build(consumer, "adventure/ol_betsy");
      Advancement.Task.create().parent(advancement7).display((ItemConvertible)Items.CROSSBOW, new TranslatableText("advancements.adventure.whos_the_pillager_now.title"), new TranslatableText("advancements.adventure.whos_the_pillager_now.description"), (Identifier)null, AdvancementFrame.TASK, true, true, false).criterion("kill_pillager", (CriterionConditions)KilledByCrossbowCriterion.Conditions.create(EntityPredicate.Builder.create().type(EntityType.PILLAGER))).build(consumer, "adventure/whos_the_pillager_now");
      Advancement.Task.create().parent(advancement7).display((ItemConvertible)Items.CROSSBOW, new TranslatableText("advancements.adventure.two_birds_one_arrow.title"), new TranslatableText("advancements.adventure.two_birds_one_arrow.description"), (Identifier)null, AdvancementFrame.CHALLENGE, true, true, false).rewards(AdvancementRewards.Builder.experience(65)).criterion("two_birds", (CriterionConditions)KilledByCrossbowCriterion.Conditions.create(EntityPredicate.Builder.create().type(EntityType.PHANTOM), EntityPredicate.Builder.create().type(EntityType.PHANTOM))).build(consumer, "adventure/two_birds_one_arrow");
      Advancement.Task.create().parent(advancement7).display((ItemConvertible)Items.CROSSBOW, new TranslatableText("advancements.adventure.arbalistic.title"), new TranslatableText("advancements.adventure.arbalistic.description"), (Identifier)null, AdvancementFrame.CHALLENGE, true, true, true).rewards(AdvancementRewards.Builder.experience(85)).criterion("arbalistic", (CriterionConditions)KilledByCrossbowCriterion.Conditions.create(NumberRange.IntRange.exactly(5))).build(consumer, "adventure/arbalistic");
      Advancement advancement8 = Advancement.Task.create().parent(advancement).display((ItemStack)Raid.getOminousBanner(), new TranslatableText("advancements.adventure.voluntary_exile.title"), new TranslatableText("advancements.adventure.voluntary_exile.description"), (Identifier)null, AdvancementFrame.TASK, true, true, true).criterion("voluntary_exile", (CriterionConditions)OnKilledCriterion.Conditions.createPlayerKilledEntity(EntityPredicate.Builder.create().type((Tag)EntityTypeTags.RAIDERS).equipment(EntityEquipmentPredicate.OMINOUS_BANNER_ON_HEAD))).build(consumer, "adventure/voluntary_exile");
      Advancement.Task.create().parent(advancement8).display((ItemStack)Raid.getOminousBanner(), new TranslatableText("advancements.adventure.hero_of_the_village.title"), new TranslatableText("advancements.adventure.hero_of_the_village.description"), (Identifier)null, AdvancementFrame.CHALLENGE, true, true, true).rewards(AdvancementRewards.Builder.experience(100)).criterion("hero_of_the_village", (CriterionConditions)LocationArrivalCriterion.Conditions.createHeroOfTheVillage()).build(consumer, "adventure/hero_of_the_village");
      Advancement.Task.create().parent(advancement).display((ItemConvertible)Blocks.HONEY_BLOCK.asItem(), new TranslatableText("advancements.adventure.honey_block_slide.title"), new TranslatableText("advancements.adventure.honey_block_slide.description"), (Identifier)null, AdvancementFrame.TASK, true, true, false).criterion("honey_block_slide", (CriterionConditions)SlideDownBlockCriterion.Conditions.create(Blocks.HONEY_BLOCK)).build(consumer, "adventure/honey_block_slide");
      Advancement.Task.create().parent(advancement5).display((ItemConvertible)Blocks.TARGET.asItem(), new TranslatableText("advancements.adventure.bullseye.title"), new TranslatableText("advancements.adventure.bullseye.description"), (Identifier)null, AdvancementFrame.CHALLENGE, true, true, false).rewards(AdvancementRewards.Builder.experience(50)).criterion("bullseye", (CriterionConditions)TargetHitCriterion.Conditions.create(NumberRange.IntRange.exactly(15), EntityPredicate.Extended.ofLegacy(EntityPredicate.Builder.create().distance(DistancePredicate.horizontal(NumberRange.FloatRange.atLeast(30.0F))).build()))).build(consumer, "adventure/bullseye");
   }

   private Advancement.Task requireListedMobsKilled(Advancement.Task task) {
      EntityType[] var2 = MONSTERS;
      int var3 = var2.length;

      for(int var4 = 0; var4 < var3; ++var4) {
         EntityType<?> entityType = var2[var4];
         task.criterion(Registry.ENTITY_TYPE.getId(entityType).toString(), (CriterionConditions)OnKilledCriterion.Conditions.createPlayerKilledEntity(EntityPredicate.Builder.create().type(entityType)));
      }

      return task;
   }

   protected static Advancement.Task requireListedBiomesVisited(Advancement.Task task, List<RegistryKey<Biome>> list) {
      Iterator var2 = list.iterator();

      while(var2.hasNext()) {
         RegistryKey<Biome> registryKey = (RegistryKey)var2.next();
         task.criterion(registryKey.getValue().toString(), (CriterionConditions)LocationArrivalCriterion.Conditions.create(LocationPredicate.biome(registryKey)));
      }

      return task;
   }

   static {
      BIOMES = ImmutableList.of(BiomeKeys.BIRCH_FOREST_HILLS, BiomeKeys.RIVER, BiomeKeys.SWAMP, BiomeKeys.DESERT, BiomeKeys.WOODED_HILLS, BiomeKeys.GIANT_TREE_TAIGA_HILLS, BiomeKeys.SNOWY_TAIGA, BiomeKeys.BADLANDS, BiomeKeys.FOREST, BiomeKeys.STONE_SHORE, BiomeKeys.SNOWY_TUNDRA, BiomeKeys.TAIGA_HILLS, BiomeKeys.SNOWY_MOUNTAINS, BiomeKeys.WOODED_BADLANDS_PLATEAU, BiomeKeys.SAVANNA, BiomeKeys.PLAINS, BiomeKeys.FROZEN_RIVER, BiomeKeys.GIANT_TREE_TAIGA, BiomeKeys.SNOWY_BEACH, BiomeKeys.JUNGLE_HILLS, BiomeKeys.JUNGLE_EDGE, BiomeKeys.MUSHROOM_FIELD_SHORE, BiomeKeys.MOUNTAINS, BiomeKeys.DESERT_HILLS, BiomeKeys.JUNGLE, BiomeKeys.BEACH, BiomeKeys.SAVANNA_PLATEAU, BiomeKeys.SNOWY_TAIGA_HILLS, BiomeKeys.BADLANDS_PLATEAU, BiomeKeys.DARK_FOREST, BiomeKeys.TAIGA, BiomeKeys.BIRCH_FOREST, BiomeKeys.MUSHROOM_FIELDS, BiomeKeys.WOODED_MOUNTAINS, BiomeKeys.WARM_OCEAN, BiomeKeys.LUKEWARM_OCEAN, BiomeKeys.COLD_OCEAN, BiomeKeys.DEEP_LUKEWARM_OCEAN, BiomeKeys.DEEP_COLD_OCEAN, BiomeKeys.DEEP_FROZEN_OCEAN, BiomeKeys.BAMBOO_JUNGLE, BiomeKeys.BAMBOO_JUNGLE_HILLS);
      MONSTERS = new EntityType[]{EntityType.BLAZE, EntityType.CAVE_SPIDER, EntityType.CREEPER, EntityType.DROWNED, EntityType.ELDER_GUARDIAN, EntityType.ENDER_DRAGON, EntityType.ENDERMAN, EntityType.ENDERMITE, EntityType.EVOKER, EntityType.GHAST, EntityType.GUARDIAN, EntityType.HOGLIN, EntityType.HUSK, EntityType.MAGMA_CUBE, EntityType.PHANTOM, EntityType.PIGLIN, EntityType.PIGLIN_BRUTE, EntityType.PILLAGER, EntityType.RAVAGER, EntityType.SHULKER, EntityType.SILVERFISH, EntityType.SKELETON, EntityType.SLIME, EntityType.SPIDER, EntityType.STRAY, EntityType.VEX, EntityType.VINDICATOR, EntityType.WITCH, EntityType.WITHER_SKELETON, EntityType.WITHER, EntityType.ZOGLIN, EntityType.ZOMBIE_VILLAGER, EntityType.ZOMBIE, EntityType.ZOMBIFIED_PIGLIN};
   }
}
