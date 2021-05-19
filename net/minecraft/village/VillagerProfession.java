package net.minecraft.village;

import com.google.common.collect.ImmutableSet;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.poi.PointOfInterestType;
import org.jetbrains.annotations.Nullable;

public class VillagerProfession {
   public static final VillagerProfession NONE;
   public static final VillagerProfession ARMORER;
   public static final VillagerProfession BUTCHER;
   public static final VillagerProfession CARTOGRAPHER;
   public static final VillagerProfession CLERIC;
   public static final VillagerProfession FARMER;
   public static final VillagerProfession FISHERMAN;
   public static final VillagerProfession FLETCHER;
   public static final VillagerProfession LEATHERWORKER;
   public static final VillagerProfession LIBRARIAN;
   public static final VillagerProfession MASON;
   public static final VillagerProfession NITWIT;
   public static final VillagerProfession SHEPHERD;
   public static final VillagerProfession TOOLSMITH;
   public static final VillagerProfession WEAPONSMITH;
   private final String id;
   private final PointOfInterestType workStation;
   private final ImmutableSet<Item> gatherableItems;
   private final ImmutableSet<Block> secondaryJobSites;
   @Nullable
   private final SoundEvent workSound;

   private VillagerProfession(String id, PointOfInterestType workStation, ImmutableSet<Item> gatherableItems, ImmutableSet<Block> secondaryJobSites, @Nullable SoundEvent workSound) {
      this.id = id;
      this.workStation = workStation;
      this.gatherableItems = gatherableItems;
      this.secondaryJobSites = secondaryJobSites;
      this.workSound = workSound;
   }

   public PointOfInterestType getWorkStation() {
      return this.workStation;
   }

   public ImmutableSet<Item> getGatherableItems() {
      return this.gatherableItems;
   }

   public ImmutableSet<Block> getSecondaryJobSites() {
      return this.secondaryJobSites;
   }

   @Nullable
   public SoundEvent getWorkSound() {
      return this.workSound;
   }

   public String toString() {
      return this.id;
   }

   static VillagerProfession register(String id, PointOfInterestType workStation, @Nullable SoundEvent workSound) {
      return register(id, workStation, ImmutableSet.of(), ImmutableSet.of(), workSound);
   }

   static VillagerProfession register(String id, PointOfInterestType workStation, ImmutableSet<Item> gatherableItems, ImmutableSet<Block> secondaryJobSites, @Nullable SoundEvent workSound) {
      return (VillagerProfession)Registry.register(Registry.VILLAGER_PROFESSION, (Identifier)(new Identifier(id)), new VillagerProfession(id, workStation, gatherableItems, secondaryJobSites, workSound));
   }

   static {
      NONE = register("none", PointOfInterestType.UNEMPLOYED, (SoundEvent)null);
      ARMORER = register("armorer", PointOfInterestType.ARMORER, SoundEvents.ENTITY_VILLAGER_WORK_ARMORER);
      BUTCHER = register("butcher", PointOfInterestType.BUTCHER, SoundEvents.ENTITY_VILLAGER_WORK_BUTCHER);
      CARTOGRAPHER = register("cartographer", PointOfInterestType.CARTOGRAPHER, SoundEvents.ENTITY_VILLAGER_WORK_CARTOGRAPHER);
      CLERIC = register("cleric", PointOfInterestType.CLERIC, SoundEvents.ENTITY_VILLAGER_WORK_CLERIC);
      FARMER = register("farmer", PointOfInterestType.FARMER, ImmutableSet.of(Items.WHEAT, Items.WHEAT_SEEDS, Items.BEETROOT_SEEDS, Items.BONE_MEAL), ImmutableSet.of(Blocks.FARMLAND), SoundEvents.ENTITY_VILLAGER_WORK_FARMER);
      FISHERMAN = register("fisherman", PointOfInterestType.FISHERMAN, SoundEvents.ENTITY_VILLAGER_WORK_FISHERMAN);
      FLETCHER = register("fletcher", PointOfInterestType.FLETCHER, SoundEvents.ENTITY_VILLAGER_WORK_FLETCHER);
      LEATHERWORKER = register("leatherworker", PointOfInterestType.LEATHERWORKER, SoundEvents.ENTITY_VILLAGER_WORK_LEATHERWORKER);
      LIBRARIAN = register("librarian", PointOfInterestType.LIBRARIAN, SoundEvents.ENTITY_VILLAGER_WORK_LIBRARIAN);
      MASON = register("mason", PointOfInterestType.MASON, SoundEvents.ENTITY_VILLAGER_WORK_MASON);
      NITWIT = register("nitwit", PointOfInterestType.NITWIT, (SoundEvent)null);
      SHEPHERD = register("shepherd", PointOfInterestType.SHEPHERD, SoundEvents.ENTITY_VILLAGER_WORK_SHEPHERD);
      TOOLSMITH = register("toolsmith", PointOfInterestType.TOOLSMITH, SoundEvents.ENTITY_VILLAGER_WORK_TOOLSMITH);
      WEAPONSMITH = register("weaponsmith", PointOfInterestType.WEAPONSMITH, SoundEvents.ENTITY_VILLAGER_WORK_WEAPONSMITH);
   }
}
