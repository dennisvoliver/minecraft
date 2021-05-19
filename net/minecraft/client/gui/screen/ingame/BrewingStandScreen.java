package net.minecraft.client.gui.screen.ingame;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.BrewingStandScreenHandler;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

@Environment(EnvType.CLIENT)
public class BrewingStandScreen extends HandledScreen<BrewingStandScreenHandler> {
   private static final Identifier TEXTURE = new Identifier("textures/gui/container/brewing_stand.png");
   private static final int[] BUBBLE_PROGRESS = new int[]{29, 24, 20, 16, 11, 6, 0};

   public BrewingStandScreen(BrewingStandScreenHandler handler, PlayerInventory inventory, Text title) {
      super(handler, inventory, title);
   }

   protected void init() {
      super.init();
      this.titleX = (this.backgroundWidth - this.textRenderer.getWidth((StringVisitable)this.title)) / 2;
   }

   public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
      this.renderBackground(matrices);
      super.render(matrices, mouseX, mouseY, delta);
      this.drawMouseoverTooltip(matrices, mouseX, mouseY);
   }

   protected void drawBackground(MatrixStack matrices, float delta, int mouseX, int mouseY) {
      RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
      this.client.getTextureManager().bindTexture(TEXTURE);
      int i = (this.width - this.backgroundWidth) / 2;
      int j = (this.height - this.backgroundHeight) / 2;
      this.drawTexture(matrices, i, j, 0, 0, this.backgroundWidth, this.backgroundHeight);
      int k = ((BrewingStandScreenHandler)this.handler).getFuel();
      int l = MathHelper.clamp((18 * k + 20 - 1) / 20, 0, 18);
      if (l > 0) {
         this.drawTexture(matrices, i + 60, j + 44, 176, 29, l, 4);
      }

      int m = ((BrewingStandScreenHandler)this.handler).getBrewTime();
      if (m > 0) {
         int n = (int)(28.0F * (1.0F - (float)m / 400.0F));
         if (n > 0) {
            this.drawTexture(matrices, i + 97, j + 16, 176, 0, 9, n);
         }

         n = BUBBLE_PROGRESS[m / 2 % 7];
         if (n > 0) {
            this.drawTexture(matrices, i + 63, j + 14 + 29 - n, 185, 29 - n, 12, n);
         }
      }

   }
}
