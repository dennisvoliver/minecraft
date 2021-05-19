package net.minecraft.util.registry;

import com.google.common.collect.Maps;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Keyable;
import com.mojang.serialization.Lifecycle;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.Map.Entry;
import java.util.function.Supplier;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.SharedConstants;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.brain.Activity;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.Schedule;
import net.minecraft.entity.ai.brain.sensor.SensorType;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.decoration.painting.PaintingMotive;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.loot.condition.LootConditionType;
import net.minecraft.loot.condition.LootConditionTypes;
import net.minecraft.loot.entry.LootPoolEntryType;
import net.minecraft.loot.entry.LootPoolEntryTypes;
import net.minecraft.loot.function.LootFunctionType;
import net.minecraft.loot.function.LootFunctionTypes;
import net.minecraft.particle.ParticleType;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.potion.Potion;
import net.minecraft.potion.Potions;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.RecipeType;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.StatType;
import net.minecraft.stat.Stats;
import net.minecraft.structure.StructurePieceType;
import net.minecraft.structure.pool.StructurePool;
import net.minecraft.structure.pool.StructurePoolElementType;
import net.minecraft.structure.processor.StructureProcessorList;
import net.minecraft.structure.processor.StructureProcessorType;
import net.minecraft.structure.rule.PosRuleTestType;
import net.minecraft.structure.rule.RuleTestType;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.IndexedIterable;
import net.minecraft.village.VillagerProfession;
import net.minecraft.village.VillagerType;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.BiomeSource;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.dimension.DimensionOptions;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.gen.carver.Carver;
import net.minecraft.world.gen.carver.ConfiguredCarver;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.chunk.ChunkGeneratorSettings;
import net.minecraft.world.gen.decorator.Decorator;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.gen.feature.ConfiguredStructureFeature;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.StructureFeature;
import net.minecraft.world.gen.feature.size.FeatureSizeType;
import net.minecraft.world.gen.foliage.FoliagePlacerType;
import net.minecraft.world.gen.placer.BlockPlacerType;
import net.minecraft.world.gen.stateprovider.BlockStateProviderType;
import net.minecraft.world.gen.surfacebuilder.ConfiguredSurfaceBuilder;
import net.minecraft.world.gen.surfacebuilder.SurfaceBuilder;
import net.minecraft.world.gen.tree.TreeDecoratorType;
import net.minecraft.world.gen.trunk.TrunkPlacerType;
import net.minecraft.world.poi.PointOfInterestType;
import org.apache.commons.lang3.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

