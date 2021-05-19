package net.minecraft.client.gui.screen;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.util.NarratorManager;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;

@Environment(EnvType.CLIENT)
public class DownloadingTerrainScreen extends Screen {
   private static final Text TEXT = new TranslatableText("multiplayer.downloadingTerrain");

   public DownloadingTerrainScreen() {
      super(NarratorManager.EMPTY);
   }

   public boolean shouldCloseOnEsc() {
      return false;
   }

   public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
      this.renderBackgroundTexture(0);
      drawCenteredText(matrices, this.textRenderer, TEXT, this.width / 2, this.height / 2 - 50, 16777215);
      super.render(matrices, mouseX, mouseY, delta);
   }

   public boolean isPauseScreen() {
      return false;
   }
}
