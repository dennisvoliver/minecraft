package net.minecraft.block.entity;

import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.RecipeType;
import net.minecraft.screen.BlastFurnaceScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;

public class BlastFurnaceBlockEntity extends AbstractFurnaceBlockEntity {
   public BlastFurnaceBlockEntity() {
      super(BlockEntityType.BLAST_FURNACE, RecipeType.BLASTING);
   }

   protected Text getContainerName() {
      return new TranslatableText("container.blast_furnace");
   }

   protected int getFuelTime(ItemStack fuel) {
      return super.getFuelTime(fuel) / 2;
   }

   protected ScreenHandler createScreenHandler(int syncId, PlayerInventory playerInventory) {
      return new BlastFurnaceScreenHandler(syncId, playerInventory, this, this.propertyDelegate);
   }
}