public abstract class Registry<T> implements Codec<T>, Keyable, IndexedIterable<T> {
   protected static final Logger LOGGER = LogManager.getLogger();
   private static final Map<Identifier, Supplier<?>> DEFAULT_ENTRIES = Maps.newLinkedHashMap();
   public static final Identifier ROOT_KEY = new Identifier("root");
   protected static final MutableRegistry<MutableRegistry<?>> ROOT = new SimpleRegistry(createRegistryKey("root"), Lifecycle.experimental());
   public static final Registry<? extends Registry<?>> REGISTRIES;
   public static final RegistryKey<Registry<SoundEvent>> SOUND_EVENT_KEY;
   public static final RegistryKey<Registry<Fluid>> FLUID_KEY;
   public static final RegistryKey<Registry<StatusEffect>> MOB_EFFECT_KEY;
   public static final RegistryKey<Registry<Block>> BLOCK_KEY;
   public static final RegistryKey<Registry<Enchantment>> ENCHANTMENT_KEY;
   public static final RegistryKey<Registry<EntityType<?>>> ENTITY_TYPE_KEY;
   public static final RegistryKey<Registry<Item>> ITEM_KEY;
   public static final RegistryKey<Registry<Potion>> POTION_KEY;
   public static final RegistryKey<Registry<ParticleType<?>>> PARTICLE_TYPE_KEY;
   public static final RegistryKey<Registry<BlockEntityType<?>>> BLOCK_ENTITY_TYPE_KEY;
   public static final RegistryKey<Registry<PaintingMotive>> MOTIVE_KEY;
   public static final RegistryKey<Registry<Identifier>> CUSTOM_STAT_KEY;
   public static final RegistryKey<Registry<ChunkStatus>> CHUNK_STATUS_KEY;
   public static final RegistryKey<Registry<RuleTestType<?>>> RULE_TEST_KEY;
   public static final RegistryKey<Registry<PosRuleTestType<?>>> POS_RULE_TEST_KEY;
   public static final RegistryKey<Registry<ScreenHandlerType<?>>> MENU_KEY;
   public static final RegistryKey<Registry<RecipeType<?>>> RECIPE_TYPE_KEY;
   public static final RegistryKey<Registry<RecipeSerializer<?>>> RECIPE_SERIALIZER_KEY;
   public static final RegistryKey<Registry<EntityAttribute>> ATTRIBUTE_KEY;
   public static final RegistryKey<Registry<StatType<?>>> STAT_TYPE_KEY;
   public static final RegistryKey<Registry<VillagerType>> VILLAGER_TYPE_KEY;
   public static final RegistryKey<Registry<VillagerProfession>> VILLAGER_PROFESSION_KEY;
   public static final RegistryKey<Registry<PointOfInterestType>> POINT_OF_INTEREST_TYPE_KEY;
   public static final RegistryKey<Registry<MemoryModuleType<?>>> MEMORY_MODULE_TYPE_KEY;
   public static final RegistryKey<Registry<SensorType<?>>> SENSOR_TYPE_KEY;
   public static final RegistryKey<Registry<Schedule>> SCHEDULE_KEY;
   public static final RegistryKey<Registry<Activity>> ACTIVITY_KEY;
   public static final RegistryKey<Registry<LootPoolEntryType>> LOOT_POOL_ENTRY_TYPE_KEY;
   public static final RegistryKey<Registry<LootFunctionType>> LOOT_FUNCTION_TYPE_KEY;
   public static final RegistryKey<Registry<LootConditionType>> LOOT_CONDITION_TYPE_KEY;
   public static final RegistryKey<Registry<DimensionType>> DIMENSION_TYPE_KEY;
   public static final RegistryKey<Registry<World>> DIMENSION;
   public static final RegistryKey<Registry<DimensionOptions>> DIMENSION_OPTIONS;
   public static final Registry<SoundEvent> SOUND_EVENT;
   public static final DefaultedRegistry<Fluid> FLUID;
   public static final Registry<StatusEffect> STATUS_EFFECT;
   public static final DefaultedRegistry<Block> BLOCK;
   public static final Registry<Enchantment> ENCHANTMENT;
   public static final DefaultedRegistry<EntityType<?>> ENTITY_TYPE;
   public static final DefaultedRegistry<Item> ITEM;
   public static final DefaultedRegistry<Potion> POTION;
   public static final Registry<ParticleType<?>> PARTICLE_TYPE;
   public static final Registry<BlockEntityType<?>> BLOCK_ENTITY_TYPE;
   public static final DefaultedRegistry<PaintingMotive> PAINTING_MOTIVE;
   public static final Registry<Identifier> CUSTOM_STAT;
   public static final DefaultedRegistry<ChunkStatus> CHUNK_STATUS;
   public static final Registry<RuleTestType<?>> RULE_TEST;
   public static final Registry<PosRuleTestType<?>> POS_RULE_TEST;
   public static final Registry<ScreenHandlerType<?>> SCREEN_HANDLER;
   public static final Registry<RecipeType<?>> RECIPE_TYPE;
   public static final Registry<RecipeSerializer<?>> RECIPE_SERIALIZER;
   public static final Registry<EntityAttribute> ATTRIBUTE;
   public static final Registry<StatType<?>> STAT_TYPE;
   public static final DefaultedRegistry<VillagerType> VILLAGER_TYPE;
   public static final DefaultedRegistry<VillagerProfession> VILLAGER_PROFESSION;
   public static final DefaultedRegistry<PointOfInterestType> POINT_OF_INTEREST_TYPE;
   public static final DefaultedRegistry<MemoryModuleType<?>> MEMORY_MODULE_TYPE;
   public static final DefaultedRegistry<SensorType<?>> SENSOR_TYPE;
   public static final Registry<Schedule> SCHEDULE;
   public static final Registry<Activity> ACTIVITY;
   public static final Registry<LootPoolEntryType> LOOT_POOL_ENTRY_TYPE;
   public static final Registry<LootFunctionType> LOOT_FUNCTION_TYPE;
   public static final Registry<LootConditionType> LOOT_CONDITION_TYPE;
   public static final RegistryKey<Registry<ChunkGeneratorSettings>> NOISE_SETTINGS_WORLDGEN;
   public static final RegistryKey<Registry<ConfiguredSurfaceBuilder<?>>> CONFIGURED_SURFACE_BUILDER_WORLDGEN;
   public static final RegistryKey<Registry<ConfiguredCarver<?>>> CONFIGURED_CARVER_WORLDGEN;
   public static final RegistryKey<Registry<ConfiguredFeature<?, ?>>> CONFIGURED_FEATURE_WORLDGEN;
   public static final RegistryKey<Registry<ConfiguredStructureFeature<?, ?>>> CONFIGURED_STRUCTURE_FEATURE_WORLDGEN;
   public static final RegistryKey<Registry<StructureProcessorList>> PROCESSOR_LIST_WORLDGEN;
   public static final RegistryKey<Registry<StructurePool>> TEMPLATE_POOL_WORLDGEN;
   public static final RegistryKey<Registry<Biome>> BIOME_KEY;
   public static final RegistryKey<Registry<SurfaceBuilder<?>>> SURFACE_BUILD_KEY;
   public static final Registry<SurfaceBuilder<?>> SURFACE_BUILDER;
   public static final RegistryKey<Registry<Carver<?>>> CARVER_KEY;
   public static final Registry<Carver<?>> CARVER;
   public static final RegistryKey<Registry<Feature<?>>> FEATURE_KEY;
   public static final Registry<Feature<?>> FEATURE;
   public static final RegistryKey<Registry<StructureFeature<?>>> STRUCTURE_FEATURE_KEY;
   public static final Registry<StructureFeature<?>> STRUCTURE_FEATURE;
   public static final RegistryKey<Registry<StructurePieceType>> STRUCTURE_PIECE_KEY;
   public static final Registry<StructurePieceType> STRUCTURE_PIECE;
   public static final RegistryKey<Registry<Decorator<?>>> DECORATOR_KEY;
   public static final Registry<Decorator<?>> DECORATOR;
   public static final RegistryKey<Registry<BlockStateProviderType<?>>> BLOCK_STATE_PROVIDER_TYPE_KEY;
   public static final RegistryKey<Registry<BlockPlacerType<?>>> BLOCK_PLACER_TYPE_KEY;
   public static final RegistryKey<Registry<FoliagePlacerType<?>>> FOLIAGE_PLACER_TYPE_KEY;
   public static final RegistryKey<Registry<TrunkPlacerType<?>>> TRUNK_PLACER_TYPE_KEY;
   public static final RegistryKey<Registry<TreeDecoratorType<?>>> TREE_DECORATOR_TYPE_KEY;
   public static final RegistryKey<Registry<FeatureSizeType<?>>> FEATURE_SIZE_TYPE_KEY;
   public static final RegistryKey<Registry<Codec<? extends BiomeSource>>> BIOME_SOURCE_KEY;
   public static final RegistryKey<Registry<Codec<? extends ChunkGenerator>>> CHUNK_GENERATOR_KEY;
   public static final RegistryKey<Registry<StructureProcessorType<?>>> STRUCTURE_PROCESSOR_KEY;
   public static final RegistryKey<Registry<StructurePoolElementType<?>>> STRUCTURE_POOL_ELEMENT_KEY;
   public static final Registry<BlockStateProviderType<?>> BLOCK_STATE_PROVIDER_TYPE;
   public static final Registry<BlockPlacerType<?>> BLOCK_PLACER_TYPE;
   public static final Registry<FoliagePlacerType<?>> FOLIAGE_PLACER_TYPE;
   public static final Registry<TrunkPlacerType<?>> TRUNK_PLACER_TYPE;
   public static final Registry<TreeDecoratorType<?>> TREE_DECORATOR_TYPE;
   public static final Registry<FeatureSizeType<?>> FEATURE_SIZE_TYPE;
   public static final Registry<Codec<? extends BiomeSource>> BIOME_SOURCE;
   public static final Registry<Codec<? extends ChunkGenerator>> CHUNK_GENERATOR;
   public static final Registry<StructureProcessorType<?>> STRUCTURE_PROCESSOR;
   public static final Registry<StructurePoolElementType<?>> STRUCTURE_POOL_ELEMENT;
   /**
    * The {@linkplain RegistryKey} representing the ID of the actual registry.
    */
   private final RegistryKey<? extends Registry<T>> registryKey;
   private final Lifecycle lifecycle;

