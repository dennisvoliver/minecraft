package net.minecraft.client.gui.screen.recipebook;

import java.util.Set;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.item.Item;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;

@Environment(EnvType.CLIENT)
public class FurnaceRecipeBookScreen extends AbstractFurnaceRecipeBookScreen {
   private static final Text TOGGLE_SMELTABLE_RECIPES_TEXT = new TranslatableText("gui.recipebook.toggleRecipes.smeltable");

   protected Text getToggleCraftableButtonText() {
      return TOGGLE_SMELTABLE_RECIPES_TEXT;
   }

   protected Set<Item> getAllowedFuels() {
      return AbstractFurnaceBlockEntity.createFuelTimeMap().keySet();
   }
}
