package net.minecraft.advancement.criterion;

import com.google.common.collect.Maps;
import java.util.Map;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

public class Criteria {
   private static final Map<Identifier, Criterion<?>> VALUES = Maps.newHashMap();
   public static final ImpossibleCriterion IMPOSSIBLE = (ImpossibleCriterion)register(new ImpossibleCriterion());
   public static final OnKilledCriterion PLAYER_KILLED_ENTITY = (OnKilledCriterion)register(new OnKilledCriterion(new Identifier("player_killed_entity")));
   public static final OnKilledCriterion ENTITY_KILLED_PLAYER = (OnKilledCriterion)register(new OnKilledCriterion(new Identifier("entity_killed_player")));
   public static final EnterBlockCriterion ENTER_BLOCK = (EnterBlockCriterion)register(new EnterBlockCriterion());
   public static final InventoryChangedCriterion INVENTORY_CHANGED = (InventoryChangedCriterion)register(new InventoryChangedCriterion());
   public static final RecipeUnlockedCriterion RECIPE_UNLOCKED = (RecipeUnlockedCriterion)register(new RecipeUnlockedCriterion());
   public static final PlayerHurtEntityCriterion PLAYER_HURT_ENTITY = (PlayerHurtEntityCriterion)register(new PlayerHurtEntityCriterion());
   public static final EntityHurtPlayerCriterion ENTITY_HURT_PLAYER = (EntityHurtPlayerCriterion)register(new EntityHurtPlayerCriterion());
   public static final EnchantedItemCriterion ENCHANTED_ITEM = (EnchantedItemCriterion)register(new EnchantedItemCriterion());
   public static final FilledBucketCriterion FILLED_BUCKET = (FilledBucketCriterion)register(new FilledBucketCriterion());
   public static final BrewedPotionCriterion BREWED_POTION = (BrewedPotionCriterion)register(new BrewedPotionCriterion());
   public static final ConstructBeaconCriterion CONSTRUCT_BEACON = (ConstructBeaconCriterion)register(new ConstructBeaconCriterion());
   public static final UsedEnderEyeCriterion USED_ENDER_EYE = (UsedEnderEyeCriterion)register(new UsedEnderEyeCriterion());
   public static final SummonedEntityCriterion SUMMONED_ENTITY = (SummonedEntityCriterion)register(new SummonedEntityCriterion());
   public static final BredAnimalsCriterion BRED_ANIMALS = (BredAnimalsCriterion)register(new BredAnimalsCriterion());
   public static final LocationArrivalCriterion LOCATION = (LocationArrivalCriterion)register(new LocationArrivalCriterion(new Identifier("location")));
   public static final LocationArrivalCriterion SLEPT_IN_BED = (LocationArrivalCriterion)register(new LocationArrivalCriterion(new Identifier("slept_in_bed")));
   public static final CuredZombieVillagerCriterion CURED_ZOMBIE_VILLAGER = (CuredZombieVillagerCriterion)register(new CuredZombieVillagerCriterion());
   public static final VillagerTradeCriterion VILLAGER_TRADE = (VillagerTradeCriterion)register(new VillagerTradeCriterion());
   public static final ItemDurabilityChangedCriterion ITEM_DURABILITY_CHANGED = (ItemDurabilityChangedCriterion)register(new ItemDurabilityChangedCriterion());
   public static final LevitationCriterion LEVITATION = (LevitationCriterion)register(new LevitationCriterion());
   public static final ChangedDimensionCriterion CHANGED_DIMENSION = (ChangedDimensionCriterion)register(new ChangedDimensionCriterion());
   public static final TickCriterion TICK = (TickCriterion)register(new TickCriterion());
   public static final TameAnimalCriterion TAME_ANIMAL = (TameAnimalCriterion)register(new TameAnimalCriterion());
   public static final PlacedBlockCriterion PLACED_BLOCK = (PlacedBlockCriterion)register(new PlacedBlockCriterion());
   public static final ConsumeItemCriterion CONSUME_ITEM = (ConsumeItemCriterion)register(new ConsumeItemCriterion());
   public static final EffectsChangedCriterion EFFECTS_CHANGED = (EffectsChangedCriterion)register(new EffectsChangedCriterion());
   public static final UsedTotemCriterion USED_TOTEM = (UsedTotemCriterion)register(new UsedTotemCriterion());
   public static final NetherTravelCriterion NETHER_TRAVEL = (NetherTravelCriterion)register(new NetherTravelCriterion());
   public static final FishingRodHookedCriterion FISHING_ROD_HOOKED = (FishingRodHookedCriterion)register(new FishingRodHookedCriterion());
   public static final ChanneledLightningCriterion CHANNELED_LIGHTNING = (ChanneledLightningCriterion)register(new ChanneledLightningCriterion());
   public static final ShotCrossbowCriterion SHOT_CROSSBOW = (ShotCrossbowCriterion)register(new ShotCrossbowCriterion());
   public static final KilledByCrossbowCriterion KILLED_BY_CROSSBOW = (KilledByCrossbowCriterion)register(new KilledByCrossbowCriterion());
   public static final LocationArrivalCriterion HERO_OF_THE_VILLAGE = (LocationArrivalCriterion)register(new LocationArrivalCriterion(new Identifier("hero_of_the_village")));
   public static final LocationArrivalCriterion VOLUNTARY_EXILE = (LocationArrivalCriterion)register(new LocationArrivalCriterion(new Identifier("voluntary_exile")));
   public static final SlideDownBlockCriterion SLIDE_DOWN_BLOCK = (SlideDownBlockCriterion)register(new SlideDownBlockCriterion());
   public static final BeeNestDestroyedCriterion BEE_NEST_DESTROYED = (BeeNestDestroyedCriterion)register(new BeeNestDestroyedCriterion());
   public static final TargetHitCriterion TARGET_HIT = (TargetHitCriterion)register(new TargetHitCriterion());
   public static final ItemUsedOnBlockCriterion ITEM_USED_ON_BLOCK = (ItemUsedOnBlockCriterion)register(new ItemUsedOnBlockCriterion());
   public static final PlayerGeneratesContainerLootCriterion PLAYER_GENERATES_CONTAINER_LOOT = (PlayerGeneratesContainerLootCriterion)register(new PlayerGeneratesContainerLootCriterion());
   public static final ThrownItemPickedUpByEntityCriterion THROWN_ITEM_PICKED_UP_BY_ENTITY = (ThrownItemPickedUpByEntityCriterion)register(new ThrownItemPickedUpByEntityCriterion());
   public static final PlayerInteractedWithEntityCriterion PLAYER_INTERACTED_WITH_ENTITY = (PlayerInteractedWithEntityCriterion)register(new PlayerInteractedWithEntityCriterion());

   private static <T extends Criterion<?>> T register(T object) {
      if (VALUES.containsKey(object.getId())) {
         throw new IllegalArgumentException("Duplicate criterion id " + object.getId());
      } else {
         VALUES.put(object.getId(), object);
         return object;
      }
   }

   @Nullable
   public static <T extends CriterionConditions> Criterion<T> getById(Identifier id) {
      return (Criterion)VALUES.get(id);
   }

   public static Iterable<? extends Criterion<?>> getCriteria() {
      return VALUES.values();
   }
}