   private static <T> RegistryKey<Registry<T>> createRegistryKey(String registryId) {
      return RegistryKey.ofRegistry(new Identifier(registryId));
   }

   public static <T extends MutableRegistry<?>> void validate(MutableRegistry<T> registry) {
      registry.forEach((mutableRegistry2) -> {
         if (mutableRegistry2.getIds().isEmpty()) {
            LOGGER.error((String)"Registry '{}' was empty after loading", (Object)registry.getId(mutableRegistry2));
            if (SharedConstants.isDevelopment) {
               throw new IllegalStateException("Registry: '" + registry.getId(mutableRegistry2) + "' is empty, not allowed, fix me!");
            }
         }

         if (mutableRegistry2 instanceof DefaultedRegistry) {
            Identifier identifier = ((DefaultedRegistry)mutableRegistry2).getDefaultId();
            Validate.notNull(mutableRegistry2.get(identifier), "Missing default of DefaultedMappedRegistry: " + identifier);
         }

      });
   }

   private static <T> Registry<T> create(RegistryKey<? extends Registry<T>> registryKey, Supplier<T> defaultEntry) {
      return create(registryKey, Lifecycle.experimental(), defaultEntry);
   }

   private static <T> DefaultedRegistry<T> create(RegistryKey<? extends Registry<T>> registryKey, String defaultId, Supplier<T> defaultEntry) {
      return create(registryKey, defaultId, Lifecycle.experimental(), defaultEntry);
   }

