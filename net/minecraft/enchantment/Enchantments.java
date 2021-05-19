package net.minecraft.enchantment;

import net.minecraft.entity.EquipmentSlot;
import net.minecraft.util.registry.Registry;

public class Enchantments {
   private static final EquipmentSlot[] ALL_ARMOR;
   public static final Enchantment PROTECTION;
   public static final Enchantment FIRE_PROTECTION;
   public static final Enchantment FEATHER_FALLING;
   public static final Enchantment BLAST_PROTECTION;
   public static final Enchantment PROJECTILE_PROTECTION;
   public static final Enchantment RESPIRATION;
   public static final Enchantment AQUA_AFFINITY;
   public static final Enchantment THORNS;
   public static final Enchantment DEPTH_STRIDER;
   public static final Enchantment FROST_WALKER;
   public static final Enchantment BINDING_CURSE;
   public static final Enchantment SOUL_SPEED;
   public static final Enchantment SHARPNESS;
   public static final Enchantment SMITE;
   public static final Enchantment BANE_OF_ARTHROPODS;
   public static final Enchantment KNOCKBACK;
   public static final Enchantment FIRE_ASPECT;
   public static final Enchantment LOOTING;
   public static final Enchantment SWEEPING;
   public static final Enchantment EFFICIENCY;
   public static final Enchantment SILK_TOUCH;
   public static final Enchantment UNBREAKING;
   public static final Enchantment FORTUNE;
   public static final Enchantment POWER;
   public static final Enchantment PUNCH;
   public static final Enchantment FLAME;
   public static final Enchantment INFINITY;
   public static final Enchantment LUCK_OF_THE_SEA;
   public static final Enchantment LURE;
   public static final Enchantment LOYALTY;
   public static final Enchantment IMPALING;
   public static final Enchantment RIPTIDE;
   public static final Enchantment CHANNELING;
   public static final Enchantment MULTISHOT;
   public static final Enchantment QUICK_CHARGE;
   public static final Enchantment PIERCING;
   public static final Enchantment MENDING;
   public static final Enchantment VANISHING_CURSE;

   private static Enchantment register(String name, Enchantment enchantment) {
      return (Enchantment)Registry.register(Registry.ENCHANTMENT, (String)name, enchantment);
   }

