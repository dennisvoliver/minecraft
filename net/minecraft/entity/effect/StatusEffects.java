package net.minecraft.entity.effect;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.Difficulty;

public class StatusEffects {
   public static final StatusEffect SPEED;
   public static final StatusEffect SLOWNESS;
   public static final StatusEffect HASTE;
   public static final StatusEffect MINING_FATIGUE;
   public static final StatusEffect STRENGTH;
   public static final StatusEffect INSTANT_HEALTH;
   public static final StatusEffect INSTANT_DAMAGE;
   public static final StatusEffect JUMP_BOOST;
   public static final StatusEffect NAUSEA;
   public static final StatusEffect REGENERATION;
   public static final StatusEffect RESISTANCE;
   public static final StatusEffect FIRE_RESISTANCE;
   public static final StatusEffect WATER_BREATHING;
   public static final StatusEffect INVISIBILITY;
   public static final StatusEffect BLINDNESS;
   public static final StatusEffect NIGHT_VISION;
   public static final StatusEffect HUNGER;
   public static final StatusEffect WEAKNESS;
   public static final StatusEffect POISON;
   public static final StatusEffect WITHER;
   public static final StatusEffect HEALTH_BOOST;
   public static final StatusEffect ABSORPTION;
   public static final StatusEffect SATURATION;
   public static final StatusEffect GLOWING;
   public static final StatusEffect LEVITATION;
   public static final StatusEffect LUCK;
   public static final StatusEffect UNLUCK;
   public static final StatusEffect SLOW_FALLING;
   public static final StatusEffect CONDUIT_POWER;
   public static final StatusEffect DOLPHINS_GRACE;
   public static final StatusEffect BAD_OMEN;
   public static final StatusEffect HERO_OF_THE_VILLAGE;

   private static StatusEffect register(int rawId, String id, StatusEffect entry) {
      return (StatusEffect)Registry.register(Registry.STATUS_EFFECT, rawId, id, entry);
   }