   private static <T> Registry<T> create(RegistryKey<? extends Registry<T>> registryKey, Lifecycle lifecycle, Supplier<T> defaultEntry) {
      return create(registryKey, (MutableRegistry)(new SimpleRegistry(registryKey, lifecycle)), (Supplier)defaultEntry, (Lifecycle)lifecycle);
   }

   private static <T> DefaultedRegistry<T> create(RegistryKey<? extends Registry<T>> registryKey, String defaultId, Lifecycle lifecycle, Supplier<T> defaultEntry) {
      return (DefaultedRegistry)create(registryKey, (MutableRegistry)(new DefaultedRegistry(defaultId, registryKey, lifecycle)), (Supplier)defaultEntry, (Lifecycle)lifecycle);
   }

   private static <T, R extends MutableRegistry<T>> R create(RegistryKey<? extends Registry<T>> registryKey, R registry, Supplier<T> defaultEntry, Lifecycle lifecycle) {
      Identifier identifier = registryKey.getValue();
      DEFAULT_ENTRIES.put(identifier, defaultEntry);
      MutableRegistry<R> mutableRegistry = ROOT;
      return (MutableRegistry)mutableRegistry.add(registryKey, registry, lifecycle);
   }

   protected Registry(RegistryKey<? extends Registry<T>> key, Lifecycle lifecycle) {
      this.registryKey = key;
      this.lifecycle = lifecycle;
   }