   static {
      ALL_ARMOR = new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};
      PROTECTION = register("protection", new ProtectionEnchantment(Enchantment.Rarity.COMMON, ProtectionEnchantment.Type.ALL, ALL_ARMOR));
      FIRE_PROTECTION = register("fire_protection", new ProtectionEnchantment(Enchantment.Rarity.UNCOMMON, ProtectionEnchantment.Type.FIRE, ALL_ARMOR));
      FEATHER_FALLING = register("feather_falling", new ProtectionEnchantment(Enchantment.Rarity.UNCOMMON, ProtectionEnchantment.Type.FALL, ALL_ARMOR));
      BLAST_PROTECTION = register("blast_protection", new ProtectionEnchantment(Enchantment.Rarity.RARE, ProtectionEnchantment.Type.EXPLOSION, ALL_ARMOR));
      PROJECTILE_PROTECTION = register("projectile_protection", new ProtectionEnchantment(Enchantment.Rarity.UNCOMMON, ProtectionEnchantment.Type.PROJECTILE, ALL_ARMOR));
      RESPIRATION = register("respiration", new RespirationEnchantment(Enchantment.Rarity.RARE, ALL_ARMOR));
      AQUA_AFFINITY = register("aqua_affinity", new AquaAffinityEnchantment(Enchantment.Rarity.RARE, ALL_ARMOR));
      THORNS = register("thorns", new ThornsEnchantment(Enchantment.Rarity.VERY_RARE, ALL_ARMOR));
      DEPTH_STRIDER = register("depth_strider", new DepthStriderEnchantment(Enchantment.Rarity.RARE, ALL_ARMOR));
      FROST_WALKER = register("frost_walker", new FrostWalkerEnchantment(Enchantment.Rarity.RARE, new EquipmentSlot[]{EquipmentSlot.FEET}));
      BINDING_CURSE = register("binding_curse", new BindingCurseEnchantment(Enchantment.Rarity.VERY_RARE, ALL_ARMOR));
      SOUL_SPEED = register("soul_speed", new SoulSpeedEnchantment(Enchantment.Rarity.VERY_RARE, new EquipmentSlot[]{EquipmentSlot.FEET}));
      SHARPNESS = register("sharpness", new DamageEnchantment(Enchantment.Rarity.COMMON, 0, new EquipmentSlot[]{EquipmentSlot.MAINHAND}));
      SMITE = register("smite", new DamageEnchantment(Enchantment.Rarity.UNCOMMON, 1, new EquipmentSlot[]{EquipmentSlot.MAINHAND}));
      BANE_OF_ARTHROPODS = register("bane_of_arthropods", new DamageEnchantment(Enchantment.Rarity.UNCOMMON, 2, new EquipmentSlot[]{EquipmentSlot.MAINHAND}));
      KNOCKBACK = register("knockback", new KnockbackEnchantment(Enchantment.Rarity.UNCOMMON, new EquipmentSlot[]{EquipmentSlot.MAINHAND}));
      FIRE_ASPECT = register("fire_aspect", new FireAspectEnchantment(Enchantment.Rarity.RARE, new EquipmentSlot[]{EquipmentSlot.MAINHAND}));
      LOOTING = register("looting", new LuckEnchantment(Enchantment.Rarity.RARE, EnchantmentTarget.WEAPON, new EquipmentSlot[]{EquipmentSlot.MAINHAND}));
      SWEEPING = register("sweeping", new SweepingEnchantment(Enchantment.Rarity.RARE, new EquipmentSlot[]{EquipmentSlot.MAINHAND}));
      EFFICIENCY = register("efficiency", new EfficiencyEnchantment(Enchantment.Rarity.COMMON, new EquipmentSlot[]{EquipmentSlot.MAINHAND}));
      SILK_TOUCH = register("silk_touch", new SilkTouchEnchantment(Enchantment.Rarity.VERY_RARE, new EquipmentSlot[]{EquipmentSlot.MAINHAND}));
      UNBREAKING = register("unbreaking", new UnbreakingEnchantment(Enchantment.Rarity.UNCOMMON, new EquipmentSlot[]{EquipmentSlot.MAINHAND}));
      FORTUNE = register("fortune", new LuckEnchantment(Enchantment.Rarity.RARE, EnchantmentTarget.DIGGER, new EquipmentSlot[]{EquipmentSlot.MAINHAND}));
      POWER = register("power", new PowerEnchantment(Enchantment.Rarity.COMMON, new EquipmentSlot[]{EquipmentSlot.MAINHAND}));
      PUNCH = register("punch", new PunchEnchantment(Enchantment.Rarity.RARE, new EquipmentSlot[]{EquipmentSlot.MAINHAND}));
      FLAME = register("flame", new FlameEnchantment(Enchantment.Rarity.RARE, new EquipmentSlot[]{EquipmentSlot.MAINHAND}));
      INFINITY = register("infinity", new InfinityEnchantment(Enchantment.Rarity.VERY_RARE, new EquipmentSlot[]{EquipmentSlot.MAINHAND}));
      LUCK_OF_THE_SEA = register("luck_of_the_sea", new LuckEnchantment(Enchantment.Rarity.RARE, EnchantmentTarget.FISHING_ROD, new EquipmentSlot[]{EquipmentSlot.MAINHAND}));
      LURE = register("lure", new LureEnchantment(Enchantment.Rarity.RARE, EnchantmentTarget.FISHING_ROD, new EquipmentSlot[]{EquipmentSlot.MAINHAND}));
      LOYALTY = register("loyalty", new LoyaltyEnchantment(Enchantment.Rarity.UNCOMMON, new EquipmentSlot[]{EquipmentSlot.MAINHAND}));
      IMPALING = register("impaling", new ImpalingEnchantment(Enchantment.Rarity.RARE, new EquipmentSlot[]{EquipmentSlot.MAINHAND}));
      RIPTIDE = register("riptide", new RiptideEnchantment(Enchantment.Rarity.RARE, new EquipmentSlot[]{EquipmentSlot.MAINHAND}));
      CHANNELING = register("channeling", new ChannelingEnchantment(Enchantment.Rarity.VERY_RARE, new EquipmentSlot[]{EquipmentSlot.MAINHAND}));
      MULTISHOT = register("multishot", new MultishotEnchantment(Enchantment.Rarity.RARE, new EquipmentSlot[]{EquipmentSlot.MAINHAND}));
      QUICK_CHARGE = register("quick_charge", new QuickChargeEnchantment(Enchantment.Rarity.UNCOMMON, new EquipmentSlot[]{EquipmentSlot.MAINHAND}));
      PIERCING = register("piercing", new PiercingEnchantment(Enchantment.Rarity.COMMON, new EquipmentSlot[]{EquipmentSlot.MAINHAND}));
      MENDING = register("mending", new MendingEnchantment(Enchantment.Rarity.RARE, EquipmentSlot.values()));
      VANISHING_CURSE = register("vanishing_curse", new VanishingCurseEnchantment(Enchantment.Rarity.VERY_RARE, EquipmentSlot.values()));
   }
}
