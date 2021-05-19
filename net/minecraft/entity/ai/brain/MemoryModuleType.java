package net.minecraft.entity.ai.brain;

import com.mojang.serialization.Codec;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.pathing.Path;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.AbstractPiglinEntity;
import net.minecraft.entity.mob.HoglinEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.dynamic.DynamicSerializableUuid;
import net.minecraft.util.dynamic.GlobalPos;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;

public class MemoryModuleType<U> {
   public static final MemoryModuleType<Void> DUMMY = register("dummy");
   public static final MemoryModuleType<GlobalPos> HOME;
   public static final MemoryModuleType<GlobalPos> JOB_SITE;
   public static final MemoryModuleType<GlobalPos> POTENTIAL_JOB_SITE;
   public static final MemoryModuleType<GlobalPos> MEETING_POINT;
   public static final MemoryModuleType<List<GlobalPos>> SECONDARY_JOB_SITE;
   public static final MemoryModuleType<List<LivingEntity>> MOBS;
   public static final MemoryModuleType<List<LivingEntity>> VISIBLE_MOBS;
   public static final MemoryModuleType<List<LivingEntity>> VISIBLE_VILLAGER_BABIES;
   public static final MemoryModuleType<List<PlayerEntity>> NEAREST_PLAYERS;
   public static final MemoryModuleType<PlayerEntity> NEAREST_VISIBLE_PLAYER;
   public static final MemoryModuleType<PlayerEntity> NEAREST_VISIBLE_TARGETABLE_PLAYER;
   public static final MemoryModuleType<WalkTarget> WALK_TARGET;
   public static final MemoryModuleType<LookTarget> LOOK_TARGET;
   public static final MemoryModuleType<LivingEntity> ATTACK_TARGET;
   public static final MemoryModuleType<Boolean> ATTACK_COOLING_DOWN;
   public static final MemoryModuleType<LivingEntity> INTERACTION_TARGET;
   public static final MemoryModuleType<PassiveEntity> BREED_TARGET;
   public static final MemoryModuleType<Entity> RIDE_TARGET;
   public static final MemoryModuleType<Path> PATH;
   public static final MemoryModuleType<List<GlobalPos>> INTERACTABLE_DOORS;
   public static final MemoryModuleType<Set<GlobalPos>> DOORS_TO_CLOSE;
   public static final MemoryModuleType<BlockPos> NEAREST_BED;
   public static final MemoryModuleType<DamageSource> HURT_BY;
   public static final MemoryModuleType<LivingEntity> HURT_BY_ENTITY;
   public static final MemoryModuleType<LivingEntity> AVOID_TARGET;
   public static final MemoryModuleType<LivingEntity> NEAREST_HOSTILE;
   public static final MemoryModuleType<GlobalPos> HIDING_PLACE;
   public static final MemoryModuleType<Long> HEARD_BELL_TIME;
   public static final MemoryModuleType<Long> CANT_REACH_WALK_TARGET_SINCE;
   public static final MemoryModuleType<Boolean> GOLEM_DETECTED_RECENTLY;
   public static final MemoryModuleType<Long> LAST_SLEPT;
   public static final MemoryModuleType<Long> LAST_WOKEN;
   public static final MemoryModuleType<Long> LAST_WORKED_AT_POI;
   public static final MemoryModuleType<PassiveEntity> NEAREST_VISIBLE_ADULT;
   public static final MemoryModuleType<ItemEntity> NEAREST_VISIBLE_WANTED_ITEM;
   public static final MemoryModuleType<MobEntity> NEAREST_VISIBLE_NEMESIS;
   public static final MemoryModuleType<UUID> ANGRY_AT;
   public static final MemoryModuleType<Boolean> UNIVERSAL_ANGER;
   public static final MemoryModuleType<Boolean> ADMIRING_ITEM;
   public static final MemoryModuleType<Integer> TIME_TRYING_TO_REACH_ADMIRE_ITEM;
   public static final MemoryModuleType<Boolean> DISABLE_WALK_TO_ADMIRE_ITEM;
   public static final MemoryModuleType<Boolean> ADMIRING_DISABLED;
   public static final MemoryModuleType<Boolean> HUNTED_RECENTLY;
   public static final MemoryModuleType<BlockPos> CELEBRATE_LOCATION;
   public static final MemoryModuleType<Boolean> DANCING;
   public static final MemoryModuleType<HoglinEntity> NEAREST_VISIBLE_HUNTABLE_HOGLIN;
   public static final MemoryModuleType<HoglinEntity> NEAREST_VISIBLE_BABY_HOGLIN;
   public static final MemoryModuleType<PlayerEntity> NEAREST_TARGETABLE_PLAYER_NOT_WEARING_GOLD;
   public static final MemoryModuleType<List<AbstractPiglinEntity>> NEARBY_ADULT_PIGLINS;
   public static final MemoryModuleType<List<AbstractPiglinEntity>> NEAREST_VISIBLE_ADULT_PIGLINS;
   public static final MemoryModuleType<List<HoglinEntity>> NEAREST_VISIBLE_ADULT_HOGLINS;
   public static final MemoryModuleType<AbstractPiglinEntity> NEAREST_VISIBLE_ADULT_PIGLIN;
   public static final MemoryModuleType<LivingEntity> NEAREST_VISIBLE_ZOMBIFIED;
   public static final MemoryModuleType<Integer> VISIBLE_ADULT_PIGLIN_COUNT;
   public static final MemoryModuleType<Integer> VISIBLE_ADULT_HOGLIN_COUNT;
   public static final MemoryModuleType<PlayerEntity> NEAREST_PLAYER_HOLDING_WANTED_ITEM;
   public static final MemoryModuleType<Boolean> ATE_RECENTLY;
   public static final MemoryModuleType<BlockPos> NEAREST_REPELLENT;
   public static final MemoryModuleType<Boolean> PACIFIED;
   private final Optional<Codec<Memory<U>>> codec;