   public RegistryKey<? extends Registry<T>> getKey() {
      return this.registryKey;
   }

   public String toString() {
      return "Registry[" + this.registryKey + " (" + this.lifecycle + ")]";
   }

   public <U> DataResult<Pair<T, U>> decode(DynamicOps<U> dynamicOps, U object) {
      return dynamicOps.compressMaps() ? dynamicOps.getNumberValue(object).flatMap((number) -> {
         T object = this.get(number.intValue());
         return object == null ? DataResult.error("Unknown registry id: " + number) : DataResult.success(object, this.getEntryLifecycle(object));
      }).map((objectx) -> {
         return Pair.of(objectx, dynamicOps.empty());
      }) : Identifier.CODEC.decode(dynamicOps, object).flatMap((pair) -> {
         T object = this.get((Identifier)pair.getFirst());
         return object == null ? DataResult.error("Unknown registry key: " + pair.getFirst()) : DataResult.success(Pair.of(object, pair.getSecond()), this.getEntryLifecycle(object));
      });
   }

   public <U> DataResult<U> encode(T object, DynamicOps<U> dynamicOps, U object2) {
      Identifier identifier = this.getId(object);
      if (identifier == null) {
         return DataResult.error("Unknown registry element " + object);
      } else {
         return dynamicOps.compressMaps() ? dynamicOps.mergeToPrimitive(object2, dynamicOps.createInt(this.getRawId(object))).setLifecycle(this.lifecycle) : dynamicOps.mergeToPrimitive(object2, dynamicOps.createString(identifier.toString())).setLifecycle(this.lifecycle);
      }
   }

   public <U> Stream<U> keys(DynamicOps<U> dynamicOps) {
      return this.getIds().stream().map((identifier) -> {
         return dynamicOps.createString(identifier.toString());
      });
   }

   @Nullable
   public abstract Identifier getId(T entry);

   public abstract Optional<RegistryKey<T>> getKey(T entry);

   public abstract int getRawId(@Nullable T entry);

   @Nullable
   public abstract T get(@Nullable RegistryKey<T> key);

   @Nullable
   public abstract T get(@Nullable Identifier id);

   /**
    * Gets the lifecycle of a registry entry.
    */
   protected abstract Lifecycle getEntryLifecycle(T object);

   public abstract Lifecycle getLifecycle();

   public Optional<T> getOrEmpty(@Nullable Identifier id) {
      return Optional.ofNullable(this.get(id));
   }

   @Environment(EnvType.CLIENT)
   public Optional<T> getOrEmpty(@Nullable RegistryKey<T> key) {
      return Optional.ofNullable(this.get(key));
   }

   /**
    * Gets an entry from the registry.
    * 
    * @throws IllegalStateException if the entry was not present in the registry
    */
   public T getOrThrow(RegistryKey<T> key) {
      T object = this.get(key);
      if (object == null) {
         throw new IllegalStateException("Missing: " + key);
      } else {
         return object;
      }
   }

   public abstract Set<Identifier> getIds();

   public abstract Set<Entry<RegistryKey<T>, T>> getEntries();

   public Stream<T> stream() {
      return StreamSupport.stream(this.spliterator(), false);
   }

   @Environment(EnvType.CLIENT)
   public abstract boolean containsId(Identifier id);

   public static <T> T register(Registry<? super T> registry, String id, T entry) {
      return register(registry, new Identifier(id), entry);
   }

   public static <V, T extends V> T register(Registry<V> registry, Identifier id, T entry) {
      return ((MutableRegistry)registry).add(RegistryKey.of(registry.registryKey, id), entry, Lifecycle.stable());
   }

   public static <V, T extends V> T register(Registry<V> registry, int rawId, String id, T entry) {
      return ((MutableRegistry)registry).set(rawId, RegistryKey.of(registry.registryKey, new Identifier(id)), entry, Lifecycle.stable());
   }

