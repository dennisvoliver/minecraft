package net.minecraft.block.entity;

import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.RecipeType;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.SmokerScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;

public class SmokerBlockEntity extends AbstractFurnaceBlockEntity {
   public SmokerBlockEntity() {
      super(BlockEntityType.SMOKER, RecipeType.SMOKING);
   }

   protected Text getContainerName() {
      return new TranslatableText("container.smoker");
   }

   protected int getFuelTime(ItemStack fuel) {
      return super.getFuelTime(fuel) / 2;
   }

   protected ScreenHandler createScreenHandler(int syncId, PlayerInventory playerInventory) {
      return new SmokerScreenHandler(syncId, playerInventory, this, this.propertyDelegate);
   }
}
