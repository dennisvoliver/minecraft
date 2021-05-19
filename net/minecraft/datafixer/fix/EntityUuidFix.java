package net.minecraft.datafixer.fix;

import com.google.common.collect.Sets;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import java.util.Iterator;
import java.util.Optional;
import java.util.Set;
import net.minecraft.datafixer.TypeReferences;

public class EntityUuidFix extends AbstractUuidFix {
   private static final Set<String> RIDEABLE_TAMEABLES = Sets.newHashSet();
   private static final Set<String> TAMEABLE_PETS = Sets.newHashSet();
   private static final Set<String> BREEDABLES = Sets.newHashSet();
   private static final Set<String> LEASHABLES = Sets.newHashSet();
   private static final Set<String> OTHER_LIVINGS = Sets.newHashSet();
   private static final Set<String> PROJECTILES = Sets.newHashSet();

   public EntityUuidFix(Schema outputSchema) {
      super(outputSchema, TypeReferences.ENTITY);
   }

   protected TypeRewriteRule makeRule() {
      return this.fixTypeEverywhereTyped("EntityUUIDFixes", this.getInputSchema().getType(this.typeReference), (typed) -> {
         typed = typed.update(DSL.remainderFinder(), EntityUuidFix::updateSelfUuid);

         Iterator var2;
         String string6;
         for(var2 = RIDEABLE_TAMEABLES.iterator(); var2.hasNext(); typed = this.updateTyped(typed, string6, EntityUuidFix::updateTameable)) {
            string6 = (String)var2.next();
         }

         for(var2 = TAMEABLE_PETS.iterator(); var2.hasNext(); typed = this.updateTyped(typed, string6, EntityUuidFix::updateTameable)) {
            string6 = (String)var2.next();
         }

         for(var2 = BREEDABLES.iterator(); var2.hasNext(); typed = this.updateTyped(typed, string6, EntityUuidFix::updateBreedable)) {
            string6 = (String)var2.next();
         }

         for(var2 = LEASHABLES.iterator(); var2.hasNext(); typed = this.updateTyped(typed, string6, EntityUuidFix::updateLeashable)) {
            string6 = (String)var2.next();
         }

         for(var2 = OTHER_LIVINGS.iterator(); var2.hasNext(); typed = this.updateTyped(typed, string6, EntityUuidFix::updateLiving)) {
            string6 = (String)var2.next();
         }

         for(var2 = PROJECTILES.iterator(); var2.hasNext(); typed = this.updateTyped(typed, string6, EntityUuidFix::updateProjectile)) {
            string6 = (String)var2.next();
         }

         typed = this.updateTyped(typed, "minecraft:bee", EntityUuidFix::updateZombifiedPiglin);
         typed = this.updateTyped(typed, "minecraft:zombified_piglin", EntityUuidFix::updateZombifiedPiglin);
         typed = this.updateTyped(typed, "minecraft:fox", EntityUuidFix::updateFox);
         typed = this.updateTyped(typed, "minecraft:item", EntityUuidFix::updateItemEntity);
         typed = this.updateTyped(typed, "minecraft:shulker_bullet", EntityUuidFix::updateShulkerBullet);
         typed = this.updateTyped(typed, "minecraft:area_effect_cloud", EntityUuidFix::updateAreaEffectCloud);
         typed = this.updateTyped(typed, "minecraft:zombie_villager", EntityUuidFix::updateZombieVillager);
         typed = this.updateTyped(typed, "minecraft:evoker_fangs", EntityUuidFix::updateEvokerFangs);
         typed = this.updateTyped(typed, "minecraft:piglin", EntityUuidFix::updateAngryAtMemory);
         return typed;
      });
   }

   private static Dynamic<?> updateAngryAtMemory(Dynamic<?> dynamic) {
      return dynamic.update("Brain", (dynamicx) -> {
         return dynamicx.update("memories", (dynamic) -> {
            return dynamic.update("minecraft:angry_at", (dynamicx) -> {
               return (Dynamic)updateStringUuid(dynamicx, "value", "value").orElseGet(() -> {
                  LOGGER.warn("angry_at has no value.");
                  return dynamicx;
               });
            });
         });
      });
   }

   private static Dynamic<?> updateEvokerFangs(Dynamic<?> dynamic) {
      return (Dynamic)updateRegularMostLeast(dynamic, "OwnerUUID", "Owner").orElse(dynamic);
   }

