package net.minecraft.entity.attribute;

import net.minecraft.util.registry.Registry;

public class EntityAttributes {
   public static final EntityAttribute GENERIC_MAX_HEALTH = register("generic.max_health", (new ClampedEntityAttribute("attribute.name.generic.max_health", 20.0D, 1.0D, 1024.0D)).setTracked(true));
   public static final EntityAttribute GENERIC_FOLLOW_RANGE = register("generic.follow_range", new ClampedEntityAttribute("attribute.name.generic.follow_range", 32.0D, 0.0D, 2048.0D));
   public static final EntityAttribute GENERIC_KNOCKBACK_RESISTANCE = register("generic.knockback_resistance", new ClampedEntityAttribute("attribute.name.generic.knockback_resistance", 0.0D, 0.0D, 1.0D));
   public static final EntityAttribute GENERIC_MOVEMENT_SPEED = register("generic.movement_speed", (new ClampedEntityAttribute("attribute.name.generic.movement_speed", 0.699999988079071D, 0.0D, 1024.0D)).setTracked(true));
   public static final EntityAttribute GENERIC_FLYING_SPEED = register("generic.flying_speed", (new ClampedEntityAttribute("attribute.name.generic.flying_speed", 0.4000000059604645D, 0.0D, 1024.0D)).setTracked(true));
   public static final EntityAttribute GENERIC_ATTACK_DAMAGE = register("generic.attack_damage", new ClampedEntityAttribute("attribute.name.generic.attack_damage", 2.0D, 0.0D, 2048.0D));
   public static final EntityAttribute GENERIC_ATTACK_KNOCKBACK = register("generic.attack_knockback", new ClampedEntityAttribute("attribute.name.generic.attack_knockback", 0.0D, 0.0D, 5.0D));
   public static final EntityAttribute GENERIC_ATTACK_SPEED = register("generic.attack_speed", (new ClampedEntityAttribute("attribute.name.generic.attack_speed", 4.0D, 0.0D, 1024.0D)).setTracked(true));
   public static final EntityAttribute GENERIC_ARMOR = register("generic.armor", (new ClampedEntityAttribute("attribute.name.generic.armor", 0.0D, 0.0D, 30.0D)).setTracked(true));
   public static final EntityAttribute GENERIC_ARMOR_TOUGHNESS = register("generic.armor_toughness", (new ClampedEntityAttribute("attribute.name.generic.armor_toughness", 0.0D, 0.0D, 20.0D)).setTracked(true));
   public static final EntityAttribute GENERIC_LUCK = register("generic.luck", (new ClampedEntityAttribute("attribute.name.generic.luck", 0.0D, -1024.0D, 1024.0D)).setTracked(true));
   public static final EntityAttribute ZOMBIE_SPAWN_REINFORCEMENTS = register("zombie.spawn_reinforcements", new ClampedEntityAttribute("attribute.name.zombie.spawn_reinforcements", 0.0D, 0.0D, 1.0D));
   public static final EntityAttribute HORSE_JUMP_STRENGTH = register("horse.jump_strength", (new ClampedEntityAttribute("attribute.name.horse.jump_strength", 0.7D, 0.0D, 2.0D)).setTracked(true));

   private static EntityAttribute register(String id, EntityAttribute attribute) {
      return (EntityAttribute)Registry.register(Registry.ATTRIBUTE, (String)id, attribute);
   }
}
