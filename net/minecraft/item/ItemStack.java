package net.minecraft.item;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.gson.JsonParseException;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.datafixers.util.Function3;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.Map.Entry;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.pattern.CachedBlockPosition;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.command.argument.BlockArgumentParser;
import net.minecraft.command.argument.BlockPredicateArgumentType;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.enchantment.UnbreakingEnchantment;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityGroup;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.decoration.ItemFrameEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvent;
import net.minecraft.stat.Stats;
import net.minecraft.tag.BlockTags;
import net.minecraft.tag.Tag;
import net.minecraft.tag.TagManager;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.Texts;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.UseAction;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

public final class ItemStack {
   public static final Codec<ItemStack> CODEC = RecordCodecBuilder.create((instance) -> {
      return instance.group(Registry.ITEM.fieldOf("id").forGetter((itemStack) -> {
         return itemStack.item;
      }), Codec.INT.fieldOf("Count").forGetter((itemStack) -> {
         return itemStack.count;
      }), CompoundTag.CODEC.optionalFieldOf("tag").forGetter((itemStack) -> {
         return Optional.ofNullable(itemStack.tag);
      })).apply(instance, (Function3)(ItemStack::new));
   });
   private static final Logger LOGGER = LogManager.getLogger();
   public static final ItemStack EMPTY = new ItemStack((Item)null);
   public static final DecimalFormat MODIFIER_FORMAT = (DecimalFormat)Util.make(new DecimalFormat("#.##"), (decimalFormat) -> {
      decimalFormat.setDecimalFormatSymbols(DecimalFormatSymbols.getInstance(Locale.ROOT));
   });
   private static final Style LORE_STYLE;
   private int count;
   private int cooldown;
   @Deprecated
   private final Item item;
   private CompoundTag tag;
   private boolean empty;
   private Entity holder;
   private CachedBlockPosition lastDestroyPos;
   private boolean lastDestroyResult;
   private CachedBlockPosition lastPlaceOnPos;
   private boolean lastPlaceOnResult;

   public ItemStack(ItemConvertible item) {
      this(item, 1);
   }

   private ItemStack(ItemConvertible itemConvertible, int count, Optional<CompoundTag> optional) {
      this(itemConvertible, count);
      optional.ifPresent(this::setTag);
   }

   public ItemStack(ItemConvertible item, int count) {
      this.item = item == null ? null : item.asItem();
      this.count = count;
      if (this.item != null && this.item.isDamageable()) {
         this.setDamage(this.getDamage());
      }

      this.updateEmptyState();
   }

   private void updateEmptyState() {
      this.empty = false;
      this.empty = this.isEmpty();
   }

   private ItemStack(CompoundTag tag) {
      this.item = (Item)Registry.ITEM.get(new Identifier(tag.getString("id")));
      this.count = tag.getByte("Count");
      if (tag.contains("tag", 10)) {
         this.tag = tag.getCompound("tag");
         this.getItem().postProcessTag(tag);
      }

      if (this.getItem().isDamageable()) {
         this.setDamage(this.getDamage());
      }

      this.updateEmptyState();
   }

   public static ItemStack fromTag(CompoundTag tag) {
      try {
         return new ItemStack(tag);
      } catch (RuntimeException var2) {
         LOGGER.debug((String)"Tried to load invalid item: {}", (Object)tag, (Object)var2);
         return EMPTY;
      }
   }

   public boolean isEmpty() {
      if (this == EMPTY) {
         return true;
      } else if (this.getItem() != null && this.getItem() != Items.AIR) {
         return this.count <= 0;
      } else {
         return true;
      }
   }

   public ItemStack split(int amount) {
      int i = Math.min(amount, this.count);
      ItemStack itemStack = this.copy();
      itemStack.setCount(i);
      this.decrement(i);
      return itemStack;
   }

   public Item getItem() {
      return this.empty ? Items.AIR : this.item;
   }

