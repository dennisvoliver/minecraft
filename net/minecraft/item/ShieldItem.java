package net.minecraft.item;

import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.DispenserBlock;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.tag.ItemTags;
import net.minecraft.text.Text;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.UseAction;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class ShieldItem extends Item {
   public ShieldItem(Item.Settings settings) {
      super(settings);
      DispenserBlock.registerBehavior(this, ArmorItem.DISPENSER_BEHAVIOR);
   }

   public String getTranslationKey(ItemStack stack) {
      return stack.getSubTag("BlockEntityTag") != null ? this.getTranslationKey() + '.' + getColor(stack).getName() : super.getTranslationKey(stack);
   }

   @Environment(EnvType.CLIENT)
   public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
      BannerItem.appendBannerTooltip(stack, tooltip);
   }

   public UseAction getUseAction(ItemStack stack) {
      return UseAction.BLOCK;
   }

   public int getMaxUseTime(ItemStack stack) {
      return 72000;
   }

   public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
      ItemStack itemStack = user.getStackInHand(hand);
      user.setCurrentHand(hand);
      return TypedActionResult.consume(itemStack);
   }

   public boolean canRepair(ItemStack stack, ItemStack ingredient) {
      return ItemTags.PLANKS.contains(ingredient.getItem()) || super.canRepair(stack, ingredient);
   }

   public static DyeColor getColor(ItemStack stack) {
      return DyeColor.byId(stack.getOrCreateSubTag("BlockEntityTag").getInt("Base"));
   }
}