   private static Dynamic<?> updateZombieVillager(Dynamic<?> dynamic) {
      return (Dynamic)updateRegularMostLeast(dynamic, "ConversionPlayer", "ConversionPlayer").orElse(dynamic);
   }

   private static Dynamic<?> updateAreaEffectCloud(Dynamic<?> dynamic) {
      return (Dynamic)updateRegularMostLeast(dynamic, "OwnerUUID", "Owner").orElse(dynamic);
   }

   private static Dynamic<?> updateShulkerBullet(Dynamic<?> dynamic) {
      dynamic = (Dynamic)updateCompoundUuid(dynamic, "Owner", "Owner").orElse(dynamic);
      return (Dynamic)updateCompoundUuid(dynamic, "Target", "Target").orElse(dynamic);
   }

   private static Dynamic<?> updateItemEntity(Dynamic<?> dynamic) {
      dynamic = (Dynamic)updateCompoundUuid(dynamic, "Owner", "Owner").orElse(dynamic);
      return (Dynamic)updateCompoundUuid(dynamic, "Thrower", "Thrower").orElse(dynamic);
   }

   private static Dynamic<?> updateFox(Dynamic<?> dynamic) {
      Optional<Dynamic<?>> optional = dynamic.get("TrustedUUIDs").result().map((dynamic2) -> {
         return dynamic.createList(dynamic2.asStream().map((dynamicx) -> {
            return (Dynamic)createArrayFromCompoundUuid(dynamicx).orElseGet(() -> {
               LOGGER.warn("Trusted contained invalid data.");
               return dynamicx;
            });
         }));
      });
      return (Dynamic)DataFixUtils.orElse(optional.map((dynamic2) -> {
         return dynamic.remove("TrustedUUIDs").set("Trusted", dynamic2);
      }), dynamic);
   }

   private static Dynamic<?> updateZombifiedPiglin(Dynamic<?> dynamic) {
      return (Dynamic)updateStringUuid(dynamic, "HurtBy", "HurtBy").orElse(dynamic);
   }

   private static Dynamic<?> updateTameable(Dynamic<?> dynamic) {
      Dynamic<?> dynamic2 = updateBreedable(dynamic);
      return (Dynamic)updateStringUuid(dynamic2, "OwnerUUID", "Owner").orElse(dynamic2);
   }

   private static Dynamic<?> updateBreedable(Dynamic<?> dynamic) {
      Dynamic<?> dynamic2 = updateLeashable(dynamic);
      return (Dynamic)updateRegularMostLeast(dynamic2, "LoveCause", "LoveCause").orElse(dynamic2);
   }

   private static Dynamic<?> updateLeashable(Dynamic<?> dynamic) {
      return updateLiving(dynamic).update("Leash", (dynamicx) -> {
         return (Dynamic)updateRegularMostLeast(dynamicx, "UUID", "UUID").orElse(dynamicx);
      });
   }

   public static Dynamic<?> updateLiving(Dynamic<?> dynamic) {
      return dynamic.update("Attributes", (dynamic2) -> {
         return dynamic.createList(dynamic2.asStream().map((dynamicx) -> {
            return dynamicx.update("Modifiers", (dynamic2) -> {
               return dynamicx.createList(dynamic2.asStream().map((dynamic) -> {
                  return (Dynamic)updateRegularMostLeast(dynamic, "UUID", "UUID").orElse(dynamic);
               }));
            });
         }));
      });
   }

   private static Dynamic<?> updateProjectile(Dynamic<?> dynamic) {
      return (Dynamic)DataFixUtils.orElse(dynamic.get("OwnerUUID").result().map((dynamic2) -> {
         return dynamic.remove("OwnerUUID").set("Owner", dynamic2);
      }), dynamic);
   }

   public static Dynamic<?> updateSelfUuid(Dynamic<?> dynamic) {
      return (Dynamic)updateRegularMostLeast(dynamic, "UUID", "UUID").orElse(dynamic);
   }

