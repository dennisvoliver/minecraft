package net.minecraft.particle;

import com.mojang.serialization.Codec;
import java.util.function.Function;
import net.minecraft.util.registry.Registry;

public class ParticleTypes {
   public static final DefaultParticleType AMBIENT_ENTITY_EFFECT = register("ambient_entity_effect", false);
   public static final DefaultParticleType ANGRY_VILLAGER = register("angry_villager", false);
   public static final DefaultParticleType BARRIER = register("barrier", false);
   public static final ParticleType<BlockStateParticleEffect> BLOCK;
   public static final DefaultParticleType BUBBLE;
   public static final DefaultParticleType CLOUD;
   public static final DefaultParticleType CRIT;
   public static final DefaultParticleType DAMAGE_INDICATOR;
   public static final DefaultParticleType DRAGON_BREATH;
   public static final DefaultParticleType DRIPPING_LAVA;
   public static final DefaultParticleType FALLING_LAVA;
   public static final DefaultParticleType LANDING_LAVA;
   public static final DefaultParticleType DRIPPING_WATER;
   public static final DefaultParticleType FALLING_WATER;
   public static final ParticleType<DustParticleEffect> DUST;
   public static final DefaultParticleType EFFECT;
   public static final DefaultParticleType ELDER_GUARDIAN;
   public static final DefaultParticleType ENCHANTED_HIT;
   public static final DefaultParticleType ENCHANT;
   public static final DefaultParticleType END_ROD;
   public static final DefaultParticleType ENTITY_EFFECT;
   public static final DefaultParticleType EXPLOSION_EMITTER;
   public static final DefaultParticleType EXPLOSION;
   public static final ParticleType<BlockStateParticleEffect> FALLING_DUST;
   public static final DefaultParticleType FIREWORK;
   public static final DefaultParticleType FISHING;
   public static final DefaultParticleType FLAME;
   public static final DefaultParticleType SOUL_FIRE_FLAME;
   public static final DefaultParticleType SOUL;
   public static final DefaultParticleType FLASH;
   public static final DefaultParticleType HAPPY_VILLAGER;
   public static final DefaultParticleType COMPOSTER;
   public static final DefaultParticleType HEART;
   public static final DefaultParticleType INSTANT_EFFECT;
   public static final ParticleType<ItemStackParticleEffect> ITEM;
   public static final DefaultParticleType ITEM_SLIME;
   public static final DefaultParticleType ITEM_SNOWBALL;
   public static final DefaultParticleType LARGE_SMOKE;
   public static final DefaultParticleType LAVA;
   public static final DefaultParticleType MYCELIUM;
   public static final DefaultParticleType NOTE;
   public static final DefaultParticleType POOF;
   public static final DefaultParticleType PORTAL;
   public static final DefaultParticleType RAIN;
   public static final DefaultParticleType SMOKE;
   public static final DefaultParticleType SNEEZE;
   public static final DefaultParticleType SPIT;
   public static final DefaultParticleType SQUID_INK;
   public static final DefaultParticleType SWEEP_ATTACK;
   public static final DefaultParticleType TOTEM_OF_UNDYING;
   public static final DefaultParticleType UNDERWATER;
   public static final DefaultParticleType SPLASH;
   public static final DefaultParticleType WITCH;
   public static final DefaultParticleType BUBBLE_POP;
   public static final DefaultParticleType CURRENT_DOWN;
   public static final DefaultParticleType BUBBLE_COLUMN_UP;
   public static final DefaultParticleType NAUTILUS;
   public static final DefaultParticleType DOLPHIN;
   public static final DefaultParticleType CAMPFIRE_COSY_SMOKE;
   public static final DefaultParticleType CAMPFIRE_SIGNAL_SMOKE;
   public static final DefaultParticleType DRIPPING_HONEY;
   public static final DefaultParticleType FALLING_HONEY;
   public static final DefaultParticleType LANDING_HONEY;
   public static final DefaultParticleType FALLING_NECTAR;
   public static final DefaultParticleType ASH;
   public static final DefaultParticleType CRIMSON_SPORE;
   public static final DefaultParticleType WARPED_SPORE;
   public static final DefaultParticleType DRIPPING_OBSIDIAN_TEAR;
   public static final DefaultParticleType FALLING_OBSIDIAN_TEAR;
   public static final DefaultParticleType LANDING_OBSIDIAN_TEAR;
   public static final DefaultParticleType REVERSE_PORTAL;
   public static final DefaultParticleType WHITE_ASH;
   public static final Codec<ParticleEffect> TYPE_CODEC;

   private static DefaultParticleType register(String name, boolean alwaysShow) {
      return (DefaultParticleType)Registry.register(Registry.PARTICLE_TYPE, (String)name, new DefaultParticleType(alwaysShow));
   }