   public ActionResult useOnBlock(ItemUsageContext context) {
      PlayerEntity playerEntity = context.getPlayer();
      BlockPos blockPos = context.getBlockPos();
      CachedBlockPosition cachedBlockPosition = new CachedBlockPosition(context.getWorld(), blockPos, false);
      if (playerEntity != null && !playerEntity.abilities.allowModifyWorld && !this.canPlaceOn(context.getWorld().getTagManager(), cachedBlockPosition)) {
         return ActionResult.PASS;
      } else {
         Item item = this.getItem();
         ActionResult actionResult = item.useOnBlock(context);
         if (playerEntity != null && actionResult.isAccepted()) {
            playerEntity.incrementStat(Stats.USED.getOrCreateStat(item));
         }

         return actionResult;
      }
   }

   public float getMiningSpeedMultiplier(BlockState state) {
      return this.getItem().getMiningSpeedMultiplier(this, state);
   }

   public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
      return this.getItem().use(world, user, hand);
   }

   public ItemStack finishUsing(World world, LivingEntity user) {
      return this.getItem().finishUsing(this, world, user);
   }

   public CompoundTag toTag(CompoundTag tag) {
      Identifier identifier = Registry.ITEM.getId(this.getItem());
      tag.putString("id", identifier == null ? "minecraft:air" : identifier.toString());
      tag.putByte("Count", (byte)this.count);
      if (this.tag != null) {
         tag.put("tag", this.tag.copy());
      }

      return tag;
   }

   public int getMaxCount() {
      return this.getItem().getMaxCount();
   }

   public boolean isStackable() {
      return this.getMaxCount() > 1 && (!this.isDamageable() || !this.isDamaged());
   }

   public boolean isDamageable() {
      if (!this.empty && this.getItem().getMaxDamage() > 0) {
         CompoundTag compoundTag = this.getTag();
         return compoundTag == null || !compoundTag.getBoolean("Unbreakable");
      } else {
         return false;
      }
   }

   public boolean isDamaged() {
      return this.isDamageable() && this.getDamage() > 0;
   }

   public int getDamage() {
      return this.tag == null ? 0 : this.tag.getInt("Damage");
   }

   public void setDamage(int damage) {
      this.getOrCreateTag().putInt("Damage", Math.max(0, damage));
   }

   public int getMaxDamage() {
      return this.getItem().getMaxDamage();
   }

   public boolean damage(int amount, Random random, @Nullable ServerPlayerEntity player) {
      if (!this.isDamageable()) {
         return false;
      } else {
         int i;
         if (amount > 0) {
            i = EnchantmentHelper.getLevel(Enchantments.UNBREAKING, this);
            int j = 0;

            for(int k = 0; i > 0 && k < amount; ++k) {
               if (UnbreakingEnchantment.shouldPreventDamage(this, i, random)) {
                  ++j;
               }
            }

            amount -= j;
            if (amount <= 0) {
               return false;
            }
         }

         if (player != null && amount != 0) {
            Criteria.ITEM_DURABILITY_CHANGED.trigger(player, this, this.getDamage() + amount);
         }

         i = this.getDamage() + amount;
         this.setDamage(i);
         return i >= this.getMaxDamage();
      }
   }

   public <T extends LivingEntity> void damage(int amount, T entity, Consumer<T> breakCallback) {
      if (!entity.world.isClient && (!(entity instanceof PlayerEntity) || !((PlayerEntity)entity).abilities.creativeMode)) {
         if (this.isDamageable()) {
            if (this.damage(amount, entity.getRandom(), entity instanceof ServerPlayerEntity ? (ServerPlayerEntity)entity : null)) {
               breakCallback.accept(entity);
               Item item = this.getItem();
               this.decrement(1);
               if (entity instanceof PlayerEntity) {
                  ((PlayerEntity)entity).incrementStat(Stats.BROKEN.getOrCreateStat(item));
               }

               this.setDamage(0);
            }

         }
      }
   }

   public void postHit(LivingEntity target, PlayerEntity attacker) {
      Item item = this.getItem();
      if (item.postHit(this, target, attacker)) {
         attacker.incrementStat(Stats.USED.getOrCreateStat(item));
      }

   }

   public void postMine(World world, BlockState state, BlockPos pos, PlayerEntity miner) {
      Item item = this.getItem();
      if (item.postMine(this, world, state, pos, miner)) {
         miner.incrementStat(Stats.USED.getOrCreateStat(item));
      }

   }

   public boolean isEffectiveOn(BlockState state) {
      return this.getItem().isEffectiveOn(state);
   }

   public ActionResult useOnEntity(PlayerEntity user, LivingEntity entity, Hand hand) {
      return this.getItem().useOnEntity(this, user, entity, hand);
   }

   public ItemStack copy() {
      if (this.isEmpty()) {
         return EMPTY;
      } else {
         ItemStack itemStack = new ItemStack(this.getItem(), this.count);
         itemStack.setCooldown(this.getCooldown());
         if (this.tag != null) {
            itemStack.tag = this.tag.copy();
         }

         return itemStack;
      }
   }

   public static boolean areTagsEqual(ItemStack left, ItemStack right) {
      if (left.isEmpty() && right.isEmpty()) {
         return true;
      } else if (!left.isEmpty() && !right.isEmpty()) {
         if (left.tag == null && right.tag != null) {
            return false;
         } else {
            return left.tag == null || left.tag.equals(right.tag);
         }
      } else {
         return false;
      }
   }

   public static boolean areEqual(ItemStack left, ItemStack right) {
      if (left.isEmpty() && right.isEmpty()) {
         return true;
      } else {
         return !left.isEmpty() && !right.isEmpty() ? left.isEqual(right) : false;
      }
   }

   private boolean isEqual(ItemStack stack) {
      if (this.count != stack.count) {
         return false;
      } else if (this.getItem() != stack.getItem()) {
         return false;
      } else if (this.tag == null && stack.tag != null) {
         return false;
      } else {
         return this.tag == null || this.tag.equals(stack.tag);
      }
   }

   public static boolean areItemsEqualIgnoreDamage(ItemStack left, ItemStack right) {
      if (left == right) {
         return true;
      } else {
         return !left.isEmpty() && !right.isEmpty() ? left.isItemEqualIgnoreDamage(right) : false;
      }
   }

   public static boolean areItemsEqual(ItemStack left, ItemStack right) {
      if (left == right) {
         return true;
      } else {
         return !left.isEmpty() && !right.isEmpty() ? left.isItemEqual(right) : false;
      }
   }

   public boolean isItemEqualIgnoreDamage(ItemStack stack) {
      return !stack.isEmpty() && this.getItem() == stack.getItem();
   }

   public boolean isItemEqual(ItemStack stack) {
      if (!this.isDamageable()) {
         return this.isItemEqualIgnoreDamage(stack);
      } else {
         return !stack.isEmpty() && this.getItem() == stack.getItem();
      }
   }

   public String getTranslationKey() {
      return this.getItem().getTranslationKey(this);
   }

   public String toString() {
      return this.count + " " + this.getItem();
   }

   public void inventoryTick(World world, Entity entity, int slot, boolean selected) {
      if (this.cooldown > 0) {
         --this.cooldown;
      }

      if (this.getItem() != null) {
         this.getItem().inventoryTick(this, world, entity, slot, selected);
      }

   }

   public void onCraft(World world, PlayerEntity player, int amount) {
      player.increaseStat(Stats.CRAFTED.getOrCreateStat(this.getItem()), amount);
      this.getItem().onCraft(this, world, player);
   }

   public int getMaxUseTime() {
      return this.getItem().getMaxUseTime(this);
   }

   public UseAction getUseAction() {
      return this.getItem().getUseAction(this);
   }

   public void onStoppedUsing(World world, LivingEntity user, int remainingUseTicks) {
      this.getItem().onStoppedUsing(this, world, user, remainingUseTicks);
   }

   public boolean isUsedOnRelease() {
      return this.getItem().isUsedOnRelease(this);
   }

   public boolean hasTag() {
      return !this.empty && this.tag != null && !this.tag.isEmpty();
   }

   @Nullable
   public CompoundTag getTag() {
      return this.tag;
   }

   public CompoundTag getOrCreateTag() {
      if (this.tag == null) {
         this.setTag(new CompoundTag());
      }

      return this.tag;
   }

   public CompoundTag getOrCreateSubTag(String key) {
      if (this.tag != null && this.tag.contains(key, 10)) {
         return this.tag.getCompound(key);
      } else {
         CompoundTag compoundTag = new CompoundTag();
         this.putSubTag(key, compoundTag);
         return compoundTag;
      }
   }

   @Nullable
   public CompoundTag getSubTag(String key) {
      return this.tag != null && this.tag.contains(key, 10) ? this.tag.getCompound(key) : null;
   }

   public void removeSubTag(String key) {
      if (this.tag != null && this.tag.contains(key)) {
         this.tag.remove(key);
         if (this.tag.isEmpty()) {
            this.tag = null;
         }
      }

   }

   public ListTag getEnchantments() {
      return this.tag != null ? this.tag.getList("Enchantments", 10) : new ListTag();
   }

   public void setTag(@Nullable CompoundTag tag) {
      this.tag = tag;
      if (this.getItem().isDamageable()) {
         this.setDamage(this.getDamage());
      }

   }

   public Text getName() {
      CompoundTag compoundTag = this.getSubTag("display");
      if (compoundTag != null && compoundTag.contains("Name", 8)) {
         try {
            Text text = Text.Serializer.fromJson(compoundTag.getString("Name"));
            if (text != null) {
               return text;
            }

            compoundTag.remove("Name");
         } catch (JsonParseException var3) {
            compoundTag.remove("Name");
         }
      }

      return this.getItem().getName(this);
   }

   public ItemStack setCustomName(@Nullable Text name) {
      CompoundTag compoundTag = this.getOrCreateSubTag("display");
      if (name != null) {
         compoundTag.putString("Name", Text.Serializer.toJson(name));
      } else {
         compoundTag.remove("Name");
      }

      return this;
   }

   public void removeCustomName() {
      CompoundTag compoundTag = this.getSubTag("display");
      if (compoundTag != null) {
         compoundTag.remove("Name");
         if (compoundTag.isEmpty()) {
            this.removeSubTag("display");
         }
      }

      if (this.tag != null && this.tag.isEmpty()) {
         this.tag = null;
      }

   }

   public boolean hasCustomName() {
      CompoundTag compoundTag = this.getSubTag("display");
      return compoundTag != null && compoundTag.contains("Name", 8);
   }

   @Environment(EnvType.CLIENT)
   public List<Text> getTooltip(@Nullable PlayerEntity player, TooltipContext context) {
      List<Text> list = Lists.newArrayList();
      MutableText mutableText = (new LiteralText("")).append(this.getName()).formatted(this.getRarity().formatting);
      if (this.hasCustomName()) {
         mutableText.formatted(Formatting.ITALIC);
      }

      list.add(mutableText);
      if (!context.isAdvanced() && !this.hasCustomName() && this.getItem() == Items.FILLED_MAP) {
         list.add((new LiteralText("#" + FilledMapItem.getMapId(this))).formatted(Formatting.GRAY));
      }

      int i = this.getHideFlags();
      if (isSectionHidden(i, ItemStack.TooltipSection.ADDITIONAL)) {
         this.getItem().appendTooltip(this, player == null ? null : player.world, list, context);
      }

      int j;
      if (this.hasTag()) {
         if (isSectionHidden(i, ItemStack.TooltipSection.ENCHANTMENTS)) {
            appendEnchantments(list, this.getEnchantments());
         }

         if (this.tag.contains("display", 10)) {
            CompoundTag compoundTag = this.tag.getCompound("display");
            if (isSectionHidden(i, ItemStack.TooltipSection.DYE) && compoundTag.contains("color", 99)) {
               if (context.isAdvanced()) {
                  list.add((new TranslatableText("item.color", new Object[]{String.format("#%06X", compoundTag.getInt("color"))})).formatted(Formatting.GRAY));
               } else {
                  list.add((new TranslatableText("item.dyed")).formatted(new Formatting[]{Formatting.GRAY, Formatting.ITALIC}));
               }
            }

            if (compoundTag.getType("Lore") == 9) {
               ListTag listTag = compoundTag.getList("Lore", 8);

               for(j = 0; j < listTag.size(); ++j) {
                  String string = listTag.getString(j);

                  try {
                     MutableText mutableText2 = Text.Serializer.fromJson(string);
                     if (mutableText2 != null) {
                        list.add(Texts.setStyleIfAbsent(mutableText2, LORE_STYLE));
                     }
                  } catch (JsonParseException var19) {
                     compoundTag.remove("Lore");
                  }
               }
            }
         }
      }

      int l;
      if (isSectionHidden(i, ItemStack.TooltipSection.MODIFIERS)) {
         EquipmentSlot[] var20 = EquipmentSlot.values();
         l = var20.length;

         for(j = 0; j < l; ++j) {
            EquipmentSlot equipmentSlot = var20[j];
            Multimap<EntityAttribute, EntityAttributeModifier> multimap = this.getAttributeModifiers(equipmentSlot);
            if (!multimap.isEmpty()) {
               list.add(LiteralText.EMPTY);
               list.add((new TranslatableText("item.modifiers." + equipmentSlot.getName())).formatted(Formatting.GRAY));
               Iterator var11 = multimap.entries().iterator();

               while(var11.hasNext()) {
                  Entry<EntityAttribute, EntityAttributeModifier> entry = (Entry)var11.next();
                  EntityAttributeModifier entityAttributeModifier = (EntityAttributeModifier)entry.getValue();
                  double d = entityAttributeModifier.getValue();
                  boolean bl = false;
                  if (player != null) {
                     if (entityAttributeModifier.getId() == Item.ATTACK_DAMAGE_MODIFIER_ID) {
                        d += player.getAttributeBaseValue(EntityAttributes.GENERIC_ATTACK_DAMAGE);
                        d += (double)EnchantmentHelper.getAttackDamage(this, EntityGroup.DEFAULT);
                        bl = true;
                     } else if (entityAttributeModifier.getId() == Item.ATTACK_SPEED_MODIFIER_ID) {
                        d += player.getAttributeBaseValue(EntityAttributes.GENERIC_ATTACK_SPEED);
                        bl = true;
                     }
                  }

                  double g;
                  if (entityAttributeModifier.getOperation() != EntityAttributeModifier.Operation.MULTIPLY_BASE && entityAttributeModifier.getOperation() != EntityAttributeModifier.Operation.MULTIPLY_TOTAL) {
                     if (((EntityAttribute)entry.getKey()).equals(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE)) {
                        g = d * 10.0D;
                     } else {
                        g = d;
                     }
                  } else {
                     g = d * 100.0D;
                  }

                  if (bl) {
                     list.add((new LiteralText(" ")).append(new TranslatableText("attribute.modifier.equals." + entityAttributeModifier.getOperation().getId(), new Object[]{MODIFIER_FORMAT.format(g), new TranslatableText(((EntityAttribute)entry.getKey()).getTranslationKey())})).formatted(Formatting.DARK_GREEN));
                  } else if (d > 0.0D) {
                     list.add((new TranslatableText("attribute.modifier.plus." + entityAttributeModifier.getOperation().getId(), new Object[]{MODIFIER_FORMAT.format(g), new TranslatableText(((EntityAttribute)entry.getKey()).getTranslationKey())})).formatted(Formatting.BLUE));
                  } else if (d < 0.0D) {
                     g *= -1.0D;
                     list.add((new TranslatableText("attribute.modifier.take." + entityAttributeModifier.getOperation().getId(), new Object[]{MODIFIER_FORMAT.format(g), new TranslatableText(((EntityAttribute)entry.getKey()).getTranslationKey())})).formatted(Formatting.RED));
                  }
               }
            }
         }
      }

      if (this.hasTag()) {
         if (isSectionHidden(i, ItemStack.TooltipSection.UNBREAKABLE) && this.tag.getBoolean("Unbreakable")) {
            list.add((new TranslatableText("item.unbreakable")).formatted(Formatting.BLUE));
         }

         ListTag listTag3;
         if (isSectionHidden(i, ItemStack.TooltipSection.CAN_DESTROY) && this.tag.contains("CanDestroy", 9)) {
            listTag3 = this.tag.getList("CanDestroy", 8);
            if (!listTag3.isEmpty()) {
               list.add(LiteralText.EMPTY);
               list.add((new TranslatableText("item.canBreak")).formatted(Formatting.GRAY));

               for(l = 0; l < listTag3.size(); ++l) {
                  list.addAll(parseBlockTag(listTag3.getString(l)));
               }
            }
         }

         if (isSectionHidden(i, ItemStack.TooltipSection.CAN_PLACE) && this.tag.contains("CanPlaceOn", 9)) {
            listTag3 = this.tag.getList("CanPlaceOn", 8);
            if (!listTag3.isEmpty()) {
               list.add(LiteralText.EMPTY);
               list.add((new TranslatableText("item.canPlace")).formatted(Formatting.GRAY));

               for(l = 0; l < listTag3.size(); ++l) {
                  list.addAll(parseBlockTag(listTag3.getString(l)));
               }
            }
         }
      }

      if (context.isAdvanced()) {
         if (this.isDamaged()) {
            list.add(new TranslatableText("item.durability", new Object[]{this.getMaxDamage() - this.getDamage(), this.getMaxDamage()}));
         }

         list.add((new LiteralText(Registry.ITEM.getId(this.getItem()).toString())).formatted(Formatting.DARK_GRAY));
         if (this.hasTag()) {
            list.add((new TranslatableText("item.nbt_tags", new Object[]{this.tag.getKeys().size()})).formatted(Formatting.DARK_GRAY));
         }
      }

      return list;
   }

   @Environment(EnvType.CLIENT)
   private static boolean isSectionHidden(int flags, ItemStack.TooltipSection tooltipSection) {
      return (flags & tooltipSection.getFlag()) == 0;
   }

   @Environment(EnvType.CLIENT)
   private int getHideFlags() {
      return this.hasTag() && this.tag.contains("HideFlags", 99) ? this.tag.getInt("HideFlags") : 0;
   }

   public void addHideFlag(ItemStack.TooltipSection tooltipSection) {
      CompoundTag compoundTag = this.getOrCreateTag();
      compoundTag.putInt("HideFlags", compoundTag.getInt("HideFlags") | tooltipSection.getFlag());
   }

   @Environment(EnvType.CLIENT)
   public static void appendEnchantments(List<Text> tooltip, ListTag enchantments) {
      for(int i = 0; i < enchantments.size(); ++i) {
         CompoundTag compoundTag = enchantments.getCompound(i);
         Registry.ENCHANTMENT.getOrEmpty(Identifier.tryParse(compoundTag.getString("id"))).ifPresent((e) -> {
            tooltip.add(e.getName(compoundTag.getInt("lvl")));
         });
      }

   }

   @Environment(EnvType.CLIENT)
   private static Collection<Text> parseBlockTag(String tag) {
      try {
         BlockArgumentParser blockArgumentParser = (new BlockArgumentParser(new StringReader(tag), true)).parse(true);
         BlockState blockState = blockArgumentParser.getBlockState();
         Identifier identifier = blockArgumentParser.getTagId();
         boolean bl = blockState != null;
         boolean bl2 = identifier != null;
         if (bl || bl2) {
            if (bl) {
               return Lists.newArrayList((Object[])(blockState.getBlock().getName().formatted(Formatting.DARK_GRAY)));
            }

            Tag<Block> tag2 = BlockTags.getTagGroup().getTag(identifier);
            if (tag2 != null) {
               Collection<Block> collection = tag2.values();
               if (!collection.isEmpty()) {
                  return (Collection)collection.stream().map(Block::getName).map((text) -> {
                     return text.formatted(Formatting.DARK_GRAY);
                  }).collect(Collectors.toList());
               }
            }
         }
      } catch (CommandSyntaxException var8) {
      }

      return Lists.newArrayList((Object[])((new LiteralText("missingno")).formatted(Formatting.DARK_GRAY)));
   }

   public boolean hasGlint() {
      return this.getItem().hasGlint(this);
   }

   public Rarity getRarity() {
      return this.getItem().getRarity(this);
   }

   public boolean isEnchantable() {
      if (!this.getItem().isEnchantable(this)) {
         return false;
      } else {
         return !this.hasEnchantments();
      }
   }

   public void addEnchantment(Enchantment enchantment, int level) {
      this.getOrCreateTag();
      if (!this.tag.contains("Enchantments", 9)) {
         this.tag.put("Enchantments", new ListTag());
      }

      ListTag listTag = this.tag.getList("Enchantments", 10);
      CompoundTag compoundTag = new CompoundTag();
      compoundTag.putString("id", String.valueOf(Registry.ENCHANTMENT.getId(enchantment)));
      compoundTag.putShort("lvl", (short)((byte)level));
      listTag.add(compoundTag);
   }

   public boolean hasEnchantments() {
      if (this.tag != null && this.tag.contains("Enchantments", 9)) {
         return !this.tag.getList("Enchantments", 10).isEmpty();
      } else {
         return false;
      }
   }

   public void putSubTag(String key, net.minecraft.nbt.Tag tag) {
      this.getOrCreateTag().put(key, tag);
   }

   public boolean isInFrame() {
      return this.holder instanceof ItemFrameEntity;
   }

   public void setHolder(@Nullable Entity holder) {
      this.holder = holder;
   }

   @Nullable
   public ItemFrameEntity getFrame() {
      return this.holder instanceof ItemFrameEntity ? (ItemFrameEntity)this.getHolder() : null;
   }

   @Nullable
   public Entity getHolder() {
      return !this.empty ? this.holder : null;
   }

   public int getRepairCost() {
      return this.hasTag() && this.tag.contains("RepairCost", 3) ? this.tag.getInt("RepairCost") : 0;
   }

   public void setRepairCost(int repairCost) {
      this.getOrCreateTag().putInt("RepairCost", repairCost);
   }

   public Multimap<EntityAttribute, EntityAttributeModifier> getAttributeModifiers(EquipmentSlot equipmentSlot) {
      Object multimap;
      if (this.hasTag() && this.tag.contains("AttributeModifiers", 9)) {
         multimap = HashMultimap.create();
         ListTag listTag = this.tag.getList("AttributeModifiers", 10);

         for(int i = 0; i < listTag.size(); ++i) {
            CompoundTag compoundTag = listTag.getCompound(i);
            if (!compoundTag.contains("Slot", 8) || compoundTag.getString("Slot").equals(equipmentSlot.getName())) {
               Optional<EntityAttribute> optional = Registry.ATTRIBUTE.getOrEmpty(Identifier.tryParse(compoundTag.getString("AttributeName")));
               if (optional.isPresent()) {
                  EntityAttributeModifier entityAttributeModifier = EntityAttributeModifier.fromTag(compoundTag);
                  if (entityAttributeModifier != null && entityAttributeModifier.getId().getLeastSignificantBits() != 0L && entityAttributeModifier.getId().getMostSignificantBits() != 0L) {
                     ((Multimap)multimap).put(optional.get(), entityAttributeModifier);
                  }
               }
            }
         }
      } else {
         multimap = this.getItem().getAttributeModifiers(equipmentSlot);
      }

      return (Multimap)multimap;
   }

   public void addAttributeModifier(EntityAttribute entityAttribute, EntityAttributeModifier modifier, @Nullable EquipmentSlot slot) {
      this.getOrCreateTag();
      if (!this.tag.contains("AttributeModifiers", 9)) {
         this.tag.put("AttributeModifiers", new ListTag());
      }

      ListTag listTag = this.tag.getList("AttributeModifiers", 10);
      CompoundTag compoundTag = modifier.toTag();
      compoundTag.putString("AttributeName", Registry.ATTRIBUTE.getId(entityAttribute).toString());
      if (slot != null) {
         compoundTag.putString("Slot", slot.getName());
      }

      listTag.add(compoundTag);
   }

   public Text toHoverableText() {
      MutableText mutableText = (new LiteralText("")).append(this.getName());
      if (this.hasCustomName()) {
         mutableText.formatted(Formatting.ITALIC);
      }

      MutableText mutableText2 = Texts.bracketed(mutableText);
      if (!this.empty) {
         mutableText2.formatted(this.getRarity().formatting).styled((style) -> {
            return style.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_ITEM, new HoverEvent.ItemStackContent(this)));
         });
      }

      return mutableText2;
   }

   private static boolean areBlocksEqual(CachedBlockPosition first, @Nullable CachedBlockPosition second) {
      if (second != null && first.getBlockState() == second.getBlockState()) {
         if (first.getBlockEntity() == null && second.getBlockEntity() == null) {
            return true;
         } else {
            return first.getBlockEntity() != null && second.getBlockEntity() != null ? Objects.equals(first.getBlockEntity().toTag(new CompoundTag()), second.getBlockEntity().toTag(new CompoundTag())) : false;
         }
      } else {
         return false;
      }
   }

   public boolean canDestroy(TagManager tagManager, CachedBlockPosition pos) {
      if (areBlocksEqual(pos, this.lastDestroyPos)) {
         return this.lastDestroyResult;
      } else {
         this.lastDestroyPos = pos;
         if (this.hasTag() && this.tag.contains("CanDestroy", 9)) {
            ListTag listTag = this.tag.getList("CanDestroy", 8);

            for(int i = 0; i < listTag.size(); ++i) {
               String string = listTag.getString(i);

               try {
                  Predicate<CachedBlockPosition> predicate = BlockPredicateArgumentType.blockPredicate().parse(new StringReader(string)).create(tagManager);
                  if (predicate.test(pos)) {
                     this.lastDestroyResult = true;
                     return true;
                  }
               } catch (CommandSyntaxException var7) {
               }
            }
         }

         this.lastDestroyResult = false;
         return false;
      }
   }

   public boolean canPlaceOn(TagManager tagManager, CachedBlockPosition pos) {
      if (areBlocksEqual(pos, this.lastPlaceOnPos)) {
         return this.lastPlaceOnResult;
      } else {
         this.lastPlaceOnPos = pos;
         if (this.hasTag() && this.tag.contains("CanPlaceOn", 9)) {
            ListTag listTag = this.tag.getList("CanPlaceOn", 8);

            for(int i = 0; i < listTag.size(); ++i) {
               String string = listTag.getString(i);

               try {
                  Predicate<CachedBlockPosition> predicate = BlockPredicateArgumentType.blockPredicate().parse(new StringReader(string)).create(tagManager);
                  if (predicate.test(pos)) {
                     this.lastPlaceOnResult = true;
                     return true;
                  }
               } catch (CommandSyntaxException var7) {
               }
            }
         }

         this.lastPlaceOnResult = false;
         return false;
      }
   }

   public int getCooldown() {
      return this.cooldown;
   }

   public void setCooldown(int cooldown) {
      this.cooldown = cooldown;
   }

   public int getCount() {
      return this.empty ? 0 : this.count;
   }

   public void setCount(int count) {
      this.count = count;
      this.updateEmptyState();
   }

   public void increment(int amount) {
      this.setCount(this.count + amount);
   }

   public void decrement(int amount) {
      this.increment(-amount);
   }

   public void usageTick(World world, LivingEntity user, int remainingUseTicks) {
      this.getItem().usageTick(world, user, this, remainingUseTicks);
   }

   public boolean isFood() {
      return this.getItem().isFood();
   }

   public SoundEvent getDrinkSound() {
      return this.getItem().getDrinkSound();
   }

   public SoundEvent getEatSound() {
      return this.getItem().getEatSound();
   }

   static {
      LORE_STYLE = Style.EMPTY.withColor(Formatting.DARK_PURPLE).withItalic(true);
   }

   public static enum TooltipSection {
      ENCHANTMENTS,
      MODIFIERS,
      UNBREAKABLE,
      CAN_DESTROY,
      CAN_PLACE,
      ADDITIONAL,
      DYE;

      private int flag = 1 << this.ordinal();

      public int getFlag() {
         return this.flag;
      }
   }
}