   static {
      RIDEABLE_TAMEABLES.add("minecraft:donkey");
      RIDEABLE_TAMEABLES.add("minecraft:horse");
      RIDEABLE_TAMEABLES.add("minecraft:llama");
      RIDEABLE_TAMEABLES.add("minecraft:mule");
      RIDEABLE_TAMEABLES.add("minecraft:skeleton_horse");
      RIDEABLE_TAMEABLES.add("minecraft:trader_llama");
      RIDEABLE_TAMEABLES.add("minecraft:zombie_horse");
      TAMEABLE_PETS.add("minecraft:cat");
      TAMEABLE_PETS.add("minecraft:parrot");
      TAMEABLE_PETS.add("minecraft:wolf");
      BREEDABLES.add("minecraft:bee");
      BREEDABLES.add("minecraft:chicken");
      BREEDABLES.add("minecraft:cow");
      BREEDABLES.add("minecraft:fox");
      BREEDABLES.add("minecraft:mooshroom");
      BREEDABLES.add("minecraft:ocelot");
      BREEDABLES.add("minecraft:panda");
      BREEDABLES.add("minecraft:pig");
      BREEDABLES.add("minecraft:polar_bear");
      BREEDABLES.add("minecraft:rabbit");
      BREEDABLES.add("minecraft:sheep");
      BREEDABLES.add("minecraft:turtle");
      BREEDABLES.add("minecraft:hoglin");
      LEASHABLES.add("minecraft:bat");
      LEASHABLES.add("minecraft:blaze");
      LEASHABLES.add("minecraft:cave_spider");
      LEASHABLES.add("minecraft:cod");
      LEASHABLES.add("minecraft:creeper");
      LEASHABLES.add("minecraft:dolphin");
      LEASHABLES.add("minecraft:drowned");
      LEASHABLES.add("minecraft:elder_guardian");
      LEASHABLES.add("minecraft:ender_dragon");
      LEASHABLES.add("minecraft:enderman");
      LEASHABLES.add("minecraft:endermite");
      LEASHABLES.add("minecraft:evoker");
      LEASHABLES.add("minecraft:ghast");
      LEASHABLES.add("minecraft:giant");
      LEASHABLES.add("minecraft:guardian");
      LEASHABLES.add("minecraft:husk");
      LEASHABLES.add("minecraft:illusioner");
      LEASHABLES.add("minecraft:magma_cube");
      LEASHABLES.add("minecraft:pufferfish");
      LEASHABLES.add("minecraft:zombified_piglin");
      LEASHABLES.add("minecraft:salmon");
      LEASHABLES.add("minecraft:shulker");
      LEASHABLES.add("minecraft:silverfish");
      LEASHABLES.add("minecraft:skeleton");
      LEASHABLES.add("minecraft:slime");
      LEASHABLES.add("minecraft:snow_golem");
      LEASHABLES.add("minecraft:spider");
      LEASHABLES.add("minecraft:squid");
      LEASHABLES.add("minecraft:stray");
      LEASHABLES.add("minecraft:tropical_fish");
      LEASHABLES.add("minecraft:vex");
      LEASHABLES.add("minecraft:villager");
      LEASHABLES.add("minecraft:iron_golem");
      LEASHABLES.add("minecraft:vindicator");
      LEASHABLES.add("minecraft:pillager");
      LEASHABLES.add("minecraft:wandering_trader");
      LEASHABLES.add("minecraft:witch");
      LEASHABLES.add("minecraft:wither");
      LEASHABLES.add("minecraft:wither_skeleton");
      LEASHABLES.add("minecraft:zombie");
      LEASHABLES.add("minecraft:zombie_villager");
      LEASHABLES.add("minecraft:phantom");
      LEASHABLES.add("minecraft:ravager");
      LEASHABLES.add("minecraft:piglin");
      OTHER_LIVINGS.add("minecraft:armor_stand");
      PROJECTILES.add("minecraft:arrow");
      PROJECTILES.add("minecraft:dragon_fireball");
      PROJECTILES.add("minecraft:firework_rocket");
      PROJECTILES.add("minecraft:fireball");
      PROJECTILES.add("minecraft:llama_spit");
      PROJECTILES.add("minecraft:small_fireball");
      PROJECTILES.add("minecraft:snowball");
      PROJECTILES.add("minecraft:spectral_arrow");
      PROJECTILES.add("minecraft:egg");
      PROJECTILES.add("minecraft:ender_pearl");
      PROJECTILES.add("minecraft:experience_bottle");
      PROJECTILES.add("minecraft:potion");
      PROJECTILES.add("minecraft:trident");
      PROJECTILES.add("minecraft:wither_skull");
   }
}
