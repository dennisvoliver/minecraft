package net.minecraft.item;

import java.util.Iterator;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionUtil;
import net.minecraft.potion.Potions;
import net.minecraft.text.Text;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class TippedArrowItem extends ArrowItem {
   public TippedArrowItem(Item.Settings settings) {
      super(settings);
   }

   public ItemStack getDefaultStack() {
      return PotionUtil.setPotion(super.getDefaultStack(), Potions.POISON);
   }

   public void appendStacks(ItemGroup group, DefaultedList<ItemStack> stacks) {
      if (this.isIn(group)) {
         Iterator var3 = Registry.POTION.iterator();

         while(var3.hasNext()) {
            Potion potion = (Potion)var3.next();
            if (!potion.getEffects().isEmpty()) {
               stacks.add(PotionUtil.setPotion(new ItemStack(this), potion));
            }
         }
      }

   }

   @Environment(EnvType.CLIENT)
   public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
      PotionUtil.buildTooltip(stack, tooltip, 0.125F);
   }

   public String getTranslationKey(ItemStack stack) {
      return PotionUtil.getPotion(stack).finishTranslationKey(this.getTranslationKey() + ".effect.");
   }
}
