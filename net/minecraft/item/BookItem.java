package net.minecraft.item;

public class BookItem extends Item {
   public BookItem(Item.Settings settings) {
      super(settings);
   }

   public boolean isEnchantable(ItemStack stack) {
      return stack.getCount() == 1;
   }

   public int getEnchantability() {
      return 1;
   }
}
