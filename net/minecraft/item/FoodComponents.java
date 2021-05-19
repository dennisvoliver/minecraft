package net.minecraft.item;

import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;

/**
 * Contains all the default food components used in vanilla food items.
 */
public class FoodComponents {
   public static final FoodComponent APPLE = (new FoodComponent.Builder()).hunger(4).saturationModifier(0.3F).build();
   public static final FoodComponent BAKED_POTATO = (new FoodComponent.Builder()).hunger(5).saturationModifier(0.6F).build();
   public static final FoodComponent BEEF = (new FoodComponent.Builder()).hunger(3).saturationModifier(0.3F).meat().build();
   public static final FoodComponent BEETROOT = (new FoodComponent.Builder()).hunger(1).saturationModifier(0.6F).build();
   public static final FoodComponent BEETROOT_SOUP = create(6);
   public static final FoodComponent BREAD = (new FoodComponent.Builder()).hunger(5).saturationModifier(0.6F).build();
   public static final FoodComponent CARROT = (new FoodComponent.Builder()).hunger(3).saturationModifier(0.6F).build();
   public static final FoodComponent CHICKEN;
   public static final FoodComponent CHORUS_FRUIT;
   public static final FoodComponent COD;
   public static final FoodComponent COOKED_BEEF;
   public static final FoodComponent COOKED_CHICKEN;
   public static final FoodComponent COOKED_COD;
   public static final FoodComponent COOKED_MUTTON;
   public static final FoodComponent COOKED_PORKCHOP;
   public static final FoodComponent COOKED_RABBIT;
   public static final FoodComponent COOKED_SALMON;
   public static final FoodComponent COOKIE;
   public static final FoodComponent DRIED_KELP;
   public static final FoodComponent ENCHANTED_GOLDEN_APPLE;
   public static final FoodComponent GOLDEN_APPLE;
   public static final FoodComponent GOLDEN_CARROT;
   public static final FoodComponent HONEY_BOTTLE;
   public static final FoodComponent MELON_SLICE;
   public static final FoodComponent MUSHROOM_STEW;
   public static final FoodComponent MUTTON;
   public static final FoodComponent POISONOUS_POTATO;
   public static final FoodComponent PORKCHOP;
   public static final FoodComponent POTATO;
   public static final FoodComponent PUFFERFISH;
   public static final FoodComponent PUMPKIN_PIE;
   public static final FoodComponent RABBIT;
   public static final FoodComponent RABBIT_STEW;
   public static final FoodComponent ROTTEN_FLESH;
   public static final FoodComponent SALMON;
   public static final FoodComponent SPIDER_EYE;
   public static final FoodComponent SUSPICIOUS_STEW;
   public static final FoodComponent SWEET_BERRIES;
   public static final FoodComponent TROPICAL_FISH;

   private static FoodComponent create(int hunger) {
      return (new FoodComponent.Builder()).hunger(hunger).saturationModifier(0.6F).build();
   }