   static {
      SPEED = register(1, "speed", (new StatusEffect(StatusEffectType.BENEFICIAL, 8171462)).addAttributeModifier(EntityAttributes.GENERIC_MOVEMENT_SPEED, "91AEAA56-376B-4498-935B-2F7F68070635", 0.20000000298023224D, EntityAttributeModifier.Operation.MULTIPLY_TOTAL));
      SLOWNESS = register(2, "slowness", (new StatusEffect(StatusEffectType.HARMFUL, 5926017)).addAttributeModifier(EntityAttributes.GENERIC_MOVEMENT_SPEED, "7107DE5E-7CE8-4030-940E-514C1F160890", -0.15000000596046448D, EntityAttributeModifier.Operation.MULTIPLY_TOTAL));
      HASTE = register(3, "haste", (new StatusEffect(StatusEffectType.BENEFICIAL, 14270531)).addAttributeModifier(EntityAttributes.GENERIC_ATTACK_SPEED, "AF8B6E3F-3328-4C0A-AA36-5BA2BB9DBEF3", 0.10000000149011612D, EntityAttributeModifier.Operation.MULTIPLY_TOTAL));
      MINING_FATIGUE = register(4, "mining_fatigue", (new StatusEffect(StatusEffectType.HARMFUL, 4866583)).addAttributeModifier(EntityAttributes.GENERIC_ATTACK_SPEED, "55FCED67-E92A-486E-9800-B47F202C4386", -0.10000000149011612D, EntityAttributeModifier.Operation.MULTIPLY_TOTAL));
      STRENGTH = register(5, "strength", (new DamageModifierStatusEffect(StatusEffectType.BENEFICIAL, 9643043, 3.0D)).addAttributeModifier(EntityAttributes.GENERIC_ATTACK_DAMAGE, "648D7064-6A60-4F59-8ABE-C2C23A6DD7A9", 0.0D, EntityAttributeModifier.Operation.ADDITION));
      INSTANT_HEALTH = register(6, "instant_health", new InstantStatusEffect(StatusEffectType.BENEFICIAL, 16262179));
      INSTANT_DAMAGE = register(7, "instant_damage", new InstantStatusEffect(StatusEffectType.HARMFUL, 4393481));
      JUMP_BOOST = register(8, "jump_boost", new StatusEffect(StatusEffectType.BENEFICIAL, 2293580));
      NAUSEA = register(9, "nausea", new StatusEffect(StatusEffectType.HARMFUL, 5578058));
      REGENERATION = register(10, "regeneration", new StatusEffect(StatusEffectType.BENEFICIAL, 13458603));
      RESISTANCE = register(11, "resistance", new StatusEffect(StatusEffectType.BENEFICIAL, 10044730));
      FIRE_RESISTANCE = register(12, "fire_resistance", new StatusEffect(StatusEffectType.BENEFICIAL, 14981690));
      WATER_BREATHING = register(13, "water_breathing", new StatusEffect(StatusEffectType.BENEFICIAL, 3035801));
      INVISIBILITY = register(14, "invisibility", new StatusEffect(StatusEffectType.BENEFICIAL, 8356754));
      BLINDNESS = register(15, "blindness", new StatusEffect(StatusEffectType.HARMFUL, 2039587));
      NIGHT_VISION = register(16, "night_vision", new StatusEffect(StatusEffectType.BENEFICIAL, 2039713));
      HUNGER = register(17, "hunger", new StatusEffect(StatusEffectType.HARMFUL, 5797459));
      WEAKNESS = register(18, "weakness", (new DamageModifierStatusEffect(StatusEffectType.HARMFUL, 4738376, -4.0D)).addAttributeModifier(EntityAttributes.GENERIC_ATTACK_DAMAGE, "22653B89-116E-49DC-9B6B-9971489B5BE5", 0.0D, EntityAttributeModifier.Operation.ADDITION));
      POISON = register(19, "poison", new StatusEffect(StatusEffectType.HARMFUL, 5149489));
      WITHER = register(20, "wither", new StatusEffect(StatusEffectType.HARMFUL, 3484199));
      HEALTH_BOOST = register(21, "health_boost", (new HealthBoostStatusEffect(StatusEffectType.BENEFICIAL, 16284963)).addAttributeModifier(EntityAttributes.GENERIC_MAX_HEALTH, "5D6F0BA2-1186-46AC-B896-C61C5CEE99CC", 4.0D, EntityAttributeModifier.Operation.ADDITION));
      ABSORPTION = register(22, "absorption", new AbsorptionStatusEffect(StatusEffectType.BENEFICIAL, 2445989));
      SATURATION = register(23, "saturation", new InstantStatusEffect(StatusEffectType.BENEFICIAL, 16262179));
      GLOWING = register(24, "glowing", new StatusEffect(StatusEffectType.NEUTRAL, 9740385));
      LEVITATION = register(25, "levitation", new StatusEffect(StatusEffectType.HARMFUL, 13565951));
      LUCK = register(26, "luck", (new StatusEffect(StatusEffectType.BENEFICIAL, 3381504)).addAttributeModifier(EntityAttributes.GENERIC_LUCK, "03C3C89D-7037-4B42-869F-B146BCB64D2E", 1.0D, EntityAttributeModifier.Operation.ADDITION));
      UNLUCK = register(27, "unluck", (new StatusEffect(StatusEffectType.HARMFUL, 12624973)).addAttributeModifier(EntityAttributes.GENERIC_LUCK, "CC5AF142-2BD2-4215-B636-2605AED11727", -1.0D, EntityAttributeModifier.Operation.ADDITION));
      SLOW_FALLING = register(28, "slow_falling", new StatusEffect(StatusEffectType.BENEFICIAL, 16773073));
      CONDUIT_POWER = register(29, "conduit_power", new StatusEffect(StatusEffectType.BENEFICIAL, 1950417));
      DOLPHINS_GRACE = register(30, "dolphins_grace", new StatusEffect(StatusEffectType.BENEFICIAL, 8954814));
      BAD_OMEN = register(31, "bad_omen", new StatusEffect(StatusEffectType.NEUTRAL, 745784) {
         public boolean canApplyUpdateEffect(int duration, int amplifier) {
            return true;
         }

         public void applyUpdateEffect(LivingEntity entity, int amplifier) {
            if (entity instanceof ServerPlayerEntity && !entity.isSpectator()) {
               ServerPlayerEntity serverPlayerEntity = (ServerPlayerEntity)entity;
               ServerWorld serverWorld = serverPlayerEntity.getServerWorld();
               if (serverWorld.getDifficulty() == Difficulty.PEACEFUL) {
                  return;
               }

               if (serverWorld.isNearOccupiedPointOfInterest(entity.getBlockPos())) {
                  serverWorld.getRaidManager().startRaid(serverPlayerEntity);
               }
            }

         }
      });
      HERO_OF_THE_VILLAGE = register(32, "hero_of_the_village", new StatusEffect(StatusEffectType.BENEFICIAL, 4521796));
   }
}