   private MemoryModuleType(Optional<Codec<U>> codec) {
      this.codec = codec.map(Memory::createCodec);
   }

   public String toString() {
      return Registry.MEMORY_MODULE_TYPE.getId(this).toString();
   }

   public Optional<Codec<Memory<U>>> getCodec() {
      return this.codec;
   }

   private static <U> MemoryModuleType<U> register(String id, Codec<U> codec) {
      return (MemoryModuleType)Registry.register(Registry.MEMORY_MODULE_TYPE, (Identifier)(new Identifier(id)), new MemoryModuleType(Optional.of(codec)));
   }

   private static <U> MemoryModuleType<U> register(String id) {
      return (MemoryModuleType)Registry.register(Registry.MEMORY_MODULE_TYPE, (Identifier)(new Identifier(id)), new MemoryModuleType(Optional.empty()));
   }

   static {
      HOME = register("home", GlobalPos.CODEC);
      JOB_SITE = register("job_site", GlobalPos.CODEC);
      POTENTIAL_JOB_SITE = register("potential_job_site", GlobalPos.CODEC);
      MEETING_POINT = register("meeting_point", GlobalPos.CODEC);
      SECONDARY_JOB_SITE = register("secondary_job_site");
      MOBS = register("mobs");
      VISIBLE_MOBS = register("visible_mobs");
      VISIBLE_VILLAGER_BABIES = register("visible_villager_babies");
      NEAREST_PLAYERS = register("nearest_players");
      NEAREST_VISIBLE_PLAYER = register("nearest_visible_player");
      NEAREST_VISIBLE_TARGETABLE_PLAYER = register("nearest_visible_targetable_player");
      WALK_TARGET = register("walk_target");
      LOOK_TARGET = register("look_target");
      ATTACK_TARGET = register("attack_target");
      ATTACK_COOLING_DOWN = register("attack_cooling_down");
      INTERACTION_TARGET = register("interaction_target");
      BREED_TARGET = register("breed_target");
      RIDE_TARGET = register("ride_target");
      PATH = register("path");
      INTERACTABLE_DOORS = register("interactable_doors");
      DOORS_TO_CLOSE = register("doors_to_close");
      NEAREST_BED = register("nearest_bed");
      HURT_BY = register("hurt_by");
      HURT_BY_ENTITY = register("hurt_by_entity");
      AVOID_TARGET = register("avoid_target");
      NEAREST_HOSTILE = register("nearest_hostile");
      HIDING_PLACE = register("hiding_place");
      HEARD_BELL_TIME = register("heard_bell_time");
      CANT_REACH_WALK_TARGET_SINCE = register("cant_reach_walk_target_since");
      GOLEM_DETECTED_RECENTLY = register("golem_detected_recently", Codec.BOOL);
      LAST_SLEPT = register("last_slept", Codec.LONG);
      LAST_WOKEN = register("last_woken", Codec.LONG);
      LAST_WORKED_AT_POI = register("last_worked_at_poi", Codec.LONG);
      NEAREST_VISIBLE_ADULT = register("nearest_visible_adult");
      NEAREST_VISIBLE_WANTED_ITEM = register("nearest_visible_wanted_item");
      NEAREST_VISIBLE_NEMESIS = register("nearest_visible_nemesis");
      ANGRY_AT = register("angry_at", DynamicSerializableUuid.CODEC);
      UNIVERSAL_ANGER = register("universal_anger", Codec.BOOL);
      ADMIRING_ITEM = register("admiring_item", Codec.BOOL);
      TIME_TRYING_TO_REACH_ADMIRE_ITEM = register("time_trying_to_reach_admire_item");
      DISABLE_WALK_TO_ADMIRE_ITEM = register("disable_walk_to_admire_item");
      ADMIRING_DISABLED = register("admiring_disabled", Codec.BOOL);
      HUNTED_RECENTLY = register("hunted_recently", Codec.BOOL);
      CELEBRATE_LOCATION = register("celebrate_location");
      DANCING = register("dancing");
      NEAREST_VISIBLE_HUNTABLE_HOGLIN = register("nearest_visible_huntable_hoglin");
      NEAREST_VISIBLE_BABY_HOGLIN = register("nearest_visible_baby_hoglin");
      NEAREST_TARGETABLE_PLAYER_NOT_WEARING_GOLD = register("nearest_targetable_player_not_wearing_gold");
      NEARBY_ADULT_PIGLINS = register("nearby_adult_piglins");
      NEAREST_VISIBLE_ADULT_PIGLINS = register("nearest_visible_adult_piglins");
      NEAREST_VISIBLE_ADULT_HOGLINS = register("nearest_visible_adult_hoglins");
      NEAREST_VISIBLE_ADULT_PIGLIN = register("nearest_visible_adult_piglin");
      NEAREST_VISIBLE_ZOMBIFIED = register("nearest_visible_zombified");
      VISIBLE_ADULT_PIGLIN_COUNT = register("visible_adult_piglin_count");
      VISIBLE_ADULT_HOGLIN_COUNT = register("visible_adult_hoglin_count");
      NEAREST_PLAYER_HOLDING_WANTED_ITEM = register("nearest_player_holding_wanted_item");
      ATE_RECENTLY = register("ate_recently");
      NEAREST_REPELLENT = register("nearest_repellent");
      PACIFIED = register("pacified");
   }
}