   static {
      CHICKEN = (new FoodComponent.Builder()).hunger(2).saturationModifier(0.3F).statusEffect(new StatusEffectInstance(StatusEffects.HUNGER, 600, 0), 0.3F).meat().build();
      CHORUS_FRUIT = (new FoodComponent.Builder()).hunger(4).saturationModifier(0.3F).alwaysEdible().build();
      COD = (new FoodComponent.Builder()).hunger(2).saturationModifier(0.1F).build();
      COOKED_BEEF = (new FoodComponent.Builder()).hunger(8).saturationModifier(0.8F).meat().build();
      COOKED_CHICKEN = (new FoodComponent.Builder()).hunger(6).saturationModifier(0.6F).meat().build();
      COOKED_COD = (new FoodComponent.Builder()).hunger(5).saturationModifier(0.6F).build();
      COOKED_MUTTON = (new FoodComponent.Builder()).hunger(6).saturationModifier(0.8F).meat().build();
      COOKED_PORKCHOP = (new FoodComponent.Builder()).hunger(8).saturationModifier(0.8F).meat().build();
      COOKED_RABBIT = (new FoodComponent.Builder()).hunger(5).saturationModifier(0.6F).meat().build();
      COOKED_SALMON = (new FoodComponent.Builder()).hunger(6).saturationModifier(0.8F).build();
      COOKIE = (new FoodComponent.Builder()).hunger(2).saturationModifier(0.1F).build();
      DRIED_KELP = (new FoodComponent.Builder()).hunger(1).saturationModifier(0.3F).snack().build();
      ENCHANTED_GOLDEN_APPLE = (new FoodComponent.Builder()).hunger(4).saturationModifier(1.2F).statusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, 400, 1), 1.0F).statusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, 6000, 0), 1.0F).statusEffect(new StatusEffectInstance(StatusEffects.FIRE_RESISTANCE, 6000, 0), 1.0F).statusEffect(new StatusEffectInstance(StatusEffects.ABSORPTION, 2400, 3), 1.0F).alwaysEdible().build();
      GOLDEN_APPLE = (new FoodComponent.Builder()).hunger(4).saturationModifier(1.2F).statusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, 100, 1), 1.0F).statusEffect(new StatusEffectInstance(StatusEffects.ABSORPTION, 2400, 0), 1.0F).alwaysEdible().build();
      GOLDEN_CARROT = (new FoodComponent.Builder()).hunger(6).saturationModifier(1.2F).build();
      HONEY_BOTTLE = (new FoodComponent.Builder()).hunger(6).saturationModifier(0.1F).build();
      MELON_SLICE = (new FoodComponent.Builder()).hunger(2).saturationModifier(0.3F).build();
      MUSHROOM_STEW = create(6);
      MUTTON = (new FoodComponent.Builder()).hunger(2).saturationModifier(0.3F).meat().build();
      POISONOUS_POTATO = (new FoodComponent.Builder()).hunger(2).saturationModifier(0.3F).statusEffect(new StatusEffectInstance(StatusEffects.POISON, 100, 0), 0.6F).build();
      PORKCHOP = (new FoodComponent.Builder()).hunger(3).saturationModifier(0.3F).meat().build();
      POTATO = (new FoodComponent.Builder()).hunger(1).saturationModifier(0.3F).build();
      PUFFERFISH = (new FoodComponent.Builder()).hunger(1).saturationModifier(0.1F).statusEffect(new StatusEffectInstance(StatusEffects.POISON, 1200, 3), 1.0F).statusEffect(new StatusEffectInstance(StatusEffects.HUNGER, 300, 2), 1.0F).statusEffect(new StatusEffectInstance(StatusEffects.NAUSEA, 300, 0), 1.0F).build();
      PUMPKIN_PIE = (new FoodComponent.Builder()).hunger(8).saturationModifier(0.3F).build();
      RABBIT = (new FoodComponent.Builder()).hunger(3).saturationModifier(0.3F).meat().build();
      RABBIT_STEW = create(10);
      ROTTEN_FLESH = (new FoodComponent.Builder()).hunger(4).saturationModifier(0.1F).statusEffect(new StatusEffectInstance(StatusEffects.HUNGER, 600, 0), 0.8F).meat().build();
      SALMON = (new FoodComponent.Builder()).hunger(2).saturationModifier(0.1F).build();
      SPIDER_EYE = (new FoodComponent.Builder()).hunger(2).saturationModifier(0.8F).statusEffect(new StatusEffectInstance(StatusEffects.POISON, 100, 0), 1.0F).build();
      SUSPICIOUS_STEW = create(6);
      SWEET_BERRIES = (new FoodComponent.Builder()).hunger(2).saturationModifier(0.1F).build();
      TROPICAL_FISH = (new FoodComponent.Builder()).hunger(1).saturationModifier(0.1F).build();
   }
}
