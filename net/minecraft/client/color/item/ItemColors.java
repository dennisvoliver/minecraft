package net.minecraft.client.color.item;

import java.util.Iterator;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.color.world.GrassColors;
import net.minecraft.item.BlockItem;
import net.minecraft.item.DyeableItem;
import net.minecraft.item.FilledMapItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.potion.PotionUtil;
import net.minecraft.util.collection.IdList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.BlockRenderView;

@Environment(EnvType.CLIENT)
public class ItemColors {
   private final IdList<ItemColorProvider> providers = new IdList(32);

   public static ItemColors create(BlockColors blockColors) {
      ItemColors itemColors = new ItemColors();
      itemColors.register((stack, tintIndex) -> {
         return tintIndex > 0 ? -1 : ((DyeableItem)stack.getItem()).getColor(stack);
      }, Items.LEATHER_HELMET, Items.LEATHER_CHESTPLATE, Items.LEATHER_LEGGINGS, Items.LEATHER_BOOTS, Items.LEATHER_HORSE_ARMOR);
      itemColors.register((stack, tintIndex) -> {
         return GrassColors.getColor(0.5D, 1.0D);
      }, Blocks.TALL_GRASS, Blocks.LARGE_FERN);
      itemColors.register((stack, tintIndex) -> {
         if (tintIndex != 1) {
            return -1;
         } else {
            CompoundTag compoundTag = stack.getSubTag("Explosion");
            int[] is = compoundTag != null && compoundTag.contains("Colors", 11) ? compoundTag.getIntArray("Colors") : null;
            if (is != null && is.length != 0) {
               if (is.length == 1) {
                  return is[0];
               } else {
                  int i = 0;
                  int j = 0;
                  int k = 0;
                  int[] var7 = is;
                  int var8 = is.length;

                  for(int var9 = 0; var9 < var8; ++var9) {
                     int l = var7[var9];
                     i += (l & 16711680) >> 16;
                     j += (l & '\uff00') >> 8;
                     k += (l & 255) >> 0;
                  }

                  i /= is.length;
                  j /= is.length;
                  k /= is.length;
                  return i << 16 | j << 8 | k;
               }
            } else {
               return 9079434;
            }
         }
      }, Items.FIREWORK_STAR);
      itemColors.register((stack, tintIndex) -> {
         return tintIndex > 0 ? -1 : PotionUtil.getColor(stack);
      }, Items.POTION, Items.SPLASH_POTION, Items.LINGERING_POTION);
      Iterator var2 = SpawnEggItem.getAll().iterator();

      while(var2.hasNext()) {
         SpawnEggItem spawnEggItem = (SpawnEggItem)var2.next();
         itemColors.register((stack, tintIndex) -> {
            return spawnEggItem.getColor(tintIndex);
         }, spawnEggItem);
      }

      itemColors.register((stack, tintIndex) -> {
         BlockState blockState = ((BlockItem)stack.getItem()).getBlock().getDefaultState();
         return blockColors.getColor(blockState, (BlockRenderView)null, (BlockPos)null, tintIndex);
      }, Blocks.GRASS_BLOCK, Blocks.GRASS, Blocks.FERN, Blocks.VINE, Blocks.OAK_LEAVES, Blocks.SPRUCE_LEAVES, Blocks.BIRCH_LEAVES, Blocks.JUNGLE_LEAVES, Blocks.ACACIA_LEAVES, Blocks.DARK_OAK_LEAVES, Blocks.LILY_PAD);
      itemColors.register((stack, tintIndex) -> {
         return tintIndex == 0 ? PotionUtil.getColor(stack) : -1;
      }, Items.TIPPED_ARROW);
      itemColors.register((stack, tintIndex) -> {
         return tintIndex == 0 ? -1 : FilledMapItem.getMapColor(stack);
      }, Items.FILLED_MAP);
      return itemColors;
   }

   public int getColorMultiplier(ItemStack item, int tintIndex) {
      ItemColorProvider itemColorProvider = (ItemColorProvider)this.providers.get(Registry.ITEM.getRawId(item.getItem()));
      return itemColorProvider == null ? -1 : itemColorProvider.getColor(item, tintIndex);
   }

   public void register(ItemColorProvider mapper, ItemConvertible... items) {
      ItemConvertible[] var3 = items;
      int var4 = items.length;

      for(int var5 = 0; var5 < var4; ++var5) {
         ItemConvertible itemConvertible = var3[var5];
         this.providers.set(mapper, Item.getRawId(itemConvertible.asItem()));
      }

   }
}