   static {
      REGISTRIES = ROOT;
      SOUND_EVENT_KEY = createRegistryKey("sound_event");
      FLUID_KEY = createRegistryKey("fluid");
      MOB_EFFECT_KEY = createRegistryKey("mob_effect");
      BLOCK_KEY = createRegistryKey("block");
      ENCHANTMENT_KEY = createRegistryKey("enchantment");
      ENTITY_TYPE_KEY = createRegistryKey("entity_type");
      ITEM_KEY = createRegistryKey("item");
      POTION_KEY = createRegistryKey("potion");
      PARTICLE_TYPE_KEY = createRegistryKey("particle_type");
      BLOCK_ENTITY_TYPE_KEY = createRegistryKey("block_entity_type");
      MOTIVE_KEY = createRegistryKey("motive");
      CUSTOM_STAT_KEY = createRegistryKey("custom_stat");
      CHUNK_STATUS_KEY = createRegistryKey("chunk_status");
      RULE_TEST_KEY = createRegistryKey("rule_test");
      POS_RULE_TEST_KEY = createRegistryKey("pos_rule_test");
      MENU_KEY = createRegistryKey("menu");
      RECIPE_TYPE_KEY = createRegistryKey("recipe_type");
      RECIPE_SERIALIZER_KEY = createRegistryKey("recipe_serializer");
      ATTRIBUTE_KEY = createRegistryKey("attribute");
      STAT_TYPE_KEY = createRegistryKey("stat_type");
      VILLAGER_TYPE_KEY = createRegistryKey("villager_type");
      VILLAGER_PROFESSION_KEY = createRegistryKey("villager_profession");
      POINT_OF_INTEREST_TYPE_KEY = createRegistryKey("point_of_interest_type");
      MEMORY_MODULE_TYPE_KEY = createRegistryKey("memory_module_type");
      SENSOR_TYPE_KEY = createRegistryKey("sensor_type");
      SCHEDULE_KEY = createRegistryKey("schedule");
      ACTIVITY_KEY = createRegistryKey("activity");
      LOOT_POOL_ENTRY_TYPE_KEY = createRegistryKey("loot_pool_entry_type");
      LOOT_FUNCTION_TYPE_KEY = createRegistryKey("loot_function_type");
      LOOT_CONDITION_TYPE_KEY = createRegistryKey("loot_condition_type");
      DIMENSION_TYPE_KEY = createRegistryKey("dimension_type");
      DIMENSION = createRegistryKey("dimension");
      DIMENSION_OPTIONS = createRegistryKey("dimension");
      SOUND_EVENT = create(SOUND_EVENT_KEY, () -> {
         return SoundEvents.ENTITY_ITEM_PICKUP;
      });
      FLUID = create(FLUID_KEY, "empty", () -> {
         return Fluids.EMPTY;
      });
      STATUS_EFFECT = create(MOB_EFFECT_KEY, () -> {
         return StatusEffects.LUCK;
      });
      BLOCK = create(BLOCK_KEY, "air", () -> {
         return Blocks.AIR;
      });
      ENCHANTMENT = create(ENCHANTMENT_KEY, () -> {
         return Enchantments.FORTUNE;
      });
      ENTITY_TYPE = create(ENTITY_TYPE_KEY, "pig", () -> {
         return EntityType.PIG;
      });
      ITEM = create(ITEM_KEY, "air", () -> {
         return Items.AIR;
      });
      POTION = create(POTION_KEY, "empty", () -> {
         return Potions.EMPTY;
      });
      PARTICLE_TYPE = create(PARTICLE_TYPE_KEY, () -> {
         return ParticleTypes.BLOCK;
      });
      BLOCK_ENTITY_TYPE = create(BLOCK_ENTITY_TYPE_KEY, () -> {
         return BlockEntityType.FURNACE;
      });
      PAINTING_MOTIVE = create(MOTIVE_KEY, "kebab", () -> {
         return PaintingMotive.KEBAB;
      });
      CUSTOM_STAT = create(CUSTOM_STAT_KEY, () -> {
         return Stats.JUMP;
      });
      CHUNK_STATUS = create(CHUNK_STATUS_KEY, "empty", () -> {
         return ChunkStatus.EMPTY;
      });
      RULE_TEST = create(RULE_TEST_KEY, () -> {
         return RuleTestType.ALWAYS_TRUE;
      });
      POS_RULE_TEST = create(POS_RULE_TEST_KEY, () -> {
         return PosRuleTestType.ALWAYS_TRUE;
      });
      SCREEN_HANDLER = create(MENU_KEY, () -> {
         return ScreenHandlerType.ANVIL;
      });
      RECIPE_TYPE = create(RECIPE_TYPE_KEY, () -> {
         return RecipeType.CRAFTING;
      });
      RECIPE_SERIALIZER = create(RECIPE_SERIALIZER_KEY, () -> {
         return RecipeSerializer.SHAPELESS;
      });
      ATTRIBUTE = create(ATTRIBUTE_KEY, () -> {
         return EntityAttributes.GENERIC_LUCK;
      });
      STAT_TYPE = create(STAT_TYPE_KEY, () -> {
         return Stats.USED;
      });
      VILLAGER_TYPE = create(VILLAGER_TYPE_KEY, "plains", () -> {
         return VillagerType.PLAINS;
      });
      VILLAGER_PROFESSION = create(VILLAGER_PROFESSION_KEY, "none", () -> {
         return VillagerProfession.NONE;
      });
      POINT_OF_INTEREST_TYPE = create(POINT_OF_INTEREST_TYPE_KEY, "unemployed", () -> {
         return PointOfInterestType.UNEMPLOYED;
      });
      MEMORY_MODULE_TYPE = create(MEMORY_MODULE_TYPE_KEY, "dummy", () -> {
         return MemoryModuleType.DUMMY;
      });
      SENSOR_TYPE = create(SENSOR_TYPE_KEY, "dummy", () -> {
         return SensorType.DUMMY;
      });
      SCHEDULE = create(SCHEDULE_KEY, () -> {
         return Schedule.EMPTY;
      });
      ACTIVITY = create(ACTIVITY_KEY, () -> {
         return Activity.IDLE;
      });
      LOOT_POOL_ENTRY_TYPE = create(LOOT_POOL_ENTRY_TYPE_KEY, () -> {
         return LootPoolEntryTypes.EMPTY;
      });
      LOOT_FUNCTION_TYPE = create(LOOT_FUNCTION_TYPE_KEY, () -> {
         return LootFunctionTypes.SET_COUNT;
      });
      LOOT_CONDITION_TYPE = create(LOOT_CONDITION_TYPE_KEY, () -> {
         return LootConditionTypes.INVERTED;
      });
      NOISE_SETTINGS_WORLDGEN = createRegistryKey("worldgen/noise_settings");
      CONFIGURED_SURFACE_BUILDER_WORLDGEN = createRegistryKey("worldgen/configured_surface_builder");
      CONFIGURED_CARVER_WORLDGEN = createRegistryKey("worldgen/configured_carver");
      CONFIGURED_FEATURE_WORLDGEN = createRegistryKey("worldgen/configured_feature");
      CONFIGURED_STRUCTURE_FEATURE_WORLDGEN = createRegistryKey("worldgen/configured_structure_feature");
      PROCESSOR_LIST_WORLDGEN = createRegistryKey("worldgen/processor_list");
      TEMPLATE_POOL_WORLDGEN = createRegistryKey("worldgen/template_pool");
      BIOME_KEY = createRegistryKey("worldgen/biome");
      SURFACE_BUILD_KEY = createRegistryKey("worldgen/surface_builder");
      SURFACE_BUILDER = create(SURFACE_BUILD_KEY, () -> {
         return SurfaceBuilder.DEFAULT;
      });
      CARVER_KEY = createRegistryKey("worldgen/carver");
      CARVER = create(CARVER_KEY, () -> {
         return Carver.CAVE;
      });
      FEATURE_KEY = createRegistryKey("worldgen/feature");
      FEATURE = create(FEATURE_KEY, () -> {
         return Feature.ORE;
      });
      STRUCTURE_FEATURE_KEY = createRegistryKey("worldgen/structure_feature");
      STRUCTURE_FEATURE = create(STRUCTURE_FEATURE_KEY, () -> {
         return StructureFeature.MINESHAFT;
      });
      STRUCTURE_PIECE_KEY = createRegistryKey("worldgen/structure_piece");
      STRUCTURE_PIECE = create(STRUCTURE_PIECE_KEY, () -> {
         return StructurePieceType.MINESHAFT_ROOM;
      });
      DECORATOR_KEY = createRegistryKey("worldgen/decorator");
      DECORATOR = create(DECORATOR_KEY, () -> {
         return Decorator.NOPE;
      });
      BLOCK_STATE_PROVIDER_TYPE_KEY = createRegistryKey("worldgen/block_state_provider_type");
      BLOCK_PLACER_TYPE_KEY = createRegistryKey("worldgen/block_placer_type");
      FOLIAGE_PLACER_TYPE_KEY = createRegistryKey("worldgen/foliage_placer_type");
      TRUNK_PLACER_TYPE_KEY = createRegistryKey("worldgen/trunk_placer_type");
      TREE_DECORATOR_TYPE_KEY = createRegistryKey("worldgen/tree_decorator_type");
      FEATURE_SIZE_TYPE_KEY = createRegistryKey("worldgen/feature_size_type");
      BIOME_SOURCE_KEY = createRegistryKey("worldgen/biome_source");
      CHUNK_GENERATOR_KEY = createRegistryKey("worldgen/chunk_generator");
      STRUCTURE_PROCESSOR_KEY = createRegistryKey("worldgen/structure_processor");
      STRUCTURE_POOL_ELEMENT_KEY = createRegistryKey("worldgen/structure_pool_element");
      BLOCK_STATE_PROVIDER_TYPE = create(BLOCK_STATE_PROVIDER_TYPE_KEY, () -> {
         return BlockStateProviderType.SIMPLE_STATE_PROVIDER;
      });
      BLOCK_PLACER_TYPE = create(BLOCK_PLACER_TYPE_KEY, () -> {
         return BlockPlacerType.SIMPLE_BLOCK_PLACER;
      });
      FOLIAGE_PLACER_TYPE = create(FOLIAGE_PLACER_TYPE_KEY, () -> {
         return FoliagePlacerType.BLOB_FOLIAGE_PLACER;
      });
      TRUNK_PLACER_TYPE = create(TRUNK_PLACER_TYPE_KEY, () -> {
         return TrunkPlacerType.STRAIGHT_TRUNK_PLACER;
      });
      TREE_DECORATOR_TYPE = create(TREE_DECORATOR_TYPE_KEY, () -> {
         return TreeDecoratorType.LEAVE_VINE;
      });
      FEATURE_SIZE_TYPE = create(FEATURE_SIZE_TYPE_KEY, () -> {
         return FeatureSizeType.TWO_LAYERS_FEATURE_SIZE;
      });
      BIOME_SOURCE = create(BIOME_SOURCE_KEY, Lifecycle.stable(), () -> {
         return BiomeSource.CODEC;
      });
      CHUNK_GENERATOR = create(CHUNK_GENERATOR_KEY, Lifecycle.stable(), () -> {
         return ChunkGenerator.CODEC;
      });
      STRUCTURE_PROCESSOR = create(STRUCTURE_PROCESSOR_KEY, () -> {
         return StructureProcessorType.BLOCK_IGNORE;
      });
      STRUCTURE_POOL_ELEMENT = create(STRUCTURE_POOL_ELEMENT_KEY, () -> {
         return StructurePoolElementType.EMPTY_POOL_ELEMENT;
      });
      BuiltinRegistries.init();
      DEFAULT_ENTRIES.forEach((identifier, supplier) -> {
         if (supplier.get() == null) {
            LOGGER.error((String)"Unable to bootstrap registry '{}'", (Object)identifier);
         }

      });
      validate(ROOT);
   }
}