   private static <T extends ParticleEffect> ParticleType<T> register(String name, ParticleEffect.Factory<T> factory, final Function<ParticleType<T>, Codec<T>> function) {
      return (ParticleType)Registry.register(Registry.PARTICLE_TYPE, (String)name, new ParticleType<T>(false, factory) {
         public Codec<T> getCodec() {
            return (Codec)function.apply(this);
         }
      });
   }

   static {
      BLOCK = register("block", BlockStateParticleEffect.PARAMETERS_FACTORY, BlockStateParticleEffect::method_29128);
      BUBBLE = register("bubble", false);
      CLOUD = register("cloud", false);
      CRIT = register("crit", false);
      DAMAGE_INDICATOR = register("damage_indicator", true);
      DRAGON_BREATH = register("dragon_breath", false);
      DRIPPING_LAVA = register("dripping_lava", false);
      FALLING_LAVA = register("falling_lava", false);
      LANDING_LAVA = register("landing_lava", false);
      DRIPPING_WATER = register("dripping_water", false);
      FALLING_WATER = register("falling_water", false);
      DUST = register("dust", DustParticleEffect.PARAMETERS_FACTORY, (particleType) -> {
         return DustParticleEffect.CODEC;
      });
      EFFECT = register("effect", false);
      ELDER_GUARDIAN = register("elder_guardian", true);
      ENCHANTED_HIT = register("enchanted_hit", false);
      ENCHANT = register("enchant", false);
      END_ROD = register("end_rod", false);
      ENTITY_EFFECT = register("entity_effect", false);
      EXPLOSION_EMITTER = register("explosion_emitter", true);
      EXPLOSION = register("explosion", true);
      FALLING_DUST = register("falling_dust", BlockStateParticleEffect.PARAMETERS_FACTORY, BlockStateParticleEffect::method_29128);
      FIREWORK = register("firework", false);
      FISHING = register("fishing", false);
      FLAME = register("flame", false);
      SOUL_FIRE_FLAME = register("soul_fire_flame", false);
      SOUL = register("soul", false);
      FLASH = register("flash", false);
      HAPPY_VILLAGER = register("happy_villager", false);
      COMPOSTER = register("composter", false);
      HEART = register("heart", false);
      INSTANT_EFFECT = register("instant_effect", false);
      ITEM = register("item", ItemStackParticleEffect.PARAMETERS_FACTORY, ItemStackParticleEffect::method_29136);
      ITEM_SLIME = register("item_slime", false);
      ITEM_SNOWBALL = register("item_snowball", false);
      LARGE_SMOKE = register("large_smoke", false);
      LAVA = register("lava", false);
      MYCELIUM = register("mycelium", false);
      NOTE = register("note", false);
      POOF = register("poof", true);
      PORTAL = register("portal", false);
      RAIN = register("rain", false);
      SMOKE = register("smoke", false);
      SNEEZE = register("sneeze", false);
      SPIT = register("spit", true);
      SQUID_INK = register("squid_ink", true);
      SWEEP_ATTACK = register("sweep_attack", true);
      TOTEM_OF_UNDYING = register("totem_of_undying", false);
      UNDERWATER = register("underwater", false);
      SPLASH = register("splash", false);
      WITCH = register("witch", false);
      BUBBLE_POP = register("bubble_pop", false);
      CURRENT_DOWN = register("current_down", false);
      BUBBLE_COLUMN_UP = register("bubble_column_up", false);
      NAUTILUS = register("nautilus", false);
      DOLPHIN = register("dolphin", false);
      CAMPFIRE_COSY_SMOKE = register("campfire_cosy_smoke", true);
      CAMPFIRE_SIGNAL_SMOKE = register("campfire_signal_smoke", true);
      DRIPPING_HONEY = register("dripping_honey", false);
      FALLING_HONEY = register("falling_honey", false);
      LANDING_HONEY = register("landing_honey", false);
      FALLING_NECTAR = register("falling_nectar", false);
      ASH = register("ash", false);
      CRIMSON_SPORE = register("crimson_spore", false);
      WARPED_SPORE = register("warped_spore", false);
      DRIPPING_OBSIDIAN_TEAR = register("dripping_obsidian_tear", false);
      FALLING_OBSIDIAN_TEAR = register("falling_obsidian_tear", false);
      LANDING_OBSIDIAN_TEAR = register("landing_obsidian_tear", false);
      REVERSE_PORTAL = register("reverse_portal", false);
      WHITE_ASH = register("white_ash", false);
      TYPE_CODEC = Registry.PARTICLE_TYPE.dispatch("type", ParticleEffect::getType, ParticleType::getCodec);
   }
}
