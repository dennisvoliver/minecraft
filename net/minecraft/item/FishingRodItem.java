package net.minecraft.item;

import java.util.function.Consumer;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.FishingBobberEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

public class FishingRodItem extends Item implements Vanishable {
   public FishingRodItem(Item.Settings settings) {
      super(settings);
   }

   public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
      ItemStack itemStack = user.getStackInHand(hand);
      int i;
      if (user.fishHook != null) {
         if (!world.isClient) {
            i = user.fishHook.use(itemStack);
            itemStack.damage(i, (LivingEntity)user, (Consumer)((p) -> {
               p.sendToolBreakStatus(hand);
            }));
         }

         world.playSound((PlayerEntity)null, user.getX(), user.getY(), user.getZ(), SoundEvents.ENTITY_FISHING_BOBBER_RETRIEVE, SoundCategory.NEUTRAL, 1.0F, 0.4F / (RANDOM.nextFloat() * 0.4F + 0.8F));
      } else {
         world.playSound((PlayerEntity)null, user.getX(), user.getY(), user.getZ(), SoundEvents.ENTITY_FISHING_BOBBER_THROW, SoundCategory.NEUTRAL, 0.5F, 0.4F / (RANDOM.nextFloat() * 0.4F + 0.8F));
         if (!world.isClient) {
            i = EnchantmentHelper.getLure(itemStack);
            int k = EnchantmentHelper.getLuckOfTheSea(itemStack);
            world.spawnEntity(new FishingBobberEntity(user, world, k, i));
         }

         user.incrementStat(Stats.USED.getOrCreateStat(this));
      }

      return TypedActionResult.success(itemStack, world.isClient());
   }

   public int getEnchantability() {
      return 1;
   }
}
