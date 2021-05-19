package net.minecraft.data.server;

import java.util.function.Consumer;
import net.minecraft.advancement.Advancement;
import net.minecraft.advancement.AdvancementFrame;
import net.minecraft.advancement.AdvancementRewards;
import net.minecraft.advancement.CriterionMerger;
import net.minecraft.advancement.criterion.BeeNestDestroyedCriterion;
import net.minecraft.advancement.criterion.BredAnimalsCriterion;
import net.minecraft.advancement.criterion.ConsumeItemCriterion;
import net.minecraft.advancement.criterion.CriterionConditions;
import net.minecraft.advancement.criterion.FilledBucketCriterion;
import net.minecraft.advancement.criterion.FishingRodHookedCriterion;
import net.minecraft.advancement.criterion.InventoryChangedCriterion;
import net.minecraft.advancement.criterion.ItemUsedOnBlockCriterion;
import net.minecraft.advancement.criterion.PlacedBlockCriterion;
import net.minecraft.advancement.criterion.TameAnimalCriterion;
import net.minecraft.block.Blocks;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.CatEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.Items;
import net.minecraft.predicate.BlockPredicate;
import net.minecraft.predicate.NumberRange;
import net.minecraft.predicate.entity.EntityPredicate;
import net.minecraft.predicate.entity.LocationPredicate;
import net.minecraft.predicate.item.EnchantmentPredicate;
import net.minecraft.predicate.item.ItemPredicate;
import net.minecraft.tag.BlockTags;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class HusbandryTabAdvancementGenerator implements Consumer<Consumer<Advancement>> {
   private static final EntityType<?>[] BREEDABLE_ANIMALS;
   private static final Item[] FISH_ITEMS;
   private static final Item[] FISH_BUCKET_ITEMS;
   private static final Item[] FOOD_ITEMS;

   public void accept(Consumer<Advancement> consumer) {
      Advancement advancement = Advancement.Task.create().display((ItemConvertible)Blocks.HAY_BLOCK, new TranslatableText("advancements.husbandry.root.title"), new TranslatableText("advancements.husbandry.root.description"), new Identifier("textures/gui/advancements/backgrounds/husbandry.png"), AdvancementFrame.TASK, false, false, false).criterion("consumed_item", (CriterionConditions)ConsumeItemCriterion.Conditions.any()).build(consumer, "husbandry/root");
      Advancement advancement2 = Advancement.Task.create().parent(advancement).display((ItemConvertible)Items.WHEAT, new TranslatableText("advancements.husbandry.plant_seed.title"), new TranslatableText("advancements.husbandry.plant_seed.description"), (Identifier)null, AdvancementFrame.TASK, true, true, false).criteriaMerger(CriterionMerger.OR).criterion("wheat", (CriterionConditions)PlacedBlockCriterion.Conditions.block(Blocks.WHEAT)).criterion("pumpkin_stem", (CriterionConditions)PlacedBlockCriterion.Conditions.block(Blocks.PUMPKIN_STEM)).criterion("melon_stem", (CriterionConditions)PlacedBlockCriterion.Conditions.block(Blocks.MELON_STEM)).criterion("beetroots", (CriterionConditions)PlacedBlockCriterion.Conditions.block(Blocks.BEETROOTS)).criterion("nether_wart", (CriterionConditions)PlacedBlockCriterion.Conditions.block(Blocks.NETHER_WART)).build(consumer, "husbandry/plant_seed");
      Advancement advancement3 = Advancement.Task.create().parent(advancement).display((ItemConvertible)Items.WHEAT, new TranslatableText("advancements.husbandry.breed_an_animal.title"), new TranslatableText("advancements.husbandry.breed_an_animal.description"), (Identifier)null, AdvancementFrame.TASK, true, true, false).criteriaMerger(CriterionMerger.OR).criterion("bred", (CriterionConditions)BredAnimalsCriterion.Conditions.any()).build(consumer, "husbandry/breed_an_animal");
      this.requireFoodItemsEaten(Advancement.Task.create()).parent(advancement2).display((ItemConvertible)Items.APPLE, new TranslatableText("advancements.husbandry.balanced_diet.title"), new TranslatableText("advancements.husbandry.balanced_diet.description"), (Identifier)null, AdvancementFrame.CHALLENGE, true, true, false).rewards(AdvancementRewards.Builder.experience(100)).build(consumer, "husbandry/balanced_diet");
      Advancement.Task.create().parent(advancement2).display((ItemConvertible)Items.NETHERITE_HOE, new TranslatableText("advancements.husbandry.netherite_hoe.title"), new TranslatableText("advancements.husbandry.netherite_hoe.description"), (Identifier)null, AdvancementFrame.CHALLENGE, true, true, false).rewards(AdvancementRewards.Builder.experience(100)).criterion("netherite_hoe", (CriterionConditions)InventoryChangedCriterion.Conditions.items(Items.NETHERITE_HOE)).build(consumer, "husbandry/obtain_netherite_hoe");
      Advancement advancement4 = Advancement.Task.create().parent(advancement).display((ItemConvertible)Items.LEAD, new TranslatableText("advancements.husbandry.tame_an_animal.title"), new TranslatableText("advancements.husbandry.tame_an_animal.description"), (Identifier)null, AdvancementFrame.TASK, true, true, false).criterion("tamed_animal", (CriterionConditions)TameAnimalCriterion.Conditions.any()).build(consumer, "husbandry/tame_an_animal");
      this.requireListedAnimalsBred(Advancement.Task.create()).parent(advancement3).display((ItemConvertible)Items.GOLDEN_CARROT, new TranslatableText("advancements.husbandry.breed_all_animals.title"), new TranslatableText("advancements.husbandry.breed_all_animals.description"), (Identifier)null, AdvancementFrame.CHALLENGE, true, true, false).rewards(AdvancementRewards.Builder.experience(100)).build(consumer, "husbandry/bred_all_animals");
      Advancement advancement5 = this.requireListedFishCaught(Advancement.Task.create()).parent(advancement).criteriaMerger(CriterionMerger.OR).display((ItemConvertible)Items.FISHING_ROD, new TranslatableText("advancements.husbandry.fishy_business.title"), new TranslatableText("advancements.husbandry.fishy_business.description"), (Identifier)null, AdvancementFrame.TASK, true, true, false).build(consumer, "husbandry/fishy_business");
      this.requireListedFishBucketsFilled(Advancement.Task.create()).parent(advancement5).criteriaMerger(CriterionMerger.OR).display((ItemConvertible)Items.PUFFERFISH_BUCKET, new TranslatableText("advancements.husbandry.tactical_fishing.title"), new TranslatableText("advancements.husbandry.tactical_fishing.description"), (Identifier)null, AdvancementFrame.TASK, true, true, false).build(consumer, "husbandry/tactical_fishing");
      this.requireAllCatsTamed(Advancement.Task.create()).parent(advancement4).display((ItemConvertible)Items.COD, new TranslatableText("advancements.husbandry.complete_catalogue.title"), new TranslatableText("advancements.husbandry.complete_catalogue.description"), (Identifier)null, AdvancementFrame.CHALLENGE, true, true, false).rewards(AdvancementRewards.Builder.experience(50)).build(consumer, "husbandry/complete_catalogue");
      Advancement.Task.create().parent(advancement).criterion("safely_harvest_honey", (CriterionConditions)ItemUsedOnBlockCriterion.Conditions.create(LocationPredicate.Builder.create().block(BlockPredicate.Builder.create().method_29233(BlockTags.BEEHIVES).build()).smokey(true), ItemPredicate.Builder.create().item(Items.GLASS_BOTTLE))).display((ItemConvertible)Items.HONEY_BOTTLE, new TranslatableText("advancements.husbandry.safely_harvest_honey.title"), new TranslatableText("advancements.husbandry.safely_harvest_honey.description"), (Identifier)null, AdvancementFrame.TASK, true, true, false).build(consumer, "husbandry/safely_harvest_honey");
      Advancement.Task.create().parent(advancement).criterion("silk_touch_nest", (CriterionConditions)BeeNestDestroyedCriterion.Conditions.create(Blocks.BEE_NEST, ItemPredicate.Builder.create().enchantment(new EnchantmentPredicate(Enchantments.SILK_TOUCH, NumberRange.IntRange.atLeast(1))), NumberRange.IntRange.exactly(3))).display((ItemConvertible)Blocks.BEE_NEST, new TranslatableText("advancements.husbandry.silk_touch_nest.title"), new TranslatableText("advancements.husbandry.silk_touch_nest.description"), (Identifier)null, AdvancementFrame.TASK, true, true, false).build(consumer, "husbandry/silk_touch_nest");
   }

   private Advancement.Task requireFoodItemsEaten(Advancement.Task task) {
      Item[] var2 = FOOD_ITEMS;
      int var3 = var2.length;

      for(int var4 = 0; var4 < var3; ++var4) {
         Item item = var2[var4];
         task.criterion(Registry.ITEM.getId(item).getPath(), (CriterionConditions)ConsumeItemCriterion.Conditions.item(item));
      }

      return task;
   }

   private Advancement.Task requireListedAnimalsBred(Advancement.Task task) {
      EntityType[] var2 = BREEDABLE_ANIMALS;
      int var3 = var2.length;

      for(int var4 = 0; var4 < var3; ++var4) {
         EntityType<?> entityType = var2[var4];
         task.criterion(EntityType.getId(entityType).toString(), (CriterionConditions)BredAnimalsCriterion.Conditions.create(EntityPredicate.Builder.create().type(entityType)));
      }

      task.criterion(EntityType.getId(EntityType.TURTLE).toString(), (CriterionConditions)BredAnimalsCriterion.Conditions.method_29918(EntityPredicate.Builder.create().type(EntityType.TURTLE).build(), EntityPredicate.Builder.create().type(EntityType.TURTLE).build(), EntityPredicate.ANY));
      return task;
   }

   private Advancement.Task requireListedFishBucketsFilled(Advancement.Task task) {
      Item[] var2 = FISH_BUCKET_ITEMS;
      int var3 = var2.length;

      for(int var4 = 0; var4 < var3; ++var4) {
         Item item = var2[var4];
         task.criterion(Registry.ITEM.getId(item).getPath(), (CriterionConditions)FilledBucketCriterion.Conditions.create(ItemPredicate.Builder.create().item(item).build()));
      }

      return task;
   }

   private Advancement.Task requireListedFishCaught(Advancement.Task task) {
      Item[] var2 = FISH_ITEMS;
      int var3 = var2.length;

      for(int var4 = 0; var4 < var3; ++var4) {
         Item item = var2[var4];
         task.criterion(Registry.ITEM.getId(item).getPath(), (CriterionConditions)FishingRodHookedCriterion.Conditions.create(ItemPredicate.ANY, EntityPredicate.ANY, ItemPredicate.Builder.create().item(item).build()));
      }

      return task;
   }

   private Advancement.Task requireAllCatsTamed(Advancement.Task task) {
      CatEntity.TEXTURES.forEach((integer, identifier) -> {
         task.criterion(identifier.getPath(), (CriterionConditions)TameAnimalCriterion.Conditions.create(EntityPredicate.Builder.create().type(identifier).build()));
      });
      return task;
   }

   static {
      BREEDABLE_ANIMALS = new EntityType[]{EntityType.HORSE, EntityType.DONKEY, EntityType.MULE, EntityType.SHEEP, EntityType.COW, EntityType.MOOSHROOM, EntityType.PIG, EntityType.CHICKEN, EntityType.WOLF, EntityType.OCELOT, EntityType.RABBIT, EntityType.LLAMA, EntityType.CAT, EntityType.PANDA, EntityType.FOX, EntityType.BEE, EntityType.HOGLIN, EntityType.STRIDER};
      FISH_ITEMS = new Item[]{Items.COD, Items.TROPICAL_FISH, Items.PUFFERFISH, Items.SALMON};
      FISH_BUCKET_ITEMS = new Item[]{Items.COD_BUCKET, Items.TROPICAL_FISH_BUCKET, Items.PUFFERFISH_BUCKET, Items.SALMON_BUCKET};
      FOOD_ITEMS = new Item[]{Items.APPLE, Items.MUSHROOM_STEW, Items.BREAD, Items.PORKCHOP, Items.COOKED_PORKCHOP, Items.GOLDEN_APPLE, Items.ENCHANTED_GOLDEN_APPLE, Items.COD, Items.SALMON, Items.TROPICAL_FISH, Items.PUFFERFISH, Items.COOKED_COD, Items.COOKED_SALMON, Items.COOKIE, Items.MELON_SLICE, Items.BEEF, Items.COOKED_BEEF, Items.CHICKEN, Items.COOKED_CHICKEN, Items.ROTTEN_FLESH, Items.SPIDER_EYE, Items.CARROT, Items.POTATO, Items.BAKED_POTATO, Items.POISONOUS_POTATO, Items.GOLDEN_CARROT, Items.PUMPKIN_PIE, Items.RABBIT, Items.COOKED_RABBIT, Items.RABBIT_STEW, Items.MUTTON, Items.COOKED_MUTTON, Items.CHORUS_FRUIT, Items.BEETROOT, Items.BEETROOT_SOUP, Items.DRIED_KELP, Items.SUSPICIOUS_STEW, Items.SWEET_BERRIES, Items.HONEY_BOTTLE};
   }
}
