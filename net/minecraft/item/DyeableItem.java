package net.minecraft.item;

import java.util.Iterator;
import java.util.List;
import net.minecraft.nbt.CompoundTag;

public interface DyeableItem {
   default boolean hasColor(ItemStack stack) {
      CompoundTag compoundTag = stack.getSubTag("display");
      return compoundTag != null && compoundTag.contains("color", 99);
   }

   default int getColor(ItemStack stack) {
      CompoundTag compoundTag = stack.getSubTag("display");
      return compoundTag != null && compoundTag.contains("color", 99) ? compoundTag.getInt("color") : 10511680;
   }

   default void removeColor(ItemStack stack) {
      CompoundTag compoundTag = stack.getSubTag("display");
      if (compoundTag != null && compoundTag.contains("color")) {
         compoundTag.remove("color");
      }

   }

   default void setColor(ItemStack stack, int color) {
      stack.getOrCreateSubTag("display").putInt("color", color);
   }

   static ItemStack blendAndSetColor(ItemStack stack, List<DyeItem> colors) {
      ItemStack itemStack = ItemStack.EMPTY;
      int[] is = new int[3];
      int i = 0;
      int j = 0;
      DyeableItem dyeableItem = null;
      Item item = stack.getItem();
      int o;
      float r;
      int n;
      if (item instanceof DyeableItem) {
         dyeableItem = (DyeableItem)item;
         itemStack = stack.copy();
         itemStack.setCount(1);
         if (dyeableItem.hasColor(stack)) {
            o = dyeableItem.getColor(itemStack);
            float f = (float)(o >> 16 & 255) / 255.0F;
            float g = (float)(o >> 8 & 255) / 255.0F;
            r = (float)(o & 255) / 255.0F;
            i = (int)((float)i + Math.max(f, Math.max(g, r)) * 255.0F);
            is[0] = (int)((float)is[0] + f * 255.0F);
            is[1] = (int)((float)is[1] + g * 255.0F);
            is[2] = (int)((float)is[2] + r * 255.0F);
            ++j;
         }

         for(Iterator var14 = colors.iterator(); var14.hasNext(); ++j) {
            DyeItem dyeItem = (DyeItem)var14.next();
            float[] fs = dyeItem.getColor().getColorComponents();
            int l = (int)(fs[0] * 255.0F);
            int m = (int)(fs[1] * 255.0F);
            n = (int)(fs[2] * 255.0F);
            i += Math.max(l, Math.max(m, n));
            is[0] += l;
            is[1] += m;
            is[2] += n;
         }
      }

      if (dyeableItem == null) {
         return ItemStack.EMPTY;
      } else {
         o = is[0] / j;
         int p = is[1] / j;
         int q = is[2] / j;
         r = (float)i / (float)j;
         float s = (float)Math.max(o, Math.max(p, q));
         o = (int)((float)o * r / s);
         p = (int)((float)p * r / s);
         q = (int)((float)q * r / s);
         n = (o << 8) + p;
         n = (n << 8) + q;
         dyeableItem.setColor(itemStack, n);
         return itemStack;
      }
   }
}
