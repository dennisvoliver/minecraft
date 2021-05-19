package net.minecraft.item;

import java.util.Iterator;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionUtil;
import net.minecraft.potion.Potions;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.stat.Stats;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.UseAction;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class PotionItem extends Item {
   public PotionItem(Item.Settings settings) {
      super(settings);
   }

   public ItemStack getDefaultStack() {
      return PotionUtil.setPotion(super.getDefaultStack(), Potions.WATER);
   }

   public ItemStack finishUsing(ItemStack stack, World world, LivingEntity user) {
      PlayerEntity playerEntity = user instanceof PlayerEntity ? (PlayerEntity)user : null;
      if (playerEntity instanceof ServerPlayerEntity) {
         Criteria.CONSUME_ITEM.trigger((ServerPlayerEntity)playerEntity, stack);
      }

      if (!world.isClient) {
         List<StatusEffectInstance> list = PotionUtil.getPotionEffects(stack);
         Iterator var6 = list.iterator();

         while(var6.hasNext()) {
            StatusEffectInstance statusEffectInstance = (StatusEffectInstance)var6.next();
            if (statusEffectInstance.getEffectType().isInstant()) {
               statusEffectInstance.getEffectType().applyInstantEffect(playerEntity, playerEntity, user, statusEffectInstance.getAmplifier(), 1.0D);
            } else {
               user.addStatusEffect(new StatusEffectInstance(statusEffectInstance));
            }
         }
      }

      if (playerEntity != null) {
         playerEntity.incrementStat(Stats.USED.getOrCreateStat(this));
         if (!playerEntity.abilities.creativeMode) {
            stack.decrement(1);
         }
      }

      if (playerEntity == null || !playerEntity.abilities.creativeMode) {
         if (stack.isEmpty()) {
            return new ItemStack(Items.GLASS_BOTTLE);
         }

         if (playerEntity != null) {
            playerEntity.inventory.insertStack(new ItemStack(Items.GLASS_BOTTLE));
         }
      }

      return stack;
   }

   public int getMaxUseTime(ItemStack stack) {
      return 32;
   }

   public UseAction getUseAction(ItemStack stack) {
      return UseAction.DRINK;
   }

   public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
      return ItemUsage.consumeHeldItem(world, user, hand);
   }

   public String getTranslationKey(ItemStack stack) {
      return PotionUtil.getPotion(stack).finishTranslationKey(this.getTranslationKey() + ".effect.");
   }

   @Environment(EnvType.CLIENT)
   public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
      PotionUtil.buildTooltip(stack, tooltip, 1.0F);
   }

   public boolean hasGlint(ItemStack stack) {
      return super.hasGlint(stack) || !PotionUtil.getPotionEffects(stack).isEmpty();
   }

   public void appendStacks(ItemGroup group, DefaultedList<ItemStack> stacks) {
      if (this.isIn(group)) {
         Iterator var3 = Registry.POTION.iterator();

         while(var3.hasNext()) {
            Potion potion = (Potion)var3.next();
            if (potion != Potions.EMPTY) {
               stacks.add(PotionUtil.setPotion(new ItemStack(this), potion));
            }
         }
      }

   }
}
